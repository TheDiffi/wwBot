package wwBot.WerwolfGame.GameStates.DayPhases.Auto;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import discord4j.core.object.entity.Channel;
import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.MainState.DayPhase;

public class Day extends AutoDayPhase {
    public Map<Player, Player> mapVotes = new HashMap<>();
    public Map<Player, Double> mapAmountVotes = new HashMap<>();
    public Game game;
    private AutoState state;

    public Day(Game getGame) {
        game = getGame;
        state = (AutoState) game.gameState;

        if (state.villageAgitated) {
            MessagesWW.announceUnruhe(game);
        }

        // loads all Commands into the mapCommands
        registerDayCommands();

    }

    // TODO: if majority votes for nobody, nobody dies

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
            MessagesWW.sendHelpDay(msgChannel, true);
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
            Player voter = null;

            // checks if the player calling this command is allowed to vote
            for (var player : game.livingPlayers.values()) {
                if (player.user.getUsername().equals(voterUser.getUsername())) {
                    allowedToVote = true;
                    voter = player;
                    break;
                }
            }
            if (!allowedToVote) {
                MessagesWW.errorNotAllowedToVote(msgChannel);

            } else {
                // finds the wanted player
                Player votedFor = null;
                // checks the syntax
                if (parameters == null || parameters.size() != 1) {
                    // wrong syntax
                    MessagesWW.errorWrongSyntax(msgChannel);
                } else {

                    // removes the dash
                    var recievedName = Globals.removeDash(parameters.get(0));

                    // if the user votes for no one none
                    if (isEmptyPlayer(recievedName)) {
                        votedFor = registerEmptyPlayer();
                    } else {
                        // finds the player
                        votedFor = game.findPlayerByName(recievedName);
                    }

                }

                // if a player has been found, it checks if this player is alive
                if (votedFor == null) {
                    MessagesWW.errorPlayerNotFound(msgChannel);
                } else if (votedFor.role != null && !votedFor.role.deathDetails.alive) {
                    MessagesWW.errorPlayerAlreadyDead(msgChannel);

                    // if the player is alive, calls addVote
                    // if the same key gets put in a second time, the first value is dropped
                } else {
                    addVote(voter, votedFor);
                }

                // removes the original message
                if (msgChannel.getType() != Channel.Type.DM) {
                    event.getMessage().delete().block();
                }

                // counts the votes: checks if all players have voted and if there is a mojority
                countVotes();

            }
        };
        mapCommands.put("vote", voteCommand);

    }

    // ----------------- Voting System ----------------

    private void addVote(Player voter, Player canidate) {
        // der Vote des Bürgermeisters zählt mehr
        var voteValue = voter.role.name.equalsIgnoreCase("Bürgermeister") ? 1.5d : 1d;

        // if the voter already voted once, the old voteAmount gets removed
        if (mapVotes != null && mapVotes.containsKey(voter)) {
            var originalVictim = mapVotes.get(voter);
            mapAmountVotes.put(originalVictim, mapAmountVotes.get(originalVictim) - voteValue);

        }

        // adds the Vote to the vote Amount
        var totalVotes = mapAmountVotes.get(canidate);
        if (totalVotes == null || totalVotes == 0) {
            mapAmountVotes.put(canidate, voteValue);

        } else {
            totalVotes += voteValue;
            mapAmountVotes.put(canidate, totalVotes);
        }

        // registers who voted for whom
        mapVotes.put(voter, canidate);

        // sends the response
        if (canidate.name.equals("Nobody")) {
            MessagesWW.voteNobody(game, voter);

        } else {
            MessagesWW.votePlayer(game, voter, canidate);
        }

    }

    // counts the votes and lynchs the player with the most
    private void countVotes() {
        // if every living player has voted, the votes get counted
        if (mapVotes.size() == game.livingPlayers.size()) {
            var votes = 0d;
            var hasMajority = false;
            Player mostVoted = null;

            // searches for the person with the highest votes. If there are more than one
            // hasMajority gets set to false
            for (var entry : mapAmountVotes.entrySet()) {
                // TODO: if mojority votes nobody, nobody dies
                if (votes == entry.getValue()) {
                    hasMajority = false;
                } else if (votes < entry.getValue()) {
                    mostVoted = entry.getKey();
                    hasMajority = true;
                    votes = entry.getValue();
                }

            }

            if (!hasMajority && mostVoted != null) {
                MessagesWW.voteResultNoMajority(game);

            } else if (hasMajority && mostVoted != null && mostVoted.name == "Nobody") {
                MessagesWW.voteResultNobody(game);

            } else if (hasMajority && mostVoted != null) {
                checkLynchConditions(mostVoted);
            }
        }
    }

    private void checkLynchConditions(Player mostVoted) {

        MessagesWW.announceMajority(game, mostVoted, mapVotes);

        // waits for a bit for suspense
        Globals.sleepWCatch(2000);

        // MÄRTYRERIN
        if (game.gameState.mapExistingRoles.containsKey("Märtyrerin")) {

            var player = game.gameState.mapExistingRoles.get("Märtyrerin").get(0);
            player.role.executePreWW(mostVoted, game, (AutoState) game.gameState);

        } else {
            lynchPlayer(mostVoted, false);
        }

    }

    public void lynchPlayer(Player victim, boolean hasSacrificed) {

        var cause = hasSacrificed ? "Märtyrerin" : "Dorfbewohner";

        // calling killPlayer() with "Dorfbewohner" means lynching him
        game.gameState.killPlayer(victim, cause);

        // waits for a bit b4 changing to night
        Globals.sleepWCatch(5000);

        endDay();
    }

    private void endDay() {

        // Unruhestifterin
        if (state.villageAgitated) {
            // reset
            resetVotes();
            state.villageAgitated = false;

        } else {
            game.gameState.changeDayPhase(DayPhase.NORMAL_NIGHT);
        }

    }

    private void resetVotes() {
        mapVotes.clear();
        mapAmountVotes.clear();

        MessagesWW.secondVote(game);
    }

    // returns an empty player type
    private Player registerEmptyPlayer() {
        Player emptyPlayer = null;
        for (var entry : mapAmountVotes.entrySet()) {
            if (entry.getKey().name.equals("Nobody")) {
                emptyPlayer = entry.getKey();
            }
        }

        if (emptyPlayer == null) {
            emptyPlayer = new Player();
            emptyPlayer.name = "Nobody";
        }

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