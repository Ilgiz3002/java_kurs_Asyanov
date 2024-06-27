import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Five in a Row - Main Menu");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        JButton startButton = new JButton("Start Game");
        JButton exitButton = new JButton("Exit");

        startButton.addActionListener(e -> startGame());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(startButton);
        panel.add(exitButton);

        add(panel);
    }

    private void startGame() {
        Client.startGame();
        dispose();
    }
}