package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Hooks class for handling cloud code hooks
 */
@Serializable
class ParseHooks : Encodable {
    companion object {
        /**
         * Get all hooks
         */
        @JvmStatic
        suspend fun getHooks(): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request("GET", "hooks")
        }

        /**
         * Get a hook by name
         */
        @JvmStatic
        suspend fun getHook(name: String): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request("GET", "hooks/$name")
        }

        /**
         * Create a before save hook
         */
        @JvmStatic
        suspend fun createBeforeSaveHook(
            className: String,
            url: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "POST",
                "hooks/$className/beforeSave",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("url" to url)
                }
            )
        }

        /**
         * Create an after save hook
         */
        @JvmStatic
        suspend fun createAfterSaveHook(
            className: String,
            url: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "POST",
                "hooks/$className/afterSave",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("url" to url)
                }
            )
        }

        /**
         * Create a before delete hook
         */
        @JvmStatic
        suspend fun createBeforeDeleteHook(
            className: String,
            url: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "POST",
                "hooks/$className/beforeDelete",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("url" to url)
                }
            )
        }

        /**
         * Create an after delete hook
         */
        @JvmStatic
        suspend fun createAfterDeleteHook(
            className: String,
            url: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "POST",
                "hooks/$className/afterDelete",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("url" to url)
                }
            )
        }

        /**
         * Update a hook
         */
        @JvmStatic
        suspend fun updateHook(
            className: String,
            hookType: String,
            url: String
        ): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request(
                "PUT",
                "hooks/$className/$hookType",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("url" to url)
                }
            )
        }

        /**
         * Delete a hook
         */
        @JvmStatic
        suspend fun deleteHook(className: String, hookType: String) {
            val client = ParseClient.getInstance()
            client.request("DELETE", "hooks/$className/$hookType")
        }
    }
}