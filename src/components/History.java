package components;

import javafx.scene.shape.Path;
import messages.Messages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class History {
    public static void saveMessageInFile(int id, Messages msg) throws IOException {

        String path = "history" + id + ".txt";

        File file = new File(path);
        if (file.createNewFile()) {
            System.out.println(file + " created");
        } else {

        }

//        FileWriter fileWriter = new FileWriter(path, true);
//        fileWriter.append(msg)

  /*      String path = "history" + id + ".txt";
        String textMess = msg.toString();

        if ((new File(path)).exists()){
            FileWriter file = new FileWriter(path, true);
            file.append(textMess);
        } else {
            File file = new File(path);
            System.out.println(file + " created");
        }*/

        String textMess = msg.toString();
        try {
            Files.write(Paths.get(file.getPath()), textMess.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
