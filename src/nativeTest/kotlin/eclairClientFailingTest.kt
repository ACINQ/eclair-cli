import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EclairClientTest {
    @Test
    fun `test getInfo unauthorized`() = runBlocking {
        val client = EclairClient()
        val result = client.getInfo("wrongPassword", "http://localhost:8080")
        assertTrue(result.isFailure)
        assertEquals("Unauthorized: Incorrect password", result.exceptionOrNull()?.message)
    }
}