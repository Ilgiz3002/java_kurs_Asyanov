import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FiveInARow extends JFrame {
    private static final int INITIAL_SIZE = 10;
    private int gridSize = INITIAL_SIZE;
    private Map<String, JButton> buttons = new HashMap<>();
    private boolean isMyTurn = false; // Track if it's the player's turn
    private JPanel panel;
    private boolean initiatedExpansion = false;
    private String symbol; // Player's symbol (X or O)

    public FiveInARow() {
        setTitle("Five in a Row");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Client.stopGame();
                new MainMenu().setVisible(true);
            }
        });

        panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        panel.setBackground(Color.darkGray);
        for (int i = 0; i < INITIAL_SIZE; i++) {
            for (int j = 0; j < INITIAL_SIZE; j++) {
                addButton(panel, i, j, c);
            }
        }

        //JButton resetButton = new JButton("Restart Game");
       // resetButton.addActionListener(e -> resetBoard());

        JPanel container = new JPanel(new BorderLayout());
        container.add(new JScrollPane(panel), BorderLayout.CENTER);
        //container.add(resetButton, BorderLayout.SOUTH);

        add(container);
    }

    private void addButton(JPanel panel, int row, int col, GridBagConstraints c) {
        String key = row + "," + col;
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(50, 50));
        button.addActionListener(e -> handleMove(button, row, col));
        Color woodColor = new Color(139, 69, 19); // RGB: 139, 69, 19 - коричневый цвет
        button.setBackground(woodColor);
        button.setForeground(Color.WHITE); // Установка цвета текста кнопки
        button.setFont(new Font("Arial", Font.BOLD, 21)); // Установка шрифта текста кнопки
        c.gridx = col;
        c.gridy = row;
        panel.add(button, c);
        buttons.put(key, button);
    }

    private void handleMove(JButton button, int row, int col) {
        if (isMyTurn && button.getText().isEmpty()) {
            button.setText(symbol);
            Client.sendMoveMessage(row, col, symbol);
            if (checkWin(row, col)) {
                Client.sendWinMessage(symbol);
            }
            if (row == gridSize - 1 || col == gridSize - 1) {
                expandBoard();
                initiatedExpansion = true;

            }
            revalidate();
            repaint();
            isMyTurn = false; // End turn
        }
    }

    public void expandBoard() {
        gridSize++;
        GridBagConstraints c = new GridBagConstraints();

        for (int i = 0; i < gridSize; i++) {
            if (i < gridSize - 1) {
                addButton(panel, i, gridSize - 1, c);
            }
            addButton(panel, gridSize - 1, i, c);
        }

        revalidate();
        repaint();
    }

    private boolean checkWin(int row, int col) {
        String symbol = buttons.get(row + "," + col).getText();
        if (symbol.isEmpty()) {
            return false;
        }

        return checkDirection(row, col, symbol, 1, 0) ||
                checkDirection(row, col, symbol, 0, 1) ||
                checkDirection(row, col, symbol, 1, 1) ||
                checkDirection(row, col, symbol, 1, -1);
    }

    private boolean checkDirection(int row, int col, String symbol, int dRow, int dCol) {
        int count = 1;
        count += countSymbols(row, col, symbol, dRow, dCol);
        count += countSymbols(row, col, symbol, -dRow, -dCol);
        return count >= 5;
    }

    private int countSymbols(int row, int col, String symbol, int dRow, int dCol) {
        int count = 0;
        for (int i = 1; i < 5; i++) {
            String key = (row + i * dRow) + "," + (col + i * dCol);
            if (symbol.equals(buttons.getOrDefault(key, new JButton()).getText())) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public void showWinDialog(String symbol) {
        JOptionPane.showMessageDialog(this, symbol + " wins!");
        returnToMainMenu();
    }

    private void returnToMainMenu() {
        Client.stopGame();
        SwingUtilities.invokeLater(() -> {
            this.dispose();
            new MainMenu().setVisible(true);
        });
    }

    /*private void resetBoard() {
        gridSize = INITIAL_SIZE;
        buttons.clear();
        panel.removeAll();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        for (int i = 0; i < INITIAL_SIZE; i++) {
            for (int j = 0; j < INITIAL_SIZE; j++) {
                addButton(panel, i, j, c);
            }
        }
        revalidate();
        repaint();
        isMyTurn = false;
    }
    */
    public void updateButton(int row, int col, String symbol) {
        String key = row + "," + col;
        JButton button = buttons.get(key);
        if (button != null) {
            button.setText(symbol);
        }
    }

    public void expandBoardFromServer() {
        if (initiatedExpansion) {
            initiatedExpansion = false;
            return;
        }
        expandBoard();
    }

    public void updateTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        // После обновления хода, выводим сообщение "Сделать ход"
        if (isMyTurn) {
            JOptionPane.showMessageDialog(this, "Сделать ход");
        }
    }

    public void chooseSymbol(String symbol) {
        this.symbol = symbol;
    }
}
