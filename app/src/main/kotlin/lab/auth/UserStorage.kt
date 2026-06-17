package lab.auth

import lab.db.DbStorage
import java.security.MessageDigest

class UserStorage {

    private val db = DbStorage()

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun findByUsername(username: String): User? {
        val result = db.findUser(username) ?: return null
        return User(result.first, result.second)
    }

    fun register(username: String, password: String): User {
        require(username.isNotBlank()) { "Username cannot be empty" }
        require(password.isNotBlank()) { "Password cannot be empty" }
        require(findByUsername(username) == null) { "User '$username' already exists" }
        val user = User(username, hashPassword(password))
        db.insertUser(user.username, user.passwordHash)
        return user
    }

    fun checkPassword(username: String, password: String): Boolean {
        val user = findByUsername(username) ?: return false
        return user.passwordHash == hashPassword(password)
    }
}