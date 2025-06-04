module wdllmh.checkit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.sql;


    opens wdllmh.checkit to javafx.fxml;
    exports wdllmh.checkit;
}