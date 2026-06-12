package lab.manager

import lab.domain.RunResult

class RunResultManager(private val runManager: RunManager) {
    private val results = mutableSetOf<RunResult>()
    private var idGenerator = 0L

    fun add(runId: Long, param: String, value: Double, unit: String, comment: String?): RunResult {
        require(runManager.getById(runId) != null) { "Run with id=$runId not found" }
        idGenerator += 1
        val id = idGenerator
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


    fun listByRun(runId: Long): List<RunResult> =
        results.filter { it.runId == runId }.sortedBy { it.id }

    fun listByRunAndParam(runId: Long, param: String): List<RunResult> =
        results.filter { it.runId == runId && it.param.equals(param, ignoreCase = true) }
            .sortedBy { it.id }



    fun getAllResultsForExperiment(experimentId: Long): List<RunResult> {
        val runIds = runManager.listByExperiment(experimentId).map { it.id }
        return results.filter { it.runId in runIds }
    }
    fun restore(result: RunResult) {
        results.add(result)
        if (result.id > idGenerator) idGenerator = result.id
    }

    fun listAll(): List<RunResult> = results.sortedBy { it.id }

    fun removeByExperiment(experimentId: Long) {
        val runIds = runManager.listByExperiment(experimentId).map { it.id }
        results.removeAll { it.runId in runIds }
    }
}