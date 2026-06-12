package lab.manager

import lab.domain.Experiment
import java.time.Instant

class ExperimentManager {
    private val experiments = mutableSetOf<Experiment>()
    private var idGenerator = 0L

    fun add(name: String, description: String?, ownerUsername: String): Experiment {
        idGenerator += 1
        val id = idGenerator
        val now = Instant.now()
        val experiment = Experiment(
            id = id,
            name = name,
            description = description,
            ownerUsername = ownerUsername,
            createdAt = now,
        )
        experiments.add(experiment)
        return experiment
    }

    fun getById(id: Long): Experiment? {
        return experiments.find { it.id == id }
    }

    fun list(): List<Experiment> = experiments.sortedBy { it.id }

    fun listByOwner(ownerUsername: String): List<Experiment> =
        experiments.filter { it.ownerUsername == ownerUsername }.sortedBy { it.id }

    fun update(id: Long, name: String? = null, description: String? = null, ownerUsername: String? = null): Experiment {
        val existing = getById(id) ?: throw IllegalArgumentException("Experiment with id=$id not found") /*
        val existing = getById(id)
if (existing == null) {
    throw IllegalArgumentException("Experiment with id=$id not found")
} */
        name?.let(fun(it: String) {
            existing.name = it
        })
        description?.let { existing.description = it }
        ownerUsername?.let { existing.ownerUsername = it }
        return existing
    }



    fun contains(id: Long): Boolean = getById(id) != null
    fun restore(experiment: Experiment) {
        experiments.add(experiment)
        if (experiment.id > idGenerator) idGenerator = experiment.id
    }

    fun listAll(): List<Experiment> = experiments.sortedBy { it.id }

    fun remove(id: Long): Boolean {
        val exp = getById(id) ?: return false
        return experiments.remove(exp)
    }
}