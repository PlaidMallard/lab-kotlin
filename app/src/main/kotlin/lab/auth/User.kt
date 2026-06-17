package lab.auth

data class User(
    val username: String,
    val passwordHash: String
)