package com.vandenbreemen.grucd.parse

import com.vandenbreemen.grucd.model.Field
import com.vandenbreemen.grucd.model.Type
import com.vandenbreemen.grucd.model.Visibility
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.common.summary.PackageHeader
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser

class ParseKotlin {

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
                    println(it.identifier?.rawName)

                    val type = Type(it.identifier?.rawName ?: "", pkg?.identifier?.get(0)?.rawName ?: "")
                    handleClassDeclaration(it, type)
                    result.add(type)

                }
            }
        }

        return result
    }

    private fun handleClassDeclaration(declaration: KlassDeclaration, type: Type) {
        declaration.children.forEach { child->
            (child as? KlassDeclaration)?.let { kd->
                kd.parameter.forEach { parm->
                    val name = parm.identifier?.rawName ?: ""
                    val parmType = parm.type[0].rawName

                    type.addField(Field(name, parmType, Visibility.Public))

                    parm.children.forEach {

                    }
                }
            }
        }
    }

}