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


public class Player {
	CardArray cards;
	int points;

	public Player() {
		cards = new CardArray();
		points = 0;
	}

	public void play(Deck deck, Player player) {}
	public boolean playFirstCard(Deck deck, Player player) { return false; }

	/**
	 * Check whether human or cpu can and want to play a 7 on 7.
	 *
	 * When a player plays a 7 card his opponent must pull 2 cards. If the
	 * opponent has a 7 card too he can play it and player will have to
	 * pull 4 cards. This can be continued as long as a player has no other
	 * 7 cards. For each 7 card played the opponent have to pull 2 more
	 * cards than the first player had to pull.
	 *
	 * @param deck deck of cards
	 * @param playerHuman human player
	 * @param playerCPU CPU player
	 * @param plCount int index, should be 0 when called from a PlayerHuman,
	 *                1 when called from a PlayerCPU
	 * @return the number of cards pulled by a player
	 */
	public static int doSevenLoop(Deck deck,
	                              PlayerHuman playerHuman,
	                              PlayerCPU playerCPU,
	                              int plCount) {
		Card card;
		int n = 2; // number of cards will be pulled
		int cp; // current player

		while (true) {
			cp = plCount % 2;
			if (cp == 0)
				card = playerCPU.chooseSeven();
			else
				card = playerHuman.chooseSeven();

			if (card == null) {
				break;
			} else {
				deck.setDowncard(card);
				n += 2;
			}
			plCount++;
		}
		cp = (plCount + 1) % 2;

		if (cp == 0) {
			System.out.printf("CPU played a 7. You pull %d cards.%n", n);
		} else {
			System.out.print("\nYou played a 7. ");
			System.out.printf("CPU will pull %d cards.%n", n);
		}

		for (int i = 0; i < n; i++) {
			if (cp == 0)
				playerHuman.cards.add(deck.dealOneCard(
				                playerHuman, playerCPU));
			else
				playerCPU.cards.add(deck.dealOneCard(
				                playerHuman, playerCPU));
		}

		return n;
	}

	/**
	 * Charge player with the sum of the points of all his cards.
	 *
	 * @return points added
	 */
	public int addPoints() {
		int points = 0;
		for (Card i : cards)
			points += i.value();

		this.points += points;
		return points;
	}

	/**
	 * Check if player has any cards left.
	 *
	 * @return true if player has no cards, else false
	 */
	public boolean hasWon() {
		if (cards.isEmpty())
			return true;
		return false;
	}
}
