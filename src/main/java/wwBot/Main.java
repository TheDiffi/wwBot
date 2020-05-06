package wwBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {

    public static List<String> listPlayers = new ArrayList<>();

    public static void main(String[] args) throws Exception {

       







        // speichert den Prefix in einer Variable
        var prefix = "&";

        DiscordClient client = DiscordClientBuilder
                .create("NzA3NjUzNTk1NjQxOTM4MDMx.XrL8Cw.SRxT4UisfP6doLQoc-ZdgI5CtYY").build();

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
                        // TODO: prefix aus parameter entfetnen sonst wird 2-mal prefix überprüft =
                        // schlampig

                        // ping testet ob der bot antwortet
                        if (command.get(0).equalsIgnoreCase(prefix + "ping")) {
                            event.getMessage().getChannel().block().createMessage("Pong!").block();
                        }

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
                    /*startgame nimmt die .size der listPlayers und started damit den Teambuilder algorithmus
                    startgame überprüft ob listCustomTeam gleich lang wie listPlayer ist; aka ob jeder Player genau eine Karte hat
                     */

                    //TODO: füge command addCard hinzu
                    
                    /* addCard lässt jeden Player eine Karte aus den verfügbaren Karten eine Karte zur listCustomTeam hinzufügen
                    addCard überprüft bei jedem Aufruf ob listCustomTeam nicht größer als listPlayers ist
                    */

                    //TODO: füge command removeCard hinzu
                    //removeCard entfernt die Karte aus listCustomTeam

                

                    //TODO: 
                    

                }
                
        }); 
        
    
        client.login().block();
    
    }


    
    } 
    
    
    








