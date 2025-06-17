package wdllmh.checkit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import wdllmh.checkit.controller.*;

import java.io.IOException;

import static wdllmh.checkit.Dao.executor;

public class HelloApplication extends Application {
    private static Stage primaryStage;
    private static final String[] fxmlNames = {
            "/hello-view.fxml",
            "/check.fxml",
            "/login.fxml",
            "/register.fxml",
            "/forget.fxml",
    };
    public static String dbPath = null;
    public static String photoPath = null;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void init() throws Exception {
        // 连接数据库等，与start方法在不同线程，效率更高
        // init 运行在javafx启动进程，不应该运行任何与ui相关的内容
    }

    @Override
    public void start(Stage stage){
        primaryStage = stage;
        primaryStage.setTitle("CheckIt 进出口发票填写助手");
        showScene(2);
    }

    @Override
    public void stop() throws Exception { // 所有东西停止
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        super.stop();
    }


    // 不能使用static否则会导致打包之后不可用
    // 在jar包环境下使用静态类的getResource返回null，不知道为什么
    public void showScene(int index) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml" + fxmlNames[index]));
            // 自定义控制器工厂，创建控制器后调用setApplication
            fxmlLoader.setControllerFactory(clazz -> {
                if (clazz == LoginController.class) {
                    LoginController controller = new LoginController();
                    controller.setApplication(this); // 调用setApplication注入
                    return controller;
                }
                else if (clazz == CheckController.class) {
                    CheckController controller = new CheckController();
                    controller.setApplication(this);
                    return controller;
                } else if (clazz == HelloController.class) {
                    HelloController controller = new HelloController();
                    controller.setApplication(this);
                    return controller;
                }
                else if (clazz == RegisterController.class) {
                    RegisterController controller = new RegisterController();
                    controller.setApplication(this);
                    return controller;
                }
                else if (clazz == ForgetController.class) {
                    ForgetController controller = new ForgetController();
                    controller.setApplication(this);
                    return controller;
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}