package it.polimi.ingsw.am46.view.utils;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.cli.utils.color.ColorCode;
import it.polimi.ingsw.am46.view.cli.utils.printer.LeaderboardPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for computing the differences between two GameStates.
 * * This class implements the "Raw Data Observation" pattern to strictly enforce the MVC architecture.
 * It compares the previous state with the current state and generates a chronological list of
 * human-readable update messages (e.g., resource changes, phase transitions, turn notifications)
 * without ever inferring the underlying game logic or business rules.
 * * Key Features:
 * - Dynamic Ordering: Adjusts the output order of events based on the context (e.g., showing phase
 * transitions before stat changes during automatic event resolutions).
 * - Anti-Spam: Suppresses redundant turn notifications during automatic server-side phases.
 * - Decoupling: Keeps the View completely isolated from the Model's mechanics, focusing solely on presentation.
 */
public class StateDiffCalculator {


    /**
     * Compute diff list.
     *
     * @param oldState the old state
     * @param newState the new state
     * @return the list
     */
    public static List<String> computeDiff(GameState oldState, GameState newState) {
        return computeDiff(oldState, newState, null);
    }

    /**
     * Compute diff list.
     *
     * @param oldState   the old state
     * @param newState   the new state
     * @param myNickname the my nickname
     * @return the list
     */
    public static List<String> computeDiff(GameState oldState, GameState newState, String myNickname) {
        List<String> updates = new ArrayList<>();

        if (oldState == null) {
            updates.add(getInitialConnectionMessage(newState));
            return updates;
        }

        if (newState.isGameStarted()) {
            checkGameStart(oldState, newState, updates);
            assembleGameUpdates(oldState, newState, updates);
        }

        checkGameOver(oldState, newState, updates, myNickname);

        return updates;
    }

    private static void assembleGameUpdates(GameState oldState, GameState newState, List<String> updates) {
        List<String> phaseUpdates = new ArrayList<>();
        checkPhaseAndRound(oldState, newState, phaseUpdates);

        List<String> eventUpdates = new ArrayList<>();
        checkResolvedEvents(oldState, newState, eventUpdates);

        List<String> resourceUpdates = new ArrayList<>();
        checkPlayerResources(oldState, newState, resourceUpdates);

        List<String> cardUpdates = new ArrayList<>();
        checkPlayerCards(oldState, newState, cardUpdates);

        mergeUpdatesDynamically(oldState, newState, phaseUpdates, eventUpdates, resourceUpdates, cardUpdates, updates);
        checkActivePlayer(oldState, newState, updates);
    }

    private static void mergeUpdatesDynamically(GameState oldState, GameState newState,
                                                List<String> phaseUpdates,
                                                List<String> eventUpdates,
                                                List<String> resourceUpdates,
                                                List<String> cardUpdates,
                                                List<String> finalUpdates) {

        finalUpdates.addAll(eventUpdates);
        finalUpdates.addAll(cardUpdates);
        finalUpdates.addAll(resourceUpdates);
        finalUpdates.addAll(phaseUpdates);
    }

    private static String getInitialConnectionMessage(GameState newState) {
        if (newState.isGameStarted()) {
            return ColorCode.success("Connection established. Game is already in progress.");
        }
        return ColorCode.success("Connection established. Welcome to the Lobby.");
    }

    private static void checkGameStart(GameState oldState, GameState newState, List<String> updates) {
        if (!oldState.isGameStarted() && newState.isGameStarted()) {
            updates.add(ColorCode.success("The game has started."));
        }
    }

    private static void checkPhaseAndRound(GameState oldState, GameState newState, List<String> updates) {
        if (!oldState.isGameStarted() && newState.isGameStarted()) {
            updates.add("Starting Phase: " + ColorCode.BRIGHT_YELLOW + newState.getCurrentPhaseName() + ColorCode.RESET);
            return;
        }

        if (newState.getRound() > oldState.getRound()) {
            updates.add( "Round " + ColorCode.BRIGHT_YELLOW +oldState.getRound()+ ColorCode.RESET+ " ended.");
            updates.add("Round " + ColorCode.info(""+newState.getRound())+ " started." + ColorCode.RESET);
        }

        if (newState.getCurrentEra() > oldState.getCurrentEra()) {
            updates.add( "Era " + ColorCode.BRIGHT_YELLOW + oldState.getCurrentEra() + " ended." + ColorCode.RESET);
            updates.add("Era " + ColorCode.info(""+newState.getCurrentEra()) + " started." + ColorCode.RESET);
        }

        if (oldState.isGameStarted() && !oldState.getCurrentPhaseName().equals(newState.getCurrentPhaseName())) {
            String newPhase = newState.getCurrentPhaseName();
            updates.add("Phase changed to: " + ColorCode.BRIGHT_YELLOW + newPhase + ColorCode.RESET);

            if ("ResolveEventState".equals(newPhase)) {
                updates.add(ColorCode.warning("Resolving events..."));
            }
        }
    }

    private static void checkPlayerResources(GameState oldState, GameState newState, List<String> updates) {
        for (PlayerState newP : newState.getPlayerStates()) {
            PlayerState oldP = oldState.getPlayerStates().stream()
                    .filter(p -> p.getNickname().equals(newP.getNickname()))
                    .findFirst().orElse(null);

            if (oldP == null) continue;

            String color = ColorCode.playerName(newState.getPlayerStateByNickname(newP.getNickname()));

            addResourceDiff(color, "food", oldP.getFood(), newP.getFood(), updates);
            addResourceDiff(color, "PP", oldP.getPP(), newP.getPP(), updates);
        }
    }

