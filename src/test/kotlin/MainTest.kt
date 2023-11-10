import com.vandenbreemen.grucd.main.Main
import org.amshove.kluent.shouldStartWith
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

    @Test
    fun `should dump plantuml code for source directory on command`() {
        Main.main(arrayOf("-d", "src/main/", "-o", "./testOutput/grucd.dat", "-p"))
    }

    @Test
    fun `should dump list of unused classes`() {
        Main.main(arrayOf("-d", "src/main/", "-o", "./testOutput/grucd.dat", "-u"))
    }

    @Test
    fun `should export svg raw when no file output is given`() {
        val svg = (Main.generateAndProcessUML(arrayOf("-d", "src/main/")))
        svg.shouldStartWith("<?xml version=\"1.0\"")
    }

}