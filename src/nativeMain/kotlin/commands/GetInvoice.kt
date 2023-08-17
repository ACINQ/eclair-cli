package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.Invoice
import types.Serialization

class GetInvoiceCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "getinvoice",
    "Queries the payment DB for a stored invoice with the given paymentHash. If none is found, it responds HTTP 404."
) {
    private val paymentHash by option(
        ArgType.String,
        description = "The payment hash of the invoice you want to retrieve"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.getinvoice(
            paymentHash = paymentHash!!,
        ).flatMap { apiResponse -> Serialization.decode<Invoice>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}