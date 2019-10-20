package com.server;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    @FXML private ImageView closeIcon;
    @FXML private JFXButton startBtn;
    @FXML public TextField portField;
    @FXML private AnchorPane mainPane;
    Server server;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[1-9][0-9]*") || Integer.parseInt(newValue) < 1024 || Integer.parseInt(newValue) > 66535) startBtn.setDisable(true);
            else
                startBtn.setDisable(false);
        });
        portField.setText("9001");
        mainPane.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    if (!startBtn.isDisabled()) onButtonAction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });
    }


    public void closeSystem() throws IOException {
        closeServer();
        Platform.exit();
        System.exit(0);
    }

    public void onButtonAction() throws Exception {
        if (startBtn.getText().equals("Start")) {
            startServer(portField.getText());
        }
        else closeServer();
    }

    private void closeServer() throws IOException {
        if (server != null) server.getListener().close();
        startBtn.setText("Start");
        portField.setDisable(false);
    }

    private void startServer(String port) throws Exception {
        server = new Server(port);
        Thread x = new Thread(server);
        x.start();
        startBtn.setText("Stop");
        portField.setDisable(true);
    }
}
