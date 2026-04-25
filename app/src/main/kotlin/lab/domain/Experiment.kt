package lab.domain

import java.time.Instant

/**
 * Эксперимент.
 * @property id уникальный идентификатор (назначается программой, неизменяем)
 * @property name название эксперимента (не пустое, до 128 символов)
 * @property description описание (может быть пустым, до 512 символов)
 * @property ownerUsername логин создателя (не пустое)
 * @property createdAt время создания (устанавливается автоматически, неизменяемо)
 * @property updatedAt время последнего изменения (обновляется автоматически)
 */
class Experiment(
    val id: Long,
    name: String,
    description: String?,
    ownerUsername: String,
    createdAt: Instant
) {
    var name: String = name
        set(value) {
            validateName(value)
            field = value
            updateTimestamp()
        }

    var description: String? = description
        set(value) {
            validateDescription(value)
            field = value
            updateTimestamp()
        }

    var ownerUsername: String = ownerUsername
        set(value) {
            validateOwnerUsername(value)
            field = value
            updateTimestamp()
        }


    var updatedAt: Instant
        private set

    init {
        validateName(name)
        validateDescription(description)
        validateOwnerUsername(ownerUsername)
        updatedAt = createdAt
    }

    private fun updateTimestamp() {
        updatedAt = Instant.now()
    }

    companion object {
        private const val MAX_NAME_LENGTH = 128
        private const val MAX_DESC_LENGTH = 512

        fun validateName(name: String) {
            require(name.isNotBlank()) { "name не может быть пустым" }
            require(name.length <= MAX_NAME_LENGTH) { "name не может быть длиннее $MAX_NAME_LENGTH символов" }
        }

        fun validateDescription(description: String?) {
            if (description != null && description.length > MAX_DESC_LENGTH) {
                throw IllegalArgumentException("description не может быть длиннее $MAX_DESC_LENGTH символов")
            }
        }

        fun validateOwnerUsername(ownerUsername: String) {
            require(ownerUsername.isNotBlank()) { "ownerUsername не может быть пустым" }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Experiment
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}