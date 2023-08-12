package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import arrow.core.flatMap
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import types.ApiError
import types.Peer

class PeersCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "peers",
    "Returns the list of currently known peers, both connected and disconnected."
) {
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.peers()
            .flatMap { apiResponse ->
                try {
                    Either.Right(format.decodeFromString<List<Peer>>(apiResponse))
                } catch (e: Throwable) {
                    Either.Left(ApiError(1, "api response could not be parsed: $apiResponse"))
                }
            }
            .map { decoded ->
                format.encodeToString(decoded)
            }
        resultWriter.write(result)
    }
}