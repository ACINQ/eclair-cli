package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CpfpBumpFeesCommandTest {
    @OptIn(ExperimentalCli::class)
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = CpfpBumpFeesCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "cpfpbumpfees",
                "-p",
                "password",
                "--outpoints",
                "30b6a6d09e504d4ae9fe992af8fd583b16ec8e8b632de5690ab273d64bbd4070:0,30b6a6d09e504d4ae9fe992af8fd583b16ec8e8b632de5690ab273d64bbd4070:1",
                "--targetFeerateSatByte",
                "10"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter = runTest(DummyEclairClient())
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        assertEquals(DummyEclairClient.validcpfpbumpfeesResponse, resultWriter.lastResult)
    }

    @Test
    fun `api error`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(FailingEclairClient(error))
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }
}