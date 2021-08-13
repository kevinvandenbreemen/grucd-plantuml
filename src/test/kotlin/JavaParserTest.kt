import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.visitor.VoidVisitor
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.vandenbreemen.grucd.model.Type
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStream

class JavaParserTest {

    @Test
    fun `should parse java class`() {
        val parser = JavaParser()
        val compilationUnit = parser.parse(File("src/test/resources/TestJava.java"))

        println(compilationUnit)
        compilationUnit.isSuccessful.shouldBeTrue()

//        compilationUnit.result.ifPresent { unit->
//            println(unit)
//        }

        val types = mutableListOf<Type>()

        compilationUnit.result.ifPresent { unit->
            unit.accept(object: VoidVisitorAdapter<Void>() {
                override fun visit(n: ClassOrInterfaceDeclaration?, arg: Void?) {
                    types.add(Type(n?.nameAsString))
                }
            }, null)
        }

        types.shouldNotBeEmpty()
        types.size shouldBeEqualTo 1
        types[0].name shouldBeEqualTo "TestJava"
    }

}