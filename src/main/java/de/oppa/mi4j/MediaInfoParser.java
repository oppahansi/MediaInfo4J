package de.oppa.mi4j;

import com.sun.jna.Pointer;
import com.sun.jna.WString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

/*
 * Copyright (C) 2025 oppahansi
 * Copyright 2015-2021 Caprica Software Limited.
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
 *
 * This code is a derivative work of the vlcj-info library
 * (https://github.com/caprica/vlcj-info) and
 * heavily modified by oppahansi (https://github.com/oppahansi).
 */

/**
 * MediaInfoParser class to parse media information.
 * <p>
 * This class provides methods to parse media information from a file or raw data.
 * It uses the MediaInfo library to extract metadata and organize it into sections.
 */
public final class MediaInfoParser {
    private static final String FAILED_TO_RETRIEVE_MEDIA_INFORMATION_DATA_IS_NULL = "Failed to retrieve media information. Data is null";
    private static final String NO_MEDIA_INFORMATION_FOUND_DATA_IS_EMPTY = "No media information found. Data is empty";
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s+.*$");

    /**
     * Parse the media information from a file.
     *
     * @param filePath file path to extract media information from
     * @return parsed media information
     * @throws MediaInfoParseException if parsing fails
     */
    public MediaInfo parseFile(String filePath) {
        if (!MediaInfo.isSupportedFileType(filePath)) {
            throw new MediaInfoParseException("Unsupported file type: %s".formatted(filePath));
        }

        MediaInfoLib mediaInfoLib = MediaInfoLib.INSTANCE;
        Pointer handle = mediaInfoLib.MediaInfo_New();
        if (handle == null) {
            throw new MediaInfoParseException("Failed to initialize MediaInfo handle");
        }

        try {
            if (mediaInfoLib.MediaInfo_Open(handle, new WString(filePath)) == 1) {
                mediaInfoLib.MediaInfo_Option(handle, new WString("Inform"), new WString(""));
                mediaInfoLib.MediaInfo_Option(handle, new WString("Complete"), new WString("1"));
                WString data = mediaInfoLib.MediaInfo_Inform(handle);

                checkDataValidity(data);

                return parseData(data.toString());
            }

            throw new MediaInfoParseException("Failed to open file. File path: %s".formatted(filePath));
        } finally {
            mediaInfoLib.MediaInfo_Close(handle);
            mediaInfoLib.MediaInfo_Delete(handle);
        }
    }

    /**
     * Parse the raw data into MediaInfo.
     *
     * @param data raw data to parse
     * @return parsed media information
     * @throws MediaInfoParseException if parsing fails
     */
    public MediaInfo parseData(String data) {
        checkDataValidity(data);

        try (var stringReader = new StringReader(data);
             var bufferedReader = new BufferedReader(stringReader)) {

            List<String> mediaInfoLines = bufferedReader.lines().toList();
            MediaInfo mediaInfo = new MediaInfo();
            String currentSectionName;
            Section currentSection = null;
            SectionType currentSectionType = null;
            int chapterNumber = 0;

            for (String infoLine : mediaInfoLines) {
                if (infoLine == null || infoLine.isEmpty()) {
                    currentSection = null;
                    currentSectionType = null;
                    continue;
                }

                // Check if the line is a section header
                if (isSectionHeader(infoLine)) {
                    currentSectionName = infoLine.trim();
                    currentSectionType = SectionType.fromName(currentSectionName);
                    currentSection = mediaInfo.getOrCreateSection(currentSectionType, currentSectionName);
                    continue;
                }

                // Parse line based on section type
                if (currentSection != null) {
                    if (currentSectionType.equals(SectionType.MENU)) {
                        if (TIMESTAMP_PATTERN.matcher(infoLine).matches()) {
                            chapterNumber++;
                        }
                        parseMenuLine(infoLine, currentSection, chapterNumber);
                    } else {
                        parseKeyValueLine(infoLine, currentSection);
                    }
                } else {
                    System.err.printf("Warning: No section defined for line: %s%n", infoLine);
                }
            }

            if (mediaInfo.hasSection(SectionType.MENU)) {
                mediaInfo.getSection(SectionType.MENU, SectionType.MENU.getName())
                    .addFieldValue("ChapterCount", String.valueOf(chapterNumber));
            }

            return mediaInfo;
        } catch (IOException e) {
            throw new MediaInfoParseException("Failed to parse data: %s".formatted(e.getMessage()), e);
        }
    }

    /**
     * Determine if a media info line is a section header.
     *
     * @param infoLine the media info line to check
     * @return true if the line is a section header
     */
    private boolean isSectionHeader(String infoLine) {
        // Check if the line matches any of the known section types
        // General, Video, Audio, Text, Image, Menu
        // or with an optional chapter number (e.g., Text #1)
        return infoLine.matches("^(%s|%s|%s( #\\d+)?|%s( #\\d+)?|%s|%s)$".formatted(
            SectionType.GENERAL.getName(),
            SectionType.VIDEO.getName(),
            SectionType.AUDIO.getName(),
            SectionType.TEXT.getName(),
            SectionType.IMAGE.getName(),
            SectionType.MENU.getName()));
    }

    /**
     * Parse a key-value media info line for most sections (General, Video, Audio, Text, Image).
     *
     * @param infoLine the media info line to parse
     * @param section  the section to add the key-value pair to
     */
    private void parseKeyValueLine(String infoLine, Section section) {
        int firstColon = infoLine.indexOf(':');
        if (firstColon != -1) {
            String key = infoLine.substring(0, firstColon).trim();
            String value = infoLine.substring(firstColon + 1).trim();
            section.addFieldValue(key, value);
        } else {
            System.err.printf("Warning: Invalid key-value pair: %s%n", infoLine);
        }
    }

    /**
     * Parse a media info line in the Menu section, handling timestamps and chapter names.
     *
     * @param infoLine the media info line to parse
     * @param section  the Menu section to add data to
     */
    private void parseMenuLine(String infoLine, Section section, int chapterNumber) {
        if (TIMESTAMP_PATTERN.matcher(infoLine).matches()) {
            String[] parts = infoLine.split("\\s+", 2);
            if (parts.length == 2) {
                String timestamp = parts[0].trim();
                String chapterName = parts[1].trim();
                // Store as a key-value pair with a unique key
                section.addFieldValue("ChapterName %d".formatted(chapterNumber), chapterName);
                section.addFieldValue("ChapterTimestamp %d".formatted(chapterNumber), timestamp);
            } else {
                System.err.printf("Warning: Invalid chapter infoLine: %s%n", infoLine);
            }
        } else {
            parseKeyValueLine(infoLine, section);
        }
    }

    private void checkDataValidity(WString data) {
        if (data == null) {
            throw new MediaInfoParseException(FAILED_TO_RETRIEVE_MEDIA_INFORMATION_DATA_IS_NULL);
        }
        if (data.isEmpty()) {
            throw new MediaInfoParseException(NO_MEDIA_INFORMATION_FOUND_DATA_IS_EMPTY);
        }
    }

    private void checkDataValidity(String data) {
        if (data == null) {
            throw new MediaInfoParseException(FAILED_TO_RETRIEVE_MEDIA_INFORMATION_DATA_IS_NULL);
        }
        if (data.isEmpty()) {
            throw new MediaInfoParseException(NO_MEDIA_INFORMATION_FOUND_DATA_IS_EMPTY);
        }

        for (SectionType sectionType : SectionType.values()) {
            if (data.contains(sectionType.getName())) {
                return;
            }
        }
        throw new MediaInfoParseException("No media information found. Data does not contain any section headers");
    }
}