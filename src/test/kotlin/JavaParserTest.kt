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

}