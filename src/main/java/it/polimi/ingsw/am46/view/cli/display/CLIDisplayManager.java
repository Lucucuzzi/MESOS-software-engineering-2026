package it.polimi.ingsw.am46.view.cli.display;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.LocalModel;
import it.polimi.ingsw.am46.view.cli.utils.CardPrinter;
import it.polimi.ingsw.am46.view.cli.utils.TilePrinter;
import it.polimi.ingsw.am46.view.utils.BoardDictionary;
import it.polimi.ingsw.am46.view.utils.CardDictionary;
import it.polimi.ingsw.am46.view.cli.utils.ColorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CLIDisplayManager — Manages ONLY display and formatting.
 *
 * RESPONSIBILITIES:
 * - Print welcome screen
 * - Print game board
 * - Print menu
 * - Print status and help messages
 * - Synchronize stdout access with screenLock
 *
 * DOES NOT:
 * - Read input
 * - Manage threading
 * - Parse commands
 *
 * THREAD-SAFETY:
 * Every public method acquires screenLock.writeLock() before printing.
 * This ensures network thread and input thread don't write to screen simultaneously.
 */
public class CLIDisplayManager {

    private final ReentrantReadWriteLock screenLock;
    private final LocalModel localModel;

    public CLIDisplayManager(ReentrantReadWriteLock screenLock, LocalModel localModel) {
        this.screenLock = screenLock;
        this.localModel = localModel;
    }

    /**
     * Prints the welcome screen.
     */
    public void displayWelcome() {
        screenLock.writeLock().lock();
        try {
            System.out.println(ColorCode.BRIGHT_CYAN +
                    "======================\n" +
                    "       MESOS \n" +
                    "======================" + ColorCode.RESET);
            System.out.println();
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints the commands menu.
     */
    public void displayHelp() {
        screenLock.writeLock().lock();
        try {
            System.out.println(ColorCode.BOLD + "Available commands:" + ColorCode.RESET);
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "move <tileId>" + ColorCode.RESET +
                    " — Move the totem");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "add <cardId>" + ColorCode.RESET +
                    " — Add a card");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "skip" + ColorCode.RESET +
                    " — Skip your extra turn");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "board" + ColorCode.RESET +
                    " — Show the board");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "status" + ColorCode.RESET +
                    " — Show the status");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "help" + ColorCode.RESET +
                    " — Show this menu");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "infocard <cardId>" + ColorCode.RESET +
                    " — Show a description of the card");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "playerstats" + ColorCode.RESET +
                    " — Show the situation of each player");
            System.out.println(" " + ColorCode.BRIGHT_CYAN + "quit" + ColorCode.RESET +
                    " — Quit the game");
            System.out.println();
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints the game board.
     *
     */
    public void displayBoard(GameState state) {
        if (state == null) return;
        screenLock.writeLock().lock();
        try {
            System.out.println("\n" + ColorCode.BRIGHT_CYAN + "==================== BOARD ====================" + ColorCode.RESET);

            printTurnOrder(state);
            printCardRow("TOP ROW", state.getTopRowCardIds());
            TilePrinter.printTrack(state);
            printCardRow("BOTTOM ROW", state.getBottomRowCardIds());

            System.out.println(ColorCode.BRIGHT_CYAN + "===============================================" + ColorCode.RESET + "\n");
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    private void printTurnOrder(GameState state) {
        System.out.println(ColorCode.BOLD + "TURN ORDER (Position Effects):" + ColorCode.RESET);

        int numPlayers = state.getPlayerStates().size();
        int pos = 1;

        for (String nick : state.getTurnOrder()) {
            String effect = BoardDictionary.getTurnTileEffect(pos, numPlayers);
            System.out.printf("  [%d] %-15s | %s\n", pos, ColorCode.playerName(state.getPlayerStateByNickname(nick)), ColorCode.info(effect));
            pos++;
        }
        System.out.println();
    }

    /**
     * Prints quick game status.
     */
    public void displayStatus(GameState state) {
        if (state == null) {
            displayMessage(ColorCode.warning("⚠️  Status not available"));
            return;
        }

        screenLock.writeLock().lock();
        try {
            System.out.println();
            System.out.println(ColorCode.colorizeBold("📊 Quick status:", ColorCode.BRIGHT_CYAN));
            System.out.println(ColorCode.info("Round: " + state.getRound()));
            System.out.println(ColorCode.info("Turn: ") + ColorCode.playerName(state.getPlayerStateByNickname(state.getActivePlayerNickname())));
            System.out.println(ColorCode.info("Phase: " + state.getCurrentPhaseName()));
            System.out.println(ColorCode.info("Era:   ") + state.getCurrentEra());
            System.out.println();
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    public void displayPlayerStats(GameState state) {
        if (state == null) return;
        screenLock.writeLock().lock();
        try {
            System.out.println("\n" + ColorCode.BRIGHT_CYAN + "👥 PLAYER STATS" + ColorCode.RESET);
            System.out.println("--------------------------------------------------");
            for (var player : state.getPlayerStates()) {
                System.out.printf("👤 %-12s | 🥩 Food: %-2d | 🏆 PP: %-2d\n",
                        ColorCode.playerName(player), player.getFood(), player.getPP());

                // Stampa un riassunto delle carte
                printCardRow("Cards", player.getCardIds());
                System.out.println("\n--------------------------------------------------");
            }
            System.out.println();
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints a generic message.
     * Called by CLIInputHandler to send feedback.
     */
    public void displayMessage(String message) {
        screenLock.writeLock().lock();
        try {
            System.out.println(message);
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    /**
     * Prints a list of state updates dynamically.
     * Restores the input prompt if the user was typing.
     */
    public void displayUpdates(List<String> updates, boolean isWaitingForInput) {
        if (updates == null || updates.isEmpty()) return;

        screenLock.writeLock().lock();
        try {
            System.out.println(); // Spacing for readability

            for (String update : updates) {
                System.out.println(update);
            }

            // Restore the prompt symbol if the user is expected to type
            if (isWaitingForInput) {
                System.out.print(ColorCode.BRIGHT_YELLOW + ">" + ColorCode.RESET);
                System.out.flush();
            }
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    public void displayCardInfo(String idString) {
        screenLock.writeLock().lock();
        try {
            int id = Integer.parseInt(idString);
            System.out.println();
            CardPrinter.printCard(id); // CAMBIATO: era printCard(id, name, type, cost, info)
            System.out.println();
        } catch (NumberFormatException e) {
            System.out.println(ColorCode.error("Invalid ID format."));
        } finally {
            screenLock.writeLock().unlock();
        }
    }

    private void printCardRow(String rowName, List<Integer> cardIds) {
        System.out.println(ColorCode.BOLD + "--- " + rowName + " ---" + ColorCode.RESET);

        int cardsPerRow = 5;
        for (int i = 0; i < cardIds.size(); i += cardsPerRow) {
            List<Integer> chunk = cardIds.subList(i, Math.min(i + cardsPerRow, cardIds.size()));

            List<String[]> allCardLines = new ArrayList<>();
            for (Integer id : chunk) {
                allCardLines.add(CardPrinter.getCardLines(id)); // array of array
            }

            for (int lineIdx = 0; lineIdx < CardPrinter.CARD_HEIGHT; lineIdx++) {
                StringBuilder row = new StringBuilder();
                for (String[] cardLines : allCardLines) {
                    row.append(cardLines[lineIdx]).append("  ");
                }
                System.out.println(row); // BUGFIX: mancava nell'originale
            }
            System.out.println();
        }
    }
}