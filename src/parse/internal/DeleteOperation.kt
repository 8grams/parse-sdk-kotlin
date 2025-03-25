package parse.internal

/**
 * Operation to delete a field
 */
class DeleteOperation : FieldOperation {
    override fun apply(oldValue: Any?): Any? = null

    override fun encode(): Map<String, Any?> = mapOf(
        "__op" to "Delete"
    )
}