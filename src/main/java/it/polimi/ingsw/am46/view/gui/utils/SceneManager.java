package it.polimi.ingsw.am46.view.gui.utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;


/*
 * Gestisce le transizioni tra le diverse schermate della GUI.
 * Le scene vengono registrate una sola volta all'avvio (in GUIView.start())
 * e riutilizzate ad ogni transizione — nessuna riallocazione durante il gioco.
 */

public class SceneManager {
    public enum SceneName {
        LOBBY, GAME, ENDGAME
    }
    private final Stage stage;
    private SceneName currentScene = null;
    private final Map<SceneName, Scene> scenes = new HashMap<>();
    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    /*
     * Registra una scena pre-costruita.
     * Chiamato una sola volta per ogni scena in GUIView.start().
     */
    public void register(SceneName name, Scene scene) {
        scenes.put(name, scene);
    }

    /*
     * Cambia la schermata visibile.
     * Può essere chiamato da qualsiasi thread.
     */
    public void switchTo(SceneName name) {
        if (name == currentScene) return; // nessun cambio, non fare nulla
        currentScene = name;
        Scene scene = scenes.get(name);
        if (scene == null)
            throw new IllegalStateException("Scena non registrata: " + name);
        //controlla su quale thread siamo in quel momento. Se siamo già sul JavaFX thread
        //esegue direttamente, altrimenti mette l'operazione in coda per essere eseguita sul thread
        //giusto
        if (Platform.isFxApplicationThread())
            stage.setScene(scene);
        else
            Platform.runLater(() -> stage.setScene(scene));
    }


}
