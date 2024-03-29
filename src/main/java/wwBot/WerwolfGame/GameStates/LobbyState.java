package wwBot.WerwolfGame.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.object.entity.User;
import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.WerwolfGame.Deckbuilder;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.cards.Card;
import wwBot.WerwolfGame.cards.Role;

public class LobbyState extends GameState {
    public List<User> listJoinedUsers = new ArrayList<>();
    public List<Role> deck = new ArrayList<>();
    public Map<String, Card> mapRegisteredCardsSpecs = new TreeMap<String, Card>(String.CASE_INSENSITIVE_ORDER);
    public User userModerator;

    public LobbyState(Game game) {
        super(game);
        mapRegisteredCardsSpecs = Globals.mapRegisteredCardsSpecs;
        registerGameCommands();
        MessagesWW.newGameStartMessage(game.mainChannel);

    }

    // TODO: idea: when it show the deck, delete the previous message that showed the
    // deck

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerGameCommands() {

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {

            msgChannel.createMessage("Pong! LobbyState").block();

        };
        gameStateCommands.put("ping", pingCommand);

        Command helpCommand = (event, parameters, msgChannel) -> {
            MessagesWW.sendHelpLobby(msgChannel);

        };
        gameStateCommands.put("help", helpCommand);
        gameStateCommands.put("hilfe", helpCommand);

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = MessagesWW.getCommandsMain();
            mssg += "\n" + MessagesWW.getCommandsGame();
            mssg += "\n" + MessagesWW.getCommandsLobby();
            mssg += "\n" + MessagesWW.getHelpInfo();
            Globals.createEmbed(msgChannel, Color.CYAN, "Commands", mssg);

        };
        gameStateCommands.put("showCommands", showCommandsCommand);
        gameStateCommands.put("lsCommands", showCommandsCommand);
        gameStateCommands.put("Commands", showCommandsCommand);

        // join füght den user zu listJoinedUsers hinzu
        Command joinCommand = (event, parameters, msgChannel) -> {
            User user = event.getMessage().getAuthor().get();
            var bool = true;
            // falls es einen moderator gibt darf dieser nicht joinen
            if (!game.gameRuleAutomaticMod && userModerator != null && user.getId().equals(userModerator.getId())) {
                bool = false;

            }
            // mit einer DM soll man nicht joinen können
            if (!event.getGuildId().isPresent()) {
                bool = false;
            }

            // falls der User noch nicht registriert ist, wird er hinugefügt
            if (listJoinedUsers.indexOf(user) == -1 && bool) {
                listJoinedUsers.add(user);
                msgChannel
                        .createMessage(
                                user.asMember(game.server.getId()).block().getDisplayName() + " ist beigetreten!")
                        .block();
            } else if (!bool) {
                msgChannel.createMessage("You cannot join if you are the Moderator").block();
            } else {
                msgChannel.createMessage("looks like you're already joined").block();
            }

        };
        gameStateCommands.put("join", joinCommand);

        // leave entfernt den user von listJoinedUsers
        Command leaveCommand = (event, parameters, msgChannel) -> {
            Globals.loadingMsg(game, msgChannel);
            User user = event.getMessage().getAuthor().get();

            // falls der user in der liste ist, wird er entfernt
            if (listJoinedUsers.indexOf(user) != -1) {
                listJoinedUsers.remove(user);
                msgChannel
                        .createMessage(user.asMember(game.server.getId()).block().getDisplayName() + " ist ausgetreten")
                        .block();
            } else {
                msgChannel.createMessage("looks like you're already not in the game").block();
            }

        };
        gameStateCommands.put("leave", leaveCommand);

