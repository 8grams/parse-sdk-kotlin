package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Session class for handling user sessions
 */
@Serializable
class ParseSession : ParseObject("_Session") {
    var sessionToken: String? = null
        private set
        get() {
            return get("sessionToken") as? String
        }

    var user: ParseUser? = null
        private set
        get() {
            return get("user") as? ParseUser
        }

    var installationId: String? = null
        private set
        get() {
            return get("installationId") as? String
        }

    var expiresAt: Date? = null
        private set
        get() {
            return get("expiresAt") as? Date
        }

    var createdWith: Map<String, Any?>? = null
        private set
        get() {
            @Suppress("UNCHECKED_CAST")
            return get("createdWith") as? Map<String, Any?>
        }

    var restricted: Boolean = false
        private set
        get() {
            return get("restricted") as? Boolean ?: false
        }

    /**
     * Get the current session
     */
    suspend fun getCurrentSession(): ParseSession? {
        val client = ParseClient.getInstance()
        val response = client.request("GET", "sessions/me")
        return fromMap(response)
    }

    /**
     * Upgrade to a revocable session
     */
    suspend fun upgradeToRevocable(): ParseSession {
        val client = ParseClient.getInstance()
        val response = client.request("POST", "upgradeToRevocableSession")
        return fromMap(response)
    }

    /**
     * Check if this session is valid
     */
    fun isValid(): Boolean {
        val expiry = expiresAt ?: return false
        return expiry.after(Date())
    }

    /**
     * Check if this session is revocable
     */
    fun isRevocable(): Boolean {
        return sessionToken?.startsWith("r:") ?: false
    }

    override suspend fun save(): ParseSession {
        throw UnsupportedOperationException("Sessions cannot be directly saved")
    }

    override suspend fun delete() {
        if (objectId == null) {
            throw IllegalStateException("Cannot delete a session without an objectId")
        }
        val client = ParseClient.getInstance()
        client.request("DELETE", "sessions/$objectId")
    }

    companion object {
        private var currentSession: ParseSession? = null

        /**
         * Get a query for sessions
         */
        fun query(): ParseQuery<ParseSession> {
            return ParseQuery("_Session")
        }

        /**
         * Get the current session
         */
        fun getCurrentSession(): ParseSession? = currentSession

        /**
         * Set the current session
         */
        internal fun setCurrentSession(session: ParseSession?) {
            currentSession = session
        }

        /**
         * Create a session from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseSession {
            val session = ParseSession()
            
            map.forEach { (key, value) ->
                when (key) {
                    "objectId" -> session.objectId = value as String
                    "createdAt" -> session.createdAt = Date(value as String)
                    "updatedAt" -> session.updatedAt = Date(value as String)
                    "ACL" -> session.acl = ParseACL.fromMap(value as Map<String, Any?>)
                    "sessionToken" -> session["sessionToken"] = value
                    "user" -> {
                        @Suppress("UNCHECKED_CAST")
                        val userMap = value as? Map<String, Any?>
                        if (userMap != null) {
                            session["user"] = ParseUser.fromMap(userMap)
                        }
                    }
                    "installationId" -> session["installationId"] = value
                    "expiresAt" -> session["expiresAt"] = Date(value as String)
                    "createdWith" -> session["createdWith"] = value
                    "restricted" -> session["restricted"] = value
                    else -> session[key] = value
                }
            }

            return session
        }
    }
}