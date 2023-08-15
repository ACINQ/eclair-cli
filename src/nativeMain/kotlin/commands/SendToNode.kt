package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
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
        val result = eclairClient.sendtonode(
            nodeId!!,
            amountMsat!!,
            maxAttempts,
            maxFeeFlatSat,
            maxFeePct,
            externalId,
            pathFindingExperimentName,
        ).map { response ->
            val result = if (response.length == 36) {
                InvoiceResult(true, response)
            } else {
                InvoiceResult(false, response)
            }
            Serialization.encode(result)
        }
        resultWriter.write(result)
    }
}