package wwBot.GameStates.DayPhases;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.Interfaces.Command;

public class Night {
	public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
	Game game;

	public Night(Game getGame) {
		game = getGame;
		registerNightCommands();
		initiateNight();

	}

	private void initiateNight() {
		// sorts all roles by when they wake up in a list (i starts at 0 => all Roles
		// that dont wanke up are not added)
		var sortedRoles = new ArrayList<Player>();
		for (int i = 0; i < 4; i++) {

			for (var player : game.livingPlayers.entrySet()) {
				if (player.getValue().role.specs.nightSequence == i) {
					sortedRoles.add(player.getValue());
				}
			}
		}
		if (sortedRoles != null || sortedRoles.size() < 1) {
			MessagesMain.onNightSemi(game, sortedRoles);
		}
	}

	// --------------- Commands -------------------

	public void registerNightCommands() {

		// replys with pong!
		Command pingCommand = (event, parameters, msgChannel) -> {
			event.getMessage().getChannel().block().createMessage("Pong! NightPhase").block();
		};
		mapCommands.put("ping", pingCommand);

		// shows the available Commands in this Phase
		Command helpCommand = (event, parameters, msgChannel) -> {
			// replies to the moderator
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				MessagesMain.helpNightPhaseMod(msgChannel);
			} else {
				MessagesMain.helpNightPhase(msgChannel);
			}
		};
		mapCommands.put("help", helpCommand);
		mapCommands.put("hilfe", helpCommand);

		// zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = MessagesMain.showCommandsMain();
            mssg = "\n" + MessagesMain.showCommandsGame();
            mssg = "\n" + MessagesMain.showCommandsSemiMainGameState();
            mssg = "\n" + MessagesMain.showCommandsNight();
            msgChannel.createMessage(mssg);
        };
        mapCommands.put("showCommands", showCommandsCommand);

		// shows the moderator the list of players
		Command endNightCommand = (event, parameters, msgChannel) -> {

			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				endNight();
			} else {
				MessagesMain.errorModOnlyCommand(msgChannel);
			}
		};
		mapCommands.put("endNight", endNightCommand);
		mapCommands.put("next", endNightCommand);
		mapCommands.put("end", endNightCommand);

	}

	// --------------- Other -------------------

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

	private void endNight() {
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "");
		game.gameState.changeDayPhase();

	}

}
