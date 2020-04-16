import com.mjrcompany.eventplannerservice.module
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {
            // FIXME REMOVE THIS
        }
    }
}
