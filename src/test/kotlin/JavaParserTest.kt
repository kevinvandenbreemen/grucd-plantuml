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

        fields.size shouldBeEqualTo 1
        fields[0].name shouldBeEqualTo "publicInt"
        fields[0].typeName shouldBeEqualTo "int"
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

}