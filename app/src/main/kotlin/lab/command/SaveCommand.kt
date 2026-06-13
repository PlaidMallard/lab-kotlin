package lab.command

import lab.service.LabService
import lab.storage.StorageException
import java.util.Scanner

class SaveCommand : CliCommand {
    override val name = "save"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        if (args.isEmpty()) { println("Использование: save <файл>"); return true }
        try {
            service.save(args[0])
            println("OK сохранено в ${args[0]}")
        } catch (e: StorageException) {
            println("Ошибка: ${e.message}")
        }
        return true

    }
}