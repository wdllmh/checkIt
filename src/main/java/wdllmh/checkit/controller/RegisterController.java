package wdllmh.checkit.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import wdllmh.checkit.Dao;
import wdllmh.checkit.HelloApplication;

import java.util.concurrent.CompletableFuture;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private CheckBox agreementCheckbox;
    @FXML private Button registerButton;
    @FXML private Label login;

    private HelloApplication application;
    private Dao dao;

    @FXML
    private void initialize() {
        dao = new Dao();

        registerButton.setOnAction(event -> handleRegister());
        login.setOnMouseClicked(event -> {
            application.showScene(2);
        });
    }

    public void setApplication(HelloApplication application) {
        this.application = application;
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();

        // 表单验证
        if (username.length() < 5 || username.length() > 20) {
            showAlert("用户名长度应为 5-20 位");
            return;
        }

        if (password.length() < 8 || password.length() > 20) {
            showAlert("密码长度应为 8-20 位");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("两次密码输入不一致");
            return;
        }

        if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            showAlert("请输入有效的电子邮箱地址");
            return;
        }

        if (!agreementCheckbox.isSelected()) {
            showAlert("请勾选同意用户协议和隐私政策");
            return;
        }

        registerButton.setText("注册中...");
        registerButton.setDisable(true);

        CompletableFuture<Boolean> future = dao.registerUserAsync(username, password);
        future.thenAccept(result -> {
            Platform.runLater(() -> {
                if (result) {
                    showInfo("注册成功，请登录！");
                    if (application != null) {
                        application.showScene(2); // 切回登录页面
                    }
                } else {
                    showAlert("用户名已存在，请更换后重试");
                }

                resetButton();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showAlert("注册过程中出现错误：" + ex.getMessage());
                resetButton();

            });
            ex.printStackTrace();
            return null;
        });
    }

    private void handleGoToLogin(MouseEvent event) {
        if (application != null) {
            application.showScene(0); // 登录页下标应为 0
        }
    }

    private void resetButton() {
        registerButton.setText("注册");
        registerButton.setDisable(false);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("注册错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("注册成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
