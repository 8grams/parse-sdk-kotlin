package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Installation class for handling installation data and push notification settings
 */
@Serializable
class ParseInstallation : ParseObject("_Installation") {
    var deviceType: String? = null
        set(value) {
            field = value
            this["deviceType"] = value
        }

    var installationId: String? = null
        set(value) {
            field = value
            this["installationId"] = value
        }

    var deviceToken: String? = null
        set(value) {
            field = value
            this["deviceToken"] = value
        }

    var badge: Int = 0
        set(value) {
            field = value
            this["badge"] = value
        }

    var timeZone: String? = null
        set(value) {
            field = value
            this["timeZone"] = value
        }

    var channels: MutableList<String> = mutableListOf()
        set(value) {
            field = value
            this["channels"] = value
        }

    var appName: String? = null
        set(value) {
            field = value
            this["appName"] = value
        }

    var appVersion: String? = null
        set(value) {
            field = value
            this["appVersion"] = value
        }

    var appIdentifier: String? = null
        set(value) {
            field = value
            this["appIdentifier"] = value
        }

    var parseVersion: String? = null
        set(value) {
            field = value
            this["parseVersion"] = value
        }

    var localeIdentifier: String? = null
        set(value) {
            field = value
            this["localeIdentifier"] = value
        }

    companion object {
        private var currentInstallation: ParseInstallation? = null

        /**
         * Get the current installation
         */
        @JvmStatic
        fun getCurrentInstallation(): ParseInstallation? = currentInstallation

        /**
         * Get a query for installations
         */
        @JvmStatic
        fun query(): ParseQuery<ParseInstallation> {
            return ParseQuery("_Installation")
        }

        /**
         * Create a new installation
         */
        @JvmStatic
        fun create(): ParseInstallation {
            return ParseInstallation().apply {
                this.deviceType = "android" // or "ios" based on platform
                this.installationId = UUID.randomUUID().toString()
                this.timeZone = TimeZone.getDefault().id
                this.parseVersion = "1.0.0" // Update with actual SDK version
            }
        }

        /**
         * Get or create the current installation
         */
        @JvmStatic
        suspend fun getCurrentInstallationOrCreate(): ParseInstallation {
            if (currentInstallation != null) {
                return currentInstallation!!
            }

            val installation = create()
            try {
                installation.save()
                currentInstallation = installation
                return installation
            } catch (e: Exception) {
                // If save fails, try to find existing installation
                val query = query()
                query.where["installationId"] = installation.installationId
                val results = query.find()
                if (results.isNotEmpty()) {
                    currentInstallation = results[0]
                    return currentInstallation!!
                }
                throw e
            }
        }
    }

    /**
     * Add a channel to the installation
     */
    fun addChannel(channel: String) {
        if (!channels.contains(channel)) {
            channels.add(channel)
        }
    }

    /**
     * Remove a channel from the installation
     */
    fun removeChannel(channel: String) {
        channels.remove(channel)
    }

    /**
     * Set the device token for push notifications
     */
    fun setDeviceToken(token: String) {
        deviceToken = token
    }

    /**
     * Set the badge count
     */
    fun setBadge(count: Int) {
        badge = count
    }

    override suspend fun save(): ParseInstallation {
        val client = ParseClient.getInstance()
        val method = if (objectId == null) "POST" else "PUT"
        val endpoint = "installations" + (objectId?.let { "/$it" } ?: "")

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

    override suspend fun delete() {
        if (objectId == null) {
            throw IllegalStateException("Cannot delete an installation without an objectId")
        }

        val client = ParseClient.getInstance()
        client.request("DELETE", "installations/$objectId")
        currentInstallation = null
    }
}