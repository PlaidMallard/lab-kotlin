package lab.command

import lab.auth.AuthService
import lab.service.LabService
import java.util.Scanner

class LogoutCommand(private val authService: AuthService) : CliCommand {
    override val name = "logout"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        authService.logout()
        println("OK вы вышли из системы")
        return true
    }
}