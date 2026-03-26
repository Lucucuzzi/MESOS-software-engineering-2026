package it.polimi.ingsw.am46.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TurnTile {
    private List<Space> spaces;

    // al costruttore gli passi la configurazione degli spaces specifici creata con JSON
    public TurnTile(List<Space> initialSpaces) {
        this.spaces = new ArrayList<>(initialSpaces);
    }


    // aggiunge il totem (Player) in una determinata posizione
    public void pushTotem(int position, Player player) {
        for (Space s : spaces) {
            if (s.getPos() == position) {
                s.setPlayer(player);
                break;
            }
        }
    }

    // prende il totem dalla posizione e restituisce il Player
    public Player takeTotem(int position) {
        for (Space s : spaces) {
            if (s.getPos() == position) {
                Player p = s.getPlayer();
                s.setPlayer(null);
                return p;
            }
        }
        return null;
    }

    // applica l'effetto dello spazio al giocatore che lo occupa
    public void applyTTEffect(Space space) {
        Player p = space.getPlayer();
        if (p != null) {
            p.modifyFood(space.getFood());
            p.modifyPP(space.getPP());
        }
    }

    // restituisce la lista di giocatori nell'ordine di turno corretto
    public List<Player> getTurnOrder() {
        return spaces.stream()
                .map(Space::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    // passi la lista di tutti i giocatori e lui te li piazza casualmente
    public void randomlyPlaceTotems(List<Player> players) {
        List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);
        for (int i = 0; i < shuffledPlayers.size(); i++) {
            Player p = shuffledPlayers.get(i);
            this.pushTotem(i + 1, p);
        }
    }
    // ritorna lo spazio sul quale 'Player' è sopra
    public Space getSpaceOfPlayer(Player player) {
        for (Space s : spaces) {
            if (s.getPlayer() == player) {
                return s;
            }
        }
        return null;
    }

}
