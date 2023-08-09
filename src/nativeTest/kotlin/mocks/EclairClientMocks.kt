package mocks

import api.ConnectionTarget
import api.IEclairClient
import api.IEclairClientBuilder
import arrow.core.Either
import types.ApiError

class DummyEclairClient(
    private val getInfoResponse: String = validGetInfoResponse,
    private val connectResponseMap: Map<ConnectionTarget, String> = mapOf(),
    private val disconnectResponse: String = validDisconnectResponse
) : IEclairClient, IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = this
    override suspend fun getInfo(): Either<ApiError, String> = Either.Right(getInfoResponse)
    override suspend fun connect(target: ConnectionTarget): Either<ApiError, String> =
        Either.Right(connectResponseMap[target] ?: validConnectResponse)

    override suspend fun disconnect(nodeId: String): Either<ApiError, String> = Either.Right(disconnectResponse)

    companion object {
        val validGetInfoResponse =
            """{"version":"0.9.0","nodeId":"03e319aa4ecc7a89fb8b3feb6efe9075864b91048bff5bef14efd55a69760ddf17","alias":"alice","color":"#49daaa","features":{"activated":{"var_onion_optin":"mandatory","option_static_remotekey":"optional"},"unknown":[151,178]},"chainHash":"06226e46111a0b59caaf126043eb5bbf28c34f3a5e332a1fc7b2b73cf188910f","network":"regtest","blockHeight":107,"publicAddresses":[],"instanceId":"be74bd9a-fc54-4f24-bc41-0477c9ce2fb4"}"""
        val invalidGetInfoResponse = """{"version":42}"""
        val validConnectResponse = """{
  "type": "types.ConnectionResult",
  "success": true
}"""
        val validDisconnectResponse =
            "peer 02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e disconnecting"
    }
}

class FailingEclairClient(private val error: ApiError) : IEclairClient, IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = this
    override suspend fun getInfo(): Either<ApiError, String> = Either.Left(error)
    override suspend fun connect(target: ConnectionTarget): Either<ApiError, String> = Either.Left(error)
    override suspend fun disconnect(nodeId: String): Either<ApiError, String> = Either.Left(error)
}