package it.polimi.ingsw.am46.view.gui.scenes;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import it.polimi.ingsw.am46.view.gui.utils.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/**
 * Lobby pane.
 */
public class LobbyPane extends StackPane {

    private final Label statusLabel;
    private final VBox playerListBox;
    private final Label startingLabel = new Label("✦ The game is starting... ✦");

    /**
     * Creates a new lobby pane.
     *
     * @param sceneManager the scene manager
     */
    public LobbyPane(SceneManager sceneManager) {

        // LAYER 1 — background
        ImageView bgView = new ImageView();
        bgView.setImage(ImageCache.getFull("/images/backgrounds/lobby_bg.jpg"));
        bgView.setPreserveRatio(false);
        bgView.fitWidthProperty().bind(widthProperty());
        bgView.fitHeightProperty().bind(heightProperty());
        getChildren().add(bgView);

        // LAYER 2 — dark overlay for readability
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        getChildren().add(overlay);

        // LAYER 3 — content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(40));

        Label titleLabel = new Label("MESOS");
        titleLabel.setStyle(
                "-fx-text-fill: #c9a84c;" +
                        "-fx-font-size: 52;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 12, 0.8, 0, 0);"
        );

        Label subtitle = new Label("The Mesolithic Game");
        subtitle.setStyle(
                "-fx-text-fill: #a89060;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-style: italic;"
        );

        // Player list box
        VBox listBox = new VBox(10);
        listBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.55);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(201,168,76,0.6);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 20;"
        );
        listBox.setMaxWidth(420);

        Label listTitle = new Label("Waiting Room");
        listTitle.setStyle(
                "-fx-text-fill: #c9a84c;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;"
        );

        playerListBox = new VBox(8);

        statusLabel = new Label("Waiting for other players...");
        statusLabel.setStyle(
                "-fx-text-fill: #9a8a6a;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-style: italic;"
        );

        startingLabel.setStyle(
                "-fx-text-fill: #e07b30;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 8, 0.6, 0, 0);"
        );
        startingLabel.setVisible(false);
        listBox.getChildren().addAll(listTitle, playerListBox, statusLabel, startingLabel);
        content.getChildren().addAll(titleLabel, subtitle, listBox);
        getChildren().add(content);
    }

    /**
     * Updates the lobby with the current game state.
     *
     * @param state the state
     */
    public void update(GameState state) {
        if (state.getPlayerStates() == null) {
            statusLabel.setText("No players connected.");
            return;
        }

        int connected = state.getPlayerStates().size();
        if (playerListBox.getChildren().size() != connected) {
            playerListBox.getChildren().clear();
            for (PlayerState ps : state.getPlayerStates()) {
                String color = toFxColor(ps.getColor().name());
                Label playerLabel = new Label("⚔ " + ps.getNickname());
                playerLabel.setStyle("-fx-text-fill: " + color + ";" + "-fx-font-size: 15;" + "-fx-font-weight: bold;"
                );
                playerListBox.getChildren().add(playerLabel);
            }
        }
        int c = connected;
        statusLabel.setText(
                c + " player" + (c == 1 ? "" : "s") + " connected. Waiting for others..."
        );
    }

    private String toFxColor(String colorName) {
        return switch (colorName) {
            case "RED"    -> "#e74c3c";
            case "BLUE"   -> "#3498db";
            case "GREEN"  -> "#2ecc71";
            case "YELLOW" -> "#f1c40f";
            case "PURPLE" -> "#9b59b6";
            case "WHITE"  -> "#ecf0f1";
            default       -> "#c9a84c";
        };
    }

    /**
     * Shows the match starting message.
     */
    public void showStarting() {
        statusLabel.setVisible(false);
        startingLabel.setVisible(true);
    }
}
