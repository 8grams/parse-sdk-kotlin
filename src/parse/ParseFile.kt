package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.io.File
import java.io.InputStream
import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * File class for handling file uploads and downloads
 */
@Serializable
class ParseFile(
    name: String,
    private var data: ByteArray? = null,
    private var contentType: String? = null,
    private var metadata: Map<String, Any>? = null
) : Encodable {
    var name: String = name
        private set

    var url: String? = null
        private set

    var objectId: String? = null
        private set

    var createdAt: Date? = null
        private set

    var updatedAt: Date? = null
        private set

    /**
     * Get the file data
     */
    fun getData(): ByteArray? = data

    /**
     * Get the content type
     */
    fun getContentType(): String? = contentType

    /**
     * Check if the file is dirty (has unsaved changes)
     */
    fun isDirty(): Boolean = data != null

    /**
     * Get file data as a stream
     */
    suspend fun getDataStream(): java.io.InputStream {
        if (url == null) {
            throw IllegalStateException("File has not been saved")
        }
        val client = ParseClient.getInstance()
        return client.getHttpClient().getInputStream(url!!)
    }

    /**
     * Get file URL
     */
    fun getUrl(): String? = url

    /**
     * Check if file data is available locally
     */
    fun isDataAvailable(): Boolean = data != null

    /**
     * Save the file to Parse Server
     */
    suspend fun save(): ParseFile {
        if (data == null) {
            throw IllegalStateException("Cannot save a file without data")
        }

        val client = ParseClient.getInstance()
        val response = client.request(
            "POST",
            "files/$name",
            object : Encodable {
                override fun encode(): Map<String, Any?> = mapOf(
                    "file" to data,
                    "contentType" to contentType
                )
            }
        )

        url = response["url"] as String
        name = response["name"] as String
        objectId = response["objectId"] as String
        createdAt = Date(response["createdAt"] as String)
        updatedAt = Date(response["updatedAt"] as String)

        // Clear the data after successful upload
        data = null

        return this
    }

    /**
     * Delete the file from Parse Server
     */
    suspend fun delete() {
        if (objectId == null) {
            throw IllegalStateException("Cannot delete a file without an objectId")
        }

        val client = ParseClient.getInstance()
        client.request("DELETE", "files/$objectId")
    }

    /**
     * Download the file data
     */
    suspend fun download(): ByteArray {
        if (url == null) {
            throw IllegalStateException("Cannot download a file without a URL")
        }

        val client = ParseClient.getInstance()
        return client.getHttpClient().download(url!!)
    }

    /**
     * Download the file to a local file
     */
    suspend fun downloadToFile(file: File) {
        if (url == null) {
            throw IllegalStateException("Cannot download a file without a URL")
        }

        val client = ParseClient.getInstance()
        client.getHttpClient().downloadToFile(url!!, file)
    }

    /**
     * Get an input stream for the file
     */
    suspend fun getInputStream(): InputStream {
        if (url == null) {
            throw IllegalStateException("Cannot get input stream for a file without a URL")
        }

        val client = ParseClient.getInstance()
        return client.getHttpClient().getInputStream(url!!)
    }

    /**
     * Save file with progress callback
     */
    suspend fun save(onProgress: ((Int) -> Unit)? = null): ParseFile {
        val client = ParseClient.getInstance()
        
        val response = client.request(
            "POST",
            "files/$name",
            object : Encodable {
                override fun encode(): Map<String, Any?> = mapOf(
                    "base64" to android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP),
                    "_ContentType" to contentType
                )
            }
        ) { bytesWritten, contentLength ->
            onProgress?.invoke((bytesWritten.toFloat() / contentLength * 100).toInt())
        }

        url = response["url"] as String
        name = response["name"] as String
        return this
    }

    /**
     * Save file asynchronously with progress callback
     */
    fun saveInBackground(
        onProgress: ((Int) -> Unit)? = null,
        callback: (ParseException?) -> Unit
    ) {
        kotlinx.coroutines.GlobalScope.launch {
            try {
                save(onProgress)
                callback(null)
            } catch (e: ParseException) {
                callback(e)
            }
        }
    }

    /**
     * Delete file asynchronously
     */
    fun deleteInBackground(callback: (ParseException?) -> Unit) {
        GlobalScope.launch {
            try {
                delete()
                callback(null)
            } catch (e: ParseException) {
                callback(e)
            }
        }
    }

    /**
     * Get file data asynchronously
     */
    fun getDataInBackground(callback: (ByteArray?, ParseException?) -> Unit) {
        GlobalScope.launch {
            try {
                val data = getData()
                callback(data, null)
            } catch (e: ParseException) {
                callback(null, e)
            }
        }
    }

    /**
     * Get file data stream asynchronously
     */
    fun getDataStreamInBackground(callback: (java.io.InputStream?, ParseException?) -> Unit) {
        GlobalScope.launch {
            try {
                val stream = getDataStream()
                callback(stream, null)
            } catch (e: ParseException) {
                callback(null, e)
            }
        }
    }

    /**
     * Get file metadata
     */
    fun getMetadata(): Map<String, Any>? = metadata

    /**
     * Set file metadata
     */
    fun setMetadata(metadata: Map<String, Any>) {
        this.metadata = metadata
    }

    /**
     * Get file size
     */
    fun getSize(): Long {
        return data?.size?.toLong() ?: -1
    }

    /**
     * Cancel ongoing operations
     */
    fun cancel() {
        // Implementation depends on the HTTP client's cancellation support
        // For now, we'll just clear the data
        data = null
    }

    companion object {
        /**
         * Create a file from byte array
         */
        @JvmStatic
        fun create(name: String, data: ByteArray, contentType: String? = null): ParseFile {
            return ParseFile(name, data, contentType)
        }

        /**
         * Create a file from local file
         */
        @JvmStatic
        fun create(file: File): ParseFile {
            return ParseFile(file.name, file.readBytes(), file.extension.let { if (it.isNotEmpty()) "application/$it" else null })
        }

        /**
         * Create a file from input stream
         */
        @JvmStatic
        fun create(name: String, inputStream: InputStream, contentType: String? = null): ParseFile {
            return ParseFile(name, inputStream.readBytes(), contentType)
        }

        /**
         * Create a file with metadata
         */
        @JvmStatic
        fun create(name: String, data: ByteArray, contentType: String? = null, metadata: Map<String, Any>? = null): ParseFile {
            return ParseFile(name, data, contentType, metadata)
        }

        /**
         * Create a file from URL
         */
        @JvmStatic
        suspend fun createFromUrl(url: String): ParseFile {
            val client = ParseClient.getInstance()
            val stream = client.getHttpClient().getInputStream(url)
            val fileName = url.substringAfterLast('/')
            return create(fileName, stream.readBytes())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParseFile

        if (!name.contentEquals(other.name)) return false
        if (objectId != other.objectId) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (objectId?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }

    override fun encode(): Map<String, Any?> {
        val encoded = mutableMapOf<String, Any?>()
        
        if (objectId != null) {
            encoded["__type"] = "File"
            encoded["objectId"] = objectId
            encoded["name"] = name
            encoded["url"] = url
            metadata?.let { encoded["metadata"] = it }
        } else if (data != null) {
            encoded["file"] = data
            encoded["contentType"] = contentType
            metadata?.let { encoded["metadata"] = it }
        }

        return encoded
    }
}