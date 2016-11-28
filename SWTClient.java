/**
* SWTClient.java
*
* This program will connect to the server so that the user can play a multiplayer Salem Witch Trial game
* The game will have several stages
*/

import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Scanner;

public class SWTClient{
  public static void main(String[] args){
    Scanner keyboard;
    String userInput="", serverReply="", IOState="", username="", serverAddress="";
    boolean isFirst;
    Socket connectionSock;
    BufferedWriter serverOutput;
    BufferedReader serverInput;
    Control control= new Control();

    keyboard = new Scanner(System.in);
    System.out.print("Please enter the address of the server (press enter if it is a localhost): ");
    serverAddress= keyboard.nextLine();
    if(serverAddress.equals(""))
    serverAddress="localhost";
    System.out.println("Connecting to server on port 7654.");
    try{
      connectionSock=new Socket(serverAddress, 7654);
      serverOutput= new BufferedWriter(new OutputStreamWriter(connectionSock.getOutputStream()));
      serverInput= new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));

      System.out.println("Connection made.");
      System.out.println("--------------------------------------------------------------------------------");
      System.out.println("Welcome to Salem Witch Trial Game!!");
      System.out.println("You will be matched with 8 other players to play a game. Here are the rules:");
      System.out.println("    - You will be given a role which will be either on Innocent or Mafia faction.");
      System.out.println("    - The Mafia will win if it has more players than the Innocent does.");
      System.out.println("    - The Innocent will win if all the Maifa members are killed.");
      System.out.println("--------------------------------------------------------------------------------");

      System.out.print("Please enter a name you wished to be addressed as: ");
      username= keyboard.nextLine();
      serverOutput.write(username+"\n");
      serverOutput.flush();

      IOLoop:
      while(true){
        IOState=serverInput.readLine();
        switch(IOState){

          case "0": //server is broadcasting information
          serverReply= serverInput.readLine();
          System.out.println(serverReply);
          break;

          case "1"://server is asking a question and expect a response
          serverReply= serverInput.readLine();
          System.out.println(serverReply);
          userInput= keyboard.nextLine();
          serverOutput.write(userInput+"\n");
          serverOutput.flush();
          break;

          case "2"://server is in chat mode
          control.end=false;//shared variable between two thread to know when to end
          ClientListener listener = new ClientListener(serverInput, control);
          Thread theThread = new Thread(listener);
          theThread.start();
          while(!control.end){
            userInput= keyboard.nextLine();
            if (control.end)//double checking before sending the line
            break;
            serverOutput.write(userInput+"\n");
            serverOutput.flush();
          }
          break;

          case "3"://server said game over
          serverReply= serverInput.readLine();
          System.out.println(serverReply);
          break IOLoop;
          
          case "4"://server telling to connect again
          serverReply= serverInput.readLine();
          System.out.println(serverReply);
          serverInput.close();
          serverOutput.close();
          connectionSock.close();
          connectionSock=new Socket(serverAddress, 7654);
          serverOutput= new BufferedWriter(new OutputStreamWriter(connectionSock.getOutputStream()));
          serverInput= new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
          serverOutput.write(username+"\n");
          serverOutput.flush();
          break;

          default:
          System.out.println("IO Error had occured in the server.");
          break IOLoop;
        }

      }
      serverInput.close();
      serverOutput.close();
      connectionSock.close();
    }
    catch (IOException e){
      System.out.println(e.getMessage());
    }
  }
}
