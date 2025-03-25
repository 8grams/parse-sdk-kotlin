package parse

/**
 * In-memory implementation of ParseStorageInterface
 */
class ParseMemoryStorage : ParseStorageInterface {
    private val storage = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? {
        return storage[key]
    }

    override suspend fun set(key: String, value: String) {
        storage[key] = value
    }

    override suspend fun remove(key: String) {
        storage.remove(key)
    }

    override suspend fun clear() {
        storage.clear()
    }

    override suspend fun getAllKeys(): Set<String> {
        return storage.keys.toSet()
    }

    override suspend fun contains(key: String): Boolean {
        return storage.containsKey(key)
    }

    override suspend fun size(): Int {
        return storage.size
    }

    companion object {
        private var instance: ParseMemoryStorage? = null

        /**
         * Get the singleton instance
         */
        fun getInstance(): ParseMemoryStorage {
            if (instance == null) {
                instance = ParseMemoryStorage()
            }
            return instance!!
        }
    }
}