/**
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets Copyright (C) dmulloy2
 * <http://dmulloy2.net> Copyright (C) Kristian S. Strangeland
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */
package me.catcoder.sidebar.wrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.IntEnum;

public class WrapperPlayServerScoreboardObjective extends AbstractPacket {

  public static final PacketType TYPE =
      PacketType.Play.Server.SCOREBOARD_OBJECTIVE;

  public WrapperPlayServerScoreboardObjective() {
    super(new PacketContainer(TYPE), TYPE);
    handle.getModifier().writeDefaults();
  }

  public WrapperPlayServerScoreboardObjective(PacketContainer packet) {
    super(packet, TYPE);
  }

  /**
   * Enum containing all known packet modes.
   *
   * @author dmulloy2
   */
  public static class Mode extends IntEnum {

    public static final int ADD_OBJECTIVE = 0;
    public static final int REMOVE_OBJECTIVE = 1;
    public static final int UPDATE_VALUE = 2;

    private static final Mode INSTANCE = new Mode();

    public static Mode getInstance() {
      return INSTANCE;
    }
  }

  /**
   * Retrieve Objective name.
   * <p>
   * Notes: an unique name for the objective
   *
   * @return The current Objective name
   */
  public String getName() {
    return handle.getStrings().read(0);
  }

  /**
   * Set Objective name.
   *
   * @param value - new value.
   */
  public void setName(String value) {
    handle.getStrings().write(0, value);
  }

  /**
   * Retrieve Objective DisplayName.
   * <p>
   * Notes: only if mode is 0 or 2. The text to be displayed for the score.
   *
   * @return The current Objective value
   */
  public String getDisplayName() {
    return handle.getStrings().read(1);
  }

  /**
   * Set Objective DisplayName.
   *
   * @param value - new value.
   */
  public void setDisplayName(String value) {
    handle.getStrings().write(1, value);
  }

  /**
   * Retrieve health display.
   * <p>
   * Notes: Can be either INTEGER or HEARTS
   *
   * @return health display
   */
  public HealthDisplay getHealthDisplay() {
    return handle.getEnumModifier(HealthDisplay.class, 2).read(0);
  }

  /**
   * Set health display.
   *
   * @param value - value
   * @see #getHealthDisplay()
   */
  public void setHealthDisplay(HealthDisplay value) {
    handle.getEnumModifier(HealthDisplay.class, 2).write(0, value);
  }

  /**
   * Retrieve Mode.
   * <p>
   * Notes: 0 to create the scoreboard. 1 to remove the scoreboard. 2 to
   * update the display text.
   *
   * @return The current Mode
   */
  public int getMode() {
    return handle.getIntegers().read(0);
  }

  /**
   * Set Mode.
   *
   * @param value - new value.
   */
  public void setMode(int value) {
    handle.getIntegers().write(0, value);
  }

  public static enum HealthDisplay {
    INTEGER, HEARTS;
  }
}
