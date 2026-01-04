package com.chessmaster.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the chess board state with all pieces and game logic
 */
public class Board {
    
    private final Piece[][] squares;
    private Position enPassantTarget;
    private Position whiteKingPosition;
    private Position blackKingPosition;
    
    public Board() {
        squares = new Piece[8][8];
        setupInitialPosition();
    }
    
    /**
     * Creates a deep copy of the board
     */
    public Board copy() {
        Board copy = new Board();
        copy.clear();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (squares[row][col] != null) {
                    copy.squares[row][col] = squares[row][col].copy();
                }
            }
        }
        
        copy.enPassantTarget = this.enPassantTarget;
        copy.whiteKingPosition = this.whiteKingPosition;
        copy.blackKingPosition = this.blackKingPosition;
        
        return copy;
    }
    
    private void clear() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = null;
            }
        }
    }
    
    /**
     * Sets up the initial chess position
     */
    public void setupInitialPosition() {
        clear();
        
        // White pieces (row 0 and 1)
        squares[0][0] = new Piece(PieceType.ROOK, PieceColor.WHITE);
        squares[0][1] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        squares[0][2] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        squares[0][3] = new Piece(PieceType.QUEEN, PieceColor.WHITE);
        squares[0][4] = new Piece(PieceType.KING, PieceColor.WHITE);
        squares[0][5] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        squares[0][6] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        squares[0][7] = new Piece(PieceType.ROOK, PieceColor.WHITE);
        
        for (int col = 0; col < 8; col++) {
            squares[1][col] = new Piece(PieceType.PAWN, PieceColor.WHITE);
        }
        
        // Black pieces (row 6 and 7)
        squares[7][0] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        squares[7][1] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        squares[7][2] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        squares[7][3] = new Piece(PieceType.QUEEN, PieceColor.BLACK);
        squares[7][4] = new Piece(PieceType.KING, PieceColor.BLACK);
        squares[7][5] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        squares[7][6] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        squares[7][7] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        
        for (int col = 0; col < 8; col++) {
            squares[6][col] = new Piece(PieceType.PAWN, PieceColor.BLACK);
        }
        
        whiteKingPosition = new Position(0, 4);
        blackKingPosition = new Position(7, 4);
        enPassantTarget = null;
    }
    
    public Piece getPiece(Position pos) {
        return squares[pos.row()][pos.col()];
    }
    
    public Piece getPiece(int row, int col) {
        return squares[row][col];
    }
    
    public void setPiece(Position pos, Piece piece) {
        squares[pos.row()][pos.col()] = piece;
        
        // Update king position tracking
        if (piece != null && piece.getType() == PieceType.KING) {
            if (piece.getColor() == PieceColor.WHITE) {
                whiteKingPosition = pos;
            } else {
                blackKingPosition = pos;
            }
        }
    }
    
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }
    
    public void setEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }
    
    public Position getKingPosition(PieceColor color) {
        return color == PieceColor.WHITE ? whiteKingPosition : blackKingPosition;
    }
    
    /**
     * Executes a move on the board without validation
     */
    public void makeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPiece();
        
        // Handle special moves
        switch (move.getType()) {
            case CASTLING_KINGSIDE -> {
                // Move king
                squares[to.row()][to.col()] = piece;
                squares[from.row()][from.col()] = null;
                piece.setHasMoved(true);
                
                // Move rook
                int backRank = piece.getColor().getBackRank();
                Piece rook = squares[backRank][7];
                squares[backRank][5] = rook;
                squares[backRank][7] = null;
                rook.setHasMoved(true);
                
                updateKingPosition(piece.getColor(), to);
            }
            
            case CASTLING_QUEENSIDE -> {
                // Move king
                squares[to.row()][to.col()] = piece;
                squares[from.row()][from.col()] = null;
                piece.setHasMoved(true);
                
                // Move rook
                int backRank = piece.getColor().getBackRank();
                Piece rook = squares[backRank][0];
                squares[backRank][3] = rook;
                squares[backRank][0] = null;
                rook.setHasMoved(true);
                
                updateKingPosition(piece.getColor(), to);
            }
            
            case EN_PASSANT -> {
                // Move pawn
                squares[to.row()][to.col()] = piece;
                squares[from.row()][from.col()] = null;
                piece.setHasMoved(true);
                
                // Remove captured pawn
                squares[from.row()][to.col()] = null;
            }
            
            case PROMOTION -> {
                // Replace pawn with promoted piece
                Piece promotedPiece = new Piece(move.getPromotionType(), piece.getColor());
                promotedPiece.setHasMoved(true);
                squares[to.row()][to.col()] = promotedPiece;
                squares[from.row()][from.col()] = null;
            }
            
            case DOUBLE_PAWN_PUSH -> {
                squares[to.row()][to.col()] = piece;
                squares[from.row()][from.col()] = null;
                piece.setHasMoved(true);
                
                // Set en passant target
                int direction = piece.getColor().getPawnDirection();
                enPassantTarget = new Position(from.row() + direction, from.col());
            }
            
            default -> {
                // Normal move
                squares[to.row()][to.col()] = piece;
                squares[from.row()][from.col()] = null;
                piece.setHasMoved(true);
                
                if (piece.getType() == PieceType.KING) {
                    updateKingPosition(piece.getColor(), to);
                }
            }
        }
        
        // Clear en passant target if this wasn't a double pawn push
        if (move.getType() != Move.MoveType.DOUBLE_PAWN_PUSH) {
            enPassantTarget = null;
        }
    }
    
    private void updateKingPosition(PieceColor color, Position pos) {
        if (color == PieceColor.WHITE) {
            whiteKingPosition = pos;
        } else {
            blackKingPosition = pos;
        }
    }
    
    /**
     * Checks if a square is attacked by any piece of the given color
     */
    public boolean isSquareAttacked(Position pos, PieceColor attackerColor) {
        // Check for pawn attacks
        int pawnDir = attackerColor.opposite().getPawnDirection();
        for (int dc : new int[]{-1, 1}) {
            Position pawnPos = pos.offset(pawnDir, dc);
            if (pawnPos != null) {
                Piece piece = getPiece(pawnPos);
                if (piece != null && piece.getColor() == attackerColor && 
                        piece.getType() == PieceType.PAWN) {
                    return true;
                }
            }
        }
        
        // Check for knight attacks
        int[][] knightOffsets = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        for (int[] offset : knightOffsets) {
            Position knightPos = pos.offset(offset[0], offset[1]);
            if (knightPos != null) {
                Piece piece = getPiece(knightPos);
                if (piece != null && piece.getColor() == attackerColor && 
                        piece.getType() == PieceType.KNIGHT) {
                    return true;
                }
            }
        }
        
        // Check for king attacks
        int[][] kingOffsets = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
        };
        for (int[] offset : kingOffsets) {
            Position kingPos = pos.offset(offset[0], offset[1]);
            if (kingPos != null) {
                Piece piece = getPiece(kingPos);
                if (piece != null && piece.getColor() == attackerColor && 
                        piece.getType() == PieceType.KING) {
                    return true;
                }
            }
        }
        
        // Check for sliding piece attacks (bishop, rook, queen)
        int[][] bishopDirs = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        int[][] rookDirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : bishopDirs) {
            if (checkSlidingAttack(pos, dir, attackerColor, PieceType.BISHOP, PieceType.QUEEN)) {
                return true;
            }
        }
        
        for (int[] dir : rookDirs) {
            if (checkSlidingAttack(pos, dir, attackerColor, PieceType.ROOK, PieceType.QUEEN)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean checkSlidingAttack(Position pos, int[] dir, PieceColor attackerColor, 
            PieceType type1, PieceType type2) {
        Position current = pos;
        while (true) {
            current = current.offset(dir[0], dir[1]);
            if (current == null) break;
            
            Piece piece = getPiece(current);
            if (piece != null) {
                if (piece.getColor() == attackerColor && 
                        (piece.getType() == type1 || piece.getType() == type2)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }
    
    /**
     * Checks if the king of the given color is in check
     */
    public boolean isInCheck(PieceColor color) {
        Position kingPos = getKingPosition(color);
        return isSquareAttacked(kingPos, color.opposite());
    }
    
    /**
     * Generate all legal moves for the given color
     */
    public List<Move> generateLegalMoves(PieceColor color) {
        List<Move> legalMoves = new ArrayList<>();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.getColor() == color) {
                    Position pos = new Position(row, col);
                    List<Move> pseudoLegal = piece.generatePseudoLegalMoves(this, pos);
                    
                    for (Move move : pseudoLegal) {
                        if (isMoveLegal(move, color)) {
                            // Check if move causes check/checkmate
                            Board testBoard = this.copy();
                            testBoard.makeMove(move);
                            if (testBoard.isInCheck(color.opposite())) {
                                move.setCausesCheck(true);
                                if (testBoard.generateLegalMoves(color.opposite()).isEmpty()) {
                                    move.setCausesCheckmate(true);
                                }
                            }
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }
        
        return legalMoves;
    }
    
    /**
     * Checks if a move is legal (doesn't leave own king in check)
     */
    public boolean isMoveLegal(Move move, PieceColor color) {
        Board testBoard = this.copy();
        testBoard.makeMove(move);
        return !testBoard.isInCheck(color);
    }
    
    /**
     * Find all pieces of a given color
     */
    public List<Position> findPieces(PieceColor color) {
        List<Position> positions = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.getColor() == color) {
                    positions.add(new Position(row, col));
                }
            }
        }
        return positions;
    }
    
    /**
     * Calculate material balance (positive = white advantage)
     */
    public int getMaterialBalance() {
        int balance = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null) {
                    int value = piece.getValue();
                    balance += piece.getColor() == PieceColor.WHITE ? value : -value;
                }
            }
        }
        return balance;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = squares[r][c];
                if (p == null) sb.append(".");
                else sb.append(p.toString());
            }
        }
        sb.append(enPassantTarget != null ? enPassantTarget.toString() : "-");
        return sb.toString();
    }
}

