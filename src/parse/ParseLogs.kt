package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Logs class for handling server logs
 */
@Serializable
class ParseLogs : Encodable {
    companion object {
        /**
         * Get logs
         */
        @JvmStatic
        suspend fun getLogs(
            level: String? = null,
            from: Date? = null,
            until: Date? = null,
            order: String? = null,
            limit: Int? = null
        ): List<Map<String, Any?>> {
            val client = ParseClient.getInstance()
            val params = mutableMapOf<String, Any?>()
            
            level?.let { params["level"] = it }
            from?.let { params["from"] = it.toISOString() }
            until?.let { params["until"] = it.toISOString() }
            order?.let { params["order"] = it }
            limit?.let { params["limit"] = it }

            val response = client.request(
                "GET",
                "logs",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = params
                }
            )

            @Suppress("UNCHECKED_CAST")
            return response["results"] as List<Map<String, Any?>>
        }

        /**
         * Get error logs
         */
        @JvmStatic
        suspend fun getErrorLogs(
            from: Date? = null,
            until: Date? = null,
            order: String? = null,
            limit: Int? = null
        ): List<Map<String, Any?>> {
            return getLogs("error", from, until, order, limit)
        }

        /**
         * Get warning logs
         */
        @JvmStatic
        suspend fun getWarningLogs(
            from: Date? = null,
            until: Date? = null,
            order: String? = null,
            limit: Int? = null
        ): List<Map<String, Any?>> {
            return getLogs("warn", from, until, order, limit)
        }

        /**
         * Get info logs
         */
        @JvmStatic
        suspend fun getInfoLogs(
            from: Date? = null,
            until: Date? = null,
            order: String? = null,
            limit: Int? = null
        ): List<Map<String, Any?>> {
            return getLogs("info", from, until, order, limit)
        }

        /**
         * Get debug logs
         */
        @JvmStatic
        suspend fun getDebugLogs(
            from: Date? = null,
            until: Date? = null,
            order: String? = null,
            limit: Int? = null
        ): List<Map<String, Any?>> {
            return getLogs("debug", from, until, order, limit)
        }

        /**
         * Get verbose logs
         */
        @JvmStatic
        suspend fun getVerboseLogs(
            from: Date? = null,
            until: Date? = null,
            order: String? = null,
            limit: Int? = null
        ): List<Map<String, Any?>> {
            return getLogs("verbose", from, until, order, limit)
        }
    }
}