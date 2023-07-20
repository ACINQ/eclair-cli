import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*


interface IEclairClient {
    suspend fun getInfo(password: String, host: String): Result<String>
    suspend fun connectUri(password: String, host: String, uri: String): Result<String>
    suspend fun connectManual(password: String, host: String, nodeId: String, manualHost: String): Result<String>
    suspend fun connectNodeId(password: String, host: String, nodeId: String): Result<String>
    suspend fun disconnect(password: String, host: String, nodeId: String): Result<String>
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

    @OptIn(InternalAPI::class)
    override suspend fun connectUri(password: String, host: String, uri: String): Result<String> {
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
            val response: HttpResponse = httpClient.post("$host/connect") {
                parameter("uri", uri)
            }
            if (response.status == HttpStatusCode.OK) {
                "Connected"
            } else {
                throw Exception("Failed to connect")
            }
        }.onFailure { e ->
            if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
                throw Exception("Unauthorized: Incorrect password")
            } else {
                throw Exception("Error connecting to $uri: ${e.message}")
            }
        }.also {
            httpClient.close()
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun connectManual(
        password: String,
        host: String,
        nodeId: String,
        manualHost: String
    ): Result<String> {
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
            val response: HttpResponse = httpClient.post("$host/connect") {
                parameter("nodeId", nodeId)
                parameter("host", manualHost)
            }
            if (response.status == HttpStatusCode.OK) {
                "Connected"
            } else {
                throw Exception("Failed to connect")
            }
        }.onFailure { e ->
            if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
                throw Exception("Unauthorized: Incorrect password")
            } else {
                throw Exception("Error connecting to $nodeId: ${e.message}")
            }
        }.also {
            httpClient.close()
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun connectNodeId(password: String, host: String, nodeId: String): Result<String> {
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
            val response: HttpResponse = httpClient.post("$host/connect") {
                parameter("nodeId", nodeId)
            }
            if (response.status == HttpStatusCode.OK) {
                "Connected"
            } else {
                throw Exception("Failed to connect")
            }
        }.onFailure { e ->
            if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
                throw Exception("Unauthorized: Incorrect password")
            } else {
                throw Exception("Error connecting to $nodeId: ${e.message}")
            }
        }.also {
            httpClient.close()
        }
    }

    override suspend fun disconnect(password: String, host: String, nodeId: String): Result<String> {
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
            val response: HttpResponse = httpClient.post("$host/disconnect") {
                parameter("nodeId", nodeId)
            }
            if (response.status == HttpStatusCode.OK) {
                "Disconnected"
            } else {
                throw Exception("Failed to disconnect")
            }
        }.onFailure { e ->
            if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
                throw Exception("Unauthorized: Incorrect password")
            } else {
                throw Exception("Error disconnecting: ${e.message}")
            }
        }.also {
            httpClient.close()
        }
    }
}