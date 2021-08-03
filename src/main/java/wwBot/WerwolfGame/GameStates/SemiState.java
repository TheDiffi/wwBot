package wwBot.WerwolfGame.GameStates;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.DayPhases.Semi.DaySemi;
import wwBot.WerwolfGame.GameStates.DayPhases.Semi.FirstNightSemi;
import wwBot.WerwolfGame.GameStates.DayPhases.Semi.MorningSemi;
import wwBot.WerwolfGame.GameStates.DayPhases.Semi.NightSemi;
import wwBot.WerwolfGame.cards.Role;
import wwBot.WerwolfGame.cards.RoleDoppelgängerin;

public class SemiState extends MainState {

	public DaySemi day = null;
	public NightSemi night = null;
	public MorningSemi morning = null;
	public FirstNightSemi firstNight = null;
	public DayPhase dayPhase = DayPhase.FIRST_NIGHT;

	public User userModerator;
	public Map<String, Command> modCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);

	SemiState(Game game) {
		// sets variables
		super(game);
		mapPlayers = game.mapPlayers;
		userModerator = game.userModerator;

	}

	public void start() {
		// registers Commands; loads the lists and creates the Deathroom
		registerStateCommands();
		createDeathChat();
		updateGameLists();
		changeDayPhase(DayPhase.FIRST_NIGHT);

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
		if (!unluckyPlayer.role.deathDetails.alive) {
			MessagesWW.errorPlayerAlreadyDead(modChannel);
			dies = false;

		}

		// VERFLUCHTER
		if (unluckyPlayer.role.name.equals("Verfluchter") && causedByRole.equalsIgnoreCase("Werwolf")) {
			unluckyPlayer.role = Role.createRole("Werwolf");
			MessagesWW.verfluchtenMutation(game);
			dies = false;
		}

		// HARTER BURSCHE
		if (unluckyPlayer.role.name.equals("Harter-Bursche") && !causedByRole.equalsIgnoreCase("Confirmed")) {
			MessagesWW.checkHarterBurscheDeath(modChannel);
			dies = false;

			// if confirmed it kills the player. if canceled nothing happenes
			PrivateCommand confirmCommand = (event, parameters, msgChannel) -> {

				if (parameters != null && parameters.get(0).equalsIgnoreCase("confirm")) {
					killPlayer(unluckyPlayer, "Confirmed");
					MessagesWW.confirm(msgChannel);
					return true;

				} else if (parameters != null && parameters.get(0).equalsIgnoreCase("cancel")) {
					Globals.createMessage(modChannel, "Canceled");
					return true;

				} else {
					return false;
				}
			};
			game.addPrivateCommand(game.userModerator.getId(), confirmCommand);

		}

		// PRINZ
		if (unluckyPlayer.role.name.equals("Prinz") && causedByRole.equalsIgnoreCase("Dorfbewohner")) {
			MessagesWW.prinzSurvives(game);
			dies = false;

		}

		return dies;
	}

	@Override
	public boolean killPlayer(Player unluckyPlayer, String causedByRole) {

		// kills player
		unluckyPlayer.role.deathDetails.alive = false;
		game.deadPlayers.add(unluckyPlayer);
		updateGameLists();
		updateDeathChat();

		Globals.tryMutePlayer(unluckyPlayer, game.server.getId());

		// reveals the players death and identity
		checkDeathMessages(unluckyPlayer, causedByRole);

		// calculates the consequences
		checkConsequences(unluckyPlayer, causedByRole);

		if (calculateGameEnd() != 0) {
			MessagesWW.notifyModGameEnd(game.userModerator.getPrivateChannel().block(), calculateGameEnd());
		}

		return true;
	}

	private void checkConsequences(Player unluckyPlayer, String causedByRole) {

		// SEHER LEHRLING
		if (unluckyPlayer.role.name.equalsIgnoreCase("Seher")) {
			// looks if there is a Zauberlehrling in the game
			for (var player : game.livingPlayers.entrySet()) {
				// if a Lehrling id found, he is the new Seher
				if (player.getValue().role.name.equalsIgnoreCase("SeherLehrling")) {
					player.getValue().role = Role.createRole("Seher");
					MessagesWW.onSeherlehrlingPromotion(game, unluckyPlayer);

				}
			}

			// AUSSÄTZIGE
		} else if (unluckyPlayer.role.name.equalsIgnoreCase("Aussätzige")) {
			// if killed by Werwölfe
			if (causedByRole != null && causedByRole.equalsIgnoreCase("Werwolf")) {
				// if the dying player is the Aussätzige, the Werwölfe kill noone the next night
				MessagesWW.onAussätzigeDeath(game);

			}

			// WOLFSJUNGES
		} else if (unluckyPlayer.role.name.equalsIgnoreCase("Wolfsjunges")) {
			// if not killed by Werwölfe (does not make sense but ok.)
			if (causedByRole != null && !causedByRole.equalsIgnoreCase("Werwolf")) {
				// if the Wolfsjunges dies, the WW can kill two players in the following night.
				MessagesWW.onWolfsjungesDeath(game);

			}

			// JÄGER
		} else if (unluckyPlayer.role.name.equalsIgnoreCase("Jäger")) {
			MessagesWW.onJägerDeath(game, unluckyPlayer);

			PrivateCommand jägerCommand = (event, parameters, msgChannel) -> {
				var foundPlayer = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

				if (foundPlayer != null) {
					MessagesWW.confirm(msgChannel);
					killPlayer(foundPlayer, "Jäger");

					return true;
				} else {
					return false;
				}

			};
			game.addPrivateCommand(unluckyPlayer.user.getId(), jägerCommand);

		}

		// AMOR
		if (unluckyPlayer.role.inLoveWith != null && unluckyPlayer.role.inLoveWith.role.deathDetails.alive) {
			if (checkIfDies(unluckyPlayer.role.inLoveWith, "Amor")) {
				killPlayer(unluckyPlayer.role.inLoveWith, "Amor");
			}
		}

		// Doppelgängerin
		if (mapExistingRoles.containsKey("Doppelgängerin")) {
			var dpPlayer = mapExistingRoles.get("Doppelgängerin").get(0);
			var dpRole = (RoleDoppelgängerin) dpPlayer.role;

			// if the dead user equals the one chosen by the DP, the DP gets the role of the
			// dead player
			if (dpRole.boundTo.user.getId().equals(unluckyPlayer.user.getId())) {
				dpPlayer.role = Role.createRole(unluckyPlayer.role.name);
				MessagesWW.onDoppelgängerinTransformation(game, dpPlayer, unluckyPlayer);
			}
		}

	}

	private void checkDeathMessages(Player player, String cause) {

		switch (cause.replaceAll("\\s+", "").toLowerCase()) {
			case "werwolf":
			case "werwölfe":
				MessagesWW.deathByWW(game, player);
				break;
			case "hexe":
				MessagesWW.deathByMagic(game, player);
				break;
			case "magier":
				MessagesWW.deathByMagic(game, player);
				break;
			case "amor":
				MessagesWW.deathByLove(game, player);
				break;
			case "jäger":
				MessagesWW.deathByGunshot(game, player);
				break;
			case "dorfbewohner":
				MessagesWW.deathByLynchen(game, player);
				break;
			default:
				MessagesWW.deathByDefault(game, player);
		}

		if (game.gameRulePrintCardOnDeath) {
			Globals.printCard(player.role.name, game.mainChannel);
		}

	}

	@Override
	public void changeDayPhase(DayPhase nextPhase) {
		updateGameBackup(nextPhase);
		updateGameLists();
		if (!checkIfGameEnds()) {
			// transitions to Night
			if (nextPhase == DayPhase.NORMAL_NIGHT) {

				night = new NightSemi(game);
				dayPhase = DayPhase.NORMAL_NIGHT;

				// transitions to Morning
			} else if (nextPhase == DayPhase.MORNING) {

				morning = new MorningSemi(game);
				dayPhase = DayPhase.MORNING;

				// transitions to Day
			} else if (nextPhase == DayPhase.DAY) {

				day = new DaySemi(game);
				dayPhase = DayPhase.DAY;

				// transitions to 1st Night
			} else if (nextPhase == DayPhase.FIRST_NIGHT) {

				firstNight = new FirstNightSemi(game);
				dayPhase = DayPhase.FIRST_NIGHT;
			}
		}

	}

	private void updateGameBackup(DayPhase nextPhase) {
		game.backupGame = game;
		game.backupDayPhase = nextPhase;
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
		var handeled = false;
		if (livingPlayers.containsKey(event.getMessage().getAuthor().get().getId())
				|| event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {

			// checks the Command map of the current DayPhase
			if (dayPhase == DayPhase.MORNING) {
				var foundCommand = morning.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					handeled = true;
				} else if (!handeled && day != null && day.mapCommands.containsKey(requestedCommand)) {
					event.getMessage().getChannel().block().createMessage("This command is only available during Day")
							.block();
					handeled = true;
				} else {
					handeled = false;
				}
			} else if (dayPhase == DayPhase.DAY) {
				var foundCommand = day.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					handeled = true;
				} else if (!handeled && night != null && night.mapCommands.containsKey(requestedCommand)) {
					event.getMessage().getChannel().block().createMessage("This command is only available during Night")
							.block();
					handeled = true;
				} else {
					handeled = false;
				}
			} else if (dayPhase == DayPhase.NORMAL_NIGHT) {
				var foundCommand = night.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					handeled = true;
				} else if (!handeled && morning != null && morning.mapCommands.containsKey(requestedCommand)) {
					event.getMessage().getChannel().block()
							.createMessage("This command is only available during the Morning").block();
					handeled = true;
				} else {
					handeled = false;
				}
			} else if (dayPhase == DayPhase.FIRST_NIGHT) {
				var foundCommand = firstNight.mapCommands.get(requestedCommand);
				if (foundCommand != null) {
					foundCommand.execute(event, parameters, runningInChannel);
					handeled = true;
				} else {
					handeled = false;
				}
			}

			// if mod then mod commands
			if (!handeled && event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				var foundModCommand = modCommands.get(requestedCommand);
				if (foundModCommand != null) {
					foundModCommand.execute(event, parameters, runningInChannel);
					handeled = true;
				} else {
					handeled = false;
				}

			}

			// if not mod and not command in dayphase look in gamestatecommands
			if (!handeled) {
				handeled = super.handleCommand(requestedCommand, event, parameters, runningInChannel);
			}

		} else {
			MessagesWW.errorNoAccessToCommand(game, event.getMessage().getChannel().block());
			handeled = true;
		}

		return handeled;
	}

	private void registerStateCommands() {

		// ping testet ob der bot antwortet
		Command pingCommand = (event, parameters, msgChannel) -> {
			msgChannel.createMessage("Pong! SemiMainGameState").block();

		};
		gameStateCommands.put("ping", pingCommand);

		// ------------------------ HELP --------------------------------

		// zeigt die verfügbaren commands
		Command showCommandsCommand = (event, parameters, msgChannel) -> {
			var mssg = "";
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				mssg += MessagesWW.getModCommands() + "\n";
			}
			mssg += MessagesWW.getCommandsMain();
			mssg += "\n" + MessagesWW.getCommandsGame();
			mssg += "\n" + MessagesWW.getCommandsSemiState();
			mssg += "\n" + MessagesWW.getHelpInfo();
			Globals.createEmbed(msgChannel, Color.CYAN, "Commands", mssg);
		};
		gameStateCommands.put("showCommands", showCommandsCommand);
		gameStateCommands.put("lsCommands", showCommandsCommand);
		gameStateCommands.put("Commands", showCommandsCommand);

		// zeigt die verfügbaren commands
		Command showModCommandsCommand = (event, parameters, msgChannel) -> {
			var mssg = MessagesWW.getModCommands();
			Globals.createEmbed(msgChannel, Color.ORANGE, "Moderator Commands", mssg);
		};
		modCommands.put("showModCommands", showModCommandsCommand);
		modCommands.put("modCommands", showModCommandsCommand);

		// ---------------------- MOD COMMANDS ----------------------------------

		// prints the living players and their role
		Command listPlayersCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.printPlayersMap(msgChannel, game.mapPlayers, "Alle Spieler", true);
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("listPlayers", listPlayersCommand);

		// prints the living players and their role
		Command listLivingCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.printPlayersMap(msgChannel, game.livingPlayers, "Alle Spieler", true);
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("listliving", listLivingCommand);
		modCommands.put("listlivingPlayers", listLivingCommand);

		// ummutes a specific player
		Command muteCommand = (event, parameters, msgChannel) -> {

			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				// finds the requested Player
				var foundPlayer = game.findPlayerByName(parameters.get(0));
				// mutes the found player
				if (foundPlayer != null) {
					foundPlayer.user.asMember(game.server.getId()).block().edit(a -> a.setMute(true)).block();
				} else {
					MessagesWW.errorPlayerNotFound(msgChannel);
				}
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("mute", muteCommand);
		modCommands.put("stfu", muteCommand);

		// ummutes a specific player
		Command unMuteCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				// finds the requested Player
				var foundPlayer = game.findPlayerByName(parameters.get(0));
				// mutes the found player
				if (foundPlayer != null) {
					foundPlayer.user.asMember(game.server.getId()).block().edit(a -> a.setMute(false)).block();
				} else {
					MessagesWW.errorPlayerNotFound(msgChannel);
				}
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("unMute", unMuteCommand);

		// shows the moderator the list of players
		Command muteAllCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.setMuteAllPlayers(game.livingPlayers, true, game.server.getId());
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("muteAll", muteAllCommand);
		modCommands.put("stfuAll", muteAllCommand);

		// shows the moderator the list of players
		Command unMuteAllCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.setMuteAllPlayers(game.livingPlayers, false, game.server.getId());
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("unMuteAll", unMuteAllCommand);

		// lets the moderator kill a person and checks the consequences
		Command killCommand = (event, parameters, msgChannel) -> {
			// only the moderator can use this command
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				if (parameters.size() == 2 || parameters.size() == 1) {
					var delMessg = event.getMessage().getChannel().block().createMessage("einen Moment...").block();

					// finds the requested Player
					var unluckyPlayer = game.findPlayerByNameLiving(parameters.get(0));

					// stores the cause
					var causedBy = "null";
					if (parameters.size() == 2) {
						causedBy = parameters.get(1);
					}

					if (unluckyPlayer != null && (Globals.mapRegisteredCardsSpecs.containsKey(causedBy)
							|| causedBy.equalsIgnoreCase("Null"))) {
						if (unluckyPlayer.role.deathDetails.alive) {
							if (checkIfDies(unluckyPlayer, causedBy)) {
								killPlayer(unluckyPlayer, causedBy);
								event.getMessage().getChannel().block().createMessage("Erfolg!").block();

							}
						} else {
							MessagesWW.errorPlayerAlreadyDead(msgChannel);
						}
					} else {
						MessagesWW.errorWrongSyntaxOnKill(event);
					}
					delMessg.delete().block();
				} else {
					MessagesWW.errorWrongSyntaxOnKill(event);
				}
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("kill", killCommand);

		// used by the mod to make 2 ppl fall in love (amor effect)
		Command setLoveCommand = (event, parameters, msgChannel) -> {
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				if (parameters != null && parameters.size() == 2) {

					// finds the players
					var player1 = game.findPlayerByName(parameters.get(0));
					var player2 = game.findPlayerByName(parameters.get(1));

					// sets the "inLoveWith" variables
					if (player1 != null && player2 != null) {
						if (player1 != player2) {
							player1.role.inLoveWith = player2;
							player2.role.inLoveWith = player1;
							MessagesWW.amorSuccess(game, player1, player2);

						} else {
							MessagesWW.errorPlayersIdentical(msgChannel);
						}
					} else {
						MessagesWW.errorPlayerNotFound(msgChannel);
					}
				} else {
					MessagesWW.errorWrongSyntax(msgChannel);
				}
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("inLove", setLoveCommand);
		modCommands.put("Amor", setLoveCommand);

		// set the doublegangers secret identity
		Command setDoppelgängerinCommand = (event, parameters, msgChannel) -> {
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				if (parameters != null && parameters.size() == 1) {
					// finds the players
					var foundPlayer = game.findPlayerByName(parameters.get(0));

					if (foundPlayer != null) {
						var dp = mapExistingRoles.get("Doppelgängerin").get(0);

						// sets the variable
						var dpRole = (RoleDoppelgängerin) dp.role;
						dpRole.boundTo = foundPlayer;
						MessagesWW.doppelgängerinSuccess(game, dp, foundPlayer);

					} else {
						MessagesWW.errorPlayerNotFound(msgChannel);
					}
				} else {
					MessagesWW.errorWrongSyntax(msgChannel);
				}
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		modCommands.put("clone", setDoppelgängerinCommand);
		modCommands.put("Doppelgängerin", setDoppelgängerinCommand);

		// ---------------------- OTHER COMMANDS ----------------------------------

	}

}
