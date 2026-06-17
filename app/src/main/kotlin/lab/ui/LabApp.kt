package lab.ui

import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import lab.domain.Experiment
import lab.domain.Run
import lab.domain.RunResult
import lab.service.LabService
import lab.auth.AuthService

class LabApp : Application() {

    private val service = LabService()
    private val authService = AuthService()

    private val expData = FXCollections.observableArrayList<ExpRow>()
    private val runData = FXCollections.observableArrayList<RunRow>()
    private val resData = FXCollections.observableArrayList<ResRow>()

    private var selectedExpId: Long? = null
    private var selectedRunId: Long? = null

    private val statusLabel = Label("Готово")

    override fun start(stage: Stage) {
        val loginDialog = LoginDialog(authService, stage)
        val loggedIn = loginDialog.show()

        if (!loggedIn) {
            stage.close()
            return
        }

        stage.title = "Лабораторная система — ${authService.getCurrentUsername()}"

        val root = BorderPane()
        root.top = makeToolbar(stage)
        root.center = makeTabs()
        root.bottom = HBox(statusLabel).apply {
            padding = Insets(4.0, 8.0, 4.0, 8.0)
            style = "-fx-background-color: #eeeeee;"
        }

        stage.scene = Scene(root, 900.0, 600.0)
        stage.show()
        loadExperiments()
    }

    private fun makeToolbar(stage: Stage): ToolBar {
        val refreshBtn = Button("Refresh")
        refreshBtn.setOnAction({  e ->
            loadExperiments()
            loadRuns()
            loadResults()
            setStatus("Обновлено")
        })

        val saveBtn = Button("Сохранить")
        saveBtn.setOnAction {
            val chooser = FileChooser()
            chooser.title = "Сохранить данные"
            chooser.extensionFilters.add(FileChooser.ExtensionFilter("JSON", "*.json"))
            val file = chooser.showSaveDialog(stage)
            if (file != null) {
                try {
                    service.save(file.absolutePath)
                    setStatus("Сохранено: ${file.name}")
                } catch (e: Exception) {
                    showError("Ошибка сохранения", e.message)
                }
            }
        }

        val loadBtn = Button("Загрузить")
        loadBtn.setOnAction {
            val chooser = FileChooser()
            chooser.title = "Загрузить данные"
            chooser.extensionFilters.add(FileChooser.ExtensionFilter("JSON", "*.json"))
            val file = chooser.showOpenDialog(stage)
            if (file != null) {
                try {
                    service.load(file.absolutePath)
                    selectedExpId = null
                    selectedRunId = null
                    loadExperiments()
                    runData.clear()
                    resData.clear()
                    setStatus("Загружено: ${file.name}")
                } catch (e: Exception) {
                    showError("Ошибка загрузки", e.message)
                }
            }
        }

        return ToolBar(refreshBtn, Separator(), saveBtn, loadBtn)
    }

    private fun makeTabs(): TabPane {
        val tabs = TabPane()
        tabs.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabs.tabs.add(Tab("Эксперименты", makeExpTab()))
        tabs.tabs.add(Tab("Запуски", makeRunTab()))
        tabs.tabs.add(Tab("Результаты", makeResTab()))
        return tabs
    }

