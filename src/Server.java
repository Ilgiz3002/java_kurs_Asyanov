import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 9806;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static int currentPlayerIndex = 0; // Index of the player whose turn it is

    public static void main(String[] args) {
        System.out.println("Server started...");
        double y = 0;
        int i = 5;
        y = i++ + i++;
        System.out.println(y);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
                synchronized (clients) {
                    clients.add(clientHandler);
                    if (clients.size() == 2) {
                        clients.get(0).setSymbol("X");
                        clients.get(1).setSymbol("O");
                        for (ClientHandler client : clients) {
                            client.startGame();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int playerIndex; // Index of this client in the clients list
        private String symbol;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    if (message.startsWith("MOVE:") || message.equals("EXPAND") || message.startsWith("WINNER:")) {
                        broadcast(message);
                        if (message.startsWith("WINNER:")) {
                            stopGame();
                            break;
                        }
                        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
                        clients.get(currentPlayerIndex).notifyTurn();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                handleDisconnection();
            }
        }

        private void handleDisconnection() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clients) {
                clients.remove(this);
                if (!clients.isEmpty()) {
                    ClientHandler remainingClient = clients.get(0);
                    remainingClient.out.println("WINNER:" + remainingClient.symbol);
                    stopGame();
                }
            }
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public void startGame() {
            playerIndex = clients.indexOf(this);
            out.println("START_GAME:" + symbol);
            if (playerIndex == currentPlayerIndex) {
                out.println("YOUR_TURN");
            } else {
                out.println("WAITING_FOR_TURN");
            }
        }

        public void notifyTurn() {
            if (playerIndex == currentPlayerIndex) {
                out.println("YOUR_TURN");
            } else {
                out.println("WAITING_FOR_TURN");
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println(message);
                }
            }
        }

        private void stopGame() {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println("GAME_OVER");
                    try {
                        client.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                clients.clear();
                currentPlayerIndex = 0; // Reset current player index
            }
        }
    }
}
