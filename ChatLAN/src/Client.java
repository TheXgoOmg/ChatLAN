import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    final static String IP = "127.0.0.1";
    String nombre;

    public Client(String ip, int port) throws IOException {
        try (Socket socket = new Socket(InetAddress.getByName(ip), port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter your name: ");
            nombre = sc.nextLine();
            System.out.println();
            out.println(nombre);
            new GiveServerMesassages(socket).start();
            String preMess = String.format("> %s: ",nombre);
            String mess;
            do {
                mess = in.readLine();
                System.out.println();
                if (!mess.trim().isEmpty()) {
                    out.println(preMess + mess);
                }
            } while (!mess.equals("/q"));
            System.out.println("You have disconnected from the server.");
        } catch (IOException i) {
            System.out.println(i);
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client(IP,5000);
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
                    System.out.printf("%n%s%n",mess);
                }
            } catch (IOException ignored) {
            }
        }
    }
}