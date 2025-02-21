import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    final static String IP = "127.0.0.1";
    private ServerSocket serverSocket = null;
    Socket socket = null;
    ArrayList<PrintWriter> printWriters = new ArrayList<>();
    static long id = 0;


    public Server(String ip, int port) throws IOException {
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
        try  {
            System.out.printf("Server started%n");
            System.out.printf("Waiting for clients...%n");
            while (true) {
                socket = serverSocket.accept();
                printWriters.add(new PrintWriter (socket.getOutputStream(), true));
                new SocketFunctions(socket,id).start();
                id++;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("%nServer stopped");
        }
    }
    public static void main(String[] args) throws IOException {
        Server server = new Server(IP,5000);
    }
    class SocketFunctions extends Thread {
        private Socket socket = null;
        private long id;

        // Getters
        @Override
        public long getId() {
            return id;
        }

        public SocketFunctions(Socket socket, long id) throws IOException {
            this.socket = socket;
            this.id = id;
        }
        @Override
        public void run() {
            String nombreClient = null;
            try (BufferedReader input = new BufferedReader (new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter (socket.getOutputStream(), true)) {
                nombreClient = input.readLine();
                System.out.printf("%nClient %s connected%n", nombreClient);
                while(true) {
                    String message = input.readLine();
                    if (message.equals("/q")) {
                        break;
                    }
                    for (int i=0; i<printWriters.size(); i++) {
                        if (i != id) {
                            printWriters.get(i).println(message);
                        }
                    }
                }
            } catch (IOException | NullPointerException ignored) {
            } finally {
                try {
                    socket.close();
                    System.out.printf("%nClient %s disconnected%n", nombreClient);
                } catch (IOException e) {
                    System.out.println("%nError cerrando la conexión: " + e.getMessage());
                }
            }
        }
    }
}
