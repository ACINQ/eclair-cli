import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


interface IEclairClient {
    suspend fun getInfo(password: String, host: String): Result<String>
}

class EclairClient : IEclairClient {

    override suspend fun getInfo(password: String, host: String): Result<String> {
        val httpClient = HttpClient(CIO) {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "", password = password)
                    }
                }
            }
        }

        return runCatching {
            val response: HttpResponse = httpClient.post("$host/getinfo")
            if (response.status == HttpStatusCode.Unauthorized) {
                throw Exception("Unauthorized: Incorrect password")
            } else {
                response.bodyAsText()
            }
        }.onFailure { e ->
            if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
                throw Exception("Unauthorized: Incorrect password")
            }
        }.also {
            httpClient.close()
        }
    }
}