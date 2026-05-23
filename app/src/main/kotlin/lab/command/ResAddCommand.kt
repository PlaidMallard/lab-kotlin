package lab.command

import lab.service.LabService
import java.util.Scanner

class ResAddCommand : CliCommand {
    override val name: String = "res_add"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) {
            println("Error: provide run ID. Example: res_add 3")
            return true
        }
        val runId = args[0].toLongOrNull()
        if (runId == null) {
            println("Error: run_id must be a number")
            return true
        }

        println("Add a result to run $runId")
        print("Parameter: ")
        val param = scanner.nextLine().trim()
        print("Value: ")
        val valueStr = scanner.nextLine().trim()
        val value = valueStr.toDoubleOrNull()
        if (value == null) {
            println("Error: value must be a number")
            return true
        }
        print("Unit: ")
        val unit = scanner.nextLine().trim()
        print("Comment (optional): ")
        val comment = scanner.nextLine().trim().takeIf { it.isNotBlank() }

        try {
            val result = service.resAdd(runId, param, value, unit, comment)
            println("OK result_id=${result.id}")
        } catch (e: IllegalArgumentException) {
            println("Error: ${e.message}")
        }
        return true
    }
}