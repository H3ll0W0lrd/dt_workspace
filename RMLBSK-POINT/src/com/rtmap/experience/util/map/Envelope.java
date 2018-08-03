/**
 * 项目名称:智能泊车系统(Smart Parker）
 * 创建时间:2011.11.1
 * 版本:1.0.0
 * Copyright (c),RTM
 */
package com.rtmap.experience.util.map;

/**
 *********************************************************
 * @文件:RTMEnvelope.java
 * @说明: 
 * @创建日期:2011-12-5
 * @作者:林巍凌
 * @版权:RTM
 * @版本:1.0.0
 * @标签:RTMEnvelope
 * @最后更新时间:2011-12-5
 * @最后更新作者:林巍凌
 *********************************************************
 */
public class Envelope implements Serializable {
	public final static int length = 16;
	public int _minx;
	public int _miny;
	public int _maxx;
	public int _maxy;

	public static boolean intersects(Coord p1, Coord p2, Coord q) {
		if(p1 == null || p2 == null || q == null)
			return false;
		if (((q.mX >= (p1.mX < p2.mX ? p1.mX : p2.mX)) && (q.mX <= (p1.mX > p2.mX ? p1.mX
				: p2.mX)))
				&& ((q.mY >= (p1.mY < p2.mY ? p1.mY : p2.mY)) && (q.mY <= (p1.mY > p2.mY ? p1.mY
						: p2.mY)))) {
			return true;
		}
		return false;
	}

	public static boolean intersects(Coord p1, Coord p2, Coord q1,
			Coord q2) {
		if(p1 == null || p2 == null || q1 == null || q2 == null)
			return false;
		int minq = Math.min(q1.mX, q2.mX);
		int maxq = Math.max(q1.mX, q2.mX);
		int minp = Math.min(p1.mX, p2.mX);
		int maxp = Math.max(p1.mX, p2.mX);

		if (minp > maxq)
			return false;
		if (maxp < minq)
			return false;

		minq = Math.min(q1.mY, q2.mY);
		maxq = Math.max(q1.mY, q2.mY);
		minp = Math.min(p1.mY, p2.mY);
		maxp = Math.max(p1.mY, p2.mY);

		if (minp > maxq)
			return false;
		if (maxp < minq)
			return false;
		return true;
	}

	public Envelope() {
		init();
	}

	public Envelope(int x1, int y1, int x2, int y2) {
		init(x1, y1, x2, y2);
	}

	public Envelope(Coord c1, Coord c2) {
		init(c1, c2);
	}

	public Envelope(Coord c) {
		init(c);
	}

	public Envelope(Envelope env) {
		init(env);
	}

	public void init() {
		setToNull();
	}

	public void init(int x1, int y1, int x2, int y2) {
		if (x1 < x2) {
			_minx = x1;
			_maxx = x2;
		} else {
			_minx = x2;
			_maxx = x1;
		}
		if (y1 < y2) {
			_miny = y1;
			_maxy = y2;
		} else {
			_miny = y2;
			_maxy = y1;
		}
	}

	public void init(Coord p1, Coord p2) {
		if(p1 != null && p2 != null){
			init(p1.mX, p1.mY, p2.mX, p2.mY);
		}
	}

	public void init(Coord p) {
		if(p != null){
			init(p.mX, p.mY, p.mX, p.mY);
		}
	}

	public void init(Envelope env) {
		if(env != null){
			this._minx = env._minx;
			this._maxx = env._maxx;
			this._miny = env._miny;
			this._maxy = env._maxy;
		}
	}

	public void setToNull() {
		_minx = 0;
		_maxx = -1;
		_miny = 0;
		_maxy = -1;
	}

	public boolean isNull() {
		if(_maxx == 0 && _maxy == 0 && _minx == 0 && _miny == 0)
			return true;
		return _maxx < _minx || _maxy < _miny;
	}

	public int getWidth() {
		return _maxx - _minx;
	}

	public int getHeight() {
		return _maxy - _miny;
	}
	
	public void setHeight(int h){
		int cy = centre().mY;
        _miny = cy - h/2;
        _maxy = cy + h/2;
	}
	
	public void setWidth(int w){
		int cx = centre().mX;
        _minx = cx - w/2;
        _maxx = cx + w/2;
	}
	
	public void inflate(int dx, int dy){
		_minx -= dx;
		_miny -= dy;
		_maxx += dx;
		_maxy += dy;
	}
	
	public void inflate(int l, int t, int r, int b){
		_minx -= l;
		_miny -= t;
		_maxx += r;
		_maxy += b;
	}
	
	public void deflate(int dx, int dy){
		// deflate can't be more than width or height
		if(dx > getWidth() || dy > getHeight())
			return;
		_minx += dx;
		_miny += dy;
		_maxx -= dx;
		_maxy -= dy;
	}
	
	public void inflateByRatio(int r){
		int cx = centre().mX;
		int cy = centre().mY;
		int nw = getWidth() * r;
		int nh = getHeight() * r;
		_minx = cx - nw;
		_miny = cy - nh;
		_maxx = cx + nw;
		_maxy = cy + nh;
	}
	
	public void expandToInclude(Coord p) {
		if(p != null){
			expandToInclude(p.mX, p.mY);
		}
	}

	public void expandBy(double distance) {
		expandBy(distance, distance);
	}

	public void expandBy(double deltaX, double deltaY) {
		if (isNull())
			return;

		_minx -= deltaX;
		_maxx += deltaX;
		_miny -= deltaY;
		_maxy += deltaY;

		// check for envelope disappearing
		if (_minx > _maxx || _miny > _maxy)
			setToNull();
	}

