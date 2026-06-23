# Mesos - Software Engineering Project
**Politecnico di Milano - Academic Year 2025/2026**

This repository contains the digital implementation of the board game Mesos, developed as the final project for the Software Engineering course at Politecnico di Milano.

The project features a distributed Client-Server architecture, supporting both RMI and Socket connections, with custom graphical and text-based user interfaces.

## Team Members (Group IS-AM46)
* Orazio Lorenzo Cioffi
* Manuel Cardia
* Riccardo Bresciani
* Luca Cassani

---

## Implemented Features
The application fulfills the requirements specified in the project assignment, specifically:
* **Game Rules:** Implementation of the Complete Rules of Mesos.
* **Architecture:** Distributed MVC (Model-View-Controller).
* **Network Protocols:** Dual implementation using both RMI and TCP Sockets. Players can choose their preferred connection method at runtime.
* **User Interfaces:**
    * GUI (Graphical User Interface) developed with JavaFX.
    * TUI (Textual User Interface) for command-line usage.
* **Advanced Features:**
    * **Resilience to Disconnections:** The server handles sudden client disconnections gracefully. Disconnected players have their turns skipped automatically. If only one player remains active, a timeout is triggered before declaring a default victory. Players can safely reconnect and resume their match.
    * **Global Leaderboard on DB (Classifica partite su DB):** The server maintains a history of match results on an external database. At the end of a match, the client displays the player's position in the global ranking based on the number of players in the match.

---

## Requirements
To compile and run the application, the following software is required:
* **Java:** JDK 21 (Java 17 is the minimum supported version).
* **Maven:** Used for dependency management and building the project.

---

## Deliverables
The pre-compiled executable JAR files and the UML documentation are located in the `deliveries` directory of this repository.

---

## How to Compile (Optional)
If you wish to recompile the project from the source code instead of using the provided executables, navigate to the root directory of the project and run the following Maven command:

```bash
mvn clean package
```

This will generate the "Fat JARs" for the Server and the Client inside the `target` directory.

## How to Run
Navigate to the directory containing the executable JAR files (e.g., the `deliveries` folder) and follow these steps:

### 1. Starting the Server
The Server must be started before any client can connect. Open a terminal and run:

```bash
java -jar am46-1.0-SNAPSHOT-Server.jar
```

The server will initialize and begin listening for incoming client connections.

### 2. Starting a Client
Open a new terminal window in the same folder and run:

```bash
java -jar am46-1.0-SNAPSHOT-Client.jar
```

Upon launching, the Client application will prompt you to choose:
* The User Interface (GUI or TUI).
* The Network Protocol (RMI or Socket).
* The Server IP Address (use `localhost` or `127.0.0.1` if running locally).

You can open multiple instances of the Client in separate terminals to simulate a multiplayer match on the same machine.
