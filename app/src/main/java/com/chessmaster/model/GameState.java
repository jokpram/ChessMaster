package com.chessmaster.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete state of a chess game
 */
public class GameState {
    
    public enum Status {
        IN_PROGRESS,
        WHITE_WINS_CHECKMATE,
        BLACK_WINS_CHECKMATE,
        STALEMATE,
        DRAW_FIFTY_MOVE,
        DRAW_THREEFOLD_REPETITION,
        DRAW_INSUFFICIENT_MATERIAL,
        DRAW_AGREEMENT,
        WHITE_RESIGNED,
        BLACK_RESIGNED
    }
    
    private final Board board;
    private PieceColor currentTurn;
    private final List<Move> moveHistory;
    private final List<String> positionHistory;
    private int halfMoveClock; // For fifty-move rule
    private int fullMoveNumber;
    private Status status;
    private List<Move> legalMoves;
    
    public GameState() {
        this.board = new Board();
        this.currentTurn = PieceColor.WHITE;
        this.moveHistory = new ArrayList<>();
        this.positionHistory = new ArrayList<>();
        this.halfMoveClock = 0;
        this.fullMoveNumber = 1;
        this.status = Status.IN_PROGRESS;
        updateLegalMoves();
    }
    
    /**
     * Resets the game to initial state
     */
    public void reset() {
        board.setupInitialPosition();
        currentTurn = PieceColor.WHITE;
        moveHistory.clear();
        positionHistory.clear();
        halfMoveClock = 0;
        fullMoveNumber = 1;
        status = Status.IN_PROGRESS;
        updateLegalMoves();
    }
    
    public Board getBoard() {
        return board;
    }
    
    public PieceColor getCurrentTurn() {
        return currentTurn;
    }
    
    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
    
