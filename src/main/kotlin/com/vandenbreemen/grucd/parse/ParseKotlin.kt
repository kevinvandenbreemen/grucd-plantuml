package com.vandenbreemen.grucd.parse

import com.vandenbreemen.grucd.model.*
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.*
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.common.klass.KlassIdentifier
import kotlinx.ast.common.klass.RawAst
import kotlinx.ast.common.klass.identifierName
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.common.summary.Import
import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.apache.log4j.Logger
import org.apache.log4j.NDC

class ParseKotlin {

    enum class ItemTypeToFind {
        KotlinDoc
    }

    companion object {
        private val logger:Logger = Logger.getLogger(ParseKotlin::class.java)
    }

    private fun findImportList(astList: List<Ast>): List<String>? {

        val result = mutableListOf<String>()
        astList.forEach { ast->
            (ast as? DefaultAstNode)?.let { defaultAstNode ->
                if(defaultAstNode.description == "importList"){
                    defaultAstNode.children.forEach { child->(child as? Import)?.let { importStatement ->
                        result.add(importStatement.identifier.identifierName())
                    } }
                }
            }
        }

        return result
    }

    fun visitAll(ast: Ast, toFind: ItemTypeToFind): Ast? {

        (ast as? AstWithAttachments)?.run {
            logger.trace("Handling attachments for ${ast.javaClass.simpleName} ${ast.description}=~=~=~=~")
            attachments.attachments.entries.forEach { entry->
                when(entry.value) {
                    is RawAst -> visitAll((entry.value as RawAst).ast, toFind)?.let { found-> return found }
                    else -> (entry.value as? Ast)?.run { visitAll(this, toFind)?.let { found->return found } } ?.run { logger.trace("unkn: ${entry.value}") }
                }

            }
            logger.trace("END attachments =~=~=~=~")
        }

        when(ast) {
            is KlassDeclaration -> {
                logger.trace("kw=${ast.keyword}")
                for (child in ast.children) {
                    visitAll(child, toFind)?.let { found->return found }
                }
            }
            is AstNode -> {
                logger.trace("AstNode:  ${ast.description}")
                for (child in ast.children) {
                    visitAll(child, toFind)?.let { found->return found }
                }
            }
            is DefaultAstTerminal -> {
                if(ast.description == "DelimitedComment" ){
                    logger.trace("Found comment ${ast.text}")
                    if(toFind == ItemTypeToFind.KotlinDoc) {
                        return ast
                    }
                }
            }
            else ->
                logger.trace("Unknown: ${ast.description}")
        }

        return null
    }

    fun parse(filePath: String): List<Type> {
        val kotlinFile = KotlinGrammarAntlrKotlinParser.parseKotlinFile(AstSource.File(filePath))
        val result = mutableListOf<Type>()
        kotlinFile.summary(false).onSuccess { astList->

            val imports = findImportList(astList)

            var pkg: PackageHeader? = null

            var classComment: String? = null

            astList.forEach { astItem->

                logger.debug("VISIT TREE FOR ${astItem.description}")
                logger.debug("=======================================")
                if(classComment == null ) { //  Only do this if we haven't yet set a top-level class comment.  Otherwise grab class comments for nested classes from inside
                    visitAll(astItem, ItemTypeToFind.KotlinDoc)?.let { comment->
                        (comment as? DefaultAstTerminal)?.let { commentTerm->
                            classComment = commentTerm.text.replace(Regex("([/][*]+)"), "")
                                .replace(Regex("([*]+[/])"), "")
                                .replace(Regex("^\\s*[*]"), "").trim()
                        }
                    }
                }
                logger.debug("=======================================")

                (astItem as? PackageHeader)?.let {
                    pkg = it
                }

                (astItem as? KlassDeclaration)?.let {

                    val type = Type(it.identifier?.rawName ?: "", pkg?.identifier?.get(0)?.rawName ?: "",
                        if(it.keyword == "interface") {TypeType.Interface } else { TypeType.Class }
                        )
                    type.imports = imports

                    handleClassDeclaration(it, type, result)
                    classComment?.let { comment->type.classDoc = comment }
                    result.add(type)

                }
            }
        }

        return result
    }

