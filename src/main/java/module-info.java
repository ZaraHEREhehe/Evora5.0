module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.kordamp.ikonli.fontawesome6;
    requires java.desktop;
    requires java.sql;

    opens com.example.demo1 to javafx.fxml;
    exports com.example.demo1;
    exports com.example.demo1.Calendar;
    opens com.example.demo1.Calendar to javafx.fxml;
}