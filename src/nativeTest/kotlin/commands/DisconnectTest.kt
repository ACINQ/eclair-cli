package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.DisconnectionResult
import types.Serialization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCli::class)
class DisconnectCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = DisconnectCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "disconnect",
                "-p",
                "password",
                "--nodeId",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter = runTest(DummyEclairClient())
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val expectedOutput = Serialization.encode(DisconnectionResult(true, DummyEclairClient.validDisconnectResponse))
        assertEquals(expectedOutput, resultWriter.lastResult)
    }

    @Test
    fun `api error`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(FailingEclairClient(error))
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }
}