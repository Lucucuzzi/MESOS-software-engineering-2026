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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.*;

/**
 * The type Game pane.
 */
public class GamePane extends StackPane {

    private final ClientController controller;
    private final LocalModel localModel;
    private final String myNickname;

    // Header
    private final Label roundLabel;
    private final Label phaseLabel;
    private final Label activePlayerLabel;

    // Card rows
    private final HBox topRowBox = new HBox(8);
    private final HBox bottomRowBox = new HBox(8);

    // Offer track
    private final HBox offerRowBox = new HBox(8);

    // Local player hand
    private final FlowPane characterBox = new FlowPane(6,6);
    private final FlowPane buildingBox = new FlowPane(6,6);
    private final Label myPpLabel = new Label();
    private final Label myFoodLabel = new Label();

    // Opponent boards
    private final VBox playersBox = new VBox(10);

    // Notification overlay
    private final StackPane notificationOverlay = new StackPane();
    private final Label notificationText = new Label();

    //Event pop-up
    private final EventPopup eventPopup = new EventPopup();
    private GameState lastState = null;
    private final List<Integer> shownEventIds = new ArrayList<>();

    //game-pause banner
    private final HBox pauseBanner = new HBox();

    // Extra draw banner
    private final HBox extraDrawBanner = new HBox(16);
    private boolean extraDrawBannerVisible = false;

    // Smart redraw maps
    private final Map<Integer, CardView> topRowViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> bottomRowViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> characterViews = new LinkedHashMap<>();
    private final Map<Integer, CardView> buildingViews = new LinkedHashMap<>();
    private final Map<Character, OfferTileView> offerTileViews = new LinkedHashMap<>();
    private final Map<String, PlayerBoardView> playerBoards = new HashMap<>();

    // Order tile
    private final TurnTileView turnTileView = new TurnTileView();
    private int lastPlayerCount = -1;

    /**
     * Instantiates a new Game pane.
     *
     * @param controller   the controller
     * @param localModel   the local model
     * @param myNickname   the my nickname
     * @param screenHeight the screen height
     */
    public GamePane(ClientController controller, LocalModel localModel, String myNickname, double screenHeight)  {
        this.controller = controller;
        this.localModel = localModel;
        this.myNickname = myNickname;

        // LAYER 1 — background (adapts to window)
        ImageView bgView = new ImageView();
        bgView.setImage(ImageCache.getFull("/images/backgrounds/game_bg.png"));
        bgView.setPreserveRatio(false);
        // Bind bg size to StackPane size
        bgView.fitWidthProperty().bind(widthProperty());
        bgView.fitHeightProperty().bind(heightProperty());
        getChildren().add(bgView);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(0));
        // Root fills all StackPane space
        StackPane.setAlignment(root, Pos.TOP_LEFT);

        // ── HEADER ──
        HBox header = new HBox(24);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #3d1f0a;");

        roundLabel = new Label("Round - | Era -");
        roundLabel.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 14; -fx-font-weight: bold;");

        phaseLabel = new Label("Phase: -");
        phaseLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13;");

        activePlayerLabel = new Label("Turn: -");
        activePlayerLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(roundLabel, phaseLabel, spacer, activePlayerLabel);
        root.setTop(header);

        // ── CENTER — card rows + offer track ──
        VBox centerArea = new VBox(4);
        centerArea.setAlignment(Pos.CENTER);
        centerArea.setPadding(new Insets(6, 8, 6, 8));

        topRowBox.setAlignment(Pos.CENTER);
        topRowBox.setPadding(new Insets(2));

        offerRowBox.setAlignment(Pos.CENTER);
        offerRowBox.setPadding(new Insets(4));
        offerRowBox.setStyle("-fx-background-color: transparent;");

        offerRowBox.getChildren().add(turnTileView);

        bottomRowBox.setAlignment(Pos.CENTER);
        bottomRowBox.setPadding(new Insets(2));

        centerArea.getChildren().addAll(topRowBox, offerRowBox, bottomRowBox);
        root.setCenter(centerArea);

        // ── BOTTOM — local player's hand ──
        // Characters on the left (grow right), Buildings on the right (grow left)
        HBox handRow = new HBox();
        handRow.setPadding(new Insets(4));

        VBox charSection = new VBox(4);
        Label charTitle = new Label("Characters");
        charTitle.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12; -fx-font-weight: bold;");
        characterBox.setAlignment(Pos.TOP_LEFT);
        characterBox.setHgap(6);
        characterBox.setVgap(6);
        charSection.getChildren().addAll(charTitle, characterBox);
        HBox.setHgrow(charSection, Priority.ALWAYS);

        VBox buildSection = new VBox(4);
        buildSection.setAlignment(Pos.TOP_RIGHT);
        Label buildTitle = new Label("Buildings");
        buildTitle.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12; -fx-font-weight: bold;");
        buildingBox.setAlignment(Pos.TOP_RIGHT);
        buildingBox.setHgap(6);
        buildingBox.setVgap(6);
        buildSection.getChildren().addAll(buildTitle, buildingBox);

