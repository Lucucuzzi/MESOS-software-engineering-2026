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

/**
 * The type Turn tile view.
 */
public class TurnTileView extends StackPane {

    // Render dimensions (same as OfferTile)
    private static final double RENDER_W = 110;
    private static final double RENDER_H = 150;

    // Original image dimensions
    private static final double IMG_W = 650;
    private static final double IMG_H = 950;

    // Slots on the original image: [slotX, slotY, slotW, slotH]
    // for 2, 3, 4, 5 players
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


    /**
     * Instantiates a new Turn tile view.
     */
    public TurnTileView() {
        imgView.setFitWidth(RENDER_W);
        imgView.setFitHeight(RENDER_H);
        imgView.setPreserveRatio(false);
        overlay.setMouseTransparent(true);
        getChildren().addAll(imgView, overlay);
    }

    /**
     * Update.
     *
     * @param state the state
     */
    public void update(GameState state) {
        if (state.getPlayerStates() == null) return;

        int numPlayers = state.getPlayerStates().size();

        if (numPlayers != lastPlayerCount) {
            lastPlayerCount = numPlayers;
            int clamped = Math.max(2, Math.min(5, numPlayers));
            imgView.setImage(ImageCache.get(
                    "/images/orderTile/OrdineTurno" + clamped + "players.jpg"));
        }

        // Build nickname→color map (always needed)
        java.util.Map<String, Color> colorMap = new java.util.HashMap<>();
        for (PlayerState ps : state.getPlayerStates()) {
            colorMap.put(ps.getNickname(), resolveColor(ps.getColor().name()));
        }

        drawOverlay(state, numPlayers, colorMap);
    }

    private void drawOverlay(GameState state, int numPlayers, java.util.Map<String, Color> colorMap) {
        double[][] slots = getSlotsForCount(numPlayers);
        double scaleX = RENDER_W / IMG_W;
        double scaleY = RENDER_H / IMG_H;

        GraphicsContext gc = overlay.getGraphicsContext2D();
        gc.clearRect(0, 0, RENDER_W, RENDER_H);

        List<String> turnOrder = state.getTurnOrderWithGaps();
        if (turnOrder == null || turnOrder.isEmpty()) return;

        double marginX = 5;
        double marginY = 2;

        for (int i = 0; i < turnOrder.size(); i++) {
            if (i >= slots.length) break;

            String nickname = turnOrder.get(i);
            if (nickname == null) continue;

            Color base = colorMap.get(nickname);
            if (base == null) continue;

            double rx = slots[i][0] * scaleX + marginX;
            double ry = slots[i][1] * scaleY + marginY;
            double rw = slots[i][2] * scaleX - marginX * 2;
            double rh = slots[i][3] * scaleY - marginY * 2;

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

}