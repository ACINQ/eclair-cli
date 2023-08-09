package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking

class RbfOpenCommand(
    private val resultWriter: IResultWriter, private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "rbfopen", "Increase the fees of an unconfirmed dual-funded channel to speed up confirmation."
) {
    private val channelId by option(
        ArgType.String, description = "The channelId of the channel that should be RBF-ed"
    )
    private val targetFeerateSatByte by option(
        ArgType.Int, description = "Feerate in sat/byte to apply to the funding transaction"
    )
    private val lockTime by option(
        ArgType.Int, description = "The nLockTime to apply to the funding transaction"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.rbfopen(
            channelId = channelId!!,
            targetFeerateSatByte = targetFeerateSatByte!!,
            lockTime = lockTime
        )
        resultWriter.write(result)
    }
}