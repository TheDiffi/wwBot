package wwBot.GameStates;

import java.awt.Color;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.DayPhases.Semi.DaySemi;
import wwBot.GameStates.DayPhases.Semi.FirstNightSemi;
import wwBot.GameStates.DayPhases.Semi.MorningSemi;
import wwBot.GameStates.DayPhases.Semi.NightSemi;
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

	}

	public void start() {
		// registers Commands; loads the lists and creates the Deathroom
		registerStateCommands();
		createDeathChat();
		updateGameLists();

		// sends the first messages
		MessagesMain.onGameStartSemi(game);
		greetMod(game);
	}

	// greets the mod and waits for the mod to start the first night
	private void greetMod(Game game) {
		MessagesMain.greetMod(game);
		Globals.printPlayersMap(game.userModerator.getPrivateChannel().block(), game.mapPlayers, "Alle Spieler", game,
				true);

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
		if (!unluckyPlayer.role.deathDetails.alive) {
			MessagesMain.errorPlayerAlreadyDead(modChannel);
			dies = false;

		}

		// VERFLUCHTER
		if (unluckyPlayer.role.name.equals("Verfluchter") && causedByRole.equalsIgnoreCase("Werwolf")) {
			unluckyPlayer.role = Role.createRole("Werwolf");
			MessagesMain.verfluchtenMutation(game);
			dies = false;
		}

		// HARTER BURSCHE
		if (unluckyPlayer.role.name.equals("Harter-Bursche") && !causedByRole.equalsIgnoreCase("Confirmed")) {
			MessagesMain.checkHarterBurscheDeath(modChannel);
			dies = false;

			// if confirmed it kills the player. if canceled nothing happenes
			PrivateCommand confirmCommand = (event, parameters, msgChannel) -> {

				if (parameters != null && parameters.get(0).equalsIgnoreCase("confirm")) {
					killPlayer(unluckyPlayer, "Confirmed");
					MessagesMain.confirm(msgChannel);
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
			MessagesMain.prinzSurvives(game);
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

		try {
			unluckyPlayer.user.asMember(game.server.getId()).block().edit(a -> a.setMute(true)).block();
		} catch (Exception e) {
		}

		// reveals the players death and identity
		checkDeathMessages(unluckyPlayer, causedByRole);

		// calculates the consequences
		checkConsequences(unluckyPlayer, causedByRole);

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

			PrivateCommand jägerCommand = (event, parameters, msgChannel) -> {
				var foundPlayer = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

				if (foundPlayer != null) {
					MessagesMain.confirm(msgChannel);
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
				MessagesMain.onDoppelgängerinTransformation(game, dpPlayer, unluckyPlayer);
			}
		}

	}

	private void checkDeathMessages(Player player, String cause) {

		switch (cause) {
			case "Werwolf":
				MessagesMain.deathByWW(game, player);
			case "Hexe":
				MessagesMain.deathByMagic(game, player);
			case "Magier":
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

		if (game.gameRulePrintCardOnDeath) {
			Globals.printCard(player.role.name, game.mainChannel);
		}

	}

	@Override
	public void changeDayPhase(DayPhase nextPhase) {
		updateGameLists();
		// transitions to Night
		if (nextPhase == DayPhase.NORMAL_NIGHT) {
			checkIfGameEnds();
			if (game.gameRuleMutePlayersAtNight) {
				Globals.setMuteAllPlayers(game.livingPlayers, true, game.server.getId());
			}
			createWerwolfChat();

			night = new NightSemi(game);
			dayPhase = DayPhase.NORMAL_NIGHT;

			// transitions to Morning
		} else if (nextPhase == DayPhase.MORNING) {
			if (game.gameRuleMutePlayersAtNight) {
				Globals.setMuteAllPlayers(game.livingPlayers, false, game.server.getId());
			}
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

			if (!handeled) {
				handeled = super.handleCommand(requestedCommand, event, parameters, runningInChannel);
			}

		} else {
			MessagesMain.errorNoAccessToCommand(game, event.getMessage().getChannel().block());
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
		gameStateCommands.put("lsCommands", showCommandsCommand);

		// zeigt die verfügbaren commands
		Command showModCommandsCommand = (event, parameters, msgChannel) -> {
			var mssg = "**To show Moderator Commands type \"&modCommands\"** ";
			mssg += "\n" + MessagesMain.getModCommands();
			mssg += "\n" + MessagesMain.getHelpInfo();

			Globals.createEmbed(msgChannel, Color.ORANGE, "Moderator Commands", mssg);
		};
		gameStateCommands.put("showModCommands", showModCommandsCommand);
		gameStateCommands.put("modCommands", showModCommandsCommand);

		// prints the living players and their role
		Command listPlayersCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.printPlayersMap(msgChannel, game.mapPlayers, "Alle Spieler", game, true);
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("listPlayers", listPlayersCommand);

		// prints the living players and their role
		Command listLivingCommand = (event, parameters, msgChannel) -> {
			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
				Globals.printPlayersMap(msgChannel, game.livingPlayers, "Alle Spieler", game, true);
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
				var foundPlayer = game.findPlayerByName(parameters.get(0));
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
				var foundPlayer = game.findPlayerByName(parameters.get(0));
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
				Globals.setMuteAllPlayers(game.livingPlayers, true, game.server.getId());
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
				Globals.setMuteAllPlayers(game.livingPlayers, false, game.server.getId());
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
							MessagesMain.errorPlayerAlreadyDead(msgChannel);
						}
					} else {
						MessagesMain.errorWrongSyntaxOnKill(event);
					}
				} else {
					MessagesMain.errorWrongSyntaxOnKill(event);
				}
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		gameStateCommands.put("kill", killCommand);

	}

}
