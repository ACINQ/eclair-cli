package api

import arrow.core.Either
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import types.ApiError

interface IEclairClientBuilder {
    fun build(apiHost: String, apiPassword: String): IEclairClient
}

class EclairClientBuilder : IEclairClientBuilder {
    override fun build(apiHost: String, apiPassword: String): IEclairClient = EclairClient(apiHost, apiPassword)
}

sealed class ConnectionTarget {
    data class Uri(val uri: String) : ConnectionTarget()
    data class NodeId(val nodeId: String) : ConnectionTarget()
    data class Manual(val nodeId: String, val address: String, val port: Int? = null) : ConnectionTarget()
}

interface IEclairClient {
    suspend fun getInfo(): Either<ApiError, String>
    suspend fun connect(target: ConnectionTarget): Either<ApiError, String>
    suspend fun disconnect(nodeId: String): Either<ApiError, String>
    suspend fun open(
        nodeId: String,
        fundingSatoshis: Int,
        channelType: String?,
        pushMsat: Int?,
        fundingFeerateSatByte: Int?,
        announceChannel: Boolean?,
        openTimeoutSeconds: Int?
    ): Either<ApiError, String>

    suspend fun rbfopen(
        channelId: String,
        targetFeerateSatByte: Int,
        lockTime: Int?
    ): Either<ApiError, String>

    suspend fun cpfpbumpfees(outpoints: List<String>, targetFeerateSatByte: Int): Either<ApiError, String>

    suspend fun close(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
        scriptPubKey: String?,
        preferredFeerateSatByte: Int?,
        minFeerateSatByte: Int?,
        maxFeerateSatByte: Int?
    ): Either<ApiError, String>

    suspend fun forceclose(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
    ): Either<ApiError, String>

    suspend fun updaterelayfee(
        nodeId: String?,
        nodeIds: List<String>?,
        feeBaseMsat: Int,
        feeProportionalMillionths: Int
    ): Either<ApiError, String>

    suspend fun peers(): Either<ApiError, String>

    suspend fun nodes(nodeIds: List<String>?): Either<ApiError, String>

    suspend fun node(nodeId: String): Either<ApiError, String>

    suspend fun allchannels(): Either<ApiError, String>

    suspend fun allupdates(nodeId: String?): Either<ApiError, String>

    suspend fun createinvoice(
        description: String?,
        descriptionHash: String?,
        amountMsat: Int?,
        expireIn: Int?,
        fallbackAddress: String?,
        paymentPreimage: String?,
    ): Either<ApiError, String>

    suspend fun deleteinvoice(paymentHash: String): Either<ApiError, String>

    suspend fun parseinvoice(invoice: String): Either<ApiError, String>

    suspend fun payinvoice(
        invoice: String,
        amountMsat: Int?,
        maxAttempts: Int?,
        maxFeeFlatSat: Int?,
        maxFeePct: Int?,
        externalId: String?,
        pathFindingExperimentName: String?,
        blocking: Boolean?,
    ): Either<ApiError, String>

    suspend fun sendtonode(
        nodeId: String,
        amountMsat: Int,
        maxAttempts: Int?,
        maxFeeFlatSat: Int?,
        maxFeePct: Int?,
        externalId: String?,
        pathFindingExperimentName: String?,
    ): Either<ApiError, String>

    suspend fun sendtoroute(
        invoice: String,
        nodeIds: List<String>?,
        shortChannelIds: List<String>?,
        amountMsat: Int,
        paymentHash: String,
        finalCltvExpiry: Int,
        maxFeeMsat: Int?,
        recipientAmountMsat: Int?,
        parentId: String?,
        externalId: String?
    ): Either<ApiError, String>

    suspend fun getsentinfo(paymentHash: String, id: String?): Either<ApiError, String>

    suspend fun getreceivedinfo(paymentHash: String?, invoice: String?): Either<ApiError, String>

    suspend fun listreceivedpayments(from: Int?, to: Int?, count: Int?, skip: Int?): Either<ApiError, String>

    suspend fun getinvoice(paymentHash: String): Either<ApiError, String>

    suspend fun listinvoices(
        from: Int?,
        to: Int?,
        count: Int?,
        skip: Int?
    ): Either<ApiError, String>

    suspend fun listpendinginvoices(
        from: Int?,
        to: Int?,
        count: Int?,
        skip: Int?
    ): Either<ApiError, String>

