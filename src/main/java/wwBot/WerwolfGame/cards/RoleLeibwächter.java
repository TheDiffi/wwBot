
package wwBot.WerwolfGame.cards;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.MainState.DeathState;

public class RoleLeibwächter extends Role {
    public Player protectingPlayer;

    RoleLeibwächter() {
        super("Leibwächter");

    }

    @Override
    public void executePreWW(Player leibwächter,Game game, AutoState state) {
        MessagesWW.callLeibwächter(leibwächter);

        PrivateCommand leibwächterCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (protectingPlayer == null || !player.name.equals(protectingPlayer.name))) {
                protectingPlayer = player;
                protectingPlayer.role.deathDetails.deathState = DeathState.PROTECTED;
                MessagesWW.confirm(msgChannel);

                state.setDoneNight(leibwächter);
                return true;

            } else if (protectingPlayer != null && player.name.equals(protectingPlayer.name)) {
                MessagesWW.errorChoseIdenticalPlayer(msgChannel);
                return false;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(leibwächter.user.getId(), leibwächterCommand);
    }
}
