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

    @Test
    fun `should dump plantuml code for source directory on command`() {
        Main.main(arrayOf("-d", "src/main/", "-o", "./testOutput/grucd.dat", "-p"))
    }

    @Test
    fun `should dump list of unused classes`() {
        Main.main(arrayOf("-d", "src/main/", "-o", "./testOutput/grucd.dat", "-u"))
    }

}