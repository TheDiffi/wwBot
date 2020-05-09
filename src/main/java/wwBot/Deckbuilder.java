package wwBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Deckbuilder {

    public static List<Card> create(int playerAmount) throws Exception {

        // Create a deep copy of available cards

        var availableCardsDeckbuilder = ReadJSONCard.readAvailableCards();

        var totalValue = 0;
        var listDeck = new ArrayList<Card>();

        if (playerAmount < 5 || playerAmount > 35) {
            return null;
        }

        // add Seher
        int numbSeher = playerAmount > 18 ? 2 : 1;
        addMultiple(numbSeher, availableCardsDeckbuilder.get("Seher"), listDeck);
        availableCardsDeckbuilder.get("Seher").unique = false;

        // add Werwölfe
        int numbWerwölfe = (int) Math.pow(((playerAmount - 2) / 3d), 0.85);
        addMultiple(numbWerwölfe, availableCardsDeckbuilder.get("Werwolf"), listDeck);

        // add Dorfbewohner
        int numbDorfbewohner = (int) Math.pow(playerAmount * 1.3, 0.65);
        addMultiple(numbDorfbewohner, availableCardsDeckbuilder.get("Dorfbewohner"), listDeck);

        // add Spezialkarten
        int numbSpezialkarten = playerAmount - (numbDorfbewohner + numbWerwölfe + numbSeher);
        int numbAdjustCards = numbSpezialkarten != 0 ? (int) (numbSpezialkarten / 5) + 1 : 0;

        // add random specialcards außer so viele wie "Ajustierende Karten" sind und
        // setzt den unique Wert der hinzugefügten Karte auf false

        for (int i = 0; i < (numbSpezialkarten - numbAdjustCards); i++) {
            var randomCard = getRandomSpezialkarte(numbSpezialkarten, availableCardsDeckbuilder);
            listDeck.add(randomCard);
            availableCardsDeckbuilder.get(randomCard.name).unique = false;
        }

        // add last special-card, with considering card value of all cards

        // wird so oft ausgeführt wie "Ajustierende Karten" sind
        for (int i = 0; i < numbAdjustCards; i++) {
            // add last special-card, with considering card value of all cards
            totalValue = Globals.totalCardValue(listDeck);
            Card smallestDifferenceCard = null;
            var tempCardList = new ArrayList<Card>();

            // kalkuliert die differenz jeder Karte. Falls die differenz der Karte kleiner
            // ist als
            // die differenz der bisherigen wird sie in smallestDifferenceCard gespeichert.

            for (var card : availableCardsDeckbuilder.values()) {
                if (card.unique) {
                    if (smallestDifferenceCard == null) {
                        smallestDifferenceCard = card;
                    } else if (Math.abs(totalValue + card.value) < Math
                            .abs(totalValue + smallestDifferenceCard.value)) {
                        smallestDifferenceCard = card;
                    }
                }
            }
            // sucht alle Karten, welche denselben Value wie die smallestDifferenceCard
            // haben und speichert diese in einer liste
            for (var card : availableCardsDeckbuilder.values()) {
                if (card.unique && smallestDifferenceCard.value == card.value) {
                    tempCardList.add(card);
                }

            }
            

            // aus der liste wird zufällig ein element ausgewählt und dem Deck hinzugefügt
            var rand = (int) (Math.random() * (tempCardList.size()));
            listDeck.add(tempCardList.get(rand));
            availableCardsDeckbuilder.get(tempCardList.get(rand).name).unique = false;
        }

        totalValue = Globals.totalCardValue(listDeck);

        return listDeck;
    }

    
    public static void addMultiple(final int amount, final Card card, final List<Card> list) {
        for (int i = 0; i < amount; i++) {
            list.add(card);
        }
    }

    public static Card getRandomSpezialkarte(int amount, Map<String, Card> originalMap) {

        //

        var probabilityMap = new ArrayList<Card>();

        for (var card : originalMap.values()) {
            // Schaut ob die karte einzigartig ist
            if (card.unique) {
                addMultiple(card.priority, card, probabilityMap);
            }
        }

        // holt sich ein zufälliges Element aus probabilityMap und returnt es
        if (amount > 0) {
            int randomValue = (int) (Math.random() * probabilityMap.size() + 1);
            return probabilityMap.get(randomValue);
        }

        System.out.println("oops, no place for spezialkarten");

        return null;
    }

}