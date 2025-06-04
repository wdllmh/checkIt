package wdllmh.checkit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.util.StringConverter;

import java.io.File;
import java.sql.SQLException;
import java.util.Stack;

public class CheckController {
    // 初始化顺序，成员变量初始化-构造函数执行-@fxml注入-initialize函数执行
    private ObservableList<DataRow> data = FXCollections.observableArrayList();
    private ObservableList<DataRow> filteredData = FXCollections.observableArrayList();// 数据模型
    private final Dao dao = new Dao(HelloApplication.dbPath, HelloApplication.photoPath);

    // 用于记录操作历史的栈
    private Stack<UndoInfo> undoStack = new Stack<>();

    private HelloApplication application;

    public void setApplication(HelloApplication application) {
        this.application = application;
    }

    // 工具栏的几个按钮
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Button addButton;
    @FXML private Button searchButton;
    @FXML private TextField queryField;

    @FXML private TableView<DataRow> table;

    // 列表的列对象
    @FXML private TableColumn<DataRow, String> name;
    @FXML private TableColumn<DataRow, String> english;
    @FXML private TableColumn<DataRow, Double> weight;
    @FXML private TableColumn<DataRow, Image> photo;
    @FXML private TableColumn<DataRow, String> comment; // 问号为通配符，前面代表行对象的类型，后面代表本列单元格的属性
    @FXML TableColumn<DataRow, Button> delete;



    // 持久化存储
    @FXML
    void changeChinese(TableColumn.CellEditEvent<DataRow, String> event) {
        dao.changeName(event.getRowValue().getId(), event.getNewValue());
        event.getRowValue().setName(event.getNewValue());
    }

    @FXML
    void changeEnglish(TableColumn.CellEditEvent<DataRow, String> event) {
        dao.changeEnglish(event.getRowValue().getId(), event.getNewValue());
        event.getRowValue().setEnglish(event.getNewValue());
    }

    @FXML
    void changeWeight(TableColumn.CellEditEvent<DataRow, Double> event) {
        dao.changeWeight(event.getRowValue().getId(), event.getNewValue());
        event.getRowValue().setWeight(event.getNewValue());
    }


    @FXML
    void changeComment(TableColumn.CellEditEvent<DataRow, String> event) {
        dao.changeComment(event.getRowValue().getId(), event.getNewValue());
        event.getRowValue().setComment(event.getNewValue());
    }

    @FXML
    void addLine(ActionEvent event) {
        dao.append(null, null, null, null);
        data.add(new DataRow(dao.getLastInsertId(), 0.00, "", "", dao.getDefaultImage(), ""));
    }

    void confirmAndDeleteLine(DataRow row) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setContentText("是否删除ID为 " + row.getId() + " 的数据？");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                dao.deleteRow(row.getId()); // 调用DAO删除
                data.remove(row); // 从数据模型移除
                filteredData.remove(row);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void clearField(ActionEvent event) {
        queryField.clear();
        this.table.setItems(data);
    }

    @FXML
    void goMainPage(ActionEvent event) {
        application.showScene(0);
    }

    @FXML
    void searchData(ActionEvent event) {
        String searchText = queryField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            return;
        }

        this.filteredData.clear();
        for (DataRow row : data) {
            if (row.getName().toLowerCase().contains(searchText) ||
                row.getEnglish().toLowerCase().contains(searchText) ||
                row.getWeight().toString().toLowerCase().contains(searchText) ||
                row.getComment().toLowerCase().contains(searchText)) {
                this.filteredData.add(row);
            }
        }

