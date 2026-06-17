package lab.cli

import lab.auth.AuthService
import lab.command.*
import lab.service.LabService
import java.util.Scanner

class Cli(private val service: LabService) {
    private val scanner = Scanner(System.`in`)
    private val authService = AuthService()
    private var running = true

    private val commands: List<CliCommand> = listOf(
        HelpCommand(),
        ExitCommand(),
        RegisterCommand(authService),
        LoginCommand(authService),
        LogoutCommand(authService),
        ExpCreateCommand(authService),
        RunAddCommand(authService),
        ResAddCommand(authService),
        ExpListCommand(),
        ExpShowCommand(),
        ExpUpdateCommand(),
        RunListCommand(),
        RunShowCommand(),
        ResListCommand(),
        ExpSummaryCommand(),
        SaveCommand(),
        LoadCommand(),
    )

    fun start() {
        println("Welcome to the experiment management system.")
        println("Type help for a list of commands.")

        while (running) {
            val user = authService.getCurrentUsername()
            print("[$user]> ")
            val line = scanner.nextLine().trim()
            if (line.isBlank()) continue

            try {
                processCommand(line)
            } catch (e: Exception) {
                println("Ошибка: ${e.message}")
            }
        }
    }

    private fun processCommand(line: String) {
        val parts = parseArguments(line)
        if (parts.isEmpty()) return

        val commandName = parts.first().lowercase()
        val args = parts.drop(1)

        val command = findCommand(commandName)
        if (command == null) {
            println("Unknown command. Write help")
            return
        }

        running = command.execute(args, service, scanner)
    }

    private fun findCommand(name: String): CliCommand? {
        return commands.find { it.name == name }
    }

    private fun parseArguments(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
        }

        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }
}