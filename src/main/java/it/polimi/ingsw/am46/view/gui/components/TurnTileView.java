package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.List;

public class TurnTileView extends StackPane {

    // Dimensioni di render (uguali alle OfferTile)
    private static final double RENDER_W = 110;
    private static final double RENDER_H = 150;

    // Dimensioni originali dell'immagine
    private static final double IMG_W = 650;
    private static final double IMG_H = 950;

    // Slot sull'immagine originale: [slotX, slotY, slotW, slotH]
    // per 2, 3, 4, 5 giocatori
    private static final double[][] SLOTS_2 = {
            {155, 240, 330, 130},
            {155, 380, 330, 130},
    };
    private static final double[][] SLOTS_3 = {
            {155, 175, 330, 115},
            {155, 315, 330, 115},
            {155, 455, 330, 115},
    };
    private static final double[][] SLOTS_4 = {
            {155, 130, 330, 105},
            {155, 260, 330, 105},
            {155, 390, 330, 105},
            {155, 520, 330, 105},
    };
    private static final double[][] SLOTS_5 = {
            {155, 100, 330, 100},
            {155, 215, 330, 100},
            {155, 330, 330, 100},
            {155, 445, 330, 100},
            {155, 560, 330, 100},
    };

    private final ImageView imgView = new ImageView();
    private final Canvas    overlay = new Canvas(RENDER_W, RENDER_H);

    private int lastPlayerCount = -1;

    public TurnTileView() {
        imgView.setFitWidth(RENDER_W);
        imgView.setFitHeight(RENDER_H);
        imgView.setPreserveRatio(false);
        overlay.setMouseTransparent(true);
        getChildren().addAll(imgView, overlay);
    }

    public void update(GameState state) {
        if (state.getPlayerStates() == null) return;

        int numPlayers = state.getPlayerStates().size();

        // Carica immagine solo se cambia il numero di giocatori
        if (numPlayers != lastPlayerCount) {
            lastPlayerCount = numPlayers;
            int clamped = Math.max(2, Math.min(5, numPlayers));
            imgView.setImage(ImageCache.get(
                    "/images/orderTile/OrdineTurno" + clamped + "players.jpg"));
        }

        // Disegna i rettangoli colorati sugli slot
        drawOverlay(state, numPlayers);
    }

    private void drawOverlay(GameState state, int numPlayers) {
        double[][] slots = getSlotsForCount(numPlayers);
        double scaleX = RENDER_W / IMG_W;
        double scaleY = RENDER_H / IMG_H;

        List<String> turnOrder = state.getTurnOrder();
        int remaining  = (turnOrder != null) ? turnOrder.size() : 0;
        int emptySlots = numPlayers - remaining;

        GraphicsContext gc = overlay.getGraphicsContext2D();
        gc.clearRect(0, 0, RENDER_W, RENDER_H);

        double marginX = 5;
        double marginY = 2;

        for (int i = 0; i < slots.length; i++) {
            double rx = slots[i][0] * scaleX + marginX;
            double ry = slots[i][1] * scaleY + marginY;
            double rw = slots[i][2] * scaleX - marginX * 2;
            double rh = slots[i][3] * scaleY - marginY * 2;

            if (i < emptySlots) continue;

            int listIndex = i - emptySlots;
            if (listIndex >= remaining) continue;

            String nickname = turnOrder.get(listIndex);
            PlayerState ps = state.getPlayerStates().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst().orElse(null);
            if (ps == null) continue;

            Color base = resolveColor(ps.getColor().name());

            gc.setFill(base);
            gc.fillRect(rx, ry, rw, rh);

            gc.setStroke(base.darker());
            gc.setLineWidth(1);
            gc.strokeRect(rx, ry, rw, rh);
        }
    }

    private double[][] getSlotsForCount(int n) {
        return switch (n) {
            case 2  -> SLOTS_2;
            case 3  -> SLOTS_3;
            case 4  -> SLOTS_4;
            default -> SLOTS_5;
        };
    }

    private Color resolveColor(String colorName) {
        return switch (colorName.toUpperCase()) {
            case "RED"    -> Color.rgb(237,  85,  59);  // Pantone 1788
            case "YELLOW" -> Color.rgb(242, 185,  23);  // Pantone 7406
            case "BLUE"   -> Color.rgb(  0, 168, 181);  // Pantone 3135
            case "PURPLE" -> Color.rgb( 56,  18,  45);  // Pantone 7449
            case "WHITE"  -> Color.rgb(237, 237, 237);  // White
            default       -> Color.rgb(150, 150, 150);
        };
    }

    private String shortenName(String name) {
        return name.length() > 8 ? name.substring(0, 7) + "…" : name;
    }
}