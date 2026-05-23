package lab.storage

import com.google.gson.GsonBuilder
import lab.domain.Experiment
import lab.domain.Run
import lab.domain.RunResult
import java.io.File
import java.time.Instant

class FileStorage {

    private val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()

    fun save(path: String, experiments: List<Experiment>, runs: List<Run>, results: List<RunResult>) {
        val dto = LabDataDto(
            experiments = experiments.map { ExperimentDto(it.id, it.name, it.description, it.ownerUsername, it.createdAt.toString(), it.updatedAt.toString()) },
            runs = runs.map { RunDto(it.id, it.experimentId, it.name, it.operatorName, it.createdAt.toString()) },
            results = results.map { RunResultDto(it.id, it.runId, it.param, it.value, it.unit, it.comment) }
        )
        File(path).apply { parentFile?.mkdirs(); writeText(gson.toJson(dto), Charsets.UTF_8) }
    }

    fun load(path: String): LabDataDto {
        val file = File(path)
        if (!file.exists()) throw StorageException("Файл не найден: $path")
        if (!file.canRead()) throw StorageException("Нет доступа к файлу: $path")

        val dto = try {
            gson.fromJson(file.readText(Charsets.UTF_8), LabDataDto::class.java)
                ?: throw StorageException("Файл пустой")
        } catch (e: com.google.gson.JsonSyntaxException) {
            throw StorageException("Некорректный JSON: ${e.message}")
        }

        val errors = FileValidator.validate(dto)
        if (errors.isNotEmpty()) throw StorageException("Ошибки:\n" + errors.joinToString("\n") { "  - $it" })

        return dto
    }

    fun toExperiment(d: ExperimentDto) = Experiment(d.id, d.name, d.description, d.ownerUsername, Instant.parse(d.createdAt))
    fun toRun(d: RunDto) = Run(d.id, d.experimentId, d.name, d.operatorName, Instant.parse(d.createdAt))
    fun toRunResult(d: RunResultDto) = RunResult(d.id, d.runId, d.param, d.value, d.unit, d.comment)
}

class StorageException(message: String) : Exception(message)