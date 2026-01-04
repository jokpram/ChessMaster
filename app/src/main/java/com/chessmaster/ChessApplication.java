package com.chessmaster;

import com.chessmaster.controller.GameController;
import com.chessmaster.view.ChessBoardView;
import com.chessmaster.view.GameInfoPanel;
import com.chessmaster.view.MenuBarView;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for ChessMaster using Swing
 */
public class ChessApplication {
    
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    
    public static void main(String[] args) {
        // Set Look and Feel to System
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("♛ ChessMaster Pro - The Ultimate Chess Experience");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            frame.setMinimumSize(new Dimension(1000, 700));
            
            GameController gameController = new GameController();
            
            // Layout
            frame.setLayout(new BorderLayout());
            
            // Menu Bar
            MenuBarView menuBar = new MenuBarView(gameController);
            frame.setJMenuBar(menuBar);
            
            // Content Panel
            JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            mainPanel.setBackground(new Color(26, 26, 46));
            
            // Board View
            ChessBoardView boardView = new ChessBoardView(gameController);
            gameController.setBoardView(boardView);
            
            // Info Panel
            GameInfoPanel infoPanel = new GameInfoPanel(gameController);
            gameController.setInfoPanel(infoPanel);
            
            mainPanel.add(boardView, BorderLayout.CENTER);
            mainPanel.add(infoPanel, BorderLayout.EAST);
            
            frame.add(mainPanel, BorderLayout.CENTER);
            
            // Finalize frame
            frame.setLocationRelativeTo(null);
            
            // Start game
            gameController.newGame();
            
            frame.setVisible(true);
        });
    }
}
