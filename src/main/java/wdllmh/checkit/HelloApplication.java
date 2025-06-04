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
            "/hello-view.fxml",
            "/check.fxml",
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
//        System.err.println("start limian");
//        System.err.println(HelloApplication.class.getResource(fxmlNames[0]));
//        System.err.println(HelloApplication.class.getResource("/" + fxmlNames[0]));
//        System.err.println(HelloApplication.class.getResource("/"));
//        System.err.println(HelloApplication.class.getResource(""));
    }

    @Override
    public void stop() { // 所有东西停止
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }


    // 不能使用static否则会导致打包之后不可用
    // 在jar包环境下使用静态类的getResource返回null，不知道为什么
    public void showScene(int index) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml" + fxmlNames[index]));
            Scene scene = new Scene(fxmlLoader.load());
            if (index == 0) {
                HelloController controller = fxmlLoader.getController();
                controller.setApplication(this);
            } else if (index == 1) {
                CheckController controller = fxmlLoader.getController();
                controller.setApplication(this);
            }

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}