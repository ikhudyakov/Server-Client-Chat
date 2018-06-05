package components;

import javafx.scene.shape.Path;
import messages.Messages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class History {
    public static void saveMessageInFile(int id, Messages msg) throws IOException {

        String path = "/history" + id + ".txt";

        File file = new File(path);
        if (file.createNewFile()) {
            System.out.println(file + " created");
        } else {

        }
        String rrr = msg.toString();
        try {
            Files.write(Paths.get(file.getPath()), rrr.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
