package it.polimi.ingsw.am46.view;

import it.polimi.ingsw.am46.view.cli.CLIView;
import it.polimi.ingsw.am46.view.gui.GUIView;

import java.util.concurrent.CountDownLatch;

/**
 * Factory to create the appropriate view.
 *
 * ClientLauncher does not depend on concrete classes (CLIView, GUIView, etc.)
 * — it only depends on this factory and the GameView interface.
 *
 * Pattern: Factory Pattern (GoF)
 */
public class ViewFactory {

    public enum ViewType {
        CLI,
        GUI
    }

    /**
     * Creates the appropriate view.
     * @param latch         the CountDownLatch that unlocks main
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
