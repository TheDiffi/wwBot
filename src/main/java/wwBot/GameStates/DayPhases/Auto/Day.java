package wwBot.GameStates.DayPhases.Auto;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.Interfaces.Command;

public class Day extends AutoDayPhase {
    public Map<Player, Player> mapVotes = new HashMap<>();
    public Map<Player, Double> mapAmountVotes = new HashMap<>();
    public Game game;

    public Day(Game getGame) {
        game = getGame;

        // loads all Commands into the mapCommands
        registerDayCommands();

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
            MessagesMain.sendHelpDay(msgChannel, true);
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
            for (var player : game.livingPlayers.values()){
                if (player.user.getUsername().equals(voterUser.getUsername())) {
                    allowedToVote = true;
                    voter = player;
                    break;
                }
            }
            if (!allowedToVote) {
                MessagesMain.errorNotAllowedToVote(msgChannel);

            } else if (allowedToVote) {
                // finds the wanted player
                Player votedFor = null;

                // checks the syntax
                if (parameters == null || parameters.size() != 1) {
                    // wrong syntax
                    MessagesMain.errorWrongSyntax(msgChannel);
                } else {

                    // removes the dash
                    var recievedName = Globals.removeDash(parameters.get(0));

                    // if the user votes for no one none
                    if (isEmptyPlayer(recievedName)) {
                        votedFor = registerEmptyPlayer();
                    } else {
                        // finds the player
                        votedFor = game.findPlayerByNameLiving(recievedName);
                    }

                }

                // if a player has been found, it checks if this player is alive
                if (votedFor == null) {
                    MessagesMain.errorPlayerNotFound(msgChannel);
                } else if (!votedFor.role.deathDetails.alive) {
                    MessagesMain.errorPlayerAlreadyDead(msgChannel);

                    // if the player is alive, calls addVote
                    // if the same key gets put in a second time, the first value is dropped
                } else {
                    addVote(voter, votedFor);
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
            MessagesMain.voteNobody(game, voter);

        } else {
            MessagesMain.votePlayer(game, voter, canidate);
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
                //
                if (!entry.getKey().name.equals("Nobody")) {
                    if (votes == entry.getValue()) {
                        hasMajority = false;
                    } else if (votes < entry.getValue()) {
                        mostVoted = entry.getKey();
                        hasMajority = true;
                        votes = entry.getValue();
                    }
                }
            }

            if (!hasMajority && mostVoted != null) {
                MessagesMain.voteButNoMajority(game);

            } else if (hasMajority && mostVoted != null) {
                checkLynchConditions(mostVoted);
            } else if (mostVoted == null) {
                game.gameState.killPlayer(null, "Dorfbewohner");
            }
        }
    }

    private void checkLynchConditions(Player mostVoted) {

        MessagesMain.announceMajority(game, mostVoted, mapVotes);

        // waits for a bit for suspense
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            game.mainChannel.createMessage("error in: Day -> Thread.sleep();");
            e.printStackTrace();
        }

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
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            game.mainChannel.createMessage("error in: Day -> Thread.sleep(1000);");
            e.printStackTrace();
        }

        game.gameState.changeDayPhase(DayPhase.NORMAL_NIGHT);
    }

    // returns an empty player type
    private Player registerEmptyPlayer() {
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