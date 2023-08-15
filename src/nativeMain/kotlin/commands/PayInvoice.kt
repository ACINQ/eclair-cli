package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.InvoiceResult
import types.Serialization

class PayInvoiceCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "payinvoice",
    "Pays a BOLT11 invoice. In case of failure, the payment will be retried up to maxAttempts times."
) {
    private val invoice by option(
        ArgType.String,
        description = "The invoice you want to pay"
    )
    private val amountMsat by option(
        ArgType.Int,
        description = "Amount to pay if the invoice does not have one"
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
        description = "Max percentage to be paid in fees along the payment route (ignored if below maxFeeFlatSat)"
    )
    private val externalId by option(
        ArgType.String,
        description = "Extra payment identifier specified by the caller"
    )
    private val pathFindingExperimentName by option(
        ArgType.String,
        description = "Name of the path-finding configuration that should be used"
    )
    private val blocking by option(
        ArgType.Boolean,
        description = "Block until the payment completes"
    )


    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)

        val result = eclairClient.payinvoice(
            invoice!!,
            amountMsat,
            maxAttempts,
            maxFeeFlatSat,
            maxFeePct,
            externalId,
            pathFindingExperimentName,
            blocking
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