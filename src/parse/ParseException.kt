package parse

/**
 * Exception class for Parse-specific errors
 */
class ParseException : Exception {
    val code: Int
    val details: Map<String, Any?>?

    constructor(message: String, code: Int = -1, details: Map<String, Any?>? = null) : super(message) {
        this.code = code
        this.details = details
    }

    constructor(message: String, cause: Throwable, code: Int = -1, details: Map<String, Any?>? = null) : super(message, cause) {
        this.code = code
        this.details = details
    }

    companion object {
        // Parse error codes
        const val OTHER_CAUSE = -1
        const val INTERNAL_SERVER_ERROR = 1
        const val CONNECTION_FAILED = 100
        const val OBJECT_NOT_FOUND = 101
        const val INVALID_QUERY = 102
        const val INVALID_CLASS_NAME = 103
        const val MISSING_OBJECT_ID = 104
        const val INVALID_KEY_NAME = 105
        const val INVALID_POINTER = 106
        const val INVALID_JSON = 107
        const val COMMAND_UNAVAILABLE = 108
        const val NOT_INITIALIZED = 109
        const val INCORRECT_TYPE = 111
        const val INVALID_CHANNEL_NAME = 112
        const val PUSH_MISCONFIGURED = 115
        const val OBJECT_TOO_LARGE = 116
        const val OPERATION_FORBIDDEN = 119
        const val CACHE_MISS = 120
        const val INVALID_NESTED_KEY = 121
        const val INVALID_FILE_NAME = 122
        const val INVALID_ACL = 123
        const val TIMEOUT = 124
        const val INVALID_EMAIL_ADDRESS = 125
        const val MISSING_CONTENT_TYPE = 126
        const val MISSING_CONTENT_LENGTH = 127
        const val INVALID_CONTENT_LENGTH = 128
        const val FILE_TOO_LARGE = 129
        const val FILE_SAVE_ERROR = 130
        const val DUPLICATE_VALUE = 137
        const val INVALID_ROLE_NAME = 139
        const val EXCEEDED_QUOTA = 140
        const val SCRIPT_FAILED = 141
        const val VALIDATION_ERROR = 142
        const val INVALID_IMAGE_DATA = 150
        const val UNSAVED_FILE_ERROR = 151
        const val INVALID_PUSH_TIME_ERROR = 152
        const val FILE_DELETE_ERROR = 153
        const val FILE_DELETE_UNSUCCESSFUL = 154
        const val REQUEST_LIMIT_EXCEEDED = 155
        const val INVALID_EVENT_NAME = 160
        const val USERNAME_MISSING = 200
        const val PASSWORD_MISSING = 201
        const val USERNAME_TAKEN = 202
        const val EMAIL_TAKEN = 203
        const val EMAIL_MISSING = 204
        const val EMAIL_NOT_FOUND = 205
        const val SESSION_MISSING = 206
        const val MUST_CREATE_USER_THROUGH_SIGNUP = 207
        const val ACCOUNT_ALREADY_LINKED = 208
        const val INVALID_SESSION_TOKEN = 209
        const val LINKED_ID_MISSING = 250
        const val INVALID_LINKED_SESSION = 251
        const val UNSUPPORTED_SERVICE = 252
        const val INVALID_SCHEMA_OPERATION = 255
        const val AGGREGATE_ERROR = 600
        const val FILE_READ_ERROR = 601
        const val X_DOMAIN_REQUEST = 602
    }
}