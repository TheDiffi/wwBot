package wwBot.GameStates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Player;
import wwBot.GameStates.DayPhases.Day;
import wwBot.GameStates.DayPhases.FirstNight;
import wwBot.GameStates.DayPhases.Morning;
import wwBot.GameStates.DayPhases.Night;
import wwBot.Interfaces.Command;

public class GameState {


    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<Snowflake, Player>();
    public Map<String, Command> gameStateCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);

	public Day day = null;
	public Night night = null;
	public Morning morning = null;
	public FirstNight firstNight = null;
    public DayPhase dayPhase = DayPhase.FIRST_NIGHT;

    public TextChannel wwChat = null;
    public TextChannel deathChat = null;
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

    public void changeDayPhase() {
    }

    public void endMainGame(int winner) {
    }

    public TextChannel createWerwolfChat() {
        return null;
    }

    public void deleteWerwolfChat() {
    }

    public TextChannel createDeathChat() {
        return null;
    }

    public void deleteDeathChat() {
    }

    public void killPlayer(Player unluckyPlayer, String causedByRole) {
    }

    // checks the conditions if the player dies
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



}