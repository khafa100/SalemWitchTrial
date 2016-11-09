/**
 * ClientListener.java
 *
 * This class runs on the client end and just
 * displays any text received from the server.
 *
 */
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class ClientListener implements Runnable{
	private Socket connectionSock = null;
	private Control control=null;

	ClientListener(Socket sock, Control control){
		this.connectionSock = sock;
		this.control=control;
	}

	public void run(){
       		 // Wait for data from the server.  If received, output it.
		try{
			BufferedReader serverInput= new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
			String IOState, serverReply;
			while (!control.end){
				// Get data sent from the server
				IOState = serverInput.readLine();
				if (IOState.equals("2")){
					serverReply= serverInput.readLine();
					System.out.println(serverReply);
				}
				else{
					control.end=true;
					serverInput.close();
				}
			}
		}
		catch (Exception e){
			System.out.println("Error: " + e.toString());
		}
	}
} // ClientListener for MTClient
