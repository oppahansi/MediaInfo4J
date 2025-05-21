package de.oppa.mi4j;

/*
 * Copyright (C) 2025 oppahansi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Enum representing different types of sections in media information.
 * <p>
 * This enum categorizes sections based on their content type, such as
 * Audio, Video, Image, Text, Menu, Other, and General.
 */
public enum SectionType {
    AUDIO("Audio"),
    VIDEO("Video"),
    IMAGE("Image"),
    TEXT("Text"),
    MENU("Menu"),
    OTHER("Other"),
    GENERAL("General");

    private final String name;

    SectionType(String name) {
        this.name = name;
    }

    /**
     * Get the SectionType enum value from a string name.
     *
     * @param name the section name (e.g., "Audio", "Audio #1", "General")
     * @return the corresponding SectionType, or OTHER if not found
     */
    public static SectionType fromName(String name) {
        if (name == null) {
            return OTHER;
        }

        // Normalize the name (e.g., "Audio #1" -> "Audio")
        String normalizedName = name.split("\\s+#")[0].trim();

        try {
            return valueOf(normalizedName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }

    public String getName() {
        return name;
    }

}
