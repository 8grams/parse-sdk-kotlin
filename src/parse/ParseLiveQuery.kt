package parse

import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * LiveQuery class for handling real-time queries
 */
class ParseLiveQuery<T : ParseObject>(
    private val query: ParseQuery<T>,
    private val webSocket: ParseWebSocket
) {
    private val subscriptionId = nextSubscriptionId.getAndIncrement()
    private val createHandlers = ConcurrentHashMap<String, (T) -> Unit>()
    private val updateHandlers = ConcurrentHashMap<String, (T) -> Unit>()
    private val deleteHandlers = ConcurrentHashMap<String, (T) -> Unit>()
    private val enterHandlers = ConcurrentHashMap<String, (T) -> Unit>()
    private val leaveHandlers = ConcurrentHashMap<String, (T) -> Unit>()
    private val errorHandlers = ConcurrentHashMap<String, (ParseException) -> Unit>()

    private var isSubscribed = false

    init {
        webSocket.onMessage("livequery_$subscriptionId") { json ->
            handleMessage(json)
        }
    }

    /**
     * Subscribe to the live query
     */
    suspend fun subscribe() {
        if (isSubscribed) return

        val subscribeMessage = buildJsonObject {
            put("op", "subscribe")
            put("requestId", subscriptionId)
            put("query", JsonObject(query.where))
            put("className", query.className)
        }

        webSocket.send(subscribeMessage)
        isSubscribed = true
    }

    /**
     * Unsubscribe from the live query
     */
    suspend fun unsubscribe() {
        if (!isSubscribed) return

        val unsubscribeMessage = buildJsonObject {
            put("op", "unsubscribe")
            put("requestId", subscriptionId)
        }

        webSocket.send(unsubscribeMessage)
        isSubscribed = false
        
        // Clean up handlers
        createHandlers.clear()
        updateHandlers.clear()
        deleteHandlers.clear()
        enterHandlers.clear()
        leaveHandlers.clear()
        errorHandlers.clear()
        
        webSocket.removeMessageHandler("livequery_$subscriptionId")
    }

    /**
     * Add a handler for object creation events
     */
    fun onCreate(id: String, handler: (T) -> Unit) {
        createHandlers[id] = handler
    }

    /**
     * Add a handler for object update events
     */
    fun onUpdate(id: String, handler: (T) -> Unit) {
        updateHandlers[id] = handler
    }

    /**
     * Add a handler for object deletion events
     */
    fun onDelete(id: String, handler: (T) -> Unit) {
        deleteHandlers[id] = handler
    }

    /**
     * Add a handler for objects entering the query
     */
    fun onEnter(id: String, handler: (T) -> Unit) {
        enterHandlers[id] = handler
    }

    /**
     * Add a handler for objects leaving the query
     */
    fun onLeave(id: String, handler: (T) -> Unit) {
        leaveHandlers[id] = handler
    }

    /**
     * Add a handler for error events
     */
    fun onError(id: String, handler: (ParseException) -> Unit) {
        errorHandlers[id] = handler
    }

    /**
     * Remove a handler by ID
     */
    fun removeHandler(id: String) {
        createHandlers.remove(id)
        updateHandlers.remove(id)
        deleteHandlers.remove(id)
        enterHandlers.remove(id)
        leaveHandlers.remove(id)
        errorHandlers.remove(id)
    }

    private fun handleMessage(json: JsonObject) {
        when (json["op"]?.jsonPrimitive?.content) {
            "create" -> handleCreate(json)
            "update" -> handleUpdate(json)
            "delete" -> handleDelete(json)
            "enter" -> handleEnter(json)
            "leave" -> handleLeave(json)
            "error" -> handleError(json)
        }
    }

    private fun handleCreate(json: JsonObject) {
        val object = parseObject(json["object"]?.jsonObject)
        createHandlers.values.forEach { handler -> handler(object) }
    }

    private fun handleUpdate(json: JsonObject) {
        val object = parseObject(json["object"]?.jsonObject)
        updateHandlers.values.forEach { handler -> handler(object) }
    }

    private fun handleDelete(json: JsonObject) {
        val object = parseObject(json["object"]?.jsonObject)
        deleteHandlers.values.forEach { handler -> handler(object) }
    }

    private fun handleEnter(json: JsonObject) {
        val object = parseObject(json["object"]?.jsonObject)
        enterHandlers.values.forEach { handler -> handler(object) }
    }

    private fun handleLeave(json: JsonObject) {
        val object = parseObject(json["object"]?.jsonObject)
        leaveHandlers.values.forEach { handler -> handler(object) }
    }

    private fun handleError(json: JsonObject) {
        val code = json["code"]?.jsonPrimitive?.int ?: ParseException.OTHER_CAUSE
        val error = json["error"]?.jsonPrimitive?.content ?: "Unknown error"
        val exception = ParseException(error, code)
        errorHandlers.values.forEach { handler -> handler(exception) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseObject(json: JsonObject?): T {
        if (json == null) {
            throw ParseException("Invalid object data", ParseException.INCORRECT_TYPE)
        }
        return query.parseObject(json.toMap()) as T
    }

    companion object {
        private val nextSubscriptionId = AtomicInteger(1)

        /**
         * Create a live query from a regular query
         */
        fun <T : ParseObject> fromQuery(query: ParseQuery<T>): ParseLiveQuery<T> {
            val serverInfo = ParseClient.getInstance().serverInfo
            val webSocket = ParseWebSocket.getInstance(
                serverInfo.serverURL,
                serverInfo.appId,
                ParseUser.getCurrentUser()?.sessionToken
            )
            return ParseLiveQuery(query, webSocket)
        }
    }
} 