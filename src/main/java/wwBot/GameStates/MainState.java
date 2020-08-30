package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;

public class MainState extends GameState{

    public TextChannel wwChat = null;
	public TextChannel deathChat = null;

	

    protected MainState(Game game) {
        super(game);
        
    }



    // --------------------- Commands ----------------------------




	// -------------------- Utility --------------------------
	
	@Override
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

	// creates a private MessageChannel and puts all the WW and the Moderator ob the
	// Whitelist
	@Override
	public void createWerwolfChat() {

		if (wwChat != null) {
			deleteWerwolfChat();
		}

		var defaultRole = game.server.getRoles().toStream().filter(r -> r.getName().equals("@everyone")).findFirst()
				.get();

		wwChat = game.server.createTextChannel(spec -> {
			var overrides = new HashSet<PermissionOverwrite>();
			overrides.add(PermissionOverwrite.forRole(defaultRole.getId(), PermissionSet.none(),
					PermissionSet.of(Permission.VIEW_CHANNEL)));
			for (var player : mapExistingRoles.get("Werwolf")) {
				overrides.add(PermissionOverwrite.forMember(player.user.asMember(game.server.getId()).block().getId(),
						PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}
			if (!game.gameRuleAutomatic) {
				overrides.add(
						PermissionOverwrite.forMember(game.userModerator.asMember(game.server.getId()).block().getId(),
								PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}

			spec.setPermissionOverwrites(overrides);
			spec.setName("Privater Werwolf-Chat");
		}).block();

		// Sends the first messages, explaining this Chat
		MessagesMain.wwChatGreeting(wwChat);
		Globals.createEmbed(wwChat, Color.LIGHT_GRAY, "",
				Globals.playerListToString(mapExistingRoles.get("Werwolf"), "Werwölfe Sind", game));

		
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

			if (!game.gameRuleAutomatic) {
				overrides.add(
						PermissionOverwrite.forMember(game.userModerator.asMember(game.server.getId()).block().getId(),
								PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
			}

			spec.setPermissionOverwrites(overrides);
			spec.setName("Friedhof-Chat");
		}).block();

		// Sends the first messages, explaining this Chat
		MessagesMain.deathChatGreeting(deathChat, game);
		Globals.printPlayersMap(deathChat, game.mapPlayers, "Alle Spieler", game);

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
			if (!game.gameRuleAutomatic) {
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

	public void loadGameLists() {
		// reloads the living Players
		livingPlayers.clear();
		for (var player : game.mapPlayers.entrySet()) {
			if (player.getValue().role.alive) {
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
			if (entry.getValue().role.name.equalsIgnoreCase("Werwolf") && entry.getValue().role.alive) {
				listWerwölfe.add(entry.getValue());
			} else if (entry.getValue().role.name.equalsIgnoreCase("Seher") && entry.getValue().role.alive) {
				listSeher.add(entry.getValue());
			} else if (entry.getValue().role.name.equalsIgnoreCase("Dorfbewohner") && entry.getValue().role.alive) {
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
	@Override
	public boolean checkIfGameEnds() {

		var amountGoodPlayers = 0;
		var amountWW = 0;
		for (var playerEntry : game.livingPlayers.entrySet()) {
			if (playerEntry.getValue().role.name.equalsIgnoreCase("Werwolf")) {
				amountWW++;
			} else {
				amountGoodPlayers++;
			}

		}
		if (amountWW < 1) {
			// int winner: 1 = Dorfbewohner, 2 = Werwölfe
			game.gameState.endMainGame(1);
			return true;
		} else if (amountWW >= amountGoodPlayers) {
			game.gameState.endMainGame(2);
			return true;
		} else {
			return false;
		}
	}

	public enum DayPhase {
		FIRST_NIGHT, NORMAL_NIGHT, DAY, MORNING
	}

	public enum DeathState {
		ALIVE, PROTECTED, SAVED, AT_RISK
	}
    
}