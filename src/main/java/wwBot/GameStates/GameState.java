package wwBot.GameStates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Player;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.Interfaces.Command;

public class GameState {

    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<Snowflake, Player>();
    public Map<String, Command> gameStateCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);

    protected Game game;

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

    public boolean killPlayer(Player unluckyPlayer, String causedByRole) {
        return false;
    }   

    public boolean checkIfDies(Player unluckyPlayer, String causedByRole) {
        return false;
    }

    public boolean checkIfGameEnds() {
        return false;
    }

    public boolean exit() {
        return true;
    }

	public void start() {
	}

    

}