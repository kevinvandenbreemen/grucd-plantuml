import com.vandenbreemen.grucd.builder.ModelBuilder
import com.vandenbreemen.grucd.model.*
import com.vandenbreemen.grucd.render.plantuml.PlantUMLScriptGenerator
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test

class PlantUMLScriptGeneratorTest {

    @Test
    fun `should generate script for a simple class`() {
        val type = Type("TestClass", "com.test.types")
        type.addField(Field("myString", "String", Visibility.Public))

        val result = PlantUMLScriptGenerator().renderType(type)
        println(result)

        result.shouldContain("class TestClass")
        result.shouldContain("+ myString: String")
    }

    @Test
    fun `should generate script that includes methods`() {
        val type = Type("TestClass", "com.test.types")
        type.addField(Field("myString", "String", Visibility.Public))
        val method = Method("getData", "String")
        method.addParameter(Parameter("argument", "Int"))
        type.addMethod(method)

        val result = PlantUMLScriptGenerator().renderType(type)
        println(result)

        result.shouldContain("+ getData(argument: Int): String")
    }

    @Test
    fun `should comma separate method args`() {
        val type = Type("TestClass", "com.test.types")
        type.addField(Field("myString", "String", Visibility.Public))
        val method = Method("getData", "String")
        method.addParameter(Parameter("argument", "Int"))
        method.addParameter(Parameter("test", "boolean"))
        type.addMethod(method)

        val result = PlantUMLScriptGenerator().renderType(type)
        println(result)

        result.shouldContain("+ getData(argument: Int, test: boolean): String")
    }

    @Test
    fun `should generate diagram with a encapsulation in it`() {
        val type = Type("TestClass", "com.test.types")
        type.addField(Field("myString", "String", Visibility.Public))
        val method = Method("getData", "String")
        method.addParameter(Parameter("argument", "Int"))
        method.addParameter(Parameter("test", "boolean"))
        type.addMethod(method)

        val container = Type("ContainingClass", "com.test.types")
        container.addField(Field("test", "TestClass", Visibility.Public))

        val model = ModelBuilder().build(listOf(type, container))
        val script = PlantUMLScriptGenerator().render(model)
        println(script)

        script.shouldContain("ContainingClass o--> TestClass")
    }

    @Test
    fun `should generate diagram with private field in it`() {
        val type = Type("TestClass", "com.test.types")
        type.addField(Field("myString", "String", Visibility.Public))
        val method = Method("getData", "String")
        method.addParameter(Parameter("argument", "Int"))
        method.addParameter(Parameter("test", "boolean"))
        type.addMethod(method)

        val container = Type("ContainingClass", "com.test.types")
        container.addField(Field("test", "TestClass", Visibility.Private))

        val model = ModelBuilder().build(listOf(type, container))
        val script = PlantUMLScriptGenerator().render(model)
        println(script)

        script.shouldContain("ContainingClass o--> TestClass")
        script.shouldContain("- test: TestClass")
    }

}