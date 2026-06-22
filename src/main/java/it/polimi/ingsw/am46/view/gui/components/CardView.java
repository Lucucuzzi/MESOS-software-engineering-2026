package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Graphical view of a card. Uses ImageCache to avoid reloading.
 */
public class CardView extends StackPane {

    private final ImageView imageView;
    private final int cardId;

    /**
     * Instantiates a new Card view.
     *
     * @param cardId the card id
     */
    public CardView(int cardId){
        this(cardId, 90);
    }

    /**
     * Instantiates a new Card view.
     *
     * @param cardId the card id
     * @param width  the width
     */
    public CardView(int cardId, double width) {
        this.cardId = cardId;

        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
        imageView.setSmooth(true);
        imageView.setImage(ImageCache.get("/images/cards/card_" + cardId + ".png"));

        // Rounded clip
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setWidth(width);
        clip.setHeight(width * 1.4);
        clip.setArcWidth(14);
        clip.setArcHeight(14);
        imageView.setClip(clip);

        imageView.setEffect(new javafx.scene.effect.DropShadow(6, 0, 2, javafx.scene.paint.Color.rgb(0,0,0,0.4)));

        getChildren().add(imageView);

        setOnMouseEntered(e -> { setScaleX(1.08); setScaleY(1.08); });
        setOnMouseExited(e -> { setScaleX(1.0); setScaleY(1.0); });
    }

    /**
     * Gets card id.
     *
     * @return the card id
     */
    public int getCardId() { return cardId; }
}