package wwBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Deckbuilder {

    public static List<Card> create(int playerAmount) throws Exception {

        // Creates a copy of available cards

        final var mapRegisteredCards = Globals.mapRegisteredCards;

        var totalValue = 0;
        var listDeck = new ArrayList<Card>();

        if (playerAmount < 5 || playerAmount > 35) {
            return null;
        }

        // add Seher
        int numbSeher = playerAmount > 18 ? 2 : 1;
        Globals.addMultiple(numbSeher, mapRegisteredCards.get("Seher"), listDeck);

        // add Werwölfe
        int numbWerwölfe = (int) Math.pow(((playerAmount - 2) / 3d), 0.85);
        Globals.addMultiple(numbWerwölfe, mapRegisteredCards.get("Werwolf"), listDeck);

        // add Dorfbewohner
        int numbDorfbewohner = (int) Math.pow(playerAmount * 1.3, 0.65);
        Globals.addMultiple(numbDorfbewohner, mapRegisteredCards.get("Dorfbewohner"), listDeck);

        // add Spezialkarten
        int numbSpezialkarten = playerAmount - (numbDorfbewohner + numbWerwölfe + numbSeher);
        int numbAdjustCards = numbSpezialkarten != 0 ? (int) (numbSpezialkarten / 5) + 1 : 0;

        // add random specialcards außer so viele wie "Ajustierende Karten" sind und
        // setzt den unique Wert der hinzugefügten Karte auf false

        for (int i = 0; i < (numbSpezialkarten - numbAdjustCards); i++) {
            var randomCard = getRandomUniqueCard(numbSpezialkarten, mapRegisteredCards, listDeck);
            listDeck.add(randomCard);
        }

        // add adjusting-special-card, with considering card value of all cards

        // wird so oft ausgeführt wie "Ajustierende Karten" sind
        for (int i = 0; i < numbAdjustCards; i++) {
            // add last special-card, with considering card value of all cards
            totalValue = Globals.totalCardValue(listDeck);
            Card smallestDifferenceCard = null;
            var tempCardList = new ArrayList<Card>();

            // kalkuliert die differenz jeder Karte. Falls die differenz der Karte kleiner
            // ist als die differenz der bisherigen wird sie in smallestDifferenceCard
            // gespeichert.

            for (var card : mapRegisteredCards.values()) {
                if (card.unique && !listDeck.contains(card)) {
                    if (smallestDifferenceCard == null || Math.abs(totalValue + card.value) < Math
                            .abs(totalValue + smallestDifferenceCard.value)) {
                        smallestDifferenceCard = card;
                    }
                }
            }
            // sucht alle Karten, welche denselben Value wie die smallestDifferenceCard
            // haben und speichert diese in einer liste
            for (var card : mapRegisteredCards.values()) {
                if (card.unique && smallestDifferenceCard.value == card.value) {
                    tempCardList.add(card);
                }
            }

            // aus der liste wird zufällig ein Element ausgewählt und dem Deck hinzugefügt
            var rand = (int) (Math.random() * (tempCardList.size()));
            listDeck.add(tempCardList.get(rand));
        }

        totalValue = Globals.totalCardValue(listDeck);

        return listDeck;
    }

    // nimmt mit Berücksichtigung auf die Häufigkeit der Karten eine zufällige Karte
    // und fügt sie dem Deck hinzu, falls sie noch nicht im Deck ist
    public static Card getRandomUniqueCard(int amount, Map<String, Card> mapRegisteredCards, List<Card> listDeck) {

        var probabilityMap = new ArrayList<Card>();

        for (var card : mapRegisteredCards.values()) {
            // Schaut ob die karte einzigartig ist und noch nicht enthalten ist
            if (card.unique && !listDeck.contains(card)) {
                Globals.addMultiple(card.priority, card, probabilityMap);
            }
        }

        // holt sich ein zufälliges Element aus probabilityMap und returnt es
        if (amount > 0) {
            int randomValue = (int) (Math.random() * probabilityMap.size());
            return probabilityMap.get(randomValue);
        }

        System.out.println("oops, no place for spezialkarten");

        return null;
    }

}