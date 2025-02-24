import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    String nombre;

    public Client(InetAddress ip, int port) throws IOException {
        try (Socket socket = new Socket(ip, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             Scanner sc = new Scanner(System.in)) {
            boolean isFile;
            System.out.print("Enter your name: ");
            nombre = sc.nextLine();
            System.out.println();
            out.println(nombre);
            new GiveServerMesassages(socket).start();
            String preMess = String.format("> %s: ",nombre);
            String mess;
            do {
                isFile = false;
                mess = in.readLine();
                if (mess == null || mess.trim().isEmpty()) {
                    continue;
                }
                System.out.println();
                mess = mess.trim();
                if (mess.split(" ")[0].equals("/upload") && mess.split(" ").length==2) {
                    try {
                        String filePath = mess.split(" ")[1];
                        File file = new File(filePath);
                        if (file.isFile()) {
                            isFile = true;
                        }
                    } catch (Exception e) {
                        System.out.println("Error uploading file");
                    }
                } else if (mess.equals("/files") || mess.split(" ")[0].equals("/show")) {
                    out.println(mess);
                    isFile = true;
                }
                if (isFile) {
                    out.println(mess);
                } else {
                    out.println(preMess + mess);
                }
            } while (!mess.equals("/q"));
            System.out.println("You have disconnected from the server.");
        } catch (IOException i) {
            System.out.println(i);
        }
    }
    public static void main(String[] args) throws IOException {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter the server IP address: ");
            String ip = sc.nextLine();
            Client client = new Client(InetAddress.getByName(ip),6789);
        } catch (Exception e) {
            System.out.println("Invalid port");
        }
    }
    class GiveServerMesassages extends Thread {
        Socket socket;

        public GiveServerMesassages(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String mess;
                while (true) {
                    mess = in.readLine();
                    if (mess != null && !mess.trim().isEmpty()) {
                        System.out.printf("%s%n%n",mess);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }
}