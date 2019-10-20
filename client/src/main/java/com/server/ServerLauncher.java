package com.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.Objects;

public class ServerLauncher extends Application {



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = null;
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/ServerUI.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        Scene mainScene = new Scene(root, 600, 350);
        mainScene.setRoot(root);
        mainScene.setFill(Color.TRANSPARENT);
        primaryStage.setResizable(false);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }



}
