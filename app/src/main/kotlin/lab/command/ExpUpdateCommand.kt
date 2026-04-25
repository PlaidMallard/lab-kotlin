package lab.command

import lab.service.LabService
import java.util.Scanner

class ExpUpdateCommand : CliCommand {
    override val name: String = "exp_update"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) {
            println("Error: provide experiment ID and fields to update. Example: exp_update 2 name=\"New name\"")
            return true
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Error: ID must be a number")
            return true
        }

        val updates = mutableMapOf<String, String>()
        for (arg in args.drop(1)) {
            val eqIndex = arg.indexOf('=')
            if (eqIndex == -1) {
                println("Warning: skipped argument '$arg' (must be in key=value format)")
                continue
            }
            val key = arg.take(eqIndex).lowercase()
            var value = arg.substring(eqIndex + 1)
            if (value.startsWith("\"") && value.endsWith("\"") && value.length >= 2) {
                value = value.removeSurrounding("\"")
            }
            updates[key] = value
        }

        try {
            val exp = service.expUpdate(
                id = id,
                name = updates["name"],
                description = updates["description"],
            )
            println("OK experiment #${exp.id} updated")
        } catch (e: IllegalArgumentException) {
            println("Error: ${e.message}")
        }
        return true
    }
}