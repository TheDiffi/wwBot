package wwBot.WerwolfGame.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import wwBot.Globals;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;

public class MainState extends GameState {

	public TextChannel wwChat = null;
	public TextChannel deathChat = null;

	protected MainState(Game game) {
		super(game);

	}

	// -------------------- Utility --------------------------

	// creates a private MessageChannel
	// Whitelist: WW, Wolfsjunges , Mod
	@Override
	public void createWerwolfChat() {

		if (wwChat != null) {
			deleteWerwolfChat();
		}

		var defaultRole = game.server.getRoles().toStream().filter(r -> r.getName().equals("@everyone")).findFirst()
				.get();
		var tempList = new ArrayList<Player>();

		// adds Wolfsjunges
		if (mapExistingRoles.containsKey("Wolfsjunges")) {
			tempList.add(mapExistingRoles.get("wolfsjunges").get(0));
		}

		// adds the WW
		for (var player : mapExistingRoles.get("Werwolf")) {
			if (player.role.deathDetails.alive) {
				tempList.add(player);
			}
		}

		wwChat = game.server.createTextChannel(spec -> {
			var overrides = new HashSet<PermissionOverwrite>();

			// @everyone can't see this channel
			overrides.add(PermissionOverwrite.forRole(defaultRole.getId(), PermissionSet.none(),
					PermissionSet.of(Permission.VIEW_CHANNEL)));

			// these members can see the channel
			for (var player : tempList) {
				overrides.add(PermissionOverwrite.forMember(player.user.asMember(game.server.getId()).block().getId(),
						PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}
			// adds the mod
			if (!game.gameRuleAutomaticMod) {
				overrides.add(
						PermissionOverwrite.forMember(game.userModerator.asMember(game.server.getId()).block().getId(),
								PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}

			// sets the permissions
			spec.setPermissionOverwrites(overrides);
			spec.setName("Privater Werwolf-Chat");
		}).block();

		// Sends the first messages, explaining this Chat
		MessagesWW.wwChatGreeting(wwChat);
		Globals.createEmbed(wwChat, Color.LIGHT_GRAY, "", Globals.playerListToList(tempList, "Werwölfe Sind", true));

	}

	// if present, deletes the wwChat
	@Override
	public void deleteWerwolfChat() {

		if (wwChat != null) {
			game.server.getChannelById(wwChat.getId()).block().delete().block();
			wwChat = null;
		} else {
			game.mainChannel.createMessage("No Channel Found").block();
		}
	}

	@Override
	public void createDeathChat() {

		if (deathChat != null) {
			deleteDeathChat();
		}

		var defaultRole = game.server.getRoles().toStream().filter(r -> r.getName().equals("@everyone")).findFirst()
				.get();
		deathChat = game.server.createTextChannel(spec -> {
			var overrides = new HashSet<PermissionOverwrite>();
			overrides.add(PermissionOverwrite.forRole(defaultRole.getId(), PermissionSet.none(),
					PermissionSet.of(Permission.VIEW_CHANNEL)));

			if (!game.gameRuleAutomaticMod) {
				overrides.add(
						PermissionOverwrite.forMember(game.userModerator.asMember(game.server.getId()).block().getId(),
								PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}

			spec.setPermissionOverwrites(overrides);
			spec.setName("Friedhof-Chat");
		}).block();

		// Sends the first messages, explaining this Chat
		MessagesWW.deathChatGreeting(deathChat, game);
		Globals.printPlayersMap(deathChat, game.mapPlayers, "Alle Spieler", true);

	}

	public void updateDeathChat() {
		// adds him to the deathChat and mutes him
		var defaultRole = game.server.getRoles().toStream().filter(r -> r.getName().equals("@everyone")).findFirst()
				.get();

		deathChat.edit(spec -> {
			var overrides = new HashSet<PermissionOverwrite>();
			overrides.add(PermissionOverwrite.forRole(defaultRole.getId(), PermissionSet.none(),
					PermissionSet.of(Permission.VIEW_CHANNEL)));
			for (var player : game.deadPlayers) {
				overrides.add(PermissionOverwrite.forMember(player.user.asMember(game.server.getId()).block().getId(),
						PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}
			if (!game.gameRuleAutomaticMod) {
				overrides.add(
						PermissionOverwrite.forMember(game.userModerator.asMember(game.server.getId()).block().getId(),
								PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}

			spec.setPermissionOverwrites(overrides);
		}).block();
	}

	// if present, deletes the deathChat
	@Override
	public void deleteDeathChat() {

		if (deathChat != null) {
			game.server.getChannelById(deathChat.getId()).block().delete().block();
			deathChat = null;
		} else {
			game.mainChannel.createMessage("No Channel Found").block();
		}
	}

	public void updateGameLists() {
		// reloads the living Players
		livingPlayers.clear();
		for (var player : game.mapPlayers.entrySet()) {
			if (player.getValue().role.deathDetails.alive) {
				livingPlayers.put(player.getKey(), player.getValue());
			}
		}

		game.livingPlayers = livingPlayers;

		// läd jede noch Player der noch lebt als nach der Rolle geordnet in eine Map
		// mit dem Rollennamen als Key (Value = Liste wo alle Player mit derselben Rolle
		// vorhanden sind)
		mapExistingRoles.clear();
		var listWerwölfe = new ArrayList<Player>();
		var listDorfbewohner = new ArrayList<Player>();
		var listSeher = new ArrayList<Player>();

		for (var entry : livingPlayers.entrySet()) {
			// prüft ob WW, Dorfbewohner oder Seher, falls nichts von dem bekommt die Rolle
			// ihre eigene Liste
			if (entry.getValue().role.name.equalsIgnoreCase("Werwolf") && entry.getValue().role.deathDetails.alive) {
				listWerwölfe.add(entry.getValue());
			} else if (entry.getValue().role.name.equalsIgnoreCase("Seher")
					&& entry.getValue().role.deathDetails.alive) {
				listSeher.add(entry.getValue());
			} else if (entry.getValue().role.name.equalsIgnoreCase("Dorfbewohner")
					&& entry.getValue().role.deathDetails.alive) {
				listDorfbewohner.add(entry.getValue());
			} else {
				var tempList = Arrays.asList(entry.getValue());
				mapExistingRoles.put(entry.getValue().role.name, tempList);

			}
		}

		mapExistingRoles.put("Werwolf", listWerwölfe);
		mapExistingRoles.put("Seher", listSeher);
		mapExistingRoles.put("Dorfbewohner", listDorfbewohner);

	}

	// collects every "good" and every "bad" role in a list and compares the size.
	// If the are equaly or less "good" than "bad" roles, the ww won
	public int calculateGameEnd() {
		var amountGoodPlayers = 0;
		var amountWW = 0;

		for (var playerEntry : game.livingPlayers.values()) {
			if (playerEntry.role.name.equalsIgnoreCase("Werwolf")
					|| playerEntry.role.name.equalsIgnoreCase("Wolfsjunges")) {
				amountWW++;
			} else {
				amountGoodPlayers++;
			}
		}

		if (livingPlayers.size() == 0) {
			return 3;

		}
		if (amountWW < 1) {
			return 1;

		} else if (amountWW >= amountGoodPlayers) {
			return 2;

		} else {
			return 0;
		}
	}

	@Override
	public boolean checkIfGameEnds() {
		return endMainGame(calculateGameEnd());
	}

	/**
	 * Ends the Game based on an EndCode
	 * 
	 * @param winner winner: 1 = Dorfbewohner, 2 = Werwölfe, 3 = Ausgleich
	 */

	public boolean endMainGame(int winner) {
		if (winner == 0) {
			// 0 means the game is still going
			return false;
		}

		// unmutes all players
		Globals.setMuteAllPlayers(game.livingPlayers, false, game.server.getId());
		// deletes deathChat
		deleteDeathChat();
		// sends gameover message
		if (winner == 1) {
			Globals.createEmbed(game.mainChannel, Color.GREEN, "GAME END: DIE DORFBEWOHNER GEWINNEN!", "");
		} else if (winner == 2) {
			Globals.createEmbed(game.mainChannel, Color.RED, "GAME END: DIE WERWÖLFE GEWINNEN!", "");
		} else if (winner == 3) {
			Globals.createEmbed(game.mainChannel, Color.GRAY, "GAME END: UNENTSCHIEDEN!", "");
		}
		// changes gamestate
		game.changeGameState(new PostGameState(game, winner));
		return true;
	}

	public enum DayPhase {
		FIRST_NIGHT, NORMAL_NIGHT, DAY, MORNING
	}

	public enum DeathState {
		ALIVE, PROTECTED, SAVED, AT_RISK
	}

}