    private static void checkPlayerCards(GameState oldState, GameState newState, List<String> updates) {
        for (PlayerState newP : newState.getPlayerStates()) {
            PlayerState oldP = oldState.getPlayerStates().stream()
                    .filter(p -> p.getNickname().equals(newP.getNickname())).findFirst().orElse(null);

            if (oldP == null) continue;

            newP.getCardIds().stream().filter(id -> !oldP.getCardIds().contains(id)).forEach(id -> {
                String source = oldState.getTopRowCardIds().contains(id) ? "TOP ROW" :
                        oldState.getBottomRowCardIds().contains(id) ? "BOTTOM ROW" : "the deck";
                updates.add(ColorCode.playerName(newP)+ ColorCode.RESET + " obtained '"
                        + ColorCode.info(""+ CardDictionary.getCardName(id)) + ColorCode.RESET +  "' from " + source + ".");
            });
        }
    }

    private static void addResourceDiff(String player, String resource, int oldVal, int newVal, List<String> updates) {
        if (newVal > oldVal) {
            updates.add(player + ColorCode.RESET + " gained " + ColorCode.success(""+(newVal - oldVal))  + " " + resource + ".");
        } else if (newVal < oldVal) {
            updates.add(player + ColorCode.RESET + " lost " + ColorCode.warning (""+(oldVal - newVal)) + " " + resource + ".");
        }
    }


    private static void checkActivePlayer(GameState oldState, GameState newState, List<String> updates) {
        String oldActive = oldState.getActivePlayerNickname();
        String newActive = newState.getActivePlayerNickname();
        String oldPhase = oldState.getCurrentPhaseName();
        String newPhase = newState.getCurrentPhaseName();

        if ("ResolveEventState".equals(newPhase) || "EndRoundState".equals(newPhase)) {
            return;
        }

        boolean playerChanged = newActive != null && !newActive.equals(oldActive);
        boolean phaseChangedForSamePlayer = newActive != null && newActive.equals(oldActive) &&
                oldPhase != null && !oldPhase.equals(newPhase);

        if (playerChanged || phaseChangedForSamePlayer) {
            updates.add("It's " +ColorCode.playerName(newState.getPlayerStateByNickname(newActive)) + ColorCode.RESET+ "'s turn." );
        }
    }

    private static void checkResolvedEvents(GameState oldState, GameState newState, List<String> updates) {
        List<Integer> oldResolved = (oldState != null && oldState.getRecentlyResolvedEvents() != null)
                ? oldState.getRecentlyResolvedEvents() : new ArrayList<>();
        List<Integer> newResolved = (newState.getRecentlyResolvedEvents() != null)
                ? newState.getRecentlyResolvedEvents() : new ArrayList<>();

        for (Integer id : newResolved) {
            if (!oldResolved.contains(id)) {
                updates.add(
                        "EVENT RESOLVED: " + ColorCode.BOLD + ColorCode.BRIGHT_CYAN + CardDictionary.getCardName(id) + ColorCode.RESET);
            }
        }
    }

    private static void checkGameOver(GameState oldState, GameState newState, List<String> updates, String myNickname) {
        boolean wasAlreadyFinished = oldState != null && oldState.isFinalPointsCounted();
        boolean isNowFinished = newState.isFinalPointsCounted();

        if (!wasAlreadyFinished && isNowFinished) {
            updates.add("\n" + ColorCode.BRIGHT_CYAN + "================================" + ColorCode.RESET);
            updates.add(ColorCode.BOLD + "           GAME OVER            " + ColorCode.RESET);
            updates.add(ColorCode.BRIGHT_CYAN + "================================" + ColorCode.RESET);

            List<String> winners = newState.getWinners();
            if (winners != null && !winners.isEmpty()) {
                updates.add(ColorCode.success("WINNERS: " + String.join(", ", winners)));
            }

            List<PlayerState> ranking = newState.getPlayerStates().stream()
                    .filter(ps -> !ps.isDisconnected())
                    .sorted((a, b) -> Integer.compare(b.getPP(), a.getPP()))
                    .toList();
            updates.add(ColorCode.info("Classifica finale:"));
            for (int i = 0; i < ranking.size(); i++) {
                PlayerState ps = ranking.get(i);
                updates.add("  " + (i + 1) + ". " + ps.getNickname() + " — " + ps.getPP() + " PP");
            }

            if (newState.getFinalLeaderboard() != null && !newState.getFinalLeaderboard().isEmpty()) {
                int myRank = -1;
                if (myNickname != null) {
                    List<LeaderboardEntry> globalBoard = newState.getFinalLeaderboard();
                    for (int i = 0; i < globalBoard.size(); i++) {
                        if (globalBoard.get(i).getNickname().equals(myNickname)) {
                            myRank = i + 1;
                            break;
                        }
                    }
                }
                List<String> globalRankLines = LeaderboardPrinter.getLeaderboardLines(
                        newState.getFinalLeaderboard(),
                        newState.getNumPlayersInGame(),
                        myRank
                );
                updates.addAll(globalRankLines);
            }
            updates.add(ColorCode.info("Type 'quit' to exit the game."));
        }
    }
}