import com.vandenbreemen.grucd.parse.ParseJava
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.jupiter.api.Test

class JavaParserTest {

    @Test
    fun `should parse java class`() {
        val types = ParseJava().parse("src/test/resources/TestJava.java")

        types.shouldNotBeEmpty()
        types.size shouldBeEqualTo 1
        types[0].name shouldBeEqualTo "TestJava"
    }

}