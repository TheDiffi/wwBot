package wwBot.GameStates.DayPhases.Auto;

import java.util.HashMap;
import java.util.Map;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.GameStates.MainState.DeathState;
import wwBot.cards.RolePriester;

public class Night {

	public Map<String, Boolean> endChecks = new HashMap<>();

	public Game game;

	public Night(Game getGame) {
		game = getGame;

		MessagesMain.onNightAuto(game);

		preWWPhase();

	}

	private void preWWPhase() {

		// preWWPhase:

		// ZAUBERMEISTERIN
		// PARANOMALER-ERMITTLER (einmalig: erwähnt 2 spieler und erfährt ob mindestens
		// einer der beiden ein ww ist)
		// UNRUHESTIFTERIN (einmalig: darf entscheiden ob nögstem tag 2 personen getötet
		// werden)

		// Seher
		if (game.gameState.mapExistingRoles.containsKey("Seher")) {

			initiateRole("Seher");
		}
		// Aura-Seherin
		if (game.gameState.mapExistingRoles.containsKey("Aura-Seherin")) {

			initiateRole("Aura-Seherin");
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
		// Priester
		if (game.gameState.mapExistingRoles.containsKey("Priester")) {
			var priest = (RolePriester) game.gameState.mapExistingRoles.get("Priester").get(0).role;

			// if the priest has not yet used his ability, he gets the chance to do so. If
			// he already used it and it did not trigger (vanish) yet, the protectedPlayer
			// is PROTECTED
			if (priest.usedAbility) {
				if (!priest.abilityVanished) {
					priest.protectedPlayer.role.deathState = DeathState.PROTECTED;
				}
			} else {
				initiateRole("Priester");
			}
		}
		// Alte-Vettel
		if (game.gameState.mapExistingRoles.containsKey("Alte-Vettel")) {

			initiateRole("Alte-Vettel");
		}

	}

	private void WWPhase() {

	}

	private void postWWPhase() {

	}

	// puts the role in endChecks and executes it
	private void initiateRole(String roleName) {
		var player = game.gameState.mapExistingRoles.get(roleName).get(0);
		endChecks.put(roleName, false);
		player.role.execute(game, player);
	}

}
