
package lab

import lab.service.LabService
import lab.cli.Cli

fun main() {
    val service = LabService()
    val cli = Cli(service)
    cli.start()
}