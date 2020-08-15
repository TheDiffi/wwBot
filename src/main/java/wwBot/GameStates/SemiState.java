package wwBot.GameStates;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.DayPhases.DaySemi;
import wwBot.GameStates.DayPhases.FirstNightSemi;
import wwBot.GameStates.DayPhases.MorningSemi;
import wwBot.GameStates.DayPhases.NightSemi;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;
import wwBot.cards.Role;
import wwBot.cards.RoleDoppelgängerin;

public class SemiState extends MainState {

	public DaySemi day = null;
	public NightSemi night = null;
	public MorningSemi morning = null;
	public FirstNightSemi firstNight = null;
	public DayPhase dayPhase = DayPhase.FIRST_NIGHT;

	public User userModerator;

	SemiState(Game game) {
		// sets variables
		super(game);
		mapPlayers = game.mapPlayers;
		userModerator = game.userModerator;

		// registers Commands; loads the lists and creates the Deathroom
		registerStateCommands();
		createDeathChat();
		loadGameLists();

		// sends the first messages
		MessagesMain.onGameStart(game);
		greetMod(game);

	}

	// greets the mod and waits for the mod to start the first night
	private void greetMod(Game game) {
		MessagesMain.greetMod(game);
		Globals.printPlayersMap(game.userModerator.getPrivateChannel().block(), game.mapPlayers, "Alle Spieler", game);

		PrivateCommand readyCommand = (event, parameters, msgChannel) -> {
			if (parameters != null && parameters.get(0).equalsIgnoreCase("Ready")) {
				changeDayPhase(DayPhase.FIRST_NIGHT);
				return true;

			} else {
				return false;
			}
		};
		game.addPrivateCommand(userModerator.getId(), readyCommand);

	}

	// -------------------- Kill System --------------------------
	// CheckIfDies --> überprüft in dieser Reihenfolge: ob der Player nicht bereits
	// Tot ist; ob er eine Spezialkarte ist welche nicht stirbt
	// KillPlayer --> falls checkIfDies true zurückgibt:
	// 1) player.alive wird auf false gesetzt; der Spieler wird zur liste der toten
	// Spieler und zum death-chat hinzugefügt
	// 2) der Player wird gemuted
	// 3) der Tot wird verkündet und die Identität gelüftet
	// 3) checkConsequences wird gerufen. Dies überprüft die Consequenzen

	// checks the conditions if the player dies
	@Override
	public boolean checkIfDies(Player unluckyPlayer, String causedByRole) {
		var dies = true;
		var modChannel = game.userModerator.getPrivateChannel().block();

		// falls der Spieler Tot ist stirbt er nicht
		if (!unluckyPlayer.role.alive) {
			dies = false;
			MessagesMain.errorPlayerAlreadyDead(game, modChannel);
		}

		// VERFLUCHTER
		if (unluckyPlayer.role.name.equals("Verfluchter") && causedByRole.equalsIgnoreCase("Werwolf")) {
			dies = false;
			MessagesMain.verfluchtenMutation(game);
		}

		// HARTER BURSCHE
		if (unluckyPlayer.role.name.equals("Harter-Bursche")) {
			dies = false;
			MessagesMain.checkHarterBurscheDeath(modChannel);

			// if confirmed it kills the player. if canceled nothing happenes
			PrivateCommand confirmCommand = (event, parameters, msgChannel) -> {
				if (parameters != null && parameters.get(0).equalsIgnoreCase("confirm")) {
					killPlayer(unluckyPlayer, causedByRole);
					msgChannel.createMessage("confirmed!").block();
					return true;
				} else if (parameters != null && parameters.get(0).equalsIgnoreCase("cancel")) {
					Globals.createMessage(modChannel, "Canceled", false);
					return true;
				} else {
					return false;
				}
			};
			game.addPrivateCommand(game.userModerator.getId(), confirmCommand);

		}

		// PRINZ
		if (unluckyPlayer.role.name.equals("Prinz") && causedByRole.equalsIgnoreCase("Dorfbewohner")) {
			dies = false;
			MessagesMain.prinzSurvivesLynching(game);
		}

		return dies;
	}

