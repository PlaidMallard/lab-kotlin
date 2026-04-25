package lab.command


import lab.service.LabService
import java.util.Scanner

class RunShowCommand : CliCommand {
    override val name: String = "run_show"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) {
            println("Error: provide run ID. Example: run_show 98")
            return true
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Error: ID must be a number")
            return true
        }
        try {
            val (run, resultsCount) = service.runShow(id)
            println("Run #${run.id}")
            println("experiment_id: ${run.experimentId}")
            println("name: ${run.name}")
            println("results: $resultsCount")
        } catch (e: IllegalArgumentException) {
            println("error: ${e.message}")
        }
        return true
    }
}