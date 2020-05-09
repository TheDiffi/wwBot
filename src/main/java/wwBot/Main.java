package wwBot;

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


        Game testGame = new Game();
        

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
                    List<String> parameters = Arrays.asList(messageContent.split(" "));


                    // Schaut ob der erste Teil der Nachricht das Prefix ist
                    if (messageContent.startsWith(prefix)) {

                        testGame.handleCommands(event);

                        User user = event.getMember().get();
                        var channel = event.getMessage().getChannel().block();

                    

                      

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



    public static String addCardToDeck(Card card, List<Card> list) {

        var message = "";
        boolean existing = false;
        // überprüft ob die karte unique ist, falls ja, wird überprüft ob die Karte
        // bereis im Deck ist (falls die liste leer ist geht es direkt zu else if wo die karte direkt hinzugefügt wird)
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
}
