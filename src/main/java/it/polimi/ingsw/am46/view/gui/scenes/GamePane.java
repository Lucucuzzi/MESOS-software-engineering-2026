package it.polimi.ingsw.am46.view.gui.scenes;
import java.util.List;
import java.util.LinkedHashMap;
import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.OfferTileState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.LocalModel;
import it.polimi.ingsw.am46.view.gui.components.*;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public class GamePane extends StackPane {
    private final ClientController controller;
    private final LocalModel localModel;
    private final BorderPane root;
    private final Label roundLabel, phaseLabel, activePlayerLabel;
    private final GridPane boardGrid;
    private final HBox characterBox = new HBox(10);
    private final HBox buildingBox = new HBox(10);
    private final VBox playersBox;
    private final StackPane notificationOverlay;
    private final Label notificationText;
    private final String myNickname;
    private final Map<Integer, CardView> topRowViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> bottomRowViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> characterViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> buildingViews = new LinkedHashMap<>();
    private final Map<Character, OfferTileView> offerTileViews = new LinkedHashMap<>();
    private final VBox turnOrderBox = new VBox(4);
    private final Map<String, TurnTileView> turnTileViews = new LinkedHashMap<>();


    private final Map<String, PlayerBoardView> playerBoards = new HashMap<>();

    public GamePane(ClientController controller, LocalModel localModel, String myNickname) {
        this.myNickname = myNickname;
        this.controller = controller;
        this.localModel = localModel;
        this.root = new BorderPane();

        // Setup Header
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2c3e50;");
        roundLabel = new Label("Round - Era -");
        roundLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        phaseLabel = new Label("Fase: -");
        phaseLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13;");
        activePlayerLabel = new Label("Turno di: -");
        activePlayerLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13; -fx-font-weight: bold;");
        header.getChildren().addAll(roundLabel, phaseLabel, activePlayerLabel);
        root.setTop(header);

        // Tabellone Centrale
        boardGrid = new GridPane();
        boardGrid.setHgap(10);
        boardGrid.setVgap(10);
        root.setCenter(boardGrid);

        // Mano (Bottom)
        Label charLabel = new Label("Personaggi");
        charLabel.setStyle("-fx-text-fill: white;");
        Label buildLabel = new Label("Edifici");
        buildLabel.setStyle("-fx-text-fill: white;");
        VBox bottomArea = new VBox(5, charLabel, characterBox, buildLabel, buildingBox);
        root.setBottom(bottomArea);

        // Player Boards (Right)
        playersBox = new VBox(10);
        root.setRight(playersBox);
        playersBox.getChildren().add(turnOrderBox);

        // Overlay Notifiche
        notificationOverlay = new StackPane();
        notificationOverlay.setVisible(false);
        notificationText = new Label();
        notificationOverlay.getChildren().add(notificationText);

        this.getChildren().addAll(root, notificationOverlay);
    }

    public void update(GameState state) {
        updateHeader(state);
        updateBoard(state);
        updateHand(state);
        updatePlayers(state);
        updateTurnOrder(state);
    }


    private void updateHeader(GameState state) {
        roundLabel.setText("Round " + state.getRound() + " — Era " + state.getCurrentEra());
        phaseLabel.setText("Fase: " + state.getCurrentPhaseName());
        activePlayerLabel.setText("Turno di: " + state.getActivePlayerNickname());
    }


    private void updateBoard(GameState state) {
        diffCardRow(state.getTopRowCardIds(), topRowViews, boardGrid, 0);
        diffCardRow(state.getBottomRowCardIds(), bottomRowViews, boardGrid, 1);
        updateOfferTiles(state.getOfferTileStates(), boardGrid);
    }


    private void diffCardRow(List<Integer> newIds, Map<Integer, CardView> currentViews,
                             GridPane grid, int row) {
        List<Integer> toRemove = currentViews.keySet().stream()
                .filter(id -> !newIds.contains(id))
                .toList();
        for (Integer id : toRemove) {
            grid.getChildren().remove(currentViews.get(id));
            currentViews.remove(id);
        }

        for (int col = 0; col < newIds.size(); col++) {
            int id = newIds.get(col);
            if (!currentViews.containsKey(id)) {
                CardView cv = new CardView(id);
                cv.setOnMouseClicked(e -> controller.onAddCard(String.valueOf(id)));  // ← qui
                currentViews.put(id, cv);
                grid.getChildren().add(cv);
            }
            GridPane.setConstraints(currentViews.get(id), col, row);
        }
    }

    private void updateTurnOrder(GameState state) {
        List<PlayerState> players = state.getPlayerStates();
        for (int i = 0; i < players.size(); i++) {
            String nick = players.get(i).getNickname();
            if (!turnTileViews.containsKey(nick)) {
                TurnTileView ttv = new TurnTileView(i + 1, nick);
                turnTileViews.put(nick, ttv);
                turnOrderBox.getChildren().add(ttv);
            } else {
                turnTileViews.get(nick).update(i + 1, nick);
            }
            // Aggiorna sempre lo stile — sia per nuovi che per esistenti
            TurnTileView ttv = turnTileViews.get(nick);
            if (nick.equals(state.getActivePlayerNickname())) {
                ttv.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #f39c12; -fx-border-width: 2;");
            } else {
                ttv.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #7f8c8d;");
            }
        }
    }


    private void updateOfferTiles(List<OfferTileState> newStates, GridPane grid) {
        int col = 0;
        for (OfferTileState ts : newStates) {
            char letter = ts.getLetter();
            if (!offerTileViews.containsKey(letter)) {
                OfferTileView view = new OfferTileView(ts);
                view.setOnMouseClicked(e -> controller.onMoveTotem(String.valueOf(ts.getLetter())));
                offerTileViews.put(letter, view);
                grid.getChildren().add(view);
                GridPane.setConstraints(view, col, 2); // solo alla creazione
            } else {
                offerTileViews.get(letter).update(ts);
            }
            col++;
        }
    }


    private void updateHand(GameState state) {
        PlayerState me = state.getPlayerStates().stream()
                .filter(p -> p.getNickname().equals(myNickname))
                .findFirst()
                .orElse(null);
        if (me == null) return;

        diffCardList(me.getCharacterCardIds(), characterViews, characterBox);
        diffCardList(me.getBuildingCardIds(), buildingViews, buildingBox);
    }


    private void diffCardList(List<Integer> newIds, Map<Integer, CardView> currentViews, HBox box) {
        List<Integer> toRemove = currentViews.keySet().stream()
                .filter(id -> !newIds.contains(id))
                .toList();
        for (Integer id : toRemove) {
            box.getChildren().remove(currentViews.get(id));
            currentViews.remove(id);
        }

        for (Integer id : newIds) {
            if (!currentViews.containsKey(id)) {
                CardView cv = new CardView(id);
                // nessun click handler — carte in sola lettura
                currentViews.put(id, cv);
                box.getChildren().add(cv);
            }
        }
    }


    private void updatePlayers(GameState state) {
        if (state.getPlayerStates() == null) return;
        for (PlayerState ps : state.getPlayerStates()) {
            if (!playerBoards.containsKey(ps.getNickname())) {
                PlayerBoardView view = new PlayerBoardView(ps);
                playersBox.getChildren().add(view);
                playerBoards.put(ps.getNickname(), view);
            } else {
                playerBoards.get(ps.getNickname()).update(ps);
            }
        }
    }

    public void showNotification(String message) {
        notificationText.setText(message);
        notificationText.setStyle(
                "-fx-background-color: rgba(0,0,0,0.75);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 6;"
        );
        notificationOverlay.setVisible(true);
        notificationOverlay.setOpacity(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notificationOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(ev -> {
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(Duration.seconds(2.5));
            pause.setOnFinished(e -> {
                FadeTransition fadeOut =
                        new FadeTransition(Duration.millis(400), notificationOverlay);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(end -> notificationOverlay.setVisible(false));
                fadeOut.play();
            });
            pause.play();
        });
        fadeIn.play();
    }

    public void showError(String error) {
        notificationText.setStyle(
                "-fx-background-color: rgba(192,57,43,0.85);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 6;"
        );
        showNotification("ERRORE: " + error);
    }
}