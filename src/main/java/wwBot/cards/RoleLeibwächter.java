
package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.PrivateCommand;

public class RoleLeibwächter extends Role {
    public Player protectingPlayer;

    RoleLeibwächter() {
        super("Leibwächter");

    }

    @Override
    public void execute(Game game, Player leibwächter) {
        /// TODO: send mssg
        leibwächter.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand leibwächterCommand = (event, parameters, msgChannel) -> {
            var player = Globals.privateCommandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null && (protectingPlayer == null || !player.name.equals(protectingPlayer.name))) {
                protectingPlayer = player;
                protectingPlayer.role.deathState = DeathState.PROTECTED;

                setDone(game, "Leibwächter");
                return true;

            } else if (protectingPlayer != null && player.name.equals(protectingPlayer.name)) {
                MessagesMain.errorChoseIdenticalPlayer();
                return false;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(leibwächter.user.getId(), leibwächterCommand);
    }
}
