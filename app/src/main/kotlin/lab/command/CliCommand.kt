
package lab.command

import lab.service.LabService
import java.util.Scanner

interface CliCommand {
    val name: String
    fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean
}