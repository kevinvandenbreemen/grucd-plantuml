import com.vandenbreemen.grucd.model.Field
import com.vandenbreemen.grucd.model.Type
import com.vandenbreemen.grucd.render.plantuml.PlantUMLScriptGenerator
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test

class PlantUMLScriptGeneratorTest {

    @Test
    fun `should generate script for a simple class`() {
        val type = Type("TestClass", "com.test.types")
        type.addField(Field("myString", "String"))

        val result = PlantUMLScriptGenerator().renderType(type)
        println(result)

        result.shouldContain("class TestClass")
        result.shouldContain("+ myString: String")
    }

}