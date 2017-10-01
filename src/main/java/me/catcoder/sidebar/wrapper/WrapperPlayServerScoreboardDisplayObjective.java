/**
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
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
package me.catcoder.sidebar.wrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerScoreboardDisplayObjective extends AbstractPacket {
	public static final PacketType TYPE =
			PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE;

	public WrapperPlayServerScoreboardDisplayObjective() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerScoreboardDisplayObjective(PacketContainer packet) {
		super(packet, TYPE);
	}

	/**
	 * Retrieve Position.
	 * <p>
	 * Notes: the position of the scoreboard. 0 = list, 1 = sidebar, 2 =
	 * belowName.
	 * 
	 * @return The current Position
	 */
	public int getPosition() {
		return handle.getIntegers().read(0);
	}

	/**
	 * Set Position.
	 * 
	 * @param value - new value.
	 */
	public void setPosition(int value) {
		handle.getIntegers().write(0, value);
	}

	/**
	 * Retrieve Score Name.
	 * <p>
	 * Notes: the unique name for the scoreboard to be displayed.
	 * 
	 * @return The current Score Name
	 */
	public String getScoreName() {
		return handle.getStrings().read(0);
	}

	/**
	 * Set Score Name.
	 * 
	 * @param value - new value.
	 */
	public void setScoreName(String value) {
		handle.getStrings().write(0, value);
	}

}
