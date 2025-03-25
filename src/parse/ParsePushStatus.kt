package parse

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Push status class for tracking push notification delivery
 */
@Serializable
class ParsePushStatus : ParseObject("_PushStatus") {
    var pushTime: Date? = null
        private set
        get() {
            return get("pushTime") as? Date
        }

    var source: String? = null
        private set
        get() {
            return get("source") as? String
        }

    var status: String? = null
        private set
        get() {
            return get("status") as? String
        }

    var numSent: Int = 0
        private set
        get() {
            return get("numSent") as? Int ?: 0
        }

    var numFailed: Int = 0
        private set
        get() {
            return get("numFailed") as? Int ?: 0
        }

    var payload: Map<String, Any?>? = null
        private set
        get() {
            @Suppress("UNCHECKED_CAST")
            return get("payload") as? Map<String, Any?>
        }

    var query: Map<String, Any?>? = null
        private set
        get() {
            @Suppress("UNCHECKED_CAST")
            return get("query") as? Map<String, Any?>
        }

    /**
     * Check if the push was successful
     */
    fun isSuccess(): Boolean {
        return status == "succeeded"
    }

    /**
     * Check if the push failed
     */
    fun isFailed(): Boolean {
        return status == "failed"
    }

    /**
     * Check if the push is pending
     */
    fun isPending(): Boolean {
        return status == "pending"
    }

    /**
     * Check if the push is running
     */
    fun isRunning(): Boolean {
        return status == "running"
    }

    override suspend fun save(): ParsePushStatus {
        throw UnsupportedOperationException("Push status cannot be directly saved")
    }

    override suspend fun delete() {
        throw UnsupportedOperationException("Push status cannot be directly deleted")
    }

    companion object {
        /**
         * Get a query for push status
         */
        fun query(): ParseQuery<ParsePushStatus> {
            return ParseQuery("_PushStatus")
        }

        /**
         * Get push status by ID
         */
        suspend fun getPushStatus(pushId: String): ParsePushStatus? {
            val query = query()
            query.where["objectId"] = pushId
            val results = query.find()
            return results.firstOrNull()
        }

        /**
         * Create a push status from a map
         */
        fun fromMap(map: Map<String, Any?>): ParsePushStatus {
            val pushStatus = ParsePushStatus()
            
            map.forEach { (key, value) ->
                when (key) {
                    "objectId" -> pushStatus.objectId = value as String
                    "createdAt" -> pushStatus.createdAt = Date(value as String)
                    "updatedAt" -> pushStatus.updatedAt = Date(value as String)
                    "ACL" -> pushStatus.acl = ParseACL.fromMap(value as Map<String, Any?>)
                    "pushTime" -> pushStatus["pushTime"] = Date(value as String)
                    "source" -> pushStatus["source"] = value
                    "status" -> pushStatus["status"] = value
                    "numSent" -> pushStatus["numSent"] = value
                    "numFailed" -> pushStatus["numFailed"] = value
                    "payload" -> pushStatus["payload"] = value
                    "query" -> pushStatus["query"] = value
                    else -> pushStatus[key] = value
                }
            }

            return pushStatus
        }
    }
}