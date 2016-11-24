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
	private ArrayList<Player> waitingList;
	private PlayerSync sync;
	private Socket playerSocket;
	private Player nominee=null;
	private String roleArray[]= new String[]{"Doctor","Civilian","Civilian","Sheriff","Lookout","Vigilante","Framer","Mafiaso","Godfather"};
	private String playerText=null, deadInitial="\n0\nDead:  ";

	ClientHandler(Player player, ArrayList<Player> playerList, ArrayList<Player> waitingList, PlayerSync sync) {
		this.player=player;
		this.sync=sync;
		this.playerList=playerList; // Keep reference to master list
		this.waitingList=waitingList;
		playerSocket=player.getSocket();
	}

	public void run() {
		try {
			gameloop:
			while(sync.currentStage<6) {
				switch (sync.currentStage){
					case 1:
					if((sync.vote[0]!=-1 && playerSocket==playerList.get(sync.vote[0]).getSocket()) || (sync.vote[1]!=-1 && playerSocket==playerList.get(sync.vote[1]).getSocket())){
					  if(deadQuestion()){
					    break gameloop;
					  }
					}
					if(player.isAlive()){
  					player.sendMessage("2\nYou can now chat for 2 minutes.");
  					while(true){
  						playerText = player.getMessage();
  						if(playerText != null){
  							if(!sync.endStage){
  								broadcastPlayers("2\n["+player.getUsername()+"]: "+playerText);
  							}
  						}
  						else {// Connection was lost
  							System.out.println("Closing connection for " + player.getUsername());
  							playerList.remove(player);// Remove from arraylist
  							player.close();
  							break gameloop;
  						}
  						if(sync.endStage){
  							player.sendMessage("0\n2 minutes chatting has ended.Press \"Enter\" to continue...");
  							break;
  						}
    				}
    				sync.threadDone++;
				  }
					
					player.sendMessage("0\nWaiting for other players to finish...");
					while(sync.currentStage==1){
						Thread.sleep(1000);
					}
					break;

					case 2:
					player.sendMessage("0\nYou can nominate one person to lynch.");
					int count=0;
					for(Player p: playerList) {
						if(p.isAlive()){
						count++;
						player.sendMessage("0\n"+count+" :"+p.getUsername());
					}
					}
					if(player.isAlive()){
						player.sendMessage("1\nPlease choose the number corresponding to the username to nominate. (1-"+count+")");
						while(true){
							try{
								playerText=player.getMessage();
								int v = Integer.parseInt(playerText);
								if(v<=sync.mafiaMember+sync.innocentMember&&v>0){
									player.sendMessage("0\nWaiting for other players to finish...");
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
					}
					if(player.isAlive()){
					  sync.threadDone++;
					}
					while(sync.currentStage==2){
						Thread.sleep(1000);
					}
					break;

					case 3:
					if(sync.mostVote==-1){
						player.sendMessage("0\nNo one got majority of vote to get lynched.");
					}
					else if(sync.mostVote==playerList.indexOf(player)){
							player.sendMessage("0\nYou were lynched.");
							if(deadQuestion()){
							  break gameloop;
							}
					}
					else{
							nominee=playerList.get(sync.mostVote);
							player.sendMessage("0\n"+nominee.getUsername()+" was lynched.");
							player.sendMessage("0\nTheir role was "+roleArray[nominee.getRole()]+".");
							if(player.isAlive()){
							if(nominee.getRole()==8){
								if(player.getRole()==7){
									player.setRole(8);
								}
								else if(player.getRole()==6){
									player.setRole(7);
								}
							}
							else if(nominee.getRole()==7 && player.getRole()==6){
								player.setRole(7);
							}
						}
					}
					player.sendMessage("0\nGoing into night time...");
					player.sendMessage("0\nMafia members are now chatting for 1 minute...");
					if(player.isAlive()&&player.getRole()>5){
						player.sendMessage("2\nYou can now chat with other Mafia Memebers for 1 minutes");
						while(true){
  						playerText = player.getMessage();
  						if(playerText != null){
  						  if(!sync.endStage){
								  for(Player p: playerList){
								    if(p.isAlive()&&p.getRole()>5&&p.getSocket()!=playerSocket){
								      p.sendMessage("2\n["+player.getUsername()+"]: "+playerText);
								    }
								  }
							  }
  						}
  						else {// Connection was lost
  							System.out.println("Closing connection for" + player.getUsername());
  							playerList.remove(player);// Remove from arraylist
  							player.close();
  							break gameloop;
  						}
    					if(sync.endStage){
    						player.sendMessage("0\n1 minutes chatting has ended. Press \"Enter\" to continue...");
    						break;
    					}
						}
					}
					if(player.isAlive()){
					  sync.threadDone++;
					}
					player.sendMessage("0\nWaiting for other players to finish...");
					while(sync.currentStage==3){
    							Thread.sleep(1000);
    			}
					break;

					case 4:
					player.sendMessage("0\nStarting night action mode...");
					count=0;
					for(Player p: playerList) {
						if(p.isAlive()){
						count++;
						player.sendMessage("0\n"+count+" :"+p.getUsername());
					}
					}
					String msg="1\n";
					if(player.isAlive()&&player.getRole()!=1&&player.getRole()!=2){
					switch(player.getRole()){
						case 0:
									msg+= "Please choose the number corresponding to the username to save.";
									break;
						case 3:
									msg+= "Please choose the number corresponding to the username to investigate.";
									break;
						case 4:
									msg+= "Please choose the number corresponding to the username to lookout.";
									break;
						case 5:
									msg+= "Please choose the number corresponding to the username to kill for justice.";
									break;
						case 6:
									msg+= "Please choose the number corresponding to the username to frame.";
									break;
						case 7:
									msg+= "Please choose the number corresponding to the username to kill in case if the Godfather doesn't choose a target.";
									break;
						case 8:
									msg+= "Please choose the number corresponding to the username to order the Mafiaso to kill. (Enter 0 to not give an order)";
									break;
						default:
									System.out.println("Error getting role...");
									playerList.remove(player);
									player.close();
									break gameloop;
					}
					msg+=" (1-"+count+")";
					player.sendMessage(msg);
					while(true){
						try{
							playerText=player.getMessage();
							int v = Integer.parseInt(playerText);
							if(v<=sync.mafiaMember+sync.innocentMember && v>=0){
								sync.vote[player.getRole()]=v-1;
								break;
							}
							else{
							player.sendMessage(msg);
						}
						}
						catch(NumberFormatException nfe){
							System.out.println("Input was not an int.");
							player.sendMessage(msg);
						}
					}
				}
  				else if(player.getRole()==1 || player.getRole()==2){
  					player.sendMessage("0\nWaiting for other players to decide...");
  				}
  				player.sendMessage("0\nProcessing all the actions...");
  				if(player.isAlive()){
  				  sync.threadDone++;
  				}
  				while(sync.currentStage==4){
  					Thread.sleep(1000);
  				}
  				break;

  				case 5:
  				if(player.isAlive()){
  					if(player.getRole()==4){
  						if(sync.vote[3]==1){
  							player.sendMessage("0\nThe person you investigated is a memeber of the Mafia.");
  						}
  						else{
  							player.sendMessage("0\nThe person you investigated is innocent.");
  						}
  					}
  					if(player.getRole()==5){
  						if(sync.vote[3]==-1){
  							player.sendMessage("0\nNo one visited the person you were watching last night.");
  						}
  						else{
  							Player p= playerList.get(sync.vote[3]);
  							player.sendMessage("0\nLast night, "+p.getUsername()+" visited the person you were watching.");
  						}
  					}
  					sync.threadDone++;
  				}
  				while(sync.currentStage==5){
  					Thread.sleep(1000);
  				}
  				break;
				}
			}
			if(sync.currentStage==6){
			  endQuestion();
			}
			
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			// Remove from arraylist
			playerList.remove(player);
			player.close();
		}
	}

	private void printBoard(){
		String alive="0\nAlive:  ";
		String dead=deadInitial;
		for(Player p: playerList){
			if(p.isAlive()){
				alive+=p.getUsername()+", ";
			}
			else{
				dead+=p.getUsername()+" ("+roleArray[p.getRole()]+"), ";
			}
		}
		alive= alive.substring(0,alive.length()-2);//deleting last instance of ", "
		dead= dead.substring(0,dead.length()-2);//deleting last instance of ", "
		player.sendMessage(alive+dead);
	}
	private void broadcastPlayers(String s){
		for(Player p: playerList) {
			if(p.getSocket() != playerSocket)
			p.sendMessage(s);
		}
	}
	// private void isGameOver(){//send the updated board and check for end conditions
	// 	player.sendMessage("0\nUpdating the player list...");
	// 	printBoard();
	// 	if (sync.mafiaMember>sync.innocentMember){
	// 		player.sendMessage("0\nThe game is over since there are more Mafia members now.");
	// 		endQuestion();
	// 	}
	// 	else if (sync.mafiaMember==0){
	// 		player.sendMessage("0\nThe game is over since all Mafia members are dead.");
	// 		endQuestion();
	// 	}
	// }
	private void endQuestion(){
		player.sendMessage("1\nDo you want to play another game? (y/n)");
		playerText=player.getMessage();
		if(playerText.equals("y")){
			player.sendMessage("0\nJoining another game...");
			playerList.remove(player);
			waitingList.add(player);
		}
		else{
			player.sendMessage("3\nHave a nice day.");
			playerList.remove(player);
			player.close();
		}
}
  private boolean deadQuestion(){
    player.sendMessage("0\nYou are dead.");
    player.sendMessage("0\nPlease choose one option below. (1-3)");
		player.sendMessage("0\n1. Quit this game and leave the server.");
		player.sendMessage("0\n2. Quit this game and join another game.");
		player.sendMessage("1\n3. Continue watching this game as a spectator.");
		while(true){
			try{
				playerText=player.getMessage();
				int v = Integer.parseInt(playerText);
				if(v==1){
					player.sendMessage("3\nHave a nice day.");
					deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
					playerList.remove(player);
					player.close();
					return true;
				}
				else if(v==2){
					player.sendMessage("0\nJoining another game...");
					deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
					playerList.remove(player);
					waitingList.add(player);
					return true;
				}
				else if(v==3){
					player.sendMessage("0\nEnjoy the conclusion.");
					playerList.remove(player);
					playerList.add(player);
					return false;
				}
				else{
					player.sendMessage("1\nPlease enter 1, 2, or 3.");
				}
			}
			catch(NumberFormatException nfe){
				System.out.println("Input was not an int.");
				player.sendMessage("1\nPlease enter a \"number\".");
			}
    }
  }
}
