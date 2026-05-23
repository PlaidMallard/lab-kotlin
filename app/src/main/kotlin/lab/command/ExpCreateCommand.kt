package lab.command

import lab.service.LabService
import java.util.*

class ExpCreateCommand : CliCommand {
    override val name: String = "exp_create"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        println("Create a new experiment")
        print("Name: ")
        val name = scanner.nextLine().trim()
        print("Description (optional): ")
        val description = scanner.nextLine().trim().takeIf { it.isNotBlank() }

        try {
            val exp = service.expCreate(name, description)
            println("OK experiment_id=${exp.id}")
        } catch (e: IllegalArgumentException) {
            println("Validation error: ${e.message}")
        }
        return true
    }
}