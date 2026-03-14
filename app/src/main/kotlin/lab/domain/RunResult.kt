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
    value: Double,
    unit: String,
    comment: String?
) {
    var param: String = param
        set(value) {
            validateParam(value)
            field = value
        }
    var value: Double = value

    var unit: String = unit
        set(value) {
            validateUnit(value)
            field = value
        }
    var comment: String? = comment


    init {
        validateParam(param)
        validateUnit(unit)
    }

    companion object {
        fun validateParam(param: String) {
            require(param.isNotBlank()) { "param не может быть пустым" }
        }

        fun validateUnit(unit: String) {
            require(unit.isNotBlank()) { "unit не может быть пустым" }
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