package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.network.dto.OfferTileState;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class OfferTileView extends StackPane {

    private final ImageView tileView;
    private final ImageView totemView;
    private OfferTileState data;

    public OfferTileView(OfferTileState data, String totemColor) {
        this.data = data;

        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");

        tileView = new ImageView();
        tileView.setPreserveRatio(true);
        tileView.setFitWidth(110);
        tileView.setFitHeight(150);
        tileView.setImage(ImageCache.get("/images/offerTile/tile_" + Character.toLowerCase(data.getLetter()) + ".jpg"));

        totemView = new ImageView();
        totemView.setFitWidth(36);
        totemView.setFitHeight(36);
        totemView.setPreserveRatio(true);
        totemView.setVisible(false);
        StackPane.setAlignment(totemView, Pos.TOP_CENTER);

        getChildren().addAll(tileView, totemView);

        updateTotem(totemColor);

        setOnMouseEntered(e -> { setScaleX(1.05); setScaleY(1.05); });
        setOnMouseExited(e -> { setScaleX(1.0); setScaleY(1.0); });
    }

    private void updateTotem(String totemColor) {
        if (data.isOccupied() && totemColor != null) {
            String path = "/images/totem/totem_" + totemColor + ".png";
            totemView.setImage(ImageCache.get(path));
            totemView.setVisible(true);
        } else {
            totemView.setVisible(false);
        }
    }

    public void update(OfferTileState newData, String totemColor) {
        this.data = newData;
        updateTotem(totemColor);
    }

    public OfferTileState getData() {
        return data;
    }
}