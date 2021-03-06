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
	private BufferedReader serverInput = null;
	private Control control=null;

	ClientListener(BufferedReader serverInput, Control control){
		this.serverInput=serverInput;
		this.control=control;
	}

	public void run(){//this method will be called when the chat server stage begins
       		 // Wait for data from the server.  If received, output it.
		try{
			String IOState, serverReply;
			serverReply= serverInput.readLine();
			System.out.println(serverReply);
			while (!control.end){//will continue looping as long as IOState is 2.
				// Get data sent from the server
				IOState = serverInput.readLine();
				if (IOState.equals("2")){
					serverReply= serverInput.readLine();
					System.out.println(serverReply);
				}
				else if (IOState.equals("0")){//will set the sentinel value to true
					control.end=true;
					serverReply= serverInput.readLine();
					System.out.println(serverReply);
				}
			}
		}
		catch (Exception e){
			System.out.println("Error: " + e.toString());
			control.end=true;
		}
	}
} // ClientListener for SWTClient
