package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.Interfaces.PrivateCommand;

public class RoleSeher extends Role {

    RoleSeher() {
        super("Seher");
    }

    @Override
    public void execute(Game game, Player seher) {
        MessagesMain.callSeher(seher);

        PrivateCommand seherCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {
                // sends the mssg
                //TODO: Lykantrophin
                MessagesMain.showSeher(seher, player, game);

                setDone(game, "Seher");
                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(seher.user.getId(), seherCommand);
    }
}