package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import kotlin.test.*

class CloseCommandTest {
    @OptIn(ExperimentalCli::class)
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = CloseCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "close",
                "-p",
                "password",
                "--channelId",
                "b7f194155be377e8c4b8fb3a8e8c465f6e7506b875e56c2a4bc8ef57df380641"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter = runTest(DummyEclairClient())
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        assertEquals(
            format.parseToJsonElement(DummyEclairClient.validCloseResponse),
            format.parseToJsonElement(resultWriter.lastResult!!),
        )
    }

    @Test
    fun `api error`() {
        val error = ApiError(43, "close failure message")
        val resultWriter = runTest(FailingEclairClient(error))
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }

    @Test
    fun `serialization error`() {
        val resultWriter = runTest(DummyEclairClient(closeResponse = "{invalidJson}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}

