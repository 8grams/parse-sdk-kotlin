package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Cloud class for handling cloud functions
 */
@Serializable
class ParseCloud : Encodable {
    companion object {
        /**
         * Call a cloud function
         */
        @JvmStatic
        suspend fun callFunction(name: String, params: Map<String, Any?> = emptyMap()): Any? {
            val client = ParseClient.getInstance()
            val response = client.request(
                "POST",
                "functions/$name",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = params
                }
            )
            return response["result"]
        }

        /**
         * Call a cloud function with progress
         */
        @JvmStatic
        suspend fun callFunctionWithProgress(
            name: String,
            params: Map<String, Any?> = emptyMap(),
            onProgress: (Int) -> Unit
        ): Any? {
            val client = ParseClient.getInstance()
            val response = client.request(
                "POST",
                "functions/$name",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = params
                },
                mapOf("X-Parse-Progress" to "true")
            )
            return response["result"]
        }

        /**
         * Call a cloud function with job status
         */
        @JvmStatic
        suspend fun callFunctionWithJobStatus(
            name: String,
            params: Map<String, Any?> = emptyMap()
        ): String {
            val client = ParseClient.getInstance()
            val response = client.request(
                "POST",
                "jobs/$name",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = params
                }
            )
            return response["jobStatusId"] as String
        }

        /**
         * Get the status of a job
         */
        @JvmStatic
        suspend fun getJobStatus(jobStatusId: String): Map<String, Any?> {
            val client = ParseClient.getInstance()
            return client.request("GET", "jobs/$jobStatusId")
        }
    }
}