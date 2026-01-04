package com.chessmaster.model;

/**
 * Represents the color/side of a chess piece or player
 */
public enum PieceColor {
    WHITE,
    BLACK;
    
    /**
     * Returns the opposite color
     */
    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
    
    /**
     * Returns the starting row for pawns of this color
     */
    public int getPawnStartRow() {
        return this == WHITE ? 1 : 6;
    }
    
    /**
     * Returns the promotion row for pawns of this color
     */
    public int getPromotionRow() {
        return this == WHITE ? 7 : 0;
    }
    
    /**
     * Returns the direction pawns of this color move (1 for white, -1 for black)
     */
    public int getPawnDirection() {
        return this == WHITE ? 1 : -1;
    }
    
    /**
     * Returns the back rank for this color
     */
    public int getBackRank() {
        return this == WHITE ? 0 : 7;
    }
    
    @Override
    public String toString() {
        return this == WHITE ? "White" : "Black";
    }
}
