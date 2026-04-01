package it.polimi.ingsw.am46.model.state;

import it.polimi.ingsw.am46.model.GameContext;
import it.polimi.ingsw.am46.model.Player;
import it.polimi.ingsw.am46.model.TriggerType;
import it.polimi.ingsw.am46.model.cards.Card;
import it.polimi.ingsw.am46.model.cards.enums.SubType;
import it.polimi.ingsw.am46.model.cards.enums.Type;
import it.polimi.ingsw.am46.model.cards.eventCards.EventCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResolveEventState extends RoundPhase{
    public ResolveEventState(){
        super(TriggerType.ONEVENT);
    }
    @Override
    public void startPhase(GameContext ctx) {
        handleResolveEvent(ctx);
    }

    @Override
    public void handleResolveEvent(GameContext ctx) {
        if (ctx.getRound() == 10 && ctx.getCurrentEra() == 3) {
            // Executes resolveAllEvents()
            resolveAllEvents(ctx);
        } else {
            // Executes the standard resolveEvents() for rounds 1 through 9
            resolveEvents(ctx);
        }
        nextPhase(ctx);

    }

    private void resolveEvents(GameContext ctx) {
        List<EventCard> orderedEvents = new ArrayList<>();
        List<EventCard> sustenanceEvents = new ArrayList<>();

        for (Card card : ctx.getBoard().getBottomRow()) {
            if (card.getType() != Type.EVENT) {
                continue;
            }
            EventCard event = (EventCard) card;
            if (event.getSubType() == SubType.SUSTENANCE) {
                sustenanceEvents.add(event);
            } else {
                orderedEvents.add(event);
            }
        }
        orderedEvents.sort(Comparator.comparingInt(Card::getEra)); // events must be resolved ordered by era
        orderedEvents.addAll(sustenanceEvents);
        if (orderedEvents.isEmpty()) {
            return;
        }

        processEvents(ctx, orderedEvents);
    }

    private void resolveAllEvents(GameContext ctx) {
        if (ctx.getCurrentEra() != 3 || ctx.getRound() != 10) {
            throw new IllegalStateException("All events can be resolved only at the end of Era III.");
        }

        List<EventCard> orderedEvents = new ArrayList<>();
        List<EventCard> sustenanceEvents = new ArrayList<>();

        // Collect events from the bottom row
        for (Card card : ctx.getBoard().getBottomRow()) {
            if (card.getType() != Type.EVENT) {
                continue;
            }
            EventCard event = (EventCard) card;
            if (event.getSubType() == SubType.SUSTENANCE) {
                sustenanceEvents.add(event);
            } else {
                orderedEvents.add(event);
            }
        }

        // Collect events from the top row
        for (Card card : ctx.getBoard().getTopRow()) {
            if (card.getType() != Type.EVENT) {
                continue;
            }
            EventCard event = (EventCard) card;
            if (event.getSubType() == SubType.SUSTENANCE) {
                sustenanceEvents.add(event);
            } else {
                orderedEvents.add(event);
            }
        }
        orderedEvents.sort(Comparator.comparingInt(Card::getEra)); // events must be resolved ordered by era
        orderedEvents.addAll(sustenanceEvents);
        if (orderedEvents.isEmpty()) {
            return;
        }

        processEvents(ctx, orderedEvents);
    }

    private void processEvents(GameContext ctx, List<EventCard> orderedEvents) {
        Player previousActivePlayer = ctx.getActivePlayer();


        for (EventCard event : orderedEvents) {
            ctx.setCurrentEvent(event);
            for (Player player : ctx.getPlayers()) {
                ctx.setActivePlayer(player);
                triggerBuildingEffects(ctx, player, getTriggerType());
            }
            event.resolve(ctx);
            ctx.getBoard().removeFromBoard(event);
        }

        // Restore the previous state
        ctx.setActivePlayer(previousActivePlayer);

    }

    @Override
    public void nextPhase(GameContext ctx) {
        RoundPhase next = new EndRoundState();
        ctx.setCurrentPhase(next);
        next.startPhase(ctx);
    }
}
