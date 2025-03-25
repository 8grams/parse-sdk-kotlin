package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable

/**
 * Query class for finding Parse objects
 */
@Serializable
class ParseQuery<T : ParseObject>(
    private val className: String
) : Encodable {
    private val where: MutableMap<String, Any> = mutableMapOf()
    private val order: MutableList<String> = mutableListOf()
    private var limit: Int? = null
    private var skip: Int? = null
    private val include: MutableList<String> = mutableListOf()
    private val select: MutableList<String> = mutableListOf()

    /**
     * Add an equal to condition
     */
    fun equalTo(key: String, value: Any): ParseQuery<T> {
        where[key] = value
        return this
    }

    /**
     * Add a not equal to condition
     */
    fun notEqualTo(key: String, value: Any): ParseQuery<T> {
        where[key] = mapOf("\$ne" to value)
        return this
    }

    /**
     * Add a less than condition
     */
    fun lessThan(key: String, value: Any): ParseQuery<T> {
        where[key] = mapOf("\$lt" to value)
        return this
    }

    /**
     * Add a less than or equal to condition
     */
    fun lessThanOrEqualTo(key: String, value: Any): ParseQuery<T> {
        where[key] = mapOf("\$lte" to value)
        return this
    }

    /**
     * Add a greater than condition
     */
    fun greaterThan(key: String, value: Any): ParseQuery<T> {
        where[key] = mapOf("\$gt" to value)
        return this
    }

    /**
     * Add a greater than or equal to condition
     */
    fun greaterThanOrEqualTo(key: String, value: Any): ParseQuery<T> {
        where[key] = mapOf("\$gte" to value)
        return this
    }

    /**
     * Add an in condition
     */
    fun `in`(key: String, values: List<Any>): ParseQuery<T> {
        where[key] = mapOf("\$in" to values)
        return this
    }

    /**
     * Add a not in condition
     */
    fun notIn(key: String, values: List<Any>): ParseQuery<T> {
        where[key] = mapOf("\$nin" to values)
        return this
    }

    /**
     * Add a contained in condition
     */
    fun containedIn(key: String, values: List<Any>): ParseQuery<T> {
        where[key] = mapOf("\$in" to values)
        return this
    }

    /**
     * Add a not contained in condition
     */
    fun notContainedIn(key: String, values: List<Any>): ParseQuery<T> {
        where[key] = mapOf("\$nin" to values)
        return this
    }

    /**
     * Add an exists condition
     */
    fun exists(key: String): ParseQuery<T> {
        where[key] = mapOf("\$exists" to true)
        return this
    }

    /**
     * Add a does not exist condition
     */
    fun doesNotExist(key: String): ParseQuery<T> {
        where[key] = mapOf("\$exists" to false)
        return this
    }

    /**
     * Add a contains condition
     */
    fun contains(key: String, value: String): ParseQuery<T> {
        where[key] = mapOf("\$regex" to value)
        return this
    }

    /**
     * Add a starts with condition
     */
    fun startsWith(key: String, value: String): ParseQuery<T> {
        where[key] = mapOf("\$regex" to "^$value")
        return this
    }

    /**
     * Add an ends with condition
     */
    fun endsWith(key: String, value: String): ParseQuery<T> {
        where[key] = mapOf("\$regex" to "$value\$")
        return this
    }

    /**
     * Add a matches condition with regex
     */
    fun matches(key: String, regex: String, modifiers: String? = null): ParseQuery<T> {
        val regexMap = mutableMapOf<String, Any>(
            "\$regex" to regex
        )
        if (modifiers != null) {
            regexMap["\$options"] = modifiers
        }
        where[key] = regexMap
        return this
    }

    /**
     * Add a matches query condition
     */
    fun matchesQuery(key: String, query: ParseQuery<*>): ParseQuery<T> {
        where[key] = mapOf("\$inQuery" to query.encode())
        return this
    }

    /**
     * Add a does not match query condition
     */
    fun doesNotMatchQuery(key: String, query: ParseQuery<*>): ParseQuery<T> {
        where[key] = mapOf("\$notInQuery" to query.encode())
        return this
    }

    /**
     * Add a matches key in query condition
     */
    fun matchesKeyInQuery(key: String, queryKey: String, query: ParseQuery<*>): ParseQuery<T> {
        where[key] = mapOf(
            "\$select" to mapOf(
                "key" to queryKey,
                "query" to query.encode()
            )
        )
        return this
    }

    /**
     * Add a does not match key in query condition
     */
    fun doesNotMatchKeyInQuery(key: String, queryKey: String, query: ParseQuery<*>): ParseQuery<T> {
        where[key] = mapOf(
            "\$dontSelect" to mapOf(
                "key" to queryKey,
                "query" to query.encode()
            )
        )
        return this
    }

    /**
     * Add a contains all condition
     */
    fun containsAll(key: String, values: List<Any>): ParseQuery<T> {
        where[key] = mapOf("\$all" to values)
        return this
    }

    /**
     * Add a within geo box condition
     */
    fun withinGeoBox(key: String, southwest: ParseGeoPoint, northeast: ParseGeoPoint): ParseQuery<T> {
        where[key] = mapOf(
            "\$within" to mapOf(
                "\$box" to listOf(
                    southwest.encode(),
                    northeast.encode()
                )
            )
        )
        return this
    }

    /**
     * Add a within polygon condition
     */
    fun withinPolygon(key: String, points: List<ParseGeoPoint>): ParseQuery<T> {
        where[key] = mapOf(
            "\$geoWithin" to mapOf(
                "\$polygon" to points.map { it.encode() }
            )
        )
        return this
    }

    /**
     * Add an ascending sort
     */
    fun orderByAscending(key: String): ParseQuery<T> {
        order.add(key)
        return this
    }

    /**
     * Add a descending sort
     */
    fun orderByDescending(key: String): ParseQuery<T> {
        order.add("-$key")
        return this
    }

    /**
     * Set the number of results to skip
     */
    fun skip(count: Int): ParseQuery<T> {
        skip = count
        return this
    }

    /**
     * Set the maximum number of results to return
     */
    fun limit(count: Int): ParseQuery<T> {
        limit = count
        return this
    }

    /**
     * Include nested Parse objects
     */
    fun include(key: String): ParseQuery<T> {
        include.add(key)
        return this
    }

    /**
     * Select specific fields to return
     */
    fun select(vararg keys: String): ParseQuery<T> {
        select.addAll(keys)
        return this
    }

    /**
     * Find objects matching the query
     */
    suspend fun find(): List<T> {
        val client = ParseClient.getInstance()
        val response = client.request("GET", "classes/$className", this)
        
        @Suppress("UNCHECKED_CAST")
        val results = response["results"] as List<Map<String, Any>>
        
        return results.map { result ->
            val obj = ParseObject.create(className) as T
            result.forEach { (key, value) ->
                when (key) {
                    "objectId" -> obj.objectId = value as String
                    "createdAt" -> obj.createdAt = java.util.Date(value as String)
                    "updatedAt" -> obj.updatedAt = java.util.Date(value as String)
                    else -> obj[key] = value
                }
            }
            obj
        }
    }

    /**
     * Get the first object matching the query
     */
    suspend fun first(): T? {
        limit(1)
        return find().firstOrNull()
    }

    /**
     * Count the number of objects matching the query
     */
    suspend fun count(): Int {
        val client = ParseClient.getInstance()
        val params = encode().toMutableMap()
        params["count"] = 1
        params["limit"] = 0

        val response = client.request("GET", "classes/$className", object : Encodable {
            override fun encode(): Map<String, Any?> = params
        })

        return response["count"] as Int
    }

    override fun encode(): Map<String, Any?> {
        val params = mutableMapOf<String, Any?>()
        
        if (where.isNotEmpty()) {
            params["where"] = where
        }
        if (order.isNotEmpty()) {
            params["order"] = order.joinToString(",")
        }
        limit?.let { params["limit"] = it }
        skip?.let { params["skip"] = it }
        if (include.isNotEmpty()) {
            params["include"] = include.joinToString(",")
        }
        if (select.isNotEmpty()) {
            params["keys"] = select.joinToString(",")
        }

        return params
    }
}