package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Role class for handling user roles and permissions
 */
@Serializable
class ParseRole : ParseObject("_Role") {
    var name: String = ""
        set(value) {
            field = value
            this["name"] = value
        }

    var users: ParseRelation<ParseUser>? = null
        private set

    var roles: ParseRelation<ParseRole>? = null
        private set

    constructor(name: String) {
        this.name = name
        this.acl = ParseACL()
    }

    /**
     * Get the users relation
     */
    fun getUsers(): ParseRelation<ParseUser> {
        if (users == null) {
            users = ParseRelation(this, "users", "_User")
        }
        return users!!
    }

    /**
     * Get the roles relation
     */
    fun getRoles(): ParseRelation<ParseRole> {
        if (roles == null) {
            roles = ParseRelation(this, "roles", "_Role")
        }
        return roles!!
    }

    /**
     * Add a user to this role
     */
    suspend fun addUser(user: ParseUser) {
        getUsers().add(user)
    }

    /**
     * Remove a user from this role
     */
    suspend fun removeUser(user: ParseUser) {
        getUsers().remove(user)
    }

    /**
     * Add a role to this role
     */
    suspend fun addRole(role: ParseRole) {
        getRoles().add(role)
    }

    /**
     * Remove a role from this role
     */
    suspend fun removeRole(role: ParseRole) {
        getRoles().remove(role)
    }

    /**
     * Get all users in this role
     */
    suspend fun getUsersInRole(): List<ParseUser> {
        return getUsers().query().find()
    }

    /**
     * Get all roles in this role
     */
    suspend fun getRolesInRole(): List<ParseRole> {
        return getRoles().query().find()
    }

    override suspend fun save(): ParseRole {
        if (name.isEmpty()) {
            throw IllegalStateException("Role name cannot be empty")
        }
        return super.save() as ParseRole
    }

    companion object {
        /**
         * Get a query for roles
         */
        fun query(): ParseQuery<ParseRole> {
            return ParseQuery("_Role")
        }

        /**
         * Create a new role
         */
        fun create(name: String): ParseRole {
            return ParseRole(name)
        }

        /**
         * Create a role from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseRole {
            val name = map["name"] as? String ?: throw IllegalArgumentException("Name is required")
            val role = ParseRole(name)
            
            map.forEach { (key, value) ->
                when (key) {
                    "objectId" -> role.objectId = value as String
                    "createdAt" -> role.createdAt = Date(value as String)
                    "updatedAt" -> role.updatedAt = Date(value as String)
                    "ACL" -> role.acl = ParseACL.fromMap(value as Map<String, Any?>)
                    else -> role[key] = value
                }
            }

            return role
        }
    }
}