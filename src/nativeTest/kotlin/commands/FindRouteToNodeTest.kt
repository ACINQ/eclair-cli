package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.FindRouteResponse
import kotlin.test.*

@OptIn(ExperimentalCli::class)
class FindRouteToNodeTestCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder, format: String? = null): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = FindRouteToNodeCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        val arguments = mutableListOf(
            "findroutetonode",
            "-p",
            "password",
            "--nodeId",
            "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e",
            "--amountMsat",
            "1000"
        )
        format?.let { arguments.addAll(listOf("--format", it)) }
        parser.parse(arguments.toTypedArray())
        return resultWriter
    }

    @Test
    fun `successful request via nodeId`() {
        val resultWriter = runTest(DummyEclairClient(), "nodeId")
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(FindRouteResponse.serializer(), DummyEclairClient.validRouteResponseNodeId),
            format.decodeFromString(FindRouteResponse.serializer(), resultWriter.lastResult!!),
        )
    }

    @Test
    fun `successful request via shortChannelId`() {
        val resultWriter = runTest(DummyEclairClient(), "shortChannelId")
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format  = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(FindRouteResponse.serializer(), DummyEclairClient.validRouteResponseShortChannelId),
            format.decodeFromString(FindRouteResponse.serializer(), resultWriter.lastResult!!)
        )
    }

    @Test
    fun `successful request via full`() {
        val resultWriter = runTest(DummyEclairClient(), "full")
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(FindRouteResponse.serializer(), DummyEclairClient.validRouteResponseFull),
            format.decodeFromString(FindRouteResponse.serializer(), resultWriter.lastResult!!),
        )
    }

    @Test
    fun `api error`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(FailingEclairClient(error))
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }

    @Test
    fun `serialization error via nodeId`() {
        val resultWriter = runTest(DummyEclairClient(findrouteResponseNodeId = "{invalidJson}"), "nodeId")
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }

    @Test
    fun `serialization error via shortChannelId`() {
        val resultWriter = runTest(DummyEclairClient(findrouteResponseShortChannelId = "{invalidJson}"), "shortChannelId")
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }

    @Test
    fun `serialization error via full`() {
        val resultWriter = runTest(DummyEclairClient(findrouteResponseFull = "{invalidJson}"), "full")
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}