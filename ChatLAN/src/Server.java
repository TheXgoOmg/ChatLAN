import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    final static String IP = "127.0.0.1";
    ServerSocket serverSocket = null;
    Socket socket = null;
    DataInputStream input = null;


    public Server(String ip, int port) throws IOException {
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
        try  {
            System.out.printf("Server started%n");
            System.out.printf("Waiting for clients...%n");
            while (true) {
                socket = serverSocket.accept();
                new SockerFunctions(socket).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("%nServer stopped");
        }
    }
    public static void main(String[] args) throws IOException {
        Server server = new Server(IP,5000);
    }
    class SockerFunctions extends Thread {
        private Socket socket = null;
        public SockerFunctions(Socket socket) throws IOException {
            this.socket = socket;
        }
        @Override
        public void run() {
            String nombreClient = null;
            try (BufferedReader input = new BufferedReader (new InputStreamReader(socket.getInputStream()))) {
                nombreClient = input.readLine();
                System.out.printf("%nClient %s connected%n", nombreClient);
                while(true) {
                    String message = input.readLine();
                    if (message.equals("/q")) {
                        break;
                    }
                    System.out.printf("%n%s: %s%n", nombreClient, message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (socket != null) socket.close();
                    System.out.printf("%nClient %s disconnected%n", nombreClient);
                } catch (IOException e) {
                    System.out.println("%nError cerrando la conexión: " + e.getMessage());
                }
            }
        }
    }
}
