import arrow.core.Either
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

data class ApiError(val code: Int, val message: String)

interface IEclairClientBuilder {
    fun build(apiHost: String, apiPassword: String): IEclairClient
}

class EclairClientBuilder : IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = EclairClient(apiHost, apiPassword)
}

interface IEclairClient {
    suspend fun getInfo(): Either<ApiError, String>
}

class EclairClient(private val apiHost: String, private val apiPassword: String) : IEclairClient {

    private val httpClient = HttpClient(CIO) {
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = "", password = apiPassword)
                }
            }
        }
    }

    override suspend fun getInfo(): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.post("$apiHost/getinfo")
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(response.bodyAsText())
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "unknown exception"))
        }
    }

    private fun convertHttpError(statusCode: HttpStatusCode): ApiError {
        return when (statusCode) {
            HttpStatusCode.Unauthorized -> ApiError(statusCode.value, "invalid api password")
            HttpStatusCode.BadRequest -> ApiError(statusCode.value, "invalid request parameters")
            else -> ApiError(statusCode.value, "api error")
        }
    }
}
