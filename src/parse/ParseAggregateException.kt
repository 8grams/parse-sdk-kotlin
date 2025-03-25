package parse

/**
 * Exception class for aggregate errors
 */
class ParseAggregateException : ParseException {
    val errors: List<ParseException>

    constructor(
        message: String,
        errors: List<ParseException>,
        code: Int = AGGREGATE_ERROR,
        details: Map<String, Any?>? = null
    ) : super(message, code, details) {
        this.errors = errors
    }

    constructor(
        message: String,
        cause: Throwable,
        errors: List<ParseException>,
        code: Int = AGGREGATE_ERROR,
        details: Map<String, Any?>? = null
    ) : super(message, cause, code, details) {
        this.errors = errors
    }

    /**
     * Get all error messages
     */
    fun getErrorMessages(): List<String> {
        return errors.map { it.message ?: "" }
    }

    /**
     * Get all error codes
     */
    fun getErrorCodes(): List<Int> {
        return errors.map { it.code }
    }

    /**
     * Get all error details
     */
    fun getErrorDetails(): List<Map<String, Any?>?> {
        return errors.map { it.details }
    }

    companion object {
        /**
         * Create an aggregate exception from a list of exceptions
         */
        fun fromExceptions(errors: List<ParseException>): ParseAggregateException {
            val message = "Multiple errors occurred: ${errors.joinToString("; ") { it.message ?: "" }}"
            return ParseAggregateException(message, errors)
        }
    }
}