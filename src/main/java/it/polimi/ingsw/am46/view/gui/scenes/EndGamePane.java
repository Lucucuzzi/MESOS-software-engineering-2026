package it.polimi.ingsw.am46.view.gui.scenes;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.network.dto.PlayerState;
import it.polimi.ingsw.am46.view.gui.utils.ImageCache;
import it.polimi.ingsw.am46.view.gui.utils.SceneManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import it.polimi.ingsw.am46.network.dto.LeaderboardEntry;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ScrollPane;

public class EndGamePane extends StackPane {

    private final SceneManager sceneManager;
    private final Label winnerLabel = new Label();
    private final VBox scoresBox = new VBox(8);
    private FadeTransition titleFade;
    private ScaleTransition winnerScale;

    // Fuochi d'artificio
    private final Canvas fireworkCanvas = new Canvas(1280, 800);
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();
    private AnimationTimer fireworkTimer;

    private String myNickname = "";
    public void setMyNickname(String myNickname) { this.myNickname = myNickname; }

    public EndGamePane(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        // LAYER 1 — sfondo
        ImageView bgView = new ImageView();
        bgView.setImage(ImageCache.getFull("/images/backgrounds/victory_bg.png"));
        bgView.setPreserveRatio(false);
        bgView.fitWidthProperty().bind(widthProperty());
        bgView.fitHeightProperty().bind(heightProperty());
        getChildren().add(bgView);

        // LAYER 2 — fuochi d'artificio
        fireworkCanvas.setMouseTransparent(true);
        fireworkCanvas.widthProperty().bind(widthProperty());
        fireworkCanvas.heightProperty().bind(heightProperty());
        getChildren().add(fireworkCanvas);

        // LAYER 3 — overlay gradiente per leggibilità
        Region overlay = new Region();
        overlay.setStyle(
                "-fx-background-color: linear-gradient(" +
                        "to bottom, rgba(0,0,0,0.0) 0%, " +
                        "rgba(0,0,0,0.4) 40%, " +
                        "rgba(0,0,0,0.75) 100%);"
        );
        getChildren().add(overlay);

        // LAYER 4 — contenuto UI
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: transparent;");
        layout.setPickOnBounds(false);

        // ── TOP — vincitore ──
        VBox topSection = new VBox(6);
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(60, 0, 0, 0));

        Label crownIcon = new Label("👑");
        crownIcon.setStyle("-fx-font-size: 36;");

        winnerLabel.setStyle(
                "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 34;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 14, 0.85, 0, 2);"
        );

        Label winnerSub = new Label("ha vinto la partita!");
        winnerSub.setStyle(
                "-fx-text-fill: #c9a84c;" +
                        "-fx-font-size: 16;" +
                        "-fx-font-style: italic;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 10, 0.9, 0, 1);"
        );

        topSection.getChildren().addAll(crownIcon, winnerLabel, winnerSub);
        layout.setTop(topSection);

        // Animazioni — definite qui, avviate in update()
        titleFade = new FadeTransition(Duration.millis(700), topSection);
        titleFade.setFromValue(0);
        titleFade.setToValue(1.0);

        winnerScale = new ScaleTransition(Duration.millis(550), winnerLabel);
        winnerScale.setFromX(0.3);
        winnerScale.setFromY(0.3);
        winnerScale.setToX(1.0);
        winnerScale.setToY(1.0);
        winnerScale.setDelay(Duration.millis(350));
        winnerScale.setInterpolator(Interpolator.EASE_OUT);

        // ── BOTTOM — classifica ──
        VBox bottomSection = new VBox(14);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setPadding(new Insets(0, 80, 40, 80));

        Label rankTitle = new Label("Classifica Finale");
        rankTitle.setStyle(
                "-fx-text-fill: #c9a84c;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;"
        );

