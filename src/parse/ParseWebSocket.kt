package parse

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket class for handling real-time connections
 */
class ParseWebSocket(
    private val serverURL: String,
    private val applicationId: String,
    private val sessionToken: String? = null
) {
    private var webSocketSession: DefaultWebSocketSession? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val messageHandlers = ConcurrentHashMap<String, (JsonObject) -> Unit>()
    private val errorHandlers = ConcurrentHashMap<String, (Throwable) -> Unit>()
    private val client = HttpClient {
        install(WebSockets)
    }

    private val isConnected: Boolean
        get() = webSocketSession?.isActive == true

    /**
     * Connect to the WebSocket server
     */
    suspend fun connect() {
        if (isConnected) return

        try {
            val wsURL = serverURL.replace("http", "ws") + "/ws"
            webSocketSession = client.webSocketSession {
                url(wsURL)
                header("X-Parse-Application-Id", applicationId)
                if (sessionToken != null) {
                    header("X-Parse-Session-Token", sessionToken)
                }
            }

            // Start listening for messages
            scope.launch {
                try {
                    webSocketSession?.incoming?.consumeEach { frame ->
                        when (frame) {
                            is Frame.Text -> handleMessage(frame.readText())
                            is Frame.Close -> disconnect()
                            else -> { /* Ignore other frame types */ }
                        }
                    }
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        } catch (e: Exception) {
            handleError(e)
            throw e
        }
    }

    /**
     * Disconnect from the WebSocket server
     */
    suspend fun disconnect() {
        webSocketSession?.close()
        webSocketSession = null
        messageHandlers.clear()
        errorHandlers.clear()
    }

    /**
     * Send a message to the server
     */
    suspend fun send(message: String) {
        if (!isConnected) {
            throw ParseException("WebSocket is not connected", ParseException.CONNECTION_FAILED)
        }
        webSocketSession?.send(Frame.Text(message))
    }

    /**
     * Send a JSON message to the server
     */
    suspend fun send(message: JsonObject) {
        send(message.toString())
    }

    /**
     * Add a message handler
     */
    fun onMessage(id: String, handler: (JsonObject) -> Unit) {
        messageHandlers[id] = handler
    }

    /**
     * Remove a message handler
     */
    fun removeMessageHandler(id: String) {
        messageHandlers.remove(id)
    }

    /**
     * Add an error handler
     */
    fun onError(id: String, handler: (Throwable) -> Unit) {
        errorHandlers[id] = handler
    }

    /**
     * Remove an error handler
     */
    fun removeErrorHandler(id: String) {
        errorHandlers.remove(id)
    }

    private fun handleMessage(text: String) {
        try {
            val json = Json.parseToJsonElement(text).jsonObject
            messageHandlers.values.forEach { handler ->
                handler(json)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun handleError(error: Throwable) {
        errorHandlers.values.forEach { handler ->
            handler(error)
        }
    }

    companion object {
        private var instance: ParseWebSocket? = null

        /**
         * Get the singleton instance
         */
        fun getInstance(
            serverURL: String,
            applicationId: String,
            sessionToken: String? = null
        ): ParseWebSocket {
            if (instance == null) {
                instance = ParseWebSocket(serverURL, applicationId, sessionToken)
            }
            return instance!!
        }
    }
} 