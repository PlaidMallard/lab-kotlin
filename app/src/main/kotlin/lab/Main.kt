package lab

import javafx.application.Application
import lab.cli.Cli
import lab.service.LabService
import lab.ui.LabApp

fun main(args: Array<String>) {
    if (args.contains("--ui")) {
        Application.launch(LabApp::class.java, *args)
        return
    }
    val service = LabService()
    val cli = Cli(service)
    cli.start()
}