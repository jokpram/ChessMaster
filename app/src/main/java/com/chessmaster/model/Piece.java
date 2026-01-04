package com.chessmaster.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chess piece with its type, color, and movement logic
 */
public class Piece {
    private final PieceType type;
    private final PieceColor color;
    private boolean hasMoved;
    
    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }
    
    public PieceType getType() {
        return type;
    }
    
    public PieceColor getColor() {
        return color;
    }
    
    public boolean hasMoved() {
        return hasMoved;
    }
    
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    
    public String getSymbol() {
        return type.getSymbol(color);
    }
    
    public int getValue() {
        return type.getValue();
    }
    
    /**
     * Generate all pseudo-legal moves for this piece from the given position
     * Does not check if moves leave king in check
     */
    public List<Move> generatePseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        
        switch (type) {
            case PAWN -> generatePawnMoves(board, from, moves);
            case KNIGHT -> generateKnightMoves(board, from, moves);
            case BISHOP -> generateSlidingMoves(board, from, moves, BISHOP_DIRECTIONS);
            case ROOK -> generateSlidingMoves(board, from, moves, ROOK_DIRECTIONS);
            case QUEEN -> generateSlidingMoves(board, from, moves, QUEEN_DIRECTIONS);
            case KING -> generateKingMoves(board, from, moves);
        }
        
        return moves;
    }
    
    private static final int[][] KNIGHT_OFFSETS = {
        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
        {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };
    
    private static final int[][] KING_OFFSETS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1}, {0, 1},
        {1, -1}, {1, 0}, {1, 1}
    };
    
    private static final int[][] BISHOP_DIRECTIONS = {
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };
    
    private static final int[][] ROOK_DIRECTIONS = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };
    
    private static final int[][] QUEEN_DIRECTIONS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1}, {0, 1},
        {1, -1}, {1, 0}, {1, 1}
    };
    
    private void generatePawnMoves(Board board, Position from, List<Move> moves) {
        int direction = color.getPawnDirection();
        int startRow = color.getPawnStartRow();
        int promotionRow = color.getPromotionRow();
        
        // Single push
        Position oneAhead = from.offset(direction, 0);
        if (oneAhead != null && board.getPiece(oneAhead) == null) {
            if (oneAhead.row() == promotionRow) {
                // Promotion
                for (PieceType promoType : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, 
                        PieceType.BISHOP, PieceType.KNIGHT}) {
                    moves.add(new Move(from, oneAhead, this, null, Move.MoveType.PROMOTION, promoType));
                }
            } else {
                moves.add(new Move(from, oneAhead, this, null, Move.MoveType.NORMAL, null));
            }
            
            // Double push from starting position
            if (from.row() == startRow) {
                Position twoAhead = from.offset(direction * 2, 0);
                if (twoAhead != null && board.getPiece(twoAhead) == null) {
                    moves.add(new Move(from, twoAhead, this, null, Move.MoveType.DOUBLE_PAWN_PUSH, null));
                }
            }
        }
        
        // Captures (including en passant)
        for (int dc : new int[]{-1, 1}) {
            Position capture = from.offset(direction, dc);
            if (capture != null) {
                Piece target = board.getPiece(capture);
                if (target != null && target.getColor() != color) {
                    if (capture.row() == promotionRow) {
                        // Capture with promotion
                        for (PieceType promoType : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, 
                                PieceType.BISHOP, PieceType.KNIGHT}) {
                            moves.add(new Move(from, capture, this, target, Move.MoveType.PROMOTION, promoType));
                        }
                    } else {
                        moves.add(new Move(from, capture, this, target, Move.MoveType.NORMAL, null));
                    }
                }
                
                // En passant
                Position enPassantTarget = board.getEnPassantTarget();
                if (enPassantTarget != null && capture.equals(enPassantTarget)) {
                    Position capturedPawnPos = new Position(from.row(), capture.col());
                    Piece capturedPawn = board.getPiece(capturedPawnPos);
                    moves.add(new Move(from, capture, this, capturedPawn, Move.MoveType.EN_PASSANT, null));
                }
            }
        }
    }
    
    private void generateKnightMoves(Board board, Position from, List<Move> moves) {
        for (int[] offset : KNIGHT_OFFSETS) {
            Position to = from.offset(offset[0], offset[1]);
            if (to != null) {
                Piece target = board.getPiece(to);
                if (target == null || target.getColor() != color) {
                    moves.add(new Move(from, to, this, target, Move.MoveType.NORMAL, null));
                }
            }
        }
    }
    
    private void generateSlidingMoves(Board board, Position from, List<Move> moves, int[][] directions) {
        for (int[] dir : directions) {
            Position current = from;
            while (true) {
                current = current.offset(dir[0], dir[1]);
                if (current == null) break;
                
                Piece target = board.getPiece(current);
                if (target == null) {
                    moves.add(new Move(from, current, this, null, Move.MoveType.NORMAL, null));
                } else {
                    if (target.getColor() != color) {
                        moves.add(new Move(from, current, this, target, Move.MoveType.NORMAL, null));
                    }
                    break;
                }
            }
        }
    }
    
    private void generateKingMoves(Board board, Position from, List<Move> moves) {
        // Normal king moves
        for (int[] offset : KING_OFFSETS) {
            Position to = from.offset(offset[0], offset[1]);
            if (to != null) {
                Piece target = board.getPiece(to);
                if (target == null || target.getColor() != color) {
                    moves.add(new Move(from, to, this, target, Move.MoveType.NORMAL, null));
                }
            }
        }
        
        // Castling
        if (!hasMoved && !board.isSquareAttacked(from, color.opposite())) {
            int backRank = color.getBackRank();
            
            // Kingside castling
            Position kingsideRookPos = new Position(backRank, 7);
            Piece kingsideRook = board.getPiece(kingsideRookPos);
            if (kingsideRook != null && kingsideRook.getType() == PieceType.ROOK && 
                    !kingsideRook.hasMoved()) {
                Position f = new Position(backRank, 5);
                Position g = new Position(backRank, 6);
                if (board.getPiece(f) == null && board.getPiece(g) == null &&
                        !board.isSquareAttacked(f, color.opposite()) &&
                        !board.isSquareAttacked(g, color.opposite())) {
                    moves.add(new Move(from, g, this, null, Move.MoveType.CASTLING_KINGSIDE, null));
                }
            }
            
            // Queenside castling
            Position queensideRookPos = new Position(backRank, 0);
            Piece queensideRook = board.getPiece(queensideRookPos);
            if (queensideRook != null && queensideRook.getType() == PieceType.ROOK && 
                    !queensideRook.hasMoved()) {
                Position d = new Position(backRank, 3);
                Position c = new Position(backRank, 2);
                Position b = new Position(backRank, 1);
                if (board.getPiece(d) == null && board.getPiece(c) == null && 
                        board.getPiece(b) == null &&
                        !board.isSquareAttacked(d, color.opposite()) &&
                        !board.isSquareAttacked(c, color.opposite())) {
                    moves.add(new Move(from, c, this, null, Move.MoveType.CASTLING_QUEENSIDE, null));
                }
            }
        }
    }
    
    /**
     * Creates a copy of this piece
     */
    public Piece copy() {
        Piece copy = new Piece(type, color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
    
    @Override
    public String toString() {
        String colorChar = color == PieceColor.WHITE ? "w" : "b";
        String typeChar = type == PieceType.PAWN ? "p" : type.getNotation().toLowerCase();
        return colorChar + typeChar + (hasMoved ? "1" : "0"); // Add moved status for castling rights in hash
    }
}

