package wdllmh.checkit.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import wdllmh.checkit.Dao;
import wdllmh.checkit.HelloApplication;

import java.util.prefs.Preferences;
import java.util.concurrent.CompletableFuture;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label register;
    @FXML
    private Label forget;

    private HelloApplication application;
    private Dao dao;

    @FXML
    private void initialize() {
        // 初始化Dao实例
        dao = new Dao();
        loginButton.setOnAction(this::handleLogin);
        register.setOnMouseClicked(event -> {
            application.showScene(3);
        });
        forget.setOnMouseClicked(event -> {
            application.showScene(4);
        });
    }

    public void setApplication(HelloApplication application) {
        this.application = application;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // 输入验证
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("请输入用户名和密码");
            shakeInputFields();
            return;
        }

        // 显示加载状态
        loginButton.setText("正在验证...");
        loginButton.setDisable(true);

        // 异步验证用户
        CompletableFuture<Boolean> verifyFuture = dao.verifyUserAsync(username, password);
        verifyFuture.thenAccept(verifyResult -> {
            Platform.runLater(() -> {
                if (verifyResult) {
                    handleLoginSuccess(username);
                } else {
                    showAlert("用户名或密码错误，请重新输入");
                    shakeInputFields();
                    loginButton.setText("登录");
                    loginButton.setDisable(false);
                }
            });
        }).exceptionally(ex -> {
            // 处理异常
            application.getHostServices().showDocument(""); // 重置UI线程
            showAlert("登录过程中发生错误：" + ex.getMessage());
            loginButton.setText("登录");
            loginButton.setDisable(false);
            return null;
        });
    }

    private void handleLoginSuccess(String username) {
        // 通知Application切换场景
        application.showScene(0);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("登录错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void shakeInputFields() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), usernameField);
        shake.setByX(10);
        shake.setCycleCount(3);
        shake.setAutoReverse(true);

        TranslateTransition shakePassword = new TranslateTransition(Duration.millis(50), passwordField);
        shakePassword.setByX(10);
        shakePassword.setCycleCount(3);
        shakePassword.setAutoReverse(true);

        shake.play();
        shakePassword.play();
    }


}