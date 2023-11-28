package com.knu.media.player;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SimpleMediaPlayer extends Application {
    private MediaPlayer mediaPlayer;
    private Slider timeSlider;
    private final Label currentTimeLabel = new Label("00:00");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MediaView mediaView = new MediaView();

        Button chooseFileButton = new Button("Choose File");
        chooseFileButton.setOnAction(e -> loadMedia(primaryStage, mediaView));

        TextField streamURLField = new TextField();
        streamURLField.setPromptText("Enter media URL...");
        streamURLField.setPrefWidth(100);

        Button streamButton = new Button("Load Stream");
        streamButton.setOnAction(e -> loadStream(streamURLField.getText(), mediaView));

        Button playButton = new Button("Play");
        playButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }
        });

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });

        Slider volumeSlider = new Slider(0, 1, 0.5); // Default volume is 50%
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().addListener(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volumeSlider.getValue());
            }
        });

        timeSlider = new Slider();
        timeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (timeSlider.isValueChanging()) {
                if (mediaPlayer != null) {
                    double preciseTime = Math.round(newValue.doubleValue() * 1000) / 1000.0;
                    mediaPlayer.seek(Duration.seconds(preciseTime));
                }
            }
        });

        timeSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                double preciseTime = Math.round(timeSlider.getValue() * 1000) / 1000.0;
                mediaPlayer.seek(Duration.seconds(preciseTime));
            }
        });

        timeSlider.setOnMouseClicked(event -> {
            if (mediaPlayer != null) {
                double mouseX = event.getX();
                double sliderWidth = timeSlider.getWidth();
                Duration duration = mediaPlayer.getTotalDuration();
                double toTime = duration.toSeconds() * (mouseX / sliderWidth);

                mediaPlayer.seek(Duration.seconds(toTime));
                timeSlider.setValue(toTime);
            }
        });

        HBox controls = new HBox(10, chooseFileButton, streamURLField, streamButton, playButton, pauseButton, stopButton, volumeSlider, timeSlider, currentTimeLabel);

        BorderPane root = new BorderPane();
        root.setCenter(mediaView);
        root.setBottom(controls);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Media Player");
        primaryStage.show();


        mediaView.fitWidthProperty().bind(root.widthProperty());
        mediaView.fitHeightProperty().bind(root.heightProperty().subtract(controls.heightProperty()));  // subtracting controls height
        mediaView.setPreserveRatio(true);
    }

    private void loadMedia(Stage stage, MediaView mediaView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.mp3", "*.avi", "*.mkv"));
        fileChooser.setTitle("Choose Media File");
        var selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            Media media = new Media(selectedFile.toURI().toString());
            loadMedia(media, mediaView);
        }
    }

    private void loadStream(String mediaURL, MediaView mediaView) {
        if (mediaURL == null || mediaURL.trim().isEmpty()) {
            System.out.println("Invalid media URL.");
            return;
        }
                    
        Media media = new Media(mediaURL);
        loadMedia(media, mediaView);
    }

    private void loadMedia(Media media, MediaView mediaView) {
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        mediaPlayer.setOnReady(() -> timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds()));

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newTime.toSeconds());
                currentTimeLabel.setText(formatTime(newTime, mediaPlayer.getTotalDuration()));
            }
        });
    }

    //duration time formatting section ---------------------------------------------------------------------------------

    private static String formatTime(Duration elapsed, Duration duration) {
        String elapsedStr = formatDuration(elapsed);
        if (duration.greaterThan(Duration.ZERO)) {
            String durationStr = formatDuration(duration);
            return elapsedStr + "/" + durationStr;
        }
        return elapsedStr;
    }

    private static String formatDuration(Duration duration) {
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int hours = totalSeconds / (60 * 60);
        int minutes = (totalSeconds % (60 * 60)) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

}
