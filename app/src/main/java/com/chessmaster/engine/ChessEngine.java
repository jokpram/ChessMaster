package com.chessmaster.engine;

import com.chessmaster.model.*;
import java.util.*;

/**
 * Advanced Chess AI engine using Iterative Deepening Minimax with Alpha-Beta pruning.
 * Features: Transposition Table, Null Move Pruning, Killer Moves, and Enhanced Evaluation.
 */
public class ChessEngine {
    
    public enum Difficulty {
        EASY(2),
        MEDIUM(4),
        HARD(5);
        
        final int depth;
        Difficulty(int depth) { this.depth = depth; }
    }
    
    private int maxDepth = Difficulty.MEDIUM.depth;
    private static final int INITIAL_DEPTH = 1;
    private static final int QUIESCENCE_DEPTH = 4;
    
    public void setDifficulty(Difficulty difficulty) {
        this.maxDepth = difficulty.depth;
    }
    
    // Evaluation Constants
    private static final int CHECKMATE_SCORE = 100000;
    private static final int STALEMATE_SCORE = 0;
    
    // Transposition Table Entry
    private static class TTEntry {
        int score;
        int depth;
        int flag; // 0: Exact, 1: Lower bound (Alpha), 2: Upper bound (Beta)
        Move bestMove;
    }
    
    private final Map<String, TTEntry> transpositionTable = new HashMap<>(16384);
    private final Move[][] killerMoves = new Move[64][2]; // Fixed size for dynamic depth
    
    private int nodesSearched;
    private Move bestMoveIteration;
    
    public ChessEngine() {
        this.nodesSearched = 0;
    }
    
    /**
     * Find the best move using Iterative Deepening
     */
    public Move findBestMove(GameState gameState) {
        nodesSearched = 0;
        Move absoluteBestMove = null;
        PieceColor color = gameState.getCurrentTurn();
        
        // Iterative Deepening
        for (int depth = INITIAL_DEPTH; depth <= maxDepth; depth++) {
            bestMoveIteration = null;
            negamax(gameState.getBoard(), depth, -CHECKMATE_SCORE * 2, CHECKMATE_SCORE * 2, color, isEndgame(gameState.getBoard()), 0);
            
            if (bestMoveIteration != null) {
                absoluteBestMove = bestMoveIteration;
            }
        }
        
        return absoluteBestMove;
    }
    
    /**
     * Negamax search with Alpha-Beta pruning and optimizations
     */
    private int negamax(Board board, int depth, int alpha, int beta, PieceColor color, boolean endgame, int ply) {
        nodesSearched++;
        
        // 1. Transposition Table Lookup
        String hash = getBoardHash(board, color);
        TTEntry tt = transpositionTable.get(hash);
        if (tt != null && tt.depth >= depth) {
            if (tt.flag == 0) return tt.score;
            if (tt.flag == 1 && tt.score > alpha) alpha = tt.score;
            if (tt.flag == 2 && tt.score < beta) beta = tt.score;
            if (alpha >= beta) return tt.score;
        }

        // 2. Base Case
        if (depth <= 0) {
            return quiescenceSearch(board, QUIESCENCE_DEPTH, alpha, beta, color, endgame);
        }

        List<Move> legalMoves = board.generateLegalMoves(color);
        if (legalMoves.isEmpty()) {
            if (board.isInCheck(color)) return -CHECKMATE_SCORE + ply;
            return STALEMATE_SCORE;
        }

        // 3. Null Move Pruning
        if (depth >= 3 && !board.isInCheck(color) && !endgame) {
            // R = 2 or 3
            int score = -negamax(board, depth - 1 - 2, -beta, -beta + 1, color.opposite(), endgame, ply + 1);
            if (score >= beta) return beta;
        }

        // 4. Move Ordering
        Move ttMove = (tt != null) ? tt.bestMove : null;
        orderMoves(legalMoves, board, ttMove, ply);

        int bestScore = -CHECKMATE_SCORE * 2;
        Move currentBestMove = null;
        int oldAlpha = alpha;

        for (Move move : legalMoves) {
            Board boardCopy = board.copy();
            boardCopy.makeMove(move);
            
            int score = -negamax(boardCopy, depth - 1, -beta, -alpha, color.opposite(), endgame, ply + 1);
            
            if (score > bestScore) {
                bestScore = score;
                currentBestMove = move;
                if (ply == 0) bestMoveIteration = move;
            }
            
            alpha = Math.max(alpha, score);
            if (alpha >= beta) {
                // 5. Killer Move Heuristic
                if (!move.isCapture()) {
                    killerMoves[ply][1] = killerMoves[ply][0];
                    killerMoves[ply][0] = move;
                }
                break;
            }
        }

        // 6. Transposition Table Store
        TTEntry newEntry = new TTEntry();
        newEntry.score = bestScore;
        newEntry.depth = depth;
        newEntry.bestMove = currentBestMove;
        if (bestScore <= oldAlpha) newEntry.flag = 2;
        else if (bestScore >= beta) newEntry.flag = 1;
        else newEntry.flag = 0;
        transpositionTable.put(hash, newEntry);

        return bestScore;
    }

