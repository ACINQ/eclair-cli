import commands.GetInfoCommand
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetInfoCommandTest {
    @OptIn(ExperimentalCli::class)
    @Test
    fun `test execute GetInfoCommand success`() {
        val resultWriter = DummyResultWriter()
        val command = GetInfoCommand(resultWriter, DummyEclairClient())
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(arrayOf("getinfo", "-p", "password"))
        assertNull(resultWriter.lastError)
        assertEquals(DummyEclairClient.mockResponse, resultWriter.lastResult)
    }

    @OptIn(ExperimentalCli::class)
    @Test
    fun `test execute GetInfoCommand failure`() {
        val resultWriter = DummyResultWriter()
        val command = GetInfoCommand(resultWriter, FailingEclairClient(Exception("Test failure message")))
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(arrayOf("getinfo", "-p", "password"))
        assertNull(resultWriter.lastResult)
        assertEquals("Error fetching information: Test failure message", resultWriter.lastError)
    }
}