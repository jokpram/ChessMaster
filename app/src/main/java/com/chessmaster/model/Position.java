package com.chessmaster.model;

/**
 * Represents a position on the chess board
 */
public record Position(int row, int col) {
    
    public Position {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            throw new IllegalArgumentException("Position must be within 0-7 range");
        }
    }
    
    /**
     * Creates a position from algebraic notation (e.g., "e4", "a1")
     */
    public static Position fromAlgebraic(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + notation);
        }
        char file = Character.toLowerCase(notation.charAt(0));
        char rank = notation.charAt(1);
        
        int col = file - 'a';
        int row = rank - '1';
        
        return new Position(row, col);
    }
    
    /**
     * Converts this position to algebraic notation
     */
    public String toAlgebraic() {
        char file = (char) ('a' + col);
        char rank = (char) ('1' + row);
        return "" + file + rank;
    }
    
    /**
     * Creates a new position offset by the given delta
     */
    public Position offset(int deltaRow, int deltaCol) {
        int newRow = row + deltaRow;
        int newCol = col + deltaCol;
        
        if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) {
            return null;
        }
        return new Position(newRow, newCol);
    }
    
    /**
     * Checks if position is valid on the board
     */
    public static boolean isValid(int row, int col) {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }
    
    /**
     * Returns true if this is a light square
     */
    public boolean isLightSquare() {
        return (row + col) % 2 == 1;
    }
    
    @Override
    public String toString() {
        return toAlgebraic();
    }
}