        scoresBox.setAlignment(Pos.CENTER);
        scoresBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.50);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16 36;"
        );
        scoresBox.setMaxWidth(500);

        Button closeBtn = new Button("Chiudi Partita");
        closeBtn.setStyle(
                "-fx-background-color: #c9a84c;" +
                        "-fx-text-fill: #1a0e05;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 44;" +
                        "-fx-background-radius: 25;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 8, 0.4, 0, 2);"
        );
        closeBtn.setOnMouseEntered(e ->
                closeBtn.setStyle(closeBtn.getStyle().replace("#c9a84c", "#f1c40f")));
        closeBtn.setOnMouseExited(e ->
                closeBtn.setStyle(closeBtn.getStyle().replace("#f1c40f", "#c9a84c")));
        closeBtn.setOnAction(e -> {
            stopFireworks();
            Platform.exit();
            System.exit(0);
        });

        bottomSection.getChildren().addAll(rankTitle, scoresBox, closeBtn);
        layout.setBottom(bottomSection);

        getChildren().add(layout);
    }

    public void update(GameState state) {
        if (state.getPlayerStates() == null) return;

        List<PlayerState> leaderboard = state.getPlayerStates().stream()
                .filter(ps -> !ps.isDisconnected())
                .sorted((a, b) -> Integer.compare(b.getPP(), a.getPP()))
                .toList();

        String winner = leaderboard.isEmpty()
                ? "Nessuno"
                : leaderboard.get(0).getNickname();
        winnerLabel.setText(winner);

        scoresBox.getChildren().clear();
        String[] medals = {"🥇", "🥈", "🥉", "4.", "5."};
        String[] rowColors = {"#f1c40f", "#bdc3c7", "#cd7f32", "#ecf0f1", "#ecf0f1"};
        for (int i = 0; i < leaderboard.size(); i++) {
            PlayerState ps = leaderboard.get(i);
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(460);

            int safeIndex = Math.min(i, rowColors.length - 1);
            String currentMedal = medals[Math.min(i, medals.length - 1)];
            String currentColor = rowColors[safeIndex];

            int fontSizeTitle = (i == 0) ? 22 : 16;
            int fontSizeName = (i == 0) ? 18 : 15;
            String fontWeightName = (i == 0) ? "bold" : "normal";

            Label medalLbl = new Label(currentMedal);
            medalLbl.setStyle("-fx-font-size: " + fontSizeTitle + ";");
            medalLbl.setMinWidth(36);

            Label nameLbl = new Label(ps.getNickname());
            nameLbl.setStyle(
                    "-fx-text-fill: " + currentColor + ";" +
                            "-fx-font-size: " + fontSizeName + ";" +
                            "-fx-font-weight: " + fontWeightName + ";"
            );
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            Label scoreLbl = new Label(ps.getPP() + " PP");
            scoreLbl.setStyle(
                    "-fx-text-fill: " + currentColor + ";" +
                            "-fx-font-size: " + fontSizeName + ";" +
                            "-fx-font-weight: bold;"
            );

            row.getChildren().addAll(medalLbl, nameLbl, scoreLbl);

            // Slide-in dal basso con ritardo progressivo
            row.setOpacity(0);
            row.setTranslateY(18);
            final int idx = i;
            PauseTransition delay = new PauseTransition(
                    Duration.millis(600 + idx * 150L));
            delay.setOnFinished(e -> {
                FadeTransition ft = new FadeTransition(Duration.millis(280), row);
                ft.setToValue(1.0);
                TranslateTransition tt = new TranslateTransition(Duration.millis(280), row);
                tt.setToY(0);
                tt.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft, tt).play();
            });
            delay.play();

            scoresBox.getChildren().add(row);
        }

        titleFade.play();
        winnerScale.play();
        startFireworks();
        if (state.getFinalLeaderboard() != null && !state.getFinalLeaderboard().isEmpty()) {
            int myRank = -1;
            List<LeaderboardEntry> globalBoard = state.getFinalLeaderboard();
            for (int i = 0; i < globalBoard.size(); i++) {
                if (globalBoard.get(i).getNickname().equals(this.myNickname)) {
                    myRank = i + 1;
                    break;
                }
            }
            scheduleLeaderboardDisplay(globalBoard, myRank, state.getNumPlayersInGame());
        }

    }

    // ── FUOCHI D'ARTIFICIO ──

    private static class Particle {
        double x, y, vx, vy, alpha, size;
        Color color;

        Particle(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            double angle = Math.random() * 2 * Math.PI;
            double speed = 1.0 + Math.random() * 4.5;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed;
            this.alpha = 1.0;
            this.size = 3.0 + Math.random() * 4.0;
            this.color = color;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.07;
            vx *= 0.98;
            alpha -= 0.018;
            size *= 0.97;
        }

        boolean isDead() { return alpha <= 0 || size < 0.5; }
    }

    private void startFireworks() {
        if (fireworkTimer != null) fireworkTimer.stop();
        particles.clear();
        fireworkTimer = new AnimationTimer() {
            private long lastBurst = 0;

            @Override
            public void handle(long now) {
                if (now - lastBurst > 650_000_000L) {
                    launchBurst();
                    lastBurst = now;
                }
                GraphicsContext gc = fireworkCanvas.getGraphicsContext2D();
                double cw = fireworkCanvas.getWidth();
                double ch = fireworkCanvas.getHeight();
                gc.clearRect(0, 0, cw, ch);
                gc.setFill(Color.color(0, 0, 0, 0.12));
                gc.fillRect(0, 0, cw, ch);

                particles.removeIf(Particle::isDead);
                for (Particle p : particles) {
                    p.update();
                    gc.setFill(Color.color(
                            p.color.getRed(),
                            p.color.getGreen(),
                            p.color.getBlue(),
                            Math.max(0, p.alpha)
                    ));
                    gc.fillOval(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
                }
            }
        };
        fireworkTimer.start();
    }

    private void launchBurst() {
        double cw = fireworkCanvas.getWidth() > 0 ? fireworkCanvas.getWidth() : 1280;
        double ch = fireworkCanvas.getHeight() > 0 ? fireworkCanvas.getHeight() : 800;
        double x = 80 + rng.nextDouble() * (cw - 160);
        double y = 50 + rng.nextDouble() * (ch * 0.45);
        Color[] palette = {
                Color.web("#f1c40f"), Color.web("#e74c3c"),
                Color.web("#3498db"), Color.web("#2ecc71"),
                Color.web("#e67e22"), Color.web("#9b59b6")
        };
        Color color = palette[rng.nextInt(palette.length)];
        for (int i = 0; i < 80; i++)
            particles.add(new Particle(x, y, color));
        for (int i = 0; i < 20; i++)
            particles.add(new Particle(x, y, Color.WHITE));
    }

    private void stopFireworks() {
        if (fireworkTimer != null) {
            fireworkTimer.stop();
            fireworkTimer = null;
        }
        particles.clear();
        fireworkCanvas.getGraphicsContext2D()
                .clearRect(0, 0, fireworkCanvas.getWidth(), fireworkCanvas.getHeight());
    }

    private void scheduleLeaderboardDisplay(List<LeaderboardEntry> leaderboard, int playerRank, int numPlayers) {
        PauseTransition delay = new PauseTransition(Duration.millis(2500));
        delay.setOnFinished(e -> displayLeaderboardTable(leaderboard, playerRank, numPlayers));
        delay.play();
    }

    private void displayLeaderboardTable(List<LeaderboardEntry> leaderboard, int playerRank, int numPlayers) {
        VBox leaderboardSection = new VBox(12);
        leaderboardSection.setStyle(
                "-fx-background-color: rgba(0,0,0,0.70);" +
                        "-fx-border-color: #c9a84c;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 16;"
        );
        leaderboardSection.setMaxWidth(700);

        Label leaderboardTitle = new Label("📊 CLASSIFICA GLOBALE (" + numPlayers + " GIOCATORI)");
        leaderboardTitle.setStyle(
                "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c9a84c;"
        );
        leaderboardSection.getChildren().add(leaderboardTitle);

        TableView<LeaderboardEntry> tableView = new TableView<>();
        tableView.setStyle("-fx-font-size: 12; -fx-control-inner-background: #1a0e05; -fx-text-fill: #ecf0f1;");
        tableView.setPrefHeight(250);

        TableColumn<LeaderboardEntry, String> posCol = new TableColumn<>("Pos");
        posCol.setPrefWidth(50);
        posCol.setCellValueFactory(cellData -> {
            int index = tableView.getItems().indexOf(cellData.getValue());
            String medal = getMedalForRank(index + 1);
            return new SimpleStringProperty(medal + " " + (index + 1));
        });

        TableColumn<LeaderboardEntry, String> nickCol = new TableColumn<>("Nickname");
        nickCol.setPrefWidth(200);
        nickCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNickname()));

        TableColumn<LeaderboardEntry, Integer> winsCol = new TableColumn<>("Vittorie");
        winsCol.setPrefWidth(100);
        winsCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalWins()));

        tableView.getColumns().addAll(posCol, nickCol, winsCol);
        tableView.getItems().addAll(leaderboard);

        leaderboardSection.getChildren().add(tableView);

        if (playerRank > 0) {
            Label rankLabel = new Label("Your position: #" + playerRank); // Messo in inglese per coerenza
            rankLabel.setStyle(
                    "-fx-font-size: 13;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #f1c40f;" +
                            "-fx-padding: 8;"
            );
            leaderboardSection.getChildren().add(rankLabel);
        } else {
            Label rankLabel = new Label("Not present in the leaderboard because you have 0 victories");
            rankLabel.setStyle(
                    "-fx-font-size: 13;" +
                            "-fx-font-style: italic;" +
                            "-fx-text-fill: #bdc3c7;" +
                            "-fx-padding: 8;"
            );
            leaderboardSection.getChildren().add(rankLabel);
        }

        ScrollPane scrollPane = new ScrollPane(leaderboardSection);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        VBox newBottom = new VBox(14);
        newBottom.setAlignment(Pos.CENTER);
        newBottom.setPadding(new Insets(20, 40, 40, 40));

        Label localTitle = new Label("Punteggi di questa Partita");
        localTitle.setStyle(
                "-fx-text-fill: #c9a84c;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;"
        );
        newBottom.getChildren().addAll(localTitle, scoresBox, scrollPane);

        Button closeBtn = new Button("Chiudi Partita");
        closeBtn.setStyle(
                "-fx-background-color: #c9a84c;" +
                        "-fx-text-fill: #1a0e05;" +
                        "-fx-font-size: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 44;" +
                        "-fx-background-radius: 25;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #000000, 8, 0.4, 0, 2);"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtn.getStyle().replace("#c9a84c", "#f1c40f")));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(closeBtn.getStyle().replace("#f1c40f", "#c9a84c")));
        closeBtn.setOnAction(e -> {
            stopFireworks();
            Platform.exit();
            System.exit(0);
        });
        newBottom.getChildren().add(closeBtn);

        BorderPane layout = (BorderPane) getChildren().get(3);
        layout.setBottom(newBottom);
    }

    private String getMedalForRank(int rank) {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "  ";
        }
    }



}