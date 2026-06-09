package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Map;

/**
 * The type Player board view.
 */
public class PlayerBoardView extends VBox {

    private final Label nameLabel;
    private final Label ppValueLabel;
    private final Label foodValueLabel;
    private final FlowPane cardsBox = new FlowPane(4, 4);
    private PlayerState data;
    private final Map<Integer, ImageView> cardViews = new java.util.LinkedHashMap<>();

    /**
     * Instantiates a new Player board view.
     *
     * @param data the data
     */
    public PlayerBoardView(PlayerState data) {
        this.data = data;

        setSpacing(6);
        setPadding(new Insets(8));
        setAlignment(Pos.TOP_LEFT);
        setStyle(
                "-fx-background-color: rgba(30,15,5,0.75);" +
                        "-fx-border-color: #c9a84c;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        // Nome giocatore
        nameLabel = new Label(data.getNickname());
        nameLabel.setStyle(
                "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c9a84c;"
        );

        // ── Box PP ──
        HBox ppBox = new HBox(5);
        ppBox.setAlignment(Pos.CENTER_LEFT);

        ImageView ppIcon = new ImageView();
        ppIcon.setImage(ImageCache.get("/images/token/1pp.png"));
        ppIcon.setFitWidth(18);
        ppIcon.setFitHeight(18);
        ppIcon.setPreserveRatio(true);

        ppValueLabel = new Label("" + data.getPP());
        ppValueLabel.setStyle(
                "-fx-text-fill: #f1c40f;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-weight: bold;"
        );

        Label ppText = new Label("PP");
        ppText.setStyle("-fx-text-fill: #a89060; -fx-font-size: 11;");

        ppBox.getChildren().addAll(ppIcon, ppValueLabel, ppText);

        // ── Box Cibo ──
        HBox foodBox = new HBox(5);
        foodBox.setAlignment(Pos.CENTER_LEFT);

        ImageView foodIcon = new ImageView();
        foodIcon.setImage(ImageCache.get("/images/token/1food.png"));
        foodIcon.setFitWidth(18);
        foodIcon.setFitHeight(18);
        foodIcon.setPreserveRatio(true);

        foodValueLabel = new Label("" + data.getFood());
        foodValueLabel.setStyle(
                "-fx-text-fill: #e67e22;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-weight: bold;"
        );

        Label foodText = new Label("Cibo");
        foodText.setStyle("-fx-text-fill: #a89060; -fx-font-size: 11;");

        foodBox.getChildren().addAll(foodIcon, foodValueLabel, foodText);

        // ── Stats box (PP e Cibo affiancati) ──
        HBox statsBox = new HBox(12);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(4, 6, 4, 6));
        statsBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.35);" +
                        "-fx-background-radius: 6;"
        );
        statsBox.getChildren().addAll(ppBox, foodBox);

        // ── Carte ──
        cardsBox.setMaxWidth(160);

        getChildren().addAll(nameLabel, statsBox, cardsBox);

        updateCards(data);
    }

    /**
     * Update.
     *
     * @param newData the new data
     */
    public void update(PlayerState newData) {
        this.data = newData;
        ppValueLabel.setText("" + newData.getPP());
        foodValueLabel.setText("" + newData.getFood());
        updateCards(newData);
    }

    private void updateCards(PlayerState ps) {
        List<Integer> newIds = ps.getCardIds();

        List<Integer> toRemove = cardViews.keySet().stream()
                .filter(id -> !newIds.contains(id))
                .toList();
        for (Integer id : toRemove) {
            cardsBox.getChildren().remove(cardViews.get(id));
            cardViews.remove(id);
        }

        for (Integer id : newIds) {
            if (!cardViews.containsKey(id)) {
                ImageView iv = new ImageView();
                iv.setImage(ImageCache.get("/images/cards/card_" + id + ".png"));
                iv.setFitWidth(40);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                cardViews.put(id, iv);
                cardsBox.getChildren().add(iv);
            }
        }
    }
}