package server;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveFile {

    Socket socket;

    private void start() {

        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("Server started on " + serverSocket);

            while (true) {
                socket = serverSocket.accept();
                receiveFile("newFile");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String filename) {
        try {
            long s;

            BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            s = Long.parseLong(buffer.readLine());

            System.out.println("File size: " + s);
            byte[] byteArray = new byte[1024];
            new File("Received").mkdir();
            File file = new File("./Received/" + filename);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            while (s > 0) {
                int i = bis.read(byteArray);
                fos.write(byteArray, 0, i);
                s -= i;
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Receive IO Error");
        }
        System.out.println("Received " + filename);
    }


    public static void main(String[] args) throws IOException {

        ReceiveFile receiveFile = new ReceiveFile();
        receiveFile.start();
    }
}