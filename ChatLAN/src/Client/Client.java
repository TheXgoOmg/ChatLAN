package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    Socket socket = null;
    BufferedReader in = null;
    BufferedReader inExtern = null;
    DataOutputStream out = null;

    public Client(String ip, int port) throws IOException {
        try {
            socket = new Socket(InetAddress.getByName(ip), port);
            in = new BufferedReader(new InputStreamReader(System.in));
            out = new DataOutputStream(socket.getOutputStream());
            // inExtern = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Recibe un mensaje del server, como mensajes de otros clientes
            String mess = "";
            mess = in.readLine();
            out.writeUTF(mess);
        } catch (IOException i) {
            System.out.println(i);
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client("0.0.0.0",5000);

        Client client1 = new Client("0.0.0.0",5000);
    }
}