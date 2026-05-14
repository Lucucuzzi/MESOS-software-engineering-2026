package it.polimi.ingsw.am46.controller;

import it.polimi.ingsw.am46.exception.GameAlreadyStartedException;
import it.polimi.ingsw.am46.exception.InvalidConnectionException;
import it.polimi.ingsw.am46.model.Color;
import it.polimi.ingsw.am46.model.Game;
import it.polimi.ingsw.am46.network.NetworkMode;
import it.polimi.ingsw.am46.network.VirtualView;
import it.polimi.ingsw.am46.network.dto.GameState;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for the {ServerController} class.
 *
 * Purpose:
 * This class verifies the core network-to-model integration logic handled by the ServerController.
 * It primarily focuses on the game lobby phase, client connection/disconnection management,
 * input validation, and error broadcasting.
 *
 * Key areas tested:
 * - Happy Path: Successful connection of multiple clients and the triggering of the game start.
 * - Edge Cases & Validation: Handling invalid nicknames, duplicate or invalid colors, and attempts
 * to join a game already in progress.
 * - Resilience & Error Handling: Ensuring appropriate error messages are routed to the VirtualView,
 * and verifying that network communication failures (e.g., failing to send an error) trigger a safe game abort.
 * - Disconnection & Host Migration: Validating that disconnected players are handled safely and,
 * if the lobby host disconnects, host privileges are correctly migrated to another player.
 *
 * Implementation Details:
 * - Employs custom test doubles (Fakes: {FakeVirtualView}, {FakeNetworkMode}) to completely
 * isolate the controller from the actual RMI/Socket network layers.
 * - Utilizes Java Reflection to inspect the internal {Game} state and to specifically test
 * private disconnection handlers.
 */

public class ServerControllerTest {

    @Test
    void connectHappyPathStartsGame() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());
        controller.connect("Bob", Color.values()[1].name(), new FakeNetworkMode());
        controller.setExpectedPlayers("Alice", 2);

        Game game = getGame(controller);
        assertTrue(game.isGameStarted());
        assertTrue(view.registered.contains("Alice"));
        assertTrue(view.registered.contains("Bob"));
        assertTrue(view.updates > 0);
    }

    @Test
    void connectInvalidInputs() {
        ServerController controller = new ServerController();

        assertThrows(InvalidConnectionException.class,
                () -> controller.connect(null, "RED", new FakeNetworkMode()));

        assertThrows(InvalidConnectionException.class,
                () -> controller.connect("Alice", "RED", null));

        assertThrows(IllegalStateException.class,
                () -> controller.connect("Alice", "RED", new FakeNetworkMode()));
    }

    @Test
    void connectColorErrorsAndGameAlreadyStarted() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());

        assertThrows(InvalidConnectionException.class,
                () -> controller.connect("Bob", "NOT_A_COLOR", new FakeNetworkMode()));

        assertThrows(InvalidConnectionException.class,
                () -> controller.connect("Bob", Color.values()[0].name(), new FakeNetworkMode()));

        Game game = getGame(controller);
        game.setGameStarted(true);

        assertThrows(GameAlreadyStartedException.class,
                () -> controller.connect("Bob", Color.values()[1].name(), new FakeNetworkMode()));
    }

    @Test
    void setExpectedPlayersErrorsAreReported() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());

        controller.setExpectedPlayers(" ", 2);
        controller.setExpectedPlayers("Bob", 2);
        controller.setExpectedPlayers("Alice", 1);

        assertTrue(view.errors >= 3);
    }

    @Test
    void moveTotemAndAddCardErrorPaths() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.moveTotem("Alice", "A");
        controller.addCard("Alice", "1");
        controller.addExtraCard("Alice", "1");

        assertTrue(view.errors >= 3);
    }

    @Test
    void sendErrorFailureTriggersAbort() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        view.throwOnSendError = true;
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());
        controller.moveTotem("Alice", "Z");

        assertTrue(view.abortMessages > 0);
        assertTrue(view.clears > 0);
    }

    @Test
    void handleDisconnectionIgnoresUnknownPlayer() {
        ServerController controller = new ServerController();
        controller.handleDisconnection("Ghost");
    }

    @Test
    void suspendPlayerAndHostDisconnectionViaReflection() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());
        controller.connect("Bob", Color.values()[1].name(), new FakeNetworkMode());

        Game game = getGame(controller);
        game.setGameStarted(false);
        game.setHostNickname("Alice");

        Method suspend = ServerController.class.getDeclaredMethod("suspendPlayer", String.class);
        suspend.setAccessible(true);
        suspend.invoke(controller, "Alice");

        assertTrue(view.unregistered.contains("Alice"));
        assertTrue(view.updates > 0);
        assertNull(game.getExpectedPlayers());
        assertEquals("Bob", game.getHostNickname());
    }

    @Test
    void getAvailableColorsReturnsList() throws Exception {
        ServerController controller = new ServerController();
        List<Color> colors = controller.getAvailableColors();
        assertFalse(colors.isEmpty());
    }

    @Test
    void connectFailsDuringNetworkRegistrationTriggersRollback() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView crashView = new FakeVirtualView() {
            @Override
            public void registerClient(String nickname, NetworkMode cur) {
                throw new RuntimeException("Simulated network crash");
            }
        };
        controller.setVirtualView(crashView);

        assertThrows(InvalidConnectionException.class, () ->
                controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode())
        );

        Game game = getGame(controller);
        assertTrue(game.getPlayers().isEmpty());
    }

    @Test
    void setExpectedPlayersWithMorePlayersAlreadyConnected() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());
        controller.connect("Bob", Color.values()[1].name(), new FakeNetworkMode());
        controller.connect("Charlie", Color.values()[2].name(), new FakeNetworkMode());

        controller.setExpectedPlayers("Alice", 2);

        assertTrue(view.errors > 0);
    }

    @Test
    void actionMethodsHandleBadFormatsAndNulls() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());
        controller.connect("Bob", Color.values()[1].name(), new FakeNetworkMode());
        controller.setExpectedPlayers("Alice", 2);

        controller.moveTotem("Alice", " ");
        controller.addCard("Alice", null);
        controller.addCard("Alice", "NOT_A_NUMBER");
        controller.addCard("Alice", "9999");

        assertEquals(4, view.errors);
    }



    @Test
    void skipExtraDrawAndNullDisconnections() throws Exception {
        ServerController controller = new ServerController();
        FakeVirtualView view = new FakeVirtualView();
        controller.setVirtualView(view);

        controller.connect("Alice", Color.values()[0].name(), new FakeNetworkMode());
        controller.connect("Bob", Color.values()[1].name(), new FakeNetworkMode());
        controller.setExpectedPlayers("Alice", 2);

        controller.skipExtraDraw("Alice");

        controller.handleDisconnection(null);
        controller.handleDisconnection("   ");

        Game game = getGame(controller);
        assertEquals(2, game.getPlayers().size());
    }

    private Game getGame(ServerController controller) throws Exception {
        Field f = ServerController.class.getDeclaredField("game");
        f.setAccessible(true);
        return (Game) f.get(controller);
    }

    private static class FakeNetworkMode implements NetworkMode {
        @Override
        public boolean isSocket() throws RemoteException {
            return false;
        }
    }

    private static class FakeVirtualView implements VirtualView {
        List<String> registered = new ArrayList<>();
        List<String> unregistered = new ArrayList<>();
        int updates = 0;
        int errors = 0;
        int abortMessages = 0;
        int clears = 0;
        boolean throwOnSendError = false;

        @Override
        public void registerClient(String nickname, NetworkMode cur) {
            registered.add(nickname);
        }

        @Override
        public void unregisterClient(String nickname) {
            unregistered.add(nickname);
        }

        @Override
        public void broadcastUpdate(GameState gameState) {
            updates++;
        }

        @Override
        public void sendError(String nickname, String errorMessage) throws Exception {
            errors++;
            if (throwOnSendError) {
                throw new RuntimeException("Forced error");
            }
        }

        @Override
        public void broadcastError(String errorMessage) {
            errors++;
        }

        @Override
        public void broadcastAbort(String message) {
            abortMessages++;
        }

        @Override
        public void broadcastWinner(GameState finalState) {
            // no-op
        }

        @Override
        public void clearClients() {
            clears++;

        }
    }
}