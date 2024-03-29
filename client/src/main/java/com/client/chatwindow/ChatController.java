package com.client.chatwindow;

import com.client.login.MainLauncher;
import com.client.util.ImageUtil;
import com.client.util.VoicePlayback;
import com.client.util.VoiceRecorder;
import com.client.util.VoiceUtil;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import com.messages.User;
import com.messages.bubble.BubbleSpec;
import com.messages.bubble.BubbledLabel;
import com.traynotifications.animations.AnimationType;
import com.traynotifications.notification.TrayNotification;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;


public class ChatController implements Initializable {
    @FXML private Button imageBtn;
    @FXML public TextArea messageBox;
    @FXML private Label usernameLabel;
    @FXML private Label onlineCountLabel;
    @FXML private ListView userList;
    @FXML private ImageView userImageView;
    @FXML private Button recordBtn;
    @FXML ListView chatPane;
    @FXML ListView statusList;
    @FXML BorderPane borderPane;
    @FXML ComboBox statusComboBox;
    @FXML ImageView microphoneImageView;
    @FXML BorderPane topPane;
    @FXML ImageView closeBtn;
    @FXML ImageView settingBtn;

    Image microphoneActiveImage = new Image(getClass().getClassLoader().getResource("images/record-active.png").toString());
    Image microphoneInactiveImage = new Image(getClass().getClassLoader().getResource("images/record.png").toString());

    private double xOffset;
    private double yOffset;

