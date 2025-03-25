package parse.httpclients

import parse.internal.Encodable

/**
 * Interface for Parse HTTP clients
 */
interface ParseHttpable {
    /**
     * Send a request to Parse Server
     * @param method The HTTP method
     * @param url The full URL
     * @param data The request data
     * @param headers The request headers
     * @param onProgress Progress callback (bytesWritten, contentLength)
     * @return The response data
     */
    suspend fun request(
        method: String,
        url: String,
        data: Encodable? = null,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Long, Long) -> Unit)? = null
    ): Map<String, Any>

    /**
     * Set request timeout
     * @param milliseconds Timeout in milliseconds
     */
    fun setTimeout(milliseconds: Int)

    /**
     * Get input stream from URL
     * @param url The URL to get stream from
     * @return The input stream
     */
    suspend fun getInputStream(url: String): java.io.InputStream

    /**
     * Download file from URL
     */
    suspend fun download(url: String): ByteArray

    /**
     * Download file to local file
     */
    suspend fun downloadToFile(url: String, file: java.io.File)

    /**
     * Cancel all ongoing requests
     */
    fun cancelAllRequests()

    /**
     * Get current request timeout
     */
    fun getTimeout(): Int

    /**
     * Check if client is closed
     */
    fun isClosed(): Boolean

    /**
     * Close the client
     */
    fun close()
}