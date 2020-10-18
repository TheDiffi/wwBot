package wwBot.GameStates.DayPhases.Auto;

import java.util.ArrayList;
import java.util.List;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.Command;

public class Night extends AutoDayPhase {

	private int nightPhase = 0; // 0 = prePhase / 1 = WWPhase / 2 = postPhase
	public Game game;
	private AutoState state;

	public Night(Game getGame) {
		game = getGame;
		state = (AutoState) game.gameState;

		// loads the Commands of the state
		registerCommands();

		preWWPhase();
	}

	// loads all of the following Commands into mapCommands
	public void registerCommands() {

		// replys with pong!
		Command pingCommand = (event, parameters, msgChannel) -> {
			event.getMessage().getChannel().block().createMessage("Pong! NightPhase").block();
		};
		mapCommands.put("ping", pingCommand);

		// help
		Command helpCommand = (event, parameters, msgChannel) -> {
			MessagesMain.sendHelpNight(msgChannel, true);
		};
		mapCommands.put("help", helpCommand);
		mapCommands.put("hilfe", helpCommand);

	}


	// PARANOMALER-ERMITTLER (einmalig: erwähnt drei Süieler und erfährt von zufälligen 2 ob sie friendly sind)
	private void preWWPhase() {
		// executes for every single card
		for (var player : game.livingPlayers.values()) {
			state.setPending(player);
			player.role.executePreWW(player, game, state);

		}
		state.endNightPhaseCheck();
	}

	private void WWPhase() {
		MessagesMain.onWWTurn(game.mainChannel, state.wwChat);

		Command slayCommand = (event, parameters, msgChannel) -> {
			var author = game.findPlayerByName(event.getMessage().getAuthor().get().getUsername());
			if (author.role.name.equalsIgnoreCase("Werwolf") && msgChannel.equals(state.wwChat)) {
				var victim = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

				if (victim != null) {
					var dState = victim.role.deathDetails.deathState;
					// updates the DeathState
					if (dState == DeathState.PROTECTED) {
						dState = DeathState.SAVED;

					} else if (dState == DeathState.ALIVE) {
						dState = DeathState.AT_RISK;
						
						    //TODO: überprüfe ob killer gesetzt werden
						victim.role.deathDetails.killer = author.role.name;

					}
					victim.role.deathDetails.deathState = dState;

					// other stuff
					MessagesMain.confirm(msgChannel);

					if (state.wwEnraged) {
						state.wwEnraged = false;
						MessagesMain.wwEnraged(state.wwChat);

					} else {
						game.gameCommands.remove("slay");
						changeNightPhase();
					}

				}

			} else {
				MessagesMain.errorWWCommandOnly(event.getMessage().getChannel().block());
			}

		};
		mapCommands.put("slay", slayCommand);

		// don't put a endNightPhaseCheck() here
	}

	private void postWWPhase() {
		MessagesMain.postWWTurn(game.mainChannel);

		// executes for every single card
		for (var player : game.livingPlayers.values()) {

			state.setPending(player);
			player.role.executePostWW(player, game, state);

		}

		state.endNightPhaseCheck();

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
