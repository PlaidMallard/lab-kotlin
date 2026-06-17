package lab.db

object DbConfig {
    val url: String
        get() = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/lab"
    val user: String
        get() = System.getenv("DB_USER") ?: "postgres"
    val password: String
        get() = System.getenv("DB_PASSWORD") ?: "postgres"
}