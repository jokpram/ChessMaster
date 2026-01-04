package com.chessmaster.controller;

import com.chessmaster.engine.ChessEngine;
import com.chessmaster.model.*;
import com.chessmaster.view.ChessBoardView;
import com.chessmaster.view.GameInfoPanel;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main game controller that manages game flow and connects model with view (Swing version)
 */
public class GameController {
    
    public enum GameMode {
        PLAYER_VS_PLAYER,
        PLAYER_VS_AI_WHITE,  // Player plays white
        PLAYER_VS_AI_BLACK   // Player plays black (AI plays white)
    }
    
    private GameState gameState;
    private ChessBoardView boardView;
    private GameInfoPanel infoPanel;
    private ChessEngine engine;
    private GameMode gameMode;
    private Position selectedPosition;
    private List<Move> highlightedMoves;
    private final ExecutorService aiExecutor;
    private boolean aiThinking;
    
    public GameController() {
        this.gameState = new GameState();
        this.engine = new ChessEngine();
        this.gameMode = GameMode.PLAYER_VS_AI_WHITE;
        this.selectedPosition = null;
        this.highlightedMoves = List.of();
        this.aiExecutor = Executors.newSingleThreadExecutor();
        this.aiThinking = false;
    }
    
    public void setBoardView(ChessBoardView boardView) {
        this.boardView = boardView;
    }
    
    public void setInfoPanel(GameInfoPanel infoPanel) {
        this.infoPanel = infoPanel;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setDifficulty(ChessEngine.Difficulty difficulty) {
        this.engine.setDifficulty(difficulty);
    }
    
    public Position getSelectedPosition() {
        return selectedPosition;
    }
    
    public List<Move> getHighlightedMoves() {
        return highlightedMoves;
    }
    
    public boolean isAiThinking() {
        return aiThinking;
    }
    
    public void newGame() {
        gameState.reset();
        selectedPosition = null;
        highlightedMoves = List.of();
        aiThinking = false;
        updateViews();
        
        if (gameMode == GameMode.PLAYER_VS_AI_BLACK) {
            makeAiMove();
        }
    }
    
    public void handleSquareClick(Position pos) {
        if (aiThinking || gameState.isGameOver()) {
            return;
        }
        
        if (gameMode == GameMode.PLAYER_VS_AI_WHITE && 
                gameState.getCurrentTurn() == PieceColor.BLACK) {
            return;
        }
        if (gameMode == GameMode.PLAYER_VS_AI_BLACK && 
                gameState.getCurrentTurn() == PieceColor.WHITE) {
            return;
        }
        
        Piece clickedPiece = gameState.getBoard().getPiece(pos);
        
        if (selectedPosition != null) {
            Move moveToMake = highlightedMoves.stream()
                    .filter(m -> m.getTo().equals(pos))
                    .findFirst()
                    .orElse(null);
            
            if (moveToMake != null) {
                if (moveToMake.isPromotion()) {
                    // Default to Queen for now, or could show JOptionPane
                    moveToMake = highlightedMoves.stream()
                            .filter(m -> m.getTo().equals(pos) && 
                                    m.getPromotionType() == PieceType.QUEEN)
                            .findFirst()
                            .orElse(moveToMake);
                }
                
                executeMove(moveToMake);
                return;
            }
            
            if (clickedPiece != null && 
                    clickedPiece.getColor() == gameState.getCurrentTurn()) {
                selectPiece(pos);
                return;
            }
            
            deselectPiece();
            return;
        }
        
        if (clickedPiece != null && 
                clickedPiece.getColor() == gameState.getCurrentTurn()) {
            selectPiece(pos);
        }
    }
    
    private void selectPiece(Position pos) {
        selectedPosition = pos;
        highlightedMoves = gameState.getLegalMovesFrom(pos);
        updateViews();
    }
    
    private void deselectPiece() {
        selectedPosition = null;
        highlightedMoves = List.of();
        updateViews();
    }
    
    private void executeMove(Move move) {
        gameState.makeMove(move);
        selectedPosition = null;
        highlightedMoves = List.of();
        updateViews();
        
        if (gameState.isGameOver()) {
            showGameOverMessage();
            return;
        }
        
        if (shouldAiMove()) {
            // Small delay for UX
            Timer timer = new Timer(300, e -> makeAiMove());
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    private boolean shouldAiMove() {
        if (gameMode == GameMode.PLAYER_VS_PLAYER) return false;
        return (gameMode == GameMode.PLAYER_VS_AI_WHITE && gameState.getCurrentTurn() == PieceColor.BLACK) ||
               (gameMode == GameMode.PLAYER_VS_AI_BLACK && gameState.getCurrentTurn() == PieceColor.WHITE);
    }
    
    private void makeAiMove() {
        if (gameState.isGameOver()) return;
        
        aiThinking = true;
        updateViews();
        
        aiExecutor.submit(() -> {
            try {
                final Move aiMove = engine.findBestMove(gameState);
                SwingUtilities.invokeLater(() -> {
                    aiThinking = false;
                    if (aiMove != null) {
                        gameState.makeMove(aiMove);
                        updateViews();
                        if (gameState.isGameOver()) showGameOverMessage();
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    aiThinking = false;
                    updateViews();
                });
                e.printStackTrace();
            }
        });
    }
    
    private void updateViews() {
        if (boardView != null) boardView.refresh();
        if (infoPanel != null) infoPanel.refresh();
    }
    
    private void showGameOverMessage() {
        JOptionPane.showMessageDialog(null, "Game Over: " + gameState.getStatusMessage());
    }
    
    public void resign() {
        if (!gameState.isGameOver()) {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to resign?", "Resign", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                gameState.resign();
                updateViews();
                showGameOverMessage();
            }
        }
    }
    
    public void shutdown() {
        aiExecutor.shutdownNow();
    }
}
