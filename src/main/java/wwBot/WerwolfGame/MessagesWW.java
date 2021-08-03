package wwBot.WerwolfGame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.TextChannel;
import wwBot.Globals;
import wwBot.Main;
import wwBot.WerwolfGame.cards.RoleZauberer;

public class MessagesWW {
	public static String prefix = Main.prefix;

	// ---------ONSTART MESSAGES--------------------------------------------

	// GAMESTART
	public static void newGameStartMessage(MessageChannel channel) {

		Globals.createEmbed(channel, Color.GREEN, "Created New Game!", "");
		Globals.createEmbed(channel, Color.GREEN, "",
				"Ihr befindet euch nun in der Lobby Phase. \nHier habt ihr Zeit für ein wenig Small-Talk während alle Mitspieler mit **\""
						+ prefix
						+ "join\"** dem Spiel beitreten und das Kartendeck erstellt wird. Genießt diese Zeit denn sobald das Spiel mit **\""
						+ prefix
						+ "Start\"** gestartet wird, könnt ihr niemanden mehr trauen.... \nFalls dies das erste mal ist, dass du mich benutzt oder du nicht weißt was du tun sollst, tippe **\""
						+ prefix + "help\"**.");

	}

	public static void greetMod(Game game) {
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Willkommen Moderator!", "");
		game.userModerator.getPrivateChannel().block().createMessage(
				"Deine Aufgabe ist es das Spiel für beide Parteien so fair wie möglich zu machen! \nDu kannst diesen Textkanal für Notizen benutzen.\nDu kannst ebenfalls jederzeit hier nützliche Commands benutzen, welche unter **\""
				+ prefix
				+ "showModCommands\"** angeführt werden.\nDu kannst nun die erste Nacht Starten, indem du mir **\"Ready\"** schreibst.")
				.block();
	}

	public static void onGameStartSemi(Game game) {

		// verkündet den Start der ersten Nacht
		Globals.createEmbed(game.mainChannel, Color.BLACK, "Willkommen bei : Die Werwölfe von Düsterwald", "");

		Globals.createMessage(game.mainChannel,
				"Unser Dorf wird seit den Tagen des alten Rom von Mythen und Sagen über Werwölfe heimgesucht. Seit kurzem sind diese Mythen zur Wirklichkeit geworden.",
				false);
		Globals.createMessage(game.mainChannel,
				"Im Mondschein bestimmen die Dorfbewohner, dass man dieser Situation ein Ende gesetzt werden muss. ",
				false);
		Globals.createMessage(game.mainChannel,
				"Es wird angekündigt das von nun an an jedem Morgen ein Dorfbewohner durch Abstimmung gelyncht wird. Somit beginnt die erste Nacht",
				false);

	}

	public static void onGameStartAuto(Game game) {
		var mssg = "";
		if (!game.gameRuleAutomaticMod) {
			mssg = "Euer Moderator ist: **" + game.userModerator.getUsername() + "!**";
		} else {
			mssg = "Es ist mir eine Ehre euer Moderator zu sein ✨";
		}

		// verkündet den Start der ersten Nacht
		Globals.createEmbed(game.mainChannel, Color.BLACK, "Willkommen bei : Die Werwölfe von Düsterwald", mssg);

	}

	public static void wwChatGreeting(TextChannel wwChat) {
		Globals.createEmbed(wwChat, Color.decode("#5499C7"), "Willkommen im Werwolf-Chat",
				"Dies ist ein Ort in dem die Werwölfe ungestört ihre Diskussionen durchführen können.");
	}

	public static void deathChatGreeting(TextChannel deathChat, Game game) {
		Globals.createEmbed(deathChat, Color.decode("#5499C7"), "Willkommen im Friedhof-Chat",
				"Dies ist ein Ort um ungestört über das Spiel zu diskutieren.");

	}

	// FIRST NIGHT
	public static void onFirstNightAuto(Game game) {
		Globals.createEmbed(game.mainChannel, Color.decode("#191970"), "🌙Die Erste Nacht🌙",
				"In dieser Phase erwachen all jene Spezialkarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren");

	}

