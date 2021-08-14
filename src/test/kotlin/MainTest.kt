import com.vandenbreemen.grucd.main.Main
import org.junit.jupiter.api.Test

class MainTest {

    @Test
    fun `should parse from java file on command`() {

        Main.main( arrayOf("-f", "src/test/resources/TestJava.java", "-o", "./testOutput/test.svg"));
    }

}