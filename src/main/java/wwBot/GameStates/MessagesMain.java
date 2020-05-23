package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Main;
import wwBot.Player;

public class MessagesMain {
        public static String prefix = Main.prefix;

        // erzeugt und sendet ein Embed und eine Nachricht. Wird zu spielstart
        // aufgerufen
        public static void newGameStartMessage(MessageChannel channel) {

                Globals.createEmbed(channel, Color.GREEN, "Created New Game!", "");

                Globals.createMessage(channel,
                                "Guten Abend liebe Dorfbewohner. \nIch, euer Moderator, werde euch helfen die Werwolfinvasion zu stoppen.",
                                true);
                Globals.createMessage(channel,
                                "Ihr befindet euch nun in der Lobby Phase. Hier habt ihr Zeit für ein wenig Small-Talk während alle Mitspieler mit \""
                                                + prefix
                                                + "join\" dem Spiel beitreten und das Kartendeck erstellt wird. Genießt diese Zeit denn sobald das Spiel mit \""
                                                + prefix
                                                + "Start\" gestartet wird, könnt ihr niemanden mehr trauen.... \nFalls dies das erste mal ist, dass du mich benüzt oder du nicht weißt was du tun sollst, tippe \""
                                                + prefix + "help\".",
                                false);

        }

        public static void onGameStart(Game game) {

                // verkündet den Start der ersten Nacht
                Globals.createEmbed(game.mainChannel, Color.BLACK, "Willkommen bei : Die Werölfe von Düsterwald", "");

                Globals.createMessage(game.mainChannel,
                                "Unser Dorf wird seit den Tagen des alten Rom von Mythen und Sagen über Werwölfe heimgesucht. Seit kurzem sind diese Mythen zur Wirklichkeit geworden.",
                                true);
                Globals.createMessage(game.mainChannel,
                                "Im Mondschein bestimmen die Dorfbewohner das man dieser Situation ein Ende setzen muss. ",
                                true);
                Globals.createMessage(game.mainChannel,
                                "Es wird angekündigt das von nun an an jedem Morgen ein Dorfbewohner durch Abstimmung gelyncht wird. Somit beginnt die erste Nacht",
                                true);

        }

        public static void firstNightAuto(Game game) {
                Globals.createEmbed(game.mainChannel, Color.BLACK, "🌙Die Erste Nacht🌙",
                                "In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren");

        }

