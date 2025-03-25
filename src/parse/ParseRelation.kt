package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Relation class for handling relationships between objects
 */
@Serializable
class ParseRelation<T : ParseObject> : Encodable {
    private val parent: ParseObject
    private val key: String
    private val targetClass: String

    constructor(parent: ParseObject, key: String, targetClass: String) {
        this.parent = parent
        this.key = key
        this.targetClass = targetClass
    }

    /**
     * Get the parent object
     */
    fun getParent(): ParseObject = parent

    /**
     * Get the key of this relation
     */
    fun getKey(): String = key

    /**
     * Get the target class name
     */
    fun getTargetClass(): String = targetClass

    /**
     * Get a query for this relation
     */
    fun query(): ParseQuery<T> {
        return ParseQuery(targetClass).apply {
            where["$key"] = mapOf(
                "__type" to "Pointer",
                "className" to targetClass,
                "objectId" to parent.objectId
            )
        }
    }

    /**
     * Add an object to this relation
     */
    suspend fun add(objects: List<T>) {
        val operation = AddOperation(objects)
        parent.addOperation(key, operation)
    }

    /**
     * Add an object to this relation
     */
    suspend fun add(obj: T) {
        add(listOf(obj))
    }

    /**
     * Remove an object from this relation
     */
    suspend fun remove(objects: List<T>) {
        val operation = RemoveOperation(objects)
        parent.addOperation(key, operation)
    }

    /**
     * Remove an object from this relation
     */
    suspend fun remove(obj: T) {
        remove(listOf(obj))
    }

    /**
     * Check if this relation is equal to another object
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParseRelation<*>) return false

        return parent == other.parent && 
               key == other.key && 
               targetClass == other.targetClass
    }

    /**
     * Get the hash code of this relation
     */
    override fun hashCode(): Int {
        var result = parent.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + targetClass.hashCode()
        return result
    }

    /**
     * Get the string representation of this relation
     */
    override fun toString(): String {
        return "ParseRelation(parent=$parent, key='$key', targetClass='$targetClass')"
    }

    /**
     * Encode this relation for JSON serialization
     */
    override fun encode(): Map<String, Any?> {
        return mapOf(
            "__type" to "Relation",
            "className" to targetClass,
            "objectId" to parent.objectId
        )
    }

    companion object {
        /**
         * Create a relation from a map
         */
        fun <T : ParseObject> fromMap(
            parent: ParseObject,
            key: String,
            map: Map<String, Any?>
        ): ParseRelation<T> {
            val className = map["className"] as? String 
                ?: throw IllegalArgumentException("ClassName is required")
            return ParseRelation(parent, key, className)
        }
    }
}