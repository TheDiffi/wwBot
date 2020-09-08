package wwBot.GameStates.DayPhases.Auto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.MainState;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.Command;
import wwBot.cards.RoleZauberer;
import wwBot.cards.RolePriester;

public class Night {

	// TODO: Command to show which role has not acted yet
	public Map<String, Boolean> endChecks = new HashMap<>();
	public List<Player> endangeredPlayers = new ArrayList<>();
	private int nightPhase = 0; // 0 = prePhase / 1 = WWPhase / 2 = postPhase

	public Game game;
	private MainState mainState;

	public Night(Game getGame) {
		game = getGame;

		mainState = (MainState) game.gameState;
		mainState.createWerwolfChat();

		MessagesMain.onNightAuto(game);

		preWWPhase();

	}

	private void preWWPhase() {

		// preWWPhase:
		// PARANOMALER-ERMITTLER (einmalig: erwähnt 2 spieler und erfährt ob mindestens
		// einer der beiden ein ww ist)
		// TODO: UNRUHESTIFTERIN (einmalig: darf entscheiden ob nögstem tag 2 personen
		// getötet
		// werden)

		// Seher
		if (game.gameState.mapExistingRoles.containsKey("Seher")) {

			initiateRole("Seher");
		}
		// Aura-Seherin
		if (game.gameState.mapExistingRoles.containsKey("Aura-Seherin")) {

			initiateRole("Aura-Seherin");
		}
		// ZAUBERMEISTERIN
		if (game.gameState.mapExistingRoles.containsKey("Zaubermeisterin")) {

			initiateRole("Zaubermeisterin");
		}
		// SÄUFER
		if (game.gameState.mapExistingRoles.containsKey("Säufer")) {

			initiateRole("Säufer");
		}
		// Leibwächter
		if (game.gameState.mapExistingRoles.containsKey("Leibwächter")) {

			initiateRole("Leibwächter");
		}
		// Alte-Vettel
		if (game.gameState.mapExistingRoles.containsKey("Alte-Vettel")) {

			initiateRole("Alte-Vettel");
		}
		// PARANOMALER-ERMITTLER
		if (game.gameState.mapExistingRoles.containsKey("Paranormaler-Ermittler")) {

			initiateRole("Paranormaler-Ermittler");
		}
		// Priester
		if (game.gameState.mapExistingRoles.containsKey("Priester")) {
			var priest = (RolePriester) game.gameState.mapExistingRoles.get("Priester").get(0).role;

			// if the priest has not yet used his ability, he gets the chance to do so. If
			// he already used it and it did not trigger (vanish) yet, the protectedPlayer
			// is PROTECTED
			if (!priest.usedAbility) {
				initiateRole("Priester");
			}

			/*
			 * if (!priest.abilityVanished) { priest.protectedPlayer.role.deathState =
			 * DeathState.PROTECTED; }
			 */

		}
		// Alte-Vettel
		if (game.gameState.mapExistingRoles.containsKey("Alte-Vettel")) {

			initiateRole("Alte-Vettel");
		}
		// Unruhestifterin
		if (game.gameState.mapExistingRoles.containsKey("Unruhestifterin")) {

			initiateRole("Unruhestifterin");
		}

	}

	private void WWPhase() {
		MessagesMain.onWWTurn(game.mainChannel, mainState.wwChat);

		Command slayCommand = (event, parameters, msgChannel) -> {
			var author = Globals.findPlayerByName(event.getMessage().getAuthor().get().getUsername(), game.mapPlayers,
					game);
			if (author.role.specs.name.equalsIgnoreCase("Werwolf") && msgChannel == mainState.wwChat) {
				var victim = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

				if (victim != null) {
					// updates the DeathState
					if (victim.role.deathState == DeathState.PROTECTED || victim.role.deathState == DeathState.SAVED) {
						victim.role.deathState = DeathState.SAVED;
					} else if (victim.role.deathState == DeathState.ALIVE) {
						victim.role.deathState = DeathState.AT_RISK;
					}

					// other stuff
					MessagesMain.confirm(msgChannel);
					endangeredPlayers.add(victim);
					game.gameCommands.remove("slay");

					nightPhase++;
					changeNightPhaseTo(nightPhase);
				}

			} else {
				MessagesMain.errorWWCommandOnly(event.getMessage().getChannel().block());
			}

		};
		game.gameCommands.put("slay", slayCommand);

	}

	private void postWWPhase() {
		MessagesMain.postWWTurn(game.mainChannel);

		// Magier
		if (game.gameState.mapExistingRoles.containsKey("Magier")) {
			var magier = (RoleZauberer) game.gameState.mapExistingRoles.get("Magier").get(0).role;

			if (!magier.healUsed || !magier.poisonUsed) {
				initiateRole("Magier");

			} else {
				var playerMagier = game.gameState.mapExistingRoles.get("Magier").get(0);
				MessagesMain.callZaubererUsedEverything(playerMagier.user.getPrivateChannel().block());

			}
		}

		// Hexe
		if (game.gameState.mapExistingRoles.containsKey("Hexe")) {
			var hexe = (RoleZauberer) game.gameState.mapExistingRoles.get("Hexe").get(0).role;

			if (!hexe.healUsed || !hexe.poisonUsed) {
				initiateRole("Hexe");

			} else {
				var playerHexe = game.gameState.mapExistingRoles.get("Hexe").get(0);
				MessagesMain.callZaubererUsedEverything(playerHexe.user.getPrivateChannel().block());

			}
		}

	}

	// puts the role in endChecks and executes it
	private void initiateRole(String roleName) {
		var player = game.gameState.mapExistingRoles.get(roleName).get(0);
		endChecks.put(roleName, false);
		player.role.execute(game, player);
	}

	public void endNightCheck() {
		var check = true;
		for (Entry<String, Boolean> done : endChecks.entrySet()) {
			if (!done.getValue()) {
				check = false;
			}
		}

		if (check) {
			nightPhase++;
			changeNightPhaseTo(nightPhase);
		}
	}

	private void changeNightPhaseTo(int newPhase) {
		if (newPhase == 0) {
			preWWPhase();
		} else if (newPhase == 1) {
			WWPhase();
		} else if (newPhase == 2) {
			postWWPhase();
		} else if (newPhase == 3) {
			game.gameState.changeDayPhase(DayPhase.MORNING);
		}
	}

}
