package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import types.ApiError
import types.InvoiceResult
import types.Serialization

class SendToNodeCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "sendtonode",
    "Sends money to a node using keysend (spontaneous payment without a Bolt11 invoice) as specified in blip 3. "
) {
    private val nodeId by option(
        ArgType.String,
        description = "The recipient of this payment"
    )
    private val amountMsat by option(
        ArgType.Int,
        description = "Amount to pay"
    )
    private val maxAttempts by option(
        ArgType.Int,
        description = "Max number of retries"
    )
    private val maxFeeFlatSat by option(
        ArgType.Int,
        description = "Fee threshold to be paid along the payment route"
    )
    private val maxFeePct by option(
        ArgType.Int,
        description = "Max percentage to be paid in fees along the payment route (ignored if below maxFeeFlatSat"
    )
    private val externalId by option(
        ArgType.String,
        description = "Extra payment identifier specified by the caller"
    )
    private val pathFindingExperimentName by option(
        ArgType.String,
        description = "Name of the path-finding configuration that should be used"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val response = eclairClient.sendtonode(
            nodeId!!,
            amountMsat!!,
            maxAttempts,
            maxFeeFlatSat,
            maxFeePct,
            externalId,
            pathFindingExperimentName,
        )
        val result: Either<ApiError, String> = when (response) {
            is Either.Left -> Either.Left(response.value)
            is Either.Right -> {
                try {
                    val decoded = format.decodeFromString<String>(response.value)
                    Either.Right(Serialization.encode(InvoiceResult(true, decoded)))
                } catch (e: SerializationException) {
                    Either.Left(ApiError(1, "API response could not be parsed: ${response.value}"))
                }
            }
        }
        resultWriter.write(result)
    }
}