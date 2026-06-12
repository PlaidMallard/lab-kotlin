package lab.command

import lab.service.LabService
import lab.storage.StorageException
import java.util.Scanner

class LoadCommand : CliCommand {
    override val name = "load"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) { println("Использование: load <файл>"); return true }
        try {
            service.load(args[0])
            println("OK загружено из ${args[0]}")
        } catch (e: StorageException) {
            println("Ошибка: ${e.message}")
        }
        return true
    }
}