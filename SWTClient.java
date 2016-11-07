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
    String userInput, serverReply="", myUsername, serverAddress;
    String roleList[]= {"Civilian", "Doctor", "Sheriff", "Lookout", "Vigilante", "Framer", "Mafiaso", "Godfather"};
    String descriptionList[]={"You are in Innocent faction. You have no special power but you vote still counts!!!",
                              "You are in Innocent faction. Each night, you will pick a person (including yourself) to stop them from dying.",
                              "You are in Innocent faction. Each night, you will investigate one person to find out if they are part of Mafia. Godfather will not show up as one of Mafia and Framer can skew your investigation.",
                              "You are in Innocent faction. Each night, you will watch one person to see who visits them.",
                              "You are in Innocent faction. Each night, you will kill someone supscious in the name of justice!!!",
                              "You are in Mafia faction. Each night, you will frame one person. If the person is investigated by the Sheriff, he will appear as a Mafia member. If a Mafiaso dies, you will take his position.",
                              "You are in Mafia faction. Each night, you will follow Godfather's order.If Godfather dies, you will take his position.",
                              "You are in Mafia faction. Each night, you will order Mafiaso to kill one person. If there are no Mafiaso left, you will kill the person yourself.")
                              
    boolean isFirst;
    int port, role;
    Socket connectionSock;
    BufferedWriter serverOutput;
    BufferedReader serverInput;

    keyboard = new Scanner(System.in);
    System.out.print("Please enter the address of the Nim server (press enter if it is a localhost): ");
    serverAddress= keyboard.nextLine();
    if(serverAddress.equals(""))
      serverAddress="localhost";
    port=7654;
    System.out.println("Connecting to server on port " + port);
    try{
      while(true){
        connectionSock=new Socket(serverAddress, port);
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
        myUsername= keyboard.nextLine();
        serverOutput.write(myUsername);
        serverOutput.flush();
        serverReply=serverInput.readLine();

        if(serverReply.equals("8")){
          System.out.println("Waiting for other players...");
          serverReply=serverInput.readLine();
        }
        role= Integer.parseInt(serverReply);
        System.out.println("--------------------------------------------------------------------------------------");
        System.out.println("Your role is "+roleList[role]+".");
        System.out.println(descriptionList[role]);
        System.out.println("--------------------------------------------------------------------------------------");

        if(!isFirst){
          System.out.println("Waiting for "+opUsername+"\'s move...");
          serverReply=serverInput.readLine();//listening for status code
          serverReply=serverInput.readLine();//getting the board
          serverReply+="\n";
          serverReply+=serverInput.readLine();
          serverReply+="\n";
          serverReply+=serverInput.readLine();
          System.out.println("----------------------------");
          System.out.println(serverReply);
          System.out.println("----------------------------");
        }
        
        while(true){
          System.out.print("Please enter the row number you wished to change: ");
          userInput= keyboard.nextLine();
          serverOutput.write(userInput+"\n");
          serverOutput.flush();
          System.out.print("Please enter the number of coins to remove: ");
          userInput= keyboard.nextLine();
          serverOutput.write(userInput+"\n");
          serverOutput.flush();

          serverReply=serverInput.readLine();//listening for status code
          if(serverReply.equals("0")){
            serverReply=serverInput.readLine();//getting the board
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            System.out.println("----------------------------");
            System.out.println(serverReply);
            System.out.println("----------------------------");
          }
          else if(serverReply.equals("1")){
            serverReply=serverInput.readLine();//getting the board
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            System.out.println("----------------------------");
            System.out.println(serverReply);
            System.out.println("----------------------------");
            System.out.println("Congratulation! You won the game.");
            break;
          }
          else{
            serverReply=serverInput.readLine();//getting the board
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            System.out.println("----------------------------");
            System.out.println(serverReply);
            System.out.println("----------------------------");
            System.out.println("The move you made is invalid.");
            continue;
          }

          System.out.println("Waiting for "+opUsername+"\'s move...");
          serverReply=serverInput.readLine();//listening for status code
          if(serverReply.equals("0")){
            serverReply=serverInput.readLine();//getting the board
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            System.out.println("----------------------------");
            System.out.println(serverReply);
            System.out.println("----------------------------");
          }
          else{
            serverReply=serverInput.readLine();//getting the board
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            serverReply+="\n";
            serverReply+=serverInput.readLine();
            System.out.println("----------------------------");
            System.out.println(serverReply);
            System.out.println("----------------------------");
            System.out.println("Sorry, you lost the game.");
            break;
          }
        }
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
