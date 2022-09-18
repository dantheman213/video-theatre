module com.videotheatre {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.videotheatre to javafx.fxml;
    exports com.videotheatre;
}