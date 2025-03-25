package parse.internal

/**
 * Interface for field operations
 */
interface FieldOperation : Encodable {
    /**
     * Apply the operation to a value
     * @param oldValue The current value
     * @return The new value after applying the operation
     */
    fun apply(oldValue: Any?): Any?
}