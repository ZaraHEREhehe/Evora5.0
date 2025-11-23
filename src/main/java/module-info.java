module com.example.Evora {
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

    requires java.naming; // needed for JDBC
    requires com.microsoft.sqlserver.jdbc; // module name for SQL Server driver

    //requires com.example.Evora;
    //exports com.example.Evora.Whitenoise;
    opens com.example.Evora to javafx.fxml;
    opens com.example.Evora.Pomodoro to javafx.fxml; //
    exports com.example.Evora.Theme;
    exports com.example.Evora;
    exports com.example.Evora.Calendar;
    opens com.example.Evora.Calendar to javafx.fxml;
}