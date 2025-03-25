package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * User class for handling authentication and user data
 */
@Serializable
class ParseUser : ParseObject("_User") {
    var username: String? = null
        set(value) {
            field = value
            this["username"] = value
        }

    var email: String? = null
        set(value) {
            field = value
            this["email"] = value
        }

    var password: String? = null
        set(value) {
            field = value
            this["password"] = value
        }

    var emailVerified: Boolean = false
        set(value) {
            field = value
            this["emailVerified"] = value
        }

    var mobilePhoneNumber: String? = null
        set(value) {
            field = value
            this["mobilePhoneNumber"] = value
        }

    var mobilePhoneVerified: Boolean = false
        set(value) {
            field = value
            this["mobilePhoneVerified"] = value
        }

    var sessionToken: String? = null
        private set

    /**
     * Get roles for this user
     */
    suspend fun getRoles(): List<ParseRole> {
        val query = ParseQuery<ParseRole>("_Role")
        query.whereEqualTo("users", this)
        return query.find()
    }

    /**
     * Get ACL for this user
     */
    fun getACL(): ParseACL? {
        return get("ACL") as? ParseACL
    }

    /**
     * Set ACL for this user
     */
    fun setACL(acl: ParseACL) {
        put("ACL", acl)
    }

    /**
     * Validate session token
     */
    suspend fun validateSessionToken(): Boolean {
        if (sessionToken == null) return false
        return try {
            val client = ParseClient.getInstance()
            client.request(
                "GET",
                "validateSessionToken",
                headers = mapOf("X-Parse-Session-Token" to sessionToken!!)
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get linked services
     */
    fun getLinkedServices(): Set<String> {
        val authData = get("authData") as? Map<String, Any> ?: return emptySet()
        return authData.keys.toSet()
    }

    /**
     * Check if user is linked to a service
     */
    fun isLinked(service: String): Boolean {
        val authData = get("authData") as? Map<String, Any> ?: return false
        return authData.containsKey(service)
    }

    companion object {
        private var currentUser: ParseUser? = null
        private var enableUnsafeUser: Boolean = false
        private var enableRevocableSession: Boolean = false

        /**
         * Enable revocable sessions
         */
        @JvmStatic
        fun enableRevocableSession() {
            enableRevocableSession = true
        }

        /**
         * Enable unsafe current user access
         */
        @JvmStatic
        fun enableUnsafeCurrentUser() {
            enableUnsafeUser = true
        }

        /**
         * Get the current user
         */
        @JvmStatic
        fun getCurrentUser(): ParseUser? = currentUser

        /**
         * Sign up a new user
         */
        @JvmStatic
        suspend fun signUp(username: String, password: String, email: String? = null): ParseUser {
            val user = ParseUser().apply {
                this.username = username
                this.password = password
                this.email = email
            }

            val client = ParseClient.getInstance()
            val response = client.request("POST", "users", user)
            
            user.objectId = response["objectId"] as String
            user.createdAt = Date(response["createdAt"] as String)
            user.updatedAt = Date(response["updatedAt"] as String)
            user.sessionToken = response["sessionToken"] as String
            
            currentUser = user
            return user
        }

        /**
         * Log in a user
         */
        @JvmStatic
        suspend fun logIn(username: String, password: String): ParseUser {
            val client = ParseClient.getInstance()
            val response = client.request(
                "GET",
                "login",
                null,
                mapOf(
                    "username" to username,
                    "password" to password
                )
            )

            val user = ParseUser().apply {
                this.objectId = response["objectId"] as String
                this.username = response["username"] as String
                this.email = response["email"] as String?
                this.createdAt = Date(response["createdAt"] as String)
                this.updatedAt = Date(response["updatedAt"] as String)
                this.sessionToken = response["sessionToken"] as String
                this.emailVerified = response["emailVerified"] as? Boolean ?: false
                this.mobilePhoneNumber = response["mobilePhoneNumber"] as? String
                this.mobilePhoneVerified = response["mobilePhoneVerified"] as? Boolean ?: false
            }

            currentUser = user
            return user
        }

        /**
         * Log in with email
         */
        @JvmStatic
        suspend fun logInWithEmail(email: String, password: String): ParseUser {
            return logIn(email, password)
        }

        /**
         * Log in with mobile phone number
         */
        @JvmStatic
        suspend fun logInWithMobilePhoneNumber(phoneNumber: String, password: String): ParseUser {
            return logIn(phoneNumber, password)
        }

        /**
         * Request password reset email
         */
        @JvmStatic
        suspend fun requestPasswordReset(email: String) {
            val client = ParseClient.getInstance()
            client.request(
                "POST",
                "requestPasswordReset",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("email" to email)
                }
            )
        }

        /**
         * Request password reset SMS
         */
        @JvmStatic
        suspend fun requestPasswordResetByPhoneNumber(phoneNumber: String) {
            val client = ParseClient.getInstance()
            client.request("POST", "requestPasswordResetByPhoneNumber", object : Encodable {
                override fun encode(): Map<String, Any?> = mapOf("phoneNumber" to phoneNumber)
            })
        }

        /**
         * Verify mobile phone number
         */
        @JvmStatic
        suspend fun verifyMobilePhoneNumber(code: String): Boolean {
            val client = ParseClient.getInstance()
            val response = client.request("POST", "verifyMobilePhoneNumber", object : Encodable {
                override fun encode(): Map<String, Any?> = mapOf("code" to code)
            })
            return response["verified"] as Boolean
        }

        /**
         * Become a user with session token
         */
        @JvmStatic
        suspend fun become(sessionToken: String): ParseUser {
            val client = ParseClient.getInstance()
            val response = client.request(
                "GET",
                "users/me",
                headers = mapOf("X-Parse-Session-Token" to sessionToken)
            )

            val user = fromMap(response)
            currentUser = user
            return user
        }

        /**
         * Verify a user's email
         */
        @JvmStatic
        suspend fun verifyEmail(token: String) {
            val client = ParseClient.getInstance()
            client.request(
                "POST",
                "verificationEmailRequest",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("token" to token)
                }
            )
        }

        /**
         * Request a new verification email
         */
        @JvmStatic
        suspend fun requestEmailVerification(email: String) {
            val client = ParseClient.getInstance()
            client.request(
                "POST",
                "verificationEmailRequest",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("email" to email)
                }
            )
        }

        /**
         * Log in with third-party auth data
         */
        @JvmStatic
        suspend fun logInWithAuthData(
            authData: Map<String, Any?>,
            provider: String
        ): ParseUser {
            val client = ParseClient.getInstance()
            val response = client.request(
                "POST",
                "users",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf(
                        "authData" to mapOf(provider to authData)
                    )
                }
            )

            val user = fromMap(response)
            currentUser = user
            return user
        }

