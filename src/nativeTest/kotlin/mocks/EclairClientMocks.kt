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
    private val forcecloseResponse: String = validForceCloseResponse,
    private val updateRelayFeeResponse: String = validUpdateRelayFeeResponse,
    private val peersResponse: String = validPeersResponse,
    private val nodesResponse: String = validNodesResponse,
    private val nodeResponse: String = validNodeResponse,
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

    override suspend fun updaterelayfee(
        nodeId: String?,
        nodeIds: List<String>?,
        feeBaseMsat: Int,
        feeProportionalMillionths: Int
    ): Either<ApiError, String> = Either.Right(updateRelayFeeResponse)

    override suspend fun peers(): Either<ApiError, String> = Either.Right(peersResponse)

    override suspend fun nodes(nodeIds: List<String>?): Either<ApiError, String> = Either.Right(nodesResponse)

    override suspend fun node(nodeId: String): Either<ApiError, String> = Either.Right(nodeResponse)


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
        val validCloseResponse = """{
    "b7f194155be377e8c4b8fb3a8e8c465f6e7506b875e56c2a4bc8ef57df380641": "closed channel b7f194155be377e8c4b8fb3a8e8c465f6e7506b875e56c2a4bc8ef57df380641"
}
"""
        val validForceCloseResponse = """{
    "b7f194155be377e8c4b8fb3a8e8c465f6e7506b875e56c2a4bc8ef57df380641": "closed channel b7f194155be377e8c4b8fb3a8e8c465f6e7506b875e56c2a4bc8ef57df380641"
}
"""
        val validUpdateRelayFeeResponse =
            """{"72ced03de5af1d82d17ea56b001b6ca70f84b81b89f34daed34975130685472c":"ok","9c894bf206ff533b4c335afdef207ede4618023a47c8e77721dcc731f5d56beb":"ok","34ff2892512058a6cd26749ed812da4aa8e16f5294527b05197b6ff181554be1":"ok","9ff6547359f841daf68730db7d4e41284fa6a38c80756f86e9cef2ce48340223":"ok","6af519c91261ddd2547c136d07e23a85db031cc8a04b92d14a9f55e2bf23a0f7":"ok","825366d6aea22b4f4abd55a997f9e333aff8dc69499c4cb8208d3e9bff93710f":"ok","c872066be83c163872864f9fcdd6ac2c8a2090fb1c65dd610495dddcf10279a1":"ok","b7f194155be377e8c4b8fb3a8e8c465f6e7506b875e56c2a4bc8ef57df380641":"ok","2f17330de81032c0efaf59f47ec7434267e8d02d638977c485d2148707ce8a56":"ok","62ba3cc9354cf0df5746972926434f4198f60de938a46bb345dd2c44157a2eb5":"ok","3cb0a0652a2005645f9aae3e333b975434e100d9a0663c418177f7c3c9c0bb41":"ok","cd990ca3f6a44f125165fa57011f45a4f37c8eacf7c44c724e0b1277eddb538f":"ok"}"""
        val validPeersResponse = """[
   {
      "nodeId":"03864ef025fde8fb587d989186ce6a4a186895ee44a926bfc370e2c366597a3f8f",
      "state":"CONNECTED",
      "address":"34.239.230.56:9735",
      "channels":1
   },
   {
      "nodeId":"039dc0e0b1d25905e44fdf6f8e89755a5e219685840d0bc1d28d3308f9628a3585",
      "state":"DISCONNECTED",
      "channels":1
   }
]"""
        val validNodesResponse = """[
  {
    "signature": "c466c08fa16c1810e2971de2a57ef1f9e5e13d36a224544cf0e3d621030b9e617652b88fb2024bfdc60066ca63b4f67504f154e8fee7f13bc39739b76cc4419f",
    "features": {
      "activated": {
        "option_onion_messages": "optional",
        "gossip_queries_ex": "optional",
        "option_data_loss_protect": "optional",
        "var_onion_optin": "mandatory",
        "option_static_remotekey": "optional",
        "option_support_large_channel": "optional",
        "option_anchors_zero_fee_htlc_tx": "optional",
        "payment_secret": "mandatory",
        "option_shutdown_anysegwit": "optional",
        "option_channel_type": "optional",
        "basic_mpp": "optional",
        "gossip_queries": "optional"
      },
      "unknown": []
    },
    "timestamp": {
      "iso": "2022-02-01T12:27:19Z",
      "unix": 1643718439
    },
    "nodeId": "028e2403fbfddb3d787843361f91adbda64c6f622921b19fb48f5766508bcadb29",
    "rgbColor": "#49daaa",
    "alias": "alice",
    "addresses": [
      "138.229.205.237:9735"
    ],
    "tlvStream": {}
  },
  {
    "signature": "f6cce33383fe1291fa60cfa7d9efa4a45c081396e445e9cadc825ab695aab30308a68733d27fc54a5c46b888bdddd467f30f2f5441e95c2920b3b6c54decc3a1",
    "features": {
      "activated": {
        "option_onion_messages": "optional",
        "gossip_queries_ex": "optional",
        "option_data_loss_protect": "optional",
        "var_onion_optin": "mandatory",
        "option_static_remotekey": "optional",
        "option_support_large_channel": "optional",
        "option_anchors_zero_fee_htlc_tx": "optional",
        "payment_secret": "mandatory",
        "option_shutdown_anysegwit": "optional",
        "option_channel_type": "optional",
        "basic_mpp": "optional",
        "gossip_queries": "optional"
      },
      "unknown": []
    },
    "timestamp": {
      "iso": "2022-02-01T12:27:19Z",
      "unix": 1643718439
    },
    "nodeId": "02fe677ac8cd61399d097535a3e8a51a0849e57cdbab9b34796c86f3e33568cbe2",
    "rgbColor": "#49daaa",
    "alias": "bob",
    "addresses": [
      "95.216.16.21:9735",
      "[2a01:4f9:2a:106a:0:0:0:2]:9736"
    ],
    "tlvStream": {}
  }
]"""
        val validNodeResponse = """{
  "type": "types.NodeCommandResponse",
  "announcement": {
    "signature": "327c3bd0933e98bd5e65d24e1e1f6aae310f8c1606544dba6cb587b95542d9111eb9abf0a34283ef007e04ed7b003bef3bdb74896a8e34fe86b0fb0734f7efc7",
    "features": {
      "activated": {
        "gossip_queries_ex": "optional",
        "option_data_loss_protect": "optional",
        "var_onion_optin": "mandatory",
        "option_static_remotekey": "optional",
        "option_scid_alias": "optional",
        "option_onion_messages": "optional",
        "option_support_large_channel": "optional",
        "option_anchors_zero_fee_htlc_tx": "optional",
        "payment_secret": "mandatory",
        "option_shutdown_anysegwit": "optional",
        "option_channel_type": "optional",
        "basic_mpp": "optional",
        "gossip_queries": "optional"
      },
      "unknown": [
      ]
    },
    "timestamp": {
      "iso": "2023-08-14T10:27:13Z",
      "unix": 1692008833
    },
    "nodeId": "02f666711319435b7905dd77d10c269d8d50c02668b975f526577167d370b50a3e",
    "rgbColor": "#49daaa",
    "alias": "bob",
    "addresses": [
    ],
    "tlvStream": {
    }
  }
}
"""
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

    override suspend fun updaterelayfee(
        nodeId: String?,
        nodeIds: List<String>?,
        feeBaseMsat: Int,
        feeProportionalMillionths: Int
    ): Either<ApiError, String> = Either.Left(error)

    override suspend fun peers(): Either<ApiError, String> = Either.Left(error)

    override suspend fun nodes(nodeIds: List<String>?): Either<ApiError, String> = Either.Left(error)

    override suspend fun node(nodeId: String): Either<ApiError, String> = Either.Left(error)
}