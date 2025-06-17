package wdllmh.checkit.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import wdllmh.checkit.HelloApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class HelloController {
    private HelloApplication application;
    @FXML private Text title;

    @FXML private VBox main;
    @FXML private Button check, backup, backupPhoto,quit; // 成员与fxml中的 fx:id一一对应

    @FXML
    protected void goCheckPage(ActionEvent event) {
        application.showScene(1);
    }

    @FXML
    protected void quit(ActionEvent event) {
        //对话框 Alert Alert.AlertType.CONFIRMATION：反问对话框
        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION); // NONE为不可关闭的提示，INFORMATION/WARNING/ERROR为带确定的提示，CONFIRMATION为带确定取消的提示
        //设置对话框标题
        alert2.setTitle("退出");
        //设置内容
        alert2.setHeaderText("你确定要退出吗，所有改动已经自动保存");
        //显示对话框
        Optional<ButtonType> result = alert2.showAndWait(); // buttonType显示几种特别的按钮，比如应用、确认等 https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ButtonType.html
        //如果点击OK
        if (result.get() == ButtonType.OK){
            // ... user chose OK
            Platform.exit();
            System.exit(0);
            //否则
        } else {
            event.consume();
        }
    }

    @FXML
    protected void backupDatabase(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择备份文件保存位置");
        // 设置默认文件名，格式为 database[备份时间].db
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String defaultFileName = "database[" + dateFormat.format(new Date()) + "].db";
        fileChooser.setInitialFileName(defaultFileName);

        // 获取数据库文件路径
        String dbPath = System.getProperty("user.dir") + "/data/database.db";
        File sourceFile = new File(dbPath);

        // 显示文件选择器
        File destinationFile = fileChooser.showSaveDialog(null);
        if (destinationFile != null) {
            try {
                // 复制数据库文件到指定位置
                Files.copy(Path.of(sourceFile.getAbsolutePath()), Path.of(destinationFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("备份成功");
                alert.setHeaderText("数据库备份成功");
                alert.setContentText("备份文件已保存到：" + destinationFile.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("备份失败");
                alert.setHeaderText("数据库备份失败");
                alert.setContentText("备份过程中出现错误：" + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    protected void backupPhotos(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择照片备份文件夹保存位置");
        // 设置默认文件夹名，格式为 photo[yyyy_mm_dd_hh_mm_ss]
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String defaultFolderName = "photo[" + dateFormat.format(new Date()) + "]";
        fileChooser.setInitialFileName(defaultFolderName);

        // 获取照片文件夹路径
        String photoPath = System.getProperty("user.dir") + "/photo";
        File sourceFolder = new File(photoPath);

        // 显示文件选择器
        File destinationParent = fileChooser.showSaveDialog(null);
        if (destinationParent != null) {
            File destinationFolder = new File(destinationParent.getParentFile(), defaultFolderName);
            try {
                // 复制照片文件夹到指定位置
                copyFolder(sourceFolder.toPath(), destinationFolder.toPath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("备份成功");
                alert.setHeaderText("照片备份成功");
                alert.setContentText("备份文件夹已保存到：" + destinationFolder.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("备份失败");
                alert.setHeaderText("照片备份失败");
                alert.setContentText("备份过程中出现错误：" + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    private void copyFolder(Path source, Path destination) throws IOException {
        Files.walk(source)
                .forEach(sourcePath -> {
                    try {
                        Path destPath = destination.resolve(source.relativize(sourcePath));
                        if (Files.isDirectory(sourcePath)) {
                            Files.createDirectories(destPath);
                        } else {
                            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void setApplication(HelloApplication application) {
        this.application = application;
    }
}

