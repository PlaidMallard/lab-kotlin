package lab.manager

import lab.domain.Run
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class RunManager(private val experimentManager: ExperimentManager) {
    private val runs = mutableSetOf<Run>()
    private val idGenerator = AtomicLong(0)

    fun add(experimentId: Long, name: String, operatorName: String): Run {
        require(experimentManager.contains(experimentId)) { "Experiment с id=$experimentId не найден" }
        val id = idGenerator.incrementAndGet()
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

    fun removeByExperiment(experimentId: Long) {
        runs.removeAll { it.experimentId == experimentId }
    }

    fun countByExperiment(experimentId: Long): Int = runs.count { it.experimentId == experimentId }
}