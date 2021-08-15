import com.vandenbreemen.grucd.main.Main
import org.junit.jupiter.api.Test

class MainTest {

    @Test
    fun `should parse from java file on command`() {

        Main.main( arrayOf("-f", "src/test/resources/TestJava.java", "-o", "./testOutput/test.svg"));
    }

    @Test
    fun `should parse source directory on command`() {
        Main.main(arrayOf("-d", "src/main/", "-o", "./testOutput/grucd.svg"))
    }

}