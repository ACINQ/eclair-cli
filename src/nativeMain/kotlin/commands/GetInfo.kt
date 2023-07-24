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
) : Subcommand(
    "getinfo",
    "Returns information about this node instance such as version, features, nodeId and current block height."
) {
    private var password by option(ArgType.String, shortName = "p", description = "Password for the Eclair API (can be found in eclair.conf)").required()
    private var host by option(ArgType.String, description = "Host URL for the Eclair API (can be found in eclair.conf)").default("http://localhost:8080")

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.getInfo()
            .flatMap { apiResponse -> Serialization.decode<NodeInfo>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}