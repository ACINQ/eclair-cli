import kotlinx.serialization.Serializable

@Serializable
data class EclairInfo(
    val version: String,
    val nodeId: String,
    val alias: String,
    val color: String,
    val features: Map<String, Map<String, String>>,
    val chainHash: String,
    val network: String,
    val blockHeight: Int,
    val publicAddresses: List<String>,
    val instanceId: String
)