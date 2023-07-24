package commands

import ApiError
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCli::class)
class GetInfoCommandTest {
    @Test
    fun `GetInfoCommand success`() {
        val resultWriter = DummyResultWriter()
        val command = GetInfoCommand(resultWriter, DummyEclairClient())
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(arrayOf("getinfo", "-p", "password"))
        assertNull(resultWriter.lastError)
        assertEquals(DummyEclairClient.getInfoResponse, resultWriter.lastResult)
    }

    @Test
    fun `GetInfoCommand failure`() {
        val resultWriter = DummyResultWriter()
        val error = ApiError(42, "test failure message")
        val command = GetInfoCommand(resultWriter, FailingEclairClient(error))
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(arrayOf("getinfo", "-p", "password"))
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }
}