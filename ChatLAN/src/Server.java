import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

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
        private Path files;

        public SocketFunctions(Socket socket, Path files) throws IOException {
            this.socket = socket;
            this.files = files;
        }
        @Override
        public void run() {
            String nombreClient = null;
            try (BufferedReader input = new BufferedReader (new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter (socket.getOutputStream(), true)) {
                nombreClient = input.readLine();

                System.out.printf("%nClient %s connected%n", nombreClient);
                boolean preMessFile = false;
                while(true) {
                    String message = input.readLine();
                    if (preMessFile) {
                        message = "";
                    }
                    preMessFile = false;
                    String[] messSplit = message.split(" ");

                    if (message.equals("/q")) {
                        break;
                    } else if (messSplit[0].equals("/upload")) {
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

                            String responseMessage = String.format("-- The file %s has been %s by %s",fileName,exists ? "overwrriten":"created",nombreClient);
                            System.out.println(responseMessage);
                            output.println(responseMessage);
                        } catch (IOException e) {
                            System.out.println("Error uploading file '" + fileName + "': " + e.getMessage());
                        }
                    } else if (message.equals("/files")) {
                        output.printf("--- Files in %s/ %s%n",files.getFileName(),"-".repeat(30));
                        Files.list(files)
                                .map(Path::getFileName)
                                .forEach(fileName -> output.println("- "+ fileName));
                        output.println("-".repeat(50));
                        System.out.println(String.format("%n-- Show %s files/ to %s",files.getFileName(),nombreClient));
                        preMessFile = true;
                        continue;
                    } else if (messSplit[0].equals("/show")) {
                        Path path = Path.of(String.valueOf(files),messSplit[1]);
                        try (Stream<String> readFile = Files.lines(path)) {
                            System.out.printf("%nShow %s content to %s%n",path, nombreClient);
                            output.printf("--- Content in %s %s%n",path,"-".repeat(20));
                            readFile.forEach(output::println);
                            output.println("-".repeat(50));
                        } catch (IOException e) {
                            System.out.printf("%nError showing file '%s': %s%n",messSplit[1],e.getMessage());
                        } finally {
                            preMessFile = true;
                            continue;
                        }
                    }
                    if (!message.isEmpty() && message.charAt(0) == '/') {
                        continue;
                    }
                    sendBroadcast(socket, message);
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
