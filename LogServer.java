import javax.net.ssl.*;
import javax.net.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogServer {
    /**
     * Port number that loggers communicate on
     */
    private static final int PORT_NUM = 8888;

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        /**
         * Helper Class to enable multithreading of log Server
         * @param socket
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Helper method that allows the server to display and log all the data all at once on multiple threads.
         */
        public void run() {
            PrintWriter pw = null;
            try {
                InputStream is = socket.getInputStream();
                String host = socket.getInetAddress().getHostAddress();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));

                FileWriter fw = new FileWriter("ServerLogs/" + host + ".log", true);
                BufferedWriter writer = new BufferedWriter(fw);
                pw = new PrintWriter(writer);


                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(Thread.activeCount() + " Hosts Currently Connected");
                    System.out.println(host + ":" + "[" + line + "]");
                    pw.println(host + ":" + "[" + line + "]");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (socket != null) {
                    try {
                        pw.close();
                        socket.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }

            }


        }
    }
    public static void main(String[] args) {
        ServerSocketFactory serverSocketFactory =
                ServerSocketFactory.getDefault();
        ServerSocket serverSocket = null;
        try {
            serverSocket =
                    serverSocketFactory.createServerSocket(PORT_NUM);
        } catch (IOException ignored) {
            System.err.println("Unable to create server");
            System.exit(-1);
        }
        System.out.printf("LogServer running on port: %s%n", PORT_NUM);
        while (true) {
            Socket socket = null;
            try {

                socket = serverSocket.accept();
                ClientHandler handle = new ClientHandler(socket);
                new Thread(handle).start();

            } catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }

        }
    }
}