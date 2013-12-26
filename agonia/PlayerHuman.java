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

import java.util.Scanner;


public class PlayerHuman extends Player {
	Card[] lastCards; // last two cards played

	public PlayerHuman() {
		super();
		lastCards = new Card[] {null, null};
	}

	/**
	 * Append card to lastCards and remove the first card.
	 *
	 * @param card card to add
	 * @return nothing
	 */
	public void addToLastCards(Card card) {
		lastCards[0] = lastCards[1];
		lastCards[1] = card;
	}


	// just a wrapper to the real function
	public void play(Deck deck, Player playerCPU) {
		play(deck, (PlayerCPU) playerCPU);
	}

	/**
	 * Get user's choise and make the appropriate manipulations depending
	 * on it.
	 *
	 * @param deck deck of cards
	 * @param playerCPU CPU player
	 * @return nothing
	 */
	public void play(Deck deck, PlayerCPU playerCPU) {
		while (true) {
			Agonia.displayTable(this, playerCPU, deck.downcard());
			Card card = chooseCard(deck, playerCPU);
			if (card == null)
				break;

			cards.remove(card);
			addToLastCards(card);
			deck.setDowncard(card);

			switch (card.num()) {
			case 1:
				deck.setDownsuit(chooseSuit());
				System.out.printf("You chose %s.%n",
				                  deck.downsuit());
				break;
			case 7:
				int n = doSevenLoop(deck, this, playerCPU, 0);
				if ((n == 4 || n == 8) && ! playerCPU.hasWon()) {
					// cpu played last
					playerCPU.printEndTurn();
					continue;
				}
				break;
			case 8:
				System.out.println("You played an 8. " +
				                   "Play again.\n");
				continue;
			}
			break;
		}
		System.out.println();
	}

	// just a wrapper to the real function
	public boolean playFirstCard(Deck deck, Player playerCPU) {
		return playFirstCard(deck, (PlayerCPU) playerCPU);
	}

	/**
	 * Make the appropriate manipulations if first card is special.
	 *
	 * @param deck deck of cards
	 * @param playerCPU CPU player
	 * @return true if player looses his turn, else false
	 */
	public boolean playFirstCard(Deck deck, PlayerCPU playerCPU) {
		switch(deck.downcard().num()) {
		case 1:
			System.out.printf("First card is %s.%n",
			                  deck.downcard().shortdesc());
			System.out.printf("Your cards: ");
			Agonia.displayCards(cards);
			System.out.println();
			deck.setDownsuit(chooseSuit());
			System.out.printf("You chose %s.%n%n", deck.downsuit());
			break;
		case 7:
			System.out.printf("First card is %s.%n",
			                  deck.downcard().shortdesc());
			int n = doSevenLoop(deck, this, playerCPU, 1);
			System.out.println();
			if (n == 4 || n == 8) // human played last
				return true;
			break;
		case 8:
			System.out.printf("First card is %s. " +
			                  "You loose your turn.%n%n",
			                  deck.downcard().shortdesc());
			return true;
		}
		return false;
	}

	/**
	 * Prompt user to select a card to play.
	 *
	 * Check if input is a valid card and if so check if the card can be
	 * played in the current state of the game. Human can pull a card from
	 * the deck (only once on each round) by entering "p" or he can fold
	 * by entering "f" by assuming he already pulled a card earlier.
	 *
	 * @param deck deck of cards
	 * @param playerCPU cpu player
	 * @return the card played or null if player folds
	 */
	public Card chooseCard(Deck deck, PlayerCPU playerCPU) {
		String input;
		Scanner sc = new Scanner(System.in);
		Card card;
		boolean pull = true; // if true, user can pull a card

		while (true) {
			System.out.print("Choose a card: ");
			input = sc.nextLine().toLowerCase().trim().
			                replaceAll("\\s+", " ");
			if (input.equals("p")) { // pull card
				if (pull) {
					card = deck.dealOneCard(this, playerCPU);
					cards.add(card);
					System.out.print("You pulled: ");
					System.out.println(card.shortdesc());
					pull = false;
				} else {
					System.out.println(
					         "You already pulled a card.");
				}
			} else if (input.equals("f")) { // fold
				if (!pull) {
					return null;
				} else {
					System.out.println("You can't fold. " +
					      "You should pull a card first.");
				}

			} else { // play a card
				card = Agonia.findCard(input, cards);
				if (card == null) {
					System.out.println("You have not " +
					                   "such a card.");
				} else if (card.num() != 1
				&& card.num() != deck.downcard().num()
				&& card.suit() != deck.downsuit()) {
					System.out.println("You can't play " +
					                   "this card.");
				} else {
					return card;
				}
			}
		}
	}

	/**
	 * Prompt user to select a suit.
	 *
	 * @return suit to set
	 */
	public Card.Suit chooseSuit() {
		Scanner sc = new Scanner(System.in);
		String input;

		System.out.print("Choose the suit you want to set (D, H, C, S): ");
		while (true) {
			input = sc.nextLine().toUpperCase().trim();
			Card.Suit suit = Card.charToSuit(input.charAt(0));
			if (suit != Card.Suit.INVALID && input.length() == 1)
				return suit;
			System.out.print("Not such suit. Try again: ");
		}
	}

	/**
	 * Prompt user to select a 7 card to play, if any.
	 *
	 * @return null if player doesn't want or cannot play a 7 card,
	 *         else the card played
	 */
	public Card chooseSeven() {
		CardArray sevenCards = new CardArray();
		for (Card i : cards)
			if (i.num() == 7)
				sevenCards.add(i);
		if (sevenCards.isEmpty())
			return null;

		Scanner sc = new Scanner(System.in);
		String input;
		while (true) {
			System.out.print("CPU played a 7. " +
			                 "Will you play one too? (y/n) ");
			input = sc.nextLine().toLowerCase().trim();
			if (input.equals("y") || input.equals("yes"))
				break;
			else if (input.equals("n") || input.equals("no"))
				return null;
		}

		Card card;

		System.out.print("Choose a card ");
		Agonia.displayCards(sevenCards);
		System.out.print(": ");
		while (true) {
			input = sc.nextLine().toLowerCase().trim()
					.replaceAll("\\s+", " ");
			card = Agonia.findCard(input, cards);
			if (card == null) {
				System.out.print("You have not such a card.");
			} else if (card.num() != 7) {
				System.out.println("You can't play this card. " +
				                   "You should play a 7.");
			} else {
				cards.remove(card);
				addToLastCards(card);
				return card;
			}
		}
	}

	/**
	 * Check if player has any cards left and display a message if so.
	 *
	 * @return true if player has no cards, else false
	 */
	public boolean hasWon() {
		if (super.hasWon()) {
			System.out.println("You won the round!");
			return true;
		}
		return false;
	}
}
