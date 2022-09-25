package com.videotheatre;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class TheatreController implements Initializable {
    private static Point2D mouseLocation = null;

    private static boolean isFirstRunMouse = true;

    private static List<String> videoQueue;

    private static List<TilePane> grids;

    private String getNewVideoPath() {
        // unique vids
        var randomIndex = Utilities.generateRandomNumber(0, videoQueue.size() - 1);
        var filePath = videoQueue.get(randomIndex);

        if (Settings.removeWatchedVideosFromList) {
            videoQueue.remove(randomIndex);
        }

        System.out.printf("Loading video: %s%n", filePath);
        return filePath;
    }

    private MediaView generateMediaView() throws Exception {
        if (videoQueue == null) {
            throw new Exception("video queue is empty!");
        }

        var mediaView = new MediaView();
        mediaView.setMediaPlayer(generateMediaPlayer(mediaView));
        mediaView.setPreserveRatio(!Settings.stretchVideoToGrid);
        return mediaView;
    }

    private MediaPlayer generateMediaPlayer(MediaView view) throws Exception {
        var filePath = getNewVideoPath();

        var player = new MediaPlayer( new Media(new File(filePath).toURI().toURL().toString()));
        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (Settings.loopVideo) {
                    player.seek(Duration.ZERO);
                    player.play();
                } else {
                    try {
                        player.dispose();
                        view.setMediaPlayer(generateMediaPlayer(view));
                    } catch (Exception ex) {
                        System.err.println(ex);
                    }
                }
            }
        });

        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                player.play();
            }
        });

        player.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.printf("UNABLE TO LOAD VIDEO..! %s\n.", player.getMedia().getSource());
                try {
                    view.setMediaPlayer(generateMediaPlayer(view));
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        });

        player.setMute(true);

        return player;
    }

    public void loadMedia() throws Exception {
        videoQueue = new ArrayList<String>();

        for (var videoDirectory: Settings.videoDirectories) {
            var videoPath = videoDirectory.trim();
            if (videoPath.equals("")) {
                continue;
            }

            try (Stream<Path> pathStream = Files.walk(Paths.get(videoPath))
                    .filter(Files::isRegularFile)) {

                for (Path file : (Iterable<Path>) pathStream::iterator) {
                    // something that throws IOException
                    var path = file.toString();
                    if (path.endsWith(".mp4")) { // TODO: support more video formats
                        videoQueue.add(file.toString());
                    }
                }
            }
        }
    }

    public void render(Stage primaryStage) throws Exception {
        var utilities = new Utilities();
        grids = new ArrayList<TilePane>();

        loadMedia();
        boolean primaryStageUsed = false;

        for(Screen screen : Screen.getScreens()) {
            Stage currentStage;
            TilePane grid;

            if(!primaryStageUsed) {
                primaryStageUsed = true;
                currentStage = primaryStage;
            } else {
                currentStage = new Stage();
                currentStage.initOwner(primaryStage); // allow multiple app windows to be on top of all displays and windows in them
            }

            currentStage.setTitle ("Video Theatre");
            currentStage.toFront();

            if (Screen.getPrimary().equals(screen)) {
                currentStage.setAlwaysOnTop(true);
            }

            var screenBounds = screen.getBounds();

            grid = new TilePane();
            grids.add(grid);

            grid.setHgap(0);
            grid.setVgap(0);
            grid.setPrefRows(Settings.rowCount);
            grid.setPrefColumns(Settings.columnCount);
            grid.setPrefTileWidth((screenBounds.getWidth() / Settings.columnCount));
            grid.setPrefTileHeight((screenBounds.getHeight() / Settings.rowCount));

            grid.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight()); // Default width and height
            grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            int videoCount = videoQueue.size();
            int tileCount = Settings.rowCount * Settings.columnCount;
            int totalTileCount = tileCount * Screen.getScreens().size();
            System.out.println(String.format("Need to load %d video tiles.. have %d video files...", totalTileCount, videoCount));
            if(videoCount >= totalTileCount) {
                // Get unique videos since the video collection is larger than the anticipated tile count
                System.out.println("Will load unique videos..!");
            } else {
                System.out.println("Will load random videos that may repeat..! Add more videos if you want a unique video per tile.");
            }

            for(int j = 0; j < tileCount; j++) {
                MediaView mediaView = generateMediaView();
                mediaView.fitWidthProperty().bind(grid.prefTileWidthProperty());
                mediaView.fitHeightProperty().bind(grid.prefTileHeightProperty());

                grid.getChildren().add(mediaView);
            }

            var sceneMain = new Scene(grid);

            sceneMain.widthProperty().addListener(new ChangeListener<Number>() {
                @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                    //System.out.println("Width: " + newSceneWidth);
                    grid.setPrefTileWidth((newSceneWidth.intValue() / Settings.columnCount));
                }
            });

            sceneMain.heightProperty().addListener(new ChangeListener<Number>() {
                @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                    grid.setPrefTileHeight((newSceneHeight.intValue() / Settings.rowCount));
                }
            });

            currentStage.initStyle(StageStyle.UNDECORATED);
            currentStage.setFullScreen(true);
            currentStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Disable fullscreen exit message

            sceneMain.setCursor(Cursor.NONE);
            sceneMain.setFill(Color.BLACK);

            currentStage.setX(screenBounds.getMinX());
            currentStage.setY(screenBounds.getMinY());
            currentStage.setWidth(screenBounds.getWidth());
            currentStage.setHeight(screenBounds.getHeight());

            sceneMain.addEventFilter(MouseEvent.MOUSE_MOVED , new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(isFirstRunMouse) {
                        isFirstRunMouse = false;
                        mouseLocation = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                    } else {
                        if(mouseLocation != null) {
                            if(mouseEvent.getX() >= mouseLocation.getX()+20 || mouseEvent.getX() >= mouseLocation.getX()-20
                                    || mouseEvent.getY() >= mouseLocation.getY()+20 || mouseEvent.getY() >= mouseLocation.getY()-20) {

                                //App.exitApplication();
                            }
                        }
                    }
                }
            });

            sceneMain.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.ESCAPE)) {
                    exitApplication();
                } else if (event.getCode().equals(KeyCode.BACK_SPACE)) {
                    setAllMediaPlayersPlayPause();
                } else if (event.getCode().equals(KeyCode.SPACE)) {
                    try {
                        loadAllNewVideos();
                    } catch (Exception ex) {
                        // TODO
                    }
                } else if (event.getCode().equals(KeyCode.R)) {
                    // toggle repeat mode
                    Settings.loopVideo = !Settings.loopVideo;
                } else {
                    // check for number press

                    var str = event.getText();

                    if (str.matches(".*\\d.*")) {
                        var num = Integer.parseInt(str);
                        if (num == 0) {
                            num = 10;
                        }

//                    // TODO,fix for workstations with more than 1-3 monitors
                        var targetGrid = 0;
                        var targetVideoIdx = num - 1;
//
//                    if (targetVideoIdx + 1> tileCount) {
//                        targetGrid = 1;
//                        targetVideoIdx -= tileCount;
//                    }

                        if (event.isControlDown()) {
                            targetGrid = 1;
                        }

                        if (event.isAltDown()) {
                            targetGrid = 2;
                        }

                        try {
                            if (event.isShiftDown()) {
                                // toggle mute for that video
                                var view = (MediaView)grids.get(targetGrid).getChildren().get(targetVideoIdx);
                                setAllMediaPlayersToMute();
                                view.getMediaPlayer().setMute(!view.getMediaPlayer().isMute());
                            } else {
                               setNewMediaPlayer(targetGrid, targetVideoIdx);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

            currentStage.setScene(sceneMain);
            currentStage.show();
        }

        var timer = new Timer();
        var task = new TimerTask() {

            @Override
            public void run() {
                checkAllMediaPlayersHalted();
            }
        };
        timer.schedule(task, 3000);
    }

    private void setNewMediaPlayer(int targetGrid, int targetVideoIdx)  throws Exception {
        try {
            // change video out
            var view = (MediaView)grids.get(targetGrid).getChildren().get(targetVideoIdx);
            view.getMediaPlayer().dispose();
            var mediaView = generateMediaView();
            mediaView.fitWidthProperty().bind(grids.get(targetGrid).prefTileWidthProperty());
            mediaView.fitHeightProperty().bind(grids.get(targetGrid).prefTileHeightProperty());

            grids.get(targetGrid).getChildren().set(targetVideoIdx, mediaView);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loadAllNewVideos() throws Exception {
        for (var gridIdx = 0; gridIdx < grids.size(); gridIdx++) {
            for (var videoIdx = 0; videoIdx < grids.get(gridIdx).getChildren().size(); videoIdx++) {
                setNewMediaPlayer(gridIdx, videoIdx);
            }
        }
    }

    private void setAllMediaPlayersToMute() {
        for (var grid : grids) {
            for (var child : grid.getChildren()) {
                var view = (MediaView)child;
                view.getMediaPlayer().setMute(true);
            }
        }
    }

    private void setAllMediaPlayersPlayPause() {
        for (var grid : grids) {
            for (var child : grid.getChildren()) {
                var view = (MediaView)child;
                var mediaPlayer = view.getMediaPlayer();

                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            }
        }
    }

    private void checkAllMediaPlayersHalted() {
        for (var grid : grids) {
            for (var child : grid.getChildren()) {
                var view = (MediaView)child;
                var mediaPlayer = view.getMediaPlayer();

                if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING &&
                    mediaPlayer.getStatus() != MediaPlayer.Status.PAUSED) {

                    System.out.printf("detected issue with video... status is: %s, trying to play again\n", mediaPlayer.getStatus().toString());
                    mediaPlayer.play();
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public static void exitApplication() {
        Platform.exit();
        System.exit(0);
    }
}
