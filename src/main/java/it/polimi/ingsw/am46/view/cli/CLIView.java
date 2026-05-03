package it.polimi.ingsw.am46.view.cli;

import it.polimi.ingsw.am46.network.dto.GameState;
import it.polimi.ingsw.am46.view.ClientController;
import it.polimi.ingsw.am46.view.GameView;
import it.polimi.ingsw.am46.view.LocalModel;
import java.util.concurrent.CountDownLatch;

public class CLIView implements GameView {

    private final LocalModel localModel;
    private final ClientController controller;
    private final CountDownLatch latch;  // Unlocks main when CLI closes
    public CLIView(LocalModel localModel, ClientController controller, CountDownLatch latch) {
        this.localModel = localModel;
        this.controller = controller;
        this.latch = latch;}

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void showMessage(String message) {

    }

    @Override
    public void drawBoard(GameState state) {

    }

    @Override
    public void onStateUpdate(GameState newState) {

    }

    @Override
    public void onError(String errorMessage) {

    }
}
