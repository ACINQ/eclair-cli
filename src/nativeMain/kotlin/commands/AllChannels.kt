package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import arrow.core.flatMap
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import types.AllChannels
import types.ApiError

class AllChannelsCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "allchannels",
    "Returns non-detailed information about all public channels in the network."
) {
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.allchannels()
            .flatMap { apiResponse ->
                try {
                    Either.Right(format.decodeFromString<List<AllChannels>>(apiResponse))
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