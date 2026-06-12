package it.polimi.ingsw.am46.server.model;

import com.google.gson.Gson;
import it.polimi.ingsw.am46.network.dto.CardDataDTO;
import it.polimi.ingsw.am46.network.dto.CardDataDTO.BuildingDTO;
import it.polimi.ingsw.am46.network.dto.CardDataDTO.CharacterDTO;
import it.polimi.ingsw.am46.network.dto.CardDataDTO.EventDTO;
import it.polimi.ingsw.am46.server.model.cards.TribeCard;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingCard;
import it.polimi.ingsw.am46.server.model.cards.TribeCardFactory;
import it.polimi.ingsw.am46.server.model.cards.buildingCards.BuildingFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * The type Deck loader.
 */
public class DeckLoader {

    private final CardDataDTO cardData;


    /**
     * Instantiates a new Deck loader.
     */
    public DeckLoader() {
        String jsonString = readFromResources("cards.json");
        this.cardData = new Gson().fromJson(jsonString, CardDataDTO.class);
    }


    /**
     * Load building deck deck.
     *
     * @param numPlayers the num players
     * @param targetEra  the target era
     * @return the deck
     */
    // Builds the Building Deck for a specific Era and number of players
    // Filter the cards based on the number of players and limit the quantity.
    public Deck<BuildingCard> loadBuildingDeck(int numPlayers, int targetEra) {
        List<BuildingCard> eraBuildings = new ArrayList<>();
        for (BuildingDTO dto : cardData.buildings) {
            if (dto.era == targetEra) {
                eraBuildings.add(BuildingFactory.createBuilding(dto));
            }
        }
        // shuffles among cards of the same era
        Collections.shuffle(eraBuildings);
        // take only the needed number of buildings
        Deck<BuildingCard> deck = new Deck<>();
        int buildingCount = BoardRules.getBuildingsPerEra(numPlayers, targetEra);
        for (int i = 0; i < buildingCount && i < eraBuildings.size(); i++) {
            deck.addCardToBottom(eraBuildings.get(i));
        }
        return deck;
    }

    /**
     * Load tribe deck deck.
     *
     * @param numPlayers the num players
     * @return the deck
     */
    // Builds tribe deck
    public Deck<TribeCard> loadTribeDeck(int numPlayers) {
        List<TribeCard> era1 = new ArrayList<>();
        List<TribeCard> era2 = new ArrayList<>();
        List<TribeCard> era3 = new ArrayList<>();
        List<TribeCard> finalCards = new ArrayList<>();

        // characters
        if (cardData.characters != null) {
            for (CharacterDTO dto : cardData.characters) {
                if (numPlayers >= dto.minPlayers) {
                    TribeCard card = TribeCardFactory.createCharacter(dto);
                    addToEra(card, dto.era, era1, era2, era3);
                }
            }
        }

        // events
        if (cardData.events != null) {
            for (EventDTO dto : cardData.events) {
                TribeCard card = TribeCardFactory.createEvent(dto);
                if (dto.finalEvent) {
                    finalCards.add(card);
                } else {
                    addToEra(card, dto.era, era1, era2, era3);
                }
            }
        }

        Collections.shuffle(era1);
        Collections.shuffle(era2);
        Collections.shuffle(era3);

        Deck<TribeCard> finalDeck = new Deck<>();
        finalDeck.addAll(era1);
        finalDeck.addAll(era2);
        finalDeck.addAll(era3);
        finalDeck.addAll(finalCards); // final events in the bottom

        return finalDeck;
    }

    // Internal utility method for sorting cards
    // can be removed using a map (but it is a private method anyway)

    private void addToEra(TribeCard card, int era, List<TribeCard> e1, List<TribeCard> e2, List<TribeCard> e3) {
        if (era == 1) e1.add(card);
        else if (era == 2) e2.add(card);
        else if (era == 3) e3.add(card);
    }

    // Safe reading of the JSON file (both from IDE and from JAR) using an InputStream
    private String readFromResources(String filePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        if (is == null) throw new IllegalArgumentException("File doesn't find: " + filePath);
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

}