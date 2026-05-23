package lab.domain

/**
 * Результат запуска (измерение).
 * @property id уникальный идентификатор
 * @property runId идентификатор запуска
 * @property param название параметра (не пустое)
 * @property value числовое значение
 * @property unit единица измерения (не пустая)
 * @property comment комментарий (может быть пустым)
 */
class RunResult(
    val id: Long,
    val runId: Long,
    param: String,
    var value: Double,
    unit: String,
    var comment: String?
) {
    var param: String = param
        set(value) {
            validateParam(value)
            field = value
        }

    var unit: String = unit
        set(value) {
            validateUnit(value)
            field = value
        }


    init {
        validateParam(param)
        validateUnit(unit)
    }

    companion object {
        fun validateParam(param: String) {
            require(param.isNotBlank()) { "param cant be empty" }
        }

        fun validateUnit(unit: String) {
            require(unit.isNotBlank()) { "unit cant be empty" }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RunResult
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}