
package com.rtm.frm.vmap;



public class Coord{
	public final static int LENGTH = 8;
	
	public int mX;
	public int mY;

	public static int compare(Coord c1, Coord c2) {
		return c1.compareTo(c2);
	}

	public Coord() {
		this(0, 0);
	}

	public Coord(int x, int y) {
		this.mX = x;
		this.mY = y;
	}

	public Coord(Coord c) {
		this(c.mX, c.mY);
	}

	public boolean equals(Coord other) {
		if (mX != other.mX) {
			return false;
		}

		if (mY != other.mY) {
			return false;
		}

		return true;
	}

	public int compareTo(Coord other) {
		if (mX < other.mX)
			return -1;
		if (mX > other.mX)
			return 1;
		if (mY < other.mY)
			return -1;
		if (mY > other.mY)
			return 1;
		return 0;
	}

	public float distance(Coord c) {
		float dx = mX - c.mX;
		float dy = mY - c.mY;

		return (float)Math.sqrt(dx * dx + dy * dy);
	}

	

	public class Double{
		double mX;
		double mY;
	}
}
