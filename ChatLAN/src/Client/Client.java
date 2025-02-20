package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    Socket socket = null;
    BufferedReader in = null;
    DataOutputStream out = null;

    public Client(String ip, int port) throws IOException {
        try {
            socket = new Socket(InetAddress.getByName(ip), port);
            in = new BufferedReader(new InputStreamReader(System.in));
            out = new DataOutputStream(socket.getOutputStream());
            String mess = "";
            mess = in.readLine();
            out.writeUTF(mess);
        } catch (IOException i) {
            System.out.println(i);
            return;
        }

        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client("0.0.0.0",5000);
    }
}