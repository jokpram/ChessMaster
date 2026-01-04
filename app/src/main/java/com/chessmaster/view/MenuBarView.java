package com.chessmaster.view;

import com.chessmaster.controller.GameController;
import com.chessmaster.engine.ChessEngine;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Menu bar for the chess application using Swing
 */
public class MenuBarView extends JMenuBar {
    
    private final GameController controller;
    
    public MenuBarView(GameController controller) {
        this.controller = controller;
        
        // Game Menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(KeyEvent.VK_G);
        
        JMenuItem newGame = new JMenuItem("New Game", KeyEvent.VK_N);
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newGame.addActionListener(e -> controller.newGame());
        
        JMenuItem resign = new JMenuItem("Resign");
        resign.addActionListener(e -> controller.resign());
        
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        exit.addActionListener(e -> System.exit(0));
        
        gameMenu.add(newGame);
        gameMenu.add(resign);
        gameMenu.addSeparator();
        gameMenu.add(exit);
        
        // Mode Menu
        JMenu modeMenu = new JMenu("Mode");
        ButtonGroup group = new ButtonGroup();
        
        addModeItem(modeMenu, group, "Player vs AI (White)", GameController.GameMode.PLAYER_VS_AI_WHITE, true);
        addModeItem(modeMenu, group, "Player vs AI (Black)", GameController.GameMode.PLAYER_VS_AI_BLACK, false);
        addModeItem(modeMenu, group, "Player vs Player", GameController.GameMode.PLAYER_VS_PLAYER, false);
        
        // Difficulty Menu
        JMenu diffMenu = new JMenu("Difficulty");
        ButtonGroup diffGroup = new ButtonGroup();
        addDiffItem(diffMenu, diffGroup, "Easy", ChessEngine.Difficulty.EASY, false);
        addDiffItem(diffMenu, diffGroup, "Medium", ChessEngine.Difficulty.MEDIUM, true);
        addDiffItem(diffMenu, diffGroup, "Hard", ChessEngine.Difficulty.HARD, false);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(null, 
            "ChessMaster Pro v1.0.0\nProfessional Strategic Chess Engine\nFeaturing Iterative Deepening AI.",
            "About ChessMaster", JOptionPane.INFORMATION_MESSAGE));
        
        helpMenu.add(about);
        
        add(gameMenu);
        add(modeMenu);
        add(diffMenu);
        add(helpMenu);
    }
    
    private void addDiffItem(JMenu menu, ButtonGroup group, String label, ChessEngine.Difficulty diff, boolean selected) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(label, selected);
        item.addActionListener(e -> controller.setDifficulty(diff));
        group.add(item);
        menu.add(item);
    }
    
    private void addModeItem(JMenu menu, ButtonGroup group, String label, GameController.GameMode mode, boolean selected) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(label, selected);
        item.addActionListener(e -> {
            controller.setGameMode(mode);
            controller.newGame();
        });
        group.add(item);
        menu.add(item);
    }
}
