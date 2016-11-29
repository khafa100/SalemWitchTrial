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
	
	private Player player;//This contains all the functions of the current client
	private ArrayList<Player> playerList;//This contains the list of all the player that is watching the game
	private PlayerSync sync;//This is a class shared between all players and the game
	private Socket playerSocket;//The socket of the player
	private String descriptionArray[]= new String[]{"You heal one person (including yourself) each night preventing them from dying.",
	"You are a part of the crowd that can vote to lynch people each round.",
	"You are a part of the crowd that can vote to lynch people each round.",
	"You investigate one person each night to find out if the target is a member of the Mafia, except for the Godfather and Vigilante. Beware for Framer can fool you.",
	"You watch one person each night to see who visits them.",
	"You kill someone each night to save the town.",
	"You choose someone to frame at night. If the target is investigated, they will appear to be a member of the Mafia. If Mafioso dies, you will take their position.",
	"You carry out Godfather’s orders. If Godfather dies, you will take their position.",
	"You will kill someone each night if the Mafioso is dead. Else, the Mafioso will kill the target for you."};//Descriptions of the roles
	private String roleArray[]= new String[]{"Doctor","Civilian","Civilian","Sheriff","Lookout","Vigilante","Framer","Mafiaso","Godfather"};//Name of the roles
	private String playerText=null;//The will store inputs from the user

	ClientHandler(Player player, ArrayList<Player> playerList, PlayerSync sync) {
		this.player=player;
		this.sync=sync;
		this.playerList=playerList;
		playerSocket=player.getSocket();//This will be later used to compare sockets to make sure it is not the player himself
	}

	public void run() {
		try {
			//Stating and describing player's role
		  	player.sendMessage("0\n-----------------------------------------------------------------");
			player.sendMessage("0\nYour role is "+roleArray[player.getRole()]+".");
			player.sendMessage("0\n"+descriptionArray[player.getRole()]);
			game:
			while(sync.currentStage<6) {
				switch (sync.currentStage){
					case 1://Day chat server stage
  					printBoard();//Stating the current stage
  					for(int x=0; x<2; ++x){
  					  if(sync.vote[x]!=-1){//checking if any players are killed from stage 5
  					    if(playerSocket==playerList.get(sync.vote[x]).getSocket()){//if the player himself is killed
  					      if(deadQuestion()){
  					        break game;
  					      }
  					    }
  					    else if(player.isAlive()){//if someone died, the roles will be updated depending on the role
  					      Player dead=playerList.get(sync.vote[x]);
  							  if(dead.getRole()==8){
  								  if(player.getRole()==7){
  								    player.sendMessage("0\nYou role is promoted to Godfather.");
  									  player.setRole(8);
  								  }
  								  else if(player.getRole()==6){
  								    player.sendMessage("0\nYou role is promoted to Mafiaso.");
  									  player.setRole(7);
  								  }
  								  player.sendMessage("0\n"+descriptionArray[player.getRole()]);
  							  }
  							  else if(dead.getRole()==7 && player.getRole()==6){
  							    player.sendMessage("0\nYou role is promoted to Mafiaso.");
  							    player.sendMessage("0\n"+descriptionArray[7]);
  								  player.setRole(7);
  							  }
  						  }
  					  }
  					}
  					if(player.isAlive()){//intiating the chat server
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
    							sync.deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
    							playerList.remove(player);// Remove from arraylist
    							player.close();
    							break game;
    						}
    						if(sync.endStage){//checking for end condition
    							player.sendMessage("0\n2 minutes chatting has ended.Press \"Enter\" to continue...");
    							break;
    				}
      				}
      				sync.threadDone++;//stating that stage one is completed
  				  }
  					player.sendMessage("0\nWaiting for other players to finish...");
  					while(sync.currentStage==1){//will loop until all players are finished
  						Thread.sleep(1000);
  			    }
  					break;

					case 2:
  					printBoard();
  					player.sendMessage("0\nYou can nominate one person to lynch.");
  					player.sendMessage("0\n-----------------------------------------------------------------");
  					//printing out the list of players
					int count=0;
  					player.sendMessage("0\n0 : No nomination");
  					for(Player p: playerList) {
  						if(p.isAlive()){
  						  count++;
  						  player.sendMessage("0\n"+count+" : "+p.getUsername());
  					  }
  					}
  					if(player.isAlive()){//asking for input to lynch
  						player.sendMessage("1\nPlease choose the number corresponding to the username to nominate. (0-"+count+")");
  						while(true){
  							try{
  								playerText=player.getMessage();
  								int v = Integer.parseInt(playerText);
  								if(v<=sync.mafiaMember+sync.innocentMember&&v>0){
  									player.sendMessage("0\nWaiting for other players to finish...");
  									sync.vote[v-1]++;
  									break;
  								}
  								else if(v==0){
  								  player.sendMessage("0\nWaiting for other players to finish...");
  									break;
  								}
  								else{
  								  player.sendMessage("1\nPlease enter a number \"corresponding\" to the username to nominate. (0-"+count+")");
  								}
  							}
  							catch(NumberFormatException nfe){
  								player.sendMessage("1\nPlease enter a \"number\" corresponding to the username to nominate. (0-"+count+")");
  							}
  						}
  					  sync.threadDone++;//stating that stage one is completed
  					}
  					while(sync.currentStage==2){//will loop until all players are finished
  						Thread.sleep(1000);
  					}
  					break;

					case 3:
  					printBoard();
  					if(sync.mostVote==-1){//if the no one got lynched
  						player.sendMessage("0\nNo one got majority of vote to get lynched.");
  					}
  					else if(sync.mostVote==playerList.indexOf(player)){//if the player is lynched
  						player.sendMessage("0\nYou were lynched.");
  						if(deadQuestion()){
  						  break game;
  						}
  					}
  					else{//if someone got lynched state their role and update the roles of mafia members
  						Player nominee=playerList.get(sync.mostVote);
  						player.sendMessage("0\n"+nominee.getUsername()+" was lynched.");
  						player.sendMessage("0\nTheir role was "+roleArray[nominee.getRole()]+".");
  						if(player.isAlive()){
  							if(nominee.getRole()==8){
  								if(player.getRole()==7){
  								  player.sendMessage("0\nYou role is promoted to Godfather.");
  									player.setRole(8);
  									player.sendMessage("0\n"+descriptionArray[8]);
  								}
  								else if(player.getRole()==6){
  								  player.sendMessage("0\nYou role is promoted to Mafiaso.");
  									player.setRole(7);
  									player.sendMessage("0\n"+descriptionArray[7]);
  								}
  							}
  							else if(nominee.getRole()==7 && player.getRole()==6){
  							  player.sendMessage("0\nYou role is promoted to Mafiaso.");
  							  player.sendMessage("0\n"+descriptionArray[7]);
  								player.setRole(7);
  							}
  					  }
  			}
  					player.sendMessage("0\n-----------------------------------------------------------------");
  					player.sendMessage("0\nGoing into night time...");
  					player.sendMessage("0\nMafia members can now chat for 1 minute.");
  					if(player.isAlive()){//Starting the night time chat server
  					  if(player.getRole()>5){
    						player.sendMessage("2\nYou can now chat with other Mafia Memebers for 1 minute.");
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
      							sync.deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
      							playerList.remove(player);// Remove from arraylist
      							player.close();
      							break game;
      						}
        					if(sync.endStage){//checking for end condition
        						player.sendMessage("0\n1 minutes chatting has ended. Press \"Enter\" to continue...");
        						break;
        					}
    						}
  					  }
  					  sync.threadDone++;
  					}
  					player.sendMessage("0\nWaiting for other players to finish...");
  					while(sync.currentStage==3){
      				Thread.sleep(1000);
      	}
  					break;

					case 4:
  					printBoard();
  					player.sendMessage("0\nStarting night action mode...");
  					count=0;
  					player.sendMessage("0\n0 : Do nothing");
  					for(Player p: playerList){
  						if(p.isAlive()){
  						  count++;
  						  player.sendMessage("0\n"+count+" : "+p.getUsername());
  					  }
  					}
  					String msg="1\n";
  					if(player.isAlive()){//if player is a civilian, don't ask anything
  					  if(player.getRole()==1 || player.getRole()==2){
    					player.sendMessage("0\nWaiting for other players to decide...");
    				}
  					  else{//else ask question depending on the role and take in the answer
      					switch(player.getRole()){//buliding the question depending on the role
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
      									msg+= "Please choose the number corresponding to the username to order the Mafiaso to kill.";
      									break;
      						default:
      									System.out.println("Error getting role...");
      									sync.deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
      									playerList.remove(player);
      									player.close();
      									break game;
      					}
  					    msg+=" (0-"+count+")";
  					    player.sendMessage(msg);
  					    while(true){//getting the correct input from the user
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
  							player.sendMessage(msg);
  						}
  					}
  				    }
    				  sync.threadDone++;
    				}
    				player.sendMessage("0\nProcessing all the actions...");
    				while(sync.currentStage==4){
    					Thread.sleep(1000);
    		}
    				break;

  				case 5:
    				if(player.isAlive()){
    					if(player.getRole()==3){//giving the investigation result if player is sheriff
    					  player.sendMessage("0\n-----------------------------------------------------------------");
    						if(sync.vote[3]==1){
    							player.sendMessage("0\nThe person you investigated is a memeber of the Mafia.");
    						}
    						else if (sync.vote[3]==0){
    							player.sendMessage("0\nThe person you investigated is innocent.");
    						}
    						else{
    						  player.sendMessage("You chose to not investgate anyone.");
    						}
    					}
    					if(player.getRole()==4){//telling who visited if player is look out
    					  player.sendMessage("0\n-----------------------------------------------------------------");
    						if(sync.vote[2]==-1){
    							player.sendMessage("0\nNothing happened.");
    						}
    						else{
    						  if(sync.vote[2]==8 && sync.mafiaMember!=1){
    						    sync.vote[2]=7;
    						  }
    						  for(Player p: playerList){
    						    if(p.getRole()==sync.vote[2]){
    							    player.sendMessage("0\nLast night, "+p.getUsername()+" visited the person you were watching.");
    						    }
    						  }
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
			if(sync.currentStage==6){//will ask question if the game is ended properly
			  endQuestion();
			}
		}
		catch (Exception e) {//If any error occur, the player will be considered dead and removed from the list
			System.out.println("Error: " + e.toString());
			sync.deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
			playerList.remove(player);
			player.close();
		}
	}
	private void printBoard(){//This show the current state of the game
		String alive="0\nAlive:  ";//contain all alive players
		String dead="";//contain all dead players
		dead+=sync.deadInitial;
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
		player.sendMessage("0\n-----------------------------------------------------------------");
		player.sendMessage("0\nMafia Members left: "+sync.mafiaMember);
		player.sendMessage("0\nInnocent people left: "+sync.innocentMember);
		player.sendMessage(alive+dead);
		player.sendMessage("0\n-----------------------------------------------------------------");
	}
	private void broadcastPlayers(String s){//This will send to all other players in the list
		for(Player p: playerList) {
			if(p.getSocket() != playerSocket)
			  p.sendMessage(s);
		}
	}
	private void endQuestion(){//The game is over so all the player have a choice to join another game or leave
		player.sendMessage("1\nDo you want to play another game? (y/n)");
		playerText=player.getMessage();
		if(playerText.equals("y")){
			player.sendMessage("4\nJoining another game...");
			playerList.remove(player);
			player.close();
		}
		else{
			player.sendMessage("3\nHave a nice day.");
			playerList.remove(player);
			player.close();
		}
}
  private boolean deadQuestion(){//if a person is dead, they can stay in the game, reconnect to another game, or quit from server
    player.sendMessage("0\nYou are dead.");
    player.sendMessage("0\n-----------------------------------------------------------------");
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
					sync.deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
					playerList.remove(player);
					player.close();
					return true;
				}
				else if(v==2){
					player.sendMessage("4\nJoining another game...");
					sync.deadInitial+=player.getUsername()+" ("+roleArray[player.getRole()]+"), ";
					playerList.remove(player);
					player.close();
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
