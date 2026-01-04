# ♛ ChessMaster Pro

A professional desktop chess game built with Java and JavaFX featuring an intelligent AI opponent.

## Features

- **Full Chess Rules Implementation**
  - All standard piece movements
  - Castling (kingside and queenside)
  - En passant capture
  - Pawn promotion
  
- **Intelligent AI Engine**
  - Minimax algorithm with Alpha-Beta pruning
  - Piece-square tables for positional evaluation
  - Quiescence search to avoid horizon effect
  - Configurable difficulty (depth-based)

- **Game Status Detection**
  - Check and checkmate detection
  - Stalemate detection
  - Draw by 50-move rule
  - Draw by threefold repetition
  - Draw by insufficient material

- **Modern Premium GUI**
  - Beautiful gradient backgrounds
  - Animated piece selection
  - Move highlighting
  - Captured pieces display
  - Move history panel

## Game Modes

1. **Player vs AI (You: White)** - Play as white against the computer
2. **Player vs AI (You: Black)** - Play as black against the computer
3. **Player vs Player** - Two human players on the same computer

## Requirements

- **Java 21** or higher (bundled in installer)
- Windows 10/11 (64-bit)

## Installation

### Option 1: Windows Installer (Recommended) ⭐

1. Double-click `SetupChess.exe` (located in `dist/` folder)
2. Follow the installation wizard
3. Choose installation directory
4. Select shortcut options (Desktop, Start Menu)
5. Launch ChessMaster from Desktop or Start Menu!

**Note**: The installer includes a bundled Java runtime, so you don't need to install Java separately!

### Option 2: Run from Distribution ZIP

1. Extract `ChessMaster-1.0.0.zip` from `app/build/distributions/`
2. Navigate to the `bin` folder
3. Run:
   - **Windows**: Double-click `ChessMaster.bat`
   - **Linux/macOS**: Run `./ChessMaster` in terminal

**Note**: This option requires Java 21+ to be installed on your system.

### Option 3: Build from Source


```bash
# Clone the repository
git clone <repository-url>
cd aplikasiedane

# Build the project
./gradlew build

# Run the application
./gradlew run

# Create distribution
./gradlew distZip
```

## How to Play

1. **Select a piece**: Click on any of your pieces
2. **View valid moves**: Green dots show where you can move, red rings show captures
3. **Make a move**: Click on a highlighted square
4. **Special moves**:
   - Castling: Move king 2 squares toward the rook
   - En passant: Capture a pawn that just moved 2 squares
   - Promotion: Automatically promotes to Queen (when pawn reaches end)

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl+N | New Game |
| Ctrl+Q | Quit |

## Project Structure

```
app/
├── src/main/java/
│   ├── com/chessmaster/
│   │   ├── ChessApplication.java    # Main entry point
│   │   ├── controller/
│   │   │   └── GameController.java  # Game flow management
│   │   ├── engine/
│   │   │   └── ChessEngine.java     # AI with Minimax
│   │   ├── model/
│   │   │   ├── Board.java           # Board state & rules
│   │   │   ├── Piece.java           # Piece logic
│   │   │   ├── Move.java            # Move representation
│   │   │   ├── Position.java        # Board position
│   │   │   ├── PieceColor.java      # White/Black enum
│   │   │   ├── PieceType.java       # Piece types enum
│   │   │   └── GameState.java       # Game state management
│   │   └── view/
│   │       ├── ChessBoardView.java  # Board visualization
│   │       ├── GameInfoPanel.java   # Side panel
│   │       └── MenuBarView.java     # Menu bar
│   └── module-info.java
└── src/main/resources/
    └── styles/
        └── chess.css                 # Premium styling
```

## Technical Details

### AI Algorithm

The chess engine uses:
- **Minimax** with **Alpha-Beta pruning** for efficient tree search
- **Piece-Square Tables** for positional evaluation
- **Quiescence Search** to avoid the horizon effect
- **Move Ordering** (MVV-LVA) for better pruning

### Evaluation Function

Pieces are valued as:
- Pawn: 100 centipawns
- Knight: 300 centipawns
- Bishop: 300 centipawns
- Rook: 500 centipawns
- Queen: 900 centipawns

Additional factors:
- Mobility bonus
- King safety
- Positional bonuses from piece-square tables

## Building for Other Platforms

To create a native installer (requires WiX Toolset for Windows):

```bash
# Create installer
./gradlew jpackage
```

## License

MIT License - Feel free to use and modify!

## Credits

Built with ❤ using:
- Java 21
- JavaFX 21
- Gradle with Kotlin DSL