	public void expandToInclude(int x, int y) {
		if (isNull()) {
			_minx = x;
			_maxx = x;
			_miny = y;
			_maxy = y;
		} else {
			if (x < _minx) {
				_minx = x;
			}
			if (x > _maxx) {
				_maxx = x;
			}
			if (y < _miny) {
				_miny = y;
			}
			if (y > _maxy) {
				_maxy = y;
			}
		}
	}

	public void expandToInclude(int minx, int miny, int maxx, int maxy){
		if (isNull()) {
			_minx = minx;
			_maxx = maxx;
			_miny = miny;
			_maxy = maxy;
		} else {
			if (minx < _minx) {
				_minx = minx;
			}
			if (maxx > _maxx) {
				_maxx = maxx;
			}
			if (miny < _miny) {
				_miny = miny;
			}
			if (maxy > _maxy) {
				_maxy = maxy;
			}
		}
	}
	
	public void expandToInclude(Envelope other) {
		if (other == null || other.isNull()) {
			return;
		}
		expandToInclude(other._minx, other._miny, other._maxx, other._maxy);
	}

	public void translate(int transX, int transY) {
		if (isNull()) {
			return;
		}
		init(_minx + transX, _miny + transY, _maxx + transX, _maxy + transY);
	}

	public Coord centre() {
		if (isNull())
			return null;
		return new Coord((_minx + _maxx) / 2, (_miny + _maxy) / 2);
	}
	
	public void setCentre(int x, int y){
		if (isNull()){
			_minx = _maxx = x;
			_miny = _maxy = y;
		}else{
			int w = _maxx - _minx;
			int h = _maxy - _miny;
			_minx = x - w/2;
			_miny = y - h/2;
			_maxx = x + w/2;
			_maxy = y + h/2;
		}
	}

	public Envelope intersection(Envelope env) {
		if (env == null || isNull() || env.isNull() || !intersects(env))
			return new Envelope();

		int intMinX = _minx > env._minx ? _minx : env._minx;
		int intMinY = _miny > env._miny ? _miny : env._miny;
		int intMaxX = _maxx < env._maxx ? _maxx : env._maxx;
		int intMaxY = _maxy < env._maxy ? _maxy : env._maxy;
		return new Envelope(intMinX, intMaxX, intMinY, intMaxY);
	}

	/**
	 * Returns <code>true</code> if the given point lies in or on the envelope.
	 * 
	 * @param p
	 *            the point which this <code>RTEnvelope</code> is being checked
	 *            for containing
	 * @return <code>true</code> if the point lies in the interior or on the
	 *         boundary of this <code>RTEnvelope</code>.
	 */
	public boolean contains(Coord p) {
		if(p == null)
			return false;
		return contains(p.mX, p.mY);
	}

	public boolean contains(int x, int y) {
		return x >= _minx && x <= _maxx && y >= _miny && y <= _maxy;
	}

	public boolean intersects(Envelope other) {
		if (other == null || isNull() || other.isNull()) {
			return false;
		}
		return intersects(other._minx, other._miny, other._maxx, other._maxy);
		/*
		return !(other._minx > _maxx || other._maxx < _minx
				|| other._miny > _maxy || other._maxy < _miny);*/
	}
	
	public boolean intersects(int minx, int miny, int maxx, int maxy){
		if (isNull()) {
			return false;
		}
		return !(minx > _maxx || maxx < _minx
				|| miny > _maxy || maxy < _miny);
	}

	public boolean intersects(Coord p) {
		if(p == null)
			return false;
		return intersects(p.mX, p.mY);
	}

	public boolean intersects(int x, int y) {
		return !(x > _maxx || x < _minx || y > _maxy || y < _miny);
	}

	public boolean contains(Envelope other) {
		if (other == null || isNull() || other.isNull()) {
			return false;
		}
		return other._minx >= _minx && other._maxx <= _maxx
				&& other._miny >= _miny && other._maxy <= _maxy;
	}

	public int distance(Envelope env) {
		if (env == null || intersects(env))
			return 0;
		int dx = 0;
		if (_maxx < env._minx)
			dx = env._minx - _maxx;
		if (_minx > env._maxx)
			dx = _minx - env._maxx;
		int dy = 0;
		if (_maxy < env._miny)
			dy = env._miny - _maxy;
		if (_miny > env._maxy)
			dy = _miny - env._maxy;

		// if either is zero, the envelopes overlap either vertically or
		// horizontally
		if (dx == 0)
			return dy;
		if (dy == 0)
			return dx;
		return (int) Math.sqrt(dx * dx + dy * dy);
	}

	public boolean equalsValue(Object other) {
		if(other == null){
			return false;
		}
		if (!(other instanceof Envelope)) {
			return false;
		}
		Envelope otherEnvelope = (Envelope) other;
		if (isNull()) {
			return otherEnvelope.isNull();
		}
		return _maxx == otherEnvelope._maxx && _maxy == otherEnvelope._maxy
				&& _minx == otherEnvelope._minx && _miny == otherEnvelope._miny;
	}

	@Override
	public int deserialize(byte[] b, int from) {
		if (b == null)
			return 0;
		if (b.length < length + from)
			return 0;

		int nPos = from;
		_minx = SerializeTool.getInt(b, nPos);
		nPos += 4;

		_miny = SerializeTool.getInt(b, nPos);
		nPos += 4;

		_maxx = SerializeTool.getInt(b, nPos);
		nPos += 4;

		_maxy = SerializeTool.getInt(b, nPos);
		return length;
	}

	@Override
	public int getByteSize() {
		return length;
	}
}
