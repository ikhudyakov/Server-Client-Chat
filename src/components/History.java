package components;

import messages.Messages;

import java.io.File;
import java.io.IOException;

public class History {
    public static void saveMessageInFile(int id, Messages msg) throws IOException {

        String path = "history" + id + ".txt";

        File file = new File(path);
            if (file.createNewFile() ){
                System.out.println(file + " created");
            } else {

            }
    }
}
