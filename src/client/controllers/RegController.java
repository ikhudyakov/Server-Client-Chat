package client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.ChatClient;
import components.IOUtils;
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
    private TextField login_field;

    @FXML
    private PasswordField password_field;

    @FXML
    private PasswordField repeat_password_field;

    @FXML
    private Button enterButton;

    @FXML
    private Label error_label;

    private Status status;
    private String login;
    private String password;
    private ChatClient chatClient;

    public void setLogin(String login) {
        this.login = login;
    }

    @FXML
    void initialize() {

        chatClient = Controller.getChatClient();

        enterButton.setOnAction(event -> {
            reg();
        });

        login_field.setOnAction(event -> {
            reg();
        });

        password_field.setOnAction(event -> {
            reg();
        });

        repeat_password_field.setOnAction(event -> {
            reg();
        });

    }

    private void reg() {
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
                    newForm(enterButton.getScene(), "/chat.fxml");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void newForm(Scene scene, String s) {
        scene.getWindow().hide();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(s));


        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> closeProgram());
        stage.showAndWait();
    }

    private void closeProgram(){
        IOUtils.closeQuietly(chatClient.getSocket());
        System.out.println("EXIT");
        System.exit(0);
    }
}