        /**
         * Link with third-party auth data
         */
        suspend fun linkWithAuthData(
            authData: Map<String, Any?>,
            provider: String
        ): ParseUser {
            val user = getCurrentUser() ?: throw IllegalStateException("Must be logged in to link with auth data")
            
            user["authData"] = mapOf(provider to authData)
            return user.save()
        }

        /**
         * Unlink from third-party auth data
         */
        suspend fun unlinkFromAuthData(provider: String): ParseUser {
            val user = getCurrentUser() ?: throw IllegalStateException("Must be logged in to unlink from auth data")
            
            user["authData"] = mapOf(provider to null)
            return user.save()
        }

        /**
         * Log in with username
         */
        @JvmStatic
        suspend fun logInWithUsername(username: String): ParseUser {
            val client = ParseClient.getInstance()
            val response = client.request(
                "GET",
                "login",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("username" to username)
                }
            )

            val user = fromMap(response)
            currentUser = user
            return user
        }

        /**
         * Log in with email
         */
        @JvmStatic
        suspend fun logInWithEmail(email: String): ParseUser {
            val client = ParseClient.getInstance()
            val response = client.request(
                "GET",
                "login",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("email" to email)
                }
            )

            val user = fromMap(response)
            currentUser = user
            return user
        }

        /**
         * Get user by session token
         */
        @JvmStatic
        suspend fun getUserBySessionToken(sessionToken: String): ParseUser? {
            return try {
                become(sessionToken)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Get user by email
         */
        @JvmStatic
        suspend fun getUserByEmail(email: String): ParseUser? {
            val query = ParseQuery<ParseUser>("_User")
            query.whereEqualTo("email", email)
            return query.first()
        }

        /**
         * Get user by username
         */
        @JvmStatic
        suspend fun getUserByUsername(username: String): ParseUser? {
            val query = ParseQuery<ParseUser>("_User")
            query.whereEqualTo("username", username)
            return query.first()
        }

        /**
         * Get user by phone number
         */
        @JvmStatic
        suspend fun getUserByPhoneNumber(phoneNumber: String): ParseUser? {
            val query = ParseQuery<ParseUser>("_User")
            query.whereEqualTo("mobilePhoneNumber", phoneNumber)
            return query.first()
        }
    }

    /**
     * Log out the current user
     */
    suspend fun logOut() {
        val client = ParseClient.getInstance()
        client.request("POST", "logout")
        currentUser = null
        sessionToken = null
    }

    /**
     * Request email verification
     */
    suspend fun requestEmailVerification() {
        val client = ParseClient.getInstance()
        client.request("POST", "verificationEmailRequest")
    }

    /**
     * Request mobile phone verification
     */
    suspend fun requestMobilePhoneVerification() {
        val client = ParseClient.getInstance()
        client.request("POST", "requestMobilePhoneVerification")
    }

    /**
     * Verify mobile phone number
     */
    suspend fun verifyMobilePhoneNumber(code: String): Boolean {
        val client = ParseClient.getInstance()
        val response = client.request("POST", "verifyMobilePhoneNumber", object : Encodable {
            override fun encode(): Map<String, Any?> = mapOf("code" to code)
        })
        return response["verified"] as Boolean
    }

    override suspend fun save(): ParseUser {
        val client = ParseClient.getInstance()
        val method = if (objectId == null) "POST" else "PUT"
        val endpoint = "users" + (objectId?.let { "/$it" } ?: "")
        val headers = sessionToken?.let { mapOf("X-Parse-Session-Token" to it) } ?: emptyMap()

        val response = client.request(method, endpoint, this, headers)
        
        objectId = response["objectId"] as String?
        createdAt = response["createdAt"]?.toString()?.let { Date(it) }
        updatedAt = response["updatedAt"]?.toString()?.let { Date(it) }
        sessionToken = response["sessionToken"] as String?

        // Apply operations and clear dirty state
        operations.forEach { (key, operation) ->
            data[key] = operation.apply(data[key])
        }
        operations.clear()
        dirtyKeys.clear()

        return this
    }

    override suspend fun delete() {
        if (objectId == null) {
            throw IllegalStateException("Cannot delete a user without an objectId")
        }

        val client = ParseClient.getInstance()
        client.request("DELETE", "users/$objectId", null, mapOf("X-Parse-Session-Token" to (sessionToken ?: "")))
        currentUser = null
        sessionToken = null
    }

    /**
     * Check if this user has a revocable session
     */
    fun hasRevocableSession(): Boolean {
        return sessionToken?.startsWith("r:") ?: false
    }

    /**
     * Convert to a revocable session
     */
    suspend fun convertToRevocableSession() {
        if (!hasRevocableSession()) {
            val client = ParseClient.getInstance()
            val response = client.request(
                "POST",
                "upgradeToRevocableSession",
                headers = mapOf("X-Parse-Session-Token" to (sessionToken ?: ""))
            )
            sessionToken = response["sessionToken"] as String
        }
    }
}