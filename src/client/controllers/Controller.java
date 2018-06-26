package client.controllers;

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
    public TextField login_field;

    @FXML
    public PasswordField password_field;

    @FXML
    public Button enterButton;

    @FXML
    public Button registrationButton;

    @FXML
    public Label error_label;

    public static ChatClient getChatClient (){
        return client;
    }

    private ChatClient chatClient;

    @FXML
    public void initialize() throws IOException, InterruptedException {

        chatClient = new ChatClient(ChatClient.parseAddress("127.0.0.1:8081"), new Scanner(System.in));
        client = chatClient;
        chatClient.start();

        enterButton.setOnAction(event -> {
            auth();
        });

        login_field.setOnAction(event -> {
            auth();
        });

        password_field.setOnAction(event -> {
            auth();
        });

        registrationButton.setOnAction(event -> {
            newForm(registrationButton.getScene(), "/registration.fxml");
        });
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

    private void auth() {
        String login = login_field.getText().toLowerCase().trim();
        String password = password_field.getText().toLowerCase().trim();


        if (!login.equals("") && !password.equals("")) {
            String msg = login + " " + password;
            try {
                chatClient.authentication(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Status status = chatClient.getStatus();
        if (status.getStatusCode() == 1)
            error_label.setText("Success");
        if (status.getStatusCode() == 2)
            error_label.setText("incorrect login");
        if (status.getStatusCode() == 3)
            error_label.setText("incorrect password");

        if (chatClient.checkAuth) {
            newForm(enterButton.getScene(), "/chat.fxml");
        }
    }

    private void closeProgram(){
        IOUtils.closeQuietly(chatClient.getSocket());
        System.out.println("EXIT");
        System.exit(0);
    }
}

