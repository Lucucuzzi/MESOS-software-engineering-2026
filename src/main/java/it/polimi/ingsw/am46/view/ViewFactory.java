package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.view.cli.CLIView;
import it.polimi.ingsw.am46.view.gui.GUIView;

import java.util.concurrent.CountDownLatch;

/**
 * Factory to create the appropriate view.
 * <p>
 * ClientLauncher does not depend on concrete classes (CLIView, GUIView, etc.)
 * — it only depends on this factory and the GameView interface.
 * <p>
 * Pattern: Factory Pattern (GoF)
 */
public class ViewFactory {

    /**
     * The enum View type.
     */
    public enum ViewType {
        /**
         * Cli view type.
         */
        CLI,
        /**
         * Gui view type.
         */
        GUI
    }

    /**
     * Creates the appropriate view.
     *
     * @param type       the type
     * @param localModel the local model
     * @param controller the controller
     * @param latch      the CountDownLatch that unlocks main
     * @return the game view
     */
    public static GameView create(ViewType type, LocalModel localModel, ClientController controller, CountDownLatch latch) {
        return switch (type) {
            case CLI -> new CLIView(localModel, controller, latch);
            case GUI -> {
                GUIView.setModel(localModel);
                GUIView.setController(controller);
                GUIView.setLatch(latch);
                yield new GUIView();
            }
        };
    }
}
