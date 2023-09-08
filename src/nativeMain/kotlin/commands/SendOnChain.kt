package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import types.ApiError
import types.SendOnChainResult
import types.Serialization

class SendOnChainCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "sendonchain",
    "Send an on-chain transaction to the given address. The API is only available with the bitcoin-core watcher type. The API returns the txid of the bitcoin transaction sent."
) {
    private val address by option(
        ArgType.String,
        description = "The bitcoin address of the recipient"
    )
    private val amountSatoshis by option(
        ArgType.Int,
        description = "The amount that should be sent"
    )
    private val confirmationTarget by option(
        ArgType.Int,
        description = "The confirmation target(blocks)"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result: Either<ApiError, String> = when (val response = eclairClient.sendonchain(
            address = address!!,
            amountSatoshis = amountSatoshis!!,
            confirmationTarget = confirmationTarget!!
        )) {
            is Either.Left -> Either.Left(response.value)
            is Either.Right -> {
                try {
                    val decoded = format.decodeFromString<String>(response.value)
                    Either.Right(Serialization.encode(SendOnChainResult(true, decoded)))
                } catch (e: SerializationException) {
                    Either.Left(ApiError(1, "API response could not be parsed: ${response.value}"))
                }
            }
        }
        resultWriter.write(result)
    }
}