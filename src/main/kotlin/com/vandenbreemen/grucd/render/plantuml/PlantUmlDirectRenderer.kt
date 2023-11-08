package com.vandenbreemen.grucd.render.plantuml

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * Renders plantuml to svg directly using the Plantuml library rather than an intermediary lib
 */
class PlantUmlDirectRenderer {

    fun render(source: String): String {
        val reader = SourceStringReader(source)
        val os = ByteArrayOutputStream()
        val description = reader.outputImage(os, FileFormatOption(FileFormat.SVG))
        os.close()

        return String(os.toByteArray(), Charset.forName("UTF-8"))
    }

}

fun main(args: Array<String>) {

    val source = """
        @startuml
     Bob -> Alice : hello
       @enduml
       """

    val reader = SourceStringReader(source)
    val os = ByteArrayOutputStream()
    val description = reader.outputImage(os, FileFormatOption(FileFormat.SVG))
    os.close()

    val svg = String(os.toByteArray(), Charset.forName("UTF-8"))

    println(svg)
}