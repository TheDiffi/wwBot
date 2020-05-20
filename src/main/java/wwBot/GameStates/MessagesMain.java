package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
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
                                "*Guten Abend liebe Dorfbewohner. \n Ich, euer Moderator, werde euch helfen die Werwolfinvasion zu stoppen.*",
                                true);
                Globals.createMessage(channel,
                                "Ihr befindet euch nun in der Lobby Phase. Hier habt ihr Zeit für ein wenig Small-Talk während alle Mitspieler mit \""
                                                + prefix
                                                + "join\" dem Spiel beitreten und das Kartendeck erstellt wird. Genießt diese Zeit denn sobald das Spiel mit \""
                                                + prefix
                                                + "StartGame\" gestartet wird, könnt ihr niemanden mehr trauen.... \nFalls dies das erste mal ist, dass du mich benüzt oder du nicht weißt was du tun sollst, tippe \""
                                                + prefix + "help\".", false);

        }

        public static void onGameStart(Game game) {

                // verkündet den Start der ersten Nacht
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "Willkommen bei : Die Werölfe von Düsterwald",
                                "");

                Globals.createMessage(game.runningInChannel,
                                "Unser Dorf wird seit den Tagen des alten Rom von Mythen und Sagen über Werwölfe heimgesucht. Seit kurzem sind diese Mythen zur Wirklichkeit geworden.",
                                true);
                Globals.createMessage(game.runningInChannel,
                                "Im Mondschein bestimmen die Dorfbewohner das man dieser Situation ein Ende setzen muss. ",
                                true);
                Globals.createMessage(game.runningInChannel,
                                "Es wird angekündigt das von nun an an jedem Morgen ein Dorfbewohner durch Abstimmung gelyncht wird. Somit beginnt die erste Nacht",
                                true);

        }

        public static void firstNightAuto(Game game) {
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "🌙Die Erste Nacht🌙",
                                "In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren");

        }

        public static void firstNightMod(Game game) {
                // Nachricht an alle
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "🌙Die Erste Nacht🌙",
                                "`In dieser Phase erwachen all jene SpezialKarten, welche in der ersten Nacht eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist, wird der Moderator den Namen deiner Rolle aufrufen. Um die Identität dieser Personen zu wahren, sollten nun alle Spieler ihre Augen schließen oder ihre Webcam deaktivieren. \n Tipp: ihr könnt mich jederzeit mit \"&showCard\" fragen euch eure Rolle zu Zeigen (tut dies aber nur im Privatchat mit mir). `");
                // der Moderator bekommt eine Liste mit allen Spielern und ihren Rollen, sowie
                // eine Liste mit allen Rollen, welche aufgerufen werden müssen

                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY, "Erste Nacht",
                                "In der ersten Nacht kannst du dir einen Überblick über die Rollen jedes Spielers verschaffen. In der ersten Nacht töten die Werwölfe niemanden, der Seher darf allerdings eine Person überprüfen. \n Es folgt eine Liste mit den Rollen welche in dieser Nacht aufgerufen werden sollten.");

        }

        public static void onNightAuto(Game game) {
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "Es wird Nacht...🌇",
                                "`In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren`");

        }

        public static void onDayAuto(Game game) {
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "Es wird Tag...🌅",
                                "Die Dorfbewohner erwachen und ihnen schwant übles. Wer wird heute von ihnenen gegangen sein?");
        }

        public static void semiOnNightStart(Game game, ArrayList<Player> sortedRoles) {
                // Nachricht an alle
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "🌙Nacht🌙",
                                "In dieser Phase des Spieles erwachen Spezoalkarten und die Werwölfe einigen sich auf ein Opfer.");
                // Nachricht an Moderator
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY, "Nacht",
                                "Nachts einigen sich die werwölfe auf ein Opfer. In dieser Phase erwachen Spezialkarten, es folgt eine Liste mit den Rollen und die von ihnen zu befolgende Reihenfolge.");
                var mssg = "";
                for (int i = 0; i < sortedRoles.size(); i++) {
                        mssg += Integer.toString(i) + ") " + sortedRoles.get(i).user.getUsername() + ": ist "
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

        public static String revealId(Player player) {
                var mssg = player.user.getUsername() + " war ein " + player.role.name;
                return mssg;
        }

        public static void deathByWW(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                player.user.getUsername() + " wird am Morgen halb zerfressen aufgefunden. ",
                                revealId(player));
        }

        public static void deathByMagic(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                player.user.getUsername() + "wird Tod neben einer leeren Trankflasche aufgefunden. ",
                                revealId(player));
        }

        public static void deathByGunshot(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED, player.user.getUsername()
                                + " wurde von einem Schuss im Bein getroffen und verblutete daraufhin. ",
                                revealId(player));
        }

        public static void deathByLynchen(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                player.user.getUsername() + " wird öffentlich hingerichtet. ", revealId(player));
        }

        public static void deathByLove(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED, player.user.getUsername()
                                + " erträgt die Welt ohne seiner/ihrer Geliebte/n nicht mehr und erhängt sich. ",
                                revealId(player));
        }

        public static void deathByMartyrium(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                player.user.getUsername()
                                                + " wirft sich freiwillig von der Brücke um ein Zeichen zu setzen. ",
                                revealId(player));
        }

        public static void death(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                "Das Leben von " + player.user.getUsername() + " kam zu einem tragischen Ende. ",
                                revealId(player));
        }

        public static void wwInfection(Game game) {
                Globals.createMessage(game.runningInChannel,
                                "Die Werwölfe wurden infiziert und dürfen in der nägsten Nacht niemanden töten", true);

        }

        public static void seherlehrlingWork(Game game, Player player) {
                Globals.createMessage(game.runningInChannel, "Bestürzt über den Tod seines Meisters, beschließt der "
                                + player.role.name
                                + " die Sache selbst in die Hand zu nehmen. Fortan tritt er in die Fußstapfen seines Meisters und such jede Nacht nach den Werwölfen.",
                                true);

        }

        public static void verfluchtenMutation(Game game, Player player) {
                Globals.createMessage(game.runningInChannel,
                                "Die Dorfbewohner finden zerfetzte Kleider im Wald und wissen, dass dies nur eines bedeuten kann: der Verfluchte ist mutiert!",
                                true);
        }

        public static void wolfsjungesDeath(Game game, Player player) {
                Globals.createMessage(game.runningInChannel,
                                "Die Werwölfmutter ist über ihren Verlust entsetzt und die Werwölfe beschließen, dass es in der nächsten Nacht 2 Tode geben wird.",
                                true);
        }

        public static void jägerDeath(Game game, Player player) {
                Globals.createMessage(game.runningInChannel,
                                "Mit letzter kraft zückt der Jäger sein Gewehr. Er ist nun gebeten mir *privat* die Person zu nennen auf die er schießt.",
                                true);
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

        public static void helpGame(MessageCreateEvent event) {
                // help
                event.getMessage().getChannel().block().createMessage("TODO: add help Command in Game").block();

                Globals.createMessage(event.getMessage().getChannel().block(),
                                "`Ihr könnt dem Dorf beitreten indem ihr \"" + prefix
                                                + "join\" eingebt. \nSobald alle Mitspieler beigetreten sind, wollt ihr als nächstes euer Kartendeck für dieses Spiel bestimmen.\nMit \""
                                                + prefix
                                                + "buildDeck\" generiert mein algorythmus automatisch ein faires Deck. \n Dieses kann anschließend mit \""
                                                + prefix + "addCard <Karte>\" und \"" + prefix
                                                + "removeCard <Karte>\" bearbeitet werden. \nMit \"" + prefix
                                                + "gamerule manual\" und \"" + prefix
                                                + "gamerule automatic\"(coming soon) könnt ihr den Moderationsmodus des Spiels bestimmen. Bei \"Manaul\" moderiert ein menschlicher Spieler den Spielverlauf und ich helfe ihm eine Übersicht zu behalten. Im \"Automatic\" Moderationsmodus nehme ich die Rolle des Moderators ein(Coming soon)\n*Wenn alle Spieler beigetretn und ein Deck registriert wurde, lasse das Spiel mit \""
                                                + prefix + "startgame\" starten!*`",
                                false);
        }

}