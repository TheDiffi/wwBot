package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.TextChannel;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Main;
import wwBot.Player;

public class MessagesMain {
	public static String prefix = Main.prefix;

	// ---------ONSTART MESSAGES--------------------------------------------

	// erzeugt und sendet ein Embed und eine Nachricht. Wird zu spielstart
	// aufgerufen
	public static void newGameStartMessage(MessageChannel channel) {

		Globals.createEmbed(channel, Color.GREEN, "Created New Game!", "");
		Globals.createMessage(channel,
				"Ihr befindet euch nun in der Lobby Phase. Hier habt ihr Zeit für ein wenig Small-Talk während alle Mitspieler mit \""
						+ prefix
						+ "join\" dem Spiel beitreten und das Kartendeck erstellt wird. Genießt diese Zeit denn sobald das Spiel mit \""
						+ prefix
						+ "Start\" gestartet wird, könnt ihr niemanden mehr trauen.... \nFalls dies das erste mal ist, dass du mich benutzt oder du nicht weißt was du tun sollst, tippe \""
						+ prefix + "help\".",
				false);

	}

	public static void greetMod(Game game) {
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Willkommen Moderator!", "");
		game.userModerator.getPrivateChannel().block().createMessage(
				"Deine Aufgabe ist es das Spiel für beide Parteien so fair wie möglich zu machen! \nDu kannst diesen Textkanal für Notizen benutzen.\nDu kannst nun mit dem Command **\"Ready\"** die erste Nacht Starten.")
				.block();
	}

	public static void onGameStart(Game game) {

		// verkündet den Start der ersten Nacht
		Globals.createEmbed(game.mainChannel, Color.BLACK, "Willkommen bei : Die Werwölfe von Düsterwald", "");

		Globals.createMessage(game.mainChannel,
				"Unser Dorf wird seit den Tagen des alten Rom von Mythen und Sagen über Werwölfe heimgesucht. Seit kurzem sind diese Mythen zur Wirklichkeit geworden.",
				false);
		Globals.createMessage(game.mainChannel,
				"Im Mondschein bestimmen die Dorfbewohner das man dieser Situation ein Ende setzen muss. ", false);
		Globals.createMessage(game.mainChannel,
				"Es wird angekündigt das von nun an an jedem Morgen ein Dorfbewohner durch Abstimmung gelyncht wird. Somit beginnt die erste Nacht",
				false);

	}

	public static void wwChatGreeting(TextChannel wwChat) {
		Globals.createEmbed(wwChat, Color.decode("#5499C7"), "Willkommen im Werwolf-Chat",
				"Dies ist ein Ort in dem die Werwölfe ungestört ihre Diskussionen durchführen können.");
	}

	public static void deathChatGreeting(TextChannel deathChat, Game game) {
		Globals.createEmbed(deathChat, Color.decode("#5499C7"), "Willkommen im Friedhof-Chat",
				"Dies ist ein Ort um ungestört über das Spiel zu diskutieren.");
		Globals.printPlayersMap(deathChat, game.mapPlayers, "Alle Spieler", game);

	}

	public static void firstNightAuto(Game game) {
		Globals.createEmbed(game.mainChannel, Color.decode("#191970"), "🌙Die Erste Nacht🌙",
				"In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren");

	}

