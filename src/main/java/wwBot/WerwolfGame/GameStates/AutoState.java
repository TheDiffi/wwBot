package wwBot.WerwolfGame.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.DayPhases.Auto.AutoDayPhase;
import wwBot.WerwolfGame.GameStates.DayPhases.Auto.Day;
import wwBot.WerwolfGame.GameStates.DayPhases.Auto.FirstNight;
import wwBot.WerwolfGame.GameStates.DayPhases.Auto.Morning;
import wwBot.WerwolfGame.GameStates.DayPhases.Auto.Night;
import wwBot.WerwolfGame.cards.Role;
import wwBot.WerwolfGame.cards.RoleDoppelgängerin;
import wwBot.WerwolfGame.cards.RoleHarterBursche;
import wwBot.WerwolfGame.cards.RolePriester;

//----------------------- ! WORK IN PROGRESS ! --------------------------------

//TODO: refractore DayPhases

public class AutoState extends MainState {

    public DayPhase dayPhaseEnum = DayPhase.FIRST_NIGHT;
    public AutoDayPhase dayPhase = null;

    public boolean wwEnraged = false;
    public boolean wwInfected = false;
    public boolean villageAgitated = false;

    public List<Player> pending = new ArrayList<>();

    AutoState(Game game) {
        super(game);
        registerStateCommands();

    }

    public void start() {
        MessagesWW.onGameStartAuto(game);
        createDeathChat();
        changeDayPhase(DayPhase.FIRST_NIGHT);
    }

    // --------------------- Day/Night - Cycle ----------------------------

    @Override
    public void changeDayPhase(DayPhase nextPhase) {
        updateGameLists();

        if (!checkIfGameEnds()) {

            // transitions to Night
            if (nextPhase == DayPhase.NORMAL_NIGHT) {
                if (game.gameRuleMutePlayersAtNight) {
                    Globals.setMuteAllPlayers(game.livingPlayers, true, game.server.getId());
                }
                villageAgitated = false;

                dayPhase = new Night(game);
                dayPhaseEnum = DayPhase.NORMAL_NIGHT;

                MessagesWW.onNightAuto(game);

                // transitions to Morning
            } else if (nextPhase == DayPhase.MORNING) {
                if (game.gameRuleMutePlayersAtNight) {
                    Globals.setMuteAllPlayers(game.livingPlayers, false, game.server.getId());
                }
                deleteWerwolfChat();

                dayPhaseEnum = DayPhase.MORNING;
                MessagesWW.onMorningAuto(game);

                dayPhase = new Morning(game);

                // transitions to Day
            } else if (nextPhase == DayPhase.DAY) {
                resetStuff();
                
                dayPhaseEnum = DayPhase.DAY;
                MessagesWW.onDayAuto(game);

                dayPhase = new Day(game);


                // transitions to 1st Night
            } else if (nextPhase == DayPhase.FIRST_NIGHT) {
                createWerwolfChat();
                dayPhaseEnum = DayPhase.FIRST_NIGHT;

                dayPhase = new FirstNight(game);

            }
        }

    }

    private void resetStuff() {
        wwEnraged = false;
        wwInfected = false;

        for (var player : game.livingPlayers.values()) {
            player.role.deathDetails.deathState = DeathState.ALIVE;
        }
        if (pending != null) {
            pending.clear();
        }
    }

    // -----------------------------------------------------------

    // --------------------- Commands ----------------------------

