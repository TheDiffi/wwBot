
package wwBot.WerwolfGame.cards;

import java.awt.Color;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleAlteVettel extends Role {
    public Player banishedPlayer;

    RoleAlteVettel() {
        super("AlteVettel");

    }

    @Override
    public void executePreWW(Player vettel, Game game, AutoState state) {
        MessagesWW.callVettel(vettel);
        vettel.user.getPrivateChannel().block().createMessage("TEST");

        //seaches player and sets him to banishedPlayer
        PrivateCommand vettelCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (banishedPlayer == null || !player.name.equals(banishedPlayer.name))) {

                banishedPlayer = player;
                Globals.createEmbed(msgChannel, Color.GREEN, "Erfolg", banishedPlayer.name + " wurde aus dem Dorf verbannt.");

                state.setDoneNight(vettel);
                return true;

            } else if (banishedPlayer != null && player.name.equals(banishedPlayer.name)) {
                MessagesWW.errorChoseIdenticalPlayer(msgChannel);
                return false;
                
            } else {
                return false;
            }
            
        };
        game.addPrivateCommand(vettel.user.getId(), vettelCommand);
    }


}