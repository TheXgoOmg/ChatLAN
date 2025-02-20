package Server;


import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket serverSocket = null;
    Socket socket = null;
    DataInputStream input = null;


    public Server(String ip, int port) throws IOException {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            System.out.println("Server started");
            System.out.println("Waiting for clients...");
            while(true) {
                socket = serverSocket.accept();
                System.out.println("Client connected");
                input = new DataInputStream(socket.getInputStream());
                String message = input.readUTF();
                System.out.println();
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws IOException {
        Server server = new Server("0.0.0.0",5000);
    }
}
