package lab.command

import lab.auth.AuthService
import lab.service.LabService
import java.util.Scanner

class LoginCommand(private val authService: AuthService) : CliCommand {
    override val name = "login"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        print("Логин: ")
        val username = scanner.nextLine().trim()
        print("Пароль: ")
        val password = scanner.nextLine().trim()

        try {
            val user = authService.login(username, password)
            println("OK добро пожаловать, ${user.username}!")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
        return true
    }
}