package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.SendOnChainResult
import types.Serialization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCli::class)
class SendOnChainCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = SendOnChainCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "sendonchain",
                "-p",
                "password",
                "--address",
                "bcrt1qassq4a3xayeza0w6vv47uvnjqq074avqje03v8",
                "--amountSatoshis",
                "1000",
                "--confirmationTarget",
                "1000"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter = runTest(DummyEclairClient())
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val expectedOutput = Serialization.encode(SendOnChainResult(true, DummyEclairClient.validSendOnChainResponse))
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