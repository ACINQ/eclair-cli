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
import types.SentInfoResponse

class GetSentInfoCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "getsentinfo",
    "Returns a list of attempts to send an outgoing payment."
) {
    private val paymentHash by option(
        ArgType.String,
        description = "The payment hash common to all payment attempts to be retrieved"
    )
    private val id by option(
        ArgType.String,
        description = "The unique id of the payment attempt"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.getsentinfo(
            paymentHash = paymentHash!!,
            id = id
        )
            .flatMap { apiResponse ->
                try {
                    Either.Right(format.decodeFromString<List<SentInfoResponse>>(apiResponse))
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