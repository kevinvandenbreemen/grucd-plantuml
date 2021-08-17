package com.vandenbreemen.grucd.parse

import com.vandenbreemen.grucd.model.*
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.DefaultAstNode
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.common.klass.KlassIdentifier
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.apache.log4j.Logger
import org.apache.log4j.NDC

class ParseKotlin {

    companion object {
        private val logger:Logger = Logger.getLogger(ParseKotlin::class.java)
    }

    fun parse(filePath: String): List<Type> {
        val kotlinFile = KotlinGrammarAntlrKotlinParser.parseKotlinFile(AstSource.File(filePath))
        val result = mutableListOf<Type>()
        kotlinFile.summary(false).onSuccess { astList->

            var pkg: PackageHeader? = null

            astList.forEach { astItem->

                (astItem as? PackageHeader)?.let {
                    pkg = it
                }

                (astItem as? KlassDeclaration)?.let {

                    val type = Type(it.identifier?.rawName ?: "", pkg?.identifier?.get(0)?.rawName ?: "")
                    handleClassDeclaration(it, type, result)
                    result.add(type)

                }
            }
        }

        return result
    }

    private fun handleClassDeclaration(declaration: KlassDeclaration, type: Type, classList: MutableList<Type>) {

        logger.debug("Parsing class ${type.name}...")
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

        type.addField(Field(name, parmType, modifier))
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