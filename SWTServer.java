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
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

public class SWTServer {
	// Maintain list of all client sockets for broadcast
	private ArrayList<Socket> socketList;

	public SWTServer.java() {
		socketList = new ArrayList<Socket>();
	}

	private void getConnection() {
		// Wait for a connection from the client
		try {
			System.out.println("Waiting for client connections on port 7654.");
			ServerSocket serverSock = new ServerSocket(7654);
			// This is an infinite loop, the user will have to shut it down
			// using control-c
			SWTGame game = new SWTGame();

			while(true) {
				for(int i = 1; i < 10; i++) {
					Socket connectionSock = serverSock.accept();
					// Add this socket to the list
					socektList.add(connectionSock);
					// Send to ClientHandler the socket and arraylist of all sockets

					System.out.println("Player " + i + " connected successfully.");

					ClientHandler handler = new ClientHandler(connectionSock, this.socketList);
					Thread theThread = new Thread(handler);
					theThread.start();
			}
			// Will never get here, but if the above loop is given
			// an exit condition then we'll go ahead and close the socket
			//serverSock.close();
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