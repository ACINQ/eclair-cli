package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.required
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCli::class)
class DisconnectCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "disconnect",
    "Disconnect from a peer."
) {
    private val nodeId by option(ArgType.String, description = "Disconnect from a connected peer.").required()
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.disconnect(nodeId)
        resultWriter.write(result)
    }
}