package it.polimi.ingsw.am46.view.gui.scenes;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.gui.utils.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import it.polimi.ingsw.am46.network.dto.PlayerState;

/**
 * Schermata di lobby: mostra i giocatori connessi.
 */
public class LobbyPane extends VBox {

    private final SceneManager sceneManager;

    private final Label titleLabel;
    private final Label statusLabel;
    private final VBox playerListBox;

    public LobbyPane(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        setSpacing(10);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);

        titleLabel = new Label("Lobby");
        titleLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        statusLabel = new Label("In attesa degli altri giocatori...");
        statusLabel.setStyle("-fx-text-fill: #555;");

        playerListBox = new VBox(5);
        playerListBox.setAlignment(Pos.TOP_LEFT);

        getChildren().addAll(titleLabel, statusLabel, playerListBox);
    }

    public void update(GameState state) {
        if (state.getPlayerStates() == null) {
            statusLabel.setText("Nessun giocatore connesso.");
            return;
        }

        // Aggiorna solo se il numero di giocatori è cambiato
        int connected = state.getPlayerStates().size();
        if (playerListBox.getChildren().size() != connected) {
            playerListBox.getChildren().clear();
            for (PlayerState ps : state.getPlayerStates()) {
                Label playerLabel = new Label("• " + ps.getNickname());
                playerLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #2ecc71;");
                playerListBox.getChildren().add(playerLabel);
            }
        }
        statusLabel.setText(connected + " giocatori connessi. In attesa degli altri...");
    }

}
