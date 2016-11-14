/**
 * SWTServer.java
 *
 * This program will:
 *   - Connect clients to a multithreaded chat server with the help of a ClientHandler in a separate file.
 *   - Assign Roles to players.
 *   - Manage the game stages.
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

public class SWTServer {
	// Maintain list of all client sockets for broadcast
	private ArrayList<Player> waitingList;


	public SWTServer() {
		waitingList = new ArrayList<Player>();
	}

	private void getConnection() {
		ArrayList<Player> playerList;
		Socket connectionSock;
		BufferedWriter clientOutput;
		BufferedReader clientInput;
		String playerName;
		Player player;
		// Wait for a connection from the client
		try {
			System.out.println("Waiting for client connections on port 7654.");
			ServerSocket serverSock = new ServerSocket(7654);
			// This is an infinite loop, the user will have to shut it down
			// using control-c
			while(true) {
					connectionSock = serverSock.accept();
					clientOutput= new BufferedWriter(new OutputStreamWriter(connectionSock.getOutputStream()));
		      clientInput= new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
					playerName=clientInput.readLine();
					player=new Player(connectionSock,clientInput,clientOutput,playerName,-1);

					waitingList.add(player);
					System.out.println(playerName+" has connected successfully.");

					if(waitingList.size()<9){
							clientOutput.write("0\n");
							clientOutput.write("Waiting for more players...\n");
							clientOutput.flush();
					}
					else{
						playerList=new ArrayList<Player>();
						for(int x=0; x<9; ++x){
							playerList.add(waitingList.remove(0));
						}
						SWTGame game=new SWTGame(playerList,waitingList);
						Thread theThread = new Thread(game);
						theThread.start();
					}

			// Will never get here, but if the above loop is given
			// an exit condition then we'll go ahead and close the socket
			//serverSock.close();
		}
	}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		SWTServer server = new SWTServer();
		server.getConnection();
	}
}
