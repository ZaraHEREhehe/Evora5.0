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
    requires javafx.media;

    requires java.sql;  // for sql
    requires java.naming; // needed for JDBC
    requires com.microsoft.sqlserver.jdbc; // module name for SQL Server driver

    //requires com.example.demo1;
    exports com.example.demo1.Whitenoise;
    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.Pomodoro to javafx.fxml; //
    exports com.example.demo1.Theme;
    exports com.example.demo1;
    exports com.example.demo1.Calendar;
    opens com.example.demo1.Calendar to javafx.fxml;
}