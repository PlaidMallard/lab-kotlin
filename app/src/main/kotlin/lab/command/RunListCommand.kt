package lab.command

import lab.service.LabService
import java.util.Scanner

class RunListCommand : CliCommand {
    override val name: String = "run_list"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) {
            println("Error: provide experiment ID. Example: run_list 123")
            return true
        }
        val experimentId = args[0].toLongOrNull()
        if (experimentId == null) {
            println("Error: ID must be a number")
            return true
        }

        var last: Int? = null
        var i = 1
        while (i < args.size) {
            when (args[i]) {
                "--last" -> {
                    if (i + 1 >= args.size) {
                        println("Error: --last expects a number")
                        return true
                    }
                    last = args[i + 1].toIntOrNull()
                    if (last == null) {
                        println("Error: --last must be a number")
                        return true
                    }
                    i += 2
                }
                else -> {
                    println("Uknown option: ${args[i]}")
                    return true
                }
            }
        }

        val runs = service.runList(experimentId, last)
        if (runs.isEmpty()) {
            println("Нет запусков для этого эксперимента.")
            return true
        }
        println("ID\tRun name\tOperator\tTime")
        runs.forEach {
            println("${it.id}\t${it.name}\t${it.operatorName}\t${it.createdAt}")
        }
        return true
    }
}