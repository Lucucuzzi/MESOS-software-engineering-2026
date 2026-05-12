package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.network.dto.PlayerState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Vista grafica della plancia di un giocatore.
 */
public class PlayerBoardView extends VBox {

    private final Label nameLabel;
    private final Label scoreLabel;
    private final Label foodLabel;
    private PlayerState data;

    public PlayerBoardView(PlayerState data) {
        this.data = data;

        setSpacing(6);
        setPadding(new Insets(8));
        setAlignment(Pos.TOP_LEFT);
        setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-border-color: #7f8c8d;" +
                        "-fx-border-width: 1;"
        );

        nameLabel = new Label(data.getNickname());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white;");

        scoreLabel = new Label("PP: " + data.getPP());
        scoreLabel.setStyle("-fx-text-fill: white;");

        foodLabel = new Label("Cibo: " + data.getFood());
        foodLabel.setStyle("-fx-text-fill: white;");

        getChildren().addAll(nameLabel, scoreLabel, foodLabel);
    }

    public void update(PlayerState newData) {
        this.data = newData;
        scoreLabel.setText("PP: " + newData.getPP());
        foodLabel.setText("Cibo: " + newData.getFood());
    }
}
