package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * LocalDatastore class for handling local data persistence
 */
@Serializable
class ParseLocalDatastore private constructor() {
    private val storage = ConcurrentHashMap<String, MutableMap<String, Any?>>()

    companion object {
        private var instance: ParseLocalDatastore? = null
        private const val PIN_PREFIX = "parsePin_"
        private const val DEFAULT_PIN = "_default"

        /**
         * Get the singleton instance
         */
        @JvmStatic
        fun getInstance(): ParseLocalDatastore {
            if (instance == null) {
                instance = ParseLocalDatastore()
            }
            return instance!!
        }

        /**
         * Pin an object to local datastore
         */
        @JvmStatic
        suspend fun pinObject(obj: ParseObject, name: String? = null) {
            val pinName = getPinName(name)
            val store = getInstance()
            
            val objects = store.storage.getOrPut(pinName) { mutableMapOf() }
            objects["${obj.className}_${obj.objectId}"] = obj.encode()
        }

        /**
         * Unpin an object from local datastore
         */
        @JvmStatic
        suspend fun unpinObject(obj: ParseObject, name: String? = null) {
            val pinName = getPinName(name)
            val store = getInstance()
            
            store.storage[pinName]?.remove("${obj.className}_${obj.objectId}")
        }

        /**
         * Pin multiple objects to local datastore
         */
        @JvmStatic
        suspend fun pinObjects(objects: List<ParseObject>, name: String? = null) {
            val pinName = getPinName(name)
            val store = getInstance()
            
            val pinned = store.storage.getOrPut(pinName) { mutableMapOf() }
            objects.forEach { obj ->
                pinned["${obj.className}_${obj.objectId}"] = obj.encode()
            }
        }

        /**
         * Unpin multiple objects from local datastore
         */
        @JvmStatic
        suspend fun unpinObjects(objects: List<ParseObject>, name: String? = null) {
            val pinName = getPinName(name)
            val store = getInstance()
            
            val pinned = store.storage[pinName]
            objects.forEach { obj ->
                pinned?.remove("${obj.className}_${obj.objectId}")
            }
        }

        /**
         * Get all objects for a pin name
         */
        @JvmStatic
        suspend fun getAllObjects(name: String? = null): List<ParseObject> {
            val pinName = getPinName(name)
            val store = getInstance()
            
            return store.storage[pinName]?.values?.mapNotNull { encoded ->
                @Suppress("UNCHECKED_CAST")
                val map = encoded as? Map<String, Any?> ?: return@mapNotNull null
                val className = map["className"] as? String ?: return@mapNotNull null
                ParseObject.create(className).apply {
                    map.forEach { (key, value) ->
                        when (key) {
                            "objectId" -> objectId = value as String
                            "createdAt" -> createdAt = Date(value as String)
                            "updatedAt" -> updatedAt = Date(value as String)
                            else -> this[key] = value
                        }
                    }
                }
            } ?: emptyList()
        }

        /**
         * Clear all objects for a pin name
         */
        @JvmStatic
        suspend fun clearAll(name: String? = null) {
            val pinName = getPinName(name)
            val store = getInstance()
            store.storage.remove(pinName)
        }

        /**
         * Clear all objects from local datastore
         */
        @JvmStatic
        suspend fun clearAllPins() {
            val store = getInstance()
            store.storage.clear()
        }

        private fun getPinName(name: String?): String {
            return PIN_PREFIX + (name ?: DEFAULT_PIN)
        }
    }
} 