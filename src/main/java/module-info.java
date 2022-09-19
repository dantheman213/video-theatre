module com.videotheatre {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;


    opens com.videotheatre to javafx.fxml;
    exports com.videotheatre;
}