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


public class Card {
	private int num;          // 1 to 13, eg. 4 is the num of '4S'
	private Suit suit;
	private String shortdesc; // short description eg. 'JS'
	private String longdesc;  // long description eg. 'Jack of Spades'
	private int value;        // card's worth in points, according to rules

	public enum Suit {
		INVALID   (0, "-"),
		DIAMONDS  (1, "Diamonds"),
		HEARTS    (2, "Hearts"),
		SPADES    (3, "Spades"),
		CLUBS     (4, "Clubs");

		private final int id;
		private final String str;

		Suit(int id, String str) {
			this.id = id;
			this.str = str;
		}

		public int id() { return id; }
		public String str() { return str; }
	}

	//--------------------Constructors----------------------------

	public Card(int num, Suit suit, int value) {
		this.num = num;
		this.suit = suit;
		this.value = value;
		this.shortdesc = this.numIntToStr(num) + suit.str().charAt(0);

		if (num == 1)
			longdesc = "Ace";
		else if (num < 11)
			longdesc = String.valueOf(num);
		else
			longdesc = (new String[]
		                    {"Jack", "Queen", "King"})[num - 11];

		longdesc += " of ";
		longdesc += suit.str();

	}
	public Card(int num, int suit, int value) {
		this(num, Card.intToSuit(suit), value);
	}

	public Card(char num, char suit, int value) {
		this(Card.numCharToInt(num), Card.charToSuit(suit), value);
	}

	public Card(char num, Suit suit, int value) {
		this(Card.numCharToInt(num), suit, value);
	}

	//-----------------Non-static methods-------------------------

	public int num() { return num; }
	public Suit suit() { return suit; }
	public String shortdesc() { return shortdesc; }
	public String longdesc() { return longdesc; }
	public int value() { return value; }

	public boolean isValid() {
		if (suit != Suit.INVALID && num >= 1 && num <= 13)
			return true;
		return false;
	}

	//--------------------Static methods--------------------------

	public static Suit charToSuit(char c) {
		for (Suit i : Suit.values())
			if (i.str.charAt(0) == c)
				return i;
		return Suit.INVALID;
	}

	private static Suit intToSuit(int n) {
		for (Suit i : Suit.values())
			if (i.id() == n)
				return i;
		return Suit.INVALID;
	}

	private static String numIntToStr(int n) {
		if (n == 1)
			return "A";
		if (n < 10)
			return String.valueOf(n);
		return (new String[] {"T", "J", "Q", "K"})[n - 10];
	}

	private static int numCharToInt(char c) {
		switch(c) {
		case 'T':
			return 10;
		case 'J':
			return 11;
		case 'Q':
			return 12;
		case 'K':
			return 13;
		case 'A':
			return 1;
		default:
			return c - Integer.valueOf('0'); // eg. '4' to 4
		}
	}
}
