# SalemWitchTrial
Class Project
* Roles
	* Hein Thu: SWTClient.java, SWTGame.java, SWTServer.java, ClientHandler.java, ClientListener.java, Player.java, PlayerSync.java, Control.java
	* Shereef Khafagi: SWTServer.java, ClientHandler.java
* Game Description
	* The game will have 9 total players each having one of these roles:
		* 2 Civilians (Innocents): can vote to lynch people each round.
		* Doctor (Innocents): heals one person (including himself) each night preventing them from dying.
		* Sheriff (Innocents): checks one person each night to investigate for suspicious activity and can find out if the target is a member of the Mafia, except for the Godfather and Vigilante.
		* Lookout (Innocents): watches one person each night to see who visits them.
		* Vigilante (Innocents): chooses someone each night to kill to save the town.
		* Framer (Mafia): chooses someone to frame at night. If the target is investigated, they will appear to be a member of the Mafia. If Mafioso dies, Framer will take their position.
		* Mafiaso (Mafia): carries out Godfather’s orders. If Godfather dies, Mafioso will take their position. 
		* Godfather (Mafia): kills someone each night if the Mafioso is dead. Else, the Mafioso will kill the target.
	* The game ends when all Mafia members dies or when the number of Mafia becomes equal to the number of Innocents
	* The game will repeat the rounds until the ending condition is met.
	* Each round contains multiple stages:
		* Day Talk
			* Two minutes long
			* Every player alive can talk to each other on a chat server
		* Nomination
			* Each player can nominate a person to lynch or choose to abstain
		* Night Talk
			* One minute long
			* All Mafia members can discuss their plan
		* Night Action
			* Players with roles will decide their actions
		* Explanation
			* The server will take in all the actions and calculate the outcome
			* Then, the server will tell everyone who died and the cause of death