    private int quiescenceSearch(Board board, int depth, int alpha, int beta, PieceColor color, boolean endgame) {
        int standPat = evaluate(board, color, endgame);
        if (standPat >= beta) return beta;
        if (alpha < standPat) alpha = standPat;
        if (depth <= 0) return alpha;

        List<Move> captures = board.generateLegalMoves(color).stream()
                .filter(Move::isCapture)
                .toList();
        
        // Simple sorting for captures
        captures = new ArrayList<>(captures);
        captures.sort((a, b) -> (10 * b.getCapturedPiece().getValue() - b.getPiece().getValue()) - 
                                (10 * a.getCapturedPiece().getValue() - a.getPiece().getValue()));

        for (Move move : captures) {
            Board boardCopy = board.copy();
            boardCopy.makeMove(move);
            int score = -quiescenceSearch(boardCopy, depth - 1, -beta, -alpha, color.opposite(), endgame);
            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }
        return alpha;
    }

    private void orderMoves(List<Move> moves, Board board, Move ttMove, int ply) {
        moves.sort((a, b) -> {
            // 1. PV Move (from TT)
            if (a.equals(ttMove)) return -1000000;
            if (b.equals(ttMove)) return 1000000;
            
            // 2. MVV-LVA for captures
            if (a.isCapture() || b.isCapture()) {
                int scoreA = a.isCapture() ? (10 * a.getCapturedPiece().getValue() - a.getPiece().getValue() + 10000) : 0;
                int scoreB = b.isCapture() ? (10 * b.getCapturedPiece().getValue() - b.getPiece().getValue() + 10000) : 0;
                return scoreB - scoreA;
            }
            
            // 3. Killer Moves
            if (a.equals(killerMoves[ply][0])) return -5000;
            if (b.equals(killerMoves[ply][0])) return 5000;
            if (a.equals(killerMoves[ply][1])) return -2000;
            if (b.equals(killerMoves[ply][1])) return 2000;
            
            return 0;
        });
    }

