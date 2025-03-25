package parse.internal

/**
 * Operation to set a field value
 */
class SetOperation(private val value: Any?) : FieldOperation {
    override fun apply(oldValue: Any?): Any? = value

    override fun encode(): Map<String, Any?> = mapOf(
        "__op" to "Set",
        "value" to value
    )
}