import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface IEclairClient {
    suspend fun getInfo(): Result<EclairInfo>
}

class EclairClient: IEclairClient {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    override suspend fun getInfo(): Result<EclairInfo> {
        val response = httpClient.get("https://localhost:8080/getinfo")
        return response.runCatching {
            body<EclairInfo>()
        }
    }
}