        if (filteredData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("搜索结果");
            alert.setHeaderText(null);
            alert.setContentText("未找到匹配的内容!");
            alert.showAndWait();
        } else {
            this.table.setItems(filteredData);
        }
    }


    public void initialize() {
        table.setEditable(true);
        table.setItems(data); // 将ui与数据类进行绑定，可以通过ui操作数据，并使ui能够反映数据的修改

        // 设置单元格值工厂
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        english.setCellValueFactory(new PropertyValueFactory<>("english"));
        weight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        photo.setCellValueFactory(new PropertyValueFactory<>("photo"));
        comment.setCellValueFactory(new PropertyValueFactory<>("comment"));

        // 设置列宽
        name.setPrefWidth(150);
        english.setPrefWidth(200);
        weight.setPrefWidth(100);
        photo.setPrefWidth(125);
        comment.setPrefWidth(125);

        // 设置单元格格式工厂
        name.setCellFactory(TextFieldTableCell.forTableColumn());
        english.setCellFactory(TextFieldTableCell.forTableColumn()); // 本质是将列对象传入，生成可以显示的string内容，输入修改也是通过string中转
        weight.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return object == null ? "" : String.format("%.2f", object);
            }

            @Override
            public Double fromString(String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }));
        photo.setCellFactory(column -> new TableCell<DataRow, Image>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);


            }

            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);

                } else {
                    imageView.setImage(item);
                    setGraphic(imageView);
                }
                // 要放在这里，因为上面设置单元格时尚未与具体行关联，getTableRow返回null
                TableRow<DataRow> tableRow = getTableRow();
                if (tableRow != null && tableRow.getItem() != null) {
                    this.getProperties().put("row", tableRow.getItem());// 让imageview带有dataRow对象
                }

                setupDragAndDrop(this);
            }
        });
        comment.setCellFactory(TextFieldTableCell.forTableColumn());
        delete.setCellFactory(column -> new TableCell<DataRow, Button>() {
            private final Button button = new Button("删除");

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                  button.setOnAction(event -> {
                      DataRow row = getTableRow().getItem(); // 获取当前行数据
                      confirmAndDeleteLine(row); // 传递行对象
                  });
                  setGraphic(button);
            }
        }); // 删除按钮



        // 读取内容
        try {
            data = dao.getData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 监听 Ctrl + V 按键事件
        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                pasteFromClipboard();
                event.consume();
            }
        });

        table.setItems(data);
    }

    private void pasteFromClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            // 记录粘贴前的数据状态
            ObservableList<DataRow> beforePasteData = FXCollections.observableArrayList(data);
            // 获取起始行（优先使用选中行，若无则使用焦点行或0）
            int startRow = table.getSelectionModel().getSelectedIndex();
            if (startRow == -1) {
                TablePosition<DataRow, ?> focusCell = table.getFocusModel().getFocusedCell();
                startRow = focusCell != null ? focusCell.getRow() : 0;
            }
            startRow = Math.max(0, startRow); // 确保起始行≥0

            int startCol = table.getColumns().indexOf(table.getFocusModel().getFocusedCell().getTableColumn());

            String clipboardText = clipboard.getString();
            String[][] values = parseClipboardText(clipboardText);

            // 确保有足够的行来粘贴数据
            int requiredRows = startRow + values.length;
            if (requiredRows > data.size()) {
                // 计算需要添加的行数
                int rowsToAdd = requiredRows - data.size();
                for (int i = 0; i < rowsToAdd; i++) {
                    // 创建新行并添加到数据模型
                    dao.append(null, null, null, null);
                    DataRow newRow = new DataRow(dao.getLastInsertId(), 0.0, "", "", dao.getDefaultImage(), "");
                    data.add(newRow);
                }
            }

            // 填充数据
            for (int i = 0; i < values.length; i++) {
                int currentRow = startRow + i;
                DataRow row = data.get(currentRow);
                for (int j = 0; j < values[i].length; j++) {
                    int currentCol = startCol + j;
                    if (currentCol >= table.getColumns().size()) {
                        break;
                    }
                    TableColumn<DataRow, ?> column = table.getColumns().get(currentCol);
                    if (column == name) {
                        row.setName(values[i][j]);
                        dao.changeName(row.getId(), values[i][j]);
                    } else if (column == english) {
                        row.setEnglish(values[i][j]);
                        dao.changeEnglish(row.getId(), values[i][j]);
                    } else if (column == weight) {
                        try {
                            double value = Double.parseDouble(values[i][j]);
                            row.setWeight(value);
                            dao.changeWeight(row.getId(), value);
                        } catch (NumberFormatException e) {
                            // 处理无效的数字输入
                        }
                    } else if (column == comment) {
                        row.setComment(values[i][j]);
                        dao.changeComment(row.getId(), values[i][j]);
                    }
                }
            }

            // 将粘贴操作的信息压入栈中
            undoStack.push(new UndoInfo(startRow, startCol, beforePasteData));
        }
    }

    private String[][] parseClipboardText(String text) {
        String[] lines = text.split("\n");
        String[][] values = new String[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            values[i] = lines[i].split("\t");
        }
        return values;
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".png") ||
                name.endsWith(".jpeg") || name.endsWith(".gif");
    }

    // 设置拖放目标
    private void setupDragAndDrop(TableCell<DataRow, Image> cell) {
        // 拖入事件
        cell.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // 拖入视觉反馈
        cell.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()) {
                cell.setStyle("-fx-background-color: lightblue;");
            }
        });

        // 拖出恢复
        cell.setOnDragExited(event -> {
            cell.setStyle("");
        });

        // 拖放完成
        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (isImageFile(file)) {
                        try {
                            DataRow row = (DataRow) cell.getProperties().get("row");
                            if (row != null) {
                                // 处理图片文件
                                dao.changePhotoAsync(row.getId(), file)
                                        .thenAccept(succ -> {
                                            if (succ) {
                                                Platform.runLater(() -> {
                                                    // 更新UI（如刷新表格中的图片）
                                                    try {
                                                        row.setPhoto(dao.getImage(row.getId()));
                                                    } catch (SQLException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                });
                                            } else {
                                                System.out.println("图片更新失败");
                                            }
                                        })
                                        .exceptionally(ex -> {
                                            System.err.println("异步处理失败: " + ex.getMessage());
                                            return null;
                                        });

                                success = true;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    // 用于记录撤销信息的内部类
    private static class UndoInfo {
        int startRow;
        int startCol;
        ObservableList<DataRow> beforePasteData;

        UndoInfo(int startRow, int startCol, ObservableList<DataRow> beforePasteData) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.beforePasteData = beforePasteData;
        }
    }

}


