package it.polimi.ingsw.am46.view.gui.scenes;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.gui.utils.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.Comparator;
import java.util.List;

public class EndGamePane extends StackPane {

    private final SceneManager sceneManager;
    private final Label winnerLabel;
    private final VBox scoresBox;

    // Animazioni come campi — così update() può chiamare play()
    private final FadeTransition titleFade;
    private final ScaleTransition winnerScale;

    public EndGamePane(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #000000);");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label title = new Label("Fine partita");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28; -fx-font-weight: bold;");

        // Animazione definita ma NON avviata qui
        titleFade = new FadeTransition(Duration.millis(800), title);
        titleFade.setFromValue(0.0);
        titleFade.setToValue(1.0);

        winnerLabel = new Label("");
        winnerLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 22; -fx-font-weight: bold;");

        // Animazione definita ma NON avviata qui
        winnerScale = new ScaleTransition(Duration.millis(500), winnerLabel);
        winnerScale.setFromX(0.5);
        winnerScale.setFromY(0.5);
        winnerScale.setToX(1.0);
        winnerScale.setToY(1.0);
        winnerScale.setDelay(Duration.millis(600));

        scoresBox = new VBox(6);
        scoresBox.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("Chiudi Partita");
        backButton.setStyle(
                "-fx-background-color: #6C63FF; -fx-text-fill: white;" +
                        "-fx-font-size: 14; -fx-padding: 10 30;"
        );
        backButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        content.getChildren().addAll(title, winnerLabel, scoresBox, backButton);
        getChildren().add(content);
    }

    public void update(GameState state) {
        List<PlayerState> leaderboard = state.getPlayerStates()
                .stream()
                .sorted(Comparator.comparingInt(PlayerState::getPP).reversed())
                .toList();

        String winnerName = leaderboard.isEmpty()
                ? "Nessuno"
                : leaderboard.get(0).getNickname();
        winnerLabel.setText(winnerName + " ha vinto!");

        scoresBox.getChildren().clear();
        for (int i = 0; i < leaderboard.size(); i++) {
            PlayerState ps = leaderboard.get(i);
            Label row = new Label((i + 1) + ". " + ps.getNickname() + " — " + ps.getPP() + " PP");
            row.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
            scoresBox.getChildren().add(row);
        }

        // Animazioni avviate qui — quando la scena sta per essere mostrata
        titleFade.play();
        winnerScale.play();
    }
}
