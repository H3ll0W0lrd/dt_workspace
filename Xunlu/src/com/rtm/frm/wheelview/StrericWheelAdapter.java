/*
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rtm.frm.wheelview;

import java.util.ArrayList;

import com.rtm.frm.model.Floor;

/**
 * Numeric Wheel adapter.
 */
public class StrericWheelAdapter implements WheelAdapter {

	/** The default min value */
	// private String[] strContents;
	ArrayList<Floor> floors;

	/**
	 * 构造方法
	 * 
	 * @param strContents
	 */
	public StrericWheelAdapter(ArrayList<Floor> floors) {
		
		this.floors = floors;

	}

	public ArrayList<Floor> getStrContents() {
		return floors;
	}

	public void setStrContents(ArrayList<Floor> floors) {
		this.floors = floors;
	}

	public String getItem(int index) {
		if (index >= 0 && index < getItemsCount()) {
			return floors.get(index).getFloor();
		}
		return null;
	}

	public int getItemsCount() {
		if (floors != null) {
			return floors.size();
		} else {
			return 0;
		}
	}

	/**
	 * 设置最大的宽度
	 */
	public int getMaximumLength() {
		int maxLen = 5;
		return maxLen;
	}
}