	public static void onFirstNightSemi(Game game, ArrayList<Player> listRolesToBeCalled) {
		// Nachricht an alle
		Globals.createEmbed(game.mainChannel, Color.decode("#191970"), "🌙Die Erste Nacht🌙",
				"```In dieser Phase erwachen all jene Spezialkarten, welche in der ersten Nacht eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist, wird der Moderator den Namen deiner Rolle aufrufen. Um die Identität dieser Personen zu wahren, sollten nun alle Spieler ihre Augen schließen oder ihre Webcam deaktivieren.```\nTipp: ihr könnt mich jederzeit mit \"&showCard\" fragen euch eure Rolle zu Zeigen (tut dies im Privatchat mit mir, falls es geheim bleiben soll 😉). ");
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

	// NIGHT
	public static void onNightSemi(Game game, ArrayList<Player> sortedRoles) {
		// Nachricht an alle
		Globals.createEmbed(game.mainChannel, Color.decode("#191970"), "Es wird NACHT...🌇",
				"In dieser Phase des Spieles erwachen Spezialkarten und die Werwölfe einigen sich auf ein Opfer.");
		// Nachricht an Moderator
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"), "Nacht",
				"```Nachts erwachen die Werwölfe und einigen Sich auf ein Opfer. Dazu steht ihnen sein geheimer Text-Kanal auf dem Server zur verfügung, auf den auch du Zugriff hast. \nEbenfalls erwachen in dieser Phase einige Spezialkarten.``` \nEs folgt eine Liste mit den Rollen und die von ihnen zu befolgende Reihenfolge.");
		var mssg = "```\n";
		for (int i = 0; i < sortedRoles.size(); i++) {
			mssg += Integer.toString(i) + ") " + sortedRoles.get(i).name + ": ist " + sortedRoles.get(i).role.name
					+ "\n";
		}
		mssg += "```\nTipp: benutz &showCard <NameDerKarte> um dir die Details der Karte nochmals anzusehen";
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"),
				"Diese Rollen müssen in dieser Reihenfolge aufgerufen werden:", mssg);
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.decode("#191970"),
				"Wichtig! Töte den Player erst im Morgengrauen!",
				"Beende zuerst die Nacht mit **\"&endNight\"**, und versichere dich, dass alle Spieler wach sind bevor du den Spieler tötest und somit auch die Identität des Spielers preisgibst. Die Werwölfe haben Nachts immer auf einen Werwolf-Chat zugriff.");
	}

	public static void onNightAuto(List<Player> pending, MessageChannel channel) {
		Globals.createEmbed(channel, Color.decode("#191970"), "Es wird NACHT... 🌘", "");

	}

	public static void erwachenSpieler(MessageChannel mainChannel, List<Player> erwachen) {
		Globals.createEmbed(mainChannel, Color.LIGHT_GRAY, "", Globals.playerListToRoleList(erwachen, "Es erwachen"));

	}

	public static void onWWTurn(MessageChannel mainChannel, TextChannel wwChat) {
		Globals.createMessage(mainChannel,
				"```Die Nacht schreitet fort und als das ganze Dorf in einen pechschwarzen Schatten getaucht ist, kriechen die Werwölfe aus ihrem Versteck... 🌕```");
		Globals.createEmbed(mainChannel, Color.black, "DIE WERWÖLFE SCHLAGEN ZU 💀", "");

		Globals.createEmbed(wwChat, Color.black, "DIE WERWÖLFE SCHLAGEN ZU 💀",
				"Ihr könnt nun **&slay <Spieler>** benutzen um **EINEN** Spieler zu töten.");
	}

	public static void postWWTurn(MessageChannel mainChannel) {
		Globals.createMessage(mainChannel,
				"```Die Zeit verstreicht und der Gestank von Blut zieht durch die Gassen... 🌒```");
	}

	// MORNING
	public static void onMorningAuto(Game game) {
		Globals.createEmbed(game.mainChannel, Color.decode("#cf9b2b"), "Der MORGEN Bricht An...🌅",
				"Die Dorfbewohner erwachen, froh es durch die Nacht geschafft zu haben. Wer wird heute von ihnen gegangen sein?");
	}

	public static void onMorningSemi(Game game) {
		Globals.createEmbed(game.mainChannel, Color.decode("#cf9b2b"), "Der MORGEN Bricht An...🌅",
				"Die Dorfbewohner erwachen und ihnen schwant übles. Wer wird heute von ihnen gegangen sein?");
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.ORANGE, "MORGEN",
				"```In dieser Phase tötest du die Spieler welche in der vorherigen Nacht getötet wurden.```\nBeende diese Phase mit \""
						+ prefix + "endMorning\" ");
	}

	// DAY
	public static void onDayAuto(Game game) {
		Globals.createEmbed(game.mainChannel, Color.YELLOW, "Es wird TAG...☀️",
				"Die Dorfbewohner versammeln sich auf dem Dorfplatz und setzen ihre Besprechungen fort. Nun werden alle dazu aufgefordert mit **\""
						+ prefix
						+ "vote <playername>\"** für eine Person zu Stimmen. \n(Falls der Name der Person ein Lehrzeichen beinhaltet, ist man gebeten dieses durch einen Bindestrich \"-\" zu ersetzen.) \nDie Person mit den meisten Stimmen wird am Ende des Tages gelyncht.");
	}

	public static void onDaySemi(Game game) {
		Globals.createEmbed(game.mainChannel, Color.YELLOW, "Es wird TAG...☀️",
				"Die Dorfbewohner versammeln sich auf dem Dorfplatz und setzen ihre Besprechungen fort. Nun werden alle dazu aufgefordert mit **\""
						+ prefix
						+ "vote <playername>\"** für eine Person zu Stimmen. Man kann auch für <nobody> wählen. Die Person mit den meisten Stimmen wird am Ende des Tages gelyncht. ");
		Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.YELLOW, "TAG ☀️",
				"```In dieser Phase stimmen die Spieler ab. Der meistgewählte Spieler wird dir mitgeteilt. Du kannst dir jederzeit mit \""
						+ prefix + "showVotes\" einen Überblick verschaffen``` \nBeende diese Phase mit\"" + prefix
						+ "endDay\"");
	}

	// ---------UNIQUE CARDS MESSAGES--------------------------------------------

	// AMOR
	public static void triggerAmor(Game game, Player amor) {
		if (!game.gameRuleAutomaticMod) {
			Globals.createMessage(game.userModerator.getPrivateChannel().block(),
					"[Optional] Du kannst mir mitteilen welche zwei Spieler verliebt sind. \nTue dies mit \"" + prefix
							+ "&inLove\" <Player1> <Player2>",
					false);
		} else {
			// TODO: rework design
			Globals.createMessage(amor.user.getPrivateChannel().block(),
					"*Teile mir nun mit, welche zwei Spieler du sich ineinander verlieben lassen möchtest*💕\nTue dies indem innerhalb einer Nachricht beide Namen nur durch ein Leerzeichen getrennt schreibst; etwa so:\nRomeo Julia");
		}
	}

	public static void amorSuccess(Game game, Player firstLover, Player secondLover) {
		if (!game.gameRuleAutomaticMod) {
			Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.PINK, "ERFOLG!", ""
					+ firstLover.user.getUsername() + " und " + secondLover.name + " haben sich unsterblich verliebt");
		}

		Globals.createEmbed(game.mainChannel, Color.PINK, "Des Amors Liebespfeile haben ihr Ziel gefunden 💘!", "");

		Globals.createEmbed(firstLover.user.getPrivateChannel().block(), Color.PINK, "💘", "Du fällst mit **"
				+ secondLover.name
				+ "** in eine unsterbliche Liebe. \n Eure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstellen könnt und deshalb sterbt sobald euer Partner stirbt");

		Globals.createEmbed(secondLover.user.getPrivateChannel().block(), Color.PINK, "💘", "Du triffst dich mit **"
				+ firstLover.name
				+ "** und verliebst dich Unsterblich in sie/ihn \nEure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstellen könnt und deshalb sterbt sobald euer Partner stirbt");

	}

	// DOPPELGÄNGERIN
	public static void triggerDoppelgängerin(Game game, Player dp) {
		if (!game.gameRuleAutomaticMod) {
			Globals.createMessage(game.userModerator.getPrivateChannel().block(),
					"Teile mir mit für welchen Spieler die Doppelgängerin sich entscheidet, damit ich (falls dieser Spieler stirbt) die Rolle der Doppelgängerin ändern kann.\nTue dies mit \""
							+ prefix + "&Doppelgängerin\" <Player-Chosen-By-The-Doppelgängerin> ",
					false);
		} else {
			Globals.createMessage(dp.user.getPrivateChannel().block(),
					"Als Doppelgängerin kannst du mir nun den Namen eines Spielers deiner Wahl mitteilen. \nFalls diese Person stirbt nimmst du die Rolle dieser Person an. \nSolltest du so zum Beispiel unabsichtlich einen Werwolf gewählt haben und dieser stirbt, wirst du zum Werwolf und kämpfst anschließend Seite an Seite mit den anderen Werwölfen.\n\n~ __Schreibe nun einen Namen deiner Wahl, auf das sich eure Schicksale für immer verweben.__ ~");
		}
	}

	public static void doppelgängerinSuccess(Game game, Player dp, Player chosenOne) {
		if (!game.gameRuleAutomaticMod) {
			Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "ERFOLG!",
					dp.name + " hat sich an " + chosenOne.name + " gebunden.");
		}

		Globals.createEmbed(dp.user.getPrivateChannel().block(), Color.GREEN,
				"Du bist nun an " + chosenOne.name + " gebunden!", "");

	}

	public static void onDoppelgängerinTransformation(Game game, Player doppelgängerin, Player unluckyPlayer) {
		if (!game.gameRuleAutomaticMod) {
			// message to mod
			Globals.createMessage(game.userModerator.getPrivateChannel().block(),
					"Die Doppelgängerin wurde zu einem/einer " + unluckyPlayer.role.name + "!");
		}

		// message to DP
		Globals.createEmbed(doppelgängerin.user.getPrivateChannel().block(), Color.WHITE,
				"Du hast dich verwandelt!\nDeine neue Rolle ist: " + unluckyPlayer.role.name,
				"Die Person, welche du am Anfang des Spieles ausgewählt hast, ist gestorben. Durch deine ungwöhnlichen Fähigkeiten hast du seine Identität absorbiert. Du nimmst seine Rolle ein und wirst zu einem/einer "
						+ unluckyPlayer.role.name);

		Globals.printCard(unluckyPlayer.role.name, doppelgängerin.user.getPrivateChannel().block());

		// message to all
		Globals.createEmbed(game.mainChannel, Color.decode("#9b1f74"), "",
				"Unbemerkt saugt die Doppelgängerin die Identität des Toten auf und verwandelt sich... ");
	}

	// GÜNSTLING
	public static void günstlingMessage(PrivateChannel privateChannel, Map<String, List<Player>> mapExistingRoles,
			Game game) {
		var tempList = new ArrayList<Player>();

		for (int i = 0; i < mapExistingRoles.get("Werwolf").size(); i++) {
			tempList.add(mapExistingRoles.get("Werwolf").get(i));
		}

		if (mapExistingRoles.containsKey("Wolfsjunges")) {
			tempList.add(mapExistingRoles.get("Wolfsjunges").get(0));
		}

		Globals.createEmbed(privateChannel, Color.RED, "",
				Globals.playerListToList(tempList, "Die Werwölfe sind:", false));

	}

	// MÄRTYRERIN
	public static void remindAboutMärtyrerin(Game game) {
		Globals.createMessage(game.userModerator.getPrivateChannel().block(),
				"Vergiss nicht die Märtyrerin zu fragen ob sie sich anstelle der nominierten Person lynchen lassen will.");

	}

	public static void remindMärtyrerin(Game game, Player player, Player mostVoted) {

		Globals.createMessage(player.user.getPrivateChannel().block(), "Auf dem Schafott steht: **" + mostVoted.name
				+ "!** Nun liegt es an dir...\nWenn du dich anstelle des Spielers opfern willst, tippe **ja** und ansonsten **nein**.");
		Globals.createMessage(game.mainChannel, "Waiting for the \"Märtyrerin\" to act...");

	}

	// PRINZ
	public static void remindAboutPrinz(Game game) {
		Globals.createMessage(game.userModerator.getPrivateChannel().block(),
				"Wenn der Prinz duch \"" + prefix + "lynch\" stirbt, zeigt er seine Identität und überlebt.");
	}

	public static void prinzSurvives(Game game) {
		Globals.printCard("Prinz", game.mainChannel);
		game.mainChannel.createMessage(
				"Im letzten Moment enthüllt der Prinz Seine Identität. Geblendet von seiner Präsenz (und seinen weißen Zähnen) verschwindet die Wut der Dorfbewohner und der Prinz überlebt.")
				.block();
	}

	// HARTER BURSCHE
	public static void checkHarterBurscheDeath(MessageChannel modChannel) {
		Globals.createMessage(modChannel,
				"Du bist kurz davor den Harten Burschen zu töten. Dieser überlebt bis zum Abend, wenn er Nachts getötet wird. Wenn du dir sicher bist, dass jetzt der richtige moment ist den Harten Burschen zu töten, tippe \"confirm\". Andernfalls tippe \"cancel\"");
		Globals.printCard("Harter-Bursche", modChannel);
	}

	public static void harterBurscheSurvives(Game game) {
		Globals.createEmbed(game.mainChannel, Color.decode("#5b3a29"), "",
				"Als die Dorfbewohner morgens das Haus verlassen finden sie zu ihrem Entsetzen auf dem Dorfplatz den **Harten Burschen** schwer verwundet am Boden liegen. Dank seiner kräftigen Statur ist der Bursche noch nicht an seinen Wunden erlegen doch es ist klar, dass ihm nichht mehr viel Zeit bleibt... ");
	}

	// ALTE VETTEL
	public static void callVettel(Player vettel) {
		Globals.createMessage(vettel.user.getPrivateChannel().block(),
				"Schreibe mir den Namen des Spielers den du für den nächsten Tag verbannen möchtest.");
	}

	// SEHER
	public static void callSeher(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Nenne mir den Namen eines Spielers über den du mehr herausfinden möchtest.");
	}

	// a mightySeher is able to see the exact role of the player, a normal one only
	// if the player is friendly or not
	public static void showSeher(Player seher, Player found, Game game, boolean mightySeher) {
		var color = found.role.specs.friendly ? Color.GREEN : Color.RED;
		var name = found.role.name;
		var channel = seher.user.getPrivateChannel().block();
		var friendly = found.role.specs.friendly ? "Dieser Spieler ist auf der Seite der DORFBEWOHNER"
				: "Dieser Spieler ist auf der Seite der WERWÖLFE";

		// Lykantrothin
		if (name.equalsIgnoreCase("Lykantrophin")) {
			color = Color.RED;
			name = "Werwolf";
			friendly = "Dieser Spieler ist auf der Seite der WERWÖLFE";
		}

		if (mightySeher) {
			Globals.createEmbed(channel, color, "", found.name + " ist: " + found.role.name);

		} else {
			Globals.createEmbed(channel, color, "", friendly);
		}
	}

	// Aura-Seherin
	public static void callAuraSeherin(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Nenne mir den Namen eines Spielers über den du mehr herausfinden möchtest.");
	}

	public static void showAuraSeherin(Player seher, Player found) {
		var color = found.role.specs.unique ? Color.GREEN : Color.WHITE;
		var revelation = found.role.specs.unique ? " ist eine" : " ist keine";

		Globals.createEmbed(seher.user.getPrivateChannel().block(), color, found.name + revelation + " Spezial Rolle",
				"");

	}

	// LEIBWÄCHTER
	public static void callLeibwächter(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Nenne mir den Namen des Spielers den du diese Nacht beschützen möchtest.");
	}

	// SÄUFER
	public static void callSäufer(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Nenne mir den Namen des Spielers bei dem du die AfterParty starten lässt.");
	}

	// UNRUHESTIFTERIN
	public static void callUnruhestifterin(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Möchtest du diese Nacht deine Fähigkeit einsetzen? Du kannst mir mit  **ja**   bzw.   **nein**   antworten.");
	}

	public static void announceUnruhe(Game game) {
		Globals.createEmbed(game.mainChannel, Color.GRAY, "Zweite Abstimmung",
				"Ein Gerücht hier, eine Anschuldigung da; die Unruhestifterin weiß wie sie Angst sähen muss damit sie fruchtet.\nAufgebracht beschließen die Bewohner, dass heute **zwei** Personen ihr Leben lassen müssen.");

	}

	public static void secondVote(Game game) {
		Globals.createEmbed(game.mainChannel, Color.GRAY, "Zweite Abstimmung",
				"Die Dorfbewohner beschließen voller Rage, dass noch eine Person heute sterben muss!");
	}

	// PRIESTER
	public static void callPriester(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Möchtest du diese Nacht deine Fähigkeit einsetzen? Du kannst mir mit  **ja**   bzw.   **nein**   antworten.");
	}

	public static void confirmPriester(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Dann wollen wir mal! Nenne mir nun den Namen des Spielers den du durch die Macht Gottes beschützt haben möchtest.");
	}

	public static void savedByPriester(Player victim, Game game) {
		Globals.createEmbed(game.mainChannel, Color.decode("#FFD700"), "",
				"Engelsgesang erfüllt die Luft, so wunderschön als wäre es nicht für sterbliche Ohren bestimmt. So plötzlich wie der Gesang kam verschwindet er schließlich wieder und der Priester kniet betend zu Boden und dankt der bevorzugten Gottheit der Spieler, das Leben seines Schützlings gerettet zu haben.\n(TLDR: der Schutz des Priesters wurde ausgelöst.) ");
	}

	// ZAUBERMEISTERIN
	public static void callZaubermeisterin(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Nenne mir den Namen des Spielers über den du mehr herausfinden möchtest.");
	}

	public static void showZaubermeisterin(Player zaubermeisterin, Player found) {
		if (found.role.name.equals("Seherin")) {
			Globals.createEmbed(zaubermeisterin.user.getPrivateChannel().block(), Color.GREEN,
					found.name + " ist die Seherin!", "");
		} else {
			Globals.createMessage(zaubermeisterin.user.getPrivateChannel().block(),
					"**" + found.name + "** ist **nicht** die Seherin.");
		}
	}

	// PARANOMALER ERMITTLER
	public static void callErmittler(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Möchtest du diese Nacht deine Fähigkeit einsetzen? Du kannst mir mit  **ja**   bzw.   **nein**   antworten.");

	}

	public static void confirmErmittler(Player player) {
		Globals.createMessage(player.user.getPrivateChannel().block(),
				"Nun gut. Dann schreibe mir jetzt die Namen der drei Personen, die du mit deinen außergewöhnlichen (man könnte auch sagen: paranormalen) Detektivfähigkeiten genauer unter die Lupe nehmen möchtest.\nTipp: deine Nachricht sollte so aussehen **<Player1> <Player2> <Player3>**");
	}

	public static void ermittlerSuccess(Game game, List<Player> tempList, Player ermittler) {
		var color = !tempList.get(1).role.specs.friendly || !tempList.get(0).role.specs.friendly ? Color.RED
				: Color.GREEN;

		var mssg1 = tempList.get(0).role.specs.friendly ? tempList.get(0).name + " ist auf der Seite der DORFBEWOHNER"
				: tempList.get(0).name + " ist auf der Seite der WERWÖLFE";
		var mssg2 = tempList.get(1).role.specs.friendly ? tempList.get(1).name + " ist auf der Seite der DORFBEWOHNER"
				: tempList.get(1).name + " ist auf der Seite der WERWÖLFE";

		Globals.createEmbed(ermittler.user.getPrivateChannel().block(), color, "Erfolg",
				"Als Folge deiner Ermittlung erfährst du folgendes:\n" + mssg1 + "\n" + mssg2);
	}

	// Zauberer (Hexe / Magier)
	public static void callZauberer(Player playerZauberer, RoleZauberer roleZauberer, List<Player> atRiskPlayers,
			Game game) {
		var message = "";

		if (!roleZauberer.healUsed) {
			Globals.createEmbed(playerZauberer.user.getPrivateChannel().block(), Color.RED, "In Todesgefahr",
					Globals.playerListToList(atRiskPlayers, "AT RISK", false));
			message += "\nbenutze: **&heal <Player>** um einen Spieler der obigen Liste vor dem sicheren Tod zu bewahren.\n";
		}
		if (!roleZauberer.poisonUsed) {
			message += "benutze: **&poison <Player>** um einen Spieler deiner Wahl zu töten.\n";
		}

		message += "benutze: **&continue** um deinen Zug zu beenden und fortzufahren";

		Globals.createMessage(playerZauberer.user.getPrivateChannel().block(), message);

	}

	public static void callZaubererUsedEverything(PrivateChannel privateChannel) {
		privateChannel.createMessage("Looks like you already used all your Potions");
	}

	// ---------DEATH MESSAGES--------------------------------------------

	public static String revealId(Player player, Game game) {
		var mssg = player.name + " war **" + player.role.name + "**";
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
		Globals.createEmbed(game.mainChannel, Color.RED, player.name + " wird öffentlich hingerichtet! ",
				revealId(player, game));
	}

	public static void onLoversDeath(Game game, Player inLoveWith) {
		Globals.createMessage(game.mainChannel, inLoveWith.name
				+ " sieht den Leblosen Körper seiner wahren Liebe zu Boden sinken und in ihrer Seele zerbricht etwas...");
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

	public static void deathBySacrifice(Game game, Player player) {
		Globals.createEmbed(game.mainChannel, Color.RED,
				"Mutig und voller Entschlossenheit tritt die Mäetyrerin aufs Schafott. Alle Stimmen verstumme als sie mit einer kleinen Träne im Auge nicht vor der Henkersaxt zurück zuckt.",
				revealId(player, game));
	}

	public static void onAussätzigeDeath(Game game) {
		if (!game.gameRuleAutomaticMod) {
			Globals.createMessage(game.userModerator.getPrivateChannel().block(),
					"Die Aussätzige ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe niemanden töten");
		}

		Globals.createMessage(game.mainChannel,
				"Die Werwölfe wurden infiziert und dürfen in der nächsten Nacht niemanden töten", true);

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
		if (!game.gameRuleAutomaticMod) {
			Globals.createMessage(game.userModerator.getPrivateChannel().block(),
					"Das Wolfsjunges ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe zwei Personen töten.",
					false);
		}
		Globals.createEmbed(game.mainChannel, Color.red, "",
				"Die Werwölfmutter ist über ihren Verlust entsetzt und die Werwölfe beschließen, dass es in der nächsten Nacht 2 Tode geben wird.");

	}

	public static void wwEnraged(TextChannel wwChat) {
		Globals.createMessage(wwChat,
				"Da am Tag das Wolfsjunges getötet wurde, könnt ihr nun einen weiteren Spieler töten.");
	}

	public static void onJägerDeath(Game game, Player player) {
		Globals.createMessage(game.mainChannel,
				"Mit letzter Kraft zückt der Jäger sein Gewehr. Schreibe mir nun wen du töten möchtest.", false);
		Globals.createEmbed(player.user.getPrivateChannel().block(), Color.RED, "Du bist gefallen",
				"Als Jäger kannst du nun noch einen Schuss aus deinem Gewehr abgeben bevor du stirbst.\nSchreibe mir nun den Namen der Person die du töten möchtest!");
	}

	public static void säuferSurvives(Game game, Player player) {
		Globals.createMessage(game.mainChannel, "TODO: fill säuferSurvives", false);
	}

	// ---------VOTE MESSAGES--------------------------------------------

	public static void announceMajority(Game game, Player mostVoted, Map<Player, Player> mapVotes) {
		if (!game.gameRuleAutomaticMod) {
			Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.RED,
					"Alle Spieler Haben Gewählt!",
					"Auf dem Schafott steht **" + mostVoted.user.getMention() + "** der ein/eine **"
							+ mostVoted.role.name
							+ "** ist.\nMit \"**&lynch <Player>\"** kannst du einen Spieler lynchen und damit die Rolle des Spielers offenbaren. \nFalls du niemanden Lynchen möchtest kannst du auch gleich mit &endDay fortfahren");
		}
		var mssg = "";
		for (var entry : mapVotes.entrySet()) {
			mssg += entry.getKey().name + " hat für " + entry.getValue().name + " abgestimmt \n";
		}

		Globals.createEmbed(game.mainChannel, Color.WHITE,
				"Die Würfel sind gefallen \nAuf dem Schafott steht: " + mostVoted.name, mssg);
	}

	public static void voteResultNoMajority(Game game) {
		Globals.createMessage(game.mainChannel,
				"Alle Spieler haben abgestimmt, jedoch gibt es **keine klare Mehrheit**. Es wird gebete, dass wenigstens ein Spieler seine Stimme ändert, damit es zu einer klaren Mehrheit kommt.",
				false);
	}

	public static void votePlayer(Game game, Player voter, Player votedFor) {
		Globals.createMessage(game.mainChannel, voter.user.getMention() + " wählte für " + votedFor.user.getMention());
	}

	public static void voteNobody(Game game, Player voter) {
		Globals.createMessage(game.mainChannel, voter.user.getMention() + " will niemanden lynchen.");

	}

	// ---------HELP MESSAGES--------------------------------------------

	public static String buildDescription(String command, String description) {
		return "\n" + "`" + prefix + command + ":` " + description;
	}

	public static String getHelpInfo() {
		var mssg = "\n\n ------ Help ------";
		mssg += buildDescription("help", "Gibt dir Rat je nach deiner aktuellen Lage");
		mssg += buildDescription("showCommands", "Listet alle dir zurzeit verfügbaren Befehle auf");
		mssg += "\n**Vergiss nicht:** Zusammen mit dem Spiel ändert sich auch, welche Commands du benutzen kannst! Frag jederzeit mit den zwei obigen Commands nach Hilfe wenn du nicht weiter weißt 🙂";
		return mssg;
	}

	public static void sendHelpMain(MessageChannel channel) {
		Globals.createEmbed(channel, Color.BLACK, "", "Schreibe **" + prefix
				+ "newGame** zm ein neues Spiel zu starten! \n(Je Server kann nur ein Spiel gleichzeitig laufen) \nFalls du weitere Fragen hast kannst du jederzeit **"
				+ prefix
				+ "showCommands** eingeben um dir eine Liste der zurzeit verfügbaren Befehle anzeigen zu lassen oder erneut mit **"
				+ prefix + "help** nach hilfe fragen 😁");
	}

	public static void sendHelpLobby(MessageChannel channel) {
		// help
		Globals.createEmbed(channel, Color.BLACK, "____ Lobby Phase ____  ",
				" - Ihr könnt dem Dorf beitreten indem ihr **" + prefix
						+ "join** eingebt. \n - Sobald alle Mitspieler beigetreten sind, wollt ihr als nächstes euer Kartendeck für dieses Spiel bestimmen.\nMit **"
						+ prefix
						+ "buildDeck** generiert mein algorithmus automatisch ein faires Deck. \n Dieses kann anschließend mit **"
						+ prefix + "addCard <Karte>** und **" + prefix
						+ "removeCard <Karte>** bearbeitet werden. \n - Mit **" + prefix + "gamerule manual** und **"
						+ prefix
						+ "gamerule automatic** könnt ihr den Moderationsmodus des Spiels bestimmen. Bei \"Manual\" moderiert ein menschlicher Spieler den Spielverlauf und ich helfe ihm eine Übersicht zu behalten. Im \"Automatic\" Moderationsmodus nehme ich die Rolle des Moderators ein (Still in test phase)\n - Wenn alle Spieler beigetreten und ein Deck registriert wurde, lasse das Spiel mit **"
						+ prefix + "start** starten!*" + getHelpInfo());
	}

	public static void sendHelpFirstNight(MessageChannel channel, boolean auto) {
		if (auto) {
			Globals.createEmbed(channel, Color.BLACK, "____ Erste Nacht ____  ",
					"In dieser Phase rufe ich bestimmte Spezialkarten mit speziellen Funktionen zu Beginn des Spiels, wie z.B. Amor, auf. \nFür die Werwölfe öffnet sich, wie in jeder Nacht, ein Chatroom im Server, allerdings dürfen sie in der ersten Nacht noch niemanden töten. Überprüft ob ihr eine private Nachricht von mir erhalten habt. Falls ja, befinden sich dort genauere Informationen. Sobald alle aufgerufenen Spieler gehandelt haben, beginnt der erste Tag!");
		} else {
			Globals.createEmbed(channel, Color.BLACK, "____ Erste Nacht ____  ",
					"In dieser Phase werden vom Moderator Spezialkarten mit bestimmten Funktionen zu Beginn des Spiels, wie z.B. Amor, aufgerufen. Für die Werwölfe öffnet sich wie in jeder Nacht ein Chatroom im Server, allerdings dürfen sie noch niemanden töten. Überprüft ob ihr eine private Nachricht von mir erhalten habt. Falls ja, befinden sich dort genauere Informationen.");
		}

	}

	public static void sendHelpNight(MessageChannel channel, boolean auto) {
		if (auto) {
			Globals.createEmbed(channel, Color.BLACK, "____ Nacht ____  ",
					"In dieser Phase erwachen all jene Spezialkarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. \nAlle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren.\n Für die Werwölfe öffnet sich, wie in jeder Nacht, ein Chatroom im Server, wo sie ungestört diskutieren können. Sobald alle aufgerufenen Spieler gehandelt haben, beginnt der nächste Tag!\nTipp: benutze **&pending** un zu erfahren welche Spieler noch handeln müssen.");
		} else {
			Globals.createEmbed(channel, Color.BLACK, "____ Nacht ____  ",
					"In dieser Phase werden Spezialkarten vom Moderator in bestimmter Reihenfolge aufgerufen. Für die Werwölfe öffnet sich wie in jeder Nacht ein Chatroom im Server, wo sie ungestört diskutieren können.");
		}

	}

	public static void sendHelpMorning(MessageChannel channel, boolean auto) {
		if (auto) {
			Globals.createEmbed(channel, Color.BLACK, "____ Morgen ____  ",
					"Am Morgen werden die Opfer der Nacht verkündet. Ebenfalls finde spezielle Interaktionen, wie z.B. der Tod des Jägers, hier statt. Sobald alle Opfer verkündet wurden, beginnt der Tag und die Abstimmung beginnt.");
		} else {
			Globals.createEmbed(channel, Color.BLACK, "____ Morgen ____  ",
					"Am Morgen verkündet der Moderator die Opfer der Nacht (und spezielle Interaktionen wie Jäger finden statt)");
		}
	}

	public static void sendHelpDay(MessageChannel channel, boolean auto) {
		if (auto) {
			Globals.createEmbed(channel, Color.BLACK, "____ Tag ____  ",
					"Es ist zurzeit Tag. In dieser Phase versuchen die Dorfbewohner durch Diskussion herauszufinden, wer die Werwölfe sind. Die Werwölfe hingegen versuchen nicht aufzufallen. Jeder Spieler kann jeden Tag mit \""
							+ prefix
							+ "vote <Name des Spielers> \" für den Tod eines Mitspielers stimmen. Die Stimme kann hierbei jederzeit durch das erneute Aufrufen des Commands geändert werden.\nSobald alle noch lebenden Spieler abgestimmt haben und eine Mehrheit besteht, wird dieser öffentlich hingerichtet und die Bewohner schlafen wieder ein.");
		} else {
			Globals.createEmbed(channel, Color.BLACK, "____ Tag ____  ",
					"Es ist zurzeit Tag. In dieser Phase versuchen die Dorfbewohner durch Diskussion herauszufinden, wer die Werwölfe sind. Die Werwölfe hingegen versuchen nicht aufzufallen. Jeder Spieler kann jeden Tag mit \""
							+ prefix
							+ "vote <Name des Spielers> \" für den Tod eines Mitspielers stimmen. Die Stimme kann hierbei jederzeit durch das erneute Aufrufen des Commands geändert werden.\nSobald alle noch lebenden Spieler abgestimmt haben und eine Mehrheit besteht, kann der Moderator diesen lynchen. Mit \""
							+ prefix + "endDay\" kann der Moderator das Spiel beenden.");
		}
	}

	// Syntax: mssg += "\n" + "`" + prefix + "<Command>" + "`" + "<Description>"
	public static String getCommandsMain() {
		String mssg = " ---- Bot ---- ";
		mssg += buildDescription("newGame", "Startet ein neues Spiel");
		mssg += buildDescription("deleteGame", "Falls aus diesem Server zurzeit ein Spiel läuft, wird es beendet");
		mssg += buildDescription("explanation", "Falls du nach einer Erklärung suchst, was \"Werwölfe\" überhaupt ist");

		return mssg;
	}

	public static String getCommandsGame() {
		var mssg = "\n ---- Utility ----";
		mssg += buildDescription("showCard <Cardname>", "Zeigt die Details einer Karte");
		mssg += buildDescription("allCards", "Listet ALLE Spielkarten auf");
		mssg += buildDescription("manual", "Zeigt die Spielanleitung");
		mssg += buildDescription("showDeck", "Zeigt das aktuelle Kartendeck");

		return mssg;
	}

	public static String getCommandsLobby() {
		var mssg = "\n ---- Lobby ----";
		mssg += buildDescription("join", "Tritt dem Spiel bei");
		mssg += buildDescription("leave", "Verlasse ein beigetretenes Spiel");
		mssg += buildDescription("makeMeMod", "Werde zum Moderator");
		mssg += buildDescription("gamerule automatic", "Aktiviert den automatischen Moderator");
		mssg += buildDescription("gamerule manual", "Aktiviert den semiautomatischen Moderator");
		mssg += buildDescription("joinedPlayers", "Listet alle beigetretenen Spieler auf");
		mssg += buildDescription("buildDeck", "Kreiert ein ausgewogenes Kartendeck");
		mssg += buildDescription("addCard <Karte>", "Fügt eine Karte dem Deck hinzu");
		mssg += buildDescription("removeCard <Karte>", "Entfernt eine Karte aus dem Deck");
		mssg += buildDescription("clearDeck", "Entfernt alle Karten aus dem Deck");
		mssg += buildDescription("start", "Startet das Spiel");

		return mssg;
	}

	public static String getCommandsPostGame() {
		var mssg = " ---- PostGame ----";
		mssg += buildDescription("randomStat", "COMING SOMETIME MBY");

		return mssg;
	}

	public static String getCommandsSemiState() {
		var mssg = "\n ---- Day ----";
		mssg += buildDescription("vote <Player>", "Stimmt für die öffentliche Hinrichtung dieses Spielers");
		mssg += buildDescription("listVotes", "Listet alle Stimmen auf");

		return mssg;
	}

	public static String getCommandsAutoState() {
		var mssg = "\n ---- Day ----";
		mssg += buildDescription("vote <Player>", "Stimmt für die öffentliche Hinrichtung dieses Spielers");
		mssg += buildDescription("listVotes", "Listet alle Stimmen auf");

		mssg += "\n ---- Night ----";
		mssg += buildDescription("listPending", "Listet alle Rollen welche in dieser Nacht noch handeln müssen");
		mssg += buildDescription("listLiving", "Listet alle lebenden Spieler auf");

		return mssg;
	}

	// -------------------------------------------------------------

	public static void sendHelpFirstNightMod(MessageChannel channel) {

		Globals.createEmbed(channel, Color.BLACK, "____ Erste Nacht ____  ",
				"Die erste Nacht ist dazu da, dass du dir einen kurzen Überblick über das Spiel verschaffen kannst. Zudem rufst du Rollen wie z.B. den Amor auf, welche zu Beginn des Spiels in Aktion treten. Obig findest du eine Liste die dir sagt, welche Personen du in welcher Reihenfolge aufrufen solltest. Mit \"&endNight\" kannst du diese Phase beenden, tue dies aber erst, sobald du alle Spieler auf der Liste kontaktiert hast! "
						+ getHelpInfo());
	}

	public static void sendHelpNightMod(MessageChannel channel) {
		Globals.createEmbed(channel, Color.BLACK, "____ Nacht ____  ",
				"Zu Beginn jeder Nacht erhältst du eine Liste mit den Rollen welche in dieser Nacht aktiv werden. Jede Rolle verhält sich anders, was du mit **&showCard <Kartenname>** nachschlagen kannst. Im Server existiert ein geheimer Chat auf den nur die Werwölfe und du als Moderator Zugriff haben. Nachdem jede Rolle agiert hat und die Wölfe ihr Ziel dir mitgeteilt haben, beende die Nacht mit **&endNight**. \nWICHTIG: Töte die Opfer dieser Nacht erst nachdem du die Nacht beendet hast und der Morgen graut!"
						+ getHelpInfo());
	}

	public static void sendHelpDayMod(MessageChannel channel) {
		Globals.createEmbed(channel, Color.BLACK, "____ Tag ____  ",
				"Am Tag stimmen die Spieler für das nächste öffentliche Opfer. Wenn alle Spieler abgestimmt haben und eine Mehrheit besteht, bekommst du eine Nachricht von mit, welche dir rät, diesen Spieler mit **&lynch <Opfer>** zu töten. Du kannst allerdings jeden Spieler deiner Wahl hinrichten (falls du dies tun willst). Beende diese Phase mit **&endDay**."
						+ getHelpInfo());
	}

	public static void sendHelpMorningMod(MessageChannel channel) {
		Globals.createEmbed(channel, Color.BLACK, "____ Morgen ____  ",
				"In dieser Phase tötest du die Opfer der Nacht mit **&kill <Opfer> (Optional)<RolleDieTötete>** die Opfer der Nacht und ich verkünde automatisch deren Rolle. Beende diese Phase mit **&endMorning**. Ich überprüfe dann, ob das Spiel von einer Partei gewonnen wurde. "
						+ getHelpInfo());
	}

	public static String getModCommands() {
		var mssg = " ---- Game ----";
		mssg += buildDescription("kill <Player> <Role>",
				"Tötet diesen Spieler, optional kann auch die Todesursache in Form einer Rolle angegeben werden");
		mssg += buildDescription("listPlayers", "Listet alle Spieler und deren Rollen auf");
		mssg += buildDescription("listLiving", "Listet alle noch lebenden Spieler auf");
		mssg += buildDescription("mute <Player>", "Schaltet diesen Spieler stumm");
		mssg += buildDescription("unMute <Player>", "Hebt eine Stummschaltung wieder auf");
		mssg += buildDescription("muteAll", "Schaltet alle Spieler Stumm");
		mssg += buildDescription("listVotes", "Hebt die Stummschaltung aller Spieler auf");
		

		mssg += "\n" + MessagesWW.getModCommandsFirstNight();
		mssg += "\n" + MessagesWW.getModCommandsNight();
		mssg += "\n" + MessagesWW.getModCommandsMorning();
		mssg += "\n" + MessagesWW.getModCommandsDay();

		return mssg;
	}

	public static String getModCommandsFirstNight() {
		var mssg = " ---- First Night ----";
		mssg += buildDescription("Amor <Player1> <Player2>", "[Optional] Du kannst mir mitteilen welche zwei Spieler verliebt sind.");
		mssg += buildDescription("Doppelgängerin <Player-Chosen-By-The-Doppelgängerin>", "Bindet die Doppelgängerin an diese Person (!WICHTIG!)");
		mssg += buildDescription("endNight ", "Beendet die erste Nacht");

		return mssg;
	}

	public static String getModCommandsNight() {
		var mssg = " ---- Night ----";
		mssg += buildDescription("endNight", "Beendet die Nacht");
		return mssg;
	}

	public static String getModCommandsMorning() {
		var mssg = " ---- Morning ----";
		mssg += buildDescription("endMorning", "Beendet die MorgenPhase");
		return mssg;
	}

	public static String getModCommandsDay() {
		var mssg = " ---- Day ----";
		mssg += buildDescription("endNight", "Beendet den Tag");
		mssg += buildDescription("lynch <Player>", "Richtet diesen Spieler öffentlich hin");

		return mssg;
	}

	public static void sendGameExplanation(MessageChannel channel) {
		Globals.createEmbed(channel, Color.BLUE, "Was ist \"WERWÖLFE\"",
				"Werwölfe ist ein interaktives Deduktionsspiel für zwei Teams: die Dorfbewohner und die Werwölfe. Während die Dorfbewohner nicht wissen, wer die Werwölfe sind, versuchen diese, unentdeckt zu bleiben und einen Dorfbewohner nach dem anderen auszuschalten. Ein Moderator „leitet“ das Spiel (es kann einer der Spieler sein), und erleichtert so den Ablauf von Werwölfe. (Alternativ kann ich diese Rolle übernehmen). Eine Partie Werwölfe verläuft über eine Reihe von „Tagen“ und „Nächten“. Jede Nacht wählen die Werwölfe ein Opfer, während die einsame Seherin Informationen über einen anderen Spieler sammelt und so lernt, ob dieser ein Werwolf ist. Während des Tages versuchen die Spieler gemeinsam herauszufinden, wer von ihnen ein Werwolf ist, um ihn dann nach einer Abstimmung zu lynchen. Das Spiel gewinnt immer eine Gruppe – entweder die Dorf­ bewohner, wenn sie alle Werwölfe gelyncht haben, oder die Werwölfe, wenn sie einen Gleichstand mit den Dorfbewohnern erreicht haben. Bei einer Partie Werwölfe müssen Sie versuchen, die anderen Mitspieler in die Irre zu führen, um das Spiel gewinnen zu können.\nTipp: Mit **"
						+ prefix + "manual** kannst du die komplette Spielanleitung aufrufen.");

	}

	// ---------ERROR MESSAGES--------------------------------------------

	public static void errorNoAccessToCommand(Game game, MessageChannel messageChannel) {
		messageChannel.createMessage("```diff\n-E: you have no access to this command\n```").block();
	}

	public static void errorWrongSyntaxOnKill(MessageCreateEvent event) {
		event.getMessage().getChannel().block()
				.createMessage("```\nE: Ich verstehe dich nicht 😕\nDein Command sollte so aussehen: \n\"" + prefix
						+ "kill\" <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nBeispiel: &kill Anne-Frank Werwolf \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verantwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)\n```")
				.block();
	}

	public static void errorPlayerNotFound(MessageChannel msgChannel) {
		msgChannel.createMessage(
				"```diff\n-E: Player not found.\nWenn der Spielername ein Leerzeichen enthält, ersetze diesen durch einen Bindestrich (-)\n```")
				.block();
	}

	public static void errorMultiplePlayersNotFound(MessageChannel msgChannel) {
		msgChannel.createMessage(
				"```diff\n-E: One or Multiple Players not found.\nWenn der Spielername ein Leerzeichen enthält, ersetze diesen durch einen Bindestrich (-)\n```")
				.block();
	}

	public static void errorCardNotFound(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: Card not found.\nMit \"" + prefix
				+ "allCards\" kannst du dir eine Liste aller verfügbaren Karten anzeigen lassen\n```").block();
	}

	public static void errorModOnlyCommand(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: Only the moderator can use this command\n```").block();
	}

	public static void errorWWCommandOnly(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: Only the WW can use this command\n```").block();
	}

	public static void errorPlayerAlreadyDead(MessageChannel msgChannel) {
		msgChannel
				.createMessage(
						"```diff\n-E: The Person you Voted for is already dead (Seriously, give him a break)\n```")
				.block();
	}

	public static void errorWrongSyntax(MessageChannel msgChannel) {
		var a = Arrays.asList("```diff\n-E: Wrong Syntax - I can't understand you\n```",
				"```diff\n-E: Wrong Syntax - Use &showCommands for a list of all Commands\n```",
				"```diff\n-E: Wrong Syntax - TIPP: when writing the name of a player, use \"-\" insplace of a space\n```",
				"```diff\n-E: Wrong Syntax - I can't understand you", "E: Wrong Syntax - I can't understand you\n```");
		msgChannel.createMessage(a.get((int) Math.random() * a.size())).block();
	}

	public static void errorWrongAnswer(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: This was not an answer I was expecting. Try again!\n```").block();
	}

	public static void errorNotAllowedToVote(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: You are not allowed to vote!\n```").block();
	}

	public static void errorPlayersIdentical(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E:Two or more Players are identical. Try again.\n```").block();
	}

	public static void errorChoseIdenticalPlayer(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: You cannot choose the same player as last round. Try again.\n```")
				.block();
	}

	public static void errorChoseSelf(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: You cannot choose yourself. Try again.\n```").block();
	}

	public static void errorCommandNotFound(MessageChannel msgChannel) {
		msgChannel.createMessage("```diff\n-E: Command Not Found\n```").block();
	}

	// ---------Need To Sort--------------------------------------------

	public static void confirm(MessageChannel msgChannel) {

		var a = Arrays.asList("Okay", "OK", "Alrighty", "You're the Boss", "Done!", "Good Decision ;)", "Nice!");

		var randMssg = a.get((int) (Math.random() * a.size()));

		Globals.createEmbed(msgChannel, Color.GREEN, randMssg, "");
	}

	public static void voteResultNobody(Game game) {
		Globals.createMessage(game.mainChannel, "Die Dorfbewohner beschließen, dass heute niemand sterben soll.",
				false);
	}

	public static void notifyModGameEnd(PrivateChannel privateChannel, int a) {
		privateChannel.createMessage("**ATTENTION**\n the game has been won by *Winnercode: " + a + "*\n Winnercodes: winner: 1 = Dorfbewohner, 2 = Werwölfe, 3 = Ausgleich\nIf you want to confirm the Game end please use **"+prefix+"endGame <Winnercode>** or use the Command to get to the next DayPhase.").block();
	}

}