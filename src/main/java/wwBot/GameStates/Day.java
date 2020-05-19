package wwBot.GameStates;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.Card;
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
        var mapAvailableCards = Globals.mapAvailableCards;

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
            var allowedToVote = false;

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

            for (var player : game.livingPlayers.entrySet()) {
                if (player.getValue().user.getUsername().equals(voter.getUsername())) {
                    allowedToVote = true;
                }
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
            } else if (!allowedToVote) {
                event.getMessage().getChannel().block().createMessage("You are not allowed to vote!").block();

            } else if (votedFor.alive && allowedToVote) {
                // if the same key gets put in a second time, the first value is dropped
                addVote(event, voter, votedFor);
            }

            countVotes();

        };
        mapCommands.put("vote", voteCommand);

        // gives the mod a one-time access to
        Command lynchCommand = (event, parameters, msgChannel) -> {
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                if (parameters != null && parameters.size() == 1) {
                    var unluckyPerson = Globals.findPlayerByName(parameters.get(0), game.livingPlayers);
                    if (unluckyPerson != null) {
                        killPlayer(unluckyPerson, mapAvailableCards.get("Dorfbewohner"));
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

        // lets the moderator kill a person and checks the consequences
		Command killCommand = (event, parameters, msgChannel) -> {
			// only the moderator can use this command
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				if (parameters.size() == 2) {
					// finds the requested Player
					var unluckyPlayer = Globals.findPlayerByName(parameters.get(0), game.livingPlayers);
					// gets the cause
					var causedBy = parameters.get(1);
					// finds the cause (role)
					var causedByRole = mapAvailableCards.get(causedBy);
					if (unluckyPlayer != null && (causedByRole != null || causedBy.equalsIgnoreCase("null"))) {
                        killPlayer(unluckyPlayer, causedByRole);
                        event.getMessage().getChannel().block().createMessage("Erfolg!").block();
					} else {
						event.getMessage().getChannel().block().createMessage(
								"Ich verstehe dich nicht 😕\nDein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)")
								.block();
					}

				} else {
					event.getMessage().getChannel().block().createMessage(
							"Ich verstehe dich nicht 😕\nDein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)")
							.block();

				}
			} else {
				event.getMessage().getChannel().block().createMessage("You have no permission for this command")
						.block();
			}
		};
		mapCommands.put("kill", killCommand);

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

    // TODO:add Bürgermeister function
    // TODO: start voting phase when every death has been announced
    // TODO: add Command for mod &startVotingPhase

    // counts the votes and lynchs the player with the most
    private void countVotes() {
        // if every living player has voted, the votes get counted
        if (mapVotes.size() >= game.livingPlayers.size()) {
            var amount = 0;
            var hasMajority = false;
            Player mostVoted = null;

            // searches for the person with the highest votes. If there are more than one
            // hasMajority gets set to false
            for (var player : mapAmountVotes.entrySet()) {
                if (amount < player.getValue()) {
                    mostVoted = player.getKey();
                    hasMajority = true;
                    amount = player.getValue();
                } else if (amount == player.getValue()) {
                    hasMajority = false;
                }
            }

            if (hasMajority && mostVoted != null) {
                suggestMostVoted(mostVoted);
            }
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
                .createMessage(voter.getMention() + " will, dass " + votedFor.user.getMention() + " gelyncht wird!")
                .block();
    }

    private void suggestMostVoted(Player mostVoted) {
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.RED, "Alle Spieler Haben Gewählt!",
                "Auf dem Schaffott steht *" + mostVoted.user.getMention()
                        + "* \nMit \"&lynch <Player>\" kannst du einen Spieler lynchen und damit die Rolle des Spielers offenbaren. Mit \"&endDay\" kanns du anschließend den Tag beenden (Falls du niemanden Lynchen möchtest kannst du acuh gleich mit &endDay fortfahren)");
        for (var player : game.livingPlayers.entrySet()) {
            if (player.getValue().role.name.equalsIgnoreCase("Märtyrerin")) {
                Globals.createMessage(game.userModerator.getPrivateChannel().block(),
                        "Frag die Märtyrerin ob sie sich anstelle der nominierten Person lynchen lassen will.", false);
                break;
            }
        }

    }

    private void killPlayer(Player unluckyPlayer, Card causedByRole) {
        var mapAvailableCards = Globals.mapAvailableCards;
        var dies = true;

        // checks if the player dies
        dies = checkIfDies(unluckyPlayer, causedByRole, dies);

        if (dies) {
            // kills player
            unluckyPlayer.alive = false;
            game.deadPlayers.add(unluckyPlayer);

            // reveals the players death and identity
            checkDeathMessages(unluckyPlayer, causedByRole);

            Globals.printCard(unluckyPlayer.role.name, game.runningInChannel);

            // calculates the consequences

            if (unluckyPlayer.role.name.equalsIgnoreCase("Seher")) {
                // looks if there is a Zauberlehrling in the game
                for (var player : game.livingPlayers.entrySet()) {
                    // if he finds a Lehrling he is the new Seher
                    if (player.getValue().role.name.equalsIgnoreCase("SeherLehrling")) {
                        player.getValue().role = mapAvailableCards.get("Seher");
                        MessagesMain.seherlehrlingWork(game, unluckyPlayer);
                    }
                }

            } else if (unluckyPlayer.role.name.equalsIgnoreCase("Aussätzige")) {
                // if killed by Werwölfe
                if (causedByRole != null && causedByRole.name.equalsIgnoreCase("Werwolf")) {
                    // if the dying player is the Aussätzige, the Werwölfe kill noone the next night
                    MessagesMain.verfluchtenMutation(game, unluckyPlayer);
                    Globals.createMessage(game.userModerator.getPrivateChannel().block(),
                            "Die Aussätzige ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe niemanden töten",
                            false);
                }

            } else if (unluckyPlayer.role.name.equalsIgnoreCase("Wolfsjunges")) {
                // if not killed by Werwölfe (does not make sense but ok.)
                if (causedByRole != null && !causedByRole.name.equalsIgnoreCase("Werwolf")) {
                    // if the Wolfsjunges dies, the WW can kill two players in the following night.
                    Globals.createMessage(game.userModerator.getPrivateChannel().block(),
                            "Das Wolfsjunges ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe zwei Personen töten.",
                            false);
                }
            } else if (unluckyPlayer.role.name.equalsIgnoreCase("Jäger")) {
                MessagesMain.jägerDeath(game, unluckyPlayer);

                PrivateCommand jägerCommand = (event, parameters, msgChannel) -> {
                    if (parameters != null) {
                        var player = Globals.findPlayerByName(parameters.get(0), game.livingPlayers);
                        // if a player is found
                        if (player != null) {
                            killPlayer(unluckyPlayer, mapAvailableCards.get("Jäger"));
                            return true;
                        } else {
                            event.getMessage().getChannel().block()
                                    .createMessage("Ich konnte diesen Spieler leider nicht finden").block();
                            return false;

                        }
                    } else {
                        return false;
                    }
                };
                game.addPrivateCommand(unluckyPlayer.user.getId(), jägerCommand);

            }
        }

    }

    // checks the conditions if the player dies
    private boolean checkIfDies(Player unluckyPlayer, Card causedByRole, Boolean dies) {
        if (unluckyPlayer.role.name.equals("Verfluchter") && causedByRole.name.equals("Werwolf")) {
            dies = false;
            Globals.createMessage(game.runningInChannel, "Der Verfluchte hat Mutiert", true);
        }
        return dies;
    }

    private void checkDeathMessages(Player player, Card cause) {

        if (cause.name.equalsIgnoreCase("Werwolf")) {
            MessagesMain.deathByWW(game, player);
        } else if (cause.name.equalsIgnoreCase("Hexe") || cause.name.equalsIgnoreCase("Magier")) {
            MessagesMain.deathByMagic(game, player);
        } else if (cause.name.equalsIgnoreCase("Amor")) {
            MessagesMain.deathByLove(game, player);
        } else if (cause.name.equalsIgnoreCase("Jäger")) {
            MessagesMain.deathByGunshot(game, player);
        } else if (cause.name.equalsIgnoreCase("Dorfbewohner")) {
            MessagesMain.deathByLynchen(game, player);
        } else {
            MessagesMain.death(game, player);

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
                game.currentGameState.changeDayPhase();

                return true;
            } else {
                return false;
            }
        };
        game.addPrivateCommand(game.userModerator.getId(), endDayCommand);
    }

}
