package lab.storage

object FileValidator {

    fun validate(data: LabDataDto): List<String> {
        val errors = mutableListOf<String>()

        checkDuplicates(data.experiments.map { it.id }, "experiment", errors)
        checkDuplicates(data.runs.map { it.id }, "run", errors)
        checkDuplicates(data.results.map { it.id }, "result", errors)

        val expIds = data.experiments.map { it.id }.toSet()
        val runIds = data.runs.map { it.id }.toSet()

        for (e in data.experiments) {
            if (e.name.isBlank()) errors += "experiment id=${e.id}: name пустое"
            if (e.ownerUsername.isBlank()) errors += "experiment id=${e.id}: ownerUsername пустой"
        }

        for (r in data.runs) {
            if (r.name.isBlank()) errors += "run id=${r.id}: name пустое"
            if (r.operatorName.isBlank()) errors += "run id=${r.id}: operatorName пустой"
            if (r.experimentId !in expIds) errors += "run id=${r.id}: experiment ${r.experimentId} не найден"
        }

        for (r in data.results) {
            if (r.param.isBlank()) errors += "result id=${r.id}: param пустой"
            if (r.unit.isBlank()) errors += "result id=${r.id}: unit пустой"
            if (r.value.isNaN() || r.value.isInfinite()) errors += "result id=${r.id}: value не число"
            if (r.runId !in runIds) errors += "result id=${r.id}: run ${r.runId} не найден"
        }

        return errors
    }

    private fun checkDuplicates(ids: List<Long>, name: String, errors: MutableList<String>) {
        val seen = mutableSetOf<Long>()
        for (id in ids) {
            if (!seen.add(id)) errors += "$name: повторяющийся id=$id"
        }
    }
}