    @Override
    public boolean handleCommand(String requestedCommand, MessageCreateEvent event, List<String> parameters,
            MessageChannel runningInChannel) {
        var handeled = false;

        if (livingPlayers.containsKey(event.getMessage().getAuthor().get().getId())) {

            // finds the Command in the dayPhase
            var foundCommand = dayPhase.mapCommands.get(requestedCommand);
            if (foundCommand != null) {
                foundCommand.execute(event, parameters, runningInChannel);
                handeled = true;

            } else {
                handeled = false;
            }

            // if the Command was not found, it gets send a level up (MainState)
            if (!handeled) {
                handeled = super.handleCommand(requestedCommand, event, parameters, runningInChannel);
            }

        } else {
            MessagesWW.errorNoAccessToCommand(game, event.getMessage().getChannel().block());
            handeled = true;
        }

        return handeled;
    }

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerStateCommands() {

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {
            msgChannel.createMessage("Pong! Game").block();
        };
        gameStateCommands.put("ping", pingCommand);

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = MessagesWW.getCommandsMain();
            mssg += "\n" + MessagesWW.getCommandsGame();
            mssg += "\n" + MessagesWW.getCommandsAutoState();
            mssg += "\n" + MessagesWW.getHelpInfo();
            Globals.createEmbed(msgChannel, Color.CYAN, "Commands", mssg);

        };
        gameStateCommands.put("showCommands", showCommandsCommand);
        gameStateCommands.put("lsCommands", showCommandsCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            Globals.createMessage(msgChannel,
                    "(E:hAS) Hmmmmm, you should not be seeing this message...\nPlease send TheDiffi#7457 a message informing him about this bug!");
        };
        gameStateCommands.put("help", helpCommand);
        gameStateCommands.put("hilfe", helpCommand);

        // listet die verbleibenden Spieler auf
        Command lsLivingCommand = (event, parameters, msgChannel) -> {
            Globals.printPlayersMap(msgChannel, game.livingPlayers, "Am Leben", game, false);
        };
        gameStateCommands.put("Alive", lsLivingCommand);
        gameStateCommands.put("ListLiving", lsLivingCommand);
        gameStateCommands.put("StillAlive", lsLivingCommand);