    Logger logger = LoggerFactory.getLogger(ChatController.class);


    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();
        if (!messageBox.getText().isEmpty()) {
            Listener.send(msg);
            messageBox.clear();
        }
    }

    public void recordVoiceMessage() throws IOException {
        if (VoiceUtil.isRecording()) {
            Platform.runLater(() -> {
                microphoneImageView.setImage(microphoneInactiveImage);
                    }
            );
            VoiceUtil.setRecording(false);
        } else {
            Platform.runLater(() -> {
                microphoneImageView.setImage(microphoneActiveImage);

                    }
            );
            VoiceRecorder.captureAudio();
        }
    }




    public synchronized void addToChat(Message msg) {
        ImageUtil imageUtil = new ImageUtil();
        Task<HBox> othersMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                String channelSource = "images/alphabet/" + msg.getChannel().substring(0,1).toLowerCase() + ".png";
                Image image = new Image(getClass().getClassLoader().getResource(msg.getPicture()).toString());
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);

                Image channel = new Image(getClass().getClassLoader().getResource(channelSource).toString());
                ImageView channelImage = new ImageView(channel);
                channelImage.setFitWidth(16);
                channelImage.setFitHeight(16);

                BubbledLabel bl6 = new BubbledLabel();
                if (msg.getType() == MessageType.VOICE){
                    ImageView imageview = new ImageView(new Image(getClass().getClassLoader().getResource("images/sound.png").toString()));
                    bl6.setGraphic(imageview);
                    bl6.setText("Sent a voice message!");
                    VoicePlayback.playAudio(msg.getVoiceMsg());
                } else if (msg.getType() == MessageType.PICTURE) {

                    Platform.runLater(() -> {
                        ImageView picture;
                        imageUtil.imageDecode(msg.getPictureMsg());
                        if (imageUtil.decodedImage != null)
                            picture = new ImageView(imageUtil.decodedImage);
                        else
                            picture = new ImageView(microphoneActiveImage);
                        picture.setFitWidth(imageUtil.width);
                        picture.setFitHeight(imageUtil.height);
                        bl6.setGraphic(picture);
                        imageUtil.decodedImage = null;
                    });
                    logger.debug(imageUtil.width + " - " + imageUtil.height);
                }
                else {
                    bl6.setText(msg.getMsg());
                }
                bl6.setBackground(new Background(new BackgroundFill(Color.WHITE,null, null)));
                HBox x = new HBox();
                x.setMaxWidth(chatPane.getWidth() - 20);
                bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,
                        null, null)));
                x.getChildren().addAll(profileImage, channelImage, bl6);
                logger.debug("ONLINE USERS: " + Integer.toString(msg.getUserlist().size()));
                setOnlineLabel(Integer.toString(msg.getOnlineCount()));
                return x;
            }
        };

        othersMessages.setOnSucceeded(event -> {
            chatPane.getItems().add(othersMessages.getValue());
            chatPane.scrollTo(chatPane.getItems().size() - 1);
        });

        Task<HBox> yourMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                Image image = userImageView.getImage();
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);

                String channelSource = "images/alphabet/" + msg.getChannel().substring(0,1).toLowerCase() + ".png";
                Image channel = new Image(getClass().getClassLoader().getResource(channelSource).toString());
                ImageView channelImage = new ImageView(channel);
                channelImage.setFitWidth(16);
                channelImage.setFitHeight(16);

                BubbledLabel bl6 = new BubbledLabel();
                if (msg.getType() == MessageType.VOICE){
                    ImageView imageview = new ImageView(new Image(getClass().getClassLoader().getResource("images/sound.png").toString()));
                    bl6.setGraphic(imageview);
                    bl6.setText("Sent a voice message!");
                    VoicePlayback.playAudio(msg.getVoiceMsg());
                } else if (msg.getType() == MessageType.PICTURE) {
                    Platform.runLater(() -> {
                        ImageView picture;
                        imageUtil.imageDecode(msg.getPictureMsg());
                        if (imageUtil.decodedImage != null)
                            picture = new ImageView(imageUtil.decodedImage);
                        else
                            picture = new ImageView(microphoneActiveImage);
                        picture.setFitWidth(imageUtil.width);
                        picture.setFitHeight(imageUtil.height);
                        bl6.setGraphic(picture);
                        imageUtil.decodedImage = null;
                    });
                    logger.debug(imageUtil.width + " - " + imageUtil.height);
                }
                else {
                    bl6.setText(msg.getMsg());
                }
                bl6.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET,
                        null, null)));
                bl6.setTextFill(Color.WHITE);

                HBox x = new HBox();
                x.setMaxWidth(chatPane.getWidth() - 20);
                x.setAlignment(Pos.TOP_RIGHT);
                bl6.setBubbleSpec(BubbleSpec.FACE_RIGHT_CENTER);
                x.getChildren().addAll(bl6, channelImage, profileImage);

                setOnlineLabel(Integer.toString(msg.getOnlineCount()));
                return x;
            }
        };
        yourMessages.setOnSucceeded(event -> {
            chatPane.getItems().add(yourMessages.getValue());
            chatPane.scrollTo(chatPane.getItems().size() - 1);
        });

        if (msg.getName().equals(usernameLabel.getText())) {
            Thread t2 = new Thread(yourMessages);
            t2.setDaemon(true);
            t2.start();
        } else {
            Thread t = new Thread(othersMessages);
            t.setDaemon(true);
            t.start();
        }
    }
    public void setUsernameLabel(String username) {
        this.usernameLabel.setText(username);
    }


    public void setOnlineLabel(String usercount) {
        logger.info("setOnlineLabel()");
        Platform.runLater(() -> onlineCountLabel.setText(usercount));
    }

    public void setUserList(Message msg) {
        logger.info("setUserList() method Enter");
        Platform.runLater(() -> {
            ObservableList<User> users = FXCollections.observableList(msg.getUsers());
            users.removeIf(user -> user.getName().equals(Listener.username));
            users.add(0, Listener.community);
            userList.setItems(users);
            userList.setCellFactory(new CellRenderer());
            userList.getSelectionModel().select(0);
            setOnlineLabel(String.valueOf(msg.getUserlist().size()));
        });
        logger.info("setUserList() method Exit");
    }

    /* Displays Notification when a user joins */
    public void newUserNotification(Message msg) {
        Platform.runLater(() -> {
            Image profileImg = new Image(getClass().getClassLoader().getResource(msg.getPicture()).toString(),50,50,false,false);
            TrayNotification tray = new TrayNotification();
            tray.setTitle("A new user has joined!");
            tray.setMessage(msg.getName() + " has joined the JavaFX Chatroom!");
            tray.setRectangleFill(Paint.valueOf("#2C3E50"));
            tray.setAnimationType(AnimationType.POPUP);
            tray.setImage(profileImg);
            tray.showAndDismiss(Duration.seconds(5));
            try {
                Media hit = new Media(getClass().getClassLoader().getResource("sounds/notification.wav").toString());
                MediaPlayer mediaPlayer = new MediaPlayer(hit);
                mediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public void sendMethod(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            sendButtonAction();
        }
    }

    @FXML
    public void closeApplication() throws IOException {
        Platform.exit();
        System.exit(0);
    }

    /* Method to display server messages */
    public synchronized void addAsServer(Message msg) {
        Task<HBox> task = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                BubbledLabel bl6 = new BubbledLabel();
                bl6.setText(msg.getMsg());
                bl6.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE,
                        null, null)));
                HBox x = new HBox();
                bl6.setBubbleSpec(BubbleSpec.FACE_BOTTOM);
                x.setAlignment(Pos.CENTER);
                x.getChildren().addAll(bl6);
                return x;
            }
        };
        task.setOnSucceeded(event -> {
            chatPane.getItems().add(task.getValue());
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    public void generateAnimation(){
        Random rand = new Random();
        int sizeOfSquare = rand.nextInt(30) + 1;
        int speedOfSquare = rand.nextInt(10) + 5;
        int startXPoint = rand.nextInt(500) + 300;
        int startYPoint = rand.nextInt(40) + 10;
        int direction = rand.nextInt(5) + 1;

        KeyValue moveXAxis = null;
        KeyValue moveYAxis = null;
        Rectangle r1 = null;

        switch (direction){
            case 1 :
                // MOVE LEFT TO RIGHT
                r1 = new Rectangle(0,startYPoint,sizeOfSquare,sizeOfSquare);
                moveXAxis = new KeyValue(r1.xProperty(), 1040 -  sizeOfSquare);
                break;
            case 2 :
                // MOVE TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSquare,sizeOfSquare);
                moveYAxis = new KeyValue(r1.yProperty(), 80 - sizeOfSquare);
                break;
            case 3 :
                // MOVE LEFT TO RIGHT, TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSquare,sizeOfSquare);
                moveXAxis = new KeyValue(r1.xProperty(), 1040 -  sizeOfSquare);
                moveYAxis = new KeyValue(r1.yProperty(), 80 - sizeOfSquare);
                break;
            case 4 :
                // MOVE BOTTOM TO TOP
                r1 = new Rectangle(startXPoint,80-sizeOfSquare ,sizeOfSquare,sizeOfSquare);
                moveYAxis = new KeyValue(r1.xProperty(), 0);
                break;
            case 5 :
                // MOVE RIGHT TO LEFT
                r1 = new Rectangle(1040-sizeOfSquare,startYPoint,sizeOfSquare,sizeOfSquare);
                moveXAxis = new KeyValue(r1.xProperty(), 0);
                break;
            case 6 :
                //MOVE RIGHT TO LEFT, BOTTOM TO TOP
                r1 = new Rectangle(startXPoint,0,sizeOfSquare,sizeOfSquare);
                moveXAxis = new KeyValue(r1.xProperty(), 1040 -  sizeOfSquare);
                moveYAxis = new KeyValue(r1.yProperty(), 80 - sizeOfSquare);
                break;

            default:
                System.out.println("default");
        }

        r1.setFill(Color.web("#FDFFFC"));
        r1.setOpacity(0.2);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(speedOfSquare * 1000), moveXAxis, moveYAxis);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        topPane.getChildren().add(topPane.getChildren().size()-1,r1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        chatPane.setFocusTraversable(false);
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

        statusComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Listener.sendStatusUpdate(Status.valueOf(newValue.toUpperCase()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        /* Added to prevent the enter from adding a new line to inputMessageBox */
        messageBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    sendButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });


        /* Initialize user user list */
        userList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        userList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
            @Override
            public void changed(ObservableValue<? extends  User> observableValue, User prev, User curr) {
                if (curr != null && !Listener.channel.equals(curr.getName()))
                    try {
                        logger.info("Open connection to " + curr.getName());
                        Listener.sendChannelUpadte(curr.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });

        imageBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Platform.runLater(() -> {
                    final FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Select an image");
                    fileChooser.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png", "*.jpg"));

                    String userDirectoryString = System.getProperty("user.home");
                    fileChooser.setInitialDirectory(new File(userDirectoryString));

                    Stage stage = (Stage) borderPane.getScene().getWindow();

                    File file = fileChooser.showOpenDialog(stage);

                    if (file != null) {
                        logger.info(file.getPath());
                        ImageUtil.imageEncode(file.getAbsolutePath());
                    }
                    else logger.info("Filechooser cance");
                });
            }
        });

        int numberOfSquares = 40;
        while (numberOfSquares > 0){
            generateAnimation();
            numberOfSquares--;
        }

        settingBtn.translateZProperty().set(100);
        closeBtn.translateZProperty().set(100);

    }

    public void setImageLabel(String selectedPicture) {
        this.userImageView.setImage(new Image(getClass().getClassLoader().getResource(selectedPicture).toString()));
    }



    public void logoutScene() {
        Platform.runLater(() -> {
            FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent window = null;
            try {
                window = (Pane) fmxlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = MainLauncher.getPrimaryStage();
            Scene scene = new Scene(window);
            stage.setMaxWidth(350);
            stage.setMaxHeight(420);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
        });
    }
}