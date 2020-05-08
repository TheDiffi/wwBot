package wwBot;

import java.awt.Color;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

public class Main {

    public static List<User> listJoinedUsers = new ArrayList<>();
    public static List<Card> listCustomDeck = new ArrayList<>();
    public static List<Card> listDeckbuilder = new ArrayList<>();
    public static List<Card> listFinalDeck = new ArrayList<>();
    public static HashMap<Snowflake, Player> listPlayer = new HashMap<Snowflake,Player>();

    public static void main(String[] args) throws Exception {

        Globals.loadGlobals();
        final var mapAvailableCards = Globals.mapAvailableCards;

        // simulates Users
        User hannelore = null;
        User friedrich = null;
        User samuel = null;
        User raffael = null;
        User santa = null;
        User ente = null;
        User fanta = null;
        
        listJoinedUsers.add(santa);
        listJoinedUsers.add(friedrich);
        listJoinedUsers.add(hannelore);
        listJoinedUsers.add(raffael);
        listJoinedUsers.add(samuel);
        listJoinedUsers.add(ente);
        listJoinedUsers.add(fanta);
        

        // speichert den Prefix in einer Variable
        var prefix = "&";

        DiscordClient client = DiscordClientBuilder
                .create("NzA3NjUzNTk1NjQxOTM4MDMx.XrL8Cw.SRxT4UisfP6doLQoc-ZdgI5CtYY").build();

        // looks at every message
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {

                    // messageContent speichert den Inhalt der Message in einer Variable
                    // command teilt diesen Inhalt bei einem Leerzeichen und speicher dies in einer
                    // Liste
                    User user = event.getMember().get();
                    String messageContent = event.getMessage().getContent().orElse("");
                    List<String> command = Arrays.asList(messageContent.split(" "));

                    // Schaut ob der erste Teil der Nachricht das Prefix ist
                    if (messageContent.startsWith(prefix)) {

                        var channel = event.getMessage().getChannel().block();

                        // Liste der Commands, content und sonstige parameter werden überprüft
                        // TODO: gruppiere Commands nach LobbyPhase, GamePhase, GameEndPhase und lagere
                        // dies in funktion aus
                        // TODO: füge bedingung für jede Gruppe hinzu

                        // TODO: prefix aus parameter entfetnen sonst wird 2-mal prefix überprüft =
                        // schlampig

                        // ping testet ob der bot antwortet
                        if (command.get(0).equalsIgnoreCase(prefix + "ping")) {
                            channel.createMessage("Pong!").block();
                        }

                        // TODO: erstelle help
                        // TODO: erstelle tutorial

                        // Commands der Lobby Phase
                        // join füght den user zu listPlayers hinzu
                        if (command.get(0).equalsIgnoreCase(prefix + "join")) {

                            if (listJoinedUsers.indexOf(user) == -1) {
                                listJoinedUsers.add(user);
                                channel.createMessage("joined").block();
                            } else {
                                channel.createMessage("looks like you're already joined").block();
                            }
                        }

                        // leave entfernt den user von listPlayers
                        if (command.get(0).equalsIgnoreCase(prefix + "leave")) {

                            if (listJoinedUsers.indexOf(user) != -1) {
                                listJoinedUsers.remove(user);
                                channel.createMessage("you left").block();
                            } else {
                                channel.createMessage("looks like you're already not in the game").block();
                            }
                        }

                        // nimmt die .size der listPlayers und started damit den Deckbuilder algorithmus
                        // übertprüft ob .size größer als 4 und kleiner als 50 ist
                        if (command.get(0).equalsIgnoreCase(prefix + "buildDeck")) {

                            if (listJoinedUsers.size() > 4 || listJoinedUsers.size() < 35) {
                                try {
                                    listDeckbuilder = Deckbuilder.create(listJoinedUsers.size());
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if (listDeckbuilder != null) {
                                    channel.createMessage(printCardList(listDeckbuilder)).block();
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

                        }

                        if (command.get(0).equalsIgnoreCase(prefix + "showCard")) {

                            String cardName = command.get(1);
                            // Card requestedCard = new Card();

                            var requestedCard = mapAvailableCards.get(cardName);
                            if (requestedCard != null) {
                                String message = "Wert: " + Integer.toString(requestedCard.value) + "\n"
                                        + "Beschreibung: " + requestedCard.description;
                                var color = requestedCard.friendly ? Color.GREEN : Color.RED;

                                channel.createEmbed(spec -> {
                                    spec.setColor(color).setTitle(cardName).setDescription(message);

                                }).block();

                            } else {
                                channel.createMessage("card not found");
                            }

                        }

                        if (command.get(0).equalsIgnoreCase(prefix + "showDeck")) {
                            // gets the list name
                            String listName = command.get(1);

                            // looks if the list exists
                            if (listName.equalsIgnoreCase("CustomDeck")) {
                                channel.createMessage(printCardList(listCustomDeck)).block();

                            } else if (listName.equalsIgnoreCase("AlgorithmDeck")) {
                                channel.createMessage(printCardList(listDeckbuilder)).block();

                            } else if (listName.equalsIgnoreCase("FinalDeck")) {
                                channel.createMessage(printCardList(listFinalDeck)).block();
                            }
                        }

                        if (command.get(0).equalsIgnoreCase(prefix + "distributeCards")) {
                            //first the programm checks if listFinalDeck is the same size as listJoinedPlayers, so that every Player gets a role, no more or less
                            if (listFinalDeck.size() == 0) {
                                channel.createMessage("Choose a Deck with **" + prefix + "chooseCustomDeck** or ++"
                                        + "chooseAlgorithmDeck**").block();
                                channel.createMessage("If you have not created a Deck jet, try **" + prefix
                                        + "buildDeck** to let the Algorithm build a Deck for you or try **" + prefix
                                        + "addCard** to add a Card to your Custom Deck").block();

                            } else if (listFinalDeck.size() != listJoinedUsers.size()) {
                                channel.createMessage("There are not as many Cards as there are registered Players")
                                        .block();
                            } else if (listFinalDeck.size() == listJoinedUsers.size()) {

                                var tempListFinalCards = new ArrayList<Card>();
                                for(Card c : listFinalDeck) {
                                    tempListFinalCards.add(c);
                                }


                                        for (User listJoinedUser : listJoinedUsers) {

                                            Player player = new Player();
                                            player.user = listJoinedUser;
                                            var rand = (int) (Math.random() * tempListFinalCards.size());
                                            player.role = tempListFinalCards.get(rand);
                                            listPlayer.put(player.user.getId(), player);
                                            tempListFinalCards.remove(rand);

                                            player.user.getPrivateChannel().block()
                                                    .createMessage("Looks like you are a: " + player.role.name).block();

                                        }
                                 

                                // TODO: distribute cards

                            }

    

                        }

                        /*
                         * addCard lässt jeden Player eine Karte aus den verfügbaren Karten eine Karte
                         * zur listDeckbuilder hinzufügen addCard überprüft bei jedem Aufruf ob
                         * listDeckbuilder nicht größer als listPlayers ist
                         */
                        // Der Command addCard fügt dem Deck eine vom User gewählte Karte hinzu
                        if (command.get(0).equalsIgnoreCase(prefix + "addCard")) {
                            var cardName = command.get(2);
                            var listName = command.get(1);
                            if (cardName == null || listName == null) {
                                channel.createMessage("syntax not correct").block();
                            } else {
                                var requestedCard = mapAvailableCards.get(cardName);
                                List<Card> requestedList = null;

                                // finds which list the user requests to be edited
                                if (listName.equalsIgnoreCase("CustomDeck") || listName.equalsIgnoreCase("PersonalDeck")
                                        || listName.equalsIgnoreCase("listCustomDeck")) {
                                    requestedList = listCustomDeck;
                                } else if (listName.equalsIgnoreCase("AlgorithmDeck")) {
                                    requestedList = listDeckbuilder;
                                }

                                // calls addCardToDeck and ercieves the Status message back
                                String message = addCardToDeck(requestedCard, requestedList);
                                channel.createMessage(message).block();
                                //shows new List with added Card
                                channel.createMessage(printCardList(requestedList)).block();

                                // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                                // und informiert den User über die Differenz
                                var figureDifference = Math.abs(requestedList.size() - listJoinedUsers.size());
                                if (figureDifference < 0) {
                                    channel.createMessage("Es gibt " + figureDifference + "Karten zu wenig").block();
                                } else if (figureDifference > 0) {
                                    channel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
                                }

                            }

                        }

                        // lässt den user eine Karte aus der gewünschten liste entfernen
                        // TODO: prüfe ob Karte im Deck ist
                        if (command.get(0).equalsIgnoreCase(prefix + "removeCard")) {
                            String cardName = command.get(1);
                            var requestedCard = mapAvailableCards.get(cardName);

                            // listDeckbuilder.get(i).name.equalsIgnoreCase(cardName);

                            listDeckbuilder.remove(requestedCard);
                            channel.createMessage("Die gewählte Karte wurde aus dem Deck entfernt").block();
                            channel.createMessage("**New Total Value**: " + Deckbuilder.totalCardValue(listDeckbuilder))
                                    .block();

                            // überprüft ob die Anzahl der Karten mit der Anzahl der Spieler übereinstimmt
                            // und informiert den User über die Differenz
                            var figureDifference = listDeckbuilder.size() - listJoinedUsers.size();
                            if (figureDifference < 0) {
                                channel.createMessage("Es gibt " + Math.abs(figureDifference) + "Karten zu wenig")
                                        .block();
                            } else if (figureDifference > 0) {
                                channel.createMessage("Es gibt " + figureDifference + "Karten zu viel").block();
                            }
                        }

                        // TODO: füge command startgame hinzu
                        /*
                         * startgame nimmt die .size der listPlayers und started damit den Deckbuilder
                         * algorithmus startgame überprüft ob listCustomDeck gleich lang wie listPlayer
                         * ist; aka ob jeder Player genau eine Karte hat
                         */

                    }

                });

        client.login().block();

    }

    //
    public static List<String> readJsonStringList(String filename) throws Exception {

        // reads filename.json into a JSONArray
        var jsonParser = new JSONParser();
        var listReader = new FileReader(filename);
        Object obj = jsonParser.parse(listReader);
        JSONArray jsonArray = (JSONArray) obj;
        System.out.println(jsonArray);

        // transforms the JSOArray into a List for easier handeling
        var list = new LinkedList<String>();
        for (var i = 0; i < jsonArray.size(); i++) {
            list.add(jsonArray.get(i).toString());
        }

        return list;
    }

    public static String printCardList(List<Card> list) {

        var messageList = "";

        if (list == null || list.isEmpty()) {
            messageList += "seems like this bitch empty";
        } else if (list != null) {
            messageList += "~~~~~~~~~~~~~~~~~" + list.getClass().getName() + "~~~~~~~~~~~~~~~~~  \n";

            for (int i = 0; i < list.size(); i++) {
                messageList += "**Karte " + (i + 1) + ":** " + list.get(i).name + " ----- **Value:** "
                        + list.get(i).value + "\n";
            }
            messageList += "**Total Value**: " + Deckbuilder.totalCardValue(list);
        }

        return (messageList);
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
}
