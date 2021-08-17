import com.vandenbreemen.grucd.doc.SystemInfo
import org.amshove.kluent.shouldNotBeNullOrEmpty
import org.amshove.kluent.shouldNotContain
import org.junit.jupiter.api.Test

class SystemInfoTest {

    @Test
    fun `should provide current version`() {
        val info = SystemInfo()
        info.version.shouldNotBeNullOrEmpty()
        info.version.shouldNotContain("\n")
    }

    @Test
    fun `should display system info`() {
        SystemInfo().print()
    }

}