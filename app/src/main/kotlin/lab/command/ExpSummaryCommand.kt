package lab.command

import lab.service.LabService
import java.util.Scanner

class ExpSummaryCommand : CliCommand {
    override val name: String = "exp_summary"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) {
            println("Error: provide experiment ID. Example: exp_summary 2")
            return true
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Error: ID must be a number")
            return true
        }
        try {
            val summary = service.expSummary(id)
            if (summary.isEmpty()) {
                println("No data for this experiment.")
                return true
            }
            for ((param, stats) in summary) {
                println("$param: count=${stats.count} min=${stats.min} max=${stats.max} avg=${stats.avg}")
            }
        } catch (e: IllegalArgumentException) {
            println("Error: ${e.message}")
        }
        return true
    }
}