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
class FindRouteCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder, format: String? = null): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = FindRouteCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        val arguments = mutableListOf(
            "findroute",
            "-p",
            "password",
            "--invoice",
            "lnbcrt10n1pjduajwpp5s6dsm9vk3q0ntxeq2zd6d4jz8mv8wau75dugud5puc3lltwp68esdqsd3shgetnw33k7er9sp5rye5z7eccrg7kx9jj6u24q2aumgl09e0e894w6hdceyk60g7a2hsmqz9gxqrrsscqp79q7sqqqqqqqqqqqqqqqqqqqsqqqqqysgq9fktzq8fpyey9js0x85t6s5mtcwqzmmd4ql9cjq04f4tunlysje894mdcmhjwkewrk5wn2ylv3da64pda7tj04s3m90en5t6p7yyglgpue2lzz"
        )
        format?.let { arguments.addAll(listOf("--format", it)) }
        parser.parse(arguments.toTypedArray())
        return resultWriter
    }

    @Test
    fun `successful request via nodeId`() {
        val resultWriter = runTest(DummyEclairClient(), "nodeId")
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(FindRouteResponse.serializer(), DummyEclairClient.validRouteResponseNodeId),
            format.decodeFromString(FindRouteResponse.serializer(), resultWriter.lastResult!!),
        )
    }

    @Test
    fun `successful request via shortChannelId`() {
        val resultWriter = runTest(DummyEclairClient(), "shortChannelId")
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(FindRouteResponse.serializer(), DummyEclairClient.validRouteResponseShortChannelId),
            format.decodeFromString(FindRouteResponse.serializer(), resultWriter.lastResult!!),
        )
    }

    @Test
    fun `successful request via full`() {
        val resultWriter = runTest(DummyEclairClient(), "full")
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(FindRouteResponse.serializer(), DummyEclairClient.validRouteResponseFull),
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
    fun `serialization error via nodeId`() {
        val resultWriter = runTest(DummyEclairClient(findrouteResponseNodeId = "{invalidJson}"), "nodeId")
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }

    @Test
    fun `serialization error via shortChannelId`() {
        val resultWriter = runTest(DummyEclairClient(findrouteResponseShortChannelId = "{invalidJson}"), "shortChannelId")
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }

    @Test
    fun `serialization error via full`() {
        val resultWriter = runTest(DummyEclairClient(findrouteResponseFull = "{invalidJson}"), "full")
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}