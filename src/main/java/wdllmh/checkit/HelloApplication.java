package wdllmh.checkit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static wdllmh.checkit.Dao.executor;

public class HelloApplication extends Application {
    private static Stage primaryStage;
    private static final String[] fxmlNames = {
            "Hello-view.fxml",
            "check.fxml",
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
        showScene(0);
    }

    @Override
    public void stop() { // 所有东西停止
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }


    public static void showScene(int index) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxmlNames[index]));
            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}