        // replys with pong!
        Command lsPendingCommand = (event, parameters, msgChannel) -> {
            Globals.createEmbed(msgChannel, Color.LIGHT_GRAY, "",
                    Globals.playerListToRoleList(pending, "Warte auf Rolle", game));
        };
        gameStateCommands.put("pending", lsPendingCommand);
        gameStateCommands.put("lsPending", lsPendingCommand);

    }

    // -------------------- Kill System --------------------------

    // CheckIfDies --> überprüft in dieser Reihenfolge: ob der Player nicht bereits
    // Tot ist; ob er eine Spezialkarte ist welche nicht stirbt
    // KillSwitch --> falls checkIfDies true zurückgibt:
    // 1) player.alive wird auf false gesetzt; der Spieler wird zur liste der toten
    // Spieler und zum death-chat hinzugefügt
    // 2) der Player wird gemuted
    // 3) der Tot wird verkündet und die Identität gelüftet
    // 3) checkConsequences wird gerufen. Dies überprüft die Consequenzen

    @Override
    public boolean killPlayer(Player victim, String cause) {

        if (checkIfDies(victim, cause)) {
            // kills player
            killSwitch(victim);

            // reveals the players death and identity
            sendDeathMessage(victim, cause);

			checkConsequences(victim, cause);
			

            return true;
		}
        return false;

    }

    // checks the conditions if the player dies
    @Override
    public boolean checkIfDies(Player victim, String cause) {
        var dies = true;

        if (savedByPriester(victim)) {
            return false;

        } else {

            // Verfluchter
            if (victim.role.name.equalsIgnoreCase("Verfluchter") && cause.equalsIgnoreCase("Werwolf")) {
                dies = false;
                victim.role = Role.createRole("Werwolf");
                MessagesWW.verfluchtenMutation(game);

            }

            // Prinz
            else if (victim.role.name.equalsIgnoreCase("Prinz") && cause.equalsIgnoreCase("Dorfbewohner")) {
                dies = false;
                MessagesWW.prinzSurvives(game);

            }

            // Harter-Bursche
            else if (victim.role.name.equalsIgnoreCase("Harter-Bursche") && cause.equalsIgnoreCase("Werwolf")) {
                var bursche = (RoleHarterBursche) mapExistingRoles.get("Harter-Bursche").get(0).role;
                dies = false;
                bursche.isDying = true;
                MessagesWW.harterBurscheSurvives(game);

            }

        }

        return dies;
    }

    private boolean savedByPriester(Player victim) {
        // Priester
        if (mapExistingRoles.containsKey("Priester")) {
            var priester = (RolePriester) mapExistingRoles.get("Priester").get(0).role;

            if (priester.abilityActive && victim == priester.protectedPlayer) {
                MessagesWW.savedByPriester(victim, game);
                priester.abilityActive = false;
                return true;

            }
        }
        return false;
    }

    private void killSwitch(Player victim) {
        victim.role.deathDetails.alive = false;
        game.deadPlayers.add(victim);

        updateDeathChat();
        updateGameLists();

        try {
            victim.user.asMember(game.server.getId()).block().edit(a -> a.setMute(true)).block();
        } catch (Exception e) {
        }
    }

    private void checkConsequences(Player victim, String cause) {

        // Wolfsjunges
        if (victim.role.name.equals("Wolfsjunges")) {
            MessagesWW.onWolfsjungesDeath(game);
            wwEnraged = true;
        }

        // Seher Lehrling
        if (victim.role.name.equals("Seher") && game.gameState.mapExistingRoles.containsKey("Seher-Lehrling")) {
            var lehrling = game.gameState.mapExistingRoles.get("Doppelgängerin").get(0);
            MessagesWW.onSeherlehrlingPromotion(game, victim);
            lehrling.role = Role.createRole("Seher");

        }

        // Aussätzige
        if (victim.role.name.equals("Aussätzige") && cause.equalsIgnoreCase("Werwolf")) {
            MessagesWW.onAussätzigeDeath(game);
            wwInfected = true;

        }
        // Jäger
        if (victim.role.name.equals("Jäger")) {
            MessagesWW.onJägerDeath(game, victim);

            setPending(victim);

            PrivateCommand jägerCommand = (event, parameters, msgChannel) -> {
                var foundPlayer = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

                if (foundPlayer != null) {
                    MessagesWW.confirm(msgChannel);
                    killPlayer(foundPlayer, "Jäger");
                    setDone(victim);

                    return true;
                } else {
                    return false;
                }

            };
            game.addPrivateCommand(victim.user.getId(), jägerCommand);

        }

        // Doppelgängerin
        if (mapExistingRoles.containsKey("Doppelgängerin")) {
            var dp = game.gameState.mapExistingRoles.get("Doppelgängerin").get(0);

            if (((RoleDoppelgängerin) dp.role).boundTo == victim) {
                dp.role = Role.createRole(victim.role.name);
                MessagesWW.onDoppelgängerinTransformation(game, dp, victim);

            }
        }

        // Amor
        if (victim.role.inLoveWith != null && victim.role.inLoveWith.role.deathDetails.alive) {
            killPlayer(victim.role.inLoveWith, "Amor");
            MessagesWW.onLoversDeath(game, victim.role.inLoveWith);
        }

        // game end immediately if the last ww is found
        if (victim.role.name.equals("Werwolf") || victim.role.name.equals("Wolfsjunges")) {
            Globals.sleepWCatch(1500);
        	checkIfGameEnds();
        }

    }

    private void sendDeathMessage(Player player, String cause) {

        switch (cause) {
            case "Werwolf":
                MessagesWW.deathByWW(game, player);
                break;
            case "Hexe":
                MessagesWW.deathByMagic(game, player);
                break;
            case "Magier":
                MessagesWW.deathByMagic(game, player);
                break;
            case "Amor":
                MessagesWW.deathByLove(game, player);
                break;
            case "Jäger":
                MessagesWW.deathByGunshot(game, player);
                break;
            case "Dorfbewohner":
                MessagesWW.deathByLynchen(game, player);
                break;
            case "Märtyrerin":
                MessagesWW.deathBySacrifice(game, player);
                break;

            default:
                MessagesWW.deathByDefault(game, player);
        }
        if (game.gameRulePrintCardOnDeath) {
            Globals.printCard(player.role.name, game.mainChannel);
        }
    }

    public void setPending(Player player) {
        pending.add(player);
    }

    public void setDone(Player player) {
        pending.remove(player);
    }

    public void setDoneNight(Player player) {
        pending.remove(player);
        endNightPhaseCheck();
    }

    public void endNightPhaseCheck() {
        if (pending == null || pending.isEmpty()) {
            dayPhase.changeNightPhase();
        }
    }
}
