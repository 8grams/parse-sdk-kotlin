package parse

import kotlinx.serialization.Serializable

/**
 * Server information class for Parse Server
 */
@Serializable
data class ParseServerInfo(
    val appId: String,
    val appName: String,
    val masterKey: String? = null,
    val serverURL: String,
    val serverVersion: String,
    val features: Map<String, Boolean> = emptyMap()
) {
    companion object {
        /**
         * Create server info from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseServerInfo {
            return ParseServerInfo(
                appId = map["appId"] as String,
                appName = map["appName"] as String,
                masterKey = map["masterKey"] as? String,
                serverURL = map["serverURL"] as String,
                serverVersion = map["serverVersion"] as String,
                features = (map["features"] as? Map<String, Boolean>) ?: emptyMap()
            )
        }
    }

    /**
     * Check if a feature is enabled
     */
    fun hasFeature(feature: String): Boolean {
        return features[feature] ?: false
    }

    /**
     * Convert to a map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "appId" to appId,
            "appName" to appName,
            "masterKey" to masterKey,
            "serverURL" to serverURL,
            "serverVersion" to serverVersion,
            "features" to features
        )
    }
}