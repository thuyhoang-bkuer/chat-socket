package com.client.login;

import com.client.chatwindow.ChatController;
import com.client.chatwindow.Listener;
import com.client.util.ResizeHelper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 *  Created by Dominic on 12-Nov-15.
 */
public class LoginController implements Initializable {
    @FXML private ImageView imageView;
    @FXML private Button connectBtn;
    @FXML public  TextField hostnameTextfield;
    @FXML private TextField portTextfield;
    @FXML private TextField usernameTextfield;
    public static ChatController controller;
    @FXML private BorderPane borderPane;


    private double xOffset;
    private double yOffset;
    private Scene scene;
    private String imageSource;

    private static LoginController instance;

    public LoginController() {
        instance = this;
    }

    public static LoginController getInstance() {
        return instance;
    }
    public void loginButtonAction() throws IOException {
        String hostname = hostnameTextfield.getText();
        int port = Integer.parseInt(portTextfield.getText());
        String username = usernameTextfield.getText();
        String picture = usernameTextfield.getText().substring(0,1);

        FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/ChatView.fxml"));
        Parent window = (Pane) fmxlLoader.load();
        controller = fmxlLoader.<ChatController>getController();
        Listener listener = new Listener(hostname, port, username, imageSource, controller);
        Thread x = new Thread(listener);
        x.start();
        this.scene = new Scene(window);
    }

    public void showScene() throws IOException {
        Platform.runLater(() -> {
            Stage stage = (Stage) hostnameTextfield.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(1040);
            stage.setHeight(620);


            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setMinWidth(800);
            stage.setMinHeight(300);
            ResizeHelper.addResizeListener(stage);
            stage.centerOnScreen();
            controller.setUsernameLabel(usernameTextfield.getText());
            controller.setImageLabel(imageSource);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectBtn.setDisable(true);
        usernameTextfield.textProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue.equals("")) connectBtn.setDisable(true);

            if (!newValue.substring(0,1).matches("[a-zA-Z]*")) {
                imageSource = "images/default.png";
            }
            else imageSource = "images/alphabet/" + newValue.substring(0,1).toLowerCase() + ".png";
            imageView.setImage(new Image(getClass().getClassLoader().getResource(imageSource).toString()));
            connectBtn.setDisable(false);
        });
        /* Drag and Drop */
        borderPane.setOnMousePressed(event -> {
            xOffset = MainLauncher.getPrimaryStage().getX() - event.getScreenX();
            yOffset = MainLauncher.getPrimaryStage().getY() - event.getScreenY();
            borderPane.setCursor(Cursor.CLOSED_HAND);
        });

        borderPane.setOnMouseDragged(event -> {
            MainLauncher.getPrimaryStage().setX(event.getScreenX() + xOffset);
            MainLauncher.getPrimaryStage().setY(event.getScreenY() + yOffset);

        });

        borderPane.setOnMouseReleased(event -> {
            borderPane.setCursor(Cursor.DEFAULT);
        });

        /* Added to prevent the enter from adding a new line to inputMessageBox */
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    if (!connectBtn.isDisabled()) loginButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });

        int numberOfSquares = 30;
        while (numberOfSquares > 0){
            generateAnimation();
            numberOfSquares--;
        }
    }


    /* This method is used to generate the animation on the login window, It will generate random ints to determine
     * the size, speed, starting points and direction of each square.
     */
    public void generateAnimation(){
        Random rand = new Random();
        int sizeOfSqaure = rand.nextInt(50) + 1;
        int speedOfSqaure = rand.nextInt(10) + 5;
        int startXPoint = rand.nextInt(420);
        int startYPoint = rand.nextInt(350);
        int direction = rand.nextInt(5) + 1;

        KeyValue moveXAxis = null;
        KeyValue moveYAxis = null;
        Rectangle r1 = null;

        switch (direction){
            case 1 :
                // MOVE LEFT TO RIGHT
                r1 = new Rectangle(0,startYPoint,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSqaure);
                break;
            case 2 :
                // MOVE TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSqaure,sizeOfSqaure);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
                break;
            case 3 :
                // MOVE LEFT TO RIGHT, TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSqaure);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
                break;
            case 4 :
                // MOVE BOTTOM TO TOP
                r1 = new Rectangle(startXPoint,420-sizeOfSqaure ,sizeOfSqaure,sizeOfSqaure);
                moveYAxis = new KeyValue(r1.xProperty(), 0);
                break;
            case 5 :
                // MOVE RIGHT TO LEFT
                r1 = new Rectangle(420-sizeOfSqaure,startYPoint,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 0);
                break;
            case 6 :
                //MOVE RIGHT TO LEFT, BOTTOM TO TOP
                r1 = new Rectangle(startXPoint,0,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSqaure);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
                break;

            default:
                System.out.println("default");
        }

        r1.setFill(Color.web("#FDFFFC"));
        r1.setOpacity(0.2);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(speedOfSqaure * 1000), moveXAxis, moveYAxis);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        borderPane.getChildren().add(borderPane.getChildren().size()-1,r1);
    }

    /* Terminates Application */
    public void closeSystem(){
        Platform.exit();
        System.exit(0);
    }

    public void minimizeWindow(){
        MainLauncher.getPrimaryStage().setIconified(true);
    }

    /* This displays an alert message to the user */
    public void showErrorDialog(String message) {
        Platform.runLater(()-> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText(message);
            alert.setContentText("Please check for firewall issues and check if the server is running.");
            alert.showAndWait();
        });

    }
}
