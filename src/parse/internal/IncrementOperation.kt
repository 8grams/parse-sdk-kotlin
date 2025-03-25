package parse.internal

/**
 * Operation to increment a numeric field
 */
class IncrementOperation(private val amount: Number) : FieldOperation {
    override fun apply(oldValue: Any?): Any {
        val current = oldValue as? Number ?: 0
        return when {
            current is Int && amount is Int -> current + amount
            current is Long && amount is Long -> current + amount
            current is Float && amount is Float -> current + amount
            current is Double && amount is Double -> current + amount
            else -> (current.toDouble() + amount.toDouble())
        }
    }

    override fun encode(): Map<String, Any?> = mapOf(
        "__op" to "Increment",
        "amount" to amount
    )
}