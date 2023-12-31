import com.vandenbreemen.grucd.builder.ModelBuilder
import com.vandenbreemen.grucd.model.*
import com.vandenbreemen.grucd.render.plantuml.PlantUMLScriptGenerator
import com.vandenbreemen.grucd.render.plantuml.PlantUmlDirectRenderer
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
    fun `should generate and render diagram with a encapsulation in it`() {
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

        println(PlantUmlDirectRenderer().render(script))
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

    @Test
    fun `should generate diagram with nested types in it`() {
        val type1 = Type("Main", "com.kevin", TypeType.Class)
        val type2 = Type("Nested", "com.kevin", TypeType.Class)
        type2.parentType = type1

        val model = ModelBuilder().build(listOf(type1, type2))

        val script = PlantUMLScriptGenerator().render(model)
        println(script)

        script.shouldContain("Main +--> Nested")
    }

    @Test
    fun `should generate diagram with superclass in it`() {
        val type1 = Type("Main", "com.kevin", TypeType.Class)
        val type2 = Type("Superclass", "com.kevin", TypeType.Class)
        type1.addSuperType("Superclass")

        val model = ModelBuilder().build(listOf(type1, type2))

        val script = PlantUMLScriptGenerator().render(model)
        println(script)

        script.shouldContain("Main --|> Superclass")
    }

    @Test
    fun `should generate diagram with interface implementation in it`() {
        val type1 = Type("Main", "com.kevin", TypeType.Class)
        val type2 = Type("Interface", "com.kevin", TypeType.Class)
        type1.addInterface("Interface")

        val model = ModelBuilder().build(listOf(type1, type2))

        val script = PlantUMLScriptGenerator().render(model)
        println(script)

        script.shouldContain("Main ..|> Interface")
    }

}