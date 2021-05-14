package wwBot.WerwolfGame.GameStates.DayPhases.Auto;

import java.util.ArrayList;
import java.util.List;

import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.MainState.DayPhase;
import wwBot.WerwolfGame.GameStates.MainState.DeathState;
import wwBot.WerwolfGame.cards.RoleWerwolf;

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
			MessagesWW.sendHelpNight(msgChannel, true);
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
		if(!state.pending.isEmpty()){
			MessagesWW.erwachenSpieler(game.mainChannel, state.pending);
		}
		if (state.pending == null || state.pending.isEmpty()) {
            nextNightPhase();
        }
	}

	private void WWPhase() {
		MessagesWW.onWWTurn(game.mainChannel, state.wwChat);

		Command slayCommand = (event, parameters, msgChannel) -> {
			var author = game.mapPlayers.get(event.getMessage().getAuthor().get().getId());
			if (author.role.getClass() == RoleWerwolf.class && msgChannel.equals(state.wwChat) && author.role.deathDetails.alive) {
				var victim = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

				if (victim != null) {
					// gets DeathState
					var dState = victim.role.deathDetails.deathState;
					// updates DeathState
					if (dState == DeathState.PROTECTED) {
						dState = DeathState.SAVED;

					} else if (dState == DeathState.ALIVE) {
						dState = DeathState.AT_RISK;
						victim.role.deathDetails.killer = "Werwolf";

					} else if (dState == DeathState.AT_RISK && victim.role.deathDetails.killer.equalsIgnoreCase("Werwolf")) {

					}

					// sets DeathState
					victim.role.deathDetails.deathState = dState;

					// other stuff
					MessagesWW.confirm(msgChannel);

					if (state.wwEnraged) {
						state.wwEnraged = false;
						MessagesWW.wwEnraged(state.wwChat);

					} else {
						game.gameCommands.remove("slay");
						nextNightPhase();
					}

				}

			} else {
				MessagesWW.errorWWCommandOnly(event.getMessage().getChannel().block());
			}

		};
		mapCommands.put("slay", slayCommand);

		// don't put a endNightPhaseCheck() here
	}

	private void postWWPhase() {
		MessagesWW.postWWTurn(game.mainChannel);

		// executes for every single card
		for (var player : game.livingPlayers.values()) {

			state.setPending(player);
			player.role.executePostWW(player, game, state);

		}

		state.endNightPhaseCheck();

	}

	public void nextNightPhase() {
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
