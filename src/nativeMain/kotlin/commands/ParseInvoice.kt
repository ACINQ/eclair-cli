package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.Invoice
import types.Serialization

class ParseInvoiceCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "parseinvoice",
    "Returns detailed information about the given invoice."
) {
    private val invoice by option(
        ArgType.String,
        description = "The invoice you want to decode"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.parseinvoice(
            invoice = invoice!!,
        )
            .flatMap { apiResponse -> Serialization.decode<Invoice>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}