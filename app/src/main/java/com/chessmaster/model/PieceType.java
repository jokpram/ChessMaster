package com.chessmaster.model;

/**
 * Represents the type of a chess piece
 */
public enum PieceType {
    KING("K", "♔", "♚", 0),      // King has infinite value
    QUEEN("Q", "♕", "♛", 9),
    ROOK("R", "♖", "♜", 5),
    BISHOP("B", "♗", "♝", 3),
    KNIGHT("N", "♘", "♞", 3),
    PAWN("", "♙", "♟", 1);
    
    private final String notation;
    private final String whiteSymbol;
    private final String blackSymbol;
    private final int value;
    
    PieceType(String notation, String whiteSymbol, String blackSymbol, int value) {
        this.notation = notation;
        this.whiteSymbol = whiteSymbol;
        this.blackSymbol = blackSymbol;
        this.value = value;
    }
    
    public String getNotation() {
        return notation;
    }
    
    public String getSymbol(PieceColor color) {
        return color == PieceColor.WHITE ? whiteSymbol : blackSymbol;
    }
    
    public int getValue() {
        return value;
    }
    
    /**
     * Parse piece type from notation character
     */
    public static PieceType fromNotation(String notation) {
        if (notation == null || notation.isEmpty()) {
            return PAWN;
        }
        return switch (notation.toUpperCase()) {
            case "K" -> KING;
            case "Q" -> QUEEN;
            case "R" -> ROOK;
            case "B" -> BISHOP;
            case "N" -> KNIGHT;
            default -> PAWN;
        };
    }
    
    /**
     * Returns true if this piece can slide (Queen, Rook, Bishop)
     */
    public boolean isSliding() {
        return this == QUEEN || this == ROOK || this == BISHOP;
    }
}
