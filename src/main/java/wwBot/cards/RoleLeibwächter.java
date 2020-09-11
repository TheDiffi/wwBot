
package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.PrivateCommand;

public class RoleLeibwächter extends Role {
    public Player protectingPlayer;

    RoleLeibwächter() {
        super("Leibwächter");

    }

    @Override
    public void executePreWW(Player leibwächter,Game game, AutoState state) {
        MessagesMain.callLeibwächter(leibwächter);

        leibwächter.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand leibwächterCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (protectingPlayer == null || !player.name.equals(protectingPlayer.name))) {
                protectingPlayer = player;
                protectingPlayer.role.deathDetails.deathState = DeathState.PROTECTED;

                state.setDoneNight(leibwächter);
                return true;

            } else if (protectingPlayer != null && player.name.equals(protectingPlayer.name)) {
                MessagesMain.errorChoseIdenticalPlayer(msgChannel);
                return false;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(leibwächter.user.getId(), leibwächterCommand);
    }
}
