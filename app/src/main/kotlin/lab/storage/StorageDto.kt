package lab.storage

data class ExperimentDto(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerUsername: String,
    val createdAt: String,
    val updatedAt: String
)

data class RunDto(
    val id: Long,
    val experimentId: Long,
    val name: String,
    val operatorName: String,
    val createdAt: String
)

data class RunResultDto(
    val id: Long,
    val runId: Long,
    val param: String,
    val value: Double,
    val unit: String,
    val comment: String?
)

data class LabDataDto(
    val experiments: List<ExperimentDto> = emptyList(),
    val runs: List<RunDto> = emptyList(),
    val results: List<RunResultDto> = emptyList()
)