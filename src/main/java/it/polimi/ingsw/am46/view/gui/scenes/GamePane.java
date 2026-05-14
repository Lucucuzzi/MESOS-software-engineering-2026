package it.polimi.ingsw.am46.view.gui.scenes;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.OfferTileState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.LocalModel;
import it.polimi.ingsw.am46.view.gui.components.*;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GamePane extends StackPane {

    private final ClientController controller;
    private final LocalModel localModel;
    private final String myNickname;

    // Header
    private final Label roundLabel;
    private final Label phaseLabel;
    private final Label activePlayerLabel;

    // Righe carte
    private final HBox topRowBox = new HBox(8);
    private final HBox bottomRowBox = new HBox(8);

    // Tracciato offerte
    private final HBox offerRowBox = new HBox(8);

    // Mano giocatore locale
    private final FlowPane characterBox = new FlowPane(6,6);
    private final FlowPane buildingBox = new FlowPane(6,6);
    private final Label myPpLabel = new Label();
    private final Label myFoodLabel = new Label();

    // Plance avversari
    private final VBox playersBox = new VBox(10);

    // Notification overlay
    private final StackPane notificationOverlay = new StackPane();
    private final Label notificationText = new Label();

    // Smart redraw maps
    private final Map<Integer, CardView> topRowViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> bottomRowViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> characterViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> buildingViews = new LinkedHashMap<>();
    private final Map<Character, OfferTileView> offerTileViews = new LinkedHashMap<>();
    private final Map<String, PlayerBoardView> playerBoards = new HashMap<>();

    // Order tile
    private final ImageView orderTileView = new ImageView();
    private int lastPlayerCount = -1;

    public GamePane(ClientController controller, LocalModel localModel, String myNickname) {
        this.controller = controller;
        this.localModel = localModel;
        this.myNickname = myNickname;

        // LAYER 1 — sfondo
        ImageView bgView = new ImageView();
        bgView.setImage(ImageCache.get("/images/backgrounds/game_bg.png"));
        bgView.setFitWidth(1280);
        bgView.setFitHeight(680);
        bgView.setPreserveRatio(false);
        getChildren().add(bgView);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(8));

        // ── HEADER ──
        HBox header = new HBox(24);
        header.setPadding(new Insets(8, 16, 8, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #3d1f0a;");

        roundLabel = new Label("Round - | Era -");
        roundLabel.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 14; -fx-font-weight: bold;");

        phaseLabel = new Label("Fase: -");
        phaseLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13;");

        activePlayerLabel = new Label("Turno di: -");
        activePlayerLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(roundLabel, phaseLabel, spacer, activePlayerLabel);
        root.setTop(header);

        // ── CENTER — righe carte + tracciato offerte ──
        VBox centerArea = new VBox(6);
        centerArea.setAlignment(Pos.CENTER);
        centerArea.setPadding(new Insets(10, 0, 10, 0));

        topRowBox.setAlignment(Pos.CENTER);
        topRowBox.setPadding(new Insets(4));

        offerRowBox.setAlignment(Pos.CENTER);
        offerRowBox.setPadding(new Insets(4));
        offerRowBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.25);" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6;"
        );

        orderTileView.setPreserveRatio(true);
        orderTileView.setFitHeight(150);
        offerRowBox.getChildren().add(orderTileView);

        bottomRowBox.setAlignment(Pos.CENTER);
        bottomRowBox.setPadding(new Insets(4));

        centerArea.getChildren().addAll(topRowBox, offerRowBox, bottomRowBox);
        root.setCenter(centerArea);

        // ── BOTTOM — mano del giocatore locale ──
        // Personaggi a sinistra (crescono verso destra), Edifici a destra (crescono verso sinistra)
        HBox handRow = new HBox();
        handRow.setPadding(new Insets(4));

        VBox charSection = new VBox(4);
        Label charTitle = new Label("Personaggi");
        charTitle.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12; -fx-font-weight: bold;");
        characterBox.setAlignment(Pos.TOP_LEFT);
        characterBox.setHgap(6);
        characterBox.setVgap(6);
        charSection.getChildren().addAll(charTitle, characterBox);
        HBox.setHgrow(charSection, Priority.ALWAYS);

        VBox buildSection = new VBox(4);
        buildSection.setAlignment(Pos.TOP_RIGHT);
        Label buildTitle = new Label("Edifici");
        buildTitle.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12; -fx-font-weight: bold;");
        buildingBox.setAlignment(Pos.TOP_RIGHT);
        buildingBox.setHgap(6);
        buildingBox.setVgap(6);
        buildSection.getChildren().addAll(buildTitle, buildingBox);

        handRow.getChildren().addAll(charSection, buildSection);

        VBox handArea = new VBox(4);
        handArea.setPadding(new Insets(8, 16, 8, 16));
        handArea.setStyle(
                "-fx-background-color: rgba(0,0,0,0.50);" +
                        "-fx-background-radius: 10 10 0 0;"
        );
        handArea.getChildren().add(handRow);

        // ── Stats giocatore locale ──
        HBox myPpBox = new HBox(5);
        myPpBox.setAlignment(Pos.CENTER_LEFT);
        ImageView myPpIcon = new ImageView(ImageCache.get("/images/token/1pp.png"));
        myPpIcon.setFitWidth(18); myPpIcon.setPreserveRatio(true);
        myPpLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 12; -fx-font-weight: bold;");
        Label myPpText = new Label("PP");
        myPpText.setStyle("-fx-text-fill: #a89060; -fx-font-size: 11;");
        myPpBox.getChildren().addAll(myPpIcon, myPpLabel, myPpText);

        HBox myFoodBox = new HBox(5);
        myFoodBox.setAlignment(Pos.CENTER_LEFT);
        ImageView myFoodIcon = new ImageView(ImageCache.get("/images/token/1food.png"));
        myFoodIcon.setFitWidth(18); myFoodIcon.setPreserveRatio(true);
        myFoodLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12; -fx-font-weight: bold;");
        Label myFoodText = new Label("Cibo");
        myFoodText.setStyle("-fx-text-fill: #a89060; -fx-font-size: 11;");
        myFoodBox.getChildren().addAll(myFoodIcon, myFoodLabel, myFoodText);

        HBox myStatsBox = new HBox(12);
        myStatsBox.setAlignment(Pos.CENTER_LEFT);
        myStatsBox.setPadding(new Insets(4, 8, 4, 8));
        myStatsBox.setStyle(
                "-fx-background-color: rgba(30,15,5,0.75);" +
                        "-fx-border-color: #c9a84c;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );
        myStatsBox.getChildren().addAll(myPpBox, myFoodBox);
        handArea.getChildren().add(0, myStatsBox); // aggiunge in cima alla handArea

        ScrollPane handScroll = new ScrollPane(handArea);
        handScroll.setFitToWidth(true);
        handScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        handScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        handScroll.setMaxHeight(220);
        handScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setBottom(handScroll);

        // ── RIGHT — plance avversari ──
        playersBox.setPadding(new Insets(8));
        playersBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.40);" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;"
        );
        playersBox.setMaxWidth(180);

        Label playersTitle = new Label("Giocatori");
        playersTitle.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12; -fx-font-weight: bold;");
        playersBox.getChildren().add(playersTitle);
        root.setRight(playersBox);

        // ── OVERLAY NOTIFICHE ──
        notificationOverlay.setVisible(false);
        notificationOverlay.setPickOnBounds(false);
        notificationOverlay.setAlignment(Pos.TOP_CENTER);
        notificationOverlay.setPadding(new Insets(60, 0, 0, 0));
        notificationText.setStyle(
                "-fx-background-color: rgba(0,0,0,0.80);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 8;"
        );
        notificationOverlay.getChildren().add(notificationText);

        getChildren().addAll(root, notificationOverlay);
    }

    // ── UPDATE ──

    public void update(GameState state) {
        updateHeader(state);
        updateOrderTile(state);
        updateTopRow(state);
        updateOfferTiles(state);
        updateBottomRow(state);
        updateHand(state);
        updatePlayers(state);
    }

    private void updateHeader(GameState state) {
        roundLabel.setText("Round " + state.getRound() + " | Era " + toRoman(state.getCurrentEra()));
        phaseLabel.setText("Fase: " + formatPhase(state.getCurrentPhaseName()));
        String active = state.getActivePlayerNickname();
        activePlayerLabel.setText(active != null ? "Turno di: " + active : "");
    }

    private void updateOrderTile(GameState state) {
        int count = state.getPlayerStates().size();
        if (count == lastPlayerCount) return; // già caricata
        lastPlayerCount = count;
        int clamped = Math.max(2, Math.min(5, count));
        Image img = ImageCache.get(
                "/images/orderTile/OrdineTurno" + clamped + "players.jpg");
        orderTileView.setImage(img);
    }

    private void updateTopRow(GameState state) {
        diffCardRow(state.getTopRowCardIds(), topRowViews, topRowBox, true);
    }

    private void updateBottomRow(GameState state) {
        diffCardRow(state.getBottomRowCardIds(), bottomRowViews, bottomRowBox, false);
    }

    private void diffCardRow(List<Integer> newIds,
                             Map<Integer, CardView> currentViews,
                             HBox box,
                             boolean isTopRow) {
        // Rimuovi carte non più presenti
        List<Integer> toRemove = currentViews.keySet().stream()
                .filter(id -> !newIds.contains(id))
                .toList();
        for (Integer id : toRemove) {
            box.getChildren().remove(currentViews.get(id));
            currentViews.remove(id);
        }

        // Aggiungi carte nuove
        for (Integer id : newIds) {
            if (!currentViews.containsKey(id)) {
                CardView cv = new CardView(id);
                cv.setOnMouseClicked(e -> {
                    GameState current = localModel.getCurrentState();
                    if (current == null) return;
                    String phase = current.getCurrentPhaseName();
                    if ("AddCardState".equals(phase)) {
                        controller.onAddCard(String.valueOf(id));
                    } else if ("ExtraDrawState".equals(phase) && isTopRow) {
                        controller.onAddExtraCard(String.valueOf(id));
                    }
                });
                currentViews.put(id, cv);
                box.getChildren().add(cv);
            }
        }
    }

    private void updateOfferTiles(GameState state) {
        if (state.getOfferTileStates() == null) return;
        for (OfferTileState ts : state.getOfferTileStates()) {
            char letter = ts.getLetter();
            // Trova il colore del proprietario del totem
            String totemColor = null;
            if (ts.isOccupied() && ts.getTotemOwnerNickname() != null) {
                totemColor = state.getPlayerStates().stream()
                        .filter(p -> p.getNickname().equals(ts.getTotemOwnerNickname()))
                        .findFirst()
                        .map(p -> p.getColor().name().toLowerCase())
                        .orElse(null);
            }
            if (!offerTileViews.containsKey(letter)) {
                OfferTileView view = new OfferTileView(ts, totemColor);
                view.setOnMouseClicked(e -> {
                    GameState current = localModel.getCurrentState();
                    if (current == null) return;
                    if ("PlaceTotemState".equals(current.getCurrentPhaseName())) {
                        controller.onMoveTotem(String.valueOf(letter));
                    }
                });
                offerTileViews.put(letter, view);
                offerRowBox.getChildren().add(view);
            } else {
                offerTileViews.get(letter).update(ts, totemColor);
            }
        }
    }

    private void updateHand(GameState state) {
        if (state.getPlayerStates() == null) return;
        state.getPlayerStates().stream()
                .filter(p -> p.getNickname().equals(myNickname))
                .findFirst()
                .ifPresent(me -> {
                    myPpLabel.setText("" + me.getPP());
                    myFoodLabel.setText("" + me.getFood());
                    diffCardList(me.getCharacterCardIds(), characterViews, characterBox);
                    diffCardList(me.getBuildingCardIds(), buildingViews, buildingBox);
                });
    }

    private void diffCardList(List<Integer> newIds, Map<Integer, CardView> currentViews, FlowPane box) {
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
                currentViews.put(id, cv);
                box.getChildren().add(cv);
            }
        }
    }

    private void updatePlayers(GameState state) {
        if (state.getPlayerStates() == null) return;
        for (PlayerState ps : state.getPlayerStates()) {
            if (ps.getNickname().equals(myNickname)) continue; // skip giocatore locale
            if (!playerBoards.containsKey(ps.getNickname())) {
                PlayerBoardView view = new PlayerBoardView(ps);
                playersBox.getChildren().add(view);
                playerBoards.put(ps.getNickname(), view);
            } else {
                playerBoards.get(ps.getNickname()).update(ps);
            }
        }
    }

    // ── NOTIFICHE ──

    public void showNotification(String message) {
        notificationText.setStyle(
                "-fx-background-color: rgba(0,0,0,0.82);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 8;"
        );
        notificationText.setText(message);
        notificationOverlay.setVisible(true);
        notificationOverlay.setOpacity(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notificationOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(ev -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
            pause.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(
                        Duration.millis(400), notificationOverlay);
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
                "-fx-background-color: rgba(180,30,30,0.88);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 8;"
        );
        showNotification("⚠ " + error);
    }

    // ── UTILITY ──

    private String toRoman(int era) {
        return switch (era) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(era);
        };
    }

    private String formatPhase(String phase) {
        if (phase == null) return "-";
        return switch (phase) {
            case "PlaceTotemState" -> "Piazza Totem";
            case "AddCardState"    -> "Pesca Carte";
            case "ExtraDrawState"  -> "Pesca Extra";
            case "ResolveEventState" -> "Risoluzione Eventi";
            case "EndRoundState"   -> "Fine Round";
            default -> phase;
        };
    }
}