package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Analytics class for handling analytics
 */
@Serializable
class ParseAnalytics : Encodable {
    companion object {
        /**
         * Track an event
         */
        @JvmStatic
        suspend fun trackEvent(name: String, params: Map<String, Any?> = emptyMap()) {
            val client = ParseClient.getInstance()
            client.request(
                "POST",
                "events/$name",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = params
                }
            )
        }

        /**
         * Track an app open
         */
        @JvmStatic
        suspend fun trackAppOpened() {
            val client = ParseClient.getInstance()
            client.request("POST", "events/AppOpened")
        }

        /**
         * Track a push notification open
         */
        @JvmStatic
        suspend fun trackPushNotificationOpen(pushHash: String) {
            val client = ParseClient.getInstance()
            client.request(
                "POST",
                "events/PushNotificationOpen",
                object : Encodable {
                    override fun encode(): Map<String, Any?> = mapOf("push_hash" to pushHash)
                }
            )
        }

        /**
         * Track a custom event
         */
        @JvmStatic
        suspend fun trackCustomEvent(name: String, params: Map<String, Any?> = emptyMap()) {
            trackEvent(name, params)
        }
    }
}