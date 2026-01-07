module com.example.ece318_librarymanagementsys_uc1069790 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.ece318_librarymanagementsys_uc1069790 to javafx.fxml;
    exports com.example.ece318_librarymanagementsys_uc1069790;
    exports com.example.ece318_librarymanagementsys_uc1069790.controller;
    opens com.example.ece318_librarymanagementsys_uc1069790.controller to javafx.fxml;
    exports com.example.ece318_librarymanagementsys_uc1069790.database;
    opens com.example.ece318_librarymanagementsys_uc1069790.database to javafx.fxml;
    opens com.example.ece318_librarymanagementsys_uc1069790.model to javafx.base;
    exports com.example.ece318_librarymanagementsys_uc1069790.controller.core;
    opens com.example.ece318_librarymanagementsys_uc1069790.controller.core to javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires com.github.librepdf.openpdf;

}