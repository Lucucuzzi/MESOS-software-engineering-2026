package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Vista grafica di una carta. Usa la ImageCache per evitare ricaricamenti.
 */
public class CardView extends StackPane {

    private final ImageView imageView;
    private final Label cardLabel;
    private final int cardId;

    public CardView(int cardId) {
        this.cardId = cardId;

        setAlignment(Pos.BOTTOM_CENTER);
        setStyle("-fx-background-color: transparent;");

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(85);

        cardLabel = new Label("Card #" + cardId);
        cardLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 3 6 3 6;" +
                        "-fx-font-size: 14;"
        );

        imageView.setImage(ImageCache.get("/images/cards/card_" + cardId + ".png"));

        getChildren().addAll(imageView, cardLabel);

        setOnMouseEntered(e -> { setScaleX(1.08); setScaleY(1.08); });
        setOnMouseExited(e -> { setScaleX(1.0); setScaleY(1.0); });
    }

    public int getCardId() { return cardId; }
}