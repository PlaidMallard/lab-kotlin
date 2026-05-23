package lab.service

import lab.domain.Experiment
import lab.domain.Run
import lab.domain.RunResult
import lab.manager.ExperimentManager
import lab.manager.RunManager
import lab.manager.RunResultManager
import lab.storage.FileStorage

class LabService {
    private var experimentManager = ExperimentManager()
    private var runManager = RunManager(experimentManager)
    private var runResultManager = RunResultManager(runManager)
    private val fileStorage = FileStorage()

    fun expCreate(name: String, description: String?, ownerUsername: String = "SYSTEM"): Experiment =
        experimentManager.add(name, description, ownerUsername)

    fun expList(mine: Boolean = false, currentUser: String = "SYSTEM"): List<Experiment> =
        if (mine) experimentManager.listByOwner(currentUser) else experimentManager.list()

    fun expShow(id: Long): Pair<Experiment, Int> {
        val exp = experimentManager.getById(id) ?: throw IllegalArgumentException("Experiment not found")
        return exp to runManager.countByExperiment(id)
    }

    fun expUpdate(id: Long, name: String? = null, description: String? = null, ownerUsername: String? = null): Experiment =
        experimentManager.update(id, name, description, ownerUsername)

    fun expDeleteCascade(id: Long): Boolean {
        if (!experimentManager.contains(id)) return false
        runResultManager.removeByExperiment(id)
        runManager.removeByExperiment(id)
        return experimentManager.remove(id)
    }

    fun runAdd(experimentId: Long, name: String, operatorName: String): Run =
        runManager.add(experimentId, name, operatorName)

    fun runList(experimentId: Long, last: Int? = null): List<Run> =
        if (last != null) runManager.listLastByExperiment(experimentId, last)
        else runManager.listByExperiment(experimentId)

    fun runShow(id: Long): Pair<Run, Int> {
        val run = runManager.getById(id) ?: throw IllegalArgumentException("Run not found")
        return run to runResultManager.listByRun(id).size
    }

    fun resAdd(runId: Long, param: String, value: Double, unit: String, comment: String?): RunResult =
        runResultManager.add(runId, param, value, unit, comment)

    fun resList(runId: Long, param: String? = null): List<RunResult> =
        if (param != null) runResultManager.listByRunAndParam(runId, param)
        else runResultManager.listByRun(runId)

    data class ParamStats(val min: Double, val max: Double, val avg: Double, val count: Int)

    fun expSummary(experimentId: Long): Map<String, ParamStats> {
        val results = runResultManager.getAllResultsForExperiment(experimentId)
        return results.groupBy { it.param }.mapValues { (_, list) ->
            val values = list.map { it.value }
            ParamStats(values.minOrNull()!!, values.maxOrNull()!!, values.average(), values.size)
        }
    }

    fun save(path: String) {
        fileStorage.save(path, experimentManager.listAll(), runManager.listAll(), runResultManager.listAll())
    }

    fun load(path: String) {
        val dto = fileStorage.load(path)
        val newExp = ExperimentManager()
        val newRun = RunManager(newExp)
        val newRes = RunResultManager(newRun)
        dto.experiments.forEach { newExp.restore(fileStorage.toExperiment(it)) }
        dto.runs.forEach { newRun.restore(fileStorage.toRun(it)) }
        dto.results.forEach { newRes.restore(fileStorage.toRunResult(it)) }
        experimentManager = newExp
        runManager = newRun
        runResultManager = newRes
    }
}