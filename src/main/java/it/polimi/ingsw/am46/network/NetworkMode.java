package it.polimi.ingsw.am46.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface Network mode.
 */
public interface NetworkMode extends Remote {
    /**
     * Is socket boolean.
     *
     * @return the boolean
     * @throws RemoteException the remote exception
     */
    boolean isSocket() throws RemoteException;
    // to dispatch if a cur is connected with socket or RMI
    // we use it to avoid type checking (instanceOf ...)
}
