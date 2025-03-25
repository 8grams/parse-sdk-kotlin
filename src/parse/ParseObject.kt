package parse

import parse.internal.*
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Base class for all Parse objects
 */
@Serializable
abstract class ParseObject(
    val className: String,
    var objectId: String? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null
) : Encodable {
    private val data: MutableMap<String, Any?> = mutableMapOf()
    private val operations: MutableMap<String, FieldOperation> = mutableMapOf()
    private val dirtyKeys: MutableSet<String> = mutableSetOf()

    /**
     * Get a value from the object
     */
    operator fun get(key: String): Any? = data[key]

    /**
     * Set a value in the object
     */
    operator fun set(key: String, value: Any?) {
        performOperation(key, SetOperation(value))
    }

    /**
     * Check if a key exists in the object
     */
    fun has(key: String): Boolean = data.containsKey(key)

    /**
     * Remove a key from the object
     */
    fun remove(key: String) {
        performOperation(key, DeleteOperation())
    }

    /**
     * Increment a number value
     */
    fun increment(key: String, amount: Number = 1) {
        performOperation(key, IncrementOperation(amount))
    }

    /**
     * Add elements to an array
     */
    fun add(key: String, values: List<Any?>) {
        performOperation(key, AddOperation(values))
    }

    /**
     * Add unique elements to an array
     */
    fun addUnique(key: String, values: List<Any?>) {
        performOperation(key, AddUniqueOperation(values))
    }

    /**
     * Remove elements from an array
     */
    fun removeAll(key: String, values: List<Any?>) {
        performOperation(key, RemoveOperation(values))
    }

    /**
     * Get all keys in the object
     */
    fun keys(): Set<String> = data.keys

    /**
     * Get all dirty keys (keys that have been modified)
     */
    fun dirtyKeys(): Set<String> = dirtyKeys.toSet()

    /**
     * Check if the object is dirty (has been modified)
     */
    fun isDirty(): Boolean = dirtyKeys.isNotEmpty()

    /**
     * Check if a specific key is dirty
     */
    fun isDirty(key: String): Boolean = dirtyKeys.contains(key)

    /**
     * Revert changes to all fields
     */
    fun revert() {
        operations.clear()
        dirtyKeys.clear()
    }

    /**
     * Revert changes to a specific field
     */
    fun revert(key: String) {
        operations.remove(key)
        dirtyKeys.remove(key)
    }

    /**
     * Save the object to Parse Server
     */
    suspend fun save(): ParseObject {
        val client = ParseClient.getInstance()
        val method = if (objectId == null) "POST" else "PUT"
        val endpoint = "classes/$className" + (objectId?.let { "/$it" } ?: "")

        val response = client.request(method, endpoint, this)
        
        objectId = response["objectId"] as String?
        createdAt = response["createdAt"]?.toString()?.let { Date(it) }
        updatedAt = response["updatedAt"]?.toString()?.let { Date(it) }

        // Apply operations and clear dirty state
        operations.forEach { (key, operation) ->
            data[key] = operation.apply(data[key])
        }
        operations.clear()
        dirtyKeys.clear()

        return this
    }

    /**
     * Delete the object from Parse Server
     */
    suspend fun delete() {
        if (objectId == null) {
            throw IllegalStateException("Cannot delete an object without an objectId")
        }

        val client = ParseClient.getInstance()
        client.request("DELETE", "classes/$className/$objectId")
    }

    /**
     * Fetch the latest data from Parse Server
     */
    suspend fun fetch(): ParseObject {
        if (objectId == null) {
            throw IllegalStateException("Cannot fetch an object without an objectId")
        }

        val client = ParseClient.getInstance()
        val response = client.request("GET", "classes/$className/$objectId")

        response.forEach { (key, value) ->
            when (key) {
                "objectId" -> objectId = value as String
                "createdAt" -> createdAt = Date(value as String)
                "updatedAt" -> updatedAt = Date(value as String)
                else -> data[key] = value
            }
        }

        return this
    }

    /**
     * Fetch this object only if it hasn't been fetched before
     */
    suspend fun fetchIfNeeded(): ParseObject {
        if (objectId == null || !isDirty()) {
            return fetch()
        }
        return this
    }

    /**
     * Pin this object to local datastore
     */
    suspend fun pin(name: String? = null) {
        ParseLocalDatastore.pinObject(this, name)
    }

    /**
     * Unpin this object from local datastore
     */
    suspend fun unpin(name: String? = null) {
        ParseLocalDatastore.unpinObject(this, name)
    }

    /**
     * Perform a field operation
     */
    private fun performOperation(key: String, operation: FieldOperation) {
        operations[key] = operation
        dirtyKeys.add(key)
    }

    override fun encode(): Map<String, Any?> {
        val encoded = mutableMapOf<String, Any?>()
        
        operations.forEach { (key, operation) ->
            encoded[key] = operation.encode()
        }

        return encoded
    }

    companion object {
        /**
         * Create a new Parse object
         */
        @JvmStatic
        fun create(className: String): ParseObject {
            return object : ParseObject(className) {}
        }

        /**
         * Fetch multiple objects at once
         */
        @JvmStatic
        suspend fun fetchAll(objects: List<ParseObject>): List<ParseObject> {
            if (objects.isEmpty()) return emptyList()
            
            val client = ParseClient.getInstance()
            val requests = objects.map { obj ->
                mapOf(
                    "method" to "GET",
                    "path" to "/1/classes/${obj.className}/${obj.objectId}"
                )
            }

            val response = client.request("POST", "batch", object : Encodable {
                override fun encode(): Map<String, Any?> = mapOf("requests" to requests)
            })

            @Suppress("UNCHECKED_CAST")
            val results = response["results"] as List<Map<String, Any?>>
            
            results.forEachIndexed { index, result ->
                val obj = objects[index]
                result.forEach { (key, value) ->
                    when (key) {
                        "objectId" -> obj.objectId = value as String
                        "createdAt" -> obj.createdAt = Date(value as String)
                        "updatedAt" -> obj.updatedAt = Date(value as String)
                        else -> obj.data[key] = value
                    }
                }
            }

            return objects
        }

        /**
         * Save multiple objects at once
         */
        @JvmStatic
        suspend fun saveAll(objects: List<ParseObject>): List<ParseObject> {
            if (objects.isEmpty()) return emptyList()
            
            val client = ParseClient.getInstance()
            val requests = objects.map { obj ->
                mapOf(
                    "method" to if (obj.objectId == null) "POST" else "PUT",
                    "path" to "/1/classes/${obj.className}${if (obj.objectId != null) "/${obj.objectId}" else ""}",
                    "body" to obj.encode()
                )
            }

            val response = client.request("POST", "batch", object : Encodable {
                override fun encode(): Map<String, Any?> = mapOf("requests" to requests)
            })

            @Suppress("UNCHECKED_CAST")
            val results = response["results"] as List<Map<String, Any?>>
            
            results.forEachIndexed { index, result ->
                val obj = objects[index]
                result.forEach { (key, value) ->
                    when (key) {
                        "objectId" -> obj.objectId = value as String
                        "createdAt" -> obj.createdAt = Date(value as String)
                        "updatedAt" -> obj.updatedAt = Date(value as String)
                    }
                }
                obj.dirtyKeys.clear()
            }

            return objects
        }

        /**
         * Delete multiple objects at once
         */
        @JvmStatic
        suspend fun deleteAll(objects: List<ParseObject>): List<ParseObject> {
            if (objects.isEmpty()) return emptyList()
            
            val client = ParseClient.getInstance()
            val requests = objects.map { obj ->
                mapOf(
                    "method" to "DELETE",
                    "path" to "/1/classes/${obj.className}/${obj.objectId}"
                )
            }

            client.request("POST", "batch", object : Encodable {
                override fun encode(): Map<String, Any?> = mapOf("requests" to requests)
            })

            return objects
        }

        /**
         * Pin multiple objects to local datastore
         */
        @JvmStatic
        suspend fun pinAll(name: String? = null, objects: List<ParseObject>) {
            ParseLocalDatastore.pinObjects(objects, name)
        }

        /**
         * Unpin multiple objects from local datastore
         */
        @JvmStatic
        suspend fun unpinAll(name: String? = null, objects: List<ParseObject>) {
            ParseLocalDatastore.unpinObjects(objects, name)
        }
    }
}