import com.vandenbreemen.grucd.render.plantuml.PlantUMLRenderer
import org.junit.jupiter.api.Test

class PlantUMLRenderingTest {

    @Test
    fun `should render SVG file`() {
        val classDiagram = """
            class TestClass {
            	+ myString: String
            }
        """.trimIndent()

        PlantUMLRenderer().render(classDiagram)
    }

}