    private fun handleClassDeclaration(declaration: KlassDeclaration, type: Type, classList: MutableList<Type>, discoveredClassComment: String? = null) {

        discoveredClassComment?.let { previouslyFoundComment->
            type.classDoc = previouslyFoundComment
        }

        val classCommentOnNextNestedType = visitAll(declaration, ItemTypeToFind.KotlinDoc)?.let { comment->
            (comment as? DefaultAstTerminal)?.let { commentTerm->
                commentTerm.text.replace(Regex("([/][*]+)"), "")
                    .replace(Regex("([*]+[/])"), "")
                    .replace(Regex("^\\s*[*]"), "").trim()
            }
        } ?: null

        logger.debug("Parsing ${type.type} ${type.name}...")
        NDC.push(type.name)

        try {
            declaration.inheritance.forEach { klassInheritance ->
                val superTypeName = klassInheritance.type.rawName
                logger.trace("Recognizing superclass $superTypeName")
                type.addSuperType(superTypeName)
            }
            declaration.children.forEach { child ->
                (child as? KlassDeclaration)?.let { kd ->
                    kd.parameter.forEach { parm ->
                        processPropertyDeclaration(parm, type)
                    }
                }
                (child as? DefaultAstNode)?.let { node ->
                    node.children.forEach {
                        (it as? KlassDeclaration)?.let { declaration ->
                            if (declaration.keyword == "val" || declaration.keyword == "var") {
                                processPropertyDeclaration(declaration, type)
                            } else if (declaration.keyword == "fun") {

                                logger.debug("fun ${declaration.identifier}")
                                val modifier = getVisibilityModifier(declaration)

                                if (modifier == Visibility.Public) {

                                    val name = declaration.identifier?.rawName ?: ""


                                    val returnType = if (declaration.type.isEmpty()) {
                                        ""
                                    } else {
                                        declaration.type[0].rawName
                                    }

                                    val method = Method(name, returnType)
                                    declaration.parameter.forEach { methodParam ->
                                        val parmType = getParameterType(methodParam)
                                        methodParam.identifier?.let { parameterName ->
                                            method.addParameter(Parameter(parameterName.rawName, parmType))
                                        }
                                    }

                                    type.addMethod(method)
                                }
                            } else if (declaration.keyword == "class") {
                                logger.debug("Found nested class ${declaration.identifier?.rawName}")
                                declaration.identifier?.rawName?.let { nestedTypeName ->
                                    val nestedType = Type(nestedTypeName, type.pkg)
                                    nestedType.parentType = type
                                    handleClassDeclaration(declaration, nestedType, classList, classCommentOnNextNestedType)
                                    classList.add(nestedType)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Could not parse type due to error", e)
        }
        finally {
            NDC.pop()
        }
    }

    private fun getParameterType(methodParam: KlassDeclaration): String {

        if(methodParam.type.isNotEmpty()) { return methodParam.type[0].rawName }

        return "unknown"
    }

    private fun findParameters(declaration: KlassDeclaration, field: Field) {
        declaration.type.forEach { typeIdentifier->
            typeIdentifier.parameter.forEach { typeParam->
                field.addTypeArgument(typeParam.identifier)
            }
        }
        declaration.children.forEach { child->
            if(child.description == "genericCallLikeComparison" && child is DefaultAstNode) {
                child.children.filter { c->c is DefaultAstNode && c.description == "callSuffix" }.firstOrNull()?.let { callSuffix->
                    (callSuffix as? DefaultAstNode)?.let { callSuffixDefault->
                        callSuffixDefault.children.forEach { cfdChild->(cfdChild as? KlassIdentifier)?.let { cfdIdentifier->
                            field.addTypeArgument(cfdIdentifier.identifier)
                        } }
                    }
                }
            }
        }
    }

    private fun processPropertyDeclaration(
        declaration: KlassDeclaration,
        type: Type
    ) {
        val name = declaration.identifier?.rawName ?: ""

        logger.debug("prop dec $name, type=${declaration.type}")
        val parmType = if(declaration.type.isEmpty()) {
            declaration.children.firstOrNull { it-> it is KlassIdentifier } ?.let { ident->
                (ident as? KlassIdentifier)?.let { klassIdentifier ->
                    klassIdentifier.identifier
                }
            } ?: ""
        } else declaration.type[0].rawName

        val modifier = getVisibilityModifier(declaration)

        type.addField(Field(name, parmType, modifier).apply {
            findParameters(declaration, this)
        })
    }

    private fun getVisibilityModifier(declaration: KlassDeclaration): Visibility {
        val modifier = if (declaration.modifiers.isEmpty()) {
            "public"
        } else {
            declaration.modifiers[0].modifier
        }
        return when (modifier) {
            "private" -> Visibility.Private
            else -> Visibility.Public
        }
    }

}