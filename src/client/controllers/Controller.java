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

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField login_field;

    @FXML
    private PasswordField password_field;

    @FXML
    private Button enterButton;

    @FXML
    private Button registrationButon;

    @FXML
    void initialize() {
        enterButton.setOnAction(event -> {
            String login = login_field.getText().trim();
            String password = password_field.getText().trim();

            if (!login.equals("") && !password.equals("")){
                String msg = login + " " + password;
                //ChatClient.buildAndSendMessage(msg);
                System.out.println("Вы нажали кнопку Enter " + msg);

                if(true) {// условие успешной авторизации, после проверки которой происходит переход в окно главного чата
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

        registrationButon.setOnAction(event -> {
            registrationButon.getScene().getWindow().hide();

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

