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


public class Deck {
	private CardArray cards;
	private Card downcard; // the up above card of all shown cards
	private Card.Suit downsuit; // suit must be played
	// not always same with downcard.suit() because of A cards

	public CardArray getDeck() { return cards; }

	//------------------Constant definitions----------------------
	static final int NUM_SUITS = 4;
	static final int NUM_NUMS = 13;
	//static final int NUM_CARDS = NUM_SUITS * NUM_NUMS;

	// number of cards initially dealt to each player
	static final int NUM_STARTINGS_CARDS = 7;

	//--------------------Constructor-----------------------------
	public Deck() {
		cards = new CardArray();
		for (int s = 1; s <= NUM_SUITS; s++) {
			for (int n = 1; n <= NUM_NUMS; n++) {
				cards.add(new Card(n, s, valueOfCard(n)));
			}
		}
	}

	//-----------------Non-static methods-------------------------

	public Card downcard() { return downcard; }
	public Card.Suit downsuit() { return downsuit; }


	public void setDowncard(Card card) {
		downcard = card;
		downsuit = downcard.suit();
	}

	public void setDownsuit(Card.Suit suit) {
		downsuit = suit;
	}

	/**
	 * Shuffles the deck and displays a spin-effect on the screen.
	 *
	 * @return nothing
	 */
	public void shuffle() {
		Collections.shuffle(cards);
		doShuffleFancySpin();
	}

	/**
	 * Re-init deck by cloning a new instance of Deck, remove from it
	 * p1cards, p2cards and the downcard, and make a shuffle.
	 *
	 * @param p1cards player1 cards
	 * @param p2cards player2 cards
	 * @return nothing
	 */
	public void reshuffle(CardArray p1cards, CardArray p2cards) {
		Deck newdeck = new Deck();
		CardArray shouldRemoved = new CardArray();

		cards = (CardArray) newdeck.cards.clone();

		shouldRemoved.add(downcard);
		for (Card i : p1cards) {
			shouldRemoved.add(i);
		}
		for (Card i : p2cards) {
			shouldRemoved.add(i);
		}

		for (Card i : shouldRemoved) {
			for (Card y : cards) {
				if (i.num() == y.num() && i.suit() == y.suit()) {
					cards.remove(y);
					break;
				}
			}
	}
		shuffle();
	}

	/**
	 * Deal downcard and the starting number of cards to each player,
	 * one by one.
	 *
	 * @param player1 player to deal cards to
	 * @param player2 player to deal cards to
	 * @return nothing
	 */
	public void dealCards(Player player1, Player player2) {
		setDowncard(dealOneCard(player1, player2));
		player1.cards.clear();
		player2.cards.clear();
		for (int i = 0; i < NUM_STARTINGS_CARDS * Agonia.NUM_PLAYERS; i++) {
			Card card = cards.get(0);
			cards.remove(0);
			if (i % 2 == 0) {
				player1.cards.add(card);
			} else {
				player2.cards.add(card);
			}
		}
	}

	/**
	 * Remove the first card from the deck and return it.
	 *
	 * @return the dealed card or null if deck is empty
	 */
	public Card dealOneCard(Player player1, Player player2) {
		if (isEmpty())
			reshuffle(player1.cards, player2.cards);

		Card card = cards.get(0);
		cards.remove(0);

		return card;
	}

	/**
	 * Check if deck is empty.
	 *
	 * @return true if deck is empty, else false
	 */
	public boolean isEmpty() {
		return cards.isEmpty();
	}

	//--------------------Static methods--------------------------

	/**
	 * Display a spinning text-effect on the screen.
	 *
	 * @return nothing
	 */
	private static void doShuffleFancySpin() {
		System.out.println("Shuffling the deck...");
		for (int i = 0; i < 50000; i++) {
			System.out.print("|");
			System.out.print("\b");
			System.out.print("/");
			System.out.print("\b");
			System.out.print("-");
			System.out.print("\b");
			System.out.print("\\");
			System.out.print("\b");
		}
		System.out.println("|/|");
		System.out.println("done\n");
	}

	/**
	 * Determine the value of a card according to game rules.
	 *
	 * @param n num of a card
	 * @return card's worth in points
	 */
	private static int valueOfCard(int n) {
		switch(n) {
		case 11: case 12: case 13:
			return 10;
		case 1:
			return 25;
		default:
			return n;
		}
	}
}
