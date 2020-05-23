package wwBot.GameStates;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import wwBot.Card;
import wwBot.Game;
import wwBot.Player;
import wwBot.Interfaces.Command;

public class GameState {
    public Map<String, Command> gameStateCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);
    public Game game;

    protected GameState(Game game2) {
        game = game2;

    }

    public void exit() {
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

    public void killPlayer(Player unluckyPlayer, Card causedByRole) {

    }


}