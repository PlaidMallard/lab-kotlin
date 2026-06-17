package lab.service

import lab.db.DbStorage
import lab.domain.Experiment
import lab.domain.Run
import lab.domain.RunResult
import lab.manager.ExperimentManager
import lab.manager.RunManager
import lab.manager.RunResultManager
import lab.storage.FileStorage
import java.time.Instant

class LabService {
    private var experimentManager = ExperimentManager()
    private var runManager = RunManager(experimentManager)
    private var runResultManager = RunResultManager(runManager)
    private val fileStorage = FileStorage()
    private val db = DbStorage()
    private var useDb = false

    // Попробовать подключиться к БД при старте
    fun tryInitDb(): Boolean {
        return try {
            db.initSchema()
            loadFromDb()
            useDb = true
            true
        } catch (e: Exception) {
            println("Ошибка БД: ${e.message}")  // добавь эту строку
            e.printStackTrace()                  // и эту
            false
        }
    }

    private fun loadFromDb() {
        val newExp = ExperimentManager()
        val newRun = RunManager(newExp)
        val newRes = RunResultManager(newRun)

        db.findAllExperiments().forEach { newExp.restore(it) }
        db.findAllRuns().forEach { newRun.restore(it) }
        db.findAllResults().forEach { newRes.restore(it) }

        experimentManager = newExp
        runManager = newRun
        runResultManager = newRes
    }

    fun expCreate(name: String, description: String?, ownerUsername: String = "SYSTEM"): Experiment {
        val exp = experimentManager.add(name, description, ownerUsername)
        if (useDb) db.insertExperiment(name, description, ownerUsername, exp.createdAt)
        return exp
    }

    fun expList(mine: Boolean = false, currentUser: String = "SYSTEM"): List<Experiment> =
        if (mine) experimentManager.listByOwner(currentUser) else experimentManager.list()

    fun expShow(id: Long): Pair<Experiment, Int> {
        val exp = experimentManager.getById(id) ?: throw IllegalArgumentException("Experiment not found")
        return exp to runManager.countByExperiment(id)
    }

    fun expUpdate(id: Long, name: String? = null, description: String? = null, ownerUsername: String? = null): Experiment {
        val exp = experimentManager.update(id, name, description, ownerUsername)
        if (useDb) db.updateExperiment(id, name, description, Instant.now())
        return exp
    }

    fun expDelete(id: Long) {
        runResultManager.removeByExperiment(id)
        runManager.removeByExperiment(id)
        experimentManager.remove(id)
        if (useDb) db.deleteExperiment(id)
    }

    fun runAdd(experimentId: Long, name: String, operatorName: String): Run {
        val run = runManager.add(experimentId, name, operatorName)
        if (useDb) db.insertRun(experimentId, name, operatorName, run.createdAt)
        return run
    }

    fun runList(experimentId: Long, last: Int? = null): List<Run> =
        if (last != null) runManager.listLastByExperiment(experimentId, last)
        else runManager.listByExperiment(experimentId)

    fun runShow(id: Long): Pair<Run, Int> {
        val run = runManager.getById(id) ?: throw IllegalArgumentException("Run not found")
        return run to runResultManager.listByRun(id).size
    }

    fun resAdd(runId: Long, param: String, value: Double, unit: String, comment: String?): RunResult {
        val res = runResultManager.add(runId, param, value, unit, comment)
        if (useDb) db.insertResult(runId, param, value, unit, comment)
        return res
    }

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

    fun isUsingDb(): Boolean = useDb
}