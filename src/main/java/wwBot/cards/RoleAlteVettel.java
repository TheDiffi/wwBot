
package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.Interfaces.PrivateCommand;

public class RoleAlteVettel extends Role {
    public Player banishedPlayer;

    RoleAlteVettel() {
        super("AlteVettel");

    }

    @Override
    public void execute(Game game, Player vettel) {
        /// TODO: send mssg
        vettel.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand vettelCommand = (event, parameters, msgChannel) -> {
            var player = Globals.privateCommandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (banishedPlayer == null || !player.name.equals(banishedPlayer.name))) {
                banishedPlayer = player;

                setDone(game, "AlteVettel");
                return true;

            } else if (banishedPlayer != null && player.name.equals(banishedPlayer.name)) {
                MessagesMain.errorChoseIdenticalPlayer();
                return false;
                
            } else {
                return false;
            }
            
        };
        game.addPrivateCommand(vettel.user.getId(), vettelCommand);
    }
}