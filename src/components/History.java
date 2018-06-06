package components;

import messages.TextMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class History {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");

    public static void saveMessageInFile(TextMessage msg) throws IOException {

        String path = "history\\history" +  msg.getId() + ".txt";

        String mes = FORMAT.format(msg.getTimestamp()) + " : " + msg.getSender() + " > " +  msg.getText() + "\n";
        File file = new File(path);
        FileWriter fileWriter = new FileWriter(path, true);
        if (file.exists()){
            fileWriter.append(mes);
            fileWriter.flush();
            fileWriter.close();
        } else {
            file.createNewFile();
            fileWriter.append(mes);
            fileWriter.flush();
            fileWriter.close();
        }
    }
}
