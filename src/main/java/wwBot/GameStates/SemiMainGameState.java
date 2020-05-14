package wwBot.GameStates;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.Command;
import wwBot.Globals;
import wwBot.Player;

public class SemiMainGameState extends GameState {

    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<Snowflake, Player>();

    public User userModerator;

    SemiMainGameState(wwBot.Game game) {
        super(game);
        registerStateCommands();
        mapPlayers = game.mapPlayers;
        userModerator = game.userModerator;

        // loads living Players for the first time
        livingPlayers = game.livingPlayers;
        for (var player : game.mapPlayers.entrySet()) {
            if (player.getValue().alive) {
                livingPlayers.put(player.getKey(), player.getValue());
            }
        }


    }

    private void registerStateCommands() {

        // shows the current Deck to the user
        Command showDeckCommand = (event, parameters, msgChannel) -> {
            // prints the deck
            msgChannel.createMessage(Globals.cardListToString(game.deck, "Deck")).block();

        };
        gameStateCommands.put("showDeck", showDeckCommand);

        // shows the moderator the list of players (alive or all)
        Command printListCommand = (event, parameters, msgChannel) -> {
        var param = parameters.get(0);

            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
                //if the user typed "Players" it prints a list of all players, if he typed "Living" it prints only the living players
                if(param.equalsIgnoreCase("Players")){
                printMapPlayers(msgChannel, mapPlayers);
                } else if(param.equalsIgnoreCase("Living")){
                    printMapPlayers(msgChannel, livingPlayers);
                }
            } else {
                msgChannel.createMessage("only the moderator can use this command").block();
            }
        };
        gameStateCommands.put("printList", printListCommand);


    }

    private void printMapPlayers(MessageChannel msgChannel, Map<Snowflake, Player> map) {
        var mssgList = "";
        for (var playerset : map.entrySet()) {
            var player = playerset.getValue();
            mssgList += player.user.getUsername() + ": ";
            mssgList += "ROLE(" + player.role.name + ") ";
            mssgList += Boolean.toString(player.alive) + "\n";
        }
        Globals.createEmbed(msgChannel, Color.DARK_GRAY, "Liste aller Spieler", mssgList);
    }

}