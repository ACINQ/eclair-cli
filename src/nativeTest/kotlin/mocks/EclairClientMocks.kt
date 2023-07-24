package mocks

import ApiError
import IEclairClient
import IEclairClientBuilder
import arrow.core.Either

class DummyEclairClient : IEclairClient, IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = this

    override suspend fun getInfo(): Either<ApiError, String> = Either.Right(getInfoResponse)

    companion object {
        val getInfoResponse =
            """{
                    "version": "0.9.0",
                    "nodeId": "03e319aa4ecc7a89fb8b3feb6efe9075864b91048bff5bef14efd55a69760ddf17",
                    "alias": "alice",
                    "color": "#49daaa",
                    "features": {
                        "activated": {
                            "gossip_queries_ex": "optional",
                            "option_payment_metadata": "optional",
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
                        "unknown": []
                    },
                    "chainHash": "06226e46111a0b59caaf126043eb5bbf28c34f3a5e332a1fc7b2b73cf188910f",
                    "network": "regtest",
                    "blockHeight": 107,
                    "publicAddresses": [],
                    "instanceId": "be74bd9a-fc54-4f24-bc41-0477c9ce2fb4"
        }""".trimIndent()
    }
}

class FailingEclairClient(private val error: ApiError) : IEclairClient, IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = this

    override suspend fun getInfo(): Either<ApiError, String> = Either.Left(error)
}