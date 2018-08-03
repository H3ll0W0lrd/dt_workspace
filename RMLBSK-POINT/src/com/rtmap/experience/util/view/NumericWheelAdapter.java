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

package com.rtmap.experience.util.view;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter implements WheelAdapter {

	public final static String[] FLOOR_ARRAY = new String[] { "B1", "B2",
		"B3", "B4", "B5", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8",
		"F9", "F10", "F11", "F12", "F13", "F14", "F15", "F16", "F17",
		"F18", "F19", "F20", "F21", "F22", "F23", "F24", "F25", "F26",
		"F27", "F28", "F29", "F30" };
	/**
	 * Default constructor
	 */
	public NumericWheelAdapter() {
	}

	@Override
	public String getItem(int index) {
		if (index >= 0 && index < getItemsCount()) {
			return FLOOR_ARRAY[index];
		}
		return null;
	}

	@Override
	public int getItemsCount() {
		return FLOOR_ARRAY.length;
	}

	@Override
	public int getMaximumLength() {
		return 4;
	}
}
