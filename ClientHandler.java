/**
* ClientHandler.java
*
* This class handles communication between the client
* and the server. It runs in a separate thread but has a
* link to a common list of sockets to handle broadcast.
*/

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
	private Player player;
	private ArrayList<Player> playerList;
	private PlayerSync sync;

	ClientHandler(Player player, ArrayList<Player> playerList, PlayerSync sync) {
		this.player=player;
		this.sync=sync;
		this.playerList=playerList; // Keep reference to master list
	}

	public void run() {
		// Get data from a client and send it to everyone else
		String playerText=null;
		Socket playerSocket=player.getSocket();
		try {
			gameloop:
			while(sync.currentStage<7) {
				switch (sync.currentStage){
					case 1:
								playerText = player.getMessage();
								if(playerText != null) {
									for(Player p: playerList) {
										if(p.getSocket() != playerSocket)
										p.sendMessage("2\n["+player.getUsername()+"]: "+playerText);
									}
								}
								else {// Connection was lost
									System.out.println("Closing connection for" + player.getUsername());
									playerList.remove(player);// Remove from arraylist
									player.close();
									break gameloop;
								}
								if(sync.endStage){
									player.sendMessage("0\n2 minutes chatting has ended.");
									sync.threadDone++;
									while(sync.currentStage==1){
										Thread.sleep(1000);
									}
								}
								break;
					case 2:
								player.sendMessage("0\nYou can nominate one person to lynch.");
								int count=1;
								for(Player p: playerList) {
									if(p.isAlive())
										player.sendMessage("0\n"+count+" :"+p.getUsername());
										count++;
								}
								player.sendMessage("1\nPlease choose the number corresponding to the username to nominate. (1-"+count+")");

								while(true){
									try{
										playerText=player.getMessage();
										int v = Integer.parseInt(playerText);
										if(v<sync.mafiaMember+sync.innocentMember){
											sync.vote[v-1]++;
											break;
										}
										else
											player.sendMessage("1\nPlease enter a number \"corresponding\" to the username to nominate. (1-"+count+")");
									}
									catch(NumberFormatException nfe){
										System.out.println("Input was not an int.");
										player.sendMessage("1\nPlease enter a \"number\" corresponding to the username to nominate. (1-"+count+")");
									}
								}
								sync.threadDone++;
								while(sync.currentStage==2){
									Thread.sleep(1000);
								}
								break;
						case 3:
								if(sync.mostVote==-1){
									player.sendMessage("0\nNo person got majority of the votes.");
								}
								else if(sync.mostVote==playerList.indexOf(player)){
									player.sendMessage("0\nYou were chosen to be lynched.");
									player.sendMessage("1\nPlease say your final statement before everyone decide if they should lynch you.");
									playerText = player.getMessage();
										for(Player p: playerList) {
											if(p.getSocket() != playerSocket)
											p.sendMessage("0\n["+player.getUsername()+"]: "+playerText);
										}
								}
								else{
									Player p = playerList.get(sync.mostVote);
									player.sendMessage("0\n"+p.getUsername()+" was chosen to be lynched.");
									player.sendMessage("0\n"+p.getUsername()+" is stating their final words...");
									Thread.sleep(3000);
									player.sendMessage("1\nDo you want to lynch"+p.getUsername()+"? (y/n)");
									playerText=player.getMessage();
									if(playerText.equals("y")){
										sync.vote[0]++;
									}
									else{
										sync.vote[1]++;
									}
								}

				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			playerList.remove(player);
			player.close();
		}
	}
}
