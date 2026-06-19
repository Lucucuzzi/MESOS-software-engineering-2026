package it.polimi.ingsw.am46.view.gui.utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;


/*
 * Manages transitions between different GUI screens.
 * Scenes are registered once at startup (in GUIView.start())
 * and reused on each transition — no reallocation during gameplay.
 */

/**
 * The type Scene manager.
 */
public class SceneManager {
    /**
     * The enum Scene name.
     */
    public enum SceneName {
        /**
         * Lobby scene name.
         */
        LOBBY,
        /**
         * Game scene name.
         */
        GAME,
        /**
         * Endgame scene name.
         */
        ENDGAME
    }
    private final Stage stage;
    private SceneName currentScene = null;
    private final Map<SceneName, Scene> scenes = new HashMap<>();

    /**
     * Instantiates a new Scene manager.
     *
     * @param stage the stage
     */
    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    /**
     * Register.
     *
     * @param name  the name
     * @param scene the scene
     */
    /*
     * Registers a pre-built scene.
     * Called once per scene in GUIView.start().
     */
    public void register(SceneName name, Scene scene) {
        scenes.put(name, scene);
    }

    /**
     * Switch to.
     *
     * @param name the name
     */
    /*
     * Switches the visible screen.
     * Can be called from any thread.
     */
    public void switchTo(SceneName name) {
        if (name == currentScene) return; // no change, do nothing
        currentScene = name;
        Scene scene = scenes.get(name);
        if (scene == null)
            throw new IllegalStateException("Scene not registered: " + name);
        //check which thread we are on. If already on the JavaFX thread
        //execute directly, otherwise queue the operation on the correct thread
        if (Platform.isFxApplicationThread())
            stage.setScene(scene);
        else
            Platform.runLater(() -> stage.setScene(scene));
    }


}
