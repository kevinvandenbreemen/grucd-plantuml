import com.vandenbreemen.grucd.render.plantuml.PlantUMLRenderer
import org.junit.jupiter.api.Test

class PlantUMLRenderingTest {

    @Test
    fun `should render SVG file`() {
        val classDiagram = """
            @startuml
            class TestClass {
            	+ myString: String
            }
            @enduml
        """.trimIndent()

        println(PlantUMLRenderer().renderSVG(classDiagram))
    }

}