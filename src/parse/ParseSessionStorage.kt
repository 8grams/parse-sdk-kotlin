package parse

/**
 * Session storage class for managing session data
 */
class ParseSessionStorage(private val storage: ParseStorageInterface) {
    companion object {
        private const val SESSION_TOKEN_KEY = "Parse.Session.Token"
        private const val CURRENT_USER_KEY = "Parse.Current.User"
        private const val INSTALLATION_ID_KEY = "Parse.Installation.Id"
        private var instance: ParseSessionStorage? = null

        /**
         * Get the singleton instance
         */
        fun getInstance(): ParseSessionStorage {
            if (instance == null) {
                instance = ParseSessionStorage(ParseMemoryStorage.getInstance())
            }
            return instance!!
        }
    }

    /**
     * Get the current session token
     */
    suspend fun getSessionToken(): String? {
        return storage.get(SESSION_TOKEN_KEY)
    }

    /**
     * Set the current session token
     */
    suspend fun setSessionToken(token: String?) {
        if (token == null) {
            storage.remove(SESSION_TOKEN_KEY)
        } else {
            storage.set(SESSION_TOKEN_KEY, token)
        }
    }

    /**
     * Get the current user data
     */
    suspend fun getCurrentUser(): String? {
        return storage.get(CURRENT_USER_KEY)
    }

    /**
     * Set the current user data
     */
    suspend fun setCurrentUser(userData: String?) {
        if (userData == null) {
            storage.remove(CURRENT_USER_KEY)
        } else {
            storage.set(CURRENT_USER_KEY, userData)
        }
    }

    /**
     * Get the installation ID
     */
    suspend fun getInstallationId(): String? {
        return storage.get(INSTALLATION_ID_KEY)
    }

    /**
     * Set the installation ID
     */
    suspend fun setInstallationId(installationId: String?) {
        if (installationId == null) {
            storage.remove(INSTALLATION_ID_KEY)
        } else {
            storage.set(INSTALLATION_ID_KEY, installationId)
        }
    }

    /**
     * Clear all session data
     */
    suspend fun clear() {
        storage.clear()
    }
}