        handRow.getChildren().addAll(charSection, buildSection);

        VBox handArea = new VBox(4);
        handArea.setPadding(new Insets(4, 16, 8, 16));
        handArea.setStyle("-fx-background-color: transparent;");
        handArea.getChildren().add(handRow);

        // ── Local player stats ──
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
        Label myFoodText = new Label("Food");
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

        ScrollPane handScroll = new ScrollPane(handArea);
        handScroll.setFitToWidth(true);
        handScroll.setFitToHeight(true);
        handScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        handScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        double bottomHeight = screenHeight * 0.20; // 20% of available screen height
        handScroll.setPrefHeight(bottomHeight);
        handScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox bottomArea = new VBox(0);
        bottomArea.setStyle(
                "-fx-background-color: rgba(0,0,0,0.50);" +
                        "-fx-background-radius: 10 10 0 0;"
        );
        bottomArea.getChildren().addAll(myStatsBox, handScroll);
        VBox.setVgrow(handScroll, Priority.ALWAYS);
        root.setBottom(bottomArea);
        BorderPane.setAlignment(bottomArea, Pos.BOTTOM_CENTER);

        // ── RIGHT — opponent boards ──
        playersBox.setPadding(new Insets(8));
        playersBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.40);" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;"
        );
        playersBox.setPrefWidth(190);
        playersBox.setMaxWidth(190);

        Label playersTitle = new Label("Players");
        playersTitle.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12; -fx-font-weight: bold;");
        playersBox.getChildren().add(playersTitle);
        ScrollPane playersScroll = new ScrollPane(playersBox);
        playersScroll.setFitToWidth(true);
        playersScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        playersScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        playersScroll.setPrefWidth(200);
        playersScroll.setMaxWidth(200);
        playersScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setRight(playersScroll);

        // ── NOTIFICATION OVERLAY ──
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

        // Root must fill all StackPane space
        StackPane.setAlignment(root, Pos.TOP_LEFT);
        root.prefWidthProperty().bind(widthProperty());
        root.prefHeightProperty().bind(heightProperty());

        // ── EXTRA DRAW BANNER ──
        extraDrawBanner.setAlignment(Pos.CENTER);
        extraDrawBanner.setPadding(new Insets(12, 24, 12, 24));
        extraDrawBanner.setStyle(
                "-fx-background-color: rgba(20,10,3,0.88);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #c9a84c;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );
        extraDrawBanner.setPickOnBounds(false);

        Label extraDrawText = new Label("✦  You can draw an extra card from the top row  ✦");
        extraDrawText.setStyle(
                "-fx-text-fill: #c9a84c; -fx-font-size: 13; -fx-font-weight: bold;"
        );

        javafx.scene.control.Button skipBtn = new javafx.scene.control.Button("Skip");
        skipBtn.setStyle(
                "-fx-background-color: #6b3a1f; -fx-text-fill: #f1c40f;" +
                        "-fx-font-size: 12; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 6 18;" +
                        "-fx-border-color: #c9a84c; -fx-border-width: 1; -fx-border-radius: 8;"
        );
        skipBtn.setOnMouseEntered(e -> skipBtn.setStyle(
                "-fx-background-color: #8b5a2f; -fx-text-fill: #f1c40f;" +
                        "-fx-font-size: 12; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 6 18;" +
                        "-fx-border-color: #c9a84c; -fx-border-width: 1; -fx-border-radius: 8;"
        ));
        skipBtn.setOnMouseExited(e -> skipBtn.setStyle(
                "-fx-background-color: #6b3a1f; -fx-text-fill: #f1c40f;" +
                        "-fx-font-size: 12; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-padding: 6 18;" +
                        "-fx-border-color: #c9a84c; -fx-border-width: 1; -fx-border-radius: 8;"
        ));
        skipBtn.setOnAction(e -> {
            controller.onSkipExtraDraw();
            hideExtraDrawBanner();
        });

        extraDrawBanner.getChildren().addAll(extraDrawText, skipBtn);
        extraDrawBanner.setVisible(false);
        extraDrawBanner.setOpacity(0);
        StackPane.setAlignment(extraDrawBanner, Pos.CENTER);
        StackPane.setMargin(extraDrawBanner, new Insets(450, 50, 50, 50));
        StackPane.setAlignment(eventPopup, Pos.CENTER);

        // ── PAUSE BANNER ──
        pauseBanner.setAlignment(Pos.CENTER);
        pauseBanner.setPadding(new Insets(12, 24, 12, 24));
        pauseBanner.setStyle(
                "-fx-background-color: rgba(10,10,10,0.92);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #7a5c2e;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );
        pauseBanner.setPickOnBounds(false);

        Label pauseText = new Label("⏸ Game paused — waiting for other players to reconnect...");
        pauseText.setStyle(
                "-fx-text-fill: #a89060; -fx-font-size: 13; -fx-font-weight: bold;"
        );

        pauseBanner.getChildren().add(pauseText);
        pauseBanner.setVisible(false);
        pauseBanner.setManaged(false);
        StackPane.setAlignment(pauseBanner, Pos.TOP_CENTER);
        StackPane.setMargin(pauseBanner, new Insets(70, 50, 50, 50));

        getChildren().addAll(root, notificationOverlay, extraDrawBanner, eventPopup, pauseBanner);
    }

    // ── UPDATE ──

    /**
     * Update.
     *
     * @param state the state
     */
    public void update(GameState state) {
        if (state.isGamePaused()) {
            showPauseBanner();
        } else {
            hidePauseBanner();
        }
        updateHeader(state);
        turnTileView.update(state);
        updateTopRow(state);
        updateOfferTiles(state);
        updateBottomRow(state);
        updateHand(state);
        updatePlayers(state);
        maybeShowEventPopup(state);
    }

    private void showPauseBanner() {
        pauseBanner.setVisible(true);
        pauseBanner.setManaged(true);
    }

    private void hidePauseBanner() {
        pauseBanner.setVisible(false);
        pauseBanner.setManaged(false);
    }

    private void updateHeader(GameState state) {
        roundLabel.setText("Round " + state.getRound() + " | Era " + toRoman(state.getCurrentEra()));
        phaseLabel.setText("Phase: " + formatPhase(state.getCurrentPhaseName()));
        String active = state.getActivePlayerNickname();
        activePlayerLabel.setText(active != null ? "Turn: " + active : "");

        boolean isMyExtraTurn = "ExtraDrawState".equals(state.getCurrentPhaseName())
                && myNickname.equals(active);
        if (isMyExtraTurn) showExtraDrawBanner();
        else hideExtraDrawBanner();
    }

    private void updateTopRow(GameState state) {
        diffCardRow(state.getTopRowCardIds(), topRowViews, topRowBox, true);
    }

    private void updateBottomRow(GameState state) {
        diffCardRow(state.getBottomRowCardIds(), bottomRowViews, bottomRowBox, false);
    }

    private void diffCardRow(List<Integer> newIds, Map<Integer, CardView> currentViews, HBox box, boolean isTopRow) {
        // Remove cards no longer present
        List<Integer> toRemove = currentViews.keySet().stream()
                .filter(id -> !newIds.contains(id))
                .toList();
        for (Integer id : toRemove) {
            box.getChildren().remove(currentViews.get(id));
            currentViews.remove(id);
        }

        // Add new cards
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
            // Find the totem owner's color
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
                CardView cv = new CardView(id, 65);
                currentViews.put(id, cv);
                box.getChildren().add(cv);
            }
        }
    }

    private void updatePlayers(GameState state) {
        if (state.getPlayerStates() == null) return;
        for (PlayerState ps : state.getPlayerStates()) {
            if (ps.getNickname().equals(myNickname)) continue; // skip local player
            if (!playerBoards.containsKey(ps.getNickname())) {
                PlayerBoardView view = new PlayerBoardView(ps);
                playersBox.getChildren().add(view);
                playerBoards.put(ps.getNickname(), view);
            } else {
                playerBoards.get(ps.getNickname()).update(ps);
            }
        }
    }

    private void maybeShowEventPopup(GameState state) {
        List<Integer> events = state.getRecentlyResolvedEvents();

        if (events != null && !events.isEmpty()) {
            List<Integer> newEvents = events.stream()
                    .filter(id -> !shownEventIds.contains(id))
                    .toList();

            if (!newEvents.isEmpty()) {
                shownEventIds.addAll(newEvents);
                eventPopup.show(state, lastState);
            }
        }
        lastState = state;
    }

    // ── NOTIFICATIONS ──

    /**
     * Show notification.
     *
     * @param message the message
     */
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

    /**
     * Show error.
     *
     * @param error the error
     */
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

    // ── EXTRA DRAW BANNER ──

    private void showExtraDrawBanner() {
        if (extraDrawBannerVisible) return;
        extraDrawBannerVisible = true;
        extraDrawBanner.setVisible(true);
        FadeTransition in = new FadeTransition(Duration.millis(300), extraDrawBanner);
        in.setFromValue(0);
        in.setToValue(1);
        in.play();
    }

    private void hideExtraDrawBanner() {
        if (!extraDrawBannerVisible) return;
        extraDrawBannerVisible = false;
        FadeTransition out = new FadeTransition(Duration.millis(250), extraDrawBanner);
        out.setFromValue(1);
        out.setToValue(0);
        out.setOnFinished(e -> extraDrawBanner.setVisible(false));
        out.play();
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
            case "PlaceTotemState" -> "Place Totem";
            case "AddCardState"    -> "Draw Cards";
            case "ExtraDrawState"  -> "Extra Draw";
            case "ResolveEventState" -> "Resolve Events";
            case "EndRoundState"   -> "End Round";
            default -> phase;
        };
    }
}