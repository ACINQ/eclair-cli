package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.OnChainBalance
import kotlin.test.*

@OptIn(ExperimentalCli::class)
class OnChainBalanceCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = OnChainBalanceCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(arrayOf("onchainbalance", "-p", "password"))
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter =
            runTest(DummyEclairClient(onchainbalanceResponse = DummyEclairClient.validOnChainBalanceResponse))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(OnChainBalance.serializer(), DummyEclairClient.validOnChainBalanceResponse),
            format.decodeFromString(OnChainBalance.serializer(), resultWriter.lastResult!!),
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
        val resultWriter = runTest(DummyEclairClient(onchainbalanceResponse = "{invalid JSON}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}