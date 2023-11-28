module com.knu.media.player {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;

    opens com.knu.media.player to javafx.fxml;
    exports com.knu.media.player;
}