        Command listJoinedPlayersCommand = (event, parameters, msgChannel) -> {
            var mssg = Globals.userListToString(listJoinedUsers, "Beigetretene Spieler", game);
            Globals.createMessage(game.mainChannel, mssg, false);

        };
        gameStateCommands.put("joinedPlayers", listJoinedPlayersCommand);
        gameStateCommands.put("lsJoined", listJoinedPlayersCommand);
        gameStateCommands.put("listJoinedUsers", listJoinedPlayersCommand);

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
                    msgChannel.createMessage(Globals.cardListToString(deck, "AlgorithmDeck", true)).block();
                } else {
                    msgChannel.createMessage("something went wrong").block();
                }

                // error Messages falls die spieleranzahl
            } else if (listJoinedUsers.size() < 5) {
                msgChannel.createMessage(messageSpec -> {
                    messageSpec.setContent(
                            "noch nicht genügend Spieler wurden registriert, probiere nach draußen zu gehen und ein paar Freunde zu finden (mindestens 5)")
                            .setTts(false);
                }).block();

            } else if (listJoinedUsers.size() >= 35) {
                msgChannel.createMessage(messageSpec -> {
                    messageSpec.setContent(
                            "theoretisch könnte der Bot mehr als 35 Spieler schaffen, dies ist aber aufgrund der Vorschriften der @Nsa und des @Fbi jedoch deaktiviert")
                            .setTts(false);
                }).block();
            }

        };
        gameStateCommands.put("buildDeck", buildDeckCommand);

        // shows the current Deck to the user
        Command showDeckCommand = (event, parameters, msgChannel) -> {

            // prints the deck
            msgChannel.createMessage(Globals.cardListToString(deck, "Deck", true)).block();

            // prints the moderator if there is one
            if (!game.gameRuleAutomaticMod && userModerator != null) {
                msgChannel.createMessage(
                        "Moderator: " + userModerator.asMember(game.server.getId()).block().getDisplayName());
            } else if (!game.gameRuleAutomaticMod && userModerator == null) {
                msgChannel.createMessage("Moderator: wurde noch nicht bestimmt!");
            }

            // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
            // und informiert den User über die Differenz
            int figureDifference = deck.size() - listJoinedUsers.size();
            if (figureDifference < 0) {
                msgChannel.createMessage("Es gibt " + Math.abs(figureDifference) + " Karten zu wenig").block();
            } else if (figureDifference > 0) {
                msgChannel.createMessage("Es gibt " + figureDifference + " Karten zu viel").block();
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

            if (parameters != null) {
                // gets the Carda by its namea
                for (String cardName : parameters) {
                    var requestedCard = mapRegisteredCardsSpecs.get(cardName);

                    if (requestedCard != null) {
                        // calls addCardToDeck and recieves the Status message back
                        String message = addCardToDeck(requestedCard, deck);
                        msgChannel.createMessage(message).block();
                    } else if(cardName.equals("") || cardName.equals(" ")){
                        System.out.println("whitespace as cardname found & ignored");
                    } else {
                        MessagesWW.errorCardNotFound(msgChannel);
                    }
                }

                // shows new List with added Card
                msgChannel.createMessage(Globals.cardListToString(deck, "Deck", true)).block();

                // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                // und informiert den User über die Differenz
                int figureDifference = deck.size() - listJoinedUsers.size();
                if (figureDifference < 0) {
                    msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu wenig").block();
                } else if (figureDifference > 0) {
                    msgChannel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
                }

            } else {
                MessagesWW.errorWrongSyntax(msgChannel);
            }
        };
        gameStateCommands.put("addCard", addCardCommand);
        gameStateCommands.put("aC", addCardCommand);

        // lässt den user eine Karte aus der gewünschten liste entfernen
        Command removeCardCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.size() == 1) {

                var requestedCard = mapRegisteredCardsSpecs.get(parameters.get(0));
                if (requestedCard != null) {

                    // calls removeCardFromDeck and recieves the Status message back
                    String message = removeCardFromDeck(requestedCard, deck);
                    msgChannel.createMessage(message).block();

                    // shows new List without removed Card
                    msgChannel.createMessage(Globals.cardListToString(deck, "Deck", true)).block();

                    // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                    // und informiert den User über die Differenz
                    int figureDifference = deck.size() - listJoinedUsers.size();
                    if (figureDifference < 0) {
                        msgChannel.createMessage("Es gibt " + Math.abs(figureDifference) + " Karten zu wenig").block();
                    } else if (figureDifference > 0) {
                        msgChannel.createMessage("Es gibt " + figureDifference + " Karten zu viel").block();
                    }

                } else {
                    MessagesWW.errorCardNotFound(msgChannel);
                }
            } else {
                MessagesWW.errorWrongSyntax(msgChannel);
            }
        };
        gameStateCommands.put("removeCard", removeCardCommand);
        gameStateCommands.put("remove", removeCardCommand);
        gameStateCommands.put("rC", removeCardCommand);

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
                if (!game.gameRuleAutomaticMod) {
                    game.gameRuleAutomaticMod = true;
                    Globals.createEmbed(msgChannel, Color.MAGENTA, "Der Moderator wurde auf automatisch gestellt",
                            "In diesem Modus wird keine Person benötigt, da der Bot die vollständige Rolle des Moderators einnimmt.");
                } else {
                    Globals.createMessage(msgChannel, "Der Moderator ist schon auf automatisch gestellt", false);
                }
            }
            if (gamerule.equalsIgnoreCase("Manual")) {
                if (game.gameRuleAutomaticMod) {
                    game.gameRuleAutomaticMod = false;
                    Globals.createEmbed(msgChannel, Color.MAGENTA, "Der Moderator wurde auf manuell gestellt",
                            "In diesem Modus wird eine Person benötigt, welche die Rolle des Moderators einnimmt. Diese Person sollte dem Spiel nicht beitreten sondern den Befehl \"&MakeMeModerator\" aufrufen.");
                } else {
                    Globals.createMessage(msgChannel, "Der Moderator ist schon auf manuell gestellt", false);
                }
            }
            if (gamerule.equalsIgnoreCase("RandomJobs")) {

                msgChannel.createMessage("TODO: add random jobs lol");
            }
            if (gamerule.equalsIgnoreCase("mute") || gamerule.equalsIgnoreCase("muteplayersatnight")) {

                if (parameters.size() > 1) {
                    if (parameters.get(1).equalsIgnoreCase("false")) {
                        game.gameRuleMutePlayersAtNight = false;
                        Globals.createEmbed(msgChannel, Color.MAGENTA,
                                "The Gamerule \"Mute Players At Night\" has been set to false.",
                                "Die Spieler werden Nachts nicht stummgeschaltet werden.");

                    } else if (parameters.get(1).equalsIgnoreCase("true")) {
                        game.gameRuleMutePlayersAtNight = true;
                        Globals.createEmbed(msgChannel, Color.MAGENTA,
                                "The Gamerule \"Mute Players At Night\" has been set to true.",
                                "Die Spieler werden nun Nachts stummgeschaltet werden.");

                    }

                } else {
                    MessagesWW.errorWrongSyntax(msgChannel);
                }

            }

            if (gamerule.equalsIgnoreCase("printCard")) {

                if (parameters.size() > 1) {
                    if (parameters.get(1).equalsIgnoreCase("false")) {
                        game.gameRuleMutePlayersAtNight = false;
                        Globals.createEmbed(msgChannel, Color.MAGENTA,
                                "The Gamerule \"printCardOnDeath\" has been set to false.", "");

                    } else if (parameters.get(1).equalsIgnoreCase("true")) {
                        game.gameRuleMutePlayersAtNight = true;
                        Globals.createEmbed(msgChannel, Color.MAGENTA,
                                "The Gamerule \"printCardOnDeath\" has been set to true.", "");

                    }

                } else {
                    MessagesWW.errorWrongSyntax(msgChannel);
                }

            }
        };
        gameStateCommands.put("gamerule", gameruleCommand);

        Command makeMeModeratorCommand = (event, parameters, msgChannel) -> {
            var bool = false;
            if (listJoinedUsers.indexOf(event.getMessage().getAuthor().get()) != -1) {
                bool = true;

                // mit einer DM soll man nicht joinen können
            }
            if (!event.getGuildId().isPresent()) {
                bool = false;
            }

            if (!game.gameRuleAutomaticMod && !bool) {
                userModerator = event.getMessage().getAuthor().get();
                Globals.createEmbed(msgChannel, Color.GREEN, " "
                        + event.getMessage().getAuthor().get().asMember(game.server.getId()).block().getDisplayName()
                        + " ist der Moderator", "");
            } else if (bool) {
                msgChannel.createMessage(
                        "Es sieht aus als wärst du dem Spiel beigetreten, benutze den Command \"&leave\" um Moderator zu werden")
                        .block();
            } else {
                msgChannel.createMessage(
                        "Das Spiel befindet sich im automatischen Moderations-Modus. Der Command \"&gamerule Manual\" ändert das Spiel in den manuellen Moderations-Modus.")
                        .block();
            }

        };
        gameStateCommands.put("makeMeModerator", makeMeModeratorCommand);
        gameStateCommands.put("makeMeMod", makeMeModeratorCommand);
        gameStateCommands.put("setMod", makeMeModeratorCommand);

        // starts the game
        // cheks if there is a moderator or the gamerule is set to automatic
        // first: the programm checks if Deck is the same size as listJoinedPlayers or
        // empty
        Command startGameCommand = (event, parameters, msgChannel) -> {
            var checkMod = false;
            if (game.gameRuleAutomaticMod) {
                checkMod = true;
            } else if (!game.gameRuleAutomaticMod && userModerator != null) {
                checkMod = true;
            } else if (!game.gameRuleAutomaticMod && userModerator == null) {
                checkMod = false;
                msgChannel.createMessage("How u gonna play with no Moderator?").block();

            }

            if (checkMod) {
                if (Math.abs(Globals.totalCardValue(deck)) > 6) {
                    msgChannel.createMessage("```Value: " + Globals.totalCardValue(deck)
                            + "\nWith a Value like this the game will be very one-sided!```").block();
                }

                // first the programm checks if Deck is the same size as listJoinedPlayers, so
                // that every Player gets a role, no more or less
                if (deck == null || deck.size() == 0) {
                    msgChannel.createMessage("How u gonna play with no Deck?").block();
                } else if (deck.size() != listJoinedUsers.size()) {
                    msgChannel.createMessage("There are not as many Cards as there are registered Players").block();

                    // if there are as many cards as joined Users, the Cards get distributed and the
                    // Game starts
                } else if (deck.size() == listJoinedUsers.size()) {

                    Globals.loadingMsg(game, msgChannel);
                    // creates a temporary copy of the Deck
                    var tempDeck = new ArrayList<Role>(deck);

                    // listPlayers gets populated with the user and its Card(role)(the card gets
                    // chosen randomly -> distributed).
                    for (User user : listJoinedUsers) {

                        // creates a new player and fills the object
                        Player player = new Player();
                        player.name = user.asMember(game.server.getId()).block().getDisplayName();
                        player.user = user;
                        var rand = (int) (Math.random() * tempDeck.size());
                        player.role = tempDeck.get(rand);
                        tempDeck.remove(rand);

                        // saves the player
                        game.mapPlayers.put(player.user.getId(), player);
                        

                        // the player gets a message describing his role
                        var privateChannel = player.user.getPrivateChannel().block();
                        privateChannel.createMessage("Es sieht aus als währst du ein/e " + player.role.name).block();
                        Globals.printCard(player.role.name, privateChannel);
                    }

                    // saves values into the game
                    game.deck = deck;
                    game.userModerator = userModerator;

                    msgChannel.createMessage("Game Created!").block();
                    // initializes the next game state
                    if (game.gameRuleAutomaticMod) {
                        game.changeGameState(new AutoState(game));
                    } else {
                        game.changeGameState(new SemiState(game));
                    }
                }

            }
        };
        gameStateCommands.put("start", startGameCommand);
        gameStateCommands.put("startGame", startGameCommand);

    }

    // recieves a card and a list and adds the card to the list, acoording to some
    // rules
    // gibt einen String mit der status-nachricht zurück
    public static String addCardToDeck(Card cardSpec, List<Role> list) {

        var message = "";
        // überprüft ob die karte unique ist, falls ja, wird überprüft ob die Karte
        // bereis im Deck ist (ausnahme: Seher)
        if ((cardSpec.unique && !cardSpec.name.equalsIgnoreCase("Seher")) && list != null) {

            // wenn die karte existiert wird ein fehler gegeben, ansonsten wird sie
            // hinzugefügt
            if (Globals.listContainsCard(list, cardSpec)) {
                message += "Die gewählte Karte ist einzigartig und bereits im Deck";
            } else {
                list.add(Role.createRole(cardSpec.name));
                message += cardSpec.name + " wurde dem Deck hinzugefügt";
            }

            // falls die karte nicht unique ist oder die liste leer ist wird die Karte ohne
            // überprüfen hinzugegügt
        } else if ((!cardSpec.unique || cardSpec.name.equalsIgnoreCase("Seher")) || list == null) {
            list.add(Role.createRole(cardSpec.name));
            message += cardSpec.name + " wurde dem Deck hinzugefügt";

        } else {
            message += "something went wrong in addCard";
        }

        return message;
    }

    // recieves a card and a list and, if the card is present in the list, removes
    // the card from the list
    // gibt einen String mit der status-nachricht zurück
    public static String removeCardFromDeck(Card cardSpec, List<Role> list) {

        var message = "";

        // prüft ob die liste nicht leer ist
        if (list != null) {

            // prüft ob die Karte in der Liste existiert und entfernt sie falls true
            var removed = false;
            for (Role card : list) {
                if (card.name.equals(cardSpec.name)) {
                    removed = list.remove(card);
                    break;
                }
            }

            // gibt die status meldung
            if (removed) {
                message += cardSpec.name + " wurde aus dem Deck entfernt";
            } else {
                message += "Die gewählte Karte ist nicht im Deck";
            }
            // falls die liste leer ist wird ein fehler gegeben
        } else {
            message += "Das Deck ist nock leer.";
        }

        return message;
    }

    public void close() {

    }
}