package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Push class for handling push notifications
 */
@Serializable
class ParsePush : Encodable {
    private var channels: MutableList<String>? = null
    private var query: ParseQuery<ParseInstallation>? = null
    private var pushTime: Date? = null
    private var expirationTime: Date? = null
    private var expirationInterval: Int? = null
    private var data: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Set the channels to send to
     */
    fun setChannels(channels: List<String>): ParsePush {
        this.channels = channels.toMutableList()
        return this
    }

    /**
     * Set the query to send to
     */
    fun setQuery(query: ParseQuery<ParseInstallation>): ParsePush {
        this.query = query
        return this
    }

    /**
     * Set the time to send the push
     */
    fun setPushTime(time: Date): ParsePush {
        this.pushTime = time
        return this
    }

    /**
     * Set the time when the push expires
     */
    fun setExpirationTime(time: Date): ParsePush {
        this.expirationTime = time
        return this
    }

    /**
     * Set the interval when the push expires
     */
    fun setExpirationInterval(seconds: Int): ParsePush {
        this.expirationInterval = seconds
        return this
    }

    /**
     * Set the push data
     */
    fun setData(data: Map<String, Any?>): ParsePush {
        this.data = data.toMutableMap()
        return this
    }

    /**
     * Send the push notification
     */
    suspend fun send() {
        val client = ParseClient.getInstance()
        client.request("POST", "push", this)
    }

    override fun encode(): Map<String, Any?> {
        val encoded = mutableMapOf<String, Any?>()

        channels?.let { encoded["channels"] = it }
        query?.let { encoded["where"] = it.encode() }
        pushTime?.let { encoded["push_time"] = it.toISOString() }
        expirationTime?.let { encoded["expiration_time"] = it.toISOString() }
        expirationInterval?.let { encoded["expiration_interval"] = it }
        encoded["data"] = data

        return encoded
    }

    companion object {
        /**
         * Create a new push notification
         */
        @JvmStatic
        fun create(): ParsePush {
            return ParsePush()
        }

        /**
         * Send a push notification to channels
         */
        @JvmStatic
        suspend fun sendToChannels(channels: List<String>, data: Map<String, Any?>) {
            create()
                .setChannels(channels)
                .setData(data)
                .send()
        }

        /**
         * Send a push notification to a query
         */
        @JvmStatic
        suspend fun sendToQuery(query: ParseQuery<ParseInstallation>, data: Map<String, Any?>) {
            create()
                .setQuery(query)
                .setData(data)
                .send()
        }
    }
}