import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface IEclairClient {
    suspend fun getInfo(): Result<String>
}

class EclairClient(
    private val username: String = "eclair-cli",
    private val password: String
): IEclairClient {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username="", password="password")
                }
                sendWithoutRequest { true }
            }
        }
    }

    override suspend fun getInfo(): Result<String> {
        val response = httpClient.get("http://localhost:8080/getinfo")
        return response.runCatching {
            response.bodyAsText()
        }
    }
}