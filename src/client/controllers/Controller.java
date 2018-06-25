package client.controllers;

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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class Controller {

    private static ChatClient client;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    public TextField login_field;

    @FXML
    public PasswordField password_field;

    @FXML
    public PasswordField repeat_password_field;

    @FXML
    public Button enterButton;

    @FXML
    public Button enterButton1;

    @FXML
    public Button registrationButton;

    @FXML
    public Label error_label;

    public static ChatClient getChatClient (){
        return client;
    }

    @FXML
    public void initialize() throws IOException, InterruptedException {

        ChatClient chatClient = new ChatClient(ChatClient.parseAddress("127.0.0.1:8081"), new Scanner(System.in));
        client = chatClient;
        chatClient.start();

        enterButton.setOnAction(event -> {

            String login = login_field.getText().toLowerCase().trim();
            String password = password_field.getText().toLowerCase().trim();


            if (!login.equals("") && !password.equals("")) {
                String msg = login + " " + password;
                try {
                    chatClient.authentication(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else error_label.setText("ERROR");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (chatClient.checkAuth) {
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

            } else error_label.setText("ERROR");
        });

        registrationButton.setOnAction(event -> {
            registrationButton.getScene().getWindow().hide();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/registration.fxml"));


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

        });


        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}

