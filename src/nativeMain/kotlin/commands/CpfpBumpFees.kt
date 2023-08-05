package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking


class CpfpBumpFeesCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "cpfpbumpfees",
    "Increase the fees of a set of unconfirmed transactions by publishing a high-fee child transaction. The targetFeerateSatByte will be applied to the whole package containing the unconfirmed transactions and the child transaction. You must identify the set of outpoints that belong to your bitcoin wallet in the unconfirmed transactions (usually change outputs). This command returns the txid of the child transaction that was published."
) {
    var outpoints by option(
        ArgType.String,
        description = "CSV list of outpoints (txid:vout) that should be spent by the child transaction"
    )
    var targetFeerateSatByte by option(
        ArgType.Int,
        description = "Feerate in sat/byte to apply to the unconfirmed transactions"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.cpfpbumpfees(
            outpoints = outpoints?.split(",")!!,
            targetFeerateSatByte = targetFeerateSatByte!!
        )
        resultWriter.write(result)
    }
}