    /**
     * Professional evaluation including piece-square tables, mobility, and positional factors.
     */
    private int evaluate(Board board, PieceColor color, boolean endgame) {
        int score = 0;
        
        int whiteMaterial = 0;
        int blackMaterial = 0;
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    int val = getPieceValue(piece, row, col, endgame);
                    if (piece.getColor() == PieceColor.WHITE) {
                        whiteMaterial += piece.getValue();
                        score += val;
                    } else {
                        blackMaterial += piece.getValue();
                        score -= val;
                    }
                }
            }
        }
        
        // 1. Center Control (e4, d4, e5, d5 bonus)
        score += evaluateCenterControl(board);
        
        // 2. King Safety
        score += evaluateKingSafety(board, whiteMaterial, blackMaterial);
        
        return color == PieceColor.WHITE ? score : -score;
    }

    private int evaluateCenterControl(Board board) {
        int bonus = 0;
        Position[] center = {new Position(3,3), new Position(3,4), new Position(4,3), new Position(4,4)};
        for (Position p : center) {
            Piece pc = board.getPiece(p);
            if (pc != null) {
                int val = (pc.getType() == PieceType.PAWN) ? 20 : 10;
                bonus += (pc.getColor() == PieceColor.WHITE) ? val : -val;
            }
        }
        return bonus;
    }

    private int evaluateKingSafety(Board board, int whiteMat, int blackMat) {
        int safety = 0;
        // White King Safety
        Position wKing = board.getKingPosition(PieceColor.WHITE);
        if (blackMat > 10) { // Only matters if black has enough material to attack
            safety += countPawnShield(board, wKing, PieceColor.WHITE);
        }
        // Black King Safety
        Position bKing = board.getKingPosition(PieceColor.BLACK);
        if (whiteMat > 10) {
            safety -= countPawnShield(board, bKing, PieceColor.BLACK);
        }
        return safety;
    }

    private int countPawnShield(Board board, Position kingPos, PieceColor side) {
        int shield = 0;
        int dir = side == PieceColor.WHITE ? 1 : -1;
        for (int dc = -1; dc <= 1; dc++) {
            Position p = kingPos.offset(dir, dc);
            if (p != null) {
                Piece pc = board.getPiece(p);
                if (pc != null && pc.getType() == PieceType.PAWN && pc.getColor() == side) {
                    shield += 15;
                }
            }
        }
        return shield;
    }

    private int getPieceValue(Piece piece, int row, int col, boolean endgame) {
        int baseValue = piece.getValue() * 100;
        int r = piece.getColor() == PieceColor.WHITE ? 7 - row : row;
        int posBonus = switch (piece.getType()) {
            case PAWN -> PAWN_TABLE[r][col];
            case KNIGHT -> KNIGHT_TABLE[r][col];
            case BISHOP -> BISHOP_TABLE[r][col];
            case ROOK -> ROOK_TABLE[r][col];
            case QUEEN -> QUEEN_TABLE[r][col];
            case KING -> endgame ? KING_ENDGAME_TABLE[r][col] : KING_MIDDLEGAME_TABLE[r][col];
        };
        return baseValue + posBonus;
    }

    private String getBoardHash(Board board, PieceColor currentTurn) {
        // Reduced hash for performance, ideally would use Zobrist
        return board.toString() + currentTurn;
    }

    private boolean isEndgame(Board board) {
        int pieceCount = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board.getPiece(r, c) != null) pieceCount++;
            }
        }
        return pieceCount <= 12;
    }

    // Piece-Square Tables (Keeping existing ones as they are quite good)
    private static final int[][] PAWN_TABLE = {{0,0,0,0,0,0,0,0}, {50,50,50,50,50,50,50,50}, {10,10,20,30,30,20,10,10}, {5,5,10,25,25,10,5,5}, {0,0,0,20,20,0,0,0}, {5,-5,-10,0,0,-10,-5,5}, {5,10,10,-20,-20,10,10,5}, {0,0,0,0,0,0,0,0}};
    private static final int[][] KNIGHT_TABLE = {{-50,-40,-30,-30,-30,-30,-40,-50}, {-40,-20,0,0,0,0,-20,-40}, {-30,0,10,15,15,10,0,-30}, {-30,5,15,20,20,15,5,-30}, {-30,0,15,20,20,15,0,-30}, {-30,5,10,15,15,10,5,-30}, {-40,-20,0,5,5,0,-20,-40}, {-50,-40,-30,-30,-30,-30,-40,-50}};
    private static final int[][] BISHOP_TABLE = {{-20,-10,-10,-10,-10,-10,-10,-20}, {-10,0,0,0,0,0,0,-10}, {-10,0,5,10,10,5,0,-10}, {-10,5,5,10,10,5,5,-10}, {-10,0,10,10,10,10,0,-10}, {-10,10,10,10,10,10,10,-10}, {-10,5,0,0,0,0,5,-10}, {-20,-10,-10,-10,-10,-10,-10,-20}};
    private static final int[][] ROOK_TABLE = {{0,0,0,0,0,0,0,0}, {5,10,10,10,10,10,10,5}, {-5,0,0,0,0,0,0,-5}, {-5,0,0,0,0,0,0,-5}, {-5,0,0,0,0,0,0,-5}, {-5,0,0,0,0,0,0,-5}, {-5,0,0,0,0,0,0,-5}, {0,0,0,5,5,0,0,0}};
    private static final int[][] QUEEN_TABLE = {{-20,-10,-10,-5,-5,-10,-10,-20}, {-10,0,0,0,0,0,0,-10}, {-10,0,5,5,5,5,0,-10}, {-5,0,5,5,5,5,0,-5}, {0,0,5,5,5,5,0,-5}, {-10,5,5,5,5,5,0,-10}, {-10,0,5,0,0,0,0,-10}, {-20,-10,-10,-5,-5,-10,-10,-20}};
    private static final int[][] KING_MIDDLEGAME_TABLE = {{-30,-40,-40,-50,-50,-40,-40,-30}, {-30,-40,-40,-50,-50,-40,-40,-30}, {-30,-40,-40,-50,-50,-40,-40,-30}, {-30,-40,-40,-50,-50,-40,-40,-30}, {-20,-30,-30,-40,-40,-30,-30,-20}, {-10,-20,-20,-20,-20,-20,-20,-10}, {20,20,0,0,0,0,20,20}, {20,30,10,0,0,10,30,20}};
    private static final int[][] KING_ENDGAME_TABLE = {{-50,-40,-30,-20,-20,-30,-40,-50}, {-30,-20,-10,0,0,-10,-20,-30}, {-30,-10,20,30,30,20,-10,-30}, {-30,-10,30,40,40,30,-10,-30}, {-30,-10,30,40,40,30,-10,-30}, {-30,-10,20,30,30,20,-10,-30}, {-30,-30,0,0,0,0,-30,-30}, {-50,-30,-30,-30,-30,-30,-30,-50}};

    public int getNodesSearched() { return nodesSearched; }
}
