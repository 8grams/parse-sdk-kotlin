package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Config class for handling app configuration
 */
@Serializable
class ParseConfig : Encodable {
    private val params: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Get a parameter value
     */
    operator fun get(key: String): Any? = params[key]

    /**
     * Check if a parameter exists
     */
    fun has(key: String): Boolean = params.containsKey(key)

    /**
     * Get all parameter keys
     */
    fun keys(): Set<String> = params.keys

    override fun encode(): Map<String, Any?> = params.toMap()

    /**
     * Get a boolean value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return get(key) as? Boolean ?: defaultValue
    }

    /**
     * Get an integer value
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return (get(key) as? Number)?.toInt() ?: defaultValue
    }

    /**
     * Get a long value
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return (get(key) as? Number)?.toLong() ?: defaultValue
    }

    /**
     * Get a double value
     */
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return (get(key) as? Number)?.toDouble() ?: defaultValue
    }

    /**
     * Get a string value
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return get(key) as? String ?: defaultValue
    }

    /**
     * Get a list value
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getList(key: String, defaultValue: List<T> = emptyList()): List<T> {
        return get(key) as? List<T> ?: defaultValue
    }

    /**
     * Get a map value
     */
    @Suppress("UNCHECKED_CAST")
    fun <K, V> getMap(key: String, defaultValue: Map<K, V> = emptyMap()): Map<K, V> {
        return get(key) as? Map<K, V> ?: defaultValue
    }

    /**
     * Get a date value
     */
    fun getDate(key: String, defaultValue: Date? = null): Date? {
        val value = get(key)
        return when (value) {
            is Date -> value
            is String -> try { Date(value) } catch (e: Exception) { defaultValue }
            else -> defaultValue
        }
    }

    /**
     * Get a file value
     */
    fun getFile(key: String, defaultValue: ParseFile? = null): ParseFile? {
        val value = get(key)
        return when (value) {
            is ParseFile -> value
            is Map<*, *> -> try {
                @Suppress("UNCHECKED_CAST")
                ParseFile.fromMap(value as Map<String, Any?>)
            } catch (e: Exception) {
                defaultValue
            }
            else -> defaultValue
        }
    }

    /**
     * Get a geo point value
     */
    fun getGeoPoint(key: String, defaultValue: ParseGeoPoint? = null): ParseGeoPoint? {
        val value = get(key)
        return when (value) {
            is ParseGeoPoint -> value
            is Map<*, *> -> try {
                @Suppress("UNCHECKED_CAST")
                ParseGeoPoint.fromMap(value as Map<String, Any?>)
            } catch (e: Exception) {
                defaultValue
            }
            else -> defaultValue
        }
    }

    companion object {
        private var currentConfig: ParseConfig? = null

        /**
         * Get the current config
         */
        @JvmStatic
        fun getCurrentConfig(): ParseConfig? = currentConfig

        /**
         * Get the current config
         */
        @JvmStatic
        suspend fun get(): ParseConfig {
            val client = ParseClient.getInstance()
            val response = client.request("GET", "config")

            val config = ParseConfig()
            response.forEach { (key, value) ->
                config.params[key] = value
            }

            currentConfig = config
            return config
        }

        /**
         * Save the current config
         */
        @JvmStatic
        suspend fun save(config: ParseConfig) {
            val client = ParseClient.getInstance()
            client.request("PUT", "config", config)
            currentConfig = config
        }
    }
}