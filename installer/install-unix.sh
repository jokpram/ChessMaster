#!/bin/bash
# ChessMaster Pro - Linux/macOS Installer Script

echo "==============================================="
echo "    ChessMaster Pro v1.0.0 - Installer"
echo "==============================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed"
    echo "Please install Java 21 or higher:"
    echo ""
    echo "Ubuntu/Debian:"
    echo "  sudo apt install openjdk-21-jdk"
    echo ""
    echo "Fedora:"
    echo "  sudo dnf install java-21-openjdk"
    echo ""
    echo "macOS (using Homebrew):"
    echo "  brew install openjdk@21"
    echo ""
    exit 1
fi

# Get Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "Detected: $JAVA_VERSION"
echo ""

# Set installation directory
if [[ "$OSTYPE" == "darwin"* ]]; then
    INSTALL_DIR="$HOME/Applications/ChessMaster"
else
    INSTALL_DIR="$HOME/.local/share/ChessMaster"
fi

echo "Installation directory: $INSTALL_DIR"
echo ""

# Create installation directory
mkdir -p "$INSTALL_DIR"
echo "Created installation directory."

# Copy files
echo ""
echo "Copying files..."
cp -r bin "$INSTALL_DIR/"
cp -r lib "$INSTALL_DIR/"
chmod +x "$INSTALL_DIR/bin/ChessMaster"
echo "Files copied successfully."

# Create desktop entry (Linux only)
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo ""
    echo "Creating desktop entry..."
    
    DESKTOP_FILE="$HOME/.local/share/applications/chessmaster.desktop"
    
    cat > "$DESKTOP_FILE" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=ChessMaster Pro
Comment=Professional Chess Game with AI
Exec=$INSTALL_DIR/bin/ChessMaster
Icon=chess
Terminal=false
Categories=Game;BoardGame;
Keywords=chess;game;board;strategy;
EOF

    echo "Desktop entry created at: $DESKTOP_FILE"
fi

# Create symlink in /usr/local/bin (optional)
if [[ -w "/usr/local/bin" ]]; then
    ln -sf "$INSTALL_DIR/bin/ChessMaster" /usr/local/bin/chessmaster
    echo "Created symlink: /usr/local/bin/chessmaster"
fi

echo ""
echo "==============================================="
echo "    Installation Complete!"
echo "==============================================="
echo ""
echo "ChessMaster has been installed to: $INSTALL_DIR"
echo ""
echo "To run ChessMaster:"
echo "  $INSTALL_DIR/bin/ChessMaster"
echo ""

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "Or find it in your application menu."
fi

echo ""
echo "Enjoy playing chess!"