    suspend fun findroute(
        invoice: String,
        amountMsat: Int?,
        ignoreNodeIds: List<String>?,
        ignoreShortChannelIds: List<String>?,
        format: String?,
        maxFeeMsat: Int?,
        includeLocalChannelCost: Boolean?,
        pathFindingExperimentName: String?
    ): Either<ApiError, String>


    suspend fun findroutetonode(
        nodeId: String,
        amountMsat: Int,
        ignoreNodeIds: List<String>?,
        ignoreShortChannelIds: List<String>?,
        format: String?,
        maxFeeMsat: Int?,
        includeLocalChannelCost: Boolean?,
        pathFindingExperimentName: String?
    ): Either<ApiError, String>
}

class EclairClient(private val apiHost: String, private val apiPassword: String) : IEclairClient {

    private val httpClient = HttpClient(CIO) {
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = "", password = apiPassword)
                }
            }
        }
        install(ContentNegotiation) {
            json()
        }
    }

    private fun convertHttpError(statusCode: HttpStatusCode): ApiError {
        return when (statusCode) {
            HttpStatusCode.Unauthorized -> ApiError(statusCode.value, "invalid api password")
            HttpStatusCode.BadRequest -> ApiError(statusCode.value, "invalid request parameters")
            else -> ApiError(statusCode.value, "api error")
        }
    }

    override suspend fun getInfo(): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.post("$apiHost/getinfo")
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(response.bodyAsText())
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "unknown exception"))
        }
    }

    override suspend fun connect(target: ConnectionTarget): Either<ApiError, String> {
        return try {
            val response: HttpResponse = when (target) {
                is ConnectionTarget.Uri -> httpClient.submitForm(
                    url = "${apiHost}/connect",
                    formParameters = Parameters.build {
                        append("uri", target.uri)
                    }
                )

                is ConnectionTarget.NodeId -> httpClient.submitForm(
                    url = "${apiHost}/connect",
                    formParameters = Parameters.build {
                        append("nodeId", target.nodeId)
                    }
                )

                is ConnectionTarget.Manual -> httpClient.submitForm(
                    url = "${apiHost}/connect",
                    formParameters = Parameters.build {
                        append("nodeId", target.nodeId)
                        append("address", target.address)
                        target.port?.let { append("port", it.toString()) }
                    }
                )
            }
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun disconnect(nodeId: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/disconnect",
                formParameters = Parameters.build {
                    append("nodeId", nodeId)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun open(
        nodeId: String,
        fundingSatoshis: Int,
        channelType: String?,
        pushMsat: Int?,
        fundingFeerateSatByte: Int?,
        announceChannel: Boolean?,
        openTimeoutSeconds: Int?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/open",
                formParameters = Parameters.build {
                    append("nodeId", nodeId)
                    append("fundingSatoshis", fundingSatoshis.toString())
                    channelType?.let { append("channelType", it) }
                    pushMsat?.let { append("pushMsat", it.toString()) }
                    fundingFeerateSatByte?.let { append("fundingFeerateSatByte", it.toString()) }
                    announceChannel?.let { append("announceChannel", it.toString()) }
                    openTimeoutSeconds?.let { append("openTimeoutSeconds", it.toString()) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun rbfopen(
        channelId: String,
        targetFeerateSatByte: Int,
        lockTime: Int?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/rbfopen",
                formParameters = Parameters.build {
                    append("channelId", channelId)
                    append("targetFeerateSatByte", targetFeerateSatByte.toString())
                    lockTime?.let { append("lockTime", it.toString()) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun cpfpbumpfees(outpoints: List<String>, targetFeerateSatByte: Int): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/cpfpbumpfees",
                formParameters = Parameters.build {
                    append("outpoints", outpoints.joinToString(","))
                    append("targetFeerateSatByte", targetFeerateSatByte.toString())
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun close(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
        scriptPubKey: String?,
        preferredFeerateSatByte: Int?,
        minFeerateSatByte: Int?,
        maxFeerateSatByte: Int?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/close",
                formParameters = Parameters.build {
                    append("channelId", channelId)
                    shortChannelId?.let { append("shortChannelId", it) }
                    channelIds?.let { append("channelIds", it.joinToString(",")) }
                    shortChannelIds?.let { append("shortChannelIds", it.joinToString(",")) }
                    scriptPubKey?.let { append("scriptPubKey", it) }
                    preferredFeerateSatByte?.let { append("preferredFeerateSatByte", it.toString()) }
                    minFeerateSatByte?.let { append("minFeerateSatByte", it.toString()) }
                    maxFeerateSatByte?.let { append("maxFeerateSatByte", it.toString()) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(response.bodyAsText())
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun forceclose(
        channelId: String,
        shortChannelId: String?,
        channelIds: List<String>?,
        shortChannelIds: List<String>?,
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/forceclose",
                formParameters = Parameters.build {
                    append("channelId", channelId)
                    shortChannelId?.let { append("shortChannelId", it) }
                    channelIds?.let { append("channelIds", it.joinToString(",")) }
                    shortChannelIds?.let { append("shortChannelIds", it.joinToString(",")) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun updaterelayfee(
        nodeId: String?,
        nodeIds: List<String>?,
        feeBaseMsat: Int,
        feeProportionalMillionths: Int
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/updaterelayfee",
                formParameters = Parameters.build {
                    nodeId?.let { append("nodeId", it) }
                    nodeIds?.let { append("nodeIds", it.joinToString(",")) }
                    append("feeBaseMsat", feeBaseMsat.toString())
                    append("feeProportionalMillionths", feeProportionalMillionths.toString())
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun peers(): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.post("$apiHost/peers")
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(response.bodyAsText())
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "unknown exception"))
        }
    }

    override suspend fun nodes(nodeIds: List<String>?): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/nodes",
                formParameters = Parameters.build {
                    nodeIds?.let { append("nodeIds", it.joinToString(",")) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun node(nodeId: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/node",
                formParameters = Parameters.build {
                    append("nodeId", nodeId)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun allchannels(): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.post("$apiHost/allchannels")
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun allupdates(nodeId: String?): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/allupdates",
                formParameters = Parameters.build {
                    nodeId?.let { append("nodeId", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun createinvoice(
        description: String?,
        descriptionHash: String?,
        amountMsat: Int?,
        expireIn: Int?,
        fallbackAddress: String?,
        paymentPreimage: String?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/createinvoice",
                formParameters = Parameters.build {
                    description?.let { append("description", it) }
                    descriptionHash?.let { append("descriptionHash", it) }
                    amountMsat?.let { append("amountMsat", it.toString()) }
                    expireIn?.let { append("expireIn", it.toString()) }
                    fallbackAddress?.let { append("fallbackAddress", it) }
                    paymentPreimage?.let { append("paymentPreimage", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun deleteinvoice(paymentHash: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/deleteinvoice",
                formParameters = Parameters.build {
                    append("paymentHash", paymentHash)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun parseinvoice(invoice: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/parseinvoice",
                formParameters = Parameters.build {
                    append("invoice", invoice)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(response.bodyAsText())
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun payinvoice(
        invoice: String,
        amountMsat: Int?,
        maxAttempts: Int?,
        maxFeeFlatSat: Int?,
        maxFeePct: Int?,
        externalId: String?,
        pathFindingExperimentName: String?,
        blocking: Boolean?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/payinvoice",
                formParameters = Parameters.build {
                    append("invoice", invoice)
                    amountMsat?.let { append("amountMsat", it.toString()) }
                    maxAttempts?.let { append("maxAttempts", it.toString()) }
                    maxFeeFlatSat?.let { append("maxFeeFlatSat", it.toString()) }
                    maxFeePct?.let { append("maxFeePct", it.toString()) }
                    externalId?.let { append("externalId", it) }
                    pathFindingExperimentName?.let { append("pathFindingExperimentName", it) }
                    blocking?.let { append("blocking", it.toString()) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(response.bodyAsText())
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun sendtonode(
        nodeId: String,
        amountMsat: Int,
        maxAttempts: Int?,
        maxFeeFlatSat: Int?,
        maxFeePct: Int?,
        externalId: String?,
        pathFindingExperimentName: String?,
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "${apiHost}/sendtonode",
                formParameters = Parameters.build {
                    append("nodeId", nodeId)
                    append("amountMsat", amountMsat.toString())
                    maxAttempts?.let { append("maxAttempts", it.toString()) }
                    maxFeeFlatSat?.let { append("maxFeeFlatSat", it.toString()) }
                    maxFeePct?.let { append("maxFeePct", it.toString()) }
                    externalId?.let { append("externalId", it) }
                    pathFindingExperimentName?.let { append("pathFindingExperimentName", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right(Json.decodeFromString(response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown error"))
        }
    }

    override suspend fun sendtoroute(
        invoice: String,
        nodeIds: List<String>?,
        shortChannelIds: List<String>?,
        amountMsat: Int,
        paymentHash: String,
        finalCltvExpiry: Int,
        maxFeeMsat: Int?,
        recipientAmountMsat: Int?,
        parentId: String?,
        externalId: String?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/sendtoroute",
                formParameters = Parameters.build {
                    append("invoice", invoice)
                    nodeIds?.let { append("nodeIds", it.joinToString(",")) }
                    shortChannelIds?.let { append("shortChannelIds", it.joinToString(",")) }
                    append("amountMsat", amountMsat.toString())
                    append("paymentHash", paymentHash)
                    append("finalCltvExpiry", finalCltvExpiry.toString())
                    maxFeeMsat?.let { append("maxFeeMsat", it.toString()) }
                    recipientAmountMsat?.let { append("recipientAmountMsat", it.toString()) }
                    parentId?.let { append("parentId", it) }
                    externalId?.let { append("externalId", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun getsentinfo(paymentHash: String, id: String?): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/getsentinfo",
                formParameters = Parameters.build {
                    append("paymentHash", paymentHash)
                    id?.let { append("id", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun getreceivedinfo(paymentHash: String?, invoice: String?): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/getreceivedinfo",
                formParameters = Parameters.build {
                    paymentHash?.let { append("paymentHash", it) }
                    invoice?.let { append("invoice", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun listreceivedpayments(from: Int?, to: Int?, count: Int?, skip: Int?): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/listreceivedpayments",
                formParameters = Parameters.build {
                    from?.let { append("from", it.toString()) }
                    to?.let { append("to", it.toString()) }
                    count?.let { append("count", it.toString()) }
                    skip?.let { append("skip", it.toString()) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun getinvoice(paymentHash: String): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/getinvoice",
                formParameters = Parameters.build {
                    append("paymentHash", paymentHash)
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun listinvoices(from: Int?, to: Int?, count: Int?, skip: Int?): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/listinvoices",
                formParameters = Parameters.build {
                    from?.let { append("from", it.toString()) }
                    to?.let { append("to", it.toString()) }
                    count?.let { append("count", it.toString()) }
                    skip?.let { append("skip", it.toString()) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun listpendinginvoices(from: Int?, to: Int?, count: Int?, skip: Int?): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/listpendinginvoices",
                formParameters = Parameters.build {
                    from?.let { append("from", it.toString()) }
                    to?.let { append("to", it.toString()) }
                    count?.let { append("count", it.toString()) }
                    skip?.let { append("skip", it.toString()) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun findroute(
        invoice: String,
        amountMsat: Int?,
        ignoreNodeIds: List<String>?,
        ignoreShortChannelIds: List<String>?,
        format: String?,
        maxFeeMsat: Int?,
        includeLocalChannelCost: Boolean?,
        pathFindingExperimentName: String?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/findroute",
                formParameters = Parameters.build {
                    append("invoice", invoice)
                    amountMsat?.let { append("amountMsat", it.toString()) }
                    ignoreNodeIds?.let { append("ignoreNodeIds", it.joinToString(",")) }
                    ignoreShortChannelIds?.let { append("ignoreShortChannelIds", it.joinToString(",")) }
                    format?.let { append("format", it) }
                    maxFeeMsat?.let { append("maxFeeMsat", it.toString()) }
                    includeLocalChannelCost?.let { append("includeLocalChannelCost", it.toString()) }
                    pathFindingExperimentName?.let { append("pathFindingExperimentName", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }

    override suspend fun findroutetonode(
        nodeId: String,
        amountMsat: Int,
        ignoreNodeIds: List<String>?,
        ignoreShortChannelIds: List<String>?,
        format: String?,
        maxFeeMsat: Int?,
        includeLocalChannelCost: Boolean?,
        pathFindingExperimentName: String?
    ): Either<ApiError, String> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$apiHost/findroutetonode",
                formParameters = Parameters.build {
                    append("nodeId", nodeId)
                    append("amountMsat", amountMsat.toString())
                    ignoreNodeIds?.let { append("ignoreNodeIds", it.joinToString(",")) }
                    ignoreShortChannelIds?.let { append("ignoreShortChannelIds", it.joinToString(",")) }
                    format?.let { append("format", it) }
                    maxFeeMsat?.let { append("maxFeeMsat", it.toString()) }
                    includeLocalChannelCost?.let { append("includeLocalChannelCost", it.toString()) }
                    pathFindingExperimentName?.let { append("pathFindingExperimentName", it) }
                }
            )
            when (response.status) {
                HttpStatusCode.OK -> Either.Right((response.bodyAsText()))
                else -> Either.Left(convertHttpError(response.status))
            }
        } catch (e: Throwable) {
            Either.Left(ApiError(0, e.message ?: "Unknown exception"))
        }
    }
}
