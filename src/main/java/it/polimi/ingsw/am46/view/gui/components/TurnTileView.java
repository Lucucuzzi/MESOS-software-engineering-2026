package it.polimi.ingsw.am46.view.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Rappresenta una posizione nell'ordine di turno. 
 */
public class TurnTileView extends HBox {

    private final Label positionLabel;
    private final Label playerLabel;

    private int position;
    private String playerName;

    public TurnTileView(int position, String playerName) {
        this.position = position;
        this.playerName = playerName;

        setSpacing(5);
        setPadding(new Insets(3));
        setAlignment(Pos.CENTER_LEFT);
        setStyle( "-fx-background-color: rgba(0,0,0,0.2);" + "-fx-border-color: #7f8c8d;");

        positionLabel = new Label("#" + position);
        positionLabel.setStyle( "-fx-text-fill: #ecf0f1;" + "-fx-font-weight: bold;" );

        playerLabel = new Label(playerName);
        playerLabel.setStyle("-fx-text-fill: #ecf0f1;");

        getChildren().addAll(positionLabel, playerLabel);
    }

    public void update(int newPosition, String newPlayerName) {
        this.position = newPosition;
        this.playerName = newPlayerName;
        positionLabel.setText("#" + newPosition);
        playerLabel.setText(newPlayerName);
    }

    public int getPosition() {
        return position;
    }

    public String getPlayerName() {
        return playerName;
    }
}