package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.FindRouteResponse
import types.Serialization

class FindRouteCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "findroute",
    "Finds a route to the node specified by the invoice. The formats currently supported are nodeId, shortChannelId or full"
) {
    private val invoice by option(
        ArgType.String,
        description = "The invoice containing the destination"
    )
    private val amountMsat by option(
        ArgType.Int,
        description = "The amount that should go through the route"
    )
    private val ignoreNodeIds by option(
        ArgType.String,
        description = "A list of nodes to exclude from path-finding"
    )
    private val ignoreShortChannelIds by option(
        ArgType.String,
        description = "A list of channels to exclude from path-finding"
    )
    private val format by option(
        ArgType.String,
        description = "Format that will be used for the resulting route"
    )
    private val maxFeeMsat by option(
        ArgType.Int,
        description = "Maximum fee allowed for this payment"
    )
    private val includeLocalChannelCost by option(
        ArgType.Boolean,
        description = "If true, the relay fees of local channels will be counted"
    )
    private val pathFindingExperimentName by option(
        ArgType.String,
        description = "Name of the path-finding configuration that should be used"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.findroute(
            invoice!!,
            amountMsat,
            ignoreNodeIds?.split(","),
            ignoreShortChannelIds?.split(","),
            format,
            maxFeeMsat,
            includeLocalChannelCost,
            pathFindingExperimentName
        ).flatMap { apiResponse -> Serialization.decode<FindRouteResponse>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}