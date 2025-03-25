package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * ACL class for handling access control lists
 */
@Serializable
class ParseACL : Encodable {
    private val permissions: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf()

    constructor() {
        // Default ACL is empty
    }

    constructor(user: ParseUser) {
        setReadAccess(user, true)
        setWriteAccess(user, true)
    }

    /**
     * Set read access for a user
     */
    fun setReadAccess(user: ParseUser, allowed: Boolean) {
        setAccess(user.objectId!!, "read", allowed)
    }

    /**
     * Set write access for a user
     */
    fun setWriteAccess(user: ParseUser, allowed: Boolean) {
        setAccess(user.objectId!!, "write", allowed)
    }

    /**
     * Set read access for a role
     */
    fun setRoleReadAccess(role: ParseRole, allowed: Boolean) {
        setAccess("role:${role.name}", "read", allowed)
    }

    /**
     * Set write access for a role
     */
    fun setRoleWriteAccess(role: ParseRole, allowed: Boolean) {
        setAccess("role:${role.name}", "write", allowed)
    }

    /**
     * Set public read access
     */
    fun setPublicReadAccess(allowed: Boolean) {
        setAccess("*", "read", allowed)
    }

    /**
     * Set public write access
     */
    fun setPublicWriteAccess(allowed: Boolean) {
        setAccess("*", "write", allowed)
    }

    /**
     * Get read access for a user
     */
    fun getReadAccess(user: ParseUser): Boolean {
        return getAccess(user.objectId!!, "read")
    }

    /**
     * Get write access for a user
     */
    fun getWriteAccess(user: ParseUser): Boolean {
        return getAccess(user.objectId!!, "write")
    }

    /**
     * Get read access for a role
     */
    fun getRoleReadAccess(role: ParseRole): Boolean {
        return getAccess("role:${role.name}", "read")
    }

    /**
     * Get write access for a role
     */
    fun getRoleWriteAccess(role: ParseRole): Boolean {
        return getAccess("role:${role.name}", "write")
    }

    /**
     * Get public read access
     */
    fun getPublicReadAccess(): Boolean {
        return getAccess("*", "read")
    }

    /**
     * Get public write access
     */
    fun getPublicWriteAccess(): Boolean {
        return getAccess("*", "write")
    }

    /**
     * Set access for a key and permission
     */
    private fun setAccess(key: String, permission: String, allowed: Boolean) {
        if (allowed) {
            permissions.getOrPut(key) { mutableMapOf() }[permission] = true
        } else {
            permissions[key]?.remove(permission)
            if (permissions[key]?.isEmpty() == true) {
                permissions.remove(key)
            }
        }
    }

    /**
     * Get access for a key and permission
     */
    private fun getAccess(key: String, permission: String): Boolean {
        return permissions[key]?.get(permission) ?: false
    }

    /**
     * Check if this ACL is equal to another object
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParseACL) return false

        return permissions == other.permissions
    }

    /**
     * Get the hash code of this ACL
     */
    override fun hashCode(): Int {
        return permissions.hashCode()
    }

    /**
     * Get the string representation of this ACL
     */
    override fun toString(): String {
        return "ParseACL(permissions=$permissions)"
    }

    /**
     * Encode this ACL for JSON serialization
     */
    override fun encode(): Map<String, Any?> {
        return permissions.mapValues { (_, value) ->
            value.mapValues { (_, allowed) -> allowed }
        }
    }

    companion object {
        /**
         * Create an ACL with public read access
         */
        fun createPublicReadACL(): ParseACL {
            return ParseACL().apply {
                setPublicReadAccess(true)
            }
        }

        /**
         * Create an ACL with public write access
         */
        fun createPublicWriteACL(): ParseACL {
            return ParseACL().apply {
                setPublicWriteAccess(true)
            }
        }

        /**
         * Create an ACL from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseACL {
            val acl = ParseACL()
            map.forEach { (key, value) ->
                if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val permissions = value as Map<String, Boolean>
                    permissions.forEach { (permission, allowed) ->
                        acl.setAccess(key, permission, allowed)
                    }
                }
            }
            return acl
        }
    }
}