package wwBot.GameStates;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.Command;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.PrivateCommand;

public class Day {

    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<Snowflake, Player> mapVotes = new HashMap<>();
    public Map<Player, Integer> mapAmountVotes = new HashMap<>();

    Game game;

    Day(Game getGame) {
        game = getGame;
        registerDayCommands();
    }

    public void registerDayCommands() {

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! DayPhase").block();
        };
        mapCommands.put("ping", pingCommand);

        // Players can vote each day
        Command voteCommand = (event, parameters, msgChannel) -> {

            // if &vote is called, the programm saves a map with the person who voted for as
            // key
            // and the person voted for as value
            // before that, it checks if the person already voted and if true changes the
            // Vote
            var voter = event.getMessage().getAuthor().get();
            Player votedFor = null;

            // checks the syntax and finds the wanted player
            if (parameters != null && parameters.size() != 0) {
                for (var player : game.mapPlayers.entrySet()) {
                    if (player.getValue().user.getUsername().equalsIgnoreCase(parameters.get(0))) {
                        votedFor = player.getValue();
                    }
                }
            } else {
                // wrong syntax
                event.getMessage().getChannel().block().createMessage("Wrong syntax").block();
            }

            // if a player has been found, it checks if this player is alive
            if (votedFor == null) {
                event.getMessage().getChannel().block().createMessage("Player not found.").block();
            } else if (!votedFor.alive) {
                event.getMessage().getChannel().block()
                        .createMessage("The Person you Voted for is already dead (Seriously, give him a break)")
                        .block();
                // if the player is alive, he and the voter get put into a map (Key = voter,
                // Value = votedFor)
            } else if (votedFor.alive) {
                // if the same key gets put in a second time, the first value is dropped
                addVote(event, voter, votedFor);
            }

            countVotes();

        };
        mapCommands.put("vote", voteCommand);

    }

    // counts the votes and lynchs the player with the most
    private void countVotes() {
        // if every living player has voted, the votes get counted
        if (mapVotes.size() == game.livingPlayers.size()) {
            var amount = 0;
            var hasMajority = false;
            Player mostVoted = null;

            // searches for the person with the highest votes. If there are more than one
            // hasMajority gets set to false
            for (var player : mapAmountVotes.entrySet()) {
                if (amount > player.getValue()) {
                    mostVoted = player.getKey();
                    hasMajority = true;
                } else if (amount == player.getValue()) {
                    hasMajority = false;
                }
            }

            if (hasMajority && mostVoted != null) {
                lynch(mostVoted);
            }
        }
    }

    private void lynch(Player mostVoted) {

        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.RED, "Alle Spieler Haben Gewählt!",
                "Auf dem Schaffott steht *" + mostVoted.user.getMention()
                        + "* \nMit \"&lynch <Player>\" kannst du einen Spieler lynchen und damit die Rolle des Spielers offenbaren.");
        // kills the player
        PrivateCommand lynchCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("lynch")) {
                if (parameters.size() == 2) {
                    var unluckyPerson = Globals.findPlayerByName(parameters.get(1), game.livingPlayers);
                    if (unluckyPerson != null) {
                        lynchPlayer(unluckyPerson);
                        endDay();
                        return true;
                    } else if (parameters.get(1).equalsIgnoreCase("Nobody")) {
                        Globals.createEmbed(game.runningInChannel, Color.GREEN, "Niemand wurde gelyncht!",
                                "Mit einer Unsicherheit im Herzen gehen die Dorfbewohner schlafen.");
                        endDay();
                        return true;
                    } else {
                        msgChannel.createMessage("Player konnte nicht gefunden werden, probiere es noch einmal.")
                                .block();
                        return false;
                    }
                } else {
                    msgChannel.createMessage("wrong syntax, try again").block();
                    return false;
                }
            } else {
                return false;
            }
        };
        game.mapPrivateCommands.put(game.userModerator.getId(), lynchCommand);

    }

    private void endDay() {
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN,
                "Wenn bu bereit bist, den Tag zu beenden tippe den Command \"endDay\"", "");
        //
        PrivateCommand endDayCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("endDay")
                    && event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                game.currentGameState.changeDayPhase();
                MessagesMain.onNightAuto(game);

                return true;
            } else {
                return false;
            }
        };
        game.mapPrivateCommands.put(game.userModerator.getId(), endDayCommand);
    }

    private void lynchPlayer(Player player) {
        // 1) Message
        Globals.createEmbed(game.runningInChannel, Color.RED, player.user.getMention() + " wurde gelyncht!",
                "Er war ein: *" + player.role.name + "*!");
        // 2) player = dead
        player.alive = false;
        game.deadPlayers.add(player);

    }

    private void addVote(MessageCreateEvent event, User voter, Player votedFor) {
        mapVotes.put(voter.getId(), votedFor);

        var tempAmount = mapAmountVotes.get(votedFor);
        if (tempAmount != null) {
            tempAmount++;
            mapAmountVotes.put(votedFor, tempAmount);
        } else {
            mapAmountVotes.put(votedFor, 1);
        }
        event.getMessage().getChannel().block()
                .createMessage(voter.getMention() + " will, dass " + votedFor.user.getMention() + " gelyncht wird!")
                .block();
    }
}
