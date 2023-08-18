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
class FindRouteBetweenNodesCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = FindRouteBetweenNodesCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "findroutebetweennodes",
                "-p",
                "password",
                "--sourceNodeId",
                "03c5b161c16e9f8ef3f3bccfb74a6e9a3b423dd41fe2848174b7209f1c2ea25dad",
                "--targetNodeId",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e",
                "--amountMsat",
                "1000"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter =
            runTest(DummyEclairClient(findroutebetweennodesResponse = DummyEclairClient.validFindRouteBetweenNodesResponse))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(
                FindRouteResponse.serializer(),
                DummyEclairClient.validFindRouteBetweenNodesResponse
            ),
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
    fun `serialization error`() {
        val resultWriter = runTest(DummyEclairClient(findroutebetweennodesResponse = "{invalidJson}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}