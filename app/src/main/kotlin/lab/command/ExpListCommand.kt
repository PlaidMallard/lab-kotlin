package lab.command

import lab.service.LabService
import java.util.Scanner

class ExpListCommand : CliCommand {
    override val name: String = "exp_list"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        val mine = args.contains("--mine")
        val experiments = service.expList(mine)
        if (experiments.isEmpty()) {
            println("No experiments")
            return true
        }
        println("ID\tName")
        experiments.forEach { println("${it.id}\t${it.name}") }
        return true
    }
}