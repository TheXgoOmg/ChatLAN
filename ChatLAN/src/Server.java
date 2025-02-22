import javax.smartcardio.CardChannel;
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
    static ArrayList<PrintWriter> printWriters = new ArrayList<>();
    long id = 0;


    public Server(String ip, int port) throws IOException {
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
        Path files = Paths.get("archivos/");
        Files.createDirectories(files);

        try  {
            System.out.printf("Server started%n");
            System.out.printf("Waiting for clients...%n");
            while (true) {
                socket = serverSocket.accept();
                printWriters.add(new PrintWriter (socket.getOutputStream(), true));
                new SocketFunctions(socket,id,files).start();
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
        private Path files;

        // Getters
        @Override
        public long getId() {
            return id;
        }

        public SocketFunctions(Socket socket, long id, Path files) throws IOException {
            this.socket = socket;
            this.id = id;
            this.files = files;
        }
        @Override
        public void run() {
            String nombreClient = null;
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
                    } else if (message.equals("/files")) {
                        try (Stream<Path> files = Files.list(Paths.get("archivos"))) {
                            StringBuilder fileMessage = new StringBuilder();
                            files.forEach(file -> fileMessage.append(file.getFileName()));
                        } catch (IOException e) {
                            System.out.println("Error displaying files");
                        }
                    }
                    sendBroadcast(message, id);
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
    static void sendBroadcast(String message, long id){
        for (int i=0; i<printWriters.size(); i++) {
            if (i != id) {
                printWriters.get(i).println(message);
            }
        }
    }
}
