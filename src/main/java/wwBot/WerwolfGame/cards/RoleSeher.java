package wwBot.WerwolfGame.cards;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

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

        MessagesWW.callSeher(seher);

        PrivateCommand seherCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {
                // sends the mssg
                MessagesWW.showSeher(seher, player, game, true);

                state.setDoneNight(seher);
                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(seher.user.getId(), seherCommand);
    }
}