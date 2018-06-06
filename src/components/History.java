package components;

import javafx.scene.shape.Path;
import messages.Messages;
import messages.TextMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class History {

    public static void saveMessageInFile(TextMessage msg) throws IOException {


        String path = "history" +  msg.getId() + ".txt";

        // проверка, существует ли файл с именем path
        // если да, то записать в конец файла msg.getText()
        // если нет, то создать файл с path и записать в него msg.getText()

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
