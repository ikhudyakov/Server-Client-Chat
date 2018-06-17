package components;

import messages.TextMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class History {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d.MM.yyyy HH:mm");

    public static void saveMessageInDB(TextMessage msg){
        String mes = FORMAT.format(msg.getTimestamp()) + " : " + msg.getSender() + " > " +  msg.getText() + "\n";
        try (java.sql.Connection JDBCConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/chatdb",
                "admin", "1qaz2wsx")) {
            PreparedStatement prepared = JDBCConnection.prepareStatement("INSERT INTO HISTORY (ID_ROOM, TEXT) VALUES (?,?)");
            prepared.setInt(1, msg.getId());
            prepared.setString(2, mes);
            prepared.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveMessageInFile(TextMessage msg) throws IOException {

        String path = "history/history" +  msg.getId() + ".txt";

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
