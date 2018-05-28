package server;
import components.*;
import messages.TextMessage;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;

public class ChatServer {

    private int port;

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private final Set<Connection> connections = new CopyOnWriteArraySet<>();
    private final BlockingDeque<TextMessage> messageQueue = new LinkedBlockingDeque<>();
    byte [] header = {(byte) 0xAA, (byte) 0xAA};


    public ChatServer(int port) {
        this.port = port;
    }

    private void start() throws IOException {



        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on " + serverSocket);

            while (true) {

                Socket sock = serverSocket.accept();
                Connection con = new Connection(sock);
                connections.add(con);
                InputStream in = sock.getInputStream();
                byte[] buf = new byte[2];
                int read = in.read(buf);
                while (read < 2 && read != -1) {
                    read = in.read(buf, read, buf.length - read);
                }

                if (Arrays.equals(buf, header)) {
                    System.out.println("Все ОК");
                    new Thread(new Reader(sock)).start();
                }
                else {
                    System.out.println("Wrong header: " + Arrays.toString(buf));
                }

                //


                new Thread(new Writer()).start();
            }
        }

    }

    private class Reader implements Runnable {
        private final Socket socket;

        private Reader(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectInputStream objIn;

            try {
                objIn = new ObjectInputStream(socket.getInputStream());
                System.out.printf("%s connected\n", socket.getInetAddress().getHostAddress());

                while (!Thread.currentThread().isInterrupted()) {
                    TextMessage msg = (TextMessage) objIn.readObject();
                    messageQueue.add(msg);
                    printMessage(msg);
                }

            }
            catch (IOException e) {
                System.err.println("Disconnected " + socket.getInetAddress().getHostAddress());
            }
            catch (ClassNotFoundException e) {
                throw new ChatUncheckedException("Error de-serializing components", e);
            }
            finally {
                connections.removeIf(connection -> connection.socket == socket);
                IOUtils.closeQuietly(socket);
            }
        }
    }

    private class Writer implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName("Writer");

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    TextMessage msg = messageQueue.take();

                    for (Connection connection : connections) {
                        try {
                            connection.objOut.writeObject(msg);
                            connection.objOut.flush();
                        }
                        catch (IOException e) {
                            System.err.printf("Error sending components %s to %s\n", msg, connection.socket);

                            connections.remove(connection);
                            IOUtils.closeQuietly(connection.socket);
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                throw new ChatUncheckedException("Writer was interrupted", e);
            }
        }
    }

    private static class Connection {
        final Socket socket;
        final ObjectOutputStream objOut;

        Connection(Socket socket) throws IOException {
            this.socket = socket;
            this.objOut = new ObjectOutputStream(socket.getOutputStream());
        }
    }

    private void printMessage(TextMessage msg) {
        System.out.printf("%s: %s => %s\n", FORMAT.format(new Date(msg.getTimestamp())), msg.getSender(), msg.getText());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("Port must be specified");
        int port = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port);
        chatServer.start();
    }
}