package de.oppa.mi4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

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
 * MediaInfo class to hold media information.
 * <p>
 * This class provides methods to check supported file types, manage sections,
 * and dump media information in a readable format.
 * <p>
 * It uses a map to store sections of media information, where each section
 * is identified by its type.
 * </p>
 */
public final class MediaInfo {
    private static final String HEX_08X_STRING_TEMPLATE = "%08X";
    private static final String HEX_02X_STRING_TEMPLATE = "%02X";
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");
    /**
     * Supported file extensions for media files.
     * <p>
     * This set contains the file extensions that are recognized as valid media file types.
     * Source:
     * <a href="https://mediaarea.net/en/MediaInfo/Support/Formats">https://mediaarea.net/en/MediaInfo/Support/Formats</a>
     * </p>
     */
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        "mkv", "mka", "mks", // Matroska
        "ogg", "ogm", // Ogg
        "avi", "wav", // Riff
        "mpeg", "mpg", "vob", // Mpeg 1&2 container
        "mp4", // Mpeg 4 container
        "mpgv", "mpv", "m1v", "m2v", // Mpeg video specific
        "mp2", "mp3", // Mpeg audio specific
        "asf", "wma", "wmv", // Windows Media
        "qt", "mov", // Quicktime
        "rm", "rmvb", "ra", // Real
        "ifo", // DVD-Video
        "ac3", // AC3
        "dts", // DTS
        "aac", // AAC
        "ape", "mac", // Monkey's Audio
        "flac", // Flac
        "dat", // CDXA, like Video-CD
        "aiff", "aifc", // Apple/SGI
        "au", // Sun/NeXT
        "iff", // Amiga IFF/SVX8/SV16
        "paf", // Ensoniq PARIS
        "sd2", // Sound Designer 2
        "irca", // Berkeley/IRCAM/CARL
        "w64", // SoundFoundry WAVE 64
        "mat", // Matlab
        "pvf", // Portable Voice format
        "xi", // FastTracker2 Extended
        "sds", // Midi Sample dump Format
        "avr" // Audio Visual Research
    );
    /**
     * Map to hold sections of media information. Grouped by type and name.
     * <p>
     * This map holds the sections of the media information, where the key is the section type
     * and the value is the corresponding section object.
     * </p>
     */
    private final Map<SectionType, Map<String, Section>> typeToNameToSection = new LinkedHashMap<>();


    /**
     * Check if the file extension is supported.
     *
     * @param filePath the file path to check
     * @return true if the file extension is supported, false otherwise
     */
    public static boolean isSupportedFileType(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        // Extract the extension (last part after the last dot)
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
            throw new IllegalArgumentException("File path does not contain a valid extension. File path: %s".formatted(filePath));
        }

        String extension = filePath.substring(lastDotIndex + 1).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    /**
     * Get or create a section for the given type.
     *
     * @param type section type
     * @return the section for the type
     */
    public Section getOrCreateSection(SectionType type, String sectionName) {
        validateSectionType(type);
        validateSectionName(sectionName);

        return typeToNameToSection
            .computeIfAbsent(type, k -> new LinkedHashMap<>())
            .computeIfAbsent(sectionName, k -> new Section());
    }

    /**
     * Get all sections.
     *
     * @return unmodifiable map of sections by type
     */
    public Map<SectionType, Map<String, Section>> getSections() {
        return Collections.unmodifiableMap(typeToNameToSection);
    }

    /**
     * Get all sections for a specific type.
     *
     * @param type section type
     * @return unmodifiable map of sections for the type
     */
    public Map<String, Section> getSections(SectionType type) {
        validateSectionType(type);

        return Collections.unmodifiableMap(typeToNameToSection.getOrDefault(type, Collections.emptyMap()));
    }

    /**
     * Get all section types.
     *
     * @return unmodifiable set of section types
     */
    public Set<SectionType> getSectionTypes() {
        return Collections.unmodifiableSet(typeToNameToSection.keySet());
    }

    /**
     * Get all section names for a specific type.
     *
     * @param type section type
     * @return unmodifiable set of section names for the type
     */
    public Set<String> getSectionNames(SectionType type) {
        validateSectionType(type);

        return Collections.unmodifiableSet(typeToNameToSection.getOrDefault(type, Collections.emptyMap()).keySet());
    }

    /**
     * Get all section names across all types.
     *
     * @return unmodifiable set of all section names
     */
    public Set<String> getSectionNames() {
        return typeToNameToSection.values().stream()
            .flatMap(map -> map.keySet().stream())
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get the section for a specific type.
     *
     * @param type section type
     * @return the section, or null if not found
     */
    public Section getSection(SectionType type, String sectionName) {
        validateSectionType(type);
        validateSectionName(sectionName);

        return typeToNameToSection
            .getOrDefault(type, Collections.emptyMap())
            .get(sectionName);
    }

    /**
     * Get the section for a specific name across all types.
     *
     * @param sectionName section name
     * @return the section, or null if not found
     */
    public Section getSection(String sectionName) {
        validateSectionName(sectionName);

        if (!hasSection(sectionName)) {
            throw new IllegalArgumentException("Section name does not exist");
        }

        return typeToNameToSection.values().stream()
            .flatMap(map -> map.entrySet().stream())
            .filter(entry -> entry.getKey().equals(sectionName))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    /**
     * Get the section for a specific type, or create it if it doesn't exist.
     *
     * @param type section type
     * @return the section
     */
    public boolean hasSection(SectionType type, String sectionName) {
        validateSectionType(type);
        validateSectionName(sectionName);

        return typeToNameToSection
            .getOrDefault(type, Collections.emptyMap())
            .containsKey(sectionName);
    }

    /**
     * Check if a section with the given name exists.
     *
     * @param sectionName section name
     * @return true if the section exists, false otherwise
     */
    public boolean hasSection(String sectionName) {
        validateSectionName(sectionName);

        return typeToNameToSection.values().stream()
            .anyMatch(map -> map.containsKey(sectionName));
    }

    /**
     * Check if a section of the given type exists.
     *
     * @param type section type
     * @return true if the section exists, false otherwise
     */
    public boolean hasSection(SectionType type) {
        validateSectionType(type);

        return typeToNameToSection.containsKey(type);
    }

    /**
     * Check if a field name exists in the section of the given type and name.
     *
     * @param type        section type
     * @param sectionName section name
     * @param fieldName   field name
     * @return true if the field name exists, false otherwise
     */
    public boolean hasFieldName(SectionType type, String sectionName, String fieldName) {
        validateSectionType(type);
        validateSectionName(sectionName);
        validateSectionName(fieldName);

        return typeToNameToSection
            .getOrDefault(type, Collections.emptyMap())
            .getOrDefault(sectionName, new Section())
            .hasField(fieldName);
    }

    /**
     * Check if a field name exists in the section of the given name.
     *
     * @param sectionName section name
     * @param fieldName   field name
     * @return true if the field name exists, false otherwise
     */
    public boolean hasFieldName(String sectionName, String fieldName) {
        validateSectionName(fieldName);
        validateSectionName(sectionName);

        Section section = getSection(sectionName);
        if (section == null) {
            return false;
        }

        return section.hasField(fieldName);
    }

    /**
     * Check if a field name exists in any section.
     *
     * @param fieldName field name
     * @return true if the field name exists, false otherwise
     */
    public boolean hasFieldName(String fieldName) {
        validateSectionName(fieldName);

        return typeToNameToSection.values().stream()
            .flatMap(map -> map.values().stream())
            .anyMatch(section -> section.hasField(fieldName));
    }

    /**
     * Get all field names for a specific section type and name.
     *
     * @param type        section type
     * @param sectionName section name
     * @return unmodifiable set of field names for the section
     */
    public Set<String> getFieldNames(SectionType type, String sectionName) {
        validateSectionType(type);
        validateSectionName(sectionName);

        return typeToNameToSection
            .getOrDefault(type, Collections.emptyMap())
            .getOrDefault(sectionName, new Section())
            .getFieldNames();
    }

    /**
     * Get all field names for a specific section type.
     *
     * @param type section type
     * @return unmodifiable set of field names for the type
     */
    public Set<String> getFieldNames(SectionType type) {
        validateSectionType(type);

        return typeToNameToSection
            .getOrDefault(type, Collections.emptyMap())
            .values().stream()
            .flatMap(section -> section.getFieldNames().stream())
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get all field names across all sections.
     *
     * @return unmodifiable set of all field names
     */
    public Set<String> getFieldNames() {
        return typeToNameToSection.values().stream()
            .flatMap(map -> map.values().stream())
            .flatMap(section -> section.getFieldNames().stream())
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Dump media information in a readable format for debugging.
     *
     * @param writer writer to dump the media information to
     */
    public void dump(Writer writer) {
        if (writer == null) {
            throw new IllegalArgumentException("Writer cannot be null");
        }


        try (PrintWriter printer = new PrintWriter(writer)) {
            if (typeToNameToSection.isEmpty()) {
                printer.println("No sections available.");
                return;
            }
            for (Map.Entry<SectionType, Map<String, Section>> entry : typeToNameToSection.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    printer.printf("Section Type: %s%n", entry.getKey().getName());
                    printer.println("  No sections available.");
                    continue;
                }

                SectionType sectionType = entry.getKey();
                Map<String, Section> sections = entry.getValue();

                printer.printf("Section Type: %s%n", sectionType.getName());
                for (Map.Entry<String, Section> sectionEntry : sections.entrySet()) {
                    String sectionName = sectionEntry.getKey();
                    Section section = sectionEntry.getValue();

                    printer.printf("  Section Name: %s%n", sectionName);
                    for (String field : section) {
                        String value = section.getFieldValue(field);
                        printer.printf("    %35s -> %s%n", field, value);
                    }
                }

                printer.println();
            }
        }
    }

    @Override
    public String toString() {
        return "%s[sectionsByType=%s]".formatted(getClass().getSimpleName(), typeToNameToSection);
    }

    /**
     * Calculate the checksum of the media file using the default CRC32 algorithm.
     *
     * @return the checksum as a hexadecimal string
     */
    public String calculateChecksum() {
        return HEX_08X_STRING_TEMPLATE.formatted(calculateCrc32());
    }

    /**
     * Calculate the checksum of the media file using the specified algorithm.
     *
     * @param algorithm the checksum algorithm to use
     * @return the checksum as a hexadecimal string
     */
    public String calculateChecksum(ChecksumAlgorithm algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }

        if (algorithm.equals(ChecksumAlgorithm.CRC32)) {
            return HEX_08X_STRING_TEMPLATE.formatted(calculateCrc32());
        } else if (algorithm.equals(ChecksumAlgorithm.ADLER32)) {
            return HEX_08X_STRING_TEMPLATE.formatted(calculateAdler32());
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm.getName());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm not supported: " + algorithm, e);
        }


        Section general = getSection(SectionType.GENERAL, SectionType.GENERAL.getName());
        File file = new File(general.getFieldValue("Complete name"));

        validateMediaFile(file);
        digestFileForMessageDigest(file, digest);

        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format(HEX_02X_STRING_TEMPLATE, b));
        }

        return hexString.toString();
    }

    /**
     * Verify the checksum of the media file.
     *
     * @param checksum the checksum to verify
     * @return true if the checksum matches, false otherwise
     */
    public boolean verifyChecksum(String checksum) {
        List<ChecksumAlgorithm> candidates = guessAlgorithm(checksum);
        String normalizedChecksum = checksum.replaceAll("\\s+", "").toLowerCase();

        for (ChecksumAlgorithm algo : candidates) {
            String computedChecksum = calculateChecksum(algo);
            String normalizedComputedChecksum = computedChecksum.replaceAll("\\s+", "").toLowerCase();
            if (normalizedComputedChecksum.equals(normalizedChecksum)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculate the CRC32 checksum of the media file.
     *
     * @return the CRC32 checksum
     */
    private long calculateCrc32() {
        Section general = getSection(SectionType.GENERAL, SectionType.GENERAL.getName());
        File file = new File(general.getFieldValue("Complete name"));

        validateMediaFile(file);

        CRC32 crc32 = new CRC32();

        processFileForChecksum(file, crc32);

        return crc32.getValue();
    }

    /**
     * Calculate the Adler-32 checksum of the media file.
     *
     * @return the Adler-32 checksum
     */
    private long calculateAdler32() {
        Section general = getSection(SectionType.GENERAL, SectionType.GENERAL.getName());
        File file = new File(general.getFieldValue("Complete name"));

        validateMediaFile(file);

        Adler32 adler32 = new Adler32();

        processFileForChecksum(file, adler32);

        return adler32.getValue();
    }

    /**
     * Process the file for checksum calculation using MessageDigest.
     *
     * @param file   the file to process
     * @param digest the MessageDigest object to update
     */
    private void digestFileForMessageDigest(File file, MessageDigest digest) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process the file for checksum calculation.
     *
     * @param file     the file to process
     * @param checksum the checksum object to update
     */
    private void processFileForChecksum(File file, Checksum checksum) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                checksum.update(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Guess the checksum algorithm based on the length of the checksum string.
     *
     * @param checksum the checksum string
     * @return a list of possible checksum algorithms
     */
    private List<ChecksumAlgorithm> guessAlgorithm(String checksum) {
        // Normalize checksum: remove spaces, convert to lowercase
        String normalized = checksum.replaceAll("\\s+", "").toLowerCase();

        // Validate hex format
        if (!HEX_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid hexadecimal checksum: " + checksum);
        }

        List<ChecksumAlgorithm> candidates = new ArrayList<>();
        switch (normalized.length()) {
            case 8: // CRC32 or Adler-32
                candidates.add(ChecksumAlgorithm.CRC32);
                candidates.add(ChecksumAlgorithm.ADLER32);
                break;
            case 32: // MD5
                candidates.add(ChecksumAlgorithm.MD5);
                break;
            case 40: // SHA-1
                candidates.add(ChecksumAlgorithm.SHA1);
                break;
            case 64: // SHA-256
                candidates.add(ChecksumAlgorithm.SHA256);
                break;
            case 128: // SHA-512
                candidates.add(ChecksumAlgorithm.SHA512);
                break;
            default:
                throw new IllegalArgumentException("Unknown checksum length: " + normalized.length());
        }
        return candidates;
    }

    private void validateSectionType(SectionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Section type cannot be null");
        }
    }

    private void validateSectionName(String sectionName) {
        if (sectionName == null || sectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be null or empty");
        }
    }

    private void validateMediaFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: %s".formatted(file.getAbsolutePath()));
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a file: %s".formatted(file.getAbsolutePath()));
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("File cannot be read: %s".formatted(file.getAbsolutePath()));
        }
    }

}