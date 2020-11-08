package wwBot.WerwolfGame.cards;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.MainState.DeathState;
import wwBot.WerwolfGame.GameStates.DayPhases.Auto.Night;

//not sure if this will work / Magier & Hexe in one role
public class RoleZauberer extends Role {
    public boolean healUsed = false;
    public boolean poisonUsed = false;

    RoleZauberer(String role) {
        super(role);
    }

    @Override
    public void executePostWW(Player zauberer, Game game, AutoState state) {
        if (!healUsed || !poisonUsed) {

            MessagesWW.callZauberer(zauberer, this, ((Night) state.dayPhase).getEndangeredPlayers(), game);

            if (!healUsed) {
                PrivateCommand healCommand = (event, parameters, msgChannel) -> {

                    if (parameters != null && parameters.get(0).equalsIgnoreCase("&heal")) {

                        parameters.remove(0);
                        var target = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

                        if (target != null) {
                            if (target.role.deathDetails.deathState == DeathState.AT_RISK) {
                                // uses the ability to save the player
                                target.role.deathDetails.deathState = DeathState.SAVED;
                                healUsed = true;
                                MessagesWW.confirm(msgChannel);
                                return true;

                            } else {
                                MessagesWW.errorPlayerNotFound(msgChannel);
                                return false;
                            }

                        } else {
                            return false;
                        }

                    } else {
                        return false;
                    }

                };
                game.addPrivateCommand(zauberer.user.getId(), healCommand);
            }

            if (!poisonUsed) {
                PrivateCommand poisonCommand = (event, parameters, msgChannel) -> {

                    if (parameters != null && parameters.get(0).equalsIgnoreCase("&heal")) {
                        parameters.remove(0);

                        var target = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

                        if (target != null) {
                            // updates to AT_RISK Status
                            target.role.deathDetails.deathState = DeathState.AT_RISK;
                            target.role.deathDetails.killer = zauberer.role.name;
                            poisonUsed = true;

                            MessagesWW.confirm(msgChannel);
                            return true;

                        } else {
                            return false;
                        }

                    } else {
                        return false;
                    }

                };
                game.addPrivateCommand(zauberer.user.getId(), poisonCommand);

            }

            PrivateCommand confirmCommand = (event, parameters, msgChannel) -> {

                if (parameters != null && parameters.get(0).equalsIgnoreCase("&continue")) {

                    MessagesWW.confirm(msgChannel);

                    state.setDoneNight(zauberer);

                    return true;

                } else {
                    return false;
                }

            };
            game.addPrivateCommand(zauberer.user.getId(), confirmCommand);


        } else {
            state.setDoneNight(zauberer);
            MessagesWW.callZaubererUsedEverything(zauberer.user.getPrivateChannel().block());

        }
    }
}