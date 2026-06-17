package lab.auth

class AuthService {

    private val userStorage = UserStorage()
    private var currentUser: User? = null

    fun register(username: String, password: String): User {
        return userStorage.register(username, password)
    }

    fun login(username: String, password: String): User {
        require(userStorage.findByUsername(username) != null) { "Пользователь '$username' не найден" }
        require(userStorage.checkPassword(username, password)) { "Неверный пароль" }
        val user = userStorage.findByUsername(username)!!
        currentUser = user
        return user
    }

    fun logout() {
        currentUser = null
    }

    fun getCurrentUser(): User? = currentUser

    fun getCurrentUsername(): String = currentUser?.username ?: "SYSTEM"

    fun isLoggedIn(): Boolean = currentUser != null
}