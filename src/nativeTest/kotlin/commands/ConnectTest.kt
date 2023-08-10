package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.ConnectionResult
import types.Serialization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCli::class)
class ConnectCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder, options: Array<String>): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = ConnectCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(options)
        return resultWriter
    }

    @Test
    fun `successful request via URI`() {
        val resultWriter = runTest(
            DummyEclairClient(),
            arrayOf(
                "connect",
                "--host",
                "http://localhost:8080",
                "-p",
                "password",
                "--uri",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e@127.0.0.1:9736"
            )
        )
        assertNull(resultWriter.lastError)
        val expectedOutput = Serialization.encode(ConnectionResult(true))
        assertEquals(expectedOutput, resultWriter.lastResult)
    }

    @Test
    fun `successful request via NodeId`() {
        val resultWriter = runTest(
            DummyEclairClient(),
            arrayOf(
                "connect",
                "--host",
                "http://localhost:8080",
                "-p",
                "password",
                "--nodeId",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e"
            )
        )
        assertNull(resultWriter.lastError)
        val expectedOutput = Serialization.encode(ConnectionResult(true))
        assertEquals(expectedOutput, resultWriter.lastResult)
    }

    @Test
    fun `successful request via Manual`() {
        val resultWriter = runTest(
            DummyEclairClient(),
            arrayOf(
                "connect",
                "--host",
                "http://localhost:8080",
                "-p",
                "password",
                "--nodeId",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e"

            )
        )
        assertNull(resultWriter.lastError)
        val expectedOutput = Serialization.encode(ConnectionResult(true))
        assertEquals(expectedOutput, resultWriter.lastResult)
    }

    @Test
    fun `api error via URI`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(
            FailingEclairClient(error),
            arrayOf(
                "connect",
                "--host",
                "http://localhost:8080",
                "-p",
                "password",
                "--uri",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e@127.0.0.1:9736"
            )
        )
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }

    @Test
    fun `api error via NodeId`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(
            FailingEclairClient(error),
            arrayOf(
                "connect",
                "--host",
                "http://localhost:8080",
                "-p",
                "password",
                "--nodeId",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e"
            )

        )
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }

    @Test
    fun `api error via Manual`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(
            FailingEclairClient(error),
            arrayOf(
                "connect",
                "--host",
                "http://localhost:8080",
                "-p",
                "password",
                "--nodeId",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e",
                "--address",
                "127.0.0.1"
            )
        )
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }
}