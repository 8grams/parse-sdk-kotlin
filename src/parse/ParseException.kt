package parse

class ParseException(
    message: String,
    val code: Int = 0,
    previous: Throwable? = null
) : Exception(message, previous)