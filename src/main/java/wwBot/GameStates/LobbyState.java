package wwBot.GameStates;

import java.awt.Color;
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
    public List<User> listJoinedUsers = new ArrayList<>();
    public List<Card> deck = new ArrayList<>();
    public boolean gameRuleAutomatic = false;
    public User userModerator;

    public LobbyState(Game game) {
        super(game);

        registerGameCommands();

        // simulates Users
        /*
         * User hannelore = null; User friedrich = null; User samuel = null; User
         * raffael = null; User santa = null; User ente = null; User fanta = null; User
         * hi = null; User ho = null; User hun = null; User huh = null; User damn =
         * null; User oof2 = null; User oof = null; User one = null; User two = null;
         * User sdfg = null; User tree = null;
         * 
         * listJoinedUsers.add(santa); listJoinedUsers.add(friedrich);
         * listJoinedUsers.add(hannelore); listJoinedUsers.add(raffael);
         * listJoinedUsers.add(samuel); listJoinedUsers.add(ente);
         * listJoinedUsers.add(fanta); listJoinedUsers.add(hi); listJoinedUsers.add(ho);
         * listJoinedUsers.add(hun); listJoinedUsers.add(huh);
         * listJoinedUsers.add(damn); listJoinedUsers.add(oof2);
         * listJoinedUsers.add(oof); listJoinedUsers.add(one); listJoinedUsers.add(two);
         * listJoinedUsers.add(sdfg); listJoinedUsers.add(tree);
         */

    }

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerGameCommands() {

        final var mapAvailableCards = Globals.mapAvailableCards;

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {

            msgChannel.createMessage("Pong! LobbyState").block();

        };
        gameStateCommands.put("ping", pingCommand);

        // join füght den user zu listJoinedUsers hinzu
        Command joinCommand = (event, parameters, msgChannel) -> {
            User user = event.getMessage().getAuthor().get();
            var bool = true;
            // falls es einen moderator gibt darf dieser nicht joinen
            if (!gameRuleAutomatic && userModerator !=null &&user.getId().equals(userModerator.getId())) {
                bool = false;
            }

            // falls der User noch nicht registriert ist, wird er hinugefügt
            if (listJoinedUsers.indexOf(user) == -1 && bool) {
                listJoinedUsers.add(user);
                msgChannel.createMessage(user.getUsername() + " ist beigetreten!").block();
            } else if (!bool) {
                msgChannel.createMessage("You cannot join if you are the Moderator").block();
            } else {
                msgChannel.createMessage("looks like you're already joined").block();
            }

        };
        gameStateCommands.put("join", joinCommand);

        // leave entfernt den user von listJoinedUsers
        Command leaveCommand = (event, parameters, msgChannel) -> {

            User user = event.getMessage().getAuthor().get();

            // falls der user in der liste ist, wird er entfernt
            if (listJoinedUsers.indexOf(user) != -1) {
                listJoinedUsers.remove(user);
                msgChannel.createMessage(user.getUsername() + " ist ausgetreten").block();
            } else {
                msgChannel.createMessage("looks like you're already not in the game").block();
            }

        };
        gameStateCommands.put("leave", leaveCommand);

        // nimmt die .size der listPlayers und started damit den Deckbuilder algorithmus
        Command buildDeckCommand = (event, parameters, msgChannel) -> {

            // übertprüft ob .size größer als 4 und kleiner als 50 ist
            if (listJoinedUsers.size() > 4 && listJoinedUsers.size() < 35) {
                // versucht den Dekcbuilder Algorithmus aufzurufen und damit deck zu füllen
                try {
                    deck = Deckbuilder.create(listJoinedUsers.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // druckt das neue Deck aus
                if (deck != null) {
                    msgChannel.createMessage(Globals.cardListToString(deck, "AlgorithmDeck")).block();
                } else {
                    msgChannel.createMessage("something went wrong").block();
                }

                // error Messages falls die spieleranzahl
            } else if (listJoinedUsers.size() < 5) {
                msgChannel.createMessage(messageSpec -> {
                    messageSpec.setContent(
                            "noch nicht genügend Spieler wurden registriert, probiere nach draußen zu gehen und ein paar Freunde zu finden (mindestens 5)")
                            .setTts(true);
                }).block();

            } else if (listJoinedUsers.size() >= 35) {
                msgChannel.createMessage(messageSpec -> {
                    messageSpec.setContent(
                            "theoretisch könnte der Bot mehr als 35 Spieler schaffen, dies ist aber aufgrund der Vorschriften der @Nsa und des @Fbi jedoch deaktiviert")
                            .setTts(true);
                }).block();
            }

        };
        gameStateCommands.put("buildDeck", buildDeckCommand);

        // shows the current Deck to the user
        Command showDeckCommand = (event, parameters, msgChannel) -> {

            // prints the deck
            msgChannel.createMessage(Globals.cardListToString(deck, "Deck")).block();

            // prints the moderator if there is one
            if (!gameRuleAutomatic && userModerator != null) {
                msgChannel.createMessage("Moderator: " + userModerator.getUsername());
            } else if (!gameRuleAutomatic && userModerator == null) {
                msgChannel.createMessage("Moderator: wurde noch nicht bestimmt!");
            }

            // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
            // und informiert den User über die Differenz
            var figureDifference = deck.size() - listJoinedUsers.size();
            if (figureDifference < 0) {
                msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu wenig").block();
            } else if (figureDifference > 0) {
                msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
            }

        };
        gameStateCommands.put("showDeck", showDeckCommand);

        /*
         * addCard lässt jeden Player eine Karte aus den verfügbaren Karten eine Karte
         * zur listDeckbuilder hinzufügen addCard überprüft bei jedem Aufruf ob
         * listDeckbuilder nicht größer als listPlayers ist
         */
        // Der Command addCard fügt dem Deck eine vom User gewählte Karte hinzu
        Command addCardCommand = (event, parameters, msgChannel) -> {

            var cardName = parameters.get(0);

            if (cardName == "" || cardName == null) {
                msgChannel.createMessage("syntax not correct").block();
            } else {

                var requestedCard = mapAvailableCards.get(cardName);

                // calls addCardToDeck and recieves the Status message back
                String message = addCardToDeck(requestedCard, deck);
                msgChannel.createMessage(message).block();

                // shows new List with added Card
                msgChannel.createMessage(Globals.cardListToString(deck, "Deck")).block();

                // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                // und informiert den User über die Differenz
                var figureDifference = deck.size() - listJoinedUsers.size();
                if (figureDifference < 0) {
                    msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu wenig").block();
                } else if (figureDifference > 0) {
                    msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
                }

            }
        };
        gameStateCommands.put("addCard", addCardCommand);

        // lässt den user eine Karte aus der gewünschten liste entfernen
        Command removeCardCommand = (event, parameters, msgChannel) -> {
            String cardName = parameters.get(0);

            if (cardName == null) {
                msgChannel.createMessage("syntax not correct").block();
            } else {
                var requestedCard = mapAvailableCards.get(cardName);

                // calls removeCardFromDeck and recieves the Status message back
                String message = removeCardFromDeck(requestedCard, deck);
                msgChannel.createMessage(message).block();

                // shows new List without removed Card
                msgChannel.createMessage(Globals.cardListToString(deck, "Deck")).block();

                // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                // und informiert den User über die Differenz
                var figureDifference = deck.size() - listJoinedUsers.size();
                if (figureDifference < 0) {
                    msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu wenig").block();
                } else if (figureDifference > 0) {
                    msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
                }

            }
        };
        gameStateCommands.put("removeCard", removeCardCommand);

        // empties the Deck
        Command clearDeckCommand = (event, parameters, msgChannel) -> {
            deck.clear();
            msgChannel.createMessage("Das Deck wurde ausgeleert").block();
        };
        gameStateCommands.put("clearDeck", clearDeckCommand);

        // sets the moderator to manual/automatic
        Command gameruleCommand = (event, parameters, msgChannel) -> {
            var gamerule = parameters.get(0);

            if (gamerule.equalsIgnoreCase("Automatic")) {
                if (!gameRuleAutomatic) {
                    gameRuleAutomatic = true;
                    Globals.createEmbed(msgChannel, Color.MAGENTA, "Der Moderator wurde auf automatisch gestellt",
                            "In diesem Modus wird keine Person benötigt, da der Bot die vollständige Rolle des Moderators einnimmt.");
                } else {
                    Globals.createMessage(msgChannel, "Der Moderator ist schon auf automatisch gestellt", false);
                }
            }
            if (gamerule.equalsIgnoreCase("Manual")) {
                if (gameRuleAutomatic) {
                    gameRuleAutomatic = false;
                    Globals.createEmbed(msgChannel, Color.MAGENTA, "Der Moderator wurde auf manuell gestellt",
                            "In diesem Modus wird eine Person benötigt, welche die Rolle des Moderators einnimmt. Diese Person sollte dem Spiel nicht beitreten sondern den Befehl \"&MakeMeModerator\" aufrufen.");
                } else {
                    Globals.createMessage(msgChannel, "Der Moderator ist schon auf manuell gestellt", false);
                }
            }
            if (gamerule.equalsIgnoreCase("RandomJobs")) {

                msgChannel.createMessage("TODO: add random jobs lol");
            }

        };
        gameStateCommands.put("gamerule", gameruleCommand);

        Command makeMeModeratorCommand = (event, parameters, msgChannel) -> {

            if (!gameRuleAutomatic) {
                userModerator = event.getMessage().getAuthor().get();
                Globals.createEmbed(msgChannel, Color.GREEN,
                        "@" + event.getMessage().getAuthor().get().getUsername() + " is the Moderator", "");

            } else {
                msgChannel.createMessage(
                        "Das Spiel befindet sich im automatischen Moderations-Modus. Der Command \"&gamerule Manual\" ändert das Spiel in den manuellen Moderations-Modus.");
            }

        };
        gameStateCommands.put("makeMeModerator", makeMeModeratorCommand);

        // starts the game
        // cheks if there is a moderator or the gamerule is set to automatic
        // first: the programm checks if Deck is the same size as listJoinedPlayers or
        // empty
        Command startGameCommand = (event, parameters, msgChannel) -> {
            var checkMod = false;

            if (gameRuleAutomatic) {
                checkMod = true;
            } else if (!gameRuleAutomatic && userModerator != null) {
                checkMod = true;
            } else if (!gameRuleAutomatic && userModerator == null) {
                checkMod = false;
                msgChannel.createMessage("How u gonna play with no Moderator?").block();

            }

            if (checkMod) {
                // first the programm checks if Deck is the same size as listJoinedPlayers, so
                // that every Player gets a role, no more or less
                if (deck == null) {
                    msgChannel.createMessage("How u gonna play with no Deck?").block();
                } else if (deck.size() != listJoinedUsers.size()) {
                    msgChannel.createMessage("There are not as many Cards as there are registered Players").block();

                    // if there are as many cards as joined Users, the Cards get distributed and the
                    // Game starts
                } else if (deck.size() == listJoinedUsers.size()) {

                    msgChannel.createMessage("Einen Moment Gedult...").block();
                    // creates a temporary copy of the Deck
                    var tempDeck = new ArrayList<Card>(deck);

                    // listPlayers gets populated with the user and its Card(role)(the card gets
                    // chosen randomly -> distributed).
                    for (User user : listJoinedUsers) {

                        // creates a new player and fills the object
                        Player player = new Player();
                        player.user = user;
                        var rand = (int) (Math.random() * tempDeck.size());
                        player.role = tempDeck.get(rand);
                        tempDeck.remove(rand);

                        // loads the values into the game
                        game.mapPlayers.put(player.user.getId(), player);
                        game.deck = deck;
                        game.gameRuleAutomatic = gameRuleAutomatic;
                        game.userModerator = userModerator;

                        // the player gets a message describing his role
                        var privateChannel = player.user.getPrivateChannel().block();
                        privateChannel.createMessage("Es sieht aus als währst du ein/e " + player.role.name).block();
                        Globals.printCard(player.role.name, privateChannel);
                    }
                    msgChannel.createMessage("Game Created!").block();
                    // initializes the next game state
                    if (gameRuleAutomatic) {
                        game.changeGameState(new MainGameState(game));
                    } else {
                        game.changeGameState(new SemiMainGameState(game));
                    }
                }

            }
        };
        gameStateCommands.put("startGame", startGameCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            msgChannel.createMessage("TODO: create help in LOBBYPHASE").block();
        };
        gameStateCommands.put("help", helpCommand);
    }

    // TODO: create a method that prints images
    /*
     * public static void printCardImage(MessageChannel channel, Card card ){
     * 
     * try { Image image = ImageIO.read(new File("strawberry.jpg")); } catch
     * (IOException e) { e.printStackTrace(); }
     * 
     * channel.createMessage(messageSpec -> { messageSpec.addFile("Card", image);
     * }).block();
     * 
     * }
     */

    // recieves a card and a list and adds the card to the list, acoording to some
    // rules
    // gibt einen String mit der status-nachricht zurück
    public static String addCardToDeck(Card card, List<Card> list) {

        var message = "";
        // überprüft ob die karte unique ist, falls ja, wird überprüft ob die Karte
        // bereis im Deck ist
        if (card.unique && list != null) {

            // wenn die karte existiert wird ein fehler gegeben, ansonsten wird sie
            // hinzugefügt
            if (list.contains(card)) {
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

    // recieves a card and a list and, if the card is present in the list, removes
    // the card from the list
    // gibt einen String mit der status-nachricht zurück
    public static String removeCardFromDeck(Card card, List<Card> list) {

        var message = "";

        // prüft ob die liste nicht leer ist
        if (list != null) {

            // prüft ob die Karte in der Liste existiert und entfernt sie falls true
            boolean removed = list.remove(card);

            // gibt die status meldung
            if (removed) {
                message += card.name + " wurde aus dem Deck entfernt";
            } else {
                message += "Die gewählte Karte ist nicht im Deck";
            }
            // falls die liste leer ist wird ein fehler gegeben
        } else {
            message += "something went wrong in removeCard";
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