    public int getMoveNumber() {
        return fullMoveNumber;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public boolean isGameOver() {
        return status != Status.IN_PROGRESS;
    }
    
    public List<Move> getLegalMoves() {
        return legalMoves;
    }
    
    /**
     * Gets legal moves for a piece at the given position
     */
    public List<Move> getLegalMovesFrom(Position from) {
        return legalMoves.stream()
                .filter(m -> m.getFrom().equals(from))
                .toList();
    }
    
    /**
     * Makes a move and updates game state
     */
    public boolean makeMove(Move move) {
        // Validate move is legal
        if (!legalMoves.contains(move)) {
            return false;
        }
        
        // Update fifty-move clock
        if (move.getPiece().getType() == PieceType.PAWN || move.isCapture()) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        // Execute move
        board.makeMove(move);
        moveHistory.add(move);
        
        // Store position for threefold repetition
        positionHistory.add(getBoardHash());
        
        // Switch turn
        if (currentTurn == PieceColor.BLACK) {
            fullMoveNumber++;
        }
        currentTurn = currentTurn.opposite();
        
        // Update legal moves and check game status
        updateLegalMoves();
        updateGameStatus();
        
        return true;
    }
    
    private void updateLegalMoves() {
        legalMoves = board.generateLegalMoves(currentTurn);
    }
    
    private void updateGameStatus() {
        // Check for checkmate or stalemate
        if (legalMoves.isEmpty()) {
            if (board.isInCheck(currentTurn)) {
                status = currentTurn == PieceColor.WHITE ? 
                        Status.BLACK_WINS_CHECKMATE : Status.WHITE_WINS_CHECKMATE;
            } else {
                status = Status.STALEMATE;
            }
            return;
        }
        
        // Check for fifty-move rule
        if (halfMoveClock >= 100) {
            status = Status.DRAW_FIFTY_MOVE;
            return;
        }
        
        // Check for threefold repetition
        String currentPos = getBoardHash();
        long count = positionHistory.stream().filter(p -> p.equals(currentPos)).count();
        if (count >= 3) {
            status = Status.DRAW_THREEFOLD_REPETITION;
            return;
        }
        
        // Check for insufficient material
        if (isInsufficientMaterial()) {
            status = Status.DRAW_INSUFFICIENT_MATERIAL;
            return;
        }
    }
    
    /**
     * Accurate board hash for threefold repetition (includes piece positions, turn, castling rights, and en passant)
     */
    private String getBoardHash() {
        StringBuilder sb = new StringBuilder();
        // Piece positions
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    sb.append(piece.getColor() == PieceColor.WHITE ? "W" : "B");
                    sb.append(piece.getType().getNotation());
                } else {
                    sb.append("--");
                }
            }
        }
        
        // Turn
        sb.append(currentTurn);
        
        // Castling Rights
        sb.append(getCastlingRightsHash(PieceColor.WHITE));
        sb.append(getCastlingRightsHash(PieceColor.BLACK));
        
        // En Passant target
        Position epTarget = board.getEnPassantTarget();
        sb.append(epTarget != null ? epTarget.toAlgebraic() : "-");
        
        return sb.toString();
    }
    
    private String getCastlingRightsHash(PieceColor color) {
        StringBuilder sb = new StringBuilder();
        Position kingPos = board.getKingPosition(color);
        Piece king = board.getPiece(kingPos);
        if (king != null && !king.hasMoved()) {
            int backRank = color.getBackRank();
            Piece kr = board.getPiece(backRank, 7);
            if (kr != null && kr.getType() == PieceType.ROOK && !kr.hasMoved()) sb.append("K");
            Piece qr = board.getPiece(backRank, 0);
            if (qr != null && qr.getType() == PieceType.ROOK && !qr.hasMoved()) sb.append("Q");
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }
    
    /**
     * Checks for insufficient material according to FIDE rules
     */
    private boolean isInsufficientMaterial() {
        List<PiecePos> whitePieces = new ArrayList<>();
        List<PiecePos> blackPieces = new ArrayList<>();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    PiecePos pp = new PiecePos(piece, new Position(row, col));
                    if (piece.getColor() == PieceColor.WHITE) whitePieces.add(pp);
                    else blackPieces.add(pp);
                }
            }
        }
        
        // K vs K
        if (whitePieces.size() == 1 && blackPieces.size() == 1) return true;
        
        // K vs KN or K vs KB
        if (whitePieces.size() <= 2 && blackPieces.size() <= 2) {
            boolean wMinor = whitePieces.size() == 2;
            boolean bMinor = blackPieces.size() == 2;
            
            if (wMinor && bMinor) {
                // KB vs KB (same colored bishops)
                PiecePos wB = whitePieces.stream().filter(p -> p.p.getType() == PieceType.BISHOP).findFirst().orElse(null);
                PiecePos bB = blackPieces.stream().filter(p -> p.p.getType() == PieceType.BISHOP).findFirst().orElse(null);
                if (wB != null && bB != null) {
                    return wB.pos.isLightSquare() == bB.pos.isLightSquare();
                }
            } else if (wMinor ^ bMinor) {
                PiecePos minor = (wMinor ? whitePieces : blackPieces).stream()
                        .filter(p -> p.p.getType() == PieceType.BISHOP || p.p.getType() == PieceType.KNIGHT)
                        .findFirst().orElse(null);
                if (minor != null) return true;
            }
        }
        
        return false;
    }
    
    private record PiecePos(Piece p, Position pos) {}

    
    /**
     * Resign from the game
     */
    public void resign() {
        status = currentTurn == PieceColor.WHITE ? Status.WHITE_RESIGNED : Status.BLACK_RESIGNED;
    }
    
    /**
     * Offer/accept draw
     */
    public void acceptDraw() {
        status = Status.DRAW_AGREEMENT;
    }
    
    /**
     * Gets status message for display
     */
    public String getStatusMessage() {
        return switch (status) {
            case IN_PROGRESS -> {
                if (board.isInCheck(currentTurn)) {
                    yield currentTurn + " is in check!";
                }
                yield currentTurn + " to move";
            }
            case WHITE_WINS_CHECKMATE -> "Checkmate! White wins!";
            case BLACK_WINS_CHECKMATE -> "Checkmate! Black wins!";
            case STALEMATE -> "Stalemate! Draw.";
            case DRAW_FIFTY_MOVE -> "Draw by fifty-move rule.";
            case DRAW_THREEFOLD_REPETITION -> "Draw by threefold repetition.";
            case DRAW_INSUFFICIENT_MATERIAL -> "Draw by insufficient material.";
            case DRAW_AGREEMENT -> "Draw by agreement.";
            case WHITE_RESIGNED -> "White resigned. Black wins!";
            case BLACK_RESIGNED -> "Black resigned. White wins!";
        };
    }
    
    /**
     * Get the last move made
     */
    public Move getLastMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }
        return moveHistory.get(moveHistory.size() - 1);
    }
}
