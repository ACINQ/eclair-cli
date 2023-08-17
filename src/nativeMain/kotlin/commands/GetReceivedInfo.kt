package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.ReceivedInfoResponse
import types.Serialization

class GetReceivedInfoCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "getreceivedinfo",
    "Checks whether a payment corresponding to the given paymentHash has been received. It is possible to use a BOLT11 invoice as parameter instead of the paymentHash but at least one of the two must be specified."
) {
    private val paymentHash by option(
        ArgType.String,
        description = "The payment hash you want to check."
    )
    private val invoice by option(
        ArgType.String,
        description = "The invoice containing the payment hash."
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.getreceivedinfo(
            paymentHash = paymentHash,
            invoice = invoice
        )
            .flatMap { apiResponse -> Serialization.decode<ReceivedInfoResponse>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}