package lab

import javafx.application.Application
import lab.cli.Cli
import lab.service.LabService
import lab.ui.LabApp

fun main(args: Array<String>) {
    val service = LabService()

    // Пробуем подключиться к БД
    if (service.tryInitDb()) {
        println("Connected to DB")
    } else {
        println("DB unreachable - works with files")
    }

    if (args.contains("--ui")) {
        Application.launch(LabApp::class.java, *args)
        return
    }

    val cli = Cli(service)
    cli.start()
}