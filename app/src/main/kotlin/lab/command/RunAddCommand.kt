package lab.command

import lab.auth.AuthService
import lab.service.LabService
import java.util.Scanner

class RunAddCommand(private val authService: AuthService) : CliCommand {
    override val name: String = "run_add"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (!authService.isLoggedIn()) {
            println("Error: please login first (command: login)")
            return true
        }

        if (args.isEmpty()) {
            println("Error: provide experiment ID. Example: run_add 7")
            return true
        }
        val experimentId = args[0].toLongOrNull()
        if (experimentId == null) {
            println("Error: experiment_id must be a number")
            return true
        }

        println("Add a run to experiment $experimentId")
        print("Run name: ")
        val name = scanner.nextLine().trim()
        print("Operator: ")
        val operator = scanner.nextLine().trim()

        try {
            val run = service.runAdd(experimentId, name, operator)
            println("OK run_id=${run.id}")
        } catch (e: IllegalArgumentException) {
            println("error: ${e.message}")
        }
        return true
    }
}