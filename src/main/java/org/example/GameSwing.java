package org.example;

import javax.swing.*;

public class GameSwing {
    private JPanel mainPanel;
    private JButton nombreButton;

    public static void main(String[] args) {
        JFrame frame = new JFrame("GameSwing");
        frame.setContentPane(new GameSwing().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
