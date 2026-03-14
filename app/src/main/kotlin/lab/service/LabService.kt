package lab.service

import lab.domain.Experiment
import lab.domain.Run
import lab.domain.RunResult
import lab.manager.ExperimentManager
import lab.manager.RunManager
import lab.manager.RunResultManager

class LabService {
    private val experimentManager = ExperimentManager()
    private val runManager = RunManager(experimentManager)
    private val runResultManager = RunResultManager(runManager)

    // Experiment commands
    fun expCreate(name: String, description: String?, ownerUsername: String = "SYSTEM"): Experiment =
        experimentManager.add(name, description, ownerUsername)

    fun expList(mine: Boolean = false, currentUser: String = "SYSTEM"): List<Experiment> =
        if (mine) experimentManager.listByOwner(currentUser) else experimentManager.list()

    fun expShow(id: Long): Pair<Experiment, Int> {
        val exp = experimentManager.getById(id) ?: throw IllegalArgumentException("Experiment не найден")
        val runsCount = runManager.countByExperiment(id)
        return exp to runsCount
    }

    fun expUpdate(id: Long, name: String? = null, description: String? = null, ownerUsername: String? = null): Experiment =
        experimentManager.update(id, name, description, ownerUsername)

    // Run commands
    fun runAdd(experimentId: Long, name: String, operatorName: String): Run =
        runManager.add(experimentId, name, operatorName)

    fun runList(experimentId: Long, last: Int? = null): List<Run> =
        if (last != null) runManager.listLastByExperiment(experimentId, last)
        else runManager.listByExperiment(experimentId)

    fun runShow(id: Long): Pair<Run, Int> {
        val run = runManager.getById(id) ?: throw IllegalArgumentException("Run не найден")
        val resultsCount = runResultManager.listByRun(id).size
        return run to resultsCount
    }

    // Result commands
    fun resAdd(runId: Long, param: String, value: Double, unit: String, comment: String?): RunResult =
        runResultManager.add(runId, param, value, unit, comment)

    fun resList(runId: Long, param: String? = null): List<RunResult> =
        if (param != null) runResultManager.listByRunAndParam(runId, param)
        else runResultManager.listByRun(runId)

    // Summary command
    data class ParamStats(val min: Double, val max: Double, val avg: Double, val count: Int)

    fun expSummary(experimentId: Long): Map<String, ParamStats> {
        val results = runResultManager.getAllResultsForExperiment(experimentId)
        return results.groupBy { it.param }
            .mapValues { (_, list) ->
                val values = list.map { it.value }
                ParamStats(
                    min = values.minOrNull()!!,
                    max = values.maxOrNull()!!,
                    avg = values.average(),
                    count = values.size
                )
            }
    }

    fun expDeleteCascade(id: Long): Boolean {
        if (!experimentManager.contains(id)) return false
        runResultManager.removeByExperiment(id)
        runManager.removeByExperiment(id)
        return experimentManager.remove(id)
    }
}