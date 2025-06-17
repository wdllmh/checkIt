module wdllmh.checkit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.sql;
    requires java.prefs;


    opens wdllmh.checkit to javafx.fxml;
    exports wdllmh.checkit;
    exports wdllmh.checkit.controller;
    opens wdllmh.checkit.controller to javafx.fxml;
}