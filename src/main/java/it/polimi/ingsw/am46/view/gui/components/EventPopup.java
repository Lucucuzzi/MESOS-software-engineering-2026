package it.polimi.ingsw.am46.view.gui.components;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.utils.CardDictionary;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

/**
 * Popup semitrasparente che mostra gli eventi risolti e i loro effetti
 * su PP e cibo per ogni giocatore. Si chiude automaticamente dopo 4 secondi
 * o al click.
 */
public class EventPopup extends StackPane {

    /**
     * Instantiates a new Event popup.
     */
    public EventPopup() {
        setPickOnBounds(false);
        setAlignment(Pos.CENTER);
        setVisible(false);
        setOpacity(0);
    }

    /**
     * Mostra il popup con gli eventi risolti e i delta calcolati
     * confrontando lo stato precedente con quello attuale.
     *
     * @param current  stato attuale (dopo la risoluzione)
     * @param previous stato precedente (prima della risoluzione)
     */
    public void show(GameState current, GameState previous) {
        List<Integer> eventIds = current.getRecentlyResolvedEvents();
        if (eventIds == null || eventIds.isEmpty()) return;

        getChildren().clear();

        // Contenitore principale del popup
        VBox box = new VBox(12);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(20, 28, 20, 28));
        box.setMaxWidth(420);
        box.setStyle(
                "-fx-background-color: rgba(20,10,3,0.93);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #c9a84c;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0.6, 0, 4);"
        );

        // Titolo
        Label title = new Label("⚡  Eventi Risolti");
        title.setStyle(
                "-fx-text-fill: #c9a84c;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;"
        );
        box.getChildren().add(title);

        // Separatore
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #c9a84c; -fx-opacity: 0.4;");
        box.getChildren().add(sep);

        // Una sezione per ogni evento risolto
        for (int eventId : eventIds) {
            box.getChildren().add(buildEventSection(eventId, current, previous));
        }

        // Hint chiusura
        Label hint = new Label("(click per chiudere)");
        hint.setStyle("-fx-text-fill: rgba(200,168,76,0.5); -fx-font-size: 10;");
        box.getChildren().add(hint);

        getChildren().add(box);

        // Click per chiudere anticipatamente
        setOnMouseClicked(e -> hide());

        // Mostra con fade in
        setVisible(true);
        setPickOnBounds(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void hide() {
        FadeTransition out = new FadeTransition(Duration.millis(350), this);
        out.setFromValue(getOpacity());
        out.setToValue(0);
        out.setOnFinished(e -> {
            setVisible(false);
            setPickOnBounds(false);
        });
        out.play();
    }

    private VBox buildEventSection(int eventId, GameState current, GameState previous) {
        VBox section = new VBox(6);
        section.setPadding(new Insets(8, 12, 8, 12));
        section.setStyle(
                "-fx-background-color: rgba(60,35,10,0.70);" +
                        "-fx-background-radius: 8;"
        );

        // Nome evento
        String eventName = CardDictionary.getCardName(eventId);
        String eventDesc = CardDictionary.getCardDetail(eventId);

        Label nameLabel = new Label("✦  " + eventName);
        nameLabel.setStyle(
                "-fx-text-fill: #f1c40f;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;"
        );

        Label descLabel = new Label(eventDesc);
        descLabel.setStyle(
                "-fx-text-fill: rgba(220,200,160,0.85);" +
                        "-fx-font-size: 11;" +
                        "-fx-font-style: italic;"
        );
        descLabel.setWrapText(true);

        section.getChildren().addAll(nameLabel, descLabel);

        // Delta PP e cibo per ogni giocatore
        if (previous != null) {
            for (PlayerState cur : current.getPlayerStates()) {
                PlayerState prev = previous.getPlayerStateByNickname(cur.getNickname());
                if (prev == null) continue;

                int deltaPP   = cur.getPP()   - prev.getPP();
                int deltaFood = cur.getFood()  - prev.getFood();

                if (deltaPP == 0 && deltaFood == 0) continue;

                HBox playerRow = buildPlayerDeltaRow(cur.getNickname(), deltaPP, deltaFood);
                section.getChildren().add(playerRow);
            }
        }

        return section;
    }

    private HBox buildPlayerDeltaRow(String nickname, int deltaPP, int deltaFood) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 4, 2, 4));

        Label nameLabel = new Label(nickname);
        nameLabel.setMinWidth(90);
        nameLabel.setStyle(
                "-fx-text-fill: #ecf0f1;" +
                        "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;"
        );

        if (deltaPP != 0) {
            Label ppLabel = new Label(formatDelta(deltaPP) + " PP");
            ppLabel.setStyle(
                    "-fx-text-fill: " + (deltaPP > 0 ? "#2ecc71" : "#e74c3c") + ";" +
                            "-fx-font-size: 11;" +
                            "-fx-font-weight: bold;"
            );
            row.getChildren().add(ppLabel);
        }

        if (deltaFood != 0) {
            Label foodLabel = new Label(formatDelta(deltaFood) + " Cibo");
            foodLabel.setStyle(
                    "-fx-text-fill: " + (deltaFood > 0 ? "#f39c12" : "#e74c3c") + ";" +
                            "-fx-font-size: 11;" +
                            "-fx-font-weight: bold;"
            );
            row.getChildren().add(foodLabel);
        }

        row.getChildren().add(0, nameLabel);
        return row;
    }

    private String formatDelta(int delta) {
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }
}
