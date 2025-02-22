import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Server {
    final static String IP = "127.0.0.1";
    private ServerSocket serverSocket = null;
    Socket socket = null;
    static ArrayList<Socket> clientSockets = new ArrayList<>();


    public Server(String ip, int port) throws IOException {
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
        Path files = Paths.get("files/");
        Files.createDirectories(files);

        try  {
            System.out.printf("Server started%n");
            System.out.printf("Waiting for clients...%n");
            while (true) {
                socket = serverSocket.accept();
                clientSockets.add(socket);
                new SocketFunctions(socket,files).start();
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
        private Path files;

        // Getters
        @Override
        public long getId() {
            return id;
        }

        public SocketFunctions(Socket socket, Path files) throws IOException {
            this.socket = socket;
            this.files = files;
        }
        @Override
        public void run() {
            String nombreClient = null;
            boolean send = true;
            try (BufferedReader input = new BufferedReader (new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter (socket.getOutputStream(), true)) {
                nombreClient = input.readLine();
                System.out.printf("%nClient %s id: %d connected%n", nombreClient, id);
                while(true) {
                    String message = input.readLine();

                    if (message.equals("/q")) {
                        break;
                    } else if (message.split(" ")[0].equals("/upload")) {
                        String[] messSplit = message.split(" ");
                        String[] pathSplit = messSplit[1].split("\\\\");
                        File originFile = new File(messSplit[1]);
                        String fileName = pathSplit[pathSplit.length - 1];
                        Path filePath = Paths.get(files+"/"+fileName);
                        boolean exists = Files.exists(filePath);
                        try {
                            if (exists) {
                                Files.delete(filePath);
                            }
                            Files.createFile(filePath);
                            Files.write(filePath, Files.readAllBytes(originFile.toPath())); // ERROR
                            System.out.println();
                            if (exists) {
                                message = String.format("-- The file %s has been override by %s", fileName,nombreClient);
                                output.print(message);
                                System.out.println(message);
                            } else {
                                message = String.format("-- The file %s has been created by %s", fileName,nombreClient);
                                output.print(message);
                                System.out.println(message);
                            }
                        } catch (IOException e) {
                            System.out.println("Error uploading file '" + fileName + "': " + e.getMessage());
                        }
                        output.println();
                        send = true;
                    } else if (message.equals("/files")) {
                        output.printf("--- Files in %s/ %s%n",files.getFileName(),"-".repeat(30));
                        Files.list(files)
                                .map(Path::getFileName)
                                .forEach(fileName -> output.println("- "+ fileName));
                        output.println("-".repeat(50));
                        System.out.println(String.format("%n-- Show %s files/%n",files.getFileName()));
                        send = false;
                    }
                    System.out.println(send);
                    if (send) {
                        sendBroadcast(socket, message);
                    }
                }
            } catch (IOException | NullPointerException ignored) {
            } finally {
                try {
                    socket.close();
                    String mess = String.format("%nClient %s disconnected", nombreClient);
                    System.out.println(mess);
                    sendBroadcast(socket, mess);
                } catch (IOException e) {
                    System.out.println("%nError cerrando la conexión: " + e.getMessage());
                }
            }
        }
    }
    static void sendBroadcast(Socket sender, String message){
        for (Socket client : clientSockets) {
            if (!client.equals(sender)) {
                try {
                    PrintWriter outBroad = new PrintWriter(client.getOutputStream(), true);
                    outBroad.println(message);
                } catch (IOException e) {
                    System.out.println("Error sending broadcast: " + e.getCause());
                }
            }
        }

    }
}
