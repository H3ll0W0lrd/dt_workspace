package com.rtm.location.entity;

import java.util.ArrayList;

import android.location.GpsSatellite;
import android.location.Location;

import com.rtm.common.model.RMLocation;

public class GpsEntity {

	public static final int BUILD_UNKNOW = 0;
	public static final int BUILD_IN = 1;
	public static final int BUILD_OUT = 2;

	private static GpsEntity instance;
	private int satelitesCount = 0;
	private Location location = null;
	private RMLocation wirelessLocation = null;
	private long startTime = 0; // 启动定位时间
	private long firstWirelessLocTime = 0;
	private long firstGpsLocTime = 0;
	private boolean accuracyFlg = false; // 初次室内外判断，前4s是否有精度因子大于12的无线定位结果
	private int build = BUILD_UNKNOW;
	private ArrayList<RMLocation> wirelessLocs;
	private boolean isBuildHasOpenSquare = false;

	private long timestampGpsStateChange = System.currentTimeMillis();
	private static final long GPS_CRASH_TIME = 10000;

	private ArrayList<GpsSatellite> snrs;

	private GpsEntity() {
		snrs = new ArrayList<GpsSatellite>();
		wirelessLocs = new ArrayList<RMLocation>();
		build = BUILD_UNKNOW;
	}

	public synchronized static GpsEntity getInstance() {
		if (instance == null) {
			instance = new GpsEntity();
		}
		return instance;
	}

	public int countBelow(int limit) {
		int ret = 0;
		for (int i = 0; i < snrs.size(); i++) {
			if (snrs.get(i).getSnr() < limit) {
				ret++;
			}
		}
		return ret;
	}

	public int countAbove(int limit) {
		int ret = 0;
		for (int i = 0; i < snrs.size(); i++) {
			if (snrs.get(i).getSnr() > limit) {
				ret++;
			}
		}
		return ret;
	}

	public int getSatelitesCount() {
		return this.satelitesCount;
	}

	public void setSatelitesCount(int satelitesCount) {
		this.satelitesCount = satelitesCount;
	}

	public ArrayList<GpsSatellite> getSnrs() {
		return this.snrs;
	}

	public void setSnrs(ArrayList<GpsSatellite> snrList) {
		synchronized (GpsEntity.this) {
			snrs.clear();
			snrs.addAll(snrList);
		}
	}

	public void In2OutSwitch() {
		// GPS 10s无状态变化认为已挂，所有建筑当做露天广场处理
		if (System.currentTimeMillis() - timestampGpsStateChange > GPS_CRASH_TIME) {
			isBuildHasOpenSquare = true;
		}
		synchronized (GpsEntity.this) {
			if (build == BUILD_IN && wirelessLocation != null) {
				boolean flg1 = true;
				int times = 0;
				if (wirelessLocs != null && wirelessLocs.size() > 0) {
					for (int i = 0; i < wirelessLocs.size(); i++) {
						if ((wirelessLocs.get(i).error != 0 || wirelessLocs
								.get(i).accuracy > 12)) {
							times++;
						} else {
							flg1 = false;
							break;
						}
					}
				}
				if (times >= 5 && flg1 == true) {
					flg1 = true;
				} else {
					flg1 = false;
				}
				if (isBuildHasOpenSquare) {
					boolean flg2 = (countAbove(30) >= 2 || countAbove(35) >= 1)
							&& (wirelessLocs.size() > 1
									&& wirelessLocs
											.get(wirelessLocs.size() - 1).error != 0 && wirelessLocs
									.get(wirelessLocs.size() - 2).error != 0);
					if (flg1 || flg2) {
						build = BUILD_OUT;
					}
				} else {
					boolean flg2 = (countAbove(30) > 2 || countAbove(35) > 1)
							&& (wirelessLocation.accuracy > 12 || wirelessLocation.error != 0);
					boolean flg3 = (countAbove(30) >= 1 && (wirelessLocation.accuracy >= 16 || wirelessLocation.error != 0));
					boolean flg4 = (countAbove(30) >= 2 || countAbove(35) >= 1);
					if (flg1 || flg2 || flg3 || flg4) {
						build = BUILD_OUT;
					}
				}
			}
		}
	}

	public void clearSnr() {
		synchronized (GpsEntity.this) {
			snrs.clear();
		}
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
		if (firstGpsLocTime == 0) {
			firstGpsLocTime = System.currentTimeMillis();
		}
	}

	public int getBuild() {
		return this.build;
	}

	public void setBuild(int build) {
		this.build = build;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setTimestampGpsStateChange(long timestampGpsStateChange) {
		this.timestampGpsStateChange = timestampGpsStateChange;
	}

	public void setWirelessLocation(RMLocation result) {

		synchronized (GpsEntity.this) {
			this.wirelessLocation = result;
			if (wirelessLocs != null && wirelessLocs.size() == 0) {
				wirelessLocs.add(new RMLocation(result));
			} else if (wirelessLocs != null
					&& wirelessLocs.size() > 0
					&& wirelessLocs.get(wirelessLocs.size() - 1).timestamp != result.timestamp) {
				wirelessLocs.add(new RMLocation(result));
			}
			if (this.wirelessLocs != null && this.wirelessLocs.size() >= 6) {
				this.wirelessLocs.remove(0);
			}
			if (firstWirelessLocTime == 0) {
				firstWirelessLocTime = result.timestamp;
			}
			if (result.accuracy < 12) {
				accuracyFlg = true;
			}
			if (isBuildHasOpenSquare) {
				boolean flg1 = (wirelessLocation.error == 0 && countAbove(25) <= 1);
				boolean flg2 = (wirelessLocation.error == 0 && wirelessLocation.accuracy < 18);
				if (flg1 || flg2) {
					build = BUILD_IN;
				}
			} else {
				boolean flg1 = (wirelessLocation.error == 0 && countAbove(25) <= 1);
				boolean flg2 = ((wirelessLocation.error == 0)
						&& (wirelessLocation.accuracy <= 12) && (countAbove(26) <= 1));
				boolean flg3 = ((wirelessLocation.error == 0)
						&& (wirelessLocation.accuracy <= 6) && (countAbove(30) <= 1));
				if (flg1 || flg2 || flg3) {
					build = BUILD_IN;
				}
			}
		}
	}

	public void FirstJudgement() {
		if (build == BUILD_UNKNOW
				&& System.currentTimeMillis() - startTime >= 4000) {
			if ((firstWirelessLocTime != 0 && firstGpsLocTime == 0)
					|| (firstWirelessLocTime != 0 && firstGpsLocTime != 0 && firstWirelessLocTime < firstGpsLocTime)) {
				build = BUILD_IN;
			} else if ((firstWirelessLocTime == 0 && firstGpsLocTime != 0)
					|| (firstWirelessLocTime != 0 && firstGpsLocTime != 0
							&& firstGpsLocTime < firstWirelessLocTime && accuracyFlg == false)) {
				build = BUILD_OUT;
			} else if (firstWirelessLocTime != 0 && firstGpsLocTime != 0
					&& firstGpsLocTime < firstWirelessLocTime
					&& accuracyFlg == true) {
				build = BUILD_IN;
			} else {
				build = BUILD_UNKNOW;
			}
			accuracyFlg = false;
		}

	}

	public void setBuildHasOpenSquare(boolean isBuildHasOpenSquare) {
		this.isBuildHasOpenSquare = isBuildHasOpenSquare;
	}

}
