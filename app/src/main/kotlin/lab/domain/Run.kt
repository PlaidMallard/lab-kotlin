package lab.domain

import java.time.Instant

/**
 * Запуск эксперимента.
 * @property id уникальный идентификатор
 * @property experimentId идентификатор эксперимента (должен существовать)
 * @property name название запуска (не пустое, до 128 символов)
 * @property operatorName оператор (не пустое, до 64 символов)
 * @property createdAt время создания (неизменяемо)
 */
class Run(
    val id: Long,
    val experimentId: Long,
    name: String,
    operatorName: String,
    val createdAt: Instant
) {
    var name: String = name
        set(value) {
            validateName(value)
            field = value
        }

    var operatorName: String = operatorName
        set(value) {
            validateOperatorName(value)
            field = value
        }

    init {
        validateName(name)
        validateOperatorName(operatorName)
    }

    companion object {
        private const val MAX_NAME_LENGTH = 128
        private const val MAX_OPERATOR_LENGTH = 64

        fun validateName(name: String) {
            require(name.isNotBlank()) { "name не может быть пустым" }
            require(name.length <= MAX_NAME_LENGTH) { "name не может быть длиннее $MAX_NAME_LENGTH символов" }
        }

        fun validateOperatorName(operatorName: String) {
            require(operatorName.isNotBlank()) { "operatorName не может быть пустым" }
            require(operatorName.length <= MAX_OPERATOR_LENGTH) { "operatorName не может быть длиннее $MAX_OPERATOR_LENGTH символов" }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Run
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}