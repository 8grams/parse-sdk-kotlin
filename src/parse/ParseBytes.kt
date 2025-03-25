package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Bytes class for handling binary data
 */
@Serializable
class ParseBytes : Encodable {
    private val data: ByteArray
    private val base64Data: String

    constructor(data: ByteArray) {
        this.data = data.clone()
        this.base64Data = Base64.getEncoder().encodeToString(data)
    }

    constructor(base64String: String) {
        this.base64Data = base64String
        this.data = try {
            Base64.getDecoder().decode(base64String)
        } catch (e: IllegalArgumentException) {
            throw ParseException(
                "Invalid base64 string",
                ParseException.INCORRECT_TYPE,
                mapOf("base64" to base64String)
            )
        }
    }

    /**
     * Get the raw byte data
     */
    fun getData(): ByteArray = data.clone()

    /**
     * Get the base64 encoded string
     */
    fun getBase64Data(): String = base64Data

    /**
     * Get the size of the data in bytes
     */
    fun size(): Int = data.size

    /**
     * Check if this byte data is empty
     */
    fun isEmpty(): Boolean = data.isEmpty()

    /**
     * Check if this byte data is not empty
     */
    fun isNotEmpty(): Boolean = data.isNotEmpty()

    /**
     * Check if this byte data is equal to another object
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParseBytes) return false

        return data.contentEquals(other.data)
    }

    /**
     * Get the hash code of this byte data
     */
    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    /**
     * Get the string representation of this byte data
     */
    override fun toString(): String {
        return "ParseBytes(size=${data.size}, base64=$base64Data)"
    }

    /**
     * Encode this byte data for JSON serialization
     */
    override fun encode(): Map<String, Any?> {
        return mapOf(
            "__type" to "Bytes",
            "base64" to base64Data
        )
    }

    companion object {
        /**
         * Create ParseBytes from raw data
         */
        fun fromByteArray(data: ByteArray): ParseBytes {
            return ParseBytes(data)
        }

        /**
         * Create ParseBytes from a base64 string
         */
        fun fromBase64String(base64String: String): ParseBytes {
            return ParseBytes(base64String)
        }

        /**
         * Create ParseBytes from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseBytes {
            val base64 = map["base64"] as? String 
                ?: throw ParseException(
                    "Base64 data is required",
                    ParseException.INCORRECT_TYPE,
                    map
                )
            return ParseBytes(base64)
        }
    }
}