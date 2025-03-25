package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Audience class for handling push notification targeting
 */
@Serializable
class ParseAudience : ParseObject("_Audience") {
    var name: String = ""
        set(value) {
            field = value
            this["name"] = value
        }

    var query: ParseQuery<ParseInstallation>? = null
        set(value) {
            field = value
            this["query"] = value?.where
        }

    constructor(name: String) {
        this.name = name
    }

    /**
     * Set the query for this audience
     */
    fun setQuery(query: ParseQuery<ParseInstallation>) {
        this.query = query
    }

    /**
     * Get the query for this audience
     */
    fun getQuery(): ParseQuery<ParseInstallation>? = query

    /**
     * Check if this audience has a query
     */
    fun hasQuery(): Boolean = query != null

    override suspend fun save(): ParseAudience {
        if (name.isEmpty()) {
            throw IllegalStateException("Audience name cannot be empty")
        }
        return super.save() as ParseAudience
    }

    companion object {
        /**
         * Get a query for audiences
         */
        fun query(): ParseQuery<ParseAudience> {
            return ParseQuery("_Audience")
        }

        /**
         * Create a new audience
         */
        fun create(name: String): ParseAudience {
            return ParseAudience(name)
        }

        /**
         * Create an audience with a query
         */
        fun create(name: String, query: ParseQuery<ParseInstallation>): ParseAudience {
            return ParseAudience(name).apply {
                setQuery(query)
            }
        }

        /**
         * Create an audience from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseAudience {
            val name = map["name"] as? String ?: throw IllegalArgumentException("Name is required")
            val audience = ParseAudience(name)
            
            map.forEach { (key, value) ->
                when (key) {
                    "objectId" -> audience.objectId = value as String
                    "createdAt" -> audience.createdAt = Date(value as String)
                    "updatedAt" -> audience.updatedAt = Date(value as String)
                    "ACL" -> audience.acl = ParseACL.fromMap(value as Map<String, Any?>)
                    "query" -> {
                        @Suppress("UNCHECKED_CAST")
                        val queryMap = value as? Map<String, Any?>
                        if (queryMap != null) {
                            val query = ParseQuery<ParseInstallation>("_Installation")
                            query.where.putAll(queryMap)
                            audience.query = query
                        }
                    }
                    else -> audience[key] = value
                }
            }

            return audience
        }
    }
}