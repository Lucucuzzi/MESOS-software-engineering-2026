package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;

/**
 * Vista grafica di una carta. Usa la ImageCache per evitare ricaricamenti.
 */
public class CardView extends StackPane {

    private final ImageView imageView;
    private final int cardId;

    public CardView(int cardId) {
        this.cardId = cardId;

        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(90);
        imageView.setSmooth(true);
        imageView.setImage(ImageCache.get("/images/cards/card_" + cardId + ".png"));

        // Clip arrotondato
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setWidth(90);
        clip.setHeight(126);
        clip.setArcWidth(14);
        clip.setArcHeight(14);
        imageView.setClip(clip);

        imageView.setEffect(new javafx.scene.effect.DropShadow(6, 0, 2, javafx.scene.paint.Color.rgb(0,0,0,0.4)));

        getChildren().add(imageView);

        setOnMouseEntered(e -> { setScaleX(1.08); setScaleY(1.08); });
        setOnMouseExited(e -> { setScaleX(1.0); setScaleY(1.0); });
    }

    public int getCardId() { return cardId; }
}