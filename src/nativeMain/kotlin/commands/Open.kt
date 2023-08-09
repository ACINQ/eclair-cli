package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking

class OpenCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "open",
    "Open a channel to another lightning node. You must specify the target nodeId and the funding satoshis for the new channel. "
) {
    private val nodeId by option(
        ArgType.String,
        description = "The nodeId of the node you want to open a channel with"
    )
    private val fundingSatoshis by option(
        ArgType.Int,
        description = "Amount of satoshis to spend in the funding of the channel"
    )
    private val channelType by option(
        ArgType.String,
        description = "Channel type (standard, static_remotekey, anchor_outputs_zero_fee_htlc_tx)"
    )
    private val pushMsat by option(
        ArgType.Int,
        description = "Amount of millisatoshi to unilaterally push to the counterparty"
    )
    private val fundingFeerateSatByte by option(
        ArgType.Int,
        description = "Feerate in sat/byte to apply to the funding transaction"
    )
    private val announceChannel by option(
        ArgType.Boolean,
        description = "True for public channels, false otherwise"
    )
    private val openTimeoutSeconds by option(
        ArgType.Int,
        description = "Timeout for the operation to complete"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.open(
            nodeId = nodeId!!,
            fundingSatoshis = fundingSatoshis!!,
            channelType = channelType,
            pushMsat = pushMsat,
            fundingFeerateSatByte = fundingFeerateSatByte,
            announceChannel = announceChannel,
            openTimeoutSeconds = openTimeoutSeconds
        )
        resultWriter.write(result)
    }
}