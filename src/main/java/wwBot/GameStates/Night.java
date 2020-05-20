package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Command;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.PrivateCommand;

public class Night {
	public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);

	Game game;

	public Night(Game getGame) {
		game = getGame;
		registerNightCommands();

		
		
		// first: Informatial message
		// second: sorted list of who tp call
		// third: another message explaining how to kill someone
		initiateNight();
		
		for (var player : game.livingPlayers.entrySet()) {
			player.getValue().user.asMember(game.runningInServer).blockOptional().ifPresent( spec ->{spec.edit( a ->{ a.setMute(true).setDeafen(false);
				});
			});
		
		}
		// TODO: create privateChannel for ww & mod
		//TODO: tell WW and mod about this channel
		//TODO: fullmute all Players until sunrise

	}

	private void initiateNight() {
		// sorts all roles by when they wake up in a list (i starts at 0 => all Roles that dont wanke up are not added)
		var sortedRoles = new ArrayList<Player>();
		for (int i = 0; i < 4; i++) {
			
			for (var player : game.livingPlayers.entrySet()) {
				if (player.getValue().role.nightSequence == i) {
					sortedRoles.add(player.getValue());
				}
			}
		}
		if (sortedRoles != null || sortedRoles.size() < 1) {
			MessagesMain.semiOnNightStart(game, sortedRoles);
		} else {
			game.userModerator.getPrivateChannel().block().createMessage("no Roles to call").block();
		}
	}

	public void registerNightCommands() {

		// replys with pong!
		Command pingCommand = (event, parameters, msgChannel) -> {
			event.getMessage().getChannel().block().createMessage("Pong! NightPhase").block();
		};
		mapCommands.put("ping", pingCommand);

		// shows the available Commands in this Phase
		Command helpCommand = (event, parameters, msgChannel) -> {
			// replies only to the moderator
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				msgChannel.createMessage("TODO: add help Command in NightPhase").block();
			}

		};
		mapCommands.put("help", helpCommand);

		// shows the moderator the list of players
        Command endNightCommand = (event, parameters, msgChannel) -> {
 
            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
			   endNight();
            } else {
                msgChannel.createMessage("only the moderator can use this command");
            }
        };
		mapCommands.put("endNight", endNightCommand);
		
		



		/* // lets the moderator kill a person and checks the consequences
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
					} else {
						event.getMessage().getChannel().block().createMessage(
								"Ich verstehe dich nicht :/ Dein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)")
								.block();
					}

				} else {
					event.getMessage().getChannel().block().createMessage(
							"Ich verstehe dich nicht :/ Dein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)")
							.block();

				}
			} else {
				event.getMessage().getChannel().block().createMessage("You have no permission for this command")
						.block();
			}
		};
		mapCommands.put("kill", killCommand); */

	}

	private void endNight() {
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN,
                "Um zu bestätigen, dass bu bereit bist die Nacht zu beenden, tippe \"confirm\"", "");
        //
        PrivateCommand endNightCommand = (event, parameters, msgChannel) -> {
			//checks if written by mod and if right command
            if (parameters != null && parameters.get(0).equalsIgnoreCase("confirm")
                    && event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                game.currentGameState.changeDayPhase();
				//TODO: unmute all players
                return true;
            } else {
                return false;
            }
        };
        game.addPrivateCommand(game.userModerator.getId(), endNightCommand);
    }

	/* private void killPlayer(Player unluckyPlayer, Card causedByRole) {
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
	} */

}
