package lab.db

import lab.domain.Experiment
import lab.domain.Run
import lab.domain.RunResult
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant

class DbStorage {

    private fun connect(): Connection {
        val props = java.util.Properties()
        props.setProperty("user", DbConfig.user)
        props.setProperty("password", DbConfig.password)
        props.setProperty("driver", "org.postgresql.Driver")

        return try {
            org.postgresql.Driver().connect(DbConfig.url, props)
                ?: throw IllegalStateException("Драйвер вернул null")
        } catch (e: Exception) {
            throw IllegalStateException("Не удалось подключиться к БД: ${e.message}")
        }
    }

    // ── Создание таблиц ──────────────────────────────────────────────────────

    fun initSchema() {
        connect().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        username VARCHAR(64) PRIMARY KEY,
                        password_hash VARCHAR(128) NOT NULL
                    )
                """)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS experiments (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(128) NOT NULL,
                        description VARCHAR(512),
                        owner_username VARCHAR(64) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL
                    )
                """)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS runs (
                        id BIGSERIAL PRIMARY KEY,
                        experiment_id BIGINT NOT NULL REFERENCES experiments(id),
                        name VARCHAR(128) NOT NULL,
                        operator_name VARCHAR(64) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL
                    )
                """)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS results (
                        id BIGSERIAL PRIMARY KEY,
                        run_id BIGINT NOT NULL REFERENCES runs(id),
                        param VARCHAR(64) NOT NULL,
                        value DOUBLE PRECISION NOT NULL,
                        unit VARCHAR(16) NOT NULL,
                        comment VARCHAR(128)
                    )
                """)
            }
        }
    }

    // ── Пользователи ─────────────────────────────────────────────────────────

    fun insertUser(username: String, passwordHash: String) {
        connect().use { conn ->
            conn.prepareStatement("INSERT INTO users(username, password_hash) VALUES(?, ?)").use { stmt ->
                stmt.setString(1, username)
                stmt.setString(2, passwordHash)
                stmt.executeUpdate()
            }
        }
    }

    fun findUser(username: String): Pair<String, String>? {
        connect().use { conn ->
            conn.prepareStatement("SELECT username, password_hash FROM users WHERE username = ?").use { stmt ->
                stmt.setString(1, username)
                val rs = stmt.executeQuery()
                if (rs.next()) return rs.getString("username") to rs.getString("password_hash")
            }
        }
        return null
    }

    // ── Эксперименты ─────────────────────────────────────────────────────────

    fun insertExperiment(name: String, description: String?, ownerUsername: String, createdAt: Instant): Long {
        connect().use { conn ->
            conn.prepareStatement(
                "INSERT INTO experiments(name, description, owner_username, created_at, updated_at) VALUES(?,?,?,?,?) RETURNING id"
            ).use { stmt ->
                stmt.setString(1, name)
                stmt.setString(2, description)
                stmt.setString(3, ownerUsername)
                stmt.setObject(4, createdAt.toString())
                stmt.setObject(5, createdAt.toString())
                val rs = stmt.executeQuery()
                rs.next()
                return rs.getLong("id")
            }
        }
    }

    fun updateExperiment(id: Long, name: String?, description: String?, updatedAt: Instant) {
        connect().use { conn ->
            if (name != null) {
                conn.prepareStatement("UPDATE experiments SET name=?, updated_at=? WHERE id=?").use { stmt ->
                    stmt.setString(1, name)
                    stmt.setObject(2, updatedAt.toString())
                    stmt.setLong(3, id)
                    stmt.executeUpdate()
                }
            }
            if (description != null) {
                conn.prepareStatement("UPDATE experiments SET description=?, updated_at=? WHERE id=?").use { stmt ->
                    stmt.setString(1, description)
                    stmt.setObject(2, updatedAt.toString())
                    stmt.setLong(3, id)
                    stmt.executeUpdate()
                }
            }
        }
    }

    fun deleteExperiment(id: Long) {
        connect().use { conn ->
            conn.prepareStatement("DELETE FROM results WHERE run_id IN (SELECT id FROM runs WHERE experiment_id=?)").use {
                it.setLong(1, id); it.executeUpdate()
            }
            conn.prepareStatement("DELETE FROM runs WHERE experiment_id=?").use {
                it.setLong(1, id); it.executeUpdate()
            }
            conn.prepareStatement("DELETE FROM experiments WHERE id=?").use {
                it.setLong(1, id); it.executeUpdate()
            }
        }
    }

    fun findAllExperiments(): List<Experiment> {
        val list = mutableListOf<Experiment>()
        connect().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM experiments ORDER BY id")
                while (rs.next()) {
                    list.add(Experiment(
                        id = rs.getLong("id"),
                        name = rs.getString("name"),
                        description = rs.getString("description"),
                        ownerUsername = rs.getString("owner_username"),
                        createdAt = Instant.parse(rs.getString("created_at"))
                    ))
                }
            }
        }
        return list
    }

    // ── Запуски ──────────────────────────────────────────────────────────────

    fun insertRun(experimentId: Long, name: String, operatorName: String, createdAt: Instant): Long {
        connect().use { conn ->
            conn.prepareStatement(
                "INSERT INTO runs(experiment_id, name, operator_name, created_at) VALUES(?,?,?,?) RETURNING id"
            ).use { stmt ->
                stmt.setLong(1, experimentId)
                stmt.setString(2, name)
                stmt.setString(3, operatorName)
                stmt.setObject(4, createdAt.toString())
                val rs = stmt.executeQuery()
                rs.next()
                return rs.getLong("id")
            }
        }
    }

    fun findRunsByExperiment(experimentId: Long): List<Run> {
        val list = mutableListOf<Run>()
        connect().use { conn ->
            conn.prepareStatement("SELECT * FROM runs WHERE experiment_id=? ORDER BY id").use { stmt ->
                stmt.setLong(1, experimentId)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    list.add(Run(
                        id = rs.getLong("id"),
                        experimentId = rs.getLong("experiment_id"),
                        name = rs.getString("name"),
                        operatorName = rs.getString("operator_name"),
                        createdAt = Instant.parse(rs.getString("created_at"))
                    ))
                }
            }
        }
        return list
    }

    fun findAllRuns(): List<Run> {
        val list = mutableListOf<Run>()
        connect().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM runs ORDER BY id")
                while (rs.next()) {
                    list.add(Run(
                        id = rs.getLong("id"),
                        experimentId = rs.getLong("experiment_id"),
                        name = rs.getString("name"),
                        operatorName = rs.getString("operator_name"),
                        createdAt = Instant.parse(rs.getString("created_at"))
                    ))
                }
            }
        }
        return list
    }

    // ── Результаты ───────────────────────────────────────────────────────────

    fun insertResult(runId: Long, param: String, value: Double, unit: String, comment: String?): Long {
        connect().use { conn ->
            conn.prepareStatement(
                "INSERT INTO results(run_id, param, value, unit, comment) VALUES(?,?,?,?,?) RETURNING id"
            ).use { stmt ->
                stmt.setLong(1, runId)
                stmt.setString(2, param)
                stmt.setDouble(3, value)
                stmt.setString(4, unit)
                stmt.setString(5, comment)
                val rs = stmt.executeQuery()
                rs.next()
                return rs.getLong("id")
            }
        }
    }

    fun findResultsByRun(runId: Long): List<RunResult> {
        val list = mutableListOf<RunResult>()
        connect().use { conn ->
            conn.prepareStatement("SELECT * FROM results WHERE run_id=? ORDER BY id").use { stmt ->
                stmt.setLong(1, runId)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    list.add(RunResult(
                        id = rs.getLong("id"),
                        runId = rs.getLong("run_id"),
                        param = rs.getString("param"),
                        value = rs.getDouble("value"),
                        unit = rs.getString("unit"),
                        comment = rs.getString("comment")
                    ))
                }
            }
        }
        return list
    }

    fun findAllResults(): List<RunResult> {
        val list = mutableListOf<RunResult>()
        connect().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM results ORDER BY id")
                while (rs.next()) {
                    list.add(RunResult(
                        id = rs.getLong("id"),
                        runId = rs.getLong("run_id"),
                        param = rs.getString("param"),
                        value = rs.getDouble("value"),
                        unit = rs.getString("unit"),
                        comment = rs.getString("comment")
                    ))
                }
            }
        }
        return list
    }
}