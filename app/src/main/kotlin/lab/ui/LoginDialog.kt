package lab.ui

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.Modality
import javafx.stage.Stage
import lab.auth.AuthService

class LoginDialog(private val authService: AuthService, private val stage: Stage) {

    // Возвращает true если пользователь успешно вошёл
    fun show(): Boolean {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Вход в систему"
        dialog.initModality(Modality.APPLICATION_MODAL)

        val loginBtn = ButtonType("Войти", ButtonBar.ButtonData.OK_DONE)
        val registerBtn = ButtonType("Зарегистрироваться", ButtonBar.ButtonData.OTHER)
        val exitBtn = ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)

        dialog.dialogPane.buttonTypes.addAll(loginBtn, registerBtn, exitBtn)

        val usernameF = TextField()
        val passwordF = PasswordField()

        val grid = GridPane()
        grid.padding = Insets(16.0)
        grid.hgap = 10.0
        grid.vgap = 8.0
        grid.add(Label("Логин:"), 0, 0)
        grid.add(usernameF, 1, 0)
        grid.add(Label("Пароль:"), 0, 1)
        grid.add(passwordF, 1, 1)
        usernameF.prefWidth = 200.0
        passwordF.prefWidth = 200.0

        dialog.dialogPane.content = grid

        val result = dialog.showAndWait()

        return when (result.orElse(exitBtn)) {
            loginBtn -> {
                try {
                    authService.login(usernameF.text.trim(), passwordF.text.trim())
                    true
                } catch (e: Exception) {
                    showError("Ошибка входа", e.message)
                    show() // показать снова
                }
            }
            registerBtn -> {
                try {
                    authService.register(usernameF.text.trim(), passwordF.text.trim())
                    authService.login(usernameF.text.trim(), passwordF.text.trim())
                    true
                } catch (e: Exception) {
                    showError("Ошибка регистрации", e.message)
                    show() // показать снова
                }
            }
            else -> false // нажали Выйти
        }
    }

    private fun showError(title: String, message: String?) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = null
        alert.contentText = message ?: "Неизвестная ошибка"
        alert.showAndWait()
    }
}