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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class TheatreController implements Initializable {
    private static Point2D mouseLocation = null;

    private static boolean isFirstRunMouse = true;

    private static List<String> videoQueue;
    private static List<String> favoriteQueue;

    private static List<TilePane> grids;

    private boolean buttonF_isPressed;

    private String getNewVideoPath() {
        // unique vids
        var randomIndex = -1;
        var filePath = "";

        if (Settings.loadFavorites && favoriteQueue.size() > 0) {
            randomIndex = Utilities.generateRandomNumber(0, favoriteQueue.size() - 1);
            filePath = favoriteQueue.get(randomIndex);

            if (Settings.removeWatchedVideosFromList) {
                favoriteQueue.remove(randomIndex);
            }
        } else {
            randomIndex = Utilities.generateRandomNumber(0, videoQueue.size() - 1);
            filePath = videoQueue.get(randomIndex);

            if (Settings.removeWatchedVideosFromList) {
                videoQueue.remove(randomIndex);
            }
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
                try {
                    System.out.printf("UNABLE TO LOAD VIDEO..! %s\n.", Utilities.decodeMediaSourceUrlToFilePath(player.getMedia().getSource()));
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

        favoriteQueue = new ArrayList<String>();
        for (var item : Settings.favoriteList) {
            favoriteQueue.add(item);
        }
        //Collections.copy(favoriteQueue, Settings.favoriteList);
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

            Scene sceneMain = new Scene(grid);

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
                if (event.getCode().equals(KeyCode.F)) {
                    buttonF_isPressed = true;
                }

                if (event.getCode().equals(KeyCode.ESCAPE)) {
                    exitApplication();
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

                        // TODO,fix for workstations with more than 1-3 monitors
                        var targetGrid = 0;
                        var targetVideoIdx = num - 1;

                        if (event.isControlDown()) {
                            targetGrid = 1;
                        }

                        if (event.isAltDown()) {
                            targetGrid = 2;
                        }

                        try {
                            var view = (MediaView)grids.get(targetGrid).getChildren().get(targetVideoIdx);

                            if (event.isShiftDown()) {
                                // toggle mute for that video
                                setAllMediaPlayersToMute();
                                view.getMediaPlayer().setMute(!view.getMediaPlayer().isMute());
                            } else if (buttonF_isPressed) {
                                // TODO: show favorite was added
                                Settings.favoriteList.add(Utilities.decodeMediaSourceUrlToFilePath(view.getMediaPlayer().getMedia().getSource()));
                                Settings.saveSettingsToFile();
                            } else {
                                // change video out
                                view.getMediaPlayer().dispose();
                                var mediaView = generateMediaView();
                                mediaView.fitWidthProperty().bind(grids.get(targetGrid).prefTileWidthProperty());
                                mediaView.fitHeightProperty().bind(grids.get(targetGrid).prefTileHeightProperty());

                                grids.get(targetGrid).getChildren().set(targetVideoIdx, mediaView);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

            sceneMain.setOnKeyReleased(event -> {
                if (event.getCode().equals(KeyCode.F)) {
                    buttonF_isPressed = false;
                }
            });

            sceneMain.setFill(Color.BLACK);

            currentStage.setScene(sceneMain);
            currentStage.show();
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public static void exitApplication() {
        Platform.exit();
        System.exit(0);
    }
}
