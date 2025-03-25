package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Schema class for handling schema management
 */
@Serializable
class ParseSchema : Encodable {
    companion object {
        /**
         * Get all schemas
         */
        @JvmStatic
        suspend fun getAllSchemas(): List<Map<String, Any?>> {
            val client = ParseClient.getInstance()
            val response = client.request("GET", "schemas")
            
            @Suppress("UNCHECKED_CAST")
            return response["results"] as List<Map<String, Any?>>
        }

        /**
         * Get a schema by class name
         */
        @JvmStatic
        suspend fun getSchema(className: String): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request("GET", "schemas/$className")
        }

        /**
         * Create a schema
         */
        @JvmStatic
        suspend fun createSchema(
            className: String,
            fields: Map<String, Map<String, Any?>>
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "POST",
                "schemas",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf(
                        "className" to className,
                        "fields" to fields
                    )
                }
            )
        }

        /**
         * Update a schema
         */
        @JvmStatic
        suspend fun updateSchema(
            className: String,
            fields: Map<String, Map<String, Any?>>
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "PUT",
                "schemas/$className",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf(
                        "fields" to fields
                    )
                }
            )
        }

        /**
         * Delete a schema
         */
        @JvmStatic
        suspend fun deleteSchema(className: String) {
            val client = ParseClient.getInstance()
            client.request("DELETE", "schemas/$className")
        }

        /**
         * Add a field to a schema
         */
        @JvmStatic
        suspend fun addField(
            className: String,
            fieldName: String,
            fieldType: String,
            required: Boolean = false,
            defaultValue: Any? = null
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "PUT",
                "schemas/$className",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf(
                        "fields" to mapOf(
                            fieldName to mapOf(
                                "type" to fieldType,
                                "required" to required,
                                "defaultValue" to defaultValue
                            )
                        )
                    )
                }
            )
        }

        /**
         * Delete a field from a schema
         */
        @JvmStatic
        suspend fun deleteField(
            className: String,
            fieldName: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "PUT",
                "schemas/$className",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf(
                        "fields" to mapOf(
                            fieldName to mapOf(
                                "__op" to "Delete"
                            )
                        )
                    )
                }
            )
        }

        /**
         * Add an index to a schema
         */
        @JvmStatic
        suspend fun addIndex(
            className: String,
            fieldName: String,
            indexType: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "PUT",
                "schemas/$className",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf(
                        "indexes" to mapOf(
                            fieldName to mapOf(
                                indexType to 1
                            )
                        )
                    )
                }
            )
        }

        /**
         * Delete an index from a schema
         */
        @JvmStatic
        suspend fun deleteIndex(
            className: String,
            fieldName: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "PUT",
                "schemas/$className",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf(
                        "indexes" to mapOf(
                            fieldName to mapOf(
                                "__op" to "Delete"
                            )
                        )
                    )
                }
            )
        }
    }
}