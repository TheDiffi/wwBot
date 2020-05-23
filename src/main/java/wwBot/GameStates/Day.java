package wwBot.GameStates;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;

public class Day {

    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    public Map<Player, Player> mapVotes = new HashMap<>();
    public Map<Player, Double> mapAmountVotes = new HashMap<>();
    Game game;

    Day(Game getGame) {
        game = getGame;
        registerDayCommands();

    }

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


        // registers the vote command
        Command voteCommand = (event, parameters, msgChannel) -> {

            // if &vote is called, the programm saves a map with the person who voted for as
            // key and the person voted for as value
            // checks if the person already voted and if true changes the Vote
            var voterUser = event.getMessage().getAuthor().get();
            var allowedToVote = false;
            var voter = new Player();

            // checks if the player calling this command is allowed to vote
            for (var entry : game.livingPlayers.entrySet()) {
                if (entry.getValue().user.getUsername().equals(voterUser.getUsername())) {
                    allowedToVote = true;
                    voter = entry.getValue();
                }
            }

            if (allowedToVote) {
                Player votedFor = null;
                var recievedName = "";
                // checks the syntax and finds the wanted player
                if (parameters != null && parameters.size() != 0) {
                    recievedName = Globals.removeDash(parameters.get(0));
                    //finds the player
                    votedFor = Globals.findPlayerByName(recievedName, game.livingPlayers, game);

                } else {
                    // wrong syntax
                    event.getMessage().getChannel().block().createMessage("Wrong syntax").block();
                }

                // if a player has been found, it checks if this player is alive
                if (votedFor == null) {
                    event.getMessage().getChannel().block().createMessage("Player not found.\nWenn der Spielername ein Leerzeichen enthält, ersetze diesen durch einen Bindestrich (-)").block();
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
                // counts the votes: checks if all players have voted and if there is a mojority
                countVotes();
            } else if (!allowedToVote) {
                event.getMessage().getChannel().block().createMessage("You are not allowed to vote!").block();

            }
        };
        mapCommands.put("vote", voteCommand);

        // lynch calls killPlayer() as killed by the villagers
        Command lynchCommand = (event, parameters, msgChannel) -> {
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                if (parameters != null && parameters.size() == 1) {
                    var unluckyPerson = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.livingPlayers, game);
                    if (unluckyPerson != null) {
                        game.gameState.killPlayer(unluckyPerson, mapRegisteredCards.get("Dorfbewohner"));
                        msgChannel.createMessage("Done! Du kannst den Tag mit \"&EndDay\" den Tag beenden").block();
                    } else {
                        msgChannel.createMessage("Player konnte nicht gefunden werden, probiere es noch einmal.")
                                .block();
                    }
                } else {
                    msgChannel.createMessage("wrong syntax, try again").block();
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
                msgChannel.createMessage("only the moderator may use this command").block();
            }
        };
        mapCommands.put("endDay", endDayCommand);

    }

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
                if (amount == entry.getValue()) {
                    hasMajority = false;
                } else if (amount < entry.getValue()) {
                    mostVoted = entry.getKey();
                    hasMajority = true;
                    amount = entry.getValue();
                }
            }

            if (!hasMajority && mostVoted != null) {
                Globals.createMessage(game.mainChannel,
                        "Alle Spieler haben abgestimmt, jedoch gibt es **keine klare Mehrheit**. Es wird gebete, dass wenigstens ein Spieler seine Stimme ändert, damit es zu einer klaren Mehrheit kommt.",
                        false);
            }

            if (hasMajority && mostVoted != null) {
                suggestMostVoted(mostVoted);

                // gibt ein Embed mit den Votes aus
                var mssg = "";
                for (var entry : mapVotes.entrySet()) {
                    mssg += entry.getKey().user.asMember(game.server.getId()).block().getDisplayName() + " hat für " + entry.getValue().user.asMember(game.server.getId()).block().getDisplayName()
                            + " abgestimmt \n";
                }

                Globals.createEmbed(game.mainChannel, Color.WHITE,
                        "Die Würfel sind gefallen \nAuf dem Schafott steht: " + mostVoted.user.asMember(game.server.getId()).block().getDisplayName(), mssg);
            }
        }
    }

    private void addVote(MessageCreateEvent event, Player voter, Player votedFor) {
        // der Vote des Bürgermeisters zählt mehr
        var voteValue = voter.role.name.equalsIgnoreCase("Bürgermeister")? 1.5d : 1d;
        
       
        // TODO: this dosnt work
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
        Globals.createMessage(game.mainChannel,
                voter.user.getMention() + " will, dass " + votedFor.user.getMention() + " gelyncht wird!", false);

    }

    private void suggestMostVoted(Player mostVoted) {
        // suggests the most Voted Player to the Mod
        MessagesMain.suggestMostVoted(game, mostVoted);
        // some cards can interfere in this stage (Prinz, Märtyrerin)
        checkLynchConditions();

    }

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

    private void endDay() {
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN,
                "Wenn bu bereit bist, den Tag zu beenden tippe den Command \"confirm\"", "");
        //
        PrivateCommand endDayCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("confirm")
                    && event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "");
                game.gameState.changeDayPhase();

                return true;
            } else {
                return false;
            }
        };
        game.addPrivateCommand(game.userModerator.getId(), endDayCommand);
    }

}
