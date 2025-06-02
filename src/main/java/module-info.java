module wdllmh.checkit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.sql;
    requires org.apache.poi.ooxml;


    opens wdllmh.checkit to javafx.fxml;
    exports wdllmh.checkit;
}