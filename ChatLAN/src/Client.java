import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    final static String IP = "127.0.0.1";

    public Client(String ip, int port) throws IOException {
        try (Socket socket = new Socket(InetAddress.getByName(ip), port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter your name: ");
            String nombre = sc.nextLine();
            out.writeUTF(nombre);
            new GiveServerMesassages(socket).start();
            String mess;
            do {
                System.out.print("> ");
                mess = in.readLine();
                if (!mess.trim().isEmpty()) {
                    out.writeUTF(mess);
                }

            } while (!mess.equals("/q"));
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
                while (true) {
                    System.out.printf("%n%s%n",in.readLine());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}