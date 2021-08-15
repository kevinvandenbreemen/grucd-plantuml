import com.vandenbreemen.grucd.builder.ModelBuilder
import com.vandenbreemen.grucd.model.RelationType
import com.vandenbreemen.grucd.model.Visibility
import com.vandenbreemen.grucd.parse.ParseJava
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class JavaParserTest {

    @Test
    fun `should parse java class`() {
        val types = ParseJava().parse("src/test/resources/TestJava.java")

        types.shouldNotBeEmpty()
        types.size shouldBeEqualTo 1
        types[0].name shouldBeEqualTo "TestJava"
    }

    @Test
    fun `should parse public java field`() {
        val types = ParseJava().parse("src/test/resources/TestJava.java")
        val type = types[0]
        val fields = type.fields

        fields.shouldNotBeNull()

        fields.size shouldBeEqualTo 2
        fields[0].name shouldBeEqualTo "publicInt"
        fields[0].typeName shouldBeEqualTo "int"
        fields[0].visibility shouldBeEqualTo Visibility.Public
    }

    @Test
    fun `should parse private java fields`() {
        val types = ParseJava().parse("src/test/resources/TestJava.java")
        val type = types[0]
        val fields = type.fields

        fields.size shouldBeEqualTo 2
        fields[1].name shouldBeEqualTo "privateInt"
        fields[1].visibility shouldBeEqualTo Visibility.Private

    }

    @Test
    fun `should be able to determine the package of a new class`() {
        val types = ParseJava().parse("src/test/resources/pkg/test/PackageTest.java")
        types[0].pkg shouldBeEqualTo "pkg.test"
    }

    @Test
    fun `should include methods on the class`() {
        val types = ParseJava().parse("src/test/resources/TestJava.java")
        val type = types[0]
        val methods = type.methods

        methods.shouldNotBeNull()

        methods.forEach { m->println(m.name) }

        methods.size shouldBeEqualTo 3

        methods[0].name shouldBeEqualTo "getPrivateInt"
        methods[0].returnType shouldBeEqualTo "int"

        methods[1].name shouldBeEqualTo "engagePrimaryInterlock"
        methods[1].returnType shouldBeEqualTo "void"
    }

    @Test
    fun `should include method arguments in methods`() {
        val types = ParseJava().parse("src/test/resources/TestJava.java")
        val type = types[0]
        val methods = type.methods

        methods[1].name shouldBeEqualTo "engagePrimaryInterlock"
        methods[1].returnType shouldBeEqualTo "void"
        methods[1].parameters.shouldNotBeNull()
        methods[1].parameters[0].name shouldBeEqualTo "parameter"
        methods[1].parameters[1].name shouldBeEqualTo "arguments"
        methods[1].parameters[0].typeName shouldBeEqualTo "String"
        methods[1].parameters[1].typeName shouldBeEqualTo "int[]"
    }

    @Test
    fun `should not include private methods`() {
        val types = ParseJava().parse("src/test/resources/TestJava.java")
        val type = types[0]
        val methods = type.methods

        methods.size shouldBeEqualTo 3
    }

    @Test
    fun `should recognized encapsulated object`() {
        val types = ParseJava().parse("src/test/resources/encapsulation.test/Encapsulation.java")
        types.size shouldBeEqualTo 2

        val builder = ModelBuilder()
        val model = builder.build(types)

        println(model.relations[0])

        model.relations.shouldNotBeEmpty()
        model.relations[0].from shouldBeEqualTo model.types[0]
        model.relations[0].to shouldBeEqualTo model.types[1]
        model.relations[0].type shouldBeEqualTo RelationType.encapsulates
    }

    @Test
    fun `should recognized encapsulated list of objects`() {
        val types = ParseJava().parse("src/test/resources/encapsulation.test/ListEncapsulationTest.java")
        types.size shouldBeEqualTo 2

        types[0].fields[0].typeArguments.shouldNotBeEmpty()

        val builder = ModelBuilder()
        val model = builder.build(types)

        println(model.relations[0])

        model.relations.shouldNotBeEmpty()
        model.relations[0].from shouldBeEqualTo model.types[0]
        model.relations[0].to shouldBeEqualTo model.types[1]
        model.relations[0].type shouldBeEqualTo RelationType.encapsulates
    }

    @Test
    fun `should parse enumerations`() {
        val types = ParseJava().parse("src/test/resources/enum/Enum.java")
        types.size shouldBeEqualTo 2
    }

}