package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;

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

	private void initiateNight() {
		// sorts all roles by when they wake up in a list (i starts at 0 => all Roles
		// that dont wanke up are not added)
		var sortedRoles = new ArrayList<Player>();
		for (int i = 0; i < 4; i++) {

			for (var player : game.livingPlayers.entrySet()) {
				if (player.getValue().role.nightSequence == i) {
					sortedRoles.add(player.getValue());
				}
			}
		}
		if (sortedRoles != null || sortedRoles.size() < 1) {
			MessagesMain.onNightSemi(game, sortedRoles);
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
			// replies to the moderator
			if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
				MessagesMain.helpNightPhaseMod(event);
			} else {
				MessagesMain.helpNightPhase(event);
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
				msgChannel.createMessage("only the moderator can use this command");
			}
		};
		mapCommands.put("endNight", endNightCommand);

		/*
		 * // lets the moderator kill a person and checks the consequences Command
		 * killCommand = (event, parameters, msgChannel) -> { // only the moderator can
		 * use this command if
		 * (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId
		 * ())) { if (parameters.size() == 2) { // finds the requested Player var
		 * unluckyPlayer = Globals.findPlayerByName(parameters.get(0),
		 * game.livingPlayers); // gets the cause var causedBy = parameters.get(1); //
		 * finds the cause (role) var causedByRole = mapRegisteredCards.get(causedBy);
		 * if (unluckyPlayer != null && (causedByRole != null ||
		 * causedBy.equalsIgnoreCase("null"))) { killPlayer(unluckyPlayer,
		 * causedByRole); } else {
		 * event.getMessage().getChannel().block().createMessage(
		 * "Ich verstehe dich nicht :/ Dein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)"
		 * ) .block(); }
		 * 
		 * } else { event.getMessage().getChannel().block().createMessage(
		 * "Ich verstehe dich nicht :/ Dein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)"
		 * ) .block();
		 * 
		 * } } else { event.getMessage().getChannel().block().
		 * createMessage("You have no permission for this command") .block(); } };
		 * mapCommands.put("kill", killCommand);
		 */

	}

	private void endNight() {
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "");
		game.gameState.changeDayPhase();

	}

}
