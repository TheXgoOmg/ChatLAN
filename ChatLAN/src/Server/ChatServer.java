package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(0,50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Server started");
            ArrayList<Socket> sockets = new ArrayList<>();
            while (true) {
                Socket socket = serverSocket.accept();
                sockets.add(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
