/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package me.catcoder.sidebar.util.version;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Determine the current Minecraft version.
 *
 * @author Kristian
 */
public final class MinecraftVersion implements Comparable<MinecraftVersion>, Serializable {

    /**
     * Regular expression used to parse version strings.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*");

    /**
     * The current version of minecraft, lazy initialized by MinecraftVersion.currentVersion()
     */
    private static MinecraftVersion currentVersion;

    private final int major;
    private final int minor;
    private final int build;
    // The development stage
    private final String development;

    private volatile Boolean atCurrentOrAbove;

    /**
     * Construct a version format from the standard release version or the snapshot verison.
     *
     * @param versionOnly - the version.
     */
    private MinecraftVersion(String versionOnly) {
        String[] section = versionOnly.split("-");
        int[] numbers;

        numbers = this.parseVersion(section[0]);

        this.major = numbers[0];
        this.minor = numbers[1];
        this.build = numbers[2];
        this.development = section.length > 1 ? section[1] : null;
    }

    /**
     * Construct a version object directly.
     *
     * @param major - major version number.
     * @param minor - minor version number.
     * @param build - build version number.
     */
    public MinecraftVersion(int major, int minor, int build) {
        this(major, minor, build, null);
    }

    /**
     * Construct a version object directly.
     *
     * @param major       - major version number.
     * @param minor       - minor version number.
     * @param build       - build version number.
     * @param development - development stage.
     */
    public MinecraftVersion(int major, int minor, int build, String development) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.development = development;
    }

    /**
     * Extract the Minecraft version from CraftBukkit itself.
     *
     * @param text - the server version in text form.
     * @return The underlying MC version.
     * @throws IllegalStateException If we could not parse the version string.
     */
    public static String extractVersion(String text) {
        Matcher version = VERSION_PATTERN.matcher(text);

        if (version.matches() && version.group(1) != null) {
            return version.group(1);
        } else {
            throw new IllegalStateException("Cannot parse version String '" + text + "'");
        }
    }

    /**
     * Parse the given server version into a Minecraft version.
     *
     * @param serverVersion - the server version.
     * @return The resulting Minecraft version.
     */
    public static MinecraftVersion fromServerVersion(String serverVersion) {
        return new MinecraftVersion(extractVersion(serverVersion));
    }

    public static MinecraftVersion getCurrentVersion() {
        if (currentVersion == null) {
            currentVersion = fromServerVersion(Bukkit.getVersion());
        }

        return currentVersion;
    }

    private static boolean atOrAbove(MinecraftVersion version) {
        return getCurrentVersion().isAtLeast(version);
    }

    private int[] parseVersion(String version) {
        String[] elements = version.split("\\.");
        int[] numbers = new int[3];

        // Make sure it's even a valid version
        if (elements.length < 1) {
            throw new IllegalStateException("Corrupt MC version: " + version);
        }

        // The String 1 or 1.2 is interpreted as 1.0.0 and 1.2.0 respectively.
        for (int i = 0; i < Math.min(numbers.length, elements.length); i++) {
            numbers[i] = Integer.parseInt(elements[i].trim());
        }
        return numbers;
    }

    /**
     * Major version number
     *
     * @return Current major version number.
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * Minor version number
     *
     * @return Current minor version number.
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * Build version number
     *
     * @return Current build version number.
     */
    public int getBuild() {
        return this.build;
    }

    /**
     * Retrieve the development stage.
     *
     * @return Development stage, or NULL if this is a release.
     */
    public String getDevelopmentStage() {
        return this.development;
    }

    /**
     * Checks if this version is at or above the current version the server is running.
     *
     * @return true if this version is equal or newer than the server version, false otherwise.
     */
    public boolean atOrAbove() {
        if (this.atCurrentOrAbove == null) {
            this.atCurrentOrAbove = atOrAbove(this);
        }

        return this.atCurrentOrAbove;
    }

    /**
     * Retrieve the version String (major.minor.build) only.
     *
     * @return A normal version string.
     */
    public String getVersion() {
        if (this.getDevelopmentStage() == null) {
            return String.format("%s.%s.%s", this.getMajor(), this.getMinor(), this.getBuild());
        } else {
            return String.format("%s.%s.%s-%s%s", this.getMajor(), this.getMinor(), this.getBuild(),
                    this.getDevelopmentStage(), "");
        }
    }

    @Override
    public int compareTo(MinecraftVersion o) {
        if (o == null) {
            return 1;
        }

        return ComparisonChain.start()
                .compare(this.getMajor(), o.getMajor())
                .compare(this.getMinor(), o.getMinor())
                .compare(this.getBuild(), o.getBuild())
                .compare(this.getDevelopmentStage(), o.getDevelopmentStage(), Ordering.natural().nullsLast())
                .result();
    }

    public boolean isAtLeast(MinecraftVersion other) {
        if (other == null) {
            return false;
        }

        return this.compareTo(other) >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        if (obj instanceof MinecraftVersion other) {
            return this.getMajor() == other.getMajor() &&
                    this.getMinor() == other.getMinor() &&
                    this.getBuild() == other.getBuild() &&
                    Objects.equals(this.getDevelopmentStage(), other.getDevelopmentStage());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getMajor(), this.getMinor(), this.getBuild());
    }

    @Override
    public String toString() {
        // Convert to a String that we can parse back again
        return String.format("(MC: %s)", this.getVersion());
    }
}