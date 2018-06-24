package client.controllers;

import client.ChatClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

import static client.ChatClient.parseAddress;
import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    public TextField login_field;

    @FXML
    public PasswordField password_field;

    @FXML
    public Button enterButton;

    @FXML
    public Button registrationButton;

    @FXML
    public void initialize() throws IOException, InterruptedException {

        ChatClient chatClient = new ChatClient(ChatClient.parseAddress("127.0.0.1:8081"), new Scanner(System.in));
        chatClient.start();

        enterButton.setOnAction(event -> {
            String login = login_field.getText().toLowerCase().trim();
            String password = password_field.getText().toLowerCase().trim();

            if (!login.equals("") && !password.equals("")){
                String msg = login + " " + password;
                //ChatClient.buildAndSendMessage(msg);
                System.out.println("Вы нажали кнопку Enter " + msg);
                try {
                    chatClient.authentication(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(chatClient.checkAuth) {// условие успешной авторизации, после проверки которой происходит переход в окно главного чата
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
                    stage.showAndWait();
                }
            } else
                System.out.println("Login or password is empty");
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
            stage.showAndWait();
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}

