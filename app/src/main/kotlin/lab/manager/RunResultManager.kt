package lab.manager

import lab.domain.RunResult
import java.util.concurrent.atomic.AtomicLong

class RunResultManager(private val runManager: RunManager) {
    private val results = mutableSetOf<RunResult>()
    private val idGenerator = AtomicLong(0)

    fun add(runId: Long, param: String, value: Double, unit: String, comment: String?): RunResult {
        require(runManager.getById(runId) != null) { "Run с id=$runId не найден" }
        val id = idGenerator.incrementAndGet()
        val result = RunResult(
            id = id,
            runId = runId,
            param = param,
            value = value,
            unit = unit,
            comment = comment
        )
        results.add(result)
        return result
    }

    fun getById(id: Long): RunResult? = results.find { it.id == id }

    fun listByRun(runId: Long): List<RunResult> =
        results.filter { it.runId == runId }.sortedBy { it.id }

    fun listByRunAndParam(runId: Long, param: String): List<RunResult> =
        results.filter { it.runId == runId && it.param.equals(param, ignoreCase = true) }
            .sortedBy { it.id }

    fun removeByExperiment(experimentId: Long) {
        val runIds = runManager.listByExperiment(experimentId).map { it.id }
        results.removeAll { it.runId in runIds }
    }

    fun getAllResultsForExperiment(experimentId: Long): List<RunResult> {
        val runIds = runManager.listByExperiment(experimentId).map { it.id }
        return results.filter { it.runId in runIds }
    }
}