package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import types.NodeInfo
import types.Serialization

@OptIn(ExperimentalCli::class)
class GetInfoCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "getinfo",
    "Returns information about this node instance such as version, features, nodeId and current block height."
) {
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.getInfo()
            .flatMap { apiResponse -> Serialization.decode<NodeInfo>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}