package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.RouteResult
import types.Serialization

class SendToRouteCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "sendtoroute",
    "Sends money to a node forcing the payment to go through the given route."
) {
    private val invoice by option(
        ArgType.String,
        description = "The invoice you want to pay"
    )
    private val nodeIds by option(
        ArgType.String,
        description = "A list of nodeIds from source to destination of the payment"
    )
    private val shortChannelIds by option(
        ArgType.String,
        description = "A list of shortChannelIds from source to destination of the payment"
    )
    private val amountMsat by option(
        ArgType.Int,
        description = "Amount to pay"
    )
    private val paymentHash by option(
        ArgType.String,
        description = "The payment hash for this payment"
    )
    private val finalCltvExpiry by option(
        ArgType.Int,
        description = "The total CLTV expiry value for this payment"
    )
    private val maxFeeMsat by option(
        ArgType.Int,
        description = "Maximum fee allowed for this payment"
    )
    private val recipientAmountMsat by option(
        ArgType.Int,
        description = "Total amount that the recipient should receive (if using MPP)"
    )
    private val parentId by option(
        ArgType.String,
        description = "Id of the whole payment (if using MPP)"
    )
    private val externalId by option(
        ArgType.String,
        description = "Extra payment identifier specified by the caller"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.sendtoroute(
            invoice = invoice!!,
            nodeIds = nodeIds?.split(","),
            shortChannelIds = shortChannelIds?.split(","),
            amountMsat = amountMsat!!,
            paymentHash = paymentHash!!,
            finalCltvExpiry = finalCltvExpiry!!,
            maxFeeMsat = maxFeeMsat,
            recipientAmountMsat = recipientAmountMsat,
            parentId = parentId,
            externalId = externalId,
        )
            .flatMap { apiResponse -> Serialization.decode<RouteResult>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}