package commands

import ConsoleResultWriter
import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import types.NodeInfo
import types.Serialization

@OptIn(ExperimentalCli::class)
class ConnectCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "connect",
    "Connect to another lightning node. This will perform a connection but no channels will be opened. "
) {
    private val uri by argument(ArgType.String, description = " This API does not require a target address. Instead, eclair will use one of the addresses published by the remote peer in its node_announcement messages.")
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.connect(uri)
        resultWriter.write(result)
    }
}