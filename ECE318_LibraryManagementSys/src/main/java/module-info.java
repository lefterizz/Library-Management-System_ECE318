module com.example.ece318_librarymanagementsys {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.ece318_librarymanagementsys to javafx.fxml;
    exports com.example.ece318_librarymanagementsys;
    exports com.example.ece318_librarymanagementsys.controller;
    opens com.example.ece318_librarymanagementsys.controller to javafx.fxml;
    exports com.example.ece318_librarymanagementsys.database;
    opens com.example.ece318_librarymanagementsys.database to javafx.fxml;
    opens com.example.ece318_librarymanagementsys.model to javafx.base;
    exports com.example.ece318_librarymanagementsys.controller.core;
    opens com.example.ece318_librarymanagementsys.controller.core to javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires com.github.librepdf.openpdf;

}