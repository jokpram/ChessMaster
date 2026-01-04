package com.chessmaster.model;

/**
 * Represents a chess move with all its properties
 */
public class Move {
    
    public enum MoveType {
        NORMAL,
        DOUBLE_PAWN_PUSH,
        EN_PASSANT,
        CASTLING_KINGSIDE,
        CASTLING_QUEENSIDE,
        PROMOTION
    }
    
    private final Position from;
    private final Position to;
    private final Piece piece;
    private final Piece capturedPiece;
    private final MoveType type;
    private final PieceType promotionType;
    private boolean causesCheck;
    private boolean causesCheckmate;
    
    public Move(Position from, Position to, Piece piece, Piece capturedPiece, 
            MoveType type, PieceType promotionType) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.type = type;
        this.promotionType = promotionType;
        this.causesCheck = false;
        this.causesCheckmate = false;
    }
    
    public Position getFrom() {
        return from;
    }
    
    public Position getTo() {
        return to;
    }
    
    public Piece getPiece() {
        return piece;
    }
    
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
    
    public MoveType getType() {
        return type;
    }
    
    public PieceType getPromotionType() {
        return promotionType;
    }
    
    public boolean isCapture() {
        return capturedPiece != null;
    }
    
    public boolean isPromotion() {
        return type == MoveType.PROMOTION;
    }
    
    public boolean isCastling() {
        return type == MoveType.CASTLING_KINGSIDE || type == MoveType.CASTLING_QUEENSIDE;
    }
    
    public boolean causesCheck() {
        return causesCheck;
    }
    
    public void setCausesCheck(boolean causesCheck) {
        this.causesCheck = causesCheck;
    }
    
    public boolean causesCheckmate() {
        return causesCheckmate;
    }
    
    public void setCausesCheckmate(boolean causesCheckmate) {
        this.causesCheckmate = causesCheckmate;
    }
    
    /**
     * Converts this move to algebraic notation
     */
    public String toAlgebraic() {
        StringBuilder sb = new StringBuilder();
        
        // Castling notation
        if (type == MoveType.CASTLING_KINGSIDE) {
            return causesCheckmate ? "O-O#" : (causesCheck ? "O-O+" : "O-O");
        }
        if (type == MoveType.CASTLING_QUEENSIDE) {
            return causesCheckmate ? "O-O-O#" : (causesCheck ? "O-O-O+" : "O-O-O");
        }
        
        // Piece type (not for pawns)
        if (piece.getType() != PieceType.PAWN) {
            sb.append(piece.getType().getNotation());
        }
        
        // From square for disambiguation (simplified - just add file for pawns capturing)
        if (piece.getType() == PieceType.PAWN && isCapture()) {
            sb.append((char) ('a' + from.col()));
        }
        
        // Capture symbol
        if (isCapture()) {
            sb.append("x");
        }
        
        // Destination square
        sb.append(to.toAlgebraic());
        
        // Promotion
        if (isPromotion() && promotionType != null) {
            sb.append("=").append(promotionType.getNotation());
        }
        
        // Check/checkmate
        if (causesCheckmate) {
            sb.append("#");
        } else if (causesCheck) {
            sb.append("+");
        }
        
        return sb.toString();
    }
    
    /**
     * Returns a simple string representation (from-to)
     */
    public String toSimpleString() {
        return from.toAlgebraic() + to.toAlgebraic();
    }
    
    @Override
    public String toString() {
        return toAlgebraic();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from.equals(move.from) && to.equals(move.to) && 
               type == move.type && promotionType == move.promotionType;
    }
    
    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (promotionType != null ? promotionType.hashCode() : 0);
        return result;
    }
}
