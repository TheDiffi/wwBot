package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;

import discord4j.core.object.entity.MessageChannel;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;

public class MessagesMain {

        // erzeugt und sendet ein Embed und eine Nachricht. Wird zu spielstart
        // aufgerufen
        public static void gameStartMessage(MessageChannel channel, String prefix) {

                Globals.createEmbed(channel, Color.GREEN, "Created New Game!", "");

                Globals.createMessage(channel,
                                "Guten Abend liebe Dorfbewohner. \n Ich, euer Moderator, werde euch helfen die Werwolfinvasion zu stoppen.",
                                true);
                Globals.createMessage(channel, "`Ihr könnt dem Dorf beitreten indem ihr \"" + prefix
                                + "join\" eingebt. Sobald alle Dorfbewohner bereit sind könnt ihr euch mit \"" + prefix
                                + "buildDeck\" ein Deck vorschlagen lassen.`", false);

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

        public static void onNightAuto(Game game) {
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "Es wird Nacht...🌙",
                                "`In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren`");

        }

        public static void firstNightManual(Game game) {
                // Nachricht an alle
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "🌙Die Erste Nacht🌙",
                                "`In dieser Phase erwachen all jene SpezialKarten, welche in der ersten Nacht eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist, wird der Moderator den Namen deiner Rolle aufrufen. Um die Identität dieser Personen zu wahren, sollten nun alle Spieler ihre Augen schließen oder ihre Webcam deaktivieren. \n Tipp: ihr könnt mich jederzeit mit \"&showCard\" fragen euch eure Rolle zu Zeigen (tut dies aber nur im Privatchat mit mir). `");
                // der Moderator bekommt eine Liste mit allen Spielern und ihren Rollen, sowie
                // eine Liste mit allen Rollen, welche aufgerufen werden müssen

                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY, "Erste Nacht",
                                "In der ersten Nacht kannst du dir einen Überblick über die Rollen jedes Spielers verschaffen. In der ersten Nacht töten die Werwölfe niemanden, der Seher darf allerdings eine Person überprüfen. \n Es folgt eine Liste mit den Rollen welche in dieser Nacht aufgerufen werden sollten.");

        }

        public static void semiOnNightStart(Game game, ArrayList<Player> sortedRoles) {
                // Nachricht an alle
                Globals.createEmbed(game.runningInChannel, Color.BLACK, "🌙Nacht🌙",
                                "In dieser Phase des Spieles erwachen Spezoalkarten und die Werwölfe einigen sich auf ein Opfer.");
                // Nachricht an Moderator
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY, "Nacht",
                                "Nachts einigen sich die werwölfe auf ein Opfer. In dieser Phase erwachen Spezialkarten, es folgt eine Liste mit den Rollen und die von ihnen zu befolgende Reihenfolge.");
                var mssg = "";
                for (Player player : sortedRoles) {
                        mssg += player.role.name + "\n";
                }
                Globals.createMessage(game.userModerator.getPrivateChannel().block(), mssg, false);

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
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                player.user.getUsername() + " wurde von einem Schuss im Bein getroffen und verblutete daraufhin. ",
                                revealId(player));
        }

        public static void deathByLynchen(Game game, Player player) {
                                Globals.createEmbed(game.runningInChannel, Color.RED, 
                                player.user.getUsername() + " wird öffentlich hingerichtet. ",
                                revealId(player));
        }

        public static void deathByLove(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                player.user.getUsername() + " erträgt die Welt ohne seiner/ihrer Geliebte/n nicht mehr und erhängt sich. ",
                                revealId(player));
        }

        public static void deathByMartyrium(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                player.user.getUsername() + " wirft sich freiwillig von der Brücke um ein Zeichen zu setzen. ",
                                revealId(player));
        }

        public static void death(Game game, Player player) {
                Globals.createEmbed(game.runningInChannel, Color.RED,
                                "Das Leben von " + player.user.getUsername() + " kam zu einem tragischen Ende. ",
                                revealId(player));
        }

        public static void wwInfection(Game game) {
                Globals.createMessage(game.runningInChannel, "Die Werwölfe wurden infiziert und dürfen in der nägsten Nacht niemanden töten", true);
                
        }
        
        public static void seherlehrlingWork(Game game, Player player) {
                Globals.createMessage(game.runningInChannel, "Bestürzt über den Tod seines Meisters, beschließt der " + player.role.name + " die Sache selbst in die Hand zu nehmen. Fortan tritt er in die Fußstapfen seines Meisters und such jede Nacht nach den Werwölfen.", true);
							
        }
        
        public static void verfluchtenMutation(Game game, Player player){
                Globals.createMessage(game.runningInChannel, "Die Dorfbewohner finden zerfetzte Kleider im Wald und wissen, dass dies nur eines bedeuten kann: der Verfluchte ist mutiert!", true);
        }

        public static void wolfsjungesDeath(Game game, Player player) {
                Globals.createMessage(game.runningInChannel, "Die Werwölfmutter ist über ihren Verlust entsetzt und die Werwölfe beschließen, dass es in der nächsten Nacht 2 Tode geben wird.", true);
        }

        public static void jägerDeath(Game game, Player player) {
                Globals.createMessage(game.runningInChannel, "Mit letzter kraft zückt der Jäger sein Gewehr. Er ist nun gebeten mir *privat* die Person zu nennen auf die er schießt.", true);
        }

        

}