    private fun makeExpTab(): VBox {
        val table = TableView(expData)

        val colId = TableColumn<ExpRow, String>("ID")
        colId.cellValueFactory = PropertyValueFactory("id")
        colId.prefWidth = 60.0

        val colName = TableColumn<ExpRow, String>("Название")
        colName.cellValueFactory = PropertyValueFactory("name")
        colName.prefWidth = 300.0

        val colOwner = TableColumn<ExpRow, String>("Владелец")
        colOwner.cellValueFactory = PropertyValueFactory("owner")
        colOwner.prefWidth = 130.0

        val colRuns = TableColumn<ExpRow, String>("Запуски")
        colRuns.cellValueFactory = PropertyValueFactory("runs")
        colRuns.prefWidth = 70.0

        table.columns.addAll(colId, colName, colOwner, colRuns)
        table.selectionModel.selectedItemProperty().addListener { _, _, row ->
            selectedExpId = row?.rawId
            selectedRunId = null
            runData.clear()
            resData.clear()
            if (row != null) loadRuns()
        }

        val addBtn = Button("Добавить")
        addBtn.setOnAction {
            val nameF = TextField()
            val descF = TextField()
            val ok = showDialog("Новый эксперимент", "Название:" to nameF, "Описание:" to descF)
            if (ok) {
                try {
                    service.expCreate(nameF.text.trim(), descF.text.trim().ifBlank { null }, authService.getCurrentUsername())
                    loadExperiments() // TODO: сделать загрузку только новых элементов
                    setStatus("Эксперимент создан")
                } catch (e: Exception) {
                    showError("Ошибка", e.message)
                }
            }
        }

        val delBtn = Button("Удалить")
        delBtn.setOnAction {
            val row = table.selectionModel.selectedItem ?: return@setOnAction

            if (row.getOwner() != authService.getCurrentUsername()) {
                showError("Ошибка", "Вы можете удалять только свои эксперименты")
                return@setOnAction
            }

            val confirmed = showConfirm("Удалить '${row.getName()}'?\nВсе запуски и результаты тоже удалятся.")
            if (confirmed) {
                try {
                    service.expDelete(row.rawId)
                    selectedExpId = null
                    loadExperiments()
                    runData.clear()
                    resData.clear()
                    setStatus("Удалено")
                } catch (e: Exception) {
                    showError("Ошибка", e.message)
                }
            }
        }

        val btns = HBox(8.0, addBtn, delBtn)
        btns.padding = Insets(6.0, 0.0, 0.0, 0.0)

        val box = VBox(8.0, table, btns)
        box.padding = Insets(10.0)
        VBox.setVgrow(table, Priority.ALWAYS)
        return box
    }

    private fun makeRunTab(): VBox {
        val table = TableView(runData)

        val colId = TableColumn<RunRow, String>("ID")
        colId.cellValueFactory = PropertyValueFactory("id")
        colId.prefWidth = 60.0

        val colName = TableColumn<RunRow, String>("Название")
        colName.cellValueFactory = PropertyValueFactory("name")
        colName.prefWidth = 260.0

        val colOp = TableColumn<RunRow, String>("Оператор")
        colOp.cellValueFactory = PropertyValueFactory("operator")
        colOp.prefWidth = 150.0

        val colRes = TableColumn<RunRow, String>("Результаты")
        colRes.cellValueFactory = PropertyValueFactory("results")
        colRes.prefWidth = 80.0

        table.columns.addAll(colId, colName, colOp, colRes)

        table.selectionModel.selectedItemProperty().addListener { _, _, row ->
            selectedRunId = row?.rawId
            resData.clear()
            if (row != null) loadResults()
        }

        val addBtn = Button("Добавить запуск")
        addBtn.setOnAction {
            val expId = selectedExpId
            if (expId == null) {
                showError("Ошибка", "Сначала выберите эксперимент на первой вкладке")
                return@setOnAction
            }
            val nameF = TextField()
            val opF = TextField()
            val ok = showDialog("Новый запуск", "Название:" to nameF, "Оператор:" to opF)
            if (ok) {
                try {
                    service.runAdd(expId, nameF.text.trim(), opF.text.trim())
                    loadRuns()
                    setStatus("Запуск создан")
                } catch (e: Exception) {
                    showError("Ошибка", e.message)
                }
            }
        }

        val btns = HBox(8.0, addBtn)
        btns.padding = Insets(6.0, 0.0, 0.0, 0.0)

        val box = VBox(8.0, table, btns)
        box.padding = Insets(10.0)
        VBox.setVgrow(table, Priority.ALWAYS)
        return box
    }

