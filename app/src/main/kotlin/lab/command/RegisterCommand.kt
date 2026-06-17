package lab.command

import lab.auth.AuthService
import lab.service.LabService
import java.util.Scanner

class RegisterCommand(private val authService: AuthService) : CliCommand {
    override val name = "register"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        print("Логин: ")
        val username = scanner.nextLine().trim()
        print("Пароль: ")
        val password = scanner.nextLine().trim()

        try {
            authService.register(username, password)
            println("OK пользователь '$username' зарегистрирован")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
        return true
    }
}