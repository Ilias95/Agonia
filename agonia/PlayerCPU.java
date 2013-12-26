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

import java.util.Collections;


public class PlayerCPU extends Player {
	// just a wrapper to the real function
	public void play(Deck deck, Player playerHuman) {
		play(deck, (PlayerHuman) playerHuman);
	}

	/**
	 * Get cpu's choise and make the appropriate manipulations depending
	 * on it.
	 *
	 * @param deck deck of cards
	 * @param playerHuman human player
	 * @return nothing
	 */
	public void play(Deck deck, PlayerHuman playerHuman) {
		while (true) {
			Card card = chooseCard(deck, playerHuman);
			if (card == null) {
				System.out.println("CPU folds.");
				break;
			}

			cards.remove(card);
			System.out.print("CPU played: ");
			System.out.println(card.shortdesc());
			Card.Suit prevSuit = deck.downsuit();
			deck.setDowncard(card);

			switch (card.num()) {
			case 1:
				deck.setDownsuit(chooseSuit(
				                 deck, prevSuit, playerHuman));
				System.out.printf("CPU set %s suit.%n",
				                  deck.downsuit());
				break;
			case 7:
				int n = doSevenLoop(deck, playerHuman, this, 1);
				if ((n == 4 || n == 8) && ! playerHuman.hasWon()) {
					// human played last
					System.out.println();
					continue;
				}
				break;
			case 8:
				System.out.println("CPU played an 8. " +
				                   "You loose your turn.\n");
				continue;
			}
			break;
		}
		printEndTurn();
	}

	// just a wrapper to the real function
	public boolean playFirstCard(Deck deck, Player playerHuman) {
		return playFirstCard(deck, (PlayerHuman) playerHuman);
	}

	/**
	 * Make the appropriate manipulations if first card is special.
	 *
	 * @param deck deck of cards
	 * @param playerHuman human player
	 * @return true if player looses his turn, else false
	 */
	public boolean playFirstCard(Deck deck, PlayerHuman playerHuman) {
		switch(deck.downcard().num()) {
		case 1:
			System.out.printf("First card is %s.%n",
			                   deck.downcard().shortdesc());
			deck.setDownsuit(chooseSuit(deck, null, playerHuman));
			System.out.printf("CPU set %s suit.%n", deck.downsuit());
			break;
		case 7:
			System.out.printf("First card is %s.%n",
			                   deck.downcard().shortdesc());
			int n = doSevenLoop(deck, playerHuman, this, 0);
			if (n == 4 || n == 8) {// cpu played last
				printEndTurn();
				return true;
			}
			break;
		case 8:
			System.out.printf("First card is %s. " +
			                  "CPU looses its turn.%n%n",
			                  deck.downcard().shortdesc());
			return true;
		}
		return false;
	}

	/**
	 * Decide which card is the most suitable to play.
	 *
	 * If human played an Ace recently and has only one card, cpu must
	 * prevent him from winning the game. In that case play a 7 card if
	 * possible (to force him pull another 2 cards) else an Ace card
	 * (to change the current suit), if any. If CPU cannot do anything to
	 * change this situation, play as normal.
	 *
	 * In normal case CPU finds all non-Ace cards that can be played on
	 * the round, then finds the dominant suit of these cards and plays the
	 * higher (in points value) card of that suit. Ace cards are played
	 * only if there are no other cards available.
	 *
	 * If there are no available cards at all, CPU pulls a card and play
	 * it if possible.
	 *
	 * @param deck deck of cards
	 * @param playerHuman human player
	 * @return the card that CPU decided to play,
	 *	   or null if CPU cannot play a card
	 */
	public Card chooseCard(Deck deck, PlayerHuman playerHuman) {
		CardArray playableCards = new CardArray();
		CardArray aceCards = new CardArray();

		Agonia.sortCards(cards);

		for (Card i : cards) {
			if (i.num() == 1)
				aceCards.add(i);
			else if (i.num() == deck.downcard().num()
			|| i.suit() == deck.downsuit())
				playableCards.add(i);
		}

		/* Reverse cards in order cards with higher points value
		   to be played first. */
		Collections.reverse(playableCards);

		if (! playableCards.isEmpty()) {
			/* Check if human played an Ace recently.
			   If so, try to prevent him from winning the game
			   by playing a Seven or an Ace. */
			for (Card i : playerHuman.lastCards) {
				if (playerHuman.cards.size() == 1
				&& i != null && i.num() == 1) {
					for (Card x : playableCards) {
						if (x.num() == 7
						&& x.suit() == deck.downsuit())
							return x;
					}
					if (! aceCards.isEmpty())
						return aceCards.get(0);
				}
			}
			Card.Suit mSuit = Agonia.findDominantSuit(playableCards);
			for (Card i : playableCards)
				if (i.suit() == mSuit)
					return i;
		}
		if (aceCards.isEmpty()) {
			Card card = deck.dealOneCard(this, playerHuman);
			cards.add(card);
			System.out.println("CPU pulled a card.");
			if (card.num() == 1
			|| card.suit() == deck.downsuit()
			|| card.num() == deck.downcard().num())
				return card;
			return null;
		}
		return aceCards.get(0);
	}

	/**
	 * Decide which suit is the most suitable to set.
	 *
	 * @param deck the deck
	 * @param prevSuit suit of the last but one card played
	 * @param playerhuman human player
	 * @return suit decided to set
	 */
	public Card.Suit chooseSuit(Deck deck,
		                    Card.Suit prevSuit,
		                    PlayerHuman playerHuman) {
		boolean shouldAvoid = false;
		for (Card i : playerHuman.lastCards) {
			if (playerHuman.cards.size() == 1
			&& i != null && i.num() == 1)
				shouldAvoid = true;
		}

		if (! shouldAvoid)
			return Agonia.findDominantSuit(cards);

		CardArray tempCards = new CardArray();
		for (Card i : cards)
			if (! (i.suit() == prevSuit))
				tempCards.add(i);

		return Agonia.findDominantSuit(tempCards);
	}

	/**
	 * Check if cpu can play a 7 card.
	 *
	 * @return the 7 card that CPU decided to play,
	 *	   or null if CPU cannot play a card
	 */
	public Card chooseSeven() {
		CardArray sevenCards = new CardArray();
		for (Card i : cards)
			if (i.num() == 7)
				sevenCards.add(i);
		if (sevenCards.isEmpty())
			return null;

		Card.Suit mSuit = Agonia.findDominantSuit(sevenCards);
		for (Card i : sevenCards)
			if (i.suit() == mSuit) {
				cards.remove(i);
				System.out.println("\nCPU played: " +
				                   i.shortdesc());
				return i;
			}
		return null;
	}

	public void printEndTurn() {
		System.out.println("---------------------------------" +
		                   "-------------------------------");
	}

	/**
	 * Check if player has any cards left and display a message if so.
	 *
	 * @return true if player has no cards, else false
	 */
	public boolean hasWon() {
		if (super.hasWon()) {
			System.out.println("CPU won the round!");
			return true;
		}
		return false;
	}
}
