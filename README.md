# go-game-client-server

A client-server implementation of the game of Go (Weiqi/Baduk) in Java, featuring both console and JavaFX graphical interfaces.

## Features

- **Multiple board sizes**: 9x9, 13x13, and 19x19
- **Client-server architecture**: Play against remote opponents
- **Two UI options**:
  - Console-based interface (ASCII art)
  - JavaFX graphical interface (visual board with mouse input)
- **Game mechanics**: Stone placement, captures, ko rule, scoring
- **Network protocol**: Custom `GameMessage` system for client-server communication

## Quick Start

### Running the Server
```bash
mvn clean compile
mvn exec:java -Pserver
```

### Running the Console Client
```bash
mvn exec:java -Pconsole
```

### Running the JavaFX GUI Client
```bash
mvn exec:java -Pgui
```

**PowerShell Alternative:** Use quotes if using `-D` syntax:
```powershell
mvn exec:java "-Dexec.mainClass=com.example.goboard.network.GameServer"
```

See [JAVAFX_GUI.md](JAVAFX_GUI.md) for detailed JavaFX documentation.

## Project Structure

- **Model**: `Board`, `Stone`, `Intersection` - core game logic
- **Controller**: `GameController` with state pattern (Playing/Scoring/GameOver)
- **Network**: `GameServer`, `GameClient`, `GameMessage` protocol
- **View**: 
  - Console: `ConsoleGameUI`, `AsciiBoardRenderer`
  - JavaFX: `JavaFXGameClient`, `BoardView`, `GameUIFX`
- **Strategy**: `MoveValidator` for game rule validation

## Requirements

- Java 21+
- Maven 3.8+
- JavaFX 21+ (for GUI client)