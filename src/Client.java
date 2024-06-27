import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static FiveInARow game;
    private static String playerSymbol;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }

    public static void startGame() {
        try {
            socket = new Socket("localhost", 9806);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(Client::listenForMessages).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listenForMessages() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                if (message.startsWith("START_GAME:")) {
                    playerSymbol = message.split(":")[1];
                    SwingUtilities.invokeLater(() -> {
                        game = new FiveInARow();
                        game.setVisible(true);
                        game.chooseSymbol(playerSymbol);
                    });
                } else if (message.startsWith("MOVE:")) {
                    String[] parts = message.substring(5).split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    String symbol = parts[2];
                    SwingUtilities.invokeLater(() -> {
                        if (game != null) {
                            game.updateButton(row, col, symbol);
                        }
                    });
                } else if (message.equals("EXPAND")) {
                    SwingUtilities.invokeLater(() -> {
                        if (game != null) {
                            game.expandBoardFromServer();
                        }
                    });
                } else if (message.startsWith("WINNER:")) {
                    String winner = message.substring(7);
                    SwingUtilities.invokeLater(() -> {
                        if (game != null) {
                            game.showWinDialog(winner);
                        }
                    });
                } else if (message.equals("YOUR_TURN")) {
                    SwingUtilities.invokeLater(() -> {
                        if (game != null) {
                            game.updateTurn(true);
                        }
                    });
                } else if (message.equals("WAITING_FOR_TURN")) {
                    SwingUtilities.invokeLater(() -> {
                        if (game != null) {
                            game.updateTurn(false);
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMoveMessage(int row, int col, String symbol) {
        out.println("MOVE:" + row + "," + col + "," + symbol);
    }

    public static void sendExpandMessage() {
        out.println("EXPAND");
    }

    public static void sendWinMessage(String symbol) {
        out.println("WINNER:" + symbol);
    }

    public static void stopGame() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
