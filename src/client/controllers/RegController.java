package client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.ChatClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import messages.Status;

public class RegController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField login_field;

    @FXML
    private PasswordField password_field;

    @FXML
    private PasswordField repeat_password_field;

    @FXML
    private Button enterButton;

    @FXML
    private TextField email_field;

    @FXML
    private Label error_label;

    Status status;
    private String login;
    private String password;

    @FXML
    void initialize() {

        ChatClient chatClient = Controller.getChatClient();

        enterButton.setOnAction(event1 -> {
            login = login_field.getText().toLowerCase().trim();
            password = password_field.getText().toLowerCase().trim();
            String repeat_password = repeat_password_field.getText().toLowerCase().trim();
            if (!login.equals("") && !password.equals("")) {
                if (password.equals(repeat_password)) {
                    String msg = login + " " + password;
                    try {
                        chatClient.registration(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else error_label.setText("check password");
            } else error_label.setText("empty field");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            status = chatClient.getStatus();
            if (status.getStatusCode() == 7)
                error_label.setText("login " + status.getLogin() + " already exists");
            if (status.getStatusCode() == 6) {
                error_label.setText("Successful registration");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (chatClient.checkReg) {
                    String msg = login + " " + password;
                    try {
                        chatClient.authentication(msg);
                        enterButton.getScene().getWindow().hide();

                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/chat.fxml"));

                        try {
                            loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Parent root = loader.getRoot();
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setResizable(false);
                        stage.showAndWait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}