	public static void firstNightMod(Game game, ArrayList<Player> listRolesToBeCalled) {
		// Nachricht an alle
		Globals.createEmbed(game.mainChannel, Color.decode("#191970"), "🌙Die Erste Nacht🌙",
				"```In dieser Phase erwachen all jene SpezialKarten, welche in der ersten Nacht eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist, wird der Moderator den Namen deiner Rolle aufrufen. Um die Identität dieser Personen zu wahren, sollten nun alle Spieler ihre Augen schließen oder ihre Webcam deaktivieren.```\nTipp: ihr könnt mich jederzeit mit \"&showCard\" fragen euch eure Rolle zu Zeigen (tut dies aber nur im Privatchat mit mir). ");
		// der Moderator bekommt eine Liste mit allen Spielern und ihren Rollen, sowie
		// eine Liste mit allen Rollen, welche aufgerufen werden müssen

		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"), "Erste Nacht",
				"In der ersten Nacht kannst du dir einen Überblick über die Rollen jedes Spielers verschaffen. In der ersten Nacht töten die Werwölfe niemanden, der Seher darf allerdings eine Person überprüfen. Die Werwölfe haben Nachts immer auf einen Werwolf-Chat zugriff. \n Es folgt eine Liste mit den Rollen welche in dieser Nacht aufgerufen werden sollten.");
		var mssg = "";
		for (Player player : listRolesToBeCalled) {
			mssg += player.name + ": ist " + player.role.name + "\n";
		}
		mssg += "Tipp: benutz &showCard <NameDerKarte> um dir die Details der Karte nochmals anzusehen";
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"),
				"Diese Rollen müssen in dieser Reihenfolge aufgerufen werden:", mssg);

	}

	public static void onNightAuto(Game game) {
		Globals.createEmbed(game.mainChannel, Color.decode("#191970"), "Es wird NACHT...🌇",
				"```In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren```");
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"), "NACHT", "");

	}

	public static void onNightSemi(Game game, ArrayList<Player> sortedRoles) {
		// Nachricht an alle
		Globals.createEmbed(game.mainChannel, Color.decode("#191970"), "Es wird NACHT...🌇",
				"In dieser Phase des Spieles erwachen Spezialkarten und die Werwölfe einigen sich auf ein Opfer.");
		// Nachricht an Moderator
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"), "Nacht",
				"```Nachts erwachen die Werwölfe und einigen Sich auf ein Opfer. Dazu steht ihnen sein geheimer Text-Kanal auf dem Server zur verfügung, auf den auch du Zugriff hast. \nEbenfalls erwachen in dieser Phase einige Spezialkarten.``` \nEs folgt eine Liste mit den Rollen und die von ihnen zu befolgende Reihenfolge.");
		var mssg = "";
		for (int i = 0; i < sortedRoles.size(); i++) {
			mssg += Integer.toString(i) + 1 + ") " + sortedRoles.get(i).name + ": ist " + sortedRoles.get(i).role.name
					+ "\n";
		}
		mssg += "Tipp: benutz &showCard <NameDerKarte> um dir die Details der Karte nochmals anzusehen";
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"),
				"Diese Rollen müssen in dieser Reihenfolge aufgerufen werden:", mssg);
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"), "Wichtig!",
				"Töte die Player mit \"&kill\" erst im Morgengrauen! Beende zuerst die Nacht mit \"&endNight\", und versichere dich, dass alle Spieler wach sind bevor du den Spieler tötest und somit auch die Identität des Spielers preisgibst. Die Werwölfe haben Nachts immer auf einen Werwolf-Chat zugriff.");
	}

	public static void onMorningAuto(Game game) {
		Globals.createEmbed(game.mainChannel, Color.ORANGE, "Der MORGEN Bricht An...🌅",
				"Die Dorfbewohner erwachen und ihnen schwant übles. Wer wird heute von ihnen gegangen sein?");
	}

	public static void onMorningSemi(Game game) {
		Globals.createEmbed(game.mainChannel, Color.ORANGE, "Der MORGEN Bricht An...🌅",
				"Die Dorfbewohner erwachen und ihnen schwant übles. Wer wird heute von ihnen gegangen sein?");
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.ORANGE, "MORGEN",
				"```In dieser Phase tötest du die Spieler welche in der vorherigen Nacht getötet wurden.```\nBeende diese Phase mit \""
						+ prefix + "endMorning\" ");
	}

	public static void onDayAuto(Game game) {
		Globals.createEmbed(game.mainChannel, Color.YELLOW, "Es wird TAG...☀️",
				"Die Dorfbewohner versammeln sich auf dem Dorfplatz und setzen ihre Besprechungen fort. Nun werden alle dazu aufgefordert mit \""
						+ prefix
						+ "vote <playername>\" für eine Person zu Stimmen. Falls der Name eine oder mehrere Personen ein Lehrzeichen beinhaltet, ist man gebeten dieses durch einen Bindestrich(-) zu ersetzen. Die Person mit den meisten Stimmen wird am Ende des Tages gelyncht.");
	}

	public static void onDaySemi(Game game) {
		Globals.createEmbed(game.mainChannel, Color.YELLOW, "Es wird TAG...☀️",
				"Die Dorfbewohner versammeln sich auf dem Dorfplatz und setzen ihre Besprechungen fort. Nun werden alle dazu aufgefordert mit \""
						+ prefix
						+ "vote <playername>\" für eine Person zu Stimmen. Man kann auch für <nobody> wählen. Die Person mit den meisten Stimmen wird am Ende des Tages gelyncht. ");
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.YELLOW, "TAG ☀️",
				"```In dieser Phase stimmen die Spieler ab. Der meistgewählte Spieler wird dir mitgeteilt. Du kannst dir jederzeit mit \""
						+ prefix + "showVotes\" einen Überblick verschaffen``` \nBeende diese Phase mit\"" + prefix
						+ "endDay\"");
	}

	// ---------UNIQUE CARDS MESSAGES--------------------------------------------

	public static void triggerAmor(Game game) {
		Globals.createMessage(game.userModerator.getPrivateChannel().block(),
				"[Optional] Du kannst mir mitteilen welche zwei Spieler verliebt sind. Tue dies mit \"" + prefix
						+ "&inLove\" <Player1> <Player2>",
				false);
	}

	public static void günstlingMessage(PrivateChannel privateChannel, Map<String, List<Player>> mapExistingRoles,
			Game game) {
		var mssg = "";
		mssg += "Die Werwölfe sind: ";
		for (int i = 0; i < mapExistingRoles.get("Werwolf").size(); i++) {
			mssg += mapExistingRoles.get("Werwolf").get(i).user.asMember(game.server.getId()).block().getDisplayName()
					+ " ";
		}
		if (mapExistingRoles.containsKey("Wolfsjunges")) {
			mssg += mapExistingRoles.get("Werwolf").get(0).user.asMember(game.server.getId()).block().getDisplayName()
					+ " ";
		}

		Globals.createEmbed(privateChannel, Color.GREEN, "Günstling", mssg);

	}

	public static void remmindAboutMärtyrerin(Game game) {
		Globals.createMessage(game.userModerator.getPrivateChannel().block(),
				"Vergiss nicht die Märtyrerin zu fragen ob sie sich anstelle der nominierten Person lynchen lassen will.");

	}

	public static void remindAboutPrinz(Game game) {
		Globals.createMessage(game.userModerator.getPrivateChannel().block(),
				"Wenn der Prinz duch \"" + prefix + "lynch\" stirbt, zeigt er seine Identität und überlebt.", false);
	}

	public static void prinzSurvivesLynching(Game game) {

		Globals.printCard("Prinz", game.mainChannel);
		game.mainChannel.createMessage(
				"Im letzten Moment enthüllt der Prinz Seine Identität. Geblendet von seiner Präsenz (und seinen weißen Zähnen) verschwindet die Wut der Dorfbewohner und der Prinz überlebt.")
				.block();
	}

	public static void checkHarterBurscheDeath(MessageChannel modChannel) {
		Globals.createMessage(modChannel,
				"Du bist kurz davor den Harten Burschen zu töten. Dieser überlebt bis zum Abend, wenn er Nachts getötet wird. Wenn du dir sicher bist, dass jetzt der richtige moment ist den Harten Burschen zu töten, tippe \"confirm\". Andernfalls tippe \"cancel\"",
				false);
	}

	public static void amorSuccess(Game game, MessageChannel msgChannel, Player firstLover, Player secondLover) {

		Globals.createEmbed(msgChannel, Color.PINK, "ERFOLG!",
				"" + firstLover.user.getUsername() + " und " + secondLover.name + " haben sich unsterblich verliebt");

		game.mainChannel.createMessage("Des Amors Liebespfeile haben ihr Ziel gefunden").block();

		firstLover.user.getPrivateChannel().block().createMessage("Du fällst mit **" + secondLover.name
				+ "** in eine unsterbliche Liebe. \n Eure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstellen könnt und deshalb sterbt sobald euer Partner stirbt")
				.block();
		secondLover.user.getPrivateChannel().block().createMessage("Du triffst dich mit **" + firstLover.name
				+ "** und verliebst dich Unsterblich in sie/ihn \n Eure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstellen könnt und deshalb sterbt sobald euer Partner stirbt")
				.block();
	}

	// ---------DEATH MESSAGES--------------------------------------------

	public static String revealId(Player player, Game game) {
		var mssg = player.name + " war ein " + player.role.name;
		return mssg;
	}

	public static void deathByWW(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.RED, player.name + " wird am Morgen halb zerfressen aufgefunden. ",
				revealId(player, game));
	}

	public static void deathByMagic(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.RED,
				player.name + "wird Tod neben einer leeren Trankflasche aufgefunden. ", revealId(player, game));
	}

	public static void deathByGunshot(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.RED,
				player.name + " wurde von einem Schuss im Bein getroffen und verblutete daraufhin. ",
				revealId(player, game));
	}

	public static void deathByLynchen(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.RED, player.name + " wird öffentlich hingerichtet. ",
				revealId(player, game));
	}

	public static void deathByLove(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.PINK, player.name

				+ " erträgt die Welt ohne seiner/ihrer Geliebte/n nicht mehr und erhängt sich. ",
				revealId(player, game));
	}

	public static void deathByMartyrium(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.RED,
				player.name + " wirft sich freiwillig von der Brücke um ein Zeichen zu setzen. ",
				revealId(player, game));
	}

	public static void deathByDefault(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.RED,
				"Das Leben von " + player.name + " kam zu einem tragischen Ende. ", revealId(player, game));
	}

	public static void onAussätzigeDeath(Game game) {
		Globals.createMessage(game.mainChannel,
				"Die Werwölfe wurden infiziert und dürfen in der nächsten Nacht niemanden töten", true);
		Globals.createMessage(game.userModerator.getPrivateChannel().block(),
				"Die Aussätzige ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe niemanden töten");

	}

	public static void onSeherlehrlingPromotion(Game game, Player player) {
		Globals.createMessage(game.mainChannel, "Bestürzt über den Tod seines Meisters, beschließt der "
				+ player.role.name
				+ " die Sache selbst in die Hand zu nehmen. Fortan tritt er in die Fußstapfen seines Meisters und such jede Nacht nach den Werwölfen.",
				true);

	}

	public static void verfluchtenMutation(Game game) {
		Globals.createMessage(game.mainChannel,
				"Die Dorfbewohner finden zerfetzte Kleider im Wald und wissen, dass dies nur eines bedeuten kann: der Verfluchte ist mutiert!",
				true);
	}

	public static void onWolfsjungesDeath(Game game) {
		Globals.createMessage(game.mainChannel,
				"Die Werwölfmutter ist über ihren Verlust entsetzt und die Werwölfe beschließen, dass es in der nächsten Nacht 2 Tode geben wird.",
				true);
		Globals.createMessage(game.userModerator.getPrivateChannel().block(),
				"Das Wolfsjunges ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe zwei Personen töten.",
				false);
	}

	public static void onJägerDeath(Game game, Player player) {
		Globals.createMessage(game.mainChannel,
				"Mit letzter kraft zückt der Jäger sein Gewehr. Schreibe mir nun wen du töten möchtest.", true);
	}

	// ---------VOTE MESSAGES--------------------------------------------

	public static void suggestMostVoted(Game game, Player mostVoted, Map<Player, Player> mapVotes) {
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.RED, "Alle Spieler Haben Gewählt!",
				"Auf dem Schafott steht **" + mostVoted.user.getMention() + "** der ein/eine **" + mostVoted.role.name
						+ "** ist.\nMit \"&lynch <Player>\" kannst du einen Spieler lynchen und damit die Rolle des Spielers offenbaren. \nMit \"&endDay\" kannst du anschließend den Tag beenden (Falls du niemanden Lynchen möchtest kannst du auch gleich mit &endDay fortfahren)");

		var mssg = "";
		for (var entry : mapVotes.entrySet()) {
			mssg += entry.getKey().name + " hat für " + entry.getValue().name + " abgestimmt \n";
		}

		Globals.createEmbed(game.mainChannel, Color.WHITE,
				"Die Würfel sind gefallen \nAuf dem Schafott steht: " + mostVoted.name, mssg);
	}

	public static void voteButNoMajority(Game game) {
		Globals.createMessage(game.mainChannel,
				"Alle Spieler haben abgestimmt, jedoch gibt es **keine klare Mehrheit**. Es wird gebete, dass wenigstens ein Spieler seine Stimme ändert, damit es zu einer klaren Mehrheit kommt.",
				false);
	}

	public static void votePlayer(Game game, Player voter, Player votedFor) {
		Globals.createMessage(game.mainChannel,
				voter.user.getMention() + " will, dass " + votedFor.user.getMention() + " gelyncht wird!", false);
	}

	public static void voteNobody(Game game, Player voter) {
		Globals.createMessage(game.mainChannel, voter.user.getMention() + " will niemanden lynchen.", false);

	}

	// ---------HELP MESSAGES--------------------------------------------

	public static String getHelpInfo() {
		var mssg = "*---------------------------*";
		mssg += "\n*" + prefix + "help*: TODO: finde gute formulierung";
		mssg += "\n*" + prefix + "showCommands*: zeigt dir die Liste mit den zurzeit verfügbaren Commands";
		mssg += "\nVergiss nicht: Zusammen mit dem Spiel ändert sich auch, welche Commands du benutzen kannst! Frag jederzeit mit sen zwei obigen Commands nach hilfe wenn du nicht weiter weißt🙂";
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
		mssg += "\n*" + prefix + "deleteGame*: Falls aus diesem Server zurzeit ein Spiel läuft, wird es gelöscht";
		mssg += getHelpInfo();
		Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
	}

	public static void helpLobbyPhase(MessageCreateEvent event) {
		// help
		Globals.createMessage(event.getMessage().getChannel().block(), "`Ihr könnt dem Dorf beitreten indem ihr \""
				+ prefix
				+ "join\" eingebt. \nSobald alle Mitspieler beigetreten sind, wollt ihr als nächstes euer Kartendeck für dieses Spiel bestimmen.\nMit \""
				+ prefix
				+ "buildDeck\" generiert mein algorithmus automatisch ein faires Deck. \n Dieses kann anschließend mit \""
				+ prefix + "addCard <Karte>\" und \"" + prefix + "removeCard <Karte>\" bearbeitet werden. \nMit \""
				+ prefix + "gamerule manual\" und \"" + prefix
				+ "gamerule automatic\"(coming soon) könnt ihr den Moderationsmodus des Spiels bestimmen. Bei \"Manual\" moderiert ein menschlicher Spieler den Spielverlauf und ich helfe ihm eine Übersicht zu behalten. Im \"Automatic\" Moderationsmodus nehme ich die Rolle des Moderators ein(Coming soon)\n*Wenn alle Spieler beigetreten und ein Deck registriert wurde, lasse das Spiel mit \""
				+ prefix + "start\" starten!*`", false);
	}

	public static void helpNightPhase(MessageCreateEvent event) {
		var mssg = "Es ist Nacht. In dieser Phase werden Spezialkarten vom Moderator aufgerufen und die Werwölfe einigen sich auf ein Opfer. Für den Werwölfen ist ein privater Chat freigeschaltet.\n";
		Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
	}

	public static void helpFirstNightPhase(MessageCreateEvent event) {
		var mssg = "Es ist die erste Nacht. In dieser Phase werden nur diejenigen Spezielkarten aufgerufen, welche eine einmalige Funktion erfüllen. (z.B. Amor oder Doppelgängerin). Für den Werwölfen ist nun ein privater Chat freigeschaltet, diese einigen sich jedoch in der ersten Nacht noch auf kein Opfer.";
		Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
	}

	public static void helpDayPhase(MessageCreateEvent event) {
		var mssg = "Es ist zurzeit Tag. In dieser Phase versuchen die Dorfbewohner durch Diskussion herauszufinden, wer die Werwölfe sind. Die Werwölfe hingegen versuchen nicht aufzufallen. Jeder Spieler kann jeden Tag mit \""
				+ prefix
				+ "vote <Name des Spielers> \" für den Tod eines Mitspielers stimmen. Die Stimme kann hierbei jederzeit durch das erneute Aufrufen des Commands geändert werden.\nSobald alle noch lebenden Spieler abgestimmt haben und eine Mehrheit besteht, kann der Moderator diesen lynchen. Mit \""
				+ prefix + "endDay\" kann der Moderator das Spiel beenden.";
		Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
	}

	public static void helpMorning(MessageCreateEvent event) {
		var mssg = "Es ist Morgen. In dieser Phase werden vom Moderator die Opfer der Nacht angekündigt.";
		Globals.createMessage(event.getMessage().getChannel().block(), mssg, false);
	}

	public static void helpNightPhaseMod(MessageCreateEvent event) {
		Globals.createMessage(event.getMessage().getChannel().block(),
				"Es ist Nacht. In dieser Phase schlafen alle Spieler und der Moderator lässt nach und nach Spezialkarten aufwachen, welche einen einfluss auf den ablauf der Nacht haben. Die Werwölfe einigen sich mittels Werwolf-Chat auf das heutige Opfer.",
				false);
	}

	public static void helpMorningMod(MessageCreateEvent event) {
		Globals.createMessage(event.getMessage().getChannel().block(),
				"Es ist Tag. In dieser Phase wird auf dem Dorfplatz diskutiert welcher Spieler am ende des Tages öffentlich Hingerichtet werden soll. mit dem Command "
						+ prefix + "vote <Spielernamen> können alle Lebenden Spieler für eine Person abstimmen.",
				false);

	}

	// ---------ERROR MESSAGES--------------------------------------------

	public static void errorNoAccessToCommand(Game game, MessageChannel messageChannel) {
		messageChannel.createMessage("E: you have no access to this command").block();
	}

	public static void errorWrongSyntaxKill(Game game, MessageCreateEvent event) {
		event.getMessage().getChannel().block()
				.createMessage("E: Ich verstehe dich nicht 😕\nDein Command sollte so aussehen: \n\"" + prefix
						+ "kill\" <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nBeispiel: &kill Anne-Frank Werwolf \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verantwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)")
				.block();
	}

	public static void errorPlayerNotFound(MessageChannel msgChannel) {
		msgChannel.createMessage(
				"E: Player not found.\nWenn der Spielername ein Leerzeichen enthält, ersetze diesen durch einen Bindestrich (-)")
				.block();
	}

	public static void errorModOnlyCommand(MessageChannel msgChannel) {
		msgChannel.createMessage("E: Only the moderator can use this command").block();
	}

	public static void errorPlayerAlreadyDead(Game game, MessageChannel msgChannel) {
		msgChannel.createMessage("E: The Person you Voted for is already dead (Seriously, give him a break)").block();
	}

	public static void errorWrongSyntax(Game game, MessageChannel msgChannel) {
		msgChannel.createMessage("E: Wrong Syntax - I can't understand you").block();
	}

	public static void errorNotAllowedToVote(Game game, MessageChannel msgChannel) {
		msgChannel.createMessage("E: You are not allowed to vote!").block();
	}

	public static void errorPlayersIdentical(MessageChannel msgChannel) {
		msgChannel.createMessage("E: Players are identical. Try again.").block();
	}
	// ---------Need To Sort--------------------------------------------

}