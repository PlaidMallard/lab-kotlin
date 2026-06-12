package lab.manager

import lab.domain.Run
import java.time.Instant

class RunManager(private val experimentManager: ExperimentManager) {
    private val runs = mutableSetOf<Run>()
    private var idGenerator = 0L

    fun add(experimentId: Long, name: String, operatorName: String): Run {
        require(experimentManager.contains(experimentId)) { "Experiment with id=$experimentId not foun" }
        idGenerator += 1
        val id = idGenerator
        val run = Run(
            id = id,
            experimentId = experimentId,
            name = name,
            operatorName = operatorName,
            createdAt = Instant.now()
        )
        runs.add(run)
        return run
    }

    fun getById(id: Long): Run? = runs.find { it.id == id }

    fun listByExperiment(experimentId: Long): List<Run> =
        runs.filter { it.experimentId == experimentId }.sortedBy { it.id }

    fun listLastByExperiment(experimentId: Long, lastN: Int): List<Run> =
        runs.filter { it.experimentId == experimentId }
            .sortedByDescending { it.createdAt }
            .take(lastN)

    fun countByExperiment(experimentId: Long): Int = runs.count { it.experimentId == experimentId }
    fun restore(run: Run) {
        runs.add(run)
        if (run.id > idGenerator) idGenerator = run.id
    }

    fun listAll(): List<Run> = runs.sortedBy { it.id }

    fun removeByExperiment(experimentId: Long) {
        runs.removeAll { it.experimentId == experimentId }
    }
}