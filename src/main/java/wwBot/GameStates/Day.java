package wwBot.GameStates;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.Interfaces.Command;

public class Day {

    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<Player, Player> mapVotes = new HashMap<>();
    public Map<Player, Double> mapAmountVotes = new HashMap<>();
    Player emptyPlayer;
    Game game;

    Day(Game getGame) {
        game = getGame;

        // loads all Commands into the mapCommands
        registerDayCommands();

        // registers an empty Player; neccessary for the voting system
        emptyPlayer = new Player();
        emptyPlayer.name = "Nobody";

    }

    // --------------------- Commands ------------------------

    // loads all of the following Commands into mapCommands
    public void registerDayCommands() {
        var mapRegisteredCards = Globals.mapRegisteredCards;

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! DayPhase").block();
        };
        mapCommands.put("ping", pingCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            MessagesMain.helpDayPhase(event);

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
            for (var entry : game.livingPlayers.entrySet()) {
                if (entry.getValue().user.getUsername().equals(voterUser.getUsername())) {
                    allowedToVote = true;
                    voter = entry.getValue();
                    break;
                }
            }

            if (allowedToVote) {
                // finds the wanted player
                Player votedFor = null;
                var recievedName = "";
                // checks the syntax
                if (parameters != null && parameters.size() != 0) {

                    //removes the dash
                    recievedName = Globals.removeDash(parameters.get(0));

                    // if the user votes for no one none
                    if (recievedName.equalsIgnoreCase("no one") || recievedName.equalsIgnoreCase("niemand")
                            || recievedName.equalsIgnoreCase("null") || recievedName.equalsIgnoreCase("nobody") || recievedName.equalsIgnoreCase("none")) {
                        votedFor = emptyPlayer;
                    } else {
                        // finds the player
                        votedFor = Globals.findPlayerByName(recievedName, game.livingPlayers, game);
                    }

                } else {
                    // wrong syntax
                    MessagesMain.errorWrongSyntax(game, msgChannel);
                }

                // if a player has been found, it checks if this player is alive
                if (votedFor == null) {
                    MessagesMain.errorPlayerNotFound(msgChannel);
                } else if (!votedFor.alive) {
                    event.getMessage().getChannel().block()
                            .createMessage("The Person you Voted for is already dead (Seriously, give him a break)")
                            .block();

                    // if the player is alive, calls addVote
                    // if the same key gets put in a second time, the first value is dropped
                } else if (votedFor.alive) {
                    addVote(event, voter, votedFor);
                }

                // counts the votes: checks if all players have voted and if there is a mojority
                countVotes();

            } else if (!allowedToVote) {
                MessagesMain.errorNotAllowedToVote(game, msgChannel);

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
                    var causedByRole = mapRegisteredCards.get("Dorfbewohner");
                    var unluckyPerson = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)),
                            game.livingPlayers, game);
                    if (unluckyPerson != null) {
                        // checks if the player is alive
                        if (unluckyPerson.alive) {
                            // calls check if dies to consider special roles
                            if (game.gameState.checkIfDies(unluckyPerson, causedByRole)) {
                                // lynch kills the player with "Dorfbewohner" being the cause
                                game.gameState.killPlayer(unluckyPerson, causedByRole);
                                // checks if the conditions for GameOver are met
                                game.gameState.checkIfGameEnds();
                                msgChannel.createMessage("Done! Du kannst den Tag mit \"&EndDay\" den Tag beenden")
                                        .block();
                            }
                        } else {
                            MessagesMain.errorPlayerAlreadyDead(game, msgChannel);
                        }

                    } else {
                        MessagesMain.errorPlayerNotFound(msgChannel);
                    }

                } else {
                    MessagesMain.errorWrongSyntax(game, msgChannel);
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
                MessagesMain.errorModOnlyCommand(msgChannel);
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
                Globals.createMessage(game.mainChannel,
                        "Alle Spieler haben abgestimmt, jedoch gibt es **keine klare Mehrheit**. Es wird gebete, dass wenigstens ein Spieler seine Stimme ändert, damit es zu einer klaren Mehrheit kommt.",
                        false);
            }

            if (hasMajority && mostVoted != null) {
                suggestMostVoted(mostVoted);
            }
        }
    }

    private void addVote(MessageCreateEvent event, Player voter, Player votedFor) {
        // der Vote des Bürgermeisters zählt mehr
        var voteValue = voter.role.name.equalsIgnoreCase("Bürgermeister") ? 1.5d : 1d;

        // if the voter already voted once, the old voteAmount gets removed
        if (mapVotes != null && mapVotes.containsKey(voter)) {
            var originallyVotedFor = mapVotes.get(voter);
            var lessVotes = mapAmountVotes.get(originallyVotedFor) - 1d;
            mapAmountVotes.put(originallyVotedFor, lessVotes);

        }

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
            Globals.createMessage(game.mainChannel, voter.user.getMention() + " will niemanden lynchen.", false);
        } else {
            Globals.createMessage(game.mainChannel,
                    voter.user.getMention() + " will, dass " + votedFor.user.getMention() + " gelyncht wird!", false);
        }

    }

    private void suggestMostVoted(Player mostVoted) {
        // suggests the most Voted Player to the Mod
        MessagesMain.suggestMostVoted(game, mostVoted);
        // gibt ein Embed mit den Votes aus
        var mssg = "";
        for (var entry : mapVotes.entrySet()) {
            mssg += entry.getKey().name + " hat für " + entry.getValue().name + " abgestimmt \n";
        }

        Globals.createEmbed(game.mainChannel, Color.WHITE,
                "Die Würfel sind gefallen \nAuf dem Schafott steht: " + mostVoted.name, mssg);
        // some cards can interfere in this stage (Prinz, Märtyrerin)
        checkLynchConditions();

    }

    // --------------------- Other ------------------------

    private void checkLynchConditions() {
        for (var entry : game.livingPlayers.entrySet()) {
            if (entry.getValue().role.name.equalsIgnoreCase("Märtyrerin")) {
                Globals.createMessage(game.userModerator.getPrivateChannel().block(),
                        "Vergiss nicht die Märtyrerin zu fragen ob sie sich anstelle der nominierten Person lynchen lassen will.",
                        false);
                break;
            }
        }
    }

    public void setMuteAllPlayers(Map<Snowflake, Player> mapPlayers, boolean isMuted) {
        // mutes all players at night
        for (var player : mapPlayers.entrySet()) {
            try {
                player.getValue().user.asMember(game.server.getId()).block().edit(a -> {
                    a.setMute(isMuted).setDeafen(false);
                }).block();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void endDay() {

        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "");
        game.gameState.changeDayPhase();

    }

}
