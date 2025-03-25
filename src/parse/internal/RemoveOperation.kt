package parse.internal

/**
 * Operation to remove elements from an array field
 */
class RemoveOperation(private val values: List<Any?>) : FieldOperation {
    override fun apply(oldValue: Any?): Any {
        val current = oldValue as? List<Any?> ?: emptyList()
        return current.filterNot { values.contains(it) }
    }

    override fun encode(): Map<String, Any?> = mapOf(
        "__op" to "Remove",
        "objects" to values
    )
}