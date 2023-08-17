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
import types.ReceivedInfoResponse

class ListReceivedPaymentsCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "listreceivedpayments",
    "Returns the list of payments received by your node."
) {
    private val from by option(
        ArgType.Int,
        description = "Filters elements no older than this unix-timestamp"
    )
    private val to by option(
        ArgType.Int,
        description = "Filters elements no younger than this unix-timestamp"
    )
    private val count by option(
        ArgType.Int,
        description = "Limits the number of results returned"
    )
    private val skip by option(
        ArgType.Int,
        description = "Skip some number of results"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.listreceivedpayments(
            from = from,
            to = to,
            count = count,
            skip = skip
        )
            .flatMap { apiResponse ->
                try {
                    Either.Right(format.decodeFromString<List<ReceivedInfoResponse>>(apiResponse))
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