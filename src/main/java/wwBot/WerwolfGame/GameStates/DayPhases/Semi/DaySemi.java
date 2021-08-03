package wwBot.WerwolfGame.GameStates.DayPhases.Semi;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.GameState;
import wwBot.WerwolfGame.GameStates.MainState.DayPhase;

public class DaySemi {

    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<Player, Player> mapVotes = new HashMap<>();
    public Map<Player, Double> mapAmountVotes = new HashMap<>();
    Game game;
    GameState gameState;

    public DaySemi(Game getGame) {
        game = getGame;
        gameState = game.gameState;

        // loads all Commands into the mapCommands
        registerDayCommands();

        MessagesWW.onDaySemi(game);

    }

    // --------------------- Commands ------------------------

    // loads all of the following Commands into mapCommands
    public void registerDayCommands() {

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! DayPhase").block();
        };
        mapCommands.put("ping", pingCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            // replies to the moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                MessagesWW.sendHelpDayMod(msgChannel);
            } else {
                MessagesWW.sendHelpDay(msgChannel, false);
            }

        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);

        // gibt ein Embed mit den Votes aus
        Command listVotesCommand = (event, parameters, msgChannel) -> {

            var mssg = "";
            for (var entry : mapVotes.entrySet()) {
                mssg += entry.getKey().name + " hat für " + entry.getValue().name + " abgestimmt \n";
            }
            Globals.createEmbed(msgChannel, Color.WHITE, "Gewählt haben ", mssg);
        };
        mapCommands.put("votes", listVotesCommand);
        mapCommands.put("listvotes", listVotesCommand);
        mapCommands.put("showvotes", listVotesCommand);

        // if &vote is called, the programm saves a map with the person who voted for as
        // key and the person voted for as value
        // an previous vote by the same person gets overwritten
        Command voteCommand = (event, parameters, msgChannel) -> {

            var voterUser = event.getMessage().getAuthor().get();
            var allowedToVote = false;
            var voter = new Player();

            // checks if the player calling this command is allowed to vote
            for (var player : game.livingPlayers.values()) {
                if (player.user.getUsername().equals(voterUser.getUsername())) {
                    allowedToVote = true;
                    voter = player;
                    break;
                }
            }

            if (allowedToVote) {
                // finds the wanted player
                Player votedFor = null;
                var recievedName = "";
                // checks the syntax
                if (parameters != null && parameters.size() != 0) {

                    // removes the dash
                    recievedName = Globals.removeDash(parameters.get(0));

                    // if the user votes for no one none
                    if (isEmptyPlayer(recievedName)) {
                        votedFor = getEmptyPlayer();
                    } else {
                        // finds the player
                        votedFor = game.findPlayerByNameLiving(recievedName);
                    }

                } else {
                    // wrong syntax
                    MessagesWW.errorWrongSyntax(msgChannel);
                }

                // if a player has been found, it checks if this player is alive
                if (votedFor == null) {
                    MessagesWW.errorPlayerNotFound(msgChannel);
                } else if (!votedFor.role.deathDetails.alive) {
                    MessagesWW.errorPlayerAlreadyDead(msgChannel);

                    // if the player is alive, calls addVote
                    // if the same key gets put in a second time, the first value is dropped
                } else if (votedFor.role.deathDetails.alive) {
                    addVote(event, voter, votedFor);
                }

                // counts the votes: checks if all players have voted and if there is a mojority
                countVotes();

            } else if (!allowedToVote) {
                MessagesWW.errorNotAllowedToVote(msgChannel);

            }
        };
        mapCommands.put("vote", voteCommand);

        // lynch calls killPlayer() as killed by the villagers
        Command lynchCommand = (event, parameters, msgChannel) -> {
            // checks if the moderator called this command
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                // checks the syntax
                if (parameters != null && parameters.size() == 1) {
                    // finds the wanted player by name
                    var unluckyPerson = game.findPlayerByNameLiving(parameters.get(0));
                    if (unluckyPerson != null) {
                        // checks if the player is alive
                        if (unluckyPerson.role.deathDetails.alive) {
                            // calls check if dies to consider special roles
                            if (game.gameState.checkIfDies(unluckyPerson, "Dorfbewohner")) {
                                // lynch kills the player with "Dorfbewohner" being the cause
                                game.gameState.killPlayer(unluckyPerson, "Dorfbewohner");
                                // checks if the conditions for GameOver are met
                                game.gameState.checkIfGameEnds();
                                msgChannel.createMessage("Done! Du kannst den Tag mit \"&EndDay\" den Tag beenden")
                                        .block();
                            }
                        } else {
                            MessagesWW.errorPlayerAlreadyDead(msgChannel);
                        }

                    } else {
                        MessagesWW.errorPlayerNotFound(msgChannel);
                    }

                } else {
                    MessagesWW.errorWrongSyntax(msgChannel);
                }
            }
        };
        mapCommands.put("lynch", lynchCommand);

        // calls endDay()
        Command endDayCommand = (event, parameters, msgChannel) -> {
            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                endDay();
            } else {
                MessagesWW.errorModOnlyCommand(msgChannel);
            }
        };
        mapCommands.put("endDay", endDayCommand);

    }

    // --------------------- Voting System ------------------------

    // counts the votes and lynchs the player with the most
    private void countVotes() {
        // if every living player has voted, the votes get counted
        if (mapVotes.size() >= game.livingPlayers.size()) {
            var amount = 0d;
            var hasMajority = false;
            Player mostVoted = null;

            // searches for the person with the highest votes. If there are more than one
            // hasMajority gets set to false
            for (var entry : mapAmountVotes.entrySet()) {
                //
                if (!entry.getKey().name.equals("Nobody")) {
                    if (amount == entry.getValue()) {
                        hasMajority = false;
                    } else if (amount < entry.getValue()) {
                        mostVoted = entry.getKey();
                        hasMajority = true;
                        amount = entry.getValue();
                    }
                }
            }

            if (!hasMajority && mostVoted != null) {
                MessagesWW.voteResultNoMajority(game);
            }

            if (hasMajority && mostVoted != null) {
                suggestMostVoted(mostVoted);
            }
        }
    }

    private void addVote(MessageCreateEvent event, Player voter, Player votedFor) {
        // der Vote des Bürgermeisters zählt mehr
        var voteValue = voter.role.name.equalsIgnoreCase("Bürgermeister") ? 1.5d : 1d;

        deleteOldVote(voter, voteValue);

        // adds the Vote to the vote Amount
        var tempAmount = mapAmountVotes.get(votedFor);
        if (tempAmount != null && tempAmount > 0) {
            tempAmount += voteValue;
            mapAmountVotes.put(votedFor, tempAmount);
        } else {
            mapAmountVotes.put(votedFor, voteValue);
        }

        // registers who voted for who
        mapVotes.put(voter, votedFor);
        if (votedFor.name.equals("Nobody")) {
            MessagesWW.voteNobody(game, voter);

        } else {
            MessagesWW.votePlayer(game, voter, votedFor);
        }

    }

    public void deleteOldVote(Player voter, double voteValue) {
        // if the voter already voted once, the old voteAmount gets removed
        if (mapVotes != null && mapVotes.containsKey(voter)) {
            var originalVictim = mapVotes.get(voter);
            mapAmountVotes.put(originalVictim, mapAmountVotes.get(originalVictim) - voteValue);

        }
        mapVotes.remove(voter);

    }

    private void suggestMostVoted(Player mostVoted) {
        // suggests the most Voted Player to the Mod
        MessagesWW.announceMajority(game, mostVoted, mapVotes);
        // gibt ein Embed mit den Votes aus

        // some cards can interfere in this stage (Prinz, Märtyrerin)
        checkLynchConditions();

    }

    private void checkLynchConditions() {

        if (game.gameState.mapExistingRoles.containsKey("Prinz")) {
            MessagesWW.remindAboutPrinz(game);
        }
        if (game.gameState.mapExistingRoles.containsKey("Märtyrerin")) {
            MessagesWW.remindAboutMärtyrerin(game);

        }

    }

    // --------------------- Other ------------------------

    private void endDay() {

        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "Switching to Night");
        game.gameState.changeDayPhase(DayPhase.NORMAL_NIGHT);

    }

    // returns an empty player type
    private Player getEmptyPlayer() {
        var emptyPlayer = new Player();
        emptyPlayer.name = "Nobody";
        return emptyPlayer;
    }

    private boolean isEmptyPlayer(String name) {
        switch (name.toLowerCase()) {
            case "no one":
                return true;
            case "niemand":
                return true;
            case "keiner":
                return true;
            case "nobody":
                return true;
            case "null":
                return true;
            case "none":
                return true;
            default:
                return false;

        }
    }

}