        public static void firstNightMod(Game game, ArrayList<Player> listRolesToBeCalled) {
                // Nachricht an alle
                Globals.createEmbed(game.mainChannel, Color.BLACK, "🌙Die Erste Nacht🌙",
                                "```In dieser Phase erwachen all jene SpezialKarten, welche in der ersten Nacht eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist, wird der Moderator den Namen deiner Rolle aufrufen. Um die Identität dieser Personen zu wahren, sollten nun alle Spieler ihre Augen schließen oder ihre Webcam deaktivieren. \n Tipp: ihr könnt mich jederzeit mit \"&showCard\" fragen euch eure Rolle zu Zeigen (tut dies aber nur im Privatchat mit mir). ```");
                // der Moderator bekommt eine Liste mit allen Spielern und ihren Rollen, sowie
                // eine Liste mit allen Rollen, welche aufgerufen werden müssen

                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY, "Erste Nacht",
                                "In der ersten Nacht kannst du dir einen Überblick über die Rollen jedes Spielers verschaffen. In der ersten Nacht töten die Werwölfe niemanden, der Seher darf allerdings eine Person überprüfen. \n Es folgt eine Liste mit den Rollen welche in dieser Nacht aufgerufen werden sollten.");
                var mssg = "";
                for (Player player : listRolesToBeCalled) {
                        mssg += player.user.asMember(game.server.getId()).block().getDisplayName() + ": ist " + player.role.name + "\n";
                }
                mssg += "Tipp: benutz &showCard <NameDerKarte> um dir die Details der Karte nochmals anzusehen";
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY,
                                "Diese Rollen müssen in dieser Nacht aufgerufen werden:", mssg);

        }

        public static void onNightAuto(Game game) {
                Globals.createEmbed(game.mainChannel, Color.BLUE, "Es wird NACHT...🌇",
                                "```In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren```");
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.BLUE, "NACHT", "");

        }

        public static void onNightSemi(Game game, ArrayList<Player> sortedRoles) {
                // Nachricht an alle
                Globals.createEmbed(game.mainChannel, Color.BLACK, "🌙Nacht🌙",
                                "In dieser Phase des Spieles erwachen Spezoalkarten und die Werwölfe einigen sich auf ein Opfer.");
                // Nachricht an Moderator
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY, "Nacht",
                                "Nachts einigen sich die werwölfe auf ein Opfer. In dieser Phase erwachen Spezialkarten, es folgt eine Liste mit den Rollen und die von ihnen zu befolgende Reihenfolge.");
                var mssg = "";
                for (int i = 0; i < sortedRoles.size(); i++) {
                        mssg += Integer.toString(i) + ") " + sortedRoles.get(i).user.asMember(game.server.getId()).block().getDisplayName() + ": ist "
                                        + sortedRoles.get(i).role.name + "\n";
                }
                mssg += "Tipp: benutz &showCard <NameDerKarte> um dir die Details der Karte nochmals anzusehen";
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY,
                                "Diese Rollen müssen in dieser Nacht aufgerufen werden:", mssg);
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY,
                                "Diese Rollen müssen in dieser Nacht aufgerufen werden:", mssg);
                Globals.createMessage(game.userModerator.getPrivateChannel().block(),
                                "Wichtig: töte die Player mit &kill <Opfer> <GetötetDurchRolle> erst wenn du deren Tod verkündest, also im Morgengrauen. Beende zuerst die Nacht mit \"&endNight\", und versichere dich, dass alle Spieler wach sind bevor du den Spieler tötest und somit auch die Identität des Spielers preisgiebst.",
                                false);
        }

        public static void onMorningAuto(Game game) {
                Globals.createEmbed(game.mainChannel, Color.ORANGE, "Der MORGEN Bricht An...🌅",
                                "Die Dorfbewohner erwachen und ihnen schwant übles. Wer wird heute von ihnenen gegangen sein?");
        }

        public static void onMorningSemi(Game game) {
                Globals.createEmbed(game.mainChannel, Color.ORANGE, "Der MORGEN Bricht An...🌅",
                                "Die Dorfbewohner erwachen und ihnen schwant übles. Wer wird heute von ihnenen gegangen sein?");
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.ORANGE, "MORGEN",
                                "```In dieser Phase tötest du die Spieler welche in der voherigen Nacht getötet wurden.```\nBeende diese Phase mit \""
                                                + prefix + "endMorning\" ");
        }

        public static void onDayAuto(Game game) {
                Globals.createEmbed(game.mainChannel, Color.YELLOW, "Es wird TAG ☀️",
                "Die Dorfbewohner versammeln sich auf dem Dorfplatz und setzen ihre Besprechungen fort. Nun werden alle dazu aufgefordert mit \""
                                + prefix
                                + "vote <playername>\" für eine Person zu Stimmen. Die Person mit den meisten Stimmen wird am Ende des Tages gelyncht.");   
        }


        public static void onDaySemi(Game game) {
                Globals.createEmbed(game.mainChannel, Color.YELLOW, "Es wird TAG ☀️",
                                "Die Dorfbewohner versammeln sich auf dem Dorfplatz und setzen ihre Besprechungen fort. Nun werden alle dazu aufgefordert mit \""
                                                + prefix
                                                + "vote <playername>\" für eine Person zu Stimmen. Die Person mit den meisten Stimmen wird am Ende des Tages gelyncht.");
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.YELLOW, "TAG ☀️",
                                "In dieser Phase stimmen die Spieler ab. Der meistgewählte Spieler wird dir mitgeteilt. Du kannst dir jederzeit mit \""
                                                + prefix + "showVotes\" einen Überblick verschaffen");
        }

        public static String revealId(Player player, Game game) {
                var mssg = player.user.asMember(game.server.getId()).block().getDisplayName() + " war ein " + player.role.name;
                return mssg;
        }

        public static void deathByWW(Game game, Player player) {
                Globals.createEmbed(game.mainChannel, Color.RED,
                                player.user.asMember(game.server.getId()).block().getDisplayName() + " wird am Morgen halb zerfressen aufgefunden. ",
                                revealId(player, game));
        }

        public static void deathByMagic(Game game, Player player) {
                Globals.createEmbed(game.mainChannel, Color.RED,
                                player.user.asMember(game.server.getId()).block().getDisplayName() + "wird Tod neben einer leeren Trankflasche aufgefunden. ",
                                revealId(player, game));
        }

        public static void deathByGunshot(Game game, Player player) {
                Globals.createEmbed(game.mainChannel, Color.RED, player.user.asMember(game.server.getId()).block().getDisplayName()
                                + " wurde von einem Schuss im Bein getroffen und verblutete daraufhin. ",
                                revealId(player,game));
        }

        public static void deathByLynchen(Game game, Player player) {
                Globals.createEmbed(game.mainChannel, Color.RED,
                                player.user.asMember(game.server.getId()).block().getDisplayName() + " wird öffentlich hingerichtet. ", revealId(player, game));
        }

        public static void deathByLove(Game game, Player player) {
                Globals.createEmbed(game.mainChannel, Color.RED, player.user.asMember(game.server.getId()).block().getDisplayName()
                                + " erträgt die Welt ohne seiner/ihrer Geliebte/n nicht mehr und erhängt sich. ",
                                revealId(player, game));
        }

        public static void deathByMartyrium(Game game, Player player) {
                Globals.createEmbed(game.mainChannel, Color.RED,
                                player.user.asMember(game.server.getId()).block().getDisplayName()
                                                + " wirft sich freiwillig von der Brücke um ein Zeichen zu setzen. ",
                                revealId(player, game));
        }

        public static void death(Game game, Player player) {
                Globals.createEmbed(game.mainChannel, Color.RED,
                                "Das Leben von " + player.user.asMember(game.server.getId()).block().getDisplayName() + " kam zu einem tragischen Ende. ",
                                revealId(player, game));
        }

        public static void wwInfection(Game game) {
                Globals.createMessage(game.mainChannel,
                                "Die Werwölfe wurden infiziert und dürfen in der nägsten Nacht niemanden töten", true);

        }

        public static void seherlehrlingWork(Game game, Player player) {
                Globals.createMessage(game.mainChannel, "Bestürzt über den Tod seines Meisters, beschließt der "
                                + player.role.name
                                + " die Sache selbst in die Hand zu nehmen. Fortan tritt er in die Fußstapfen seines Meisters und such jede Nacht nach den Werwölfen.",
                                true);

        }

        public static void verfluchtenMutation(Game game, Player player) {
                Globals.createMessage(game.mainChannel,
                                "Die Dorfbewohner finden zerfetzte Kleider im Wald und wissen, dass dies nur eines bedeuten kann: der Verfluchte ist mutiert!",
                                true);
        }

        public static void wolfsjungesDeath(Game game, Player player) {
                Globals.createMessage(game.mainChannel,
                                "Die Werwölfmutter ist über ihren Verlust entsetzt und die Werwölfe beschließen, dass es in der nächsten Nacht 2 Tode geben wird.",
                                true);
        }

        public static void jägerDeath(Game game, Player player) {
                Globals.createMessage(game.mainChannel,
                                "Mit letzter kraft zückt der Jäger sein Gewehr. Er ist nun gebeten mir *privat* die Person zu nennen auf die er schießt.",
                                true);
        }

        public static void günstlingMessage(PrivateChannel privateChannel, Map<String, List<Player>> mapExistingRoles, Game game) {
                var mssg = "";
                mssg += "Die Werwölfe sind: ";
                for (int i = 0; i < mapExistingRoles.get("Werwolf").size(); i++) {
                        mssg += mapExistingRoles.get("Werwolf").get(i).user.asMember(game.server.getId()).block().getDisplayName() + " ";
                }
                if (mapExistingRoles.containsKey("Wolfsjunges")) {
                        mssg += mapExistingRoles.get("Werwolf").get(0).user.asMember(game.server.getId()).block().getDisplayName() + " ";
                }

                Globals.createEmbed(privateChannel, Color.GREEN, "Günstling", mssg);

        }

        public static void suggestMostVoted(Game game, Player mostVoted) {
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.RED,
                                "Alle Spieler Haben Gewählt!", "Auf dem Schaffott steht *" + mostVoted.user.getMention()
                                                + "* \nMit \"&lynch <Player>\" kannst du einen Spieler lynchen und damit die Rolle des Spielers offenbaren. Mit \"&endDay\" kanns du anschließend den Tag beenden (Falls du niemanden Lynchen möchtest kannst du acuh gleich mit &endDay fortfahren)");
        }

        public static String getHelpInfo() {
                var mssg = "*---------------------------*";
                mssg += "\n*" + prefix + "help*: TODO: finde gute formulierung";
                mssg += "\n*" + prefix + "showCommands*: zeigt dir die Liste mit den zurzeit verfügbaren Commands";
                mssg += "\nVergiss nicht: Zusammen mit dem Spiel ändert sich auch, welche Commands du benutzen kannst! Frag jederzeit mit sen zwei obrigen Commands nach hilfe wenn du nicht weiter weißt🙂";
                return mssg;
        }

        public static void helpMain(MessageCreateEvent event) {
                Globals.createMessage(event.getMessage().getChannel().block(), "Schreibe " + prefix
                                + "newGame zm ein neues Spiel zu starten! \n(Je Server kann nur ein Spiel gleichzeitig laufen) \nFalls du weitere Fragen hast kannst du jederzeit "
                                + prefix
                                + "showCommands eingeben um dir eine Liste der zurzeit verfügbaren Befehle anzeigen zu lassen oder erneut mit "
                                + prefix + "help nach hilfe fragen😁", false);
        }

        public static void showCommandsMain(MessageCreateEvent event) {
                // mssg +="\n*" + prefix + "";
                var mssg = "*" + prefix + "newGame*: Startet ein neues Spiel";
                mssg += "\n*" + prefix
                                + "deleteGame*: Falls aus diesem Server zurzeit ein Spiel läuft, wird es gelöscht";
                mssg += getHelpInfo();
                Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
        }

        public static void helpLobbyPhase(MessageCreateEvent event) {
                // help
                Globals.createMessage(event.getMessage().getChannel().block(),
                                "`Ihr könnt dem Dorf beitreten indem ihr \"" + prefix
                                                + "join\" eingebt. \nSobald alle Mitspieler beigetreten sind, wollt ihr als nächstes euer Kartendeck für dieses Spiel bestimmen.\nMit \""
                                                + prefix
                                                + "buildDeck\" generiert mein algorythmus automatisch ein faires Deck. \n Dieses kann anschließend mit \""
                                                + prefix + "addCard <Karte>\" und \"" + prefix
                                                + "removeCard <Karte>\" bearbeitet werden. \nMit \"" + prefix
                                                + "gamerule manual\" und \"" + prefix
                                                + "gamerule automatic\"(coming soon) könnt ihr den Moderationsmodus des Spiels bestimmen. Bei \"Manaul\" moderiert ein menschlicher Spieler den Spielverlauf und ich helfe ihm eine Übersicht zu behalten. Im \"Automatic\" Moderationsmodus nehme ich die Rolle des Moderators ein(Coming soon)\n*Wenn alle Spieler beigetretn und ein Deck registriert wurde, lasse das Spiel mit \""
                                                + prefix + "start\" starten!*`",
                                false);
        }

        public static void helpNightPhase(MessageCreateEvent event) {
                var mssg = "Es ist Nacht. In dieser Phase werden Spezialkarten vom Moderator aufgerufen und die Werwölfe einigen sich auf ein Opfer. Für den Werwölfen ist ein privater Chat freigeschalten.\n";
                Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
        }

        public static void helpFirstNightPhase(MessageCreateEvent event) {
                var mssg = "Es ist die erste Nacht. In dieser Phase werden nur diejenigen Spezielkarten aufgerufen, welche eine einmalige Funktion erfüllen. (z.B. Amor oder Doppelgängerin). Für den Werwölfen ist nun ein privater Chat freigeschalten, diese einigen sich jedoch in der ersten Nacht noch auf kein Opfer.";
                Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
        }

        public static void helpDayPhase(MessageCreateEvent event) {
                var mssg = "Es ist zurzeit Tag. In dieser Phase versuchen die Dorbewohner durch Diskussion herauszufinden, wer die Werwölfe sind. Die Werwölfe hingegen versuchen nicht aufzufallen. Jeder Spieler kann jeden Tag mit \""
                                + prefix
                                + "vote <Name des Spielers> \" für den Tod eines Mitspielers stimmen. Die Stimme kann hierbei jederzeit durch das erneute Aufrufen des Commands geändert werden. Dies ist die einzige Chance der Dorfbewohner die Werwölfe auszurotten, also wähle weise.\nSobald alle noch lebenden Spieler abgestimmt haben und eine Mehrheit besteht, kann der Moderator diesen lynchen. Mit \""
                                + prefix + "endDay\" kann der moderator das Spiel beenden.";
                Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
        }

        public static void helpMorning(MessageCreateEvent event) {
                var mssg = "Es ist Morgen. In dieser Phase werden vom Moderator die opfer der Nacht angekündigt.";
                Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
        }

        public static void helpNightPhaseMod(MessageCreateEvent event) {
        }

        public static void helpMorningMod(MessageCreateEvent event) {
        }

}