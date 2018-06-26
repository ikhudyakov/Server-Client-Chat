package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/start.fxml"));
        primaryStage.setTitle("ChatTick");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> closeProgram());
        primaryStage.show();
    }


    private void closeProgram(){
        System.out.println("EXIT");
        System.exit(0);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        launch(args);

        String address = null;

        if (args != null && args.length > 0)
            address = args[0];

        Scanner scanner = new Scanner(System.in);

//        if (address == null) {
//            //System.out.println("Enter server address");
//            address = scanner.nextLine();
//        }

        //ChatClient client = new ChatClient(parseAddress(address), scanner);
        //client.start();
    }
}