	@Override
	public void killPlayer(Player unluckyPlayer, String causedByRole) {

		// kills player
		unluckyPlayer.role.alive = false;
		game.deadPlayers.add(unluckyPlayer);

		updateDeathChat();

		try {
			unluckyPlayer.user.asMember(game.server.getId()).block().edit(a -> a.setMute(true)).block();
		} catch (Exception e) {
		}

		loadGameLists();

		// reveals the players death and identity
		checkDeathMessages(unluckyPlayer, causedByRole);
		Globals.printCard(unluckyPlayer.role.name, game.mainChannel);

		// calculates the consequences
		checkConsequences(unluckyPlayer, causedByRole);
	}

	private void checkConsequences(Player unluckyPlayer, String causedByRole) {

		// Doppelgängerin
		if (mapExistingRoles.containsKey("Doppelgängerin")) {
			var dp = mapExistingRoles.get("Doppelgängerin").get(0);
			var dpRole = (RoleDoppelgängerin) dp.role;

			// if the dead user equals the one chosen by the DP, the DP gets the role of the
			// dead player
			if (dpRole.boundTo.user.getId().equals(unluckyPlayer.user.getId())) {
				dp.role = Role.createRole(unluckyPlayer.role.name);
				MessagesMain.onDoppelgängerinTransformation(game, dp, unluckyPlayer);
			}

		}

		// SEHER LEHRLING
		if (unluckyPlayer.role.name.equalsIgnoreCase("Seher")) {
			// looks if there is a Zauberlehrling in the game
			for (var player : game.livingPlayers.entrySet()) {
				// if a Lehrling id found, he is the new Seher
				if (player.getValue().role.name.equalsIgnoreCase("SeherLehrling")) {
					player.getValue().role = Role.createRole("Seher");
					MessagesMain.onSeherlehrlingPromotion(game, unluckyPlayer);
				}
			}

			// AUSSÄTZIGE
		} else if (unluckyPlayer.role.name.equalsIgnoreCase("Aussätzige")) {
			// if killed by Werwölfe
			if (causedByRole != null && causedByRole.equalsIgnoreCase("Werwolf")) {
				// if the dying player is the Aussätzige, the Werwölfe kill noone the next night
				MessagesMain.onAussätzigeDeath(game);

			}

			// WOLFSJUNGES
		} else if (unluckyPlayer.role.name.equalsIgnoreCase("Wolfsjunges")) {
			// if not killed by Werwölfe (does not make sense but ok.)
			if (causedByRole != null && !causedByRole.equalsIgnoreCase("Werwolf")) {
				// if the Wolfsjunges dies, the WW can kill two players in the following night.
				MessagesMain.onWolfsjungesDeath(game);

			}

			// JÄGER
		} else if (unluckyPlayer.role.name.equalsIgnoreCase("Jäger")) {
			MessagesMain.onJägerDeath(game, unluckyPlayer);
			// loads the Bot api
			DiscordClient client = DiscordClientBuilder
					.create("NzA3NjUzNTk1NjQxOTM4MDMx.Xs1bLg.RLbvLwafgTDyhLYsQZl4pi0hluc").build();
			// (hopefully) waits for the jägers answer
			client.getEventDispatcher().on(MessageCreateEvent.class)
					.filter(message -> message.getMessage().getAuthor()
							.map(user -> user.getId().equals(unluckyPlayer.user.getId())).orElse(false))
					.subscribe(event -> {
						try {
							var content = event.getMessage().getContent().get();
							var messageChannel = event.getMessage().getChannel().block();
							List<String> parameters = new LinkedList<>(Arrays.asList(content.split(" ")));
							if (content != null && parameters.size() > 0) {
								// finds the players
								Player player1 = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)),
										game.mapPlayers, game);
								Player shotPlayer = null;
								Player player2 = null;
								if (parameters.size() > 1) {
									player2 = Globals.findPlayerByName(Globals.removeDash(parameters.get(1)),
											game.mapPlayers, game);
								}

								if (player1 != null) {
									shotPlayer = player1;
								} else if (player2 != null) {
									shotPlayer = player2;
								}

								if (shotPlayer != null) {
									Globals.createMessage(messageChannel, "Erfolg!");
									Globals.createMessage(game.mainChannel, "Der Schuss trifft" + shotPlayer.name);
									if (checkIfDies(shotPlayer, "Jäger")) {
										killPlayer(shotPlayer, "Jäger");
									}
								} else {
									MessagesMain.errorPlayerNotFound(messageChannel);
								}

							} else {
								MessagesMain.errorWrongSyntax(game, messageChannel);
							}
						} catch (Exception e) {
							event.getMessage().getChannel().block().createMessage("Something went wrong").block();
						}
					});
		}

		// AMOR
		if (unluckyPlayer.role.inLoveWith != null && unluckyPlayer.role.inLoveWith.role.alive) {
			if (checkIfDies(unluckyPlayer.role.inLoveWith, "Amor")) {
				killPlayer(unluckyPlayer.role.inLoveWith, "Amor");
			}
		}

	}

	private void checkDeathMessages(Player player, String cause) {

		switch (cause) {
			case "Werwolf":
				MessagesMain.deathByWW(game, player);
			case "Hexe":
				MessagesMain.deathByMagic(game, player);
			case "Amor":
				MessagesMain.deathByLove(game, player);
			case "Jäger":
				MessagesMain.deathByGunshot(game, player);
			case "Dorfbewohner":
				MessagesMain.deathByLynchen(game, player);
			default:
				MessagesMain.deathByDefault(game, player);
		}

		/* if (cause.equalsIgnoreCase("Werwolf")) {
			MessagesMain.deathByWW(game, player);
		} else if (cause.equalsIgnoreCase("Hexe") || cause.equalsIgnoreCase("Magier")) {
			MessagesMain.deathByMagic(game, player);
		} else if (cause.equalsIgnoreCase("Amor")) {
			MessagesMain.deathByLove(game, player);
		} else if (cause.equalsIgnoreCase("Jäger")) {
			MessagesMain.deathByGunshot(game, player);
		} else if (cause.equalsIgnoreCase("Dorfbewohner")) {
			MessagesMain.deathByLynchen(game, player);
		} else {
			MessagesMain.deathByDefault(game, player);
		} */
	}

	@Override
	public void changeDayPhase(DayPhase nextPhase) {
		loadGameLists();
		// transitions to Night
		if (nextPhase == DayPhase.NORMAL_NIGHT) {
			checkIfGameEnds();
			setMuteAllPlayers(game.livingPlayers, true);
			createWerwolfChat();

			night = new NightSemi(game);
			dayPhase = DayPhase.NORMAL_NIGHT;

			// transitions to Morning
		} else if (nextPhase == DayPhase.MORNING) {
			setMuteAllPlayers(game.livingPlayers, false);
			deleteWerwolfChat();

			morning = new MorningSemi(game);
			dayPhase = DayPhase.MORNING;

			// transitions to Day
		} else if (nextPhase == DayPhase.DAY) {
			checkIfGameEnds();

			day = new DaySemi(game);
			dayPhase = DayPhase.DAY;

			// transitions to 1st Night
		} else if (nextPhase == DayPhase.FIRST_NIGHT) {

			firstNight = new FirstNightSemi(game);
			dayPhase = DayPhase.FIRST_NIGHT;
		}

	}

	@Override
	public boolean exit() {
		deleteDeathChat();
		deleteWerwolfChat();
		System.out.println("Successfully closed SemiMainGameState");
		return true;
	}

	// -----------------------------------------------------------

	// --------------------- Commands ----------------------------

	@Override
	public boolean handleCommand(String requestedCommand, MessageCreateEvent event, List<String> parameters,
			MessageChannel runningInChannel) {
		var found = false;
		if (livingPlayers.containsKey(event.getMessage().getAuthor().get().getId())
				|| event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {

			// checks the Command map of the current DayPhase
			if (dayPhase == DayPhase.MORNING) {
				var foundCommand = morning.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					found = true;
				} else if (!found && day != null && day.mapCommands.containsKey(requestedCommand)) {
					event.getMessage().getChannel().block().createMessage("This command is only available during Day")
							.block();
					found = true;
				} else {
					found = false;
				}
			} else if (dayPhase == DayPhase.DAY) {
				var foundCommand = day.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					found = true;
				} else if (!found && night != null && night.mapCommands.containsKey(requestedCommand)) {
					event.getMessage().getChannel().block().createMessage("This command is only available during Night")
							.block();
					found = true;
				} else {
					found = false;
				}
			} else if (dayPhase == DayPhase.NORMAL_NIGHT) {
				var foundCommand = night.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					found = true;
				} else if (!found && morning != null && morning.mapCommands.containsKey(requestedCommand)) {
					event.getMessage().getChannel().block()
							.createMessage("This command is only available during the Morning").block();
					found = true;
				} else {
					found = false;
				}
			} else if (dayPhase == DayPhase.FIRST_NIGHT) {
				var foundCommand = firstNight.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					found = true;
				} else {
					found = false;
				}

				if (event.getMessage().getContent().get().equalsIgnoreCase("&help")) {
					event.getMessage().getChannel().block().createMessage("In der ersten Nacht gibt es keine Commands")
							.block();
					found = true;
				}

			}

			if (!found) {
				found = super.handleCommand(requestedCommand, event, parameters, runningInChannel);
			}

		} else {
			MessagesMain.errorNoAccessToCommand(game, event.getMessage().getChannel().block());
			found = true;
		}

		return found;
	}

	private void registerStateCommands() {

		// ping testet ob der bot antwortet
		Command pingCommand = (event, parameters, msgChannel) -> {
			msgChannel.createMessage("Pong! SemiMainGameState").block();

		};
		gameStateCommands.put("ping", pingCommand);

		// zeigt die verfügbaren commands
		Command showCommandsCommand = (event, parameters, msgChannel) -> {
			var mssg = "**To show Moderator Commands type \"&modCommands\"** ";
			mssg += "\n" + MessagesMain.getCommandsMain();
			mssg += "\n" + MessagesMain.getCommandsGame();
			mssg += "\n" + MessagesMain.getCommandsSemiState();
			mssg += "\n" + MessagesMain.getHelpInfo();
			Globals.createEmbed(msgChannel, Color.CYAN, "Commands", mssg);
		};
		gameStateCommands.put("showCommands", showCommandsCommand);
		gameStateCommands.put("sC", showCommandsCommand);

		// zeigt die verfügbaren commands
		Command showModCommandsCommand = (event, parameters, msgChannel) -> {
			var mssg = "**To show Moderator Commands type \"&modCommands\"** ";
			mssg += "\n" + MessagesMain.getModCommands();
			mssg += "\n" + MessagesMain.getHelpInfo();

			Globals.createEmbed(msgChannel, Color.ORANGE, "Moderator Commands", mssg);
		};
		gameStateCommands.put("showModCommands", showModCommandsCommand);
		gameStateCommands.put("modCommands", showModCommandsCommand);

		/*
		 * // shows the moderator the list of players (alive or all) Command
		 * printListCommand = (event, parameters, msgChannel) -> {
		 * 
		 * // compares the Snowflake of the Author to the Snowflake of the Moderator if
		 * (event.getMessage().getAuthor().get().getId().equals(userModerator.getId()))
		 * { // checks if the syntax is correct if (parameters != null &&
		 * parameters.size() != 0) { var param = parameters.get(0); // if the user typed
		 * "Players" it prints a list of all players, if he typed // "Living" it prints
		 * only the living players if (param.equalsIgnoreCase("Players")) {
		 * Globals.printPlayersMap(userModerator.getPrivateChannel().block(),
		 * mapPlayers, "Spieler", game); } else if (param.equalsIgnoreCase("Living")) {
		 * Globals.printPlayersMap(userModerator.getPrivateChannel().block(),
		 * livingPlayers, "Noch Lebend", game); } } else {
		 * userModerator.getPrivateChannel().block()
		 * .createMessage("Wrong syntax! try \"&showList Players\" or \"&showList Living\""
		 * ).block();
		 * 
		 * } } else { MessagesMain.errorModOnlyCommand(msgChannel); }
		 * 
		 * }; gameStateCommands.put("printList", printListCommand);
		 * gameStateCommands.put("list", printListCommand);
		 */
		// prints the living players and their role
		Command listPlayersCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.printPlayersMap(userModerator.getPrivateChannel().block(), game.mapPlayers, "Alle Spieler",
						game);
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("listPlayers", listPlayersCommand);

		// prints the living players and their role
		Command listLivingCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.printPlayersMap(userModerator.getPrivateChannel().block(), game.livingPlayers, "Alle Spieler",
						game);
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("listliving", listLivingCommand);
		gameStateCommands.put("listlivingPlayers", listLivingCommand);

		// ummutes a specific player
		Command muteCommand = (event, parameters, msgChannel) -> {

			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				// finds the requested Player
				var foundPlayer = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.mapPlayers,
						game);
				// mutes the found player
				if (foundPlayer != null) {
					foundPlayer.user.asMember(game.server.getId()).block().edit(a -> a.setMute(true)).block();
				} else {
					MessagesMain.errorPlayerNotFound(msgChannel);
				}
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("mute", muteCommand);
		gameStateCommands.put("stfu", muteCommand);

		// ummutes a specific player
		Command unMuteCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				// finds the requested Player
				var foundPlayer = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.mapPlayers,
						game);
				// mutes the found player
				if (foundPlayer != null) {
					foundPlayer.user.asMember(game.server.getId()).block().edit(a -> a.setMute(false)).block();
				} else {
					MessagesMain.errorPlayerNotFound(msgChannel);
				}
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("unMute", unMuteCommand);

		// shows the moderator the list of players
		Command muteAllCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				setMuteAllPlayers(mapPlayers, true);
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("muteAll", muteAllCommand);
		gameStateCommands.put("stfuAll", muteAllCommand);

		// shows the moderator the list of players
		Command unMuteAllCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				setMuteAllPlayers(mapPlayers, false);
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("unMuteAll", unMuteAllCommand);

		// lets the moderator kill a person and checks the consequences
		Command killCommand = (event, parameters, msgChannel) -> {
			// only the moderator can use this command
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				if (parameters.size() == 2 || parameters.size() == 1) {

					// finds the requested Player
					var unluckyPlayer = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)),
							game.livingPlayers, game);

					// stores the cause
					var causedBy = "null";
					if (parameters.size() == 2) {
						causedBy = parameters.get(1);
					}

					if (unluckyPlayer != null && (Globals.mapRegisteredCardsSpecs.containsKey(causedBy)
							|| causedBy.equalsIgnoreCase("Null"))) {
						if (unluckyPlayer.role.alive) {
							if (checkIfDies(unluckyPlayer, causedBy)) {
								killPlayer(unluckyPlayer, causedBy);
								event.getMessage().getChannel().block().createMessage("Erfolg!").block();

							}
						} else {
							MessagesMain.errorPlayerAlreadyDead(game, msgChannel);
						}
					} else {
						MessagesMain.errorWrongSyntaxOnKill(game, event);
					}
				} else {
					MessagesMain.errorWrongSyntaxOnKill(game, event);
				}
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("kill", killCommand);

	}

}
