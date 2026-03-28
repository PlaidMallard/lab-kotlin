
package lab.cli

import lab.service.LabService
import java.util.*


class Cli(private val service: LabService) {
    private val scanner = Scanner(System.`in`)
    private var running = true

    fun start() {
        println("Добро пожаловать в систему управления экспериментами.")
        println("Введите help для списка команд.")

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

        val command = parts.first().lowercase()
        val args = parts.drop(1)

        when (command) {
            "help" -> showHelp()
            "exit" -> {
                running = false
                println("Выход")
            }


            "exp_create" -> interactiveExpCreate()
            "run_add" -> interactiveRunAdd(args)
            "res_add" -> interactiveResAdd(args)


            "exp_list" -> cmdExpList(args)
            "exp_show" -> cmdExpShow(args)
            "exp_update" -> cmdExpUpdate(args)
            "run_list" -> cmdRunList(args)
            "run_show" -> cmdRunShow(args)
            "res_list" -> cmdResList(args)
            "exp_summary" -> cmdExpSummary(args)

            else -> println("Неизвестная команда. Введите help для списка.")
        }
    }


    private fun interactiveExpCreate() {
        println("Создание нового эксперимента")
        print("Название: ")
        val name = scanner.nextLine().trim()
        print("Описание (можно оставить пустым): ")
        val description = scanner.nextLine().trim().takeIf { it.isNotBlank() }

        try {
            val exp = service.expCreate(name, description)
            println("OK experiment_id=${exp.id}")
        } catch (e: IllegalArgumentException) {
            println("Ошибка валидации: ${e.message}")
        }
    }

    private fun interactiveRunAdd(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID эксперимента. Пример: run_add 2")
            return
        }
        val experimentId = args[0].toLongOrNull()
        if (experimentId == null) {
            println("Ошибка: experiment_id должен быть числом")
            return
        }

        println("Добавление запуска к эксперименту $experimentId")
        print("Run name: ")
        val name = scanner.nextLine().trim()
        print("Operator: ")
        val operator = scanner.nextLine().trim()

        try {
            val run = service.runAdd(experimentId, name, operator)
            println("OK run_id=${run.id}")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
    }

    private fun interactiveResAdd(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID запуска. Пример: res_add 11")
            return
        }
        val runId = args[0].toLongOrNull()
        if (runId == null) {
            println("Ошибка: run_id должен быть числом")
            return
        }

        println("Добавление результата к запуску $runId")
        print("Параметр: ")
        val param = scanner.nextLine().trim()
        print("Значение: ")
        val valueStr = scanner.nextLine().trim()
        val value = valueStr.toDoubleOrNull()
        if (value == null) {
            println("Ошибка: значение должно быть числом")
            return
        }
        print("Единицы: ")
        val unit = scanner.nextLine().trim()
        print("Комментарий (можно оставить пустым): ")
        val comment = scanner.nextLine().trim().takeIf { it.isNotBlank() }

        try {
            val result = service.resAdd(runId, param, value, unit, comment)
            println("OK result_id=${result.id}")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
    }


    private fun cmdExpList(args: List<String>) {
        val mine = args.contains("--mine")
        val experiments = service.expList(mine)
        if (experiments.isEmpty()) {
            println("Нет экспериментов.")
            return
        }
        println("ID\tName")
        experiments.forEach { println("${it.id}\t${it.name}") }
    }

    private fun cmdExpShow(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID эксперимента. Пример: exp_show 2")
            return
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Ошибка: ID должен быть числом")
            return
        }
        try {
            val (exp, runsCount) = service.expShow(id)
            println("Experiment #${exp.id}")
            println("name: ${exp.name}")
            println("description: ${exp.description ?: "(пусто)"}")
            println("runs: $runsCount")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
    }

