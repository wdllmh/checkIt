package wdllmh.checkit.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import wdllmh.checkit.Dao;
import wdllmh.checkit.HelloApplication;

public class ForgetController {

    private HelloApplication application;

    public void setApplication(HelloApplication application) {
        this.application = application;
    }

    private Dao dao;

    @FXML private TextField usernameFiled;
    @FXML private TextField emailFiled;
    @FXML private Button sendVeriCode;
    @FXML private TextField veriCodeFiled;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField newPasswordTwoField;
    @FXML private Button change;
    @FXML private Label login;

    @FXML
    private void initialize() {
        dao = new Dao();
        sendVeriCode.setOnAction(e -> sendVerificationCode());
        change.setOnAction(e -> changePassword());
        login.setOnMouseClicked(this::goToLogin);
    }

    private void sendVerificationCode() {
        String email = emailFiled.getText();
        if (email == null || !email.contains("@")) {
            showAlert("错误", "请输入有效邮箱地址！");
            return;
        }

        // 模拟异步发送
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 模拟延迟
                Platform.runLater(() -> showAlert("成功", "验证码已发送，请查收邮件（可能在垃圾箱）"));
            } catch (InterruptedException e) {
                Platform.runLater(() -> showAlert("错误", "发送失败，请稍后再试"));
            }
        }).start();
    }

    private void changePassword() {
        String username = usernameFiled.getText();
        String email = emailFiled.getText();
        String code = veriCodeFiled.getText();
        String pass1 = newPasswordField.getText();
        String pass2 = newPasswordTwoField.getText();

        if (username.isEmpty() || email.isEmpty() || code.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            showAlert("错误", "请填写所有字段");
            return;
        }

        if (!pass1.equals(pass2)) {
            showAlert("错误", "两次输入的密码不一致");
            return;
        }

        if (pass1.length() < 8 || pass1.length() > 20) {
            showAlert("错误", "密码长度应为8~20位");
            return;
        }

        // 模拟密码重置逻辑
        new Thread(() -> {
            boolean result = dao.changePassword(username, pass1);
            if (result) {
                Platform.runLater(() -> {
                    showAlert("成功", "密码修改成功，请重新登录");
                    application.showScene(2); // 返回登录界面（index 视实际设计）
                });
            } else {
                Platform.runLater(() -> {
                    showAlert("失败", "未知原因");
                });
            }

        }).start();
    }

    private void goToLogin(MouseEvent event) {
        if (application != null) {
            application.showScene(2); // 返回登录页面
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
