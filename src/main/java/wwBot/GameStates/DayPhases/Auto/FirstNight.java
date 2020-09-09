package wwBot.GameStates.DayPhases.Auto;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DayPhase;

//---------------- FIRST NIGHT ----------------------------

public class FirstNight extends AutoDayPhase {

    private Game game;

    public FirstNight(Game getGame) {
        game = getGame;

        MessagesMain.onFirstNightAuto(game);
        
        initiateRoles();
    }

    private void initiateRoles() {

        //executes for every single card
        for (var player : game.mapPlayers.values()) {

            var state = (AutoState) game.gameState;
            state.pending.add(player);
            player.role.executeFirstNight(player, game, state);
            
        } 

    }


	public void changeNightPhase() {
		game.gameState.changeDayPhase(DayPhase.DAY);

	}
   
}


        /* // the Günstling gets a list with all the WW
        if (game.gameState.mapExistingRoles.containsKey("Günstling")) {

            initiateRole("Günstling");
            
        }
        // Seher
        if (game.gameState.mapExistingRoles.containsKey("Seher")) {

            initiateRole("Seher");
        }
        // Amor
        if (game.gameState.mapExistingRoles.containsKey("Amor")) {

            initiateRole("Amor");
        }
        // Doppelgängerin: recieves a plaver in the next mssg that player in her role
        if (game.gameState.mapExistingRoles.containsKey("Doppelgängerin")) {

            initiateRole("Doppelgängerin");

        }

        endNightCheck();
    } */