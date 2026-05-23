package lab.command

import lab.service.LabService
import java.util.Scanner

class ResListCommand : CliCommand {
    override val name: String = "res_list"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) {
            println("Error: provide run ID. Example: res_list 4")
            return true
        }
        val runId = args[0].toLongOrNull()
        if (runId == null) {
            println("Error: ID must be a number")
            return true
        }

        var param: String? = null
        var i = 1
        while (i < args.size) {
            when (args[i]) {
                "--param" -> {
                    if (i + 1 >= args.size) {
                        println("Error: --param expects a parameter name")
                        return true
                    }
                    param = args[i + 1]
                    i += 2
                }
                else -> {
                    println("Unknown option: ${args[i]}")
                    return true
                }
            }
        }

        val results = service.resList(runId, param)
        if (results.isEmpty()) {
            println("No result for this run.")
            return true
        }
        println("ID\tParam\tValue\tUnit\tComment")
        results.forEach {
            println("${it.id}\t${it.param}\t${it.value}\t${it.unit}\t${it.comment ?: ""}")
        }
        return true
    }
}