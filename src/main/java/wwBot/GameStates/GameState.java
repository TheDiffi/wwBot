package wwBot.GameStates;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.Interfaces.Command;

public class GameState {

    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<Snowflake, Player>();
    public Map<String, Command> gameStateCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);

    public Game game;

    protected GameState(Game game2) {
        game = game2;
    }

    public boolean handleCommand(String requestedCommand, MessageCreateEvent event, List<String> parameters,
            MessageChannel runningInChannel) {
        var foundCommandState = gameStateCommands.get(requestedCommand);
        if (foundCommandState != null) {
            foundCommandState.execute(event, parameters, runningInChannel);
            return true;
        }

        return false;
    }

    public void changeDayPhase(DayPhase nextPhase) {
    }

    public void createWerwolfChat() {
    }

    public void deleteWerwolfChat() {
    }

    public void createDeathChat() {
    }

    public void deleteDeathChat() {
    }

    public void killPlayer(Player unluckyPlayer, String causedByRole) {
    }

    public boolean checkIfDies(Player unluckyPlayer, String causedByRole) {
        return false;
    }

    public boolean checkIfGameEnds() {
        return false;
    }

    public void setMuteAllPlayers(Map<Snowflake, Player> mapPlayers, boolean isMuted) {
    }

    public boolean exit() {
        return true;
    }

    public void endMainGame(int winner) {
        // unmutes all players
        setMuteAllPlayers(mapPlayers, false);
        // deletes deathChat
        deleteDeathChat();
        // sends gameover message
        if (winner == 1) {
            Globals.createEmbed(game.mainChannel, Color.GREEN, "GAME END: DIE DORFBEWOHNER GEWINNEN!", "");
        } else if (winner == 2) {
            Globals.createEmbed(game.mainChannel, Color.RED, "GAME END: DIE WERWÖLFE GEWINNEN!", "");
        }
        // changes gamestate
        game.changeGameState(new PostGameState(game, winner));
    }

}