package parse.internal

/**
 * Operation to add elements to an array field
 */
class AddOperation(private val values: List<Any?>) : FieldOperation {
    override fun apply(oldValue: Any?): Any {
        val current = oldValue as? List<Any?> ?: emptyList()
        return current + values
    }

    override fun encode(): Map<String, Any?> = mapOf(
        "__op" to "Add",
        "objects" to values
    )
}