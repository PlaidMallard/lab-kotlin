package lab.manager

import lab.domain.Experiment
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class ExperimentManager {
    private val experiments = mutableSetOf<Experiment>()
    private val idGenerator = AtomicLong(0)

    fun add(name: String, description: String?, ownerUsername: String): Experiment {
        val id = idGenerator.incrementAndGet()
        val now = Instant.now()
        val experiment = Experiment(
            id = id,
            name = name,
            description = description,
            ownerUsername = ownerUsername,
            createdAt = now,
            updatedAt = now
        )
        experiments.add(experiment)
        return experiment
    }

    fun getById(id: Long): Experiment? = experiments.find { it.id == id }

    fun list(): List<Experiment> = experiments.sortedBy { it.id }

    fun listByOwner(ownerUsername: String): List<Experiment> =
        experiments.filter { it.ownerUsername == ownerUsername }.sortedBy { it.id }

    fun update(id: Long, name: String? = null, description: String? = null, ownerUsername: String? = null): Experiment {
        val existing = getById(id) ?: throw IllegalArgumentException("Experiment с id=$id не найден")
        name?.let { existing.name = it }
        description?.let { existing.description = it }
        ownerUsername?.let { existing.ownerUsername = it }
        return existing
    }

    fun remove(id: Long): Boolean {
        val experiment = getById(id) ?: return false
        return experiments.remove(experiment)
    }

    fun contains(id: Long): Boolean = getById(id) != null
}