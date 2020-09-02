
package wwBot.cards;

import java.awt.Color;

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
        MessagesMain.callVettel(vettel);
        vettel.user.getPrivateChannel().block().createMessage("TEST");

        //seaches player and sets him to banishedPlayer
        PrivateCommand vettelCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (banishedPlayer == null || !player.name.equals(banishedPlayer.name))) {

                banishedPlayer = player;
                Globals.createEmbed(msgChannel, Color.GREEN, "Erfolg", banishedPlayer.name + " wurde aus dem Dorf verbannt.");

                setDone(game, "AlteVettel");
                return true;

            } else if (banishedPlayer != null && player.name.equals(banishedPlayer.name)) {
                MessagesMain.errorChoseIdenticalPlayer(msgChannel);
                return false;
                
            } else {
                return false;
            }
            
        };
        game.addPrivateCommand(vettel.user.getId(), vettelCommand);
    }


}