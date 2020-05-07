package wwBot;

import java.awt.Color;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {

    public static List<String> listPlayers = new ArrayList<>();
    public static List<Card> listCustomDeck = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        Globals.loadGlobals();
        var mapAvailableCards = Globals.mapAvailableCards;

        Deckbuilder.create(15);

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

                    String messageContent = event.getMessage().getContent().orElse("");
                    List<String> command = Arrays.asList(messageContent.split(" "));

                    // Schaut ob der erste Teil der Nachricht das Prefix ist
                    if (messageContent.startsWith(prefix)) {

                        var user = event.getMessage().getAuthor().get().getUsername();
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

                            if (listPlayers.indexOf(user) == -1) {
                                listPlayers.add(user);
                                channel.createMessage("joined").block();
                            } else {
                                channel.createMessage("looks like you're already joined").block();
                            }
                        }

                        // leave entfernt den user von listPlayers
                        if (command.get(0).equalsIgnoreCase(prefix + "leave")) {

                            if (listPlayers.indexOf(user) != -1) {
                                listPlayers.remove(user);
                                channel.createMessage("you left").block();
                            } else {
                                channel.createMessage("looks like you're already not in the game").block();
                            }
                        }

                        // nimmt die .size der listPlayers und started damit den Deckbuilder algorithmus
                        // übertprüft ob .size größer als 4 und kleiner als 50 ist
                        if (command.get(0).equalsIgnoreCase(prefix + "buildDeck")) {
                            if (listPlayers.size() > 4 || listPlayers.size() < 50) {
                                /* listCustomDeck = Deckbuilder.create(listPlayers.size()); */
                            } else {
                                channel.createMessage("noch nicht gemügend spieler wurden registriert").block();
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

                                });

                            } else {
                                channel.createMessage("card not found");
                            }

                        }

                        // TODO: füge command startgame hinzu
                        /*
                         * startgame nimmt die .size der listPlayers und started damit den Deckbuilder
                         * algorithmus startgame überprüft ob listCustomDeck gleich lang wie listPlayer
                         * ist; aka ob jeder Player genau eine Karte hat
                         */

                        // TODO: füge command addCard hinzu

                        /*
                         * addCard lässt jeden Player eine Karte aus den verfügbaren Karten eine Karte
                         * zur listCustomDeck hinzufügen addCard überprüft bei jedem Aufruf ob
                         * listCustomDeck nicht größer als listPlayers ist
                         */

                        // TODO: füge command removeCard hinzu
                        // removeCard entfernt die Karte aus listCustomDeck

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

}
