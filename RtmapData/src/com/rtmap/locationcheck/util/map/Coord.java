package com.rtmap.locationcheck.util.map;

/**
 * 点的xy坐标标记
 * @author zhengnengyuan
 *
 */
public class Coord implements Serializable {
	public final static int length = 8;
	
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
	
	/**
	 * 标记点是否有效：即xy轴坐标不能为负数，超出地图界限
	 * @return
	 */
	public boolean isValid() {
		return (mX > 0 && mY > 0);
	}

	public boolean equalsValue(Coord other) {
	    if (this == other) {
            return true;
        }
	    
		if (mX != other.mX) {
			return false;
		}

		if (mY != other.mY) {
			return false;
		}

		return true;
	}

	
	
	@Override
    public boolean equals(Object o) {
	    if (o instanceof Coord) {
            Coord c = (Coord) o;
            if (c.mX == mX && c.mY == mY) {
                return true;
            }
        }
        return false;
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

	public float distance(Coord c, float mapScale) {
		float dx = (mX - c.mX) * mapScale;
		float dy = (mY - c.mY) * mapScale;

		return (float)Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public int deserialize(byte[] b, int from) {
		if(b == null)
			return 0;
		if(b.length < from + length)
			return 0;

		int nPos = from;
		mX = SerializeTool.getInt(b, nPos);
		nPos += 4;
		mY = SerializeTool.getInt(b, nPos);
		return length;
	}

	@Override
	public int getByteSize() {
		return length;
	}

	public class Double{
		double mX;
		double mY;
	}
}
