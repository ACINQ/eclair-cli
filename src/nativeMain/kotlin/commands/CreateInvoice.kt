package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.Invoice
import types.Serialization

class CreateInvoiceCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "createinvoice",
    "Create a BOLT11 payment invoice."
) {
    private val description by option(
        ArgType.String,
        description = "A description for the invoice"
    )
    private val descriptionHash by option(
        ArgType.String,
        description = "Hash of the description for the invoice"
    )
    private val amountMsat by option(
        ArgType.Int,
        description = "Amount in millisatoshi for this invoice"
    )
    private val expireIn by option(
        ArgType.Int,
        description = "Number of seconds that the invoice will be valid"
    )
    private val fallbackAddress by option(
        ArgType.String,
        description = "An on-chain fallback address to receive the payment"
    )
    private val paymentPreimage by option(
        ArgType.String,
        description = "A user defined input for the generation of the paymentHash"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.createinvoice(
            description = description,
            descriptionHash = descriptionHash,
            amountMsat = amountMsat,
            expireIn = expireIn,
            fallbackAddress = fallbackAddress,
            paymentPreimage = paymentPreimage
        )
            .flatMap { apiResponse -> Serialization.decode<Invoice>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}