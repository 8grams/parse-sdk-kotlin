package parse

/**
 * Interface for Parse storage implementations
 */
interface ParseStorageInterface {
    /**
     * Get a value from storage
     */
    suspend fun get(key: String): String?

    /**
     * Set a value in storage
     */
    suspend fun set(key: String, value: String)

    /**
     * Remove a value from storage
     */
    suspend fun remove(key: String)

    /**
     * Clear all values from storage
     */
    suspend fun clear()

    /**
     * Get all keys in storage
     */
    suspend fun getAllKeys(): Set<String>

    /**
     * Check if a key exists in storage
     */
    suspend fun contains(key: String): Boolean

    /**
     * Get the size of storage
     */
    suspend fun size(): Int

    /**
     * Check if storage is empty
     */
    suspend fun isEmpty(): Boolean = size() == 0

    /**
     * Check if storage is not empty
     */
    suspend fun isNotEmpty(): Boolean = !isEmpty()
}