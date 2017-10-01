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
import com.comphenix.protocol.wrappers.EnumWrappers.ScoreboardAction;

public class WrapperPlayServerScoreboardScore extends AbstractPacket {
	public static final PacketType TYPE =
			PacketType.Play.Server.SCOREBOARD_SCORE;

	public WrapperPlayServerScoreboardScore() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerScoreboardScore(PacketContainer packet) {
		super(packet, TYPE);
	}

	/**
	 * Retrieve Score name.
	 * <p>
	 * Notes: the name of the score to be updated or removed.
	 * 
	 * @return The current Score name
	 */
	public String getScoreName() {
		return handle.getStrings().read(0);
	}

	/**
	 * Set Score name.
	 * 
	 * @param value - new value.
	 */
	public void setScoreName(String value) {
		handle.getStrings().write(0, value);
	}

	/**
	 * Retrieve Objective Name.
	 * <p>
	 * Notes: the name of the objective the score belongs to.
	 * 
	 * @return The current Objective Name
	 */
	public String getObjectiveName() {
		return handle.getStrings().read(1);
	}

	/**
	 * Set Objective Name.
	 * 
	 * @param value - new value.
	 */
	public void setObjectiveName(String value) {
		handle.getStrings().write(1, value);
	}

	/**
	 * Retrieve Value.
	 * <p>
	 * Notes: the score to be displayed next to the entry. Only sent when
	 * Update/Remove does not equal 1.
	 * 
	 * @return The current Value
	 */
	public int getValue() {
		return handle.getIntegers().read(0);
	}

	/**
	 * Set Value.
	 * 
	 * @param value - new value.
	 */
	public void setValue(int value) {
		handle.getIntegers().write(0, value);
	}

	public ScoreboardAction getAction() {
		return handle.getScoreboardActions().read(0);
	}

	public void setScoreboardAction(ScoreboardAction value) {
		handle.getScoreboardActions().write(0, value);
	}

}
