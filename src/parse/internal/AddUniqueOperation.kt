package parse.internal

/**
 * Operation to add unique elements to an array field
 */
class AddUniqueOperation(private val values: List<Any?>) : FieldOperation {
    override fun apply(oldValue: Any?): Any {
        val current = oldValue as? List<Any?> ?: emptyList()
        return (current + values).distinct()
    }

    override fun encode(): Map<String, Any?> = mapOf(
        "__op" to "AddUnique",
        "objects" to values
    )
}