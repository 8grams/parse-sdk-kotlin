package parse.httpclients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import parse.ParseClient
import parse.internal.Encodable
import java.io.InputStream
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * HTTP client implementation using Ktor
 */
class ParseStreamHttpClient : ParseHttpable {
    private var timeout: Int = 30000
    private var client = createClient()
    private var isClosed = false
    private val activeRequests = mutableListOf<kotlinx.coroutines.Job>()

    private fun createClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(ParseClient.getJson())
            }
            engine {
                requestTimeout = timeout.milliseconds
            }
        }
    }

    override suspend fun request(
        method: String,
        url: String,
        data: Encodable?,
        headers: Map<String, String>,
        onProgress: ((Long, Long) -> Unit)?
    ): Map<String, Any> {
        if (isClosed) {
            throw IllegalStateException("Client is closed")
        }

        val job = GlobalScope.launch {
            try {
                val response = client.request(url) {
                    this.method = HttpMethod.parse(method)
                    headers.forEach { (key, value) -> header(key, value) }
                    
                    if (data != null) {
                        contentType(ContentType.Application.Json)
                        setBody(data.encode())
                        
                        if (onProgress != null) {
                            onUpload { bytesSentTotal, contentLength ->
                                onProgress(bytesSentTotal, contentLength)
                            }
                        }
                    }
                }

                val responseText = response.bodyAsText()
                if (responseText.isNotEmpty()) {
                    @Suppress("UNCHECKED_CAST")
                    ParseClient.getJson().decodeFromString<Map<String, Any>>(responseText)
                } else {
                    emptyMap()
                }
            } finally {
                activeRequests.remove(coroutineContext[kotlinx.coroutines.Job]!!)
            }
        }

        activeRequests.add(job)
        return job.await()
    }

    override fun setTimeout(milliseconds: Int) {
        timeout = milliseconds
        client.close()
        client = createClient()
    }

    override suspend fun getInputStream(url: String): InputStream {
        return client.get(url).body()
    }

    override suspend fun download(url: String): ByteArray {
        return client.get(url).body()
    }

    override suspend fun downloadToFile(url: String, file: java.io.File) {
        val bytes = download(url)
        file.writeBytes(bytes)
    }

    override fun cancelAllRequests() {
        activeRequests.forEach { it.cancel() }
        activeRequests.clear()
    }

    override fun getTimeout(): Int = timeout

    override fun isClosed(): Boolean = isClosed

    override fun close() {
        cancelAllRequests()
        client.close()
        isClosed = true
    }
}