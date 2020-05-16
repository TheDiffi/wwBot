package wwBot.GameStates;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.Command;
import wwBot.Game;
import wwBot.Player;

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

//counts the votes and lynchs the player with the most
    private void countVotes() {
        // if every living player has voted, the votes get counted
        if (mapVotes.size() == game.livingPlayers.size()) {
            var amount = 0;
            var hasMajority = false;
            Player mostVoted = null;

            //searches for the person with the highest votes. If there are more than one hasMajority gets set to false
            for (var player : mapAmountVotes.entrySet()) {
                if(amount > player.getValue()){
                    mostVoted = player.getKey();
                    hasMajority = true;
                }else if (amount == player.getValue()){
                    hasMajority = false;
                }
            }

            if(hasMajority && mostVoted != null){
                lynch(mostVoted);
            }
        }
    }

    private void lynch(Player mostVoted) {

        if (game.currentGameState.mapExistingRoles.containsKey("Märtyrerin")){

        }else{
            killPlayer(mostVoted);
        }
    }

    private void killPlayer(Player player) {
        //1) Message

        //2) player = dead
        player.alive = false;

        //3) Check consequences
        if(player.inLoveWith != null){
            //announces the lovers and kills the other person
            //TODO: announce
            player.inLoveWith.alive = false;
            
        }

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
                .createMessage(
                        voter.getMention() + " will, dass " + votedFor.user.getMention() + " gelyncht wird!")
                .block();
    }
}
