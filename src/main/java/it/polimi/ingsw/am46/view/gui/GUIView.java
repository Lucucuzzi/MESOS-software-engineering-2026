package it.polimi.ingsw.am46.view.gui;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.GameView;
import it.polimi.ingsw.am46.view.LocalModel;
import it.polimi.ingsw.am46.view.gui.scenes.EndGamePane;
import it.polimi.ingsw.am46.view.gui.scenes.GamePane;
import it.polimi.ingsw.am46.view.gui.scenes.LobbyPane;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import it.polimi.ingsw.am46.view.gui.utils.SceneManager;
import it.polimi.ingsw.am46.view.gui.utils.UIUpdater;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.CountDownLatch;

/**
 * The type Gui view.
 */
public class GUIView extends Application implements GameView {

    private boolean gameStarted = false;

    // --- Campi statici: impostati prima di launch() ---
    private static LocalModel staticModel;
    private static ClientController staticController;
    private static CountDownLatch staticLatch;

    /**
     * Sets model.
     *
     * @param m the m
     */
    public static void setModel(LocalModel m) { staticModel = m; }

    /**
     * Sets controller.
     *
     * @param c the c
     */
    public static void setController(ClientController c) { staticController = c; }

    /**
     * Sets latch.
     *
     * @param l the l
     */
    public static void setLatch(CountDownLatch l)  { staticLatch = l; }

    // --- Campi istanza ---
    private LocalModel localModel;
    private ClientController controller;
    private CountDownLatch latch;
    private static SceneManager sceneManager;
    private static UIUpdater uiUpdater;

    // Riferimenti diretti ai controller delle scene
    // per chiamare update() senza passare per sceneManager
    private static LobbyPane lobbyPane;
    private static GamePane gamePane;
    private static EndGamePane endGamePane;
    private static GameState pendingInitialState = null;

    @Override
    public void start(Stage stage) {
        this.localModel = staticModel;
        this.controller = staticController;
        this.latch = staticLatch;

        sceneManager = new SceneManager(stage);
        lobbyPane = new LobbyPane(sceneManager);
        javafx.geometry.Rectangle2D visualBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        gamePane = new GamePane(controller, localModel, controller.getMyNickname(), visualBounds.getHeight());
        endGamePane = new EndGamePane(sceneManager);

        sceneManager.register(SceneManager.SceneName.LOBBY, new Scene(lobbyPane));
        sceneManager.register(SceneManager.SceneName.GAME, new Scene(gamePane));
        sceneManager.register(SceneManager.SceneName.ENDGAME, new Scene(endGamePane));

        uiUpdater = new UIUpdater(this::applyGameState);
        if (pendingInitialState != null) {
            System.out.println("[GUI] Recupero lo stato salvato in precedenza!");
            uiUpdater.submit(pendingInitialState);
            pendingInitialState = null; // Svuota la memoria
        }

        stage.setTitle("MESOS - GUI");
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // --- INIZIO BLOCCO PRELOAD ---
        java.util.List<String> allPaths = new java.util.ArrayList<>();

        // Carte (1-122)
        for (int i = 1; i <= 122; i++) {
            allPaths.add("/images/cards/card_" + i + ".png");
        }

        // Sfondi
        allPaths.add("/images/backgrounds/game_bg.png");
        allPaths.add("/images/backgrounds/lobby_bg.jpg");
        allPaths.add("/images/backgrounds/victory_bg.png");

        // Tessere offerta
        for (char c = 'a'; c <= 'g'; c++) {
            allPaths.add("/images/offerTile/tile_" + c + ".jpg");
        }

        // Totem
        allPaths.add("/images/totem/totem_blue.png");
        allPaths.add("/images/totem/totem_red.png");
        allPaths.add("/images/totem/totem_yellow.png");
        allPaths.add("/images/totem/totem_purple.png");
        allPaths.add("/images/totem/totem_white.png");

        // Order tile
        for (int i = 2; i <= 5; i++) {
            allPaths.add("/images/orderTile/OrdineTurno" + i + "players.jpg");
        }

        ImageCache.preloadAll(allPaths);
        // --- FINE BLOCCO PRELOAD ---

        // Chiusura pulita dell'app
        stage.setOnCloseRequest(e -> {
            uiUpdater.shutdown();
            if (latch != null) latch.countDown();
            Platform.exit();
        });

        // Costruzione delle scene (il resto del codice rimane invariato)
        // La GUI parte direttamente con la lobby o il caricamento dello stato
        sceneManager.switchTo(SceneManager.SceneName.LOBBY);

        // ... registrazione altre scene ...

        stage.show();
    }

