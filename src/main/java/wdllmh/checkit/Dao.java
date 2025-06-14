package wdllmh.checkit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dao {
    private Connection connection = null;
    private String photoDir = null;
    private String dbDir = null;
    protected static final ExecutorService executor = Executors.newFixedThreadPool(2);

    private static final int MAXWIDTH = 800;
    private static final int MAXHEIGHT = 600;

    public Dao(String path, String photoDir) {
        try {
            Class.forName("org.sqlite.JDBC"); // 手动加载驱动
            connect(path, photoDir);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Dao() {
        connect(null, null);
    }

    /**
     * 连接数据库并检查照片文件夹
     * @param dbDir db文件所在的文件夹
     * @param photoDir 图片路径（全，为文件夹路径）如果为null，设置为默认路径
     */
    public void connect(String dbDir, String photoDir) {

        try {
            // 获取当前应用程序的工作目录
            String workingDir = System.getProperty("user.dir");

            // 处理数据库路径
            if (dbDir != null) {
                this.dbDir = dbDir;
            } else {
                this.dbDir = workingDir + File.separator + "data";
            }
            String dbPath = this.dbDir + File.separator + "database.db";

            // 确保目录存在
            File folder = new File(this.dbDir);
            if (!folder.exists()) {
                folder.mkdirs(); // 如果文件夹不存在，则创建它
            }

            // 处理照片目录
            if (photoDir != null) {
                this.photoDir = photoDir.trim();
            } else {

                String path = workingDir + File.separator + "photo";
                File photoFile = new File(path);
                if (!photoFile.exists()) {
                    photoFile.mkdirs();
                }
                this.photoDir = path;
            }

            // SQLite数据库文件的路径
            String url = "jdbc:sqlite:" + dbPath;
//            System.out.println(url);

            // 建立与数据库的连接
            connection = DriverManager.getConnection(url);

        } catch (SQLException e) {
            System.err.println("连接到SQLite数据库时出现错误: " + e.getMessage());
        }

        // 如果表不存在则创建
        String sql = """
                CREATE TABLE IF NOT EXISTS data (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    english TEXT NOT NULL,
                    weight REAL DEFAULT 0.0,
                    comment TEXT\s
                );""";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void close() {

    }

    /**
     * 读取数据库中的所有数据并返回DataRow类型
     * */
    public ObservableList<DataRow> getData() throws SQLException {
        String sql = "SELECT id, name, english, weight, comment FROM data";
        ObservableList<DataRow> data = FXCollections.observableArrayList();

        try (PreparedStatement stmt = this.connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DataRow row = new DataRow();
                row.setId(rs.getInt("id"));
                row.setName(rs.getString("name"));
                row.setEnglish(rs.getString("english"));
                row.setWeight(rs.getDouble("weight"));
                row.setComment(rs.getString("comment"));
                row.setPhoto(getImage(row.getId()));
                data.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    public Image getImage(int id) throws SQLException {
        File file = new File(photoDir + File.separator + id + ".jpg");
        if (!file.exists()) {
            return getDefaultImage();
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                return getDefaultImage();
            }
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getDefaultImage();
    }

    public Image getDefaultImage() {
        URL url = this.getClass().getResource("image/0.png");
//        System.out.println(url);
        if (url == null) {
            return null;
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(url);
            if (bufferedImage == null) {
                return null;
            }
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void append(String name, String english, Double weight, String comment) {
        String sql = "INSERT INTO data (name, english, weight, comment)\n"
                + "VALUES (?,?,?,?)";

        if (weight == null) {
            weight = 0.0;
        }
        if (comment == null) {
            comment = "";
        }
        if (name == null) {
            name = "";
        }
        if (english == null) {
            english = "";
        }

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, english);
            pstmt.setDouble(3, weight);
            pstmt.setString(4, comment);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getLastInsertId() {
        String sql = "SELECT last_insert_rowid()";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return (int) rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 删除数据行及关联的图片文件
     * @param id 要删除的行ID
     * @throws SQLException 数据库操作异常
     */
    public void deleteRow(int id) throws SQLException {
        // 1. 删除数据库中的记录
        String deleteSql = "DELETE FROM data WHERE id = ?";
        try (PreparedStatement pstmt = this.connection.prepareStatement(deleteSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }

        // 2. 删除对应的图片文件
        deletePhotoFile(id);
    }

    /**
     * 删除指定ID的图片文件
     */
    private void deletePhotoFile(int id) {
        if (photoDir == null) return; // 确保照片目录已初始化

        File photoFile = new File(photoDir, id + ".jpg");
        if (photoFile.exists()) {
            boolean deleted = photoFile.delete();
            if (!deleted) {
                System.err.println("警告：删除图片文件失败，路径：" + photoFile.getPath());
            }
        }
    }

    public void changeName(Integer id, String name) {
        if (name != null && !name.isEmpty()) {
            String sql = "UPDATE data SET name = ? WHERE id = " + id;
            try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    public void changeEnglish(Integer id, String english) {
        if (english != null || !english.isEmpty()) {
            String sql = "UPDATE data SET english = ? WHERE id = " + id;
            try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
                pstmt.setString(1, english);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void changeWeight(Integer id, Double weight) {
        if (weight != null) {
            String sql = "UPDATE data SET weight = ? WHERE id = " + id;
            try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
                pstmt.setDouble(1, weight);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void changeComment(Integer id, String comment) {
        if (comment != null || !comment.isEmpty()) {
            String sql = "UPDATE data SET comment = ? WHERE id = " + id; // 问号不需要被包裹在单引号中，setString会自己加上，否则会将问号视作内容
            try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
                pstmt.setString(1, comment);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------- 异步方法：处理 JavaFX Image --------------------
    /**
     * 异步修改图片（JavaFX Image 输入）
     * @param id 数据行ID
     * @param image
     * @param quality 压缩质量
     * @return CompletableFuture<Boolean> 处理结果
     */
    public CompletableFuture<Boolean> changePhotoAsync( int id, Image image) {
        File outputFile = new File(photoDir, id + ".jpg");
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        // 将JavaFX Image转换为BufferedImage
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                        // 调用原有压缩方法
                        compressAndSaveImage(bufferedImage, outputFile, 0.5F);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }, executor);
    }
    public CompletableFuture<Boolean> changePhotoAsync(int id, BufferedImage bufferedImage) {
        File outputFile = new File(photoDir, id + ".jpg");
        BufferedImage finalBufferedImage = bufferedImage;
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 直接调用原有压缩方法（已包含透明处理和缩放）
                compressAndSaveImage(finalBufferedImage, outputFile, 0.5F);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }
    public CompletableFuture<Boolean> changePhotoAsync(int id, File inputFile) {
        File outputFile = new File(photoDir, id + ".jpg");
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 读取文件为BufferedImage
                BufferedImage bufferedImage = ImageIO.read(inputFile);
                if (bufferedImage == null) throw new IOException("无法读取图片");

                // 调用原有压缩方法
                compressAndSaveImage(bufferedImage, outputFile, 0.5F);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    /**
     * 将传入的javafx的Image对象进行压缩，并转换为标准库ImageIO的BufferedImage类写入文件，请注意区分另一个Image类
     * @param image 传入图片
     * @param outputFile 导出地方的文件对象
     * @param quality 压缩质量，0-1，1为质量最好体积最大
     * @throws IOException File操作有问题
     */
    private void compressAndSaveImage(BufferedImage bufferedImage, File outputFile, float quality) throws IOException {
        // 处理可能存在的透明图层，由于jpg不支持透明图层
        bufferedImage = ImageUtils.handleTransparency(bufferedImage);
        // 缩小图片
        bufferedImage = ImageUtils.resizeImage(bufferedImage, MAXWIDTH, MAXHEIGHT);

        // 获取 JPEG 格式的 ImageWriter
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found for JPEG format.");
        }
        ImageWriter writer = writers.next();

        // 设置压缩参数
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // 0.0f 最小质量（最大压缩），1.0f 最大质量（最小压缩）

        // 创建输出文件并设置输出流
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(bufferedImage, null, null), param);
        } finally {
            writer.dispose();
        }
    }



    public static void main(String[] args) throws SQLException {
        Dao dao = new Dao();

        dao.append(null, null, null, null);
        dao.append(null, null, null, null);
        dao.append(null, null, null, null);
        dao.append(null, null, null, null);

        dao.changeComment(4, "修改备注");
        dao.changeEnglish(1, "Change English");
        dao.changeName(2, "修改中文名");
        dao.changeWeight(3, 0.114514);


        ObservableList<DataRow> list = dao.getData();
        for (DataRow row : list) {
            System.out.println(row.getId());
        }

    }




}
