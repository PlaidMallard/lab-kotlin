package lab.command

import lab.service.LabService
import java.util.Scanner

class ExpShowCommand : CliCommand {
    override val name: String = "exp_show"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) {
            println("Error: provide experiment ID. Example: exp_show 2")
            return true
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Error: ID must be a number")
            return true
        }
        try {
            val (exp, runsCount) = service.expShow(id)
            println("Experiment #${exp.id}")
            println("name: ${exp.name}")
            println("description: ${exp.description ?: "(empty)"}")
            println("runs: $runsCount")
            println("updatedAt: ${exp.updatedAt}")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
        return true
    }
}