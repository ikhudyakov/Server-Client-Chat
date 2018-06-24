package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SendFile {
    private Socket socket;
    private SocketAddress serverAddress;
    private ObjectOutputStream objOut;


    private void openConnection() throws IOException {
        socket = new Socket();
        serverAddress = new InetSocketAddress("127.0.0.1", 8081);
        socket.connect(serverAddress);
        System.out.println("Start socket");
    }

    public static void main(String[] args) throws IOException {
        SendFile sendFile = new SendFile();
        sendFile.openConnection();
        File file = new File("files/file.txt");
        sendFile.sendFile(file);

    }

    public void sendFile(final File file) {
        Runnable r = () -> {
            System.out.println("Sending " + file.getName() + "...");
            try {
                byte[] byteArray = new byte[1024];
                FileInputStream fis = new FileInputStream(file.getPath());
                long s;
                s = file.length();
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                Thread.sleep(500);
                while (s > 0) {
                    int i = fis.read(byteArray);
                    bos.write(byteArray, 0, i);
                    s-= i;
                }
                bos.flush();
                fis.close();
            } catch (FileNotFoundException e) {
                System.err.println("File not found!");
            } catch (IOException e) {
                System.err.println("IOException");
            } catch (Exception e) {

            }
        };
        new Thread(r).start();
    }
}
