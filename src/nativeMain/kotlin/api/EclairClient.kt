package api

import arrow.core.Either
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import types.ApiError

interface IEclairClientBuilder {
    fun build(apiHost: String, apiPassword: String): IEclairClient
}

class EclairClientBuilder : IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = EclairClient(apiHost, apiPassword)
}

interface IEclairClient {
    suspend fun getInfo(): Either<ApiError, String>
    suspend fun connectUri(uri: String): Either<ApiError, String>
    suspend fun connectNodeId(nodeId: String): Either<ApiError, String>
    suspend fun disconnect(nodeId: String): Either<ApiError, String>
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
        install(ContentNegotiation) {
            json()
        }
    }

    private fun convertHttpError(statusCode: HttpStatusCode): ApiError {
        return when (statusCode) {
            HttpStatusCode.Unauthorized -> ApiError(statusCode.value, "invalid api password")
            HttpStatusCode.BadRequest -> ApiError(statusCode.value, "invalid request parameters")
            else -> ApiError(statusCode.value, "api error")
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

    override suspend fun connectUri(uri: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/connect",
                formParameters = Parameters.build {
                    append("uri", uri)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Exception) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun connectNodeId(nodeId: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/connect",
                formParameters = Parameters.build {
                    append("nodeId", nodeId)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Exception) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun disconnect(nodeId: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/disconnect",
                formParameters = Parameters.build {
                    append("nodeId", nodeId)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Exception) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }
}
