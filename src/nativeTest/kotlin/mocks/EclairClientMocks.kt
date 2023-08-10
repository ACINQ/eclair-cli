package mocks

import api.ConnectionTarget
import api.IEclairClient
import api.IEclairClientBuilder
import arrow.core.Either
import types.ApiError

class DummyEclairClient(
    private val getInfoResponse: String = validGetInfoResponse,
    private val connectResponse: String = validConnectResponse,
    private val disconnectResponse: String = validDisconnectResponse,
    private val openResponse: String = validOpenResponse,
    private val rbfOpenResponse: String = validRbfOpenResponse,
    private val cpfpbumpfeesResponse: String = validcpfpbumpfeesResponse,
    private val closeResponse: String = validCloseResponse,
    private val forcecloseResponse: String = validForceCloseResponse
) : IEclairClient, IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = this
    override suspend fun getInfo(): Either<ApiError, String> = Either.Right(getInfoResponse)
    override suspend fun connect(target: ConnectionTarget): Either<ApiError, String> = Either.Right(validConnectResponse)
    override suspend fun rbfopen(
        channelId: String,
        targetFeerateSatByte: Int,
        lockTime: Int?
    ): Either<ApiError, String> = Either.Right(rbfOpenResponse)

    override suspend fun disconnect(nodeId: String): Either<ApiError, String> = Either.Right(disconnectResponse)
    override suspend fun open(
        nodeId: String,
        fundingSatoshis: Int,
        channelType: String?,
        pushMsat: Int?,
        fundingFeerateSatByte: Int?,
        announceChannel: Boolean?,
        openTimeoutSeconds: Int?
    ): Either<ApiError, String> = Either.Right(openResponse)

    override suspend fun cpfpbumpfees(outpoints: List<String>, targetFeerateSatByte: Int): Either<ApiError, String> =
        Either.Right(cpfpbumpfeesResponse)

    override suspend fun close(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
        scriptPubKey: String?,
        preferredFeerateSatByte: Int?,
        minFeerateSatByte: Int?,
        maxFeerateSatByte: Int?
    ): Either<ApiError, String> = Either.Right(closeResponse)

    override suspend fun forceclose(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
    ): Either<ApiError, String> = Either.Right(forcecloseResponse)

    companion object {
        val validGetInfoResponse =
            """{"version":"0.9.0","nodeId":"03e319aa4ecc7a89fb8b3feb6efe9075864b91048bff5bef14efd55a69760ddf17","alias":"alice","color":"#49daaa","features":{"activated":{"var_onion_optin":"mandatory","option_static_remotekey":"optional"},"unknown":[151,178]},"chainHash":"06226e46111a0b59caaf126043eb5bbf28c34f3a5e332a1fc7b2b73cf188910f","network":"regtest","blockHeight":107,"publicAddresses":[],"instanceId":"be74bd9a-fc54-4f24-bc41-0477c9ce2fb4"}"""
        val invalidGetInfoResponse = """{"version":42}"""
        val validConnectResponse = "connected"
        val validDisconnectResponse =
            "peer 02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e disconnecting"
        val validOpenResponse =
            "created channel e872f515dc5d8a3d61ccbd2127f33141eaa115807271dcc5c5c727f3eca914d3 with fundingTxId=bc2b8db55b9588d3a18bd06bd0e284f63ee8cc149c63138d51ac8ef81a72fc6f and fees=720 sat"
        val validRbfOpenResponse = "ok"
        val validcpfpbumpfeesResponse = "83d4f64bd3f7708caad602de0c372a94fcdc50f128519c9505169013215f598f"
        val validCloseResponse = "{\n" +
                "  \"<channel>\": \"ok\"\n" +
                "}"
        val validForceCloseResponse = "{\n" +
                "  \"<channel>\": \"ok\"\n" +
                "}"
    }
}

class FailingEclairClient(private val error: ApiError) : IEclairClient, IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = this
    override suspend fun getInfo(): Either<ApiError, String> = Either.Left(error)
    override suspend fun connect(target: ConnectionTarget): Either<ApiError, String> = Either.Left(error)
    override suspend fun disconnect(nodeId: String): Either<ApiError, String> = Either.Left(error)
    override suspend fun open(
        nodeId: String,
        fundingSatoshis: Int,
        channelType: String?,
        pushMsat: Int?,
        fundingFeerateSatByte: Int?,
        announceChannel: Boolean?,
        openTimeoutSeconds: Int?
    ): Either<ApiError, String> = Either.Left(error)

    override suspend fun rbfopen(
        channelId: String,
        targetFeerateSatByte: Int,
        lockTime: Int?
    ): Either<ApiError, String> = Either.Left(error)

    override suspend fun cpfpbumpfees(outpoints: List<String>, targetFeerateSatByte: Int): Either<ApiError, String> =
        Either.Left(error)

    override suspend fun close(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
        scriptPubKey: String?,
        preferredFeerateSatByte: Int?,
        minFeerateSatByte: Int?,
        maxFeerateSatByte: Int?
    ): Either<ApiError, String> = Either.Left(error)

    override suspend fun forceclose(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
    ): Either<ApiError, String> = Either.Left(error)
}