    private fun makeResTab(): VBox {
        val table = TableView(resData)

        val colId = TableColumn<ResRow, String>("ID")
        colId.cellValueFactory = PropertyValueFactory("id")
        colId.prefWidth = 60.0

        val colParam = TableColumn<ResRow, String>("Параметр")
        colParam.cellValueFactory = PropertyValueFactory("param")
        colParam.prefWidth = 150.0

        val colVal = TableColumn<ResRow, String>("Значение")
        colVal.cellValueFactory = PropertyValueFactory("value")
        colVal.prefWidth = 100.0

        val colUnit = TableColumn<ResRow, String>("Единицы")
        colUnit.cellValueFactory = PropertyValueFactory("unit")
        colUnit.prefWidth = 100.0

        val colComment = TableColumn<ResRow, String>("Комментарий")
        colComment.cellValueFactory = PropertyValueFactory("comment")
        colComment.prefWidth = 200.0

        table.columns.addAll(colId, colParam, colVal, colUnit, colComment)

        val addBtn = Button("Добавить результат")
        addBtn.setOnAction {
            val runId = selectedRunId
            if (runId == null) {
                showError("Ошибка", "Сначала выберите запуск на второй вкладке")
                return@setOnAction
            }
            val paramF = TextField()
            val valueF = TextField()
            val unitF = TextField()
            val commentF = TextField()
            val ok = showDialog(
                "Новый результат",
                "Параметр:" to paramF,
                "Значение:" to valueF,
                "Единицы:" to unitF,
                "Комментарий:" to commentF
            )
            if (ok) {
                val v = valueF.text.trim().toDoubleOrNull()
                if (v == null) {
                    showError("Ошибка", "Значение должно быть числом")
                    return@setOnAction
                }
                try {
                    service.resAdd(runId, paramF.text.trim(), v, unitF.text.trim(), commentF.text.trim().ifBlank { null })
                    loadResults()
                    setStatus("Результат добавлен")
                } catch (e: Exception) {
                    showError("Ошибка", e.message)
                }
            }
        }

        val btns = HBox(8.0, addBtn)
        btns.padding = Insets(6.0, 0.0, 0.0, 0.0)

        val box = VBox(8.0, table, btns)
        box.padding = Insets(10.0)
        VBox.setVgrow(table, Priority.ALWAYS)
        return box
    }

    private fun loadExperiments() {
        val rows = service.expList().map { exp ->
            val (_, runsCount) = service.expShow(exp.id)
            ExpRow(exp, runsCount)
        }
        expData.setAll(rows)
    }

    private fun loadRuns() {
        val expId = selectedExpId ?: return
        val rows = service.runList(expId).map { run ->
            val (_, resCount) = service.runShow(run.id)
            RunRow(run, resCount)
        }
        runData.setAll(rows)
    }

    private fun loadResults() {
        val runId = selectedRunId ?: return
        val rows = service.resList(runId).map { res -> ResRow(res) }
        resData.setAll(rows)
    }

    private fun showDialog(title: String, vararg fields: Pair<String, TextField>): Boolean {
        val dialog = Dialog<ButtonType>()
        dialog.title = title
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val grid = GridPane()
        grid.padding = Insets(16.0)
        grid.hgap = 10.0
        grid.vgap = 8.0
        fields.forEachIndexed { i, (label, field) ->
            grid.add(Label(label), 0, i)
            field.prefWidth = 240.0
            grid.add(field, 1, i)
        }
        dialog.dialogPane.content = grid

        val result = dialog.showAndWait()
        return result.isPresent && result.get() == ButtonType.OK
    }

    private fun showConfirm(message: String): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Подтверждение"
        alert.headerText = null
        alert.contentText = message
        val result = alert.showAndWait()
        return result.isPresent && result.get() == ButtonType.OK
    }

    private fun showError(title: String, message: String?) {
        Platform.runLater {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = title
            alert.headerText = null
            alert.contentText = message ?: "Неизвестная ошибка"
            alert.showAndWait()
        }
    }

    private fun setStatus(msg: String) {
        Platform.runLater { statusLabel.text = msg }
    }
}

class ExpRow(private val exp: Experiment, private val runsCount: Int) {
    val rawId = exp.id
    fun getId() = exp.id.toString()
    fun getName() = exp.name
    fun getOwner() = exp.ownerUsername
    fun getRuns() = runsCount.toString()
}

class RunRow(private val run: Run, private val resCount: Int) {
    val rawId = run.id
    fun getId() = run.id.toString()
    fun getName() = run.name
    fun getOperator() = run.operatorName
    fun getResults() = resCount.toString()
}

class ResRow(private val res: RunResult) {
    fun getId() = res.id.toString()
    fun getParam() = res.param
    fun getValue() = String.format("%.4f", res.value)
    fun getUnit() = res.unit
    fun getComment() = res.comment ?: "-"
}