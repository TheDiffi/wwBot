package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleSeher extends Role {

    RoleSeher() {
        super("Seher");
    }

    @Override
    public void executeFirstNight(Player seher, Game game, AutoState state)
    {
        executePreWW(seher, game, state);
    }

    // a mightySeher is able to see the exact role of the player, a normal one only
	// if the player is friendly or not
    @Override
    public void executePreWW(Player seher, Game game, AutoState state) {

        MessagesMain.callSeher(seher);

        PrivateCommand seherCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {
                // sends the mssg
                MessagesMain.showSeher(seher, player, game, true);

                state.setDoneNight(seher);
                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(seher.user.getId(), seherCommand);
    }
}