package types

import arrow.core.Either
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
object Serialization {
    inline fun <reified T : EclairApiType> decode(apiResponse: String): Either<ApiError, T> {
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        return try {
            Either.Right(format.decodeFromString<T>(apiResponse))
        } catch (e: Throwable) {
            Either.Left(ApiError(1, "api response could not be parsed: $apiResponse"))
        }
    }

    inline fun <reified T : EclairApiType> encode(apiResponse: T): String {
        val format = Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }
        return format.encodeToString(EclairApiType.serializer(), apiResponse)
    }
}