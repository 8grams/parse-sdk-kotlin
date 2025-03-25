package parse

import parse.httpclients.ParseHttpable
import parse.httpclients.ParseStreamHttpClient
import parse.internal.Encodable
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Main Parse Client class that handles initialization and configuration.
 */
class ParseClient private constructor(
    private val applicationId: String,
    private val clientKey: String?,
    private val masterKey: String?,
    private var serverUrl: String,
    private val httpClient: ParseHttpable
) {
    private var serverInfo: ParseServerInfo? = null
    private var storage: ParseStorageInterface? = null
    private var timeout: Int = 30000 // 30 seconds default timeout
    private var state: Map<String, Any> = mutableMapOf()
    private var installationId: String? = null
    private var localDatastore: ParseLocalDatastore? = null

    /**
     * Set server URL
     */
    fun setServerURL(url: String) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalArgumentException("Server URL must start with http:// or https://")
        }
        serverUrl = url.removeSuffix("/")
    }

    /**
     * Set request timeout
     */
    fun setTimeout(milliseconds: Int) {
        if (milliseconds <= 0) {
            throw IllegalArgumentException("Timeout must be greater than 0")
        }
        timeout = milliseconds
        httpClient.setTimeout(milliseconds)
    }

    /**
     * Set storage interface
     */
    fun setStorage(storage: ParseStorageInterface) {
        this.storage = storage
    }

    /**
     * Get storage interface
     */
    fun getStorage(): ParseStorageInterface {
        return storage ?: throw IllegalStateException("Storage interface has not been set")
    }

    /**
     * Get server info
     */
    suspend fun getServerInfo(): ParseServerInfo {
        if (serverInfo == null) {
            val response = request("GET", "serverInfo")
            serverInfo = ParseServerInfo.fromMap(response)
        }
        return serverInfo!!
    }

    /**
     * Check server health
     */
    suspend fun serverHealthCheck(): Boolean {
        return try {
            request("GET", "health")
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get installation ID
     */
    fun getInstallationId(): String {
        if (installationId == null) {
            installationId = storage?.getString("installationId") ?: UUID.randomUUID().toString()
            storage?.putString("installationId", installationId!!)
        }
        return installationId!!
    }

    /**
     * Get local datastore
     */
    fun getLocalDatastore(): ParseLocalDatastore {
        return localDatastore ?: throw IllegalStateException("Local datastore has not been initialized")
    }

    /**
     * Initialize local datastore
     */
    fun initializeLocalDatastore(datastore: ParseLocalDatastore) {
        localDatastore = datastore
    }

    /**
     * Get state value
     */
    fun getState(key: String): Any? = state[key]

    /**
     * Set state value
     */
    fun setState(key: String, value: Any?) {
        if (value == null) {
            (state as MutableMap).remove(key)
        } else {
            (state as MutableMap)[key] = value
        }
    }

    /**
     * Clear all state
     */
    fun clearState() {
        state = mutableMapOf()
    }

    companion object {
        private var instance: ParseClient? = null
        private var json: Json? = null
        private const val DEFAULT_API_VERSION = "1"
        private var apiVersion: String = DEFAULT_API_VERSION

        /**
         * Set API version
         */
        @JvmStatic
        fun setApiVersion(version: String) {
            apiVersion = version
        }

        /**
         * Get API version
         */
        @JvmStatic
        fun getApiVersion(): String = apiVersion

        /**
         * Initialize the Parse Client with the required configuration.
         * @param applicationId Your Parse Application ID
         * @param clientKey Your Parse Client Key (optional)
         * @param masterKey Your Parse Master Key (optional)
         * @param serverUrl Your Parse Server URL (defaults to https://api.parse.com/1)
         * @param httpClient Custom HTTP client implementation (optional)
         * @param storage Storage interface (optional)
         */
        @JvmStatic
        fun initialize(
            applicationId: String,
            clientKey: String? = null,
            masterKey: String? = null,
            serverUrl: String = "https://api.parse.com/1",
            httpClient: ParseHttpable? = null,
            storage: ParseStorageInterface? = null
        ) {
            if (instance != null) {
                throw IllegalStateException("Parse Client has already been initialized")
            }

            val client = httpClient ?: ParseStreamHttpClient()
            instance = ParseClient(applicationId, clientKey, masterKey, serverUrl, client).apply {
                storage?.let { setStorage(it) }
            }

            json = Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }

        /**
         * Get the current Parse Client instance.
         * @throws IllegalStateException if Parse Client hasn't been initialized
         */
        @JvmStatic
        fun getInstance(): ParseClient {
            return instance ?: throw IllegalStateException("Parse Client has not been initialized. Call ParseClient.initialize() first.")
        }

        /**
         * Get the JSON serializer instance
         */
        @JvmStatic
        fun getJson(): Json {
            return json ?: throw IllegalStateException("Parse Client has not been initialized. Call ParseClient.initialize() first.")
        }
    }

    /**
     * Get the current application ID
     */
    fun getApplicationId(): String = applicationId

    /**
     * Get the current client key
     */
    fun getClientKey(): String? = clientKey

    /**
     * Get the current master key
     */
    fun getMasterKey(): String? = masterKey

    /**
     * Get the current server URL
     */
    fun getServerUrl(): String = serverUrl

    /**
     * Get the HTTP client instance
     */
    fun getHttpClient(): ParseHttpable = httpClient

    /**
     * Send a request to Parse Server
     * @param method The HTTP method
     * @param endpoint The API endpoint
     * @param data The request data
     * @param headers Additional headers
     * @param onProgress Progress callback
     * @return The response data
     */
    suspend fun request(
        method: String,
        endpoint: String,
        data: Encodable? = null,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Long, Long) -> Unit)? = null
    ): Map<String, Any> {
        val url = "$serverUrl/$endpoint"
        val requestHeaders = mutableMapOf<String, String>()
        
        requestHeaders["X-Parse-Application-Id"] = applicationId
        clientKey?.let { requestHeaders["X-Parse-Client-Key"] = it }
        masterKey?.let { requestHeaders["X-Parse-Master-Key"] = it }
        requestHeaders.putAll(headers)

        return httpClient.request(method, url, data, requestHeaders, onProgress)
    }
}