package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

public class Main extends Application {

    Stage window;
    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/start.fxml"));
        window.setTitle("ChatTick");
        window.setScene(new Scene(root, 600, 400));
        window.setResizable(false);
        window.show();

        window.setOnCloseRequest(e -> closeProgram());
    }

    private void closeProgram(){
        window.close();
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
