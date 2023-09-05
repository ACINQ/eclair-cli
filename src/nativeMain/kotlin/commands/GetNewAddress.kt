package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import types.ApiError
import types.GetNewAddressResult
import types.Serialization

class GetNewAddressCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "getnewaddress",
    "Get a new on-chain address from the wallet. This can be used to deposit funds that will later be used to fund channels."
) {
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result: Either<ApiError, String> = when (val response = eclairClient.getnewaddress()) {
            is Either.Left -> Either.Left(response.value)
            is Either.Right -> {
                try {
                    val decoded = format.decodeFromString<String>(response.value)
                    Either.Right(Serialization.encode(GetNewAddressResult(true, decoded)))
                } catch (e: SerializationException) {
                    Either.Left(ApiError(1, "API response could not be parsed: ${response.value}"))
                }
            }
        }
        resultWriter.write(result)
    }
}