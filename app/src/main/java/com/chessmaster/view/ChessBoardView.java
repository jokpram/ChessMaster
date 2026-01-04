package com.chessmaster.view;

import com.chessmaster.controller.GameController;
import com.chessmaster.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Premium visual representation of the chess board using Swing.
 * Features: High-quality rendering, soft shadows, and elegant color palette.
 */
public class ChessBoardView extends JPanel {
    
    private static final int SQUARE_SIZE = 85; // Slightly larger for better detail
    private final GameController controller;
    private final JPanel[][] squarePanels = new JPanel[8][8];
    
    // Elegant Midnight Coffee Palette
    private static final Color LIGHT_SQUARE = new Color(235, 235, 230); // Ivory
    private static final Color DARK_SQUARE = new Color(50, 45, 40);     // Ebony/Dark Coffee
    private static final Color SELECTED_COLOR = new Color(80, 150, 250, 180); // Translucent sky blue
    private static final Color HIGHLIGHT_COLOR = new Color(46, 204, 113, 100); // Soft emerald emerald
    private static final Color LAST_MOVE_COLOR = new Color(241, 196, 15, 120); // Amber
    private static final Color CHECK_COLOR = new Color(231, 76, 60, 200);      // Crimson alert
    
    public ChessBoardView(GameController controller) {
        this.controller = controller;
        setLayout(new GridLayout(8, 8));
        setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));
        setBackground(new Color(30, 30, 30));
        
        // Premium border
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(20, 20, 20), 10),
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1)
        ));
        
        initializeBoard();
    }
    
    private void initializeBoard() {
        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                final int r = row;
                final int c = col;
                
                JPanel square = new JPanel(new BorderLayout());
                boolean isLight = (row + col) % 2 == 1;
                square.setBackground(isLight ? LIGHT_SQUARE : DARK_SQUARE);
                
                square.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        controller.handleSquareClick(new Position(r, c));
                    }
                });
                
                squarePanels[row][col] = square;
                add(square);
            }
        }
    }
    
    public void refresh() {
        Board board = controller.getGameState().getBoard();
        Position selected = controller.getSelectedPosition();
        List<Move> highlightedMoves = controller.getHighlightedMoves();
        Move lastMove = controller.getGameState().getLastMove();
        PieceColor turn = controller.getGameState().getCurrentTurn();
        Position kingInCheck = board.isInCheck(turn) ? board.getKingPosition(turn) : null;
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel square = squarePanels[row][col];
                square.removeAll();
                
                Position pos = new Position(row, col);
                boolean isLight = (row + col) % 2 == 1;
                
                // Color layer logic
                Color bg = isLight ? LIGHT_SQUARE : DARK_SQUARE;
                if (pos.equals(kingInCheck)) bg = CHECK_COLOR;
                else if (pos.equals(selected)) bg = SELECTED_COLOR;
                else if (lastMove != null && (pos.equals(lastMove.getFrom()) || pos.equals(lastMove.getTo()))) {
                    bg = LAST_MOVE_COLOR;
                }
                
                square.setBackground(bg);
                
                final boolean isMoveTarget = highlightedMoves.stream().anyMatch(m -> m.getTo().equals(pos));
                Piece piece = board.getPiece(pos);
                
                square.add(new JComponent() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                        
                        // 1. Move/Capture Highlights
                        if (isMoveTarget) {
                            g2.setColor(HIGHLIGHT_COLOR);
                            if (piece != null) {
                                // Capture ring
                                g2.setStroke(new BasicStroke(4));
                                g2.drawOval(8, 8, getWidth() - 16, getHeight() - 16);
                            } else {
                                // Move dot
                                g2.fillOval(getWidth() / 2 - 8, getHeight() / 2 - 8, 16, 16);
                            }
                        }
                        
                        // 2. Render Pieces
                        if (piece != null) {
                            String symbol = piece.getSymbol();
                            g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 56));
                            FontMetrics fm = g2.getFontMetrics();
                            int x = (getWidth() - fm.stringWidth(symbol)) / 2;
                            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                            
                            // Soft piece shadow
                            g2.setColor(new Color(0, 0, 0, 60));
                            g2.drawString(symbol, x + 3, y + 3);
                            
                            if (piece.getColor() == PieceColor.WHITE) {
                                // White pieces: Bright white with subtle blue tint
                                g2.setColor(new Color(255, 255, 255));
                                g2.drawString(symbol, x, y);
                                // Thin outline for better visibility on light squares
                                g2.setColor(new Color(0, 0, 0, 40));
                                g2.setStroke(new BasicStroke(0.5f));
                                g2.draw(g2.getFont().createGlyphVector(g2.getFontRenderContext(), symbol).getOutline(x, y));
                            } else {
                                // Black pieces: Deep Charcoal
                                g2.setColor(new Color(20, 20, 20));
                                g2.drawString(symbol, x, y);
                            }
                        }
                    }
                });
                
                square.revalidate();
                square.repaint();
            }
        }
        
        setCursor(controller.isAiThinking() ? 
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : 
                Cursor.getDefaultCursor());
    }
}
