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
        KotlinDoc,
        NestedClassToKotlinDoc
    }

    private open class AstSearchResult<T>(val result: T)
    private class MapAstSearchResult(map: MutableMap<String, DefaultAstTerminal>): AstSearchResult<MutableMap<String, DefaultAstTerminal>>(map)

    private class SearchContext {
        var tabs: String = ""
        var lastComment: DefaultAstTerminal? = null
        val classNameToComment: MutableMap<String, DefaultAstTerminal> = mutableMapOf()

        fun tabIn(): SearchContext {
            tabs += "\t"
            return this
        }

        fun tabOut(): SearchContext {
            tabs = tabs.substring(0, tabs.length - "\t".length)
            return this
        }

        override fun toString(): String {
            return "[SearchContext - lastCmt=$lastComment, classNameToCmnt=$classNameToComment]"
        }
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

    private fun visitAll(ast: Ast, toFind: ItemTypeToFind, searchContext: SearchContext? = null): AstSearchResult<out Any>? {

        var currentSearchContext = searchContext ?: SearchContext()
        val tab = searchContext?.tabs ?: ""

        logger.debug("${tab}VISIT ${ast.javaClass.simpleName}(${ast.description}) - context=$currentSearchContext ($toFind)")

        (ast as? AstWithAttachments)?.run {
            logger.trace("${tab}Handling attachments for ${ast.javaClass.simpleName} ${ast.description}=~=~=~=~")
            attachments.attachments.entries.forEach { entry ->
                when (entry.value) {
                    is RawAst -> {
                        visitAll(
                            (entry.value as RawAst).ast,
                            toFind,
                            currentSearchContext.tabIn()
                        )?.let { found ->
                            if(found !is MapAstSearchResult)
                            return found
                        }
                        currentSearchContext.tabOut()
                    }
                    else -> (entry.value as? Ast)?.run {
                        visitAll(this, toFind, currentSearchContext.tabIn())?.let { found ->
                            if(found !is MapAstSearchResult)
                            return found
                        }
                        currentSearchContext.tabOut()
                    }?.run { logger.trace("${tab}unkn: ${entry.value}") }
                }

            }
            logger.trace("${tab}END attachments =~=~=~=~")

        }

        when (ast) {
            is KlassDeclaration -> {
                logger.trace("${tab}kw=${ast.keyword}")
                if (ast.keyword == "class") {
                    currentSearchContext.run {
                        lastComment?.let { lastCmnt ->
                            ast?.identifier?.let { ident ->
                                logger.trace("${tab}Attaching comment to ${ident.identifier}")
                                this.classNameToComment[ident.identifier] = lastCmnt
                            }
                        }
                    }
                }
                for (child in ast.children) {
                    visitAll(child, toFind, currentSearchContext.tabIn())?.let { found ->
                        if(found !is MapAstSearchResult)
                         return found
                    }
                    currentSearchContext.tabOut()
                }
            }
            is AstNode -> {
                logger.trace("${tab}AstNode:  ${ast.description}")
                for (child in ast.children) {
                    visitAll(child, toFind, currentSearchContext.tabIn())?.let { found ->
                        if(found !is MapAstSearchResult)
                        return found
                    }
                    currentSearchContext.tabOut()
                }
            }
            is DefaultAstTerminal -> {
                if (ast.description == "DelimitedComment") {
                    logger.trace("${tab}Found comment ${ast.text}")
                    currentSearchContext.lastComment = ast
                    if (toFind == ItemTypeToFind.KotlinDoc) {
                        return AstSearchResult(ast)
                    }
                }
            }
            else ->
                logger.trace("${tab}Unknown: ${ast.description}")
        }

        if(toFind == ItemTypeToFind.NestedClassToKotlinDoc) {
            return MapAstSearchResult(currentSearchContext.classNameToComment)
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
                visitAll(astItem, ItemTypeToFind.KotlinDoc)?.let { comment->
                    (comment as? AstSearchResult<DefaultAstTerminal>)?.let { commentTerm->
                        classComment = comment.result.text.replace(Regex("([/][*]+)"), "")
                            .replace(Regex("([*]+[/])"), "")
                            .replace(Regex("^\\s*[*]"), "").trim()
                    }
                }
                logger.debug("END VISIT TREE\n\n\n")

                (astItem as? PackageHeader)?.let {
                    pkg = it
                }

                (astItem as? KlassDeclaration)?.let {
                    val type = Type(it.identifier?.rawName ?: "", pkg?.identifier?.get(0)?.rawName ?: "",
                        if(it.keyword == "interface") {TypeType.Interface } else { TypeType.Class }
                        )
                    type.imports = imports

                    handleClassDeclaration(it, type, result)
                    classComment?.let { comment->type.classDoc = comment } ?: run {
                        visitAll(astItem, ItemTypeToFind.KotlinDoc)?.let { comment ->
                            (comment as? DefaultAstTerminal)?.let { commentTerm ->
                                classComment = commentTerm.text.replace(Regex("([/][*]+)"), "")
                                    .replace(Regex("([*]+[/])"), "")
                                    .replace(Regex("^\\s*[*]"), "").trim()
                            }
                        }
                    }
                    result.add(type)

                }
            }
        }

        return result
    }

    private fun handleClassDeclaration(declaration: KlassDeclaration, type: Type, classList: MutableList<Type>) {

        val nestedClassesToJavadoc = visitAll(declaration, ItemTypeToFind.NestedClassToKotlinDoc)

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
                                    (nestedClassesToJavadoc as? MapAstSearchResult)?.let { result->
                                        result.result[nestedTypeName]?.let { commentNode->
                                            nestedType.classDoc = commentNode.text.replace(Regex("([/][*]+)"), "")
                                                .replace(Regex("([*]+[/])"), "")
                                                .replace(Regex("^\\s*[*]"), "").trim()
                                        }
                                    }
                                    handleClassDeclaration(declaration, nestedType, classList)
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