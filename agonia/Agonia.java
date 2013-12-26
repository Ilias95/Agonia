/*
 * Copyright (C) 2013 Ilias Stamatis <stamatis.iliass@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package agonia;

import java.util.Arrays;
import java.util.Scanner;
import java.io.IOException;


public class Agonia {
	static final int NUM_PLAYERS = 2;

	public static void main(String[] args) throws IOException {
		PlayerHuman playerHuman = new PlayerHuman();
		PlayerCPU playerCPU = new PlayerCPU();
		Player[] players = {playerHuman, playerCPU};
		int pointsLimit;
		int round = 1;

		clearScreen();
		System.out.println(getInfo());

		pointsLimit = getPointsLimit();
		whoIsFirst(playerHuman, playerCPU, players);

		do {
			System.out.println("*****************************" +
					   "ROUND " + round +
					   "*******************************\n");

			Deck deck = new Deck();
			int plCount = round - 1;              // player index
			int cp = plCount % NUM_PLAYERS;       // current player
			int np = (plCount + 1) % NUM_PLAYERS; // next player

			deck.shuffle();
			deck.dealCards(playerHuman, playerCPU);

			if (players[cp].playFirstCard(deck, players[np]))
				plCount++;

			do {
				cp = plCount % NUM_PLAYERS;
				np = (plCount + 1) % NUM_PLAYERS;

				players[cp].play(deck, players[np]);
				plCount++;
			} while (! players[cp].hasWon());

			int points = players[np].addPoints();
			if (players[np] instanceof PlayerHuman)
				System.out.println(points + " points to you.");
			else
				System.out.println(points + " points to CPU.");
			displayScore(playerHuman, playerCPU, pointsLimit);
			round++;
		} while (playerHuman.points < pointsLimit
			 && playerCPU.points < pointsLimit);

		if (playerHuman.points > playerCPU.points)
			System.out.println("CPU won the game!");
		else
			System.out.println("Congratulations! " +
			                   "You won the game!!!");
	}

	/**
	 * Prompt user to enter an integer and validate input.
	 *
	 * @return points limit, positive integer
	 */
	private static int getPointsLimit() {
		Scanner sc = new Scanner(System.in);
		int i;

		do {
			System.out.print("Set the points limit: ");
			try {
				i = Integer.parseInt(sc.nextLine());
			} catch (NumberFormatException e) {
				i = -1;
			}
		} while (i < 1);

		System.out.printf("Game will end at %d points.%n%n", i);
		return i;
	}

	/**
	 * Ask user if he wants to play second and if so, swap players in
	 * players array.
	 *
	 * The first index in the players array plays first.
	 * By default the program assumes that human player plays first.
	 *
	 * @param playerHuman human player
	 * @param playerCPU CPU player
	 * @param players an array of all players
	 * @return nothing
	 */
	private static void whoIsFirst(Player playerHuman,
		                      Player playerCPU,
		                      Player[] players) {
		Scanner sc = new Scanner(System.in);

		System.out.print("Would you like to play first? (y/n) ");
		String answer = sc.nextLine().toLowerCase().trim();
		if (answer.equals("y") || answer.equals("yes")) {
			System.out.println("Okay, you play first.\n");
		} else {
			players[0] = playerCPU;
			players[1] = playerHuman;
			System.out.println("Okay, I play first.\n");
		}
	}

	/**
	 * Sort cards by num and suit.
	 *
	 * First create 4 CardArrays each holding only same suit cards, and
	 * suitably distribute all cards to them depending on their suit.
	 * Then clear cards array and call sortSameSuitCards() 4 times - one
	 * for each cardArray - to sort same suit cards by num (value) and
	 * store them back to cards array properly sorted.
	 *
	 * @param cards cards to be sorted
	 * @return nothing
	 */
	public static void sortCards(CardArray cards) {
		CardArray DCards = new CardArray(); // diamond cards
		CardArray HCards = new CardArray(); // heart cards
		CardArray SCards = new CardArray(); // ...
		CardArray CCards = new CardArray();

		for (Card i : cards) {
			switch (i.suit()) {
			case DIAMONDS:
				DCards.add(i);
				break;
			case HEARTS:
				HCards.add(i);
				break;
			case SPADES:
				SCards.add(i);
				break;
			case CLUBS:
				CCards.add(i);
				break;
			}
		}

		cards.clear();
		sortSameSuitCards(cards, DCards);
		sortSameSuitCards(cards, HCards);
		sortSameSuitCards(cards, SCards);
		sortSameSuitCards(cards, CCards);
	}

	/**
	 * Sort cards of same suit by num.
	 *
	 * Create an array of ints and store to it the num of each card.
	 * Sort the int array using Arrays.sort() and then append cards to the
	 * cards array in right order by corresponding each num to its card.
	 *
	 * Note: nums would all be unique because we deal with same suit cards.
	 *
	 * @param cards original unsorted cards, result will be stored here
	 * @param suitCards cards to be sorted
	 * @return nothing
	 */
	private static void sortSameSuitCards(CardArray cards,
	                                     CardArray suitCards) {
		int[] cardsNums = new int[suitCards.size()];
		int y = 0; // index

		for (Card i : suitCards) {
			cardsNums[y] = i.num();
			y++;
		}
		Arrays.sort(cardsNums);

		for (int n : cardsNums) {
			for (Card i : suitCards) {
				if (i.num() == n)
					cards.add(i);
			}
		}
	}

	/**
	 * Search for a card in a cards array by description.
	 *
	 * @param cardName card description
	 * @param cards the cards array
	 * @return the card if found, else null
	 */
	public static Card findCard(String cardName, CardArray cards) {
		for (Card i : cards) {
			if (i.shortdesc().toLowerCase().equals(cardName)
			|| i.longdesc().toLowerCase().equals(cardName))
				return i;
		}
		return null;
	}

	/**
	 * Return the suit that appears most times in an array of cards.
	 *
	 * @param cards the card array to proccess
	 * @return the dominant suit
	 */
	public static Card.Suit findDominantSuit(CardArray cards) {
		int diamonds = 0, hearts = 0, spades = 0, clubs = 0;

		for (Card i : cards) {
			switch(i.suit()) {
			case DIAMONDS:
				diamonds++;
				break;
			case HEARTS:
				hearts++;
				break;
			case SPADES:
				spades++;
				break;
			case CLUBS:
				clubs++;
				break;
			}
		}

		int max = diamonds;
		int[] results = new int[] {hearts, spades, clubs};

		for (int i : results)
			if (i > max)
				max = i;

		if (max == diamonds)
			return Card.Suit.DIAMONDS;
		if (max == hearts)
			return Card.Suit.HEARTS;
		if (max == spades)
			return Card.Suit.SPADES;
		return Card.Suit.CLUBS;
	}

	/**
	 * Display the shortdesc of all cards on the screen, formated.
	 *
	 * @param cards cards to be displayed
	 * @return nothing
	 */
	public static void displayCards(CardArray cards) {
		if (cards.isEmpty()) {
			System.out.println("[]");
			return;
		}

		sortCards(cards);
		System.out.print("[");
		for (Card i : cards)
			System.out.printf("'%s', ", i.shortdesc());
		System.out.print("\b\b]");
	}

	/**
	 * Display the current status of the table on the screen.
	 * It displays human's cards, cpu's number of cards and the down card.
	 *
	 * @param playerHuman human player
	 * @param playerCPU CPU player
	 * @param downcard the shown card
	 * @return nothing
	 */
	public static void displayTable(Player playerHuman,
	                                Player playerCPU,
	                                Card downcard) {
		System.out.printf("CPU: %d more cards%n", playerCPU.cards.size());
		System.out.print("Your cards: ");
		displayCards(playerHuman.cards);
		System.out.printf("%nDown card: %s%n%n", downcard.shortdesc());
	}

	/**
	 * Display score of each player and points limit.
	 *
	 * @return nothing
	 */
	private static void displayScore(PlayerHuman playerHuman,
		                         PlayerCPU playerCPU,
		                         int limit) {
		System.out.printf("%n+---------------------%n" +
		                  "|Score (limit: %d)    %n" +
		                  "|---------------------%n" +
		                  "|Human:  %d           %n" +
		                  "|CPU:    %d           %n" +
		                  "+---------------------%n%n",
		                  limit, playerHuman.points, playerCPU.points);
	}

	private static void clearScreen() throws IOException {
		String os = System.getProperty("os.name");

		if (os.contains("Windows"))
		    Runtime.getRuntime().exec("cls");
		else if (os.contains("Linux"))
		    Runtime.getRuntime().exec("clear");
	}

	private static String getInfo() {
		return "\n" +
		  "\t+-----------------------------------------------------------+\n" +
		  "\t|                         Agonia                            |\n" +
		  "\t|-----------------------------------------------------------|\n" +
		  "\t| H: Hearts  D: Diamonds  C: Clubs  S: Spades               |\n" +
		  "\t| Special cards:                                            |\n" +
		  "\t|   -A : Set suit                                           |\n" +
		  "\t|   -8 : Opponent looses his turn                           |\n" +
		  "\t|   -7 : Opponent must pull 2 cards (or 4 or 6 or 8 if      |\n" +
		  "\t|        7 on 7)                                            |\n" +
		  "\t| Points:                                                   |\n" +
		  "\t|    10 points for each J, Q, K                             |\n" +
		  "\t|    25 points for each A                                   |\n" +
		  "\t|    rest cards worth their number in points                |\n" +
		  "\t|-----------------------------------------------------------|\n" +
		  "\t| Play cards by typing their name:                          |\n" +
		  "\t|    Eg. \"KS\" or \"King of Spades\"                           |\n" +
		  "\t| Type 'P' to pull a card and 'F' to fold.                  |\n" +
		  "\t|-----------------------------------------------------------|\n" +
		  "\t| First player to get rid of all of his cards wins a round. |\n" +
		  "\t| The player that reaches the points limit first looses.    |\n" +
		  "\t+-----------------------------------------------------------+\n";
	}
}
