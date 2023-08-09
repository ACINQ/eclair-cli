package commands

import api.ConnectionTarget
import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.ConnectionResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCli::class)
class ConnectCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = ConnectCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "connect",
                "-p",
                "password",
                "--uri",
                "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e@127.0.0.1:9736"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request via URI`() {
        val target =
            ConnectionTarget.Uri("02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e@127.0.0.1:9736")
        val resultWriter =
            runTest(DummyEclairClient(connectResponseMap = mapOf(target to DummyEclairClient.validConnectResponse)))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(ConnectionResult.serializer(), DummyEclairClient.validConnectResponse),
            format.decodeFromString(ConnectionResult.serializer(), resultWriter.lastResult!!),
        )
    }

    @Test
    fun `successful request via NodeId`() {
        val resultWriter = runTest(
            DummyEclairClient(),
        )
        assertNull(resultWriter.lastError)
        assertEquals(DummyEclairClient.validConnectResponse, resultWriter.lastResult)
    }

    @Test
    fun `successful request via Manual`() {
        val resultWriter = runTest(
            DummyEclairClient(),

            )
        assertNull(resultWriter.lastError)
        assertEquals(DummyEclairClient.validConnectResponse, resultWriter.lastResult)
    }

    @Test
    fun `api error via URI`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(
            FailingEclairClient(error),

            )
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }

    @Test
    fun `api error via NodeId`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(
            FailingEclairClient(error),

            )
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }

    @Test
    fun `api error via Manual`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(
            FailingEclairClient(error),
        )
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }
}