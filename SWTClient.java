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
  class Control{
    public volatile boolean end=false;
  }
  public static void main(String[] args){
    Scanner keyboard;
    String userInput="", serverReply="", IOState="";                              
    boolean isFirst;
    Socket connectionSock;
    BufferedWriter serverOutput;
    BufferedReader serverInput;
    Control control= new Control();

    keyboard = new Scanner(System.in);
    System.out.print("Please enter the address of the Nim server (press enter if it is a localhost): ");
    userInput= keyboard.nextLine();
    if(userInput.equals(""))
      userInput="localhost";
    System.out.println("Connecting to server on port 7654.");
    try{
      while(true){
        connectionSock=new Socket(userInput, 7654);
        serverOutput= new BufferedWriter(new OutputStreamWriter(connectionSock.getOutputStream()));
        serverInput= new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
        System.out.println("Connection made.");

        System.out.println("Welcome to Salem Witch Trial Game!!");
        System.out.println("You will be matched with 8 other players to play a game. Here are the rules:");
        System.out.println("    - You will be given a role which will be either on Innocent or Mafia faction.");
        System.out.println("    - The Mafia will win if it has more players than the Innocent does.");
        System.out.println("    - The Innocent will win if all the Maifa members are killed.");
        System.out.println("--------------------------------------------------------------------------------------");

        System.out.print("Please enter a name you wished to be addressed as: ");
        userInput= keyboard.nextLine();
        serverOutput.write(userInput);
        serverOutput.flush();
        
        while(true){
          IOState=serverInput.readLine();
          switch(IOState){
            case "0":
              serverReply= serverInput.readLine();
              System.out.println(serverReply);
              break;
            case "1":
              serverReply= serverInput.readLine();
              System.out.println(serverReply);
              userInput= keyboard.nextLine();
              serverOutput.write(userInput);
              serverOutput.flush();
              break;
            case "2":
              serverInput.close();
              ClientListener listener = new ClientListener(connectionSock, control);
			        Thread theThread = new Thread(listener);
			        theThread.start();
              while(!control.end){
                userInput= keyboard.nextLine();
                if (control.end)//double checking
                  break;
                serverOutput.write(userInput);
                serverOutput.flush();
              }
              serverInput= new BufferedReader(new InputStreamReader(connectionSock.getInputStream()));
              break;
            default:
              System.out.println("IO Error had occured in the server.");
              break;
          }
              
        }

        if(serverReply.equals("8")){
          System.out.println("Waiting for other players...");
          serverReply=serverInput.readLine();
        }
        role= Integer.parseInt(serverReply);
        System.out.println("--------------------------------------------------------------------------------------");
        System.out.println("Your role is "+roleList[role]+".");
        System.out.println(descriptionList[role]);
        System.out.println("--------------------------------------------------------------------------------------");
        
        connectionSock.close();
        serverInput.close();
        serverOutput.close();
        System.out.print("Would you like to play again? (y/n): ");
        userInput= keyboard.nextLine();
        if(userInput.equals("n"))
          break;
      }
    }
    catch (IOException e){
      System.out.println(e.getMessage());
    }
  }
}
