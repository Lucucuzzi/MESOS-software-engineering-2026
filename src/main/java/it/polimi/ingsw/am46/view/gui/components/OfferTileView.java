package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.network.dto.OfferTileState;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Vista grafica di una tessera offerta.
 */
public class OfferTileView extends StackPane {

    private final ImageView imageView;
    private final Label statusLabel;
    private OfferTileState data;

    public OfferTileView(OfferTileState data) {
        this.data = data;

        setAlignment(Pos.BOTTOM_CENTER);
        setStyle("-fx-background-color: transparent;");

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(90);

        imageView.setImage(ImageCache.get("/images/tiles/tile_" + data.getLetter() + ".png"));

        statusLabel = new Label();
        statusLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 2 4 2 4;" +
                        "-fx-font-size: 12;"
        );

        updateStatusText();

        getChildren().addAll(imageView, statusLabel);

        setOnMouseEntered(e -> { setScaleX(1.05); setScaleY(1.05); });
        setOnMouseExited(e -> { setScaleX(1.0); setScaleY(1.0); });
    }

    private void updateStatusText() {
        String text = data.isOccupied()
                ? "Occupata (totem di " + data.getTotemOwnerNickname() + ")"
                : "Libera";
        statusLabel.setText(text);
    }

    // Solo lo stato viene aggiornato — l'immagine rimane invariata
    public void update(OfferTileState newData) { this.data = newData; updateStatusText(); }
    public OfferTileState getData() {
        return data;
    }
}