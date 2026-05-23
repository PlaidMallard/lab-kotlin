package lab.command

import lab.service.LabService
import java.util.Scanner

class ExitCommand : CliCommand {
    override val name: String = "exit"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        println("Exit")
        return false
    }
}