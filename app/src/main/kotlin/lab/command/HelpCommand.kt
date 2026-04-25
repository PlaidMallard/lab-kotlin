package lab.command

import lab.service.LabService
import java.util.Scanner

class HelpCommand : CliCommand {
    override val name: String = "help"

    override fun execute(args: List<String>, service: LabService, scanner: Scanner): Boolean {
        println(
            """
            Available commands:
              help                              - show this help
              exit                              - quit application

              exp_create                        - create experiment (interactive)
              exp_list [--mine]                - list experiments
              exp_show <id>                    - show experiment details
              exp_update <id>                  - update experiment (fields: name, description)
              run_add <experiment_id>          - add run (interactive)
              run_list <experiment_id> [--last N] - list experiment runs
              run_show <run_id>                - show run details
              res_add <run_id>                 - add result (interactive)
              res_list <run_id> [--param PARAM] - list run results
              exp_summary <experiment_id>      - summary statistics by experiment params
            """.trimIndent(),
        )
        return true
    }
}