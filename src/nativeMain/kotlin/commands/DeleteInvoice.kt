package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.DeleteInvoiceResult
import types.Serialization

class DeleteInvoiceCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "deleteinvoice",
    "Delete an unpaid BOLT11 payment invoice."
) {
    private val paymentHash by option(
        ArgType.String,
        description = "The payment hash of the invoice"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.deleteinvoice(paymentHash!!).map { response ->
            val result = if (response.contains("deleted invoice")) {
                DeleteInvoiceResult(true, response)
            } else {
                DeleteInvoiceResult(false, response)
            }
            Serialization.encode(result)
        }
        resultWriter.write(result)
    }
}