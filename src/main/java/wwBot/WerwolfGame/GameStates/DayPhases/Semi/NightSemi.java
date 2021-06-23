package wwBot.WerwolfGame.GameStates.DayPhases.Semi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.GameState;
import wwBot.WerwolfGame.GameStates.MainState.DayPhase;

public class NightSemi {
	public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
	GameState gameState;
	Game game;

	public NightSemi (Game getGame) {
		game = getGame;
		gameState = game.gameState;
		registerNightCommands();

		if (game.gameRuleMutePlayersAtNight) {
			Globals.setMuteAllPlayers(game.livingPlayers, true, game.server.getId());
		}

		gameState.createWerwolfChat();

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
			MessagesWW.onNightSemi(game, sortedRoles);
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
				MessagesWW.sendHelpNightMod(msgChannel);
			} else {
				MessagesWW.sendHelpNight(msgChannel, false);
			}
		};
		mapCommands.put("help", helpCommand);
		mapCommands.put("hilfe", helpCommand);


		// shows the moderator the list of players
		Command endNightCommand = (event, parameters, msgChannel) -> {

			// compares the Snowflake of the Author to the Snowflake of the Moderator
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				endNight();
			} else {
				MessagesWW.errorModOnlyCommand(msgChannel);
			}
		};
		mapCommands.put("endNight", endNightCommand);
		mapCommands.put("next", endNightCommand);
		mapCommands.put("end", endNightCommand);

	}

	// --------------- Other -------------------



	private void endNight() {
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "Switching to Morning");
		game.gameState.changeDayPhase(DayPhase.MORNING);

	}

}
