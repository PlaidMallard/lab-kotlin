package lab.cli

import lab.command.CliCommand
import lab.command.ExitCommand
import lab.command.ExpCreateCommand
import lab.command.ExpListCommand
import lab.command.ExpShowCommand
import lab.command.ExpSummaryCommand
import lab.command.ExpUpdateCommand
import lab.command.HelpCommand
import lab.command.ResAddCommand
import lab.command.ResListCommand
import lab.command.RunAddCommand
import lab.command.RunListCommand
import lab.command.RunShowCommand
import lab.service.LabService
import java.util.Scanner

class Cli(private val service: LabService) {
    private val scanner = Scanner(System.`in`)
    private var running = true

    private val commands: List<CliCommand> = listOf(
        HelpCommand(),
        ExitCommand(),
        ExpCreateCommand(),
        RunAddCommand(),
        ResAddCommand(),
        ExpListCommand(),
        ExpShowCommand(),
        ExpUpdateCommand(),
        RunListCommand(),
        RunShowCommand(),
        ResListCommand(),
        ExpSummaryCommand(),
    )

    fun start() {
        println("Welcome to the experiment management system.")
        println("Type help for a list of commands.")


        while (running) {
            print("> ")
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
            println("Неизвестная команда. Введите help для списка.")
            return
        }

        running = command.execute(args, service, scanner)
    }

    private fun findCommand(name: String): CliCommand? {
        for (command in commands) {
            if (command.name == name) {
                return command
            }
        }
        return null
    }

    private fun parseArguments(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
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
            i++
        }

        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
    }
}