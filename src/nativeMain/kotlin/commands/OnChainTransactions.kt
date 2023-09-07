package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import types.ApiError
import types.OnChainTransaction

class OnChainTransactionsCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "onchaintransactions",
    "Retrieves information about the latest on-chain transactions made by our Bitcoin wallet(most recent transactions first."
) {
    private val count by option(
        ArgType.Int,
        description = "Number of transactions to return"
    )
    private val skip by option(
        ArgType.Int,
        description = "Number of transactions to skip"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.onchaintransactions(
            count = count!!,
            skip = skip!!
        )
            .flatMap { apiResponse ->
                try {
                    Either.Right(format.decodeFromString<List<OnChainTransaction>>(apiResponse))
                } catch (e: Throwable) {
                    Either.Left(ApiError(1, "api response could not be parsed: $apiResponse"))
                }
            }
            .map { decoded ->
                format.encodeToString(decoded)
            }
        resultWriter.write(result)
    }
}