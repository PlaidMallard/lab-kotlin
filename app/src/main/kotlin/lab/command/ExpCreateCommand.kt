package lab.command

import lab.auth.AuthService
import lab.service.LabService
import java.util.Scanner

class ExpCreateCommand(private val authService: AuthService) : CliCommand {
    override val name = "exp_create"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (!authService.isLoggedIn()) {
            println("Ошибка: сначала войдите в систему (команда login)")
            return true
        }

        println("Create a new experiment")
        print("Name: ")
        val name = scanner.nextLine().trim()
        print("Description (optional): ")
        val description = scanner.nextLine().trim().takeIf { it.isNotBlank() }

        try {
            val exp = service.expCreate(name, description, authService.getCurrentUsername())
            println("OK experiment_id=${exp.id}")
        } catch (e: IllegalArgumentException) {
            println("Validation error: ${e.message}")
        }
        return true
    }
}