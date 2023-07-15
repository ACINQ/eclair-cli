import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

interface IEclairClient {
    suspend fun getInfo(password: String, host: String): Result<String>
}

class EclairClient: IEclairClient {
    private val httpClient = HttpClient(CIO) {
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = "", password = "password")
                }
            }
        }
    }
    override suspend fun getInfo(password: String, host: String): Result<String> {
        val response: HttpResponse = httpClient.post("$host/getinfo")
        return response.runCatching {
            response.bodyAsText()
        }
    }
}