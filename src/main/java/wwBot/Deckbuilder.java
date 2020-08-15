package wwBot;

import java.util.ArrayList;
import java.util.List;

import wwBot.cards.Card;
import wwBot.cards.Role;

public class Deckbuilder {

    public static List<Role> create(int playerAmount) throws Exception {

        // final var mapRegisteredCards = Globals.mapRegisteredCards;
        var mapRegisteredCards = Globals.mapRegisteredCardsSpecs;

        var totalValue = 0;
        var listDeck = new ArrayList<Role>();

        if (playerAmount < 5 || playerAmount > 35) {
            return null;
        }

        // add Seher
        int numbSeher = playerAmount > 18 ? 2 : 1;
        Globals.addMultipleCards(numbSeher, "Seher", listDeck);

        // add Werwölfe
        int numbWerwölfe = (int) Math.pow(((playerAmount - 2) / 3d), 0.85);
        Globals.addMultipleCards(numbWerwölfe, "Werwolf", listDeck);

        // add Dorfbewohner
        int numbDorfbewohner = (int) Math.pow(playerAmount * 1.3, 0.65);
        Globals.addMultipleCards(numbDorfbewohner, "Dorfbewohner", listDeck);

        // add Spezialkarten
        int numbSpezialkarten = playerAmount - (numbDorfbewohner + numbWerwölfe + numbSeher);
        int numbAdjustCards = numbSpezialkarten != 0 ? (int) (numbSpezialkarten / 5) + 1 : 0;

        // add random specialcards außer so viele wie "Ajustierende Karten" sind und
        // setzt den unique Wert der hinzugefügten Karte auf false
        for (int i = 0; i < (numbSpezialkarten - numbAdjustCards); i++) {
            listDeck.add(getRandomUniqueCard(numbSpezialkarten, listDeck));
        }

        // fügt die "Ajustierende Karten" hinzu
        for (int i = 0; i < numbAdjustCards; i++) {
            // add last special-card, with considering card value of all cards
            totalValue = Globals.totalCardValue(listDeck);
            Card smallestDifferenceCard = null;
            
            // Kalkuliert die Differenz jeder Karte. Falls die Differenz der Karte kleiner
            // ist als die Differenz der bisherigen wird sie in smallestDifferenceCard
            // gespeichert.
            for (var card : mapRegisteredCards.values()) {

                if (card.unique && !Globals.listContainsCard(listDeck, card)) {
                    if (smallestDifferenceCard == null || Math.abs(totalValue + card.value) < Math
                            .abs(totalValue + smallestDifferenceCard.value)) {
                        smallestDifferenceCard = card;
                    }
                }
            }
            // sucht alle Karten, welche denselben Value wie die smallestDifferenceCard
            // haben und speichert diese in einer liste
            var tempCardList = new ArrayList<Card>();
            for (var card : mapRegisteredCards.values()) {
                if (card.unique && smallestDifferenceCard.value == card.value) {
                    tempCardList.add(card);
                }
            }

            // aus der liste wird zufällig ein Element ausgewählt und dem Deck hinzugefügt
            var rand = (int) (Math.random() * (tempCardList.size()));
            listDeck.add(Role.createRole(tempCardList.get(rand).name));
        }

        return listDeck;
    }

    // nimmt mit Berücksichtigung auf die Häufigkeit der Karten eine zufällige Karte
    // und fügt sie dem Deck hinzu, falls sie noch nicht im Deck ist
    public static Role getRandomUniqueCard(int amount, List<Role> listDeck) {
        var mapRegisteredCards = Globals.mapRegisteredCardsSpecs;
        var probabilityMap = new ArrayList<String>();

        // Schaut ob jede Karte einzigartig und noch nicht enthalten ist
        for (var card : mapRegisteredCards.values()) {

            if (card.unique && !Globals.listContainsCard(listDeck, card)) {
                // fügt den namen der karte so oft hinzu, wie groß die probability ist
                for (int i = 0; i < card.priority; i++) {
                    probabilityMap.add(card.name);
                }
            }
        }

        // holt sich ein zufälliges Element aus probabilityMap und returnt es
        if (amount > 0) {
            int randomValue = (int) (Math.random() * probabilityMap.size());
            return Role.createRole(probabilityMap.get(randomValue));

        } else {
            System.out.println("oops, no place for spezialkarten");
        }
        return null;
    }

}