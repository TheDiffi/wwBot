  package wwBot.GameStates;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import wwBot.Card;
import wwBot.Command;
import wwBot.Deckbuilder;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;

public class LobbyState extends GameState {
    public static List<User> listJoinedUsers = new ArrayList<>();
    public static List<Card> deck = new ArrayList<>();

    public LobbyState(Game game) {
        super(game);

        registerGameCommands();

        // simulates Users
        User hannelore = null;
        User friedrich = null;
        User samuel = null;
        User raffael = null;
        User santa = null;
        User ente = null;
        User fanta = null;
        User hi = null;
        User ho = null;
        User hun = null;
        User huh = null;
        User damn = null;
        User oof2 = null;
        User oof = null;
        User one = null;
        User two = null;
        User sdfg = null;
        User tree = null;

        listJoinedUsers.add(santa);
        listJoinedUsers.add(friedrich);
        listJoinedUsers.add(hannelore);
        listJoinedUsers.add(raffael);
        listJoinedUsers.add(samuel);
        listJoinedUsers.add(ente);
        listJoinedUsers.add(fanta);
        listJoinedUsers.add(hi);
        listJoinedUsers.add(ho);
        listJoinedUsers.add(hun);
        listJoinedUsers.add(huh);
        listJoinedUsers.add(damn);
        listJoinedUsers.add(oof2);
        listJoinedUsers.add(oof);
        listJoinedUsers.add(one);
        listJoinedUsers.add(two);
        listJoinedUsers.add(sdfg);
        listJoinedUsers.add(tree);
        
        

    }

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerGameCommands() {

        final var mapAvailableCards = Globals.mapAvailableCards;

        // join füght den user zu listJoinedUsers hinzu
        Command joinCommand = (event, parameters) -> {
            User user = event.getMember().get();
            var channel = event.getMessage().getChannel().block();

            if (listJoinedUsers.indexOf(user) == -1) {
                listJoinedUsers.add(user);
                channel.createMessage("joined").block();
            } else {
                channel.createMessage("looks like you're already joined").block();
            }
        };
        gameStateCommands.put("join", joinCommand);

        // leave entfernt den user von listJoinedUsers
        Command leaveCommand = (event, parameters) -> {

            User user = event.getMember().get();
            var channel = event.getMessage().getChannel().block();

            if (listJoinedUsers.indexOf(user) != -1) {
                listJoinedUsers.remove(user);
                channel.createMessage("you left").block();
            } else {
                channel.createMessage("looks like you're already not in the game").block();
            }

        };
        gameStateCommands.put("leave", leaveCommand);

        // nimmt die .size der listPlayers und started damit den Deckbuilder algorithmus
        // übertprüft ob .size größer als 4 und kleiner als 50 ist
        Command buildDeckCommand = (event, parameters) -> {
            var channel = event.getMessage().getChannel().block();

            if (listJoinedUsers.size() > 4 && listJoinedUsers.size() < 35) {

                try {
                    deck = Deckbuilder.create(listJoinedUsers.size());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (deck != null) {
                    channel.createMessage(Globals.printCardList(deck, "AlgorithmDeck")).block();
                    //Globals.printCardList(deck, "AlgorithmDeck")
                } else {
                    channel.createMessage("something went wrong").block();
                }

            } else if (listJoinedUsers.size() < 5) {
                channel.createMessage(
                        "noch nicht genügend Spieler wurden registriert, probiere nach draußen zu gehen und ein paar Freunde zu machen")
                        .block();
            } else if (listJoinedUsers.size() >= 35) {
                channel.createMessage(
                        "theoretisch könnte der bot bot mehr als 35 Spieler schaffen, aus Sicherheitsgründen ist dies jedoch deaktiviert")
                        .block();
            }

        };
        gameStateCommands.put("buildDeck", buildDeckCommand);

        // shows the current Deck to the user
        Command showDeckCommand = (event, parameters) -> {

                event.getMessage().getChannel().block().createMessage(Globals.printCardList(deck, "Deck")).block();

        };
        gameStateCommands.put("showDeck", showDeckCommand);

        /*
         * addCard lässt jeden Player eine Karte aus den verfügbaren Karten eine Karte
         * zur listDeckbuilder hinzufügen addCard überprüft bei jedem Aufruf ob
         * listDeckbuilder nicht größer als listPlayers ist
         */
        // Der Command addCard fügt dem Deck eine vom User gewählte Karte hinzu
        Command addCardCommand = (event, parameters) -> {

            var cardName = parameters.get(0);
            var channel = event.getMessage().getChannel().block();

            if (cardName == "" || cardName == null) {
                channel.createMessage("syntax not correct").block();
            } else {

                var requestedCard = mapAvailableCards.get(cardName);

                // calls addCardToDeck and recieves the Status message back
                String message = addCardToDeck(requestedCard, deck);
                channel.createMessage(message).block();

                // shows new List with added Card
                channel.createMessage(Globals.printCardList(deck, "Deck")).block();

                // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                // und informiert den User über die Differenz
                var figureDifference = Math.abs(deck.size() - listJoinedUsers.size());
                if (figureDifference < 0) {
                    channel.createMessage("Es gibt " + figureDifference + "Karten zu wenig").block();
                } else if (figureDifference > 0) {
                    channel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
                }

            }
        };
        gameStateCommands.put("addCard", addCardCommand);

        // lässt den user eine Karte aus der gewünschten liste entfernen
        Command removeCardCommand = (event, parameters) -> {
            String cardName = parameters.get(0);
            var channel = event.getMessage().getChannel().block();

            if (cardName == null) {
                channel.createMessage("syntax not correct").block();
            } else {
                var requestedCard = mapAvailableCards.get(cardName);

                // calls removeCardFromDeck and recieves the Status message back
                String message = removeCardFromDeck(requestedCard, deck);
                channel.createMessage(message).block();

                // shows new List with added Card
                channel.createMessage(Globals.printCardList(deck, "Deck")).block();

                // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                // und informiert den User über die Differenz
                var figureDifference = Math.abs(deck.size() - listJoinedUsers.size());
                if (figureDifference < 0) {
                    channel.createMessage("Es gibt " + figureDifference + "Karten zu wenig").block();
                } else if (figureDifference > 0) {
                    channel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
                }

            }

        };
        gameStateCommands.put("removeCard", removeCardCommand);

        // starts the game
        // first: the programm checks if Deck is the same size as listJoinedPlayers or
        // empty
        Command startGameCommand = (event, parameters) -> {
            var channel = event.getMessage().getChannel().block();

            // first the programm checks if Deck is the same size as listJoinedPlayers, so
            // that every Player gets a role, no more or less
            if (deck == null) {
                channel.createMessage("How u gonna play with no Deck??!?").block();
            } else if (deck.size() != listJoinedUsers.size()) {
                channel.createMessage("There are not as many Cards as there are registered Players").block();

                // if there are as many cards as joined Users, the Cards get distributed and the Game starts
            } else if (deck.size() == listJoinedUsers.size()) {
                // creates a temporary copy of the Deck
                var tempDeck = new ArrayList<Card>(deck);

                //listPlayers gets populated with the user and its Card(role)(the card gets chosen randomly -> distributed).
                for (User user : listJoinedUsers) {

                    Player player = new Player();
                    player.user = user;
                    var rand = (int) (Math.random() * tempDeck.size());
                    player.role = tempDeck.get(rand);
                    game.listPlayer.put(player.user.getId(), player);
                    tempDeck.remove(rand);

                    //the player gets a message describing his role
                    player.user.getPrivateChannel().block().createMessage("Looks like you are a: " + player.role.name)
                            .block();
                    Globals.printCard(player.role.name, channel);

                }

                game.changeGameState(new MainGameState(game));

            }

        };
        gameStateCommands.put("startGame", startGameCommand);

    }

    public static String addCardToDeck(Card card, List<Card> list) {

        var message = "";
        boolean existing = false;
        // überprüft ob die karte unique ist, falls ja, wird überprüft ob die Karte
        // bereis im Deck ist
        if (card.unique && list != null) {

            // prüft ob die Karte in der Liste existiert
            for (Card deckCard : list) {
                if (deckCard.name.equalsIgnoreCase(card.name)) {
                    existing = true;
                }
            }
            // wenn die karte existiert wird ein fehler gegeben, ansonsten wird sie
            // hinzugefügt
            if (existing) {
                message += "Die gewählte Karte ist einzigartig und bereits im Deck";
            } else {
                list.add(card);
                message += card.name + " wurde dem Deck hinzugefügt";
            }

            // falls die karte nicht unique ist oder die liste leer ist wird die Karte ohne
            // überprüfen hinzugegügt
        } else if (!card.unique || list == null) {
            list.add(card);
            message += card.name + " wurde dem Deck hinzugefügt";

        } else {
            message += "something went wrong in addCard";
        }

        return message;

    }

    public static String removeCardFromDeck(Card card, List<Card> list) {

        var message = "";
        boolean existing = false;
        if (list != null) {

            // prüft ob die Karte in der Liste existiert
            for (Card deckCard : list) {
                if (deckCard.name.equalsIgnoreCase(card.name)) {
                    existing = true;
                }
            }
            // wenn die karte existiert wird sie entfernt
            if (existing) {
                list.remove(card);
                message += card.name + " wurde aus dem Deck entfernt";
            } else {
                message += "Die gewählte Karte ist nicht im Deck";
            }

        } else {
            message += "something went wrong in addCard";
        }

        return message;
    }

    public static String startGameCommand(MessageCreateEvent event) {
        var message = "";

        return message;
    }

    @Override
    public void exit() {

    }
}