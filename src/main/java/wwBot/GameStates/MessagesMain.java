package wwBot.GameStates;

import java.awt.Color;
import java.util.List;

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
        Globals.createMessage(channel,
                "`Ihr könnt dem Dorf beitreten indem ihr \"" + prefix
                        + "join\" eingebt. Sobald alle Dorfbewohner bereit sind könnt ihr euch mit \"" + prefix
                        + "buildDeck\" ein Deck vorschlagen lassen.`",
                false);

    }

    public static void onGameStart(Game game) {

        // verkündet den Start der ersten Nacht
        Globals.createEmbed(game.runningInChannel, Color.BLACK, "Willkommen bei : Die Werölfe von Düsterwald", "");

        Globals.createMessage(game.runningInChannel,
                "Unser Dorf wird seit den Tagen des alten Rom von Mythen und Sagen über Werwölfe heimgesucht. Seit kurzem sind diese Mythen zur Wirklichkeit geworden.",
                true);
        Globals.createMessage(game.runningInChannel,
                "Im Mondschein bestimmen die Dorfbewohner das man dieser Situation ein Ende setzen muss. ", true);
        Globals.createMessage(game.runningInChannel,
                "Es wird angekündigt das von nun an an jedem Morgen ein Dorfbewohner durch Abstimmung gelyncht wird. Somit beginnt die erste Nacht",
                true);

    }

    public static void firstNightAuto(Game game) {
        Globals.createEmbed(game.runningInChannel, Color.BLACK, "🌙Die Erste Nacht🌙",
                "`In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren`");

    }

	public static void firstNightManual(Game game) {
        //Nachricht an alle
        Globals.createEmbed(game.runningInChannel, Color.BLACK, "🌙Die Erste Nacht🌙", "`In dieser Phase erwachen all jene SpezialKarten, welche in der ersten Nacht eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist, wird der Moderator den Namen deiner Rolle aufrufen. Um die Identität dieser Personen zu wahren, sollten nun alle Spieler ihre Augen schließen oder ihre Webcam deaktivieren. \n Tipp: ihr könnt mich jederzeit mit \"&showCard\" fragen euch eure Rolle zu Zeigen (tut dies aber nur im Privatchat mit mir). `");
        // der Moderator bekommt eine Liste mit allen Spielern und ihren Rollen, sowie eine Liste mit allen Rollen, welche aufgerufen werden müssen
        
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.DARK_GRAY, "Erste Nacht", "In der ersten Nacht kannst du dir einen Überblick über die Rollen jedes Spielers verschaffen. In der ersten Nacht töten die Werwölfe niemanden, der Seher darf allerdings eine Person überprüfen. \n Es folgt eine Liste mit den Rollen welche in dieser Nacht aufgerufen werden sollten.");
        
}



}