    private fun cmdExpUpdate(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID эксперимента и поля для обновления. Пример: exp_update 2 name=\"New name\"")
            return
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Ошибка: ID должен быть числом")
            return
        }

        val updates = mutableMapOf<String, String>()
        for (arg in args.drop(1)) {
            val eqIndex = arg.indexOf('=')
            if (eqIndex == -1) {
                println("Предупреждение: пропущен аргумент '$arg' (должен быть в формате ключ=значение)")
                continue
            }
            val key = arg.take(eqIndex).lowercase()
            var value = arg.substring(eqIndex + 1)
            if (value.startsWith("\"") && value.endsWith("\"") && value.length >= 2) {
                value = value.removeSurrounding("\"")
            }
            updates[key] = value
        }

        try {
            val exp = service.expUpdate(
                id = id,
                name = updates["name"],
                description = updates["description"]

            )
            println("OK experiment #${exp.id} обновлён")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
    }

    private fun cmdRunList(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID эксперимента. Пример: run_list 2")
            return
        }
        val experimentId = args[0].toLongOrNull()
        if (experimentId == null) {
            println("Ошибка: ID должен быть числом")
            return
        }

        var last: Int? = null
        var i = 1
        while (i < args.size) {
            when (args[i]) {
                "--last" -> {
                    if (i + 1 >= args.size) {
                        println("Ошибка: после --last нужно указать число")
                        return
                    }
                    last = args[i + 1].toIntOrNull()
                    if (last == null) {
                        println("Ошибка: --last должно быть числом")
                        return
                    }
                    i += 2
                }
                else -> {
                    println("Неизвестная опция: ${args[i]}")
                    return
                }
            }
        }

        val runs = service.runList(experimentId, last)
        if (runs.isEmpty()) {
            println("Нет запусков для этого эксперимента.")
            return
        }
        println("ID\tRun name\tOperator\tTime")
        runs.forEach {
            println("${it.id}\t${it.name}\t${it.operatorName}\t${it.createdAt}")
        }
    }

    private fun cmdRunShow(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID запуска. Пример: run_show 11")
            return
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Ошибка: ID должен быть числом")
            return
        }
        try {
            val (run, resultsCount) = service.runShow(id)
            println("Run #${run.id}")
            println("experiment_id: ${run.experimentId}")
            println("name: ${run.name}")
            println("results: $resultsCount")
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
    }

    private fun cmdResList(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID запуска. Пример: res_list 11")
            return
        }
        val runId = args[0].toLongOrNull()
        if (runId == null) {
            println("Ошибка: ID должен быть числом")
            return
        }

        var param: String? = null
        var i = 1
        while (i < args.size) {
            when (args[i]) {
                "--param" -> {
                    if (i + 1 >= args.size) {
                        println("Ошибка: после --param нужно указать параметр")
                        return
                    }
                    param = args[i + 1]
                    i += 2
                }
                else -> {
                    println("Неизвестная опция: ${args[i]}")
                    return
                }
            }
        }

        val results = service.resList(runId, param)
        if (results.isEmpty()) {
            println("Нет результатов для этого запуска.")
            return
        }
        println("ID\tParam\tValue\tUnit\tComment")
        results.forEach {
            println("${it.id}\t${it.param}\t${it.value}\t${it.unit}\t${it.comment ?: ""}")
        }
    }

    private fun cmdExpSummary(args: List<String>) {
        if (args.isEmpty()) {
            println("Ошибка: укажите ID эксперимента. Пример: exp_summary 2")
            return
        }
        val id = args[0].toLongOrNull()
        if (id == null) {
            println("Ошибка: ID должен быть числом")
            return
        }
        try {
            val summary = service.expSummary(id)
            if (summary.isEmpty()) {
                println("Нет данных для этого эксперимента.")
                return
            }
            summary.forEach { (param, stats) ->
                println("$param: count=${stats.count} min=${stats.min} max=${stats.max} avg=${stats.avg}")
            }
        } catch (e: IllegalArgumentException) {
            println("Ошибка: ${e.message}")
        }
    }


    private fun parseArguments(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    inQuotes = !inQuotes
                }
                ch.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> {
                    current.append(ch)
                }
            }
            i++
        }
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
    }

    private fun showHelp() {
        println("""
            Доступные команды:
              help                            - показать эту справку
              exit                            - выход

              exp_create                       - создать эксперимент (интерактивно)
              exp_list [--mine]                 - список экспериментов
              exp_show <id>                     - детали эксперимента
              exp_update <id>  - обновить эксперимент (поля: name, description)
              run_add <experiment_id>            - добавить запуск (интерактивно)
              run_list <experiment_id> [--last N] - список запусков эксперимента
              run_show <run_id>                  - детали запуска
              res_add <run_id>                    - добавить результат (интерактивно)
              res_list <run_id> [--param PARAM]   - список результатов запуска
              exp_summary <experiment_id>         - сводка по параметрам эксперимента
        """.trimIndent())
    }
}






