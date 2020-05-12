package wwBot.GameStates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Player;

public class MainGameState extends GameState {

    public static List<Boolean> nightRoles = new ArrayList<>();


    MainGameState(Game game){
        super(game);
        registerGameCommands();
        var mapPlayer = game.mapPlayer;
        var listRoles = new HashMap<String,List<Player>>();


        for (HashMap.Entry<String, Player> entry : mapPlayer.entrySet()) {
            var listWerwölfe = new ArrayList<Player>();
            var listDorfbewohner = new ArrayList<Player>();
            var listSeher = new ArrayList<Player>();

            if(entry.getValue().role.name.equalsIgnoreCase("Werwolf")){
                listWerwölfe.add(entry.getValue());
            }else if(entry.getValue().role.name.equalsIgnoreCase("Seher")){
                listSeher.add(entry.getValue());
            }else if(entry.getValue().role.name.equalsIgnoreCase("Dorfbewohner")){
                listWerwölfe.add(entry.getValue());
            }else{

            }
        }




        //amor, doppelgänger, günstling, werwolf 
        var playerGuenstling = mapPlayer.get("Günstling");
        var playerAmor = mapPlayer.get("Amor");    
        var playerDoppelgänger = mapPlayer.get("Doppelgänger"); 
        
        if (playerGuenstling != null){
             /* var privateChannel = playerGuenstling.user.getPrivateChannel().block();
                    privateChannel.createMessage("Die Werwölfe sind ").block();
                    for(int i=0; i<listWerwölfe.size(); i++){
                        privateChannel.createMessage(listWerwölfe.get(i)).block();;
                    }  */
        }
         
        if (playerAmor !=null){
            var privateChannel = playerAmor.user.getPrivateChannel().block();
                    privateChannel.createMessage("Bitte geben sie zwei Spieler an welche sich nun verlieben. Dies können Sie mit \"Playername\" aufrufen" ).block();
            
            
        }
        
                



        //überprüft ob jeder boolean der liste true ist 
        var isDone = true;
        for (Boolean bool1 : nightRoles) {
           if(!bool1){
                isDone = false;
           } 
        }
        //falls alle player fertig sind wird die nächste phase gestartet
        if(isDone){
            //wwPhase.start();
        }


    }

    //loads the Commands available in this GameState into the map gameStateCommands
    private void registerGameCommands(){
        //TODO: add all Commands needed here


    }

    //TODO: add Day Night Cycle
}