    /**
     * Chiamato da UIUpdater sul JavaFX Application Thread.
     * Decide quale scena mostrare e aggiorna il controller corretto.
     */
    private void applyGameState(GameState state) {
        if (state == null) return;

        // Fondamentale: Tutto ciò che tocca la grafica deve andare qui dentro
        Platform.runLater(() -> {
            try {
                // Debug: stampiamo cosa vede la GUI per capire perché non switcha
                System.out.println("[DEBUG GUI] Fase attuale: " + state.getCurrentPhaseName());

                // 1. PRIORITÀ MASSIMA: Fine Partita
                if (state.isFinalPointsCounted()) {
                    endGamePane.setMyNickname(localModel.getMyNickname());
                    endGamePane.update(state);
                    sceneManager.switchTo(SceneManager.SceneName.ENDGAME);
                    return;
                }

                // 2. LOGICA DI SWITCH TRA LOBBY E GIOCO
                String phase = state.getCurrentPhaseName();

                if (phase == null || "LOBBY".equalsIgnoreCase(phase)) {
                    lobbyPane.update(state);
                    sceneManager.switchTo(SceneManager.SceneName.LOBBY);
                } else {
                if (!gameStarted) {
                    // Prima volta che usciamo dalla lobby: mostra il messaggio per 2s
                    gameStarted = true;
                    lobbyPane.update(state);
                    lobbyPane.showStarting();
                    sceneManager.switchTo(SceneManager.SceneName.LOBBY);
                    PauseTransition delay = new PauseTransition(Duration.seconds(3));
                    delay.setOnFinished(e -> {
                        gamePane.update(state);
                        sceneManager.switchTo(SceneManager.SceneName.GAME);
                    });
                    delay.play();
                } else {
                    gamePane.update(state);
                    sceneManager.switchTo(SceneManager.SceneName.GAME);
                }
            }
            } catch (Exception e) {
                System.err.println("[ERRORE GUI] Errore durante applyGameState: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    // --- Implementazione GameView ---

    @Override
    public void start() {
        Thread jfxThread = new Thread(
                () -> Application.launch(GUIView.class),
                "javafx-launch-thread"
        );
        jfxThread.setDaemon(false);
        jfxThread.start();
    }


    @Override
    public void stop() {
        if (uiUpdater != null) uiUpdater.shutdown();
        if (latch != null) latch.countDown();
        Platform.exit();
    }


    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> {
            if (gamePane != null) gamePane.showNotification(message);
        });
    }

    @Override
    public void drawBoard(GameState state) {
        handleStateUpdate(state);
    }

    @Override
    public void onStateUpdate(GameState state) {
        handleStateUpdate(state);
    }

    private void handleStateUpdate(GameState state) {
        if (uiUpdater != null) {
            uiUpdater.submit(state);
        } else {
            System.out.println("[GUI] Attenzione: UIUpdater non ancora pronto, salvo lo stato in attesa...");
            pendingInitialState = state;
        }
    }

    @Override
    public void onError(String errorMessage) {
        Platform.runLater(() -> {
            if (gamePane != null) gamePane.showError(errorMessage);
        });
    }

    @Override
    public void onAbort(String reason) {
        Platform.runLater(() -> {
            uiUpdater.shutdown();
            if (latch != null) latch.countDown();
            // Mostra un dialog di errore prima di chiudere
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Partita interrotta");
            alert.setHeaderText("La partita è stata interrotta");
            alert.setContentText(reason);
            alert.showAndWait();
            Platform.exit();
        });
    }

}
