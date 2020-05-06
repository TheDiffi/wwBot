package wwBot;

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

    public static void main(String[] args) throws Exception {

        //TODO: implementiere JSONReader (aus discordLoungeBot) ask David wenn du Sochen in an eigenen Datei permanent speichern willsch
        //TODO: erstelle eine Datei welche alle verfügbaren Karten beinhaltet und lade diese in eine Liste 









        // speichert den Prefix in einer Variable
        var prefix = "&";

        DiscordClient client = DiscordClientBuilder.create("NzA3NjUzNTk1NjQxOTM4MDMx.XrL8Cw.SRxT4UisfP6doLQoc-ZdgI5CtYY").build();

        // looks at every message
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {
                    // messageContent speichert den Inhalt der message in einer Variable
                    // command teilt diesen Inhalt bei einem Leerzeichen und speicher dies in einer
                    // Liste
                    String messageContent = event.getMessage().getContent().orElse("");
                    List<String> command = Arrays.asList(messageContent.split(" "));

                    // Schaut ob der erste Teil der Nachricht das Prefix ist
                    if (messageContent.startsWith(prefix)) {

                        var user = event.getMessage().getAuthor().get().getUsername();

                        // Liste der Commands, content und sonstige parameter werden überprüft
                        //TODO: gruppiere Commands nach LobbyPhase, GamePhase, GameEndPhase und lagere dies in funktion aus
                        //TODO: füge bedingung für jede Gruppe hinzu

                        // TODO: prefix aus parameter entfetnen sonst wird 2-mal prefix überprüft =
                        // schlampig

                        // ping testet ob der bot antwortet
                        if (command.get(0).equalsIgnoreCase(prefix + "ping")) {
                            event.getMessage().getChannel().block().createMessage("Pong!").block();
                        }



                        //Commands der Lobby Phase
                        // join füght den user zu listPlayers hinzu
                        if (command.get(0).equalsIgnoreCase(prefix + "join")) {

                            if (listPlayers.indexOf(user) == -1) {
                                listPlayers.add(user);
                                event.getMessage().getChannel().block().createMessage("joined").block();
                            } else {
                                event.getMessage().getChannel().block().createMessage("looks like you're already joined").block();
                            }
                        }

                        // leave entfernt den user von listPlayers
                        if (command.get(0).equalsIgnoreCase(prefix + "leave")) {

                            if (listPlayers.indexOf(user) != -1) {
                                listPlayers.remove(user);
                                event.getMessage().getChannel().block().createMessage("you left").block();
                            } else {
                                event.getMessage().getChannel().block().createMessage("looks like you're already not in the game").block();
                            }
                        }


                    //TODO: füge command startgame hinzu
                    /*startgame nimmt die .size der listPlayers und started damit den Deckbuilder algorithmus
                    startgame überprüft ob listCustomDeck gleich lang wie listPlayer ist; aka ob jeder Player genau eine Karte hat
                     */

                    //TODO: füge command addCard hinzu
                    
                    /* addCard lässt jeden Player eine Karte aus den verfügbaren Karten eine Karte zur listCustomDeck hinzufügen
                    addCard überprüft bei jedem Aufruf ob listCustomDeck nicht größer als listPlayers ist
                    */

                    //TODO: füge command removeCard hinzu
                    //removeCard entfernt die Karte aus listCustomDeck

                

                    //TODO: 
                    

                }
                
        }); 
        
    
        client.login().block();
    
    }

    //
    public static List<String> readJsonStringList (String filename) throws Exception{

        //reads filename.json into a JSONArray
        var jsonParser = new JSONParser();
        var listReader = new FileReader(filename);
        Object obj = jsonParser.parse(listReader);
        JSONArray jsonArray = (JSONArray) obj;
        System.out.println(jsonArray);
        

        //transforms the JSOArray into a List for easier handeling
        var list = new LinkedList<String>();
        for (var i = 0; i < jsonArray.size(); i++) {
            list.add(jsonArray.get(i).toString());
        }

        return list;
    }

    
    } 
    
    
    








