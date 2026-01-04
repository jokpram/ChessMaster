package com.chessmaster.view;

import com.chessmaster.controller.GameController;
import com.chessmaster.engine.ChessEngine;
import com.chessmaster.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Premium side panel for game info and controls using Swing
 */
public class GameInfoPanel extends JPanel {
    
    private final GameController controller;
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JPanel moveHistoryPanel;
    private JScrollPane historyScroll;
    private JPanel whiteCapturedPanel;
    private JPanel blackCapturedPanel;
    private JProgressBar thinkingBar;
    
    // Premium Colors
    private static final Color BG_COLOR = new Color(28, 28, 45);
    private static final Color CARD_COLOR = new Color(40, 40, 65);
    private static final Color ACCENT_COLOR = new Color(108, 92, 231);
    private static final Color TEXT_COLOR = new Color(236, 240, 241);
    private static final Color SUBTEXT_COLOR = new Color(189, 195, 199);
    
    public GameInfoPanel(GameController controller) {
        this.controller = controller;
        
        setPreferredSize(new Dimension(340, 800));
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(25, 20, 25, 20));
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("CHESSMASTER PRO");
        title.setFont(new Font("Orbitron", Font.BOLD, 22)); // Elegant futuristic font
        if (title.getFont().getName().equals("Dialog")) title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT_COLOR);
        header.add(title, BorderLayout.NORTH);
        
        JLabel subtitle = new JLabel("Advanced AI Engine v1.0");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setForeground(SUBTEXT_COLOR);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        
        // --- Main Content ---
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        // Status Card
        JPanel statusCard = createStyledCard("GAME STATUS");
        turnLabel = new JLabel("White's Turn");
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        turnLabel.setForeground(TEXT_COLOR);
        
        statusLabel = new JLabel("Awaiting first move...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(SUBTEXT_COLOR);
        
        thinkingBar = new JProgressBar();
        thinkingBar.setIndeterminate(true);
        thinkingBar.setVisible(false);
        thinkingBar.setPreferredSize(new Dimension(280, 4));
        thinkingBar.setForeground(ACCENT_COLOR);
        thinkingBar.setBackground(BG_COLOR);
        thinkingBar.setBorderPainted(false);
        
        statusCard.add(turnLabel);
        statusCard.add(Box.createVerticalStrut(5));
        statusCard.add(statusLabel);
        statusCard.add(Box.createVerticalStrut(10));
        statusCard.add(thinkingBar);
        content.add(statusCard);
        content.add(Box.createVerticalStrut(15));
        
        // Captured Card
        JPanel capturedCard = createStyledCard("CAPTURED PIECES");
        whiteCapturedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        whiteCapturedPanel.setOpaque(false);
        blackCapturedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        blackCapturedPanel.setOpaque(false);
        
        JLabel wLabel = new JLabel("White Advantage:");
        wLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        wLabel.setForeground(SUBTEXT_COLOR);
        
        JLabel bLabel = new JLabel("Black Advantage:");
        bLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bLabel.setForeground(SUBTEXT_COLOR);
        
        capturedCard.add(wLabel);
        capturedCard.add(whiteCapturedPanel);
        capturedCard.add(Box.createVerticalStrut(10));
        capturedCard.add(bLabel);
        capturedCard.add(blackCapturedPanel);
        content.add(capturedCard);
        content.add(Box.createVerticalStrut(15));
        
        // History Card
        JPanel historyCard = createStyledCard("MATCH HISTORY");
        moveHistoryPanel = new JPanel();
        moveHistoryPanel.setLayout(new BoxLayout(moveHistoryPanel, BoxLayout.Y_AXIS));
        moveHistoryPanel.setBackground(new Color(20, 20, 35));
        
        historyScroll = new JScrollPane(moveHistoryPanel);
        historyScroll.setPreferredSize(new Dimension(280, 200));
        historyScroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 80)));
        historyScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        historyCard.add(historyScroll);
        content.add(historyCard);
        
        add(content, BorderLayout.CENTER);
        
        // --- Bottom Controls ---
        JPanel controls = new JPanel(new GridLayout(0, 1, 10, 10));
        controls.setOpaque(false);
        
        // Difficulty Selector
        JPanel diffPanel = new JPanel(new BorderLayout(5, 0));
        diffPanel.setOpaque(false);
        JLabel diffLabel = new JLabel("AI LEVEL: ");
        diffLabel.setForeground(TEXT_COLOR);
        diffLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JComboBox<ChessEngine.Difficulty> diffSelector = new JComboBox<>(ChessEngine.Difficulty.values());
        diffSelector.setSelectedItem(ChessEngine.Difficulty.MEDIUM);
        diffSelector.addActionListener(e -> controller.setDifficulty((ChessEngine.Difficulty) diffSelector.getSelectedItem()));
        diffPanel.add(diffLabel, BorderLayout.WEST);
        diffPanel.add(diffSelector, BorderLayout.CENTER);
        
        // Game Mode Selector
        JComboBox<String> modeSelector = new JComboBox<>(new String[]{"PLAY VS AI (WHITE)", "PLAY VS AI (BLACK)", "LOCAL MULTIPLAYER"});
        modeSelector.addActionListener(e -> {
            int idx = modeSelector.getSelectedIndex();
            if (idx == 0) controller.setGameMode(GameController.GameMode.PLAYER_VS_AI_WHITE);
            else if (idx == 1) controller.setGameMode(GameController.GameMode.PLAYER_VS_AI_BLACK);
            else controller.setGameMode(GameController.GameMode.PLAYER_VS_PLAYER);
        });
        
        JButton btnNewGame = createStyledButton("RESTART MATCH", new Color(46, 204, 113));
        btnNewGame.addActionListener(e -> controller.newGame());
        
        controls.add(diffPanel);
        controls.add(modeSelector);
        controls.add(btnNewGame);
        
        add(controls, BorderLayout.SOUTH);
    }
    
    private JPanel createStyledCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11), ACCENT_COLOR
        );
        card.setBorder(BorderFactory.createCompoundBorder(border, card.getBorder()));
        
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }
    
    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(baseColor);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(280, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(baseColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(baseColor);
            }
        });
        return btn;
    }
    
    public void refresh() {
        GameState state = controller.getGameState();
        
        turnLabel.setText(state.getCurrentTurn() + "'s Turn");
        turnLabel.setForeground(state.getCurrentTurn() == PieceColor.WHITE ? Color.WHITE : new Color(200, 200, 200));
        
        statusLabel.setText(state.getStatusMessage().toUpperCase());
        thinkingBar.setVisible(controller.isAiThinking());
        
        // Update History
        moveHistoryPanel.removeAll();
        List<Move> history = state.getMoveHistory();
        for (int i = 0; i < history.size(); i += 2) {
            String wMove = history.get(i).toAlgebraic();
            String bMove = (i + 1 < history.size()) ? history.get(i + 1).toAlgebraic() : "";
            
            JPanel moveRow = new JPanel(new GridLayout(1, 3));
            moveRow.setOpaque(false);
            moveRow.setMaximumSize(new Dimension(280, 25));
            
            JLabel num = new JLabel((i/2 + 1) + ".");
            num.setForeground(ACCENT_COLOR);
            num.setFont(new Font("Monospaced", Font.BOLD, 12));
            
            JLabel white = new JLabel(wMove);
            white.setForeground(TEXT_COLOR);
            
            JLabel black = new JLabel(bMove);
            black.setForeground(TEXT_COLOR);
            
            moveRow.add(num);
            moveRow.add(white);
            moveRow.add(black);
            moveHistoryPanel.add(moveRow);
        }
        
        // Update Captured (Material Balance logic could be added here for a better 'advantage' view)
        updateCaptured(whiteCapturedPanel, PieceColor.WHITE, history);
        updateCaptured(blackCapturedPanel, PieceColor.BLACK, history);
        
        revalidate();
        repaint();
        
        // Auto scroll
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = historyScroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    private void updateCaptured(JPanel panel, PieceColor pieceColor, List<Move> history) {
        panel.removeAll();
        history.stream()
            .filter(Move::isCapture)
            .map(Move::getCapturedPiece)
            .filter(p -> p.getColor() == pieceColor)
            .forEach(p -> {
                JLabel lbl = new JLabel(p.getSymbol());
                lbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
                lbl.setForeground(pieceColor == PieceColor.WHITE ? Color.WHITE : new Color(150, 150, 150));
                panel.add(lbl);
            });
    }
}
