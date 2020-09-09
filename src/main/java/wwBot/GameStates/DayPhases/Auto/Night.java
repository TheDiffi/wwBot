package wwBot.GameStates.DayPhases.Auto;

import java.util.ArrayList;
import java.util.List;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.Command;

public class Night extends AutoDayPhase {

	// TODO: Command to show which role has not acted yet
	private int nightPhase = 0; // 0 = prePhase / 1 = WWPhase / 2 = postPhase
	public Game game;

	public Night(Game getGame) {
		game = getGame;

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

		// executes for every single card
		for (var player : game.mapPlayers.values()) {

			var state = (AutoState) game.gameState;
			state.pending.add(player);
			player.role.executePreWW(player, game, state);

		}

		/*
		 * // Seher if (game.gameState.mapExistingRoles.containsKey("Seher")) {
		 * 
		 * initiateRole("Seher"); } // Aura-Seherin if
		 * (game.gameState.mapExistingRoles.containsKey("Aura-Seherin")) {
		 * 
		 * initiateRole("Aura-Seherin"); } // ZAUBERMEISTERIN if
		 * (game.gameState.mapExistingRoles.containsKey("Zaubermeisterin")) {
		 * 
		 * initiateRole("Zaubermeisterin"); } // SÄUFER if
		 * (game.gameState.mapExistingRoles.containsKey("Säufer")) {
		 * 
		 * initiateRole("Säufer"); } // Leibwächter if
		 * (game.gameState.mapExistingRoles.containsKey("Leibwächter")) {
		 * 
		 * initiateRole("Leibwächter"); } // Alte-Vettel if
		 * (game.gameState.mapExistingRoles.containsKey("Alte-Vettel")) {
		 * 
		 * initiateRole("Alte-Vettel"); } // PARANOMALER-ERMITTLER if
		 * (game.gameState.mapExistingRoles.containsKey("Paranormaler-Ermittler")) {
		 * 
		 * initiateRole("Paranormaler-Ermittler"); } // Priester if
		 * (game.gameState.mapExistingRoles.containsKey("Priester")) {
		 * 
		 * initiateRole("Priester");
		 * 
		 * } // Alte-Vettel if
		 * (game.gameState.mapExistingRoles.containsKey("Alte-Vettel")) {
		 * 
		 * initiateRole("Alte-Vettel"); } // Unruhestifterin if
		 * (game.gameState.mapExistingRoles.containsKey("Unruhestifterin")) {
		 * 
		 * initiateRole("Unruhestifterin"); }
		 */

	}

	// TODO: wwEnraged
	private void WWPhase() {
		var mainState = (MainState) game.gameState;
		MessagesMain.onWWTurn(game.mainChannel, mainState.wwChat);

		Command slayCommand = (event, parameters, msgChannel) -> {
			var author = game.findPlayerByName(event.getMessage().getAuthor().get().getUsername());
			if (author.role.specs.name.equalsIgnoreCase("Werwolf") && msgChannel == mainState.wwChat) {
				var victim = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

				if (victim != null) {
					var dState = victim.role.deathDetails.deathState;
					// updates the DeathState
					if (dState == DeathState.PROTECTED) {
						dState = DeathState.SAVED;

					} else if (dState == DeathState.ALIVE) {
						dState = DeathState.AT_RISK;
						victim.role.deathDetails.killer = author.role.name;

					}

					// other stuff
					MessagesMain.confirm(msgChannel);
					game.gameCommands.remove("slay");

					changeNightPhase();
				}

			} else {
				MessagesMain.errorWWCommandOnly(event.getMessage().getChannel().block());
			}

		};
		game.gameCommands.put("slay", slayCommand);

	}

	private void postWWPhase() {
		MessagesMain.postWWTurn(game.mainChannel);

		// executes for every single card
		for (var player : game.mapPlayers.values()) {

			var state = (AutoState) game.gameState;
			state.pending.add(player);
			player.role.executePostWW(player, game, state);

		}

		/*
		 * // Hexe if (game.gameState.mapExistingRoles.containsKey("Hexe")) { var hexe =
		 * (RoleZauberer) game.gameState.mapExistingRoles.get("Hexe").get(0).role;
		 * 
		 * if (!hexe.healUsed || !hexe.poisonUsed) { initiateRole("Hexe");
		 * 
		 * } else { var playerHexe = game.gameState.mapExistingRoles.get("Hexe").get(0);
		 * MessagesMain.callZaubererUsedEverything(playerHexe.user.getPrivateChannel().
		 * block());
		 * 
		 * } }
		 */

	}

	public void changeNightPhase() {
		nightPhase++;
		changeNightPhaseTo(nightPhase);

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

	public List<Player> getEndangeredPlayers() {
		var list = new ArrayList<Player>();
		for (var entry : game.livingPlayers.entrySet()) {
			if (entry.getValue().role.deathDetails.deathState == DeathState.AT_RISK) {
				list.add(entry.getValue());
			}
		}
		return list;
	}

}
