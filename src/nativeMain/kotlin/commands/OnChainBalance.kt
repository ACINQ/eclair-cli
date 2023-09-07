package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.coroutines.runBlocking
import types.OnChainBalance
import types.Serialization

class OnChainBalanceCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "onchainbalance",
    "Retrieves information about the available on-chain bitcoin balance (amounts are in satoshis). Unconfirmed balance refers to incoming transactions seen in the mempool."
) {
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.onchainbalance()
            .flatMap { apiResponse -> Serialization.decode<OnChainBalance>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}