package de.oppa.mi4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MediaInfoParserTest {

    private final String MP4_CRC32_CHECKSUM = "BE16160A";
    private final String MP4_ADLER32_CHECKSUM = "C03451BF";
    private final String MP4_MD5_CHECKSUM = "7486302389DEC25FC0E8D037FE7B30F4";
    private final String MP4_SHA1_CHECKSUM = "0E7D5CD18C3DB014C33EB4310C677B1A13D16A22";
    private final String MP4_SHA256_CHECKSUM = "81FD170E1F10CF60FFB3235FDB5518187D3703D4616FD3B16D2A25D2CFC5C587";
    private final String MP4_SHA512_CHECKSUM = "E28DBCFA48AC169A443D6341202D435ABC3A12053D7EC7AAD21D8EE4D484E2D9756AFA5D263B4D496DC144C31ABDE839F68250575606199CBE76B04D072A30D4";

    private MediaInfoParser parser;

    @BeforeEach
    void setUp() {
        parser = new MediaInfoParser();
    }

    @Test
    @DisplayName("Test successfully parsing of a mp4 file")
    void parseFileMp4() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp4-file.mp4");
        assertNotNull(info);
        assertEquals(3, info.getSections().size());

        Section general = info.getSection(SectionType.GENERAL.getName());
        assertNotNull(general);
        assertEquals("MPEG-4", general.getFieldValue("Format"));

        Section video = info.getSection(SectionType.VIDEO.getName());
        assertNotNull(video);
        assertEquals("AVC", video.getFieldValue("Format"));

        Section audio = info.getSection(SectionType.AUDIO.getName());
        assertNotNull(audio);
        assertEquals("AAC LC", audio.getFieldValue("Format"));
    }

    @Test
    @DisplayName("Test successfully parsing of a mp3 file")
    void parseFileMp3() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp3-file.mp3");
        assertNotNull(info);
        assertEquals(2, info.getSections().size());

        Section general = info.getSection(SectionType.GENERAL.getName());
        assertNotNull(general);
        assertEquals("MPEG Audio", general.getFieldValue("Format"));

        Section audio = info.getSection(SectionType.AUDIO.getName());
        assertNotNull(audio);
        assertEquals("MPEG Audio", audio.getFieldValue("Format"));
    }

    @Test
    @DisplayName("Test should fail parsing a null file")
    void parseFileNullFile() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseFile(null));
    }

    @Test
    @DisplayName("Test should fail parsing an unsupported file type")
    void parseFileUnsupportedFileType() {
        assertThrows(MediaInfoParseException.class, () -> parser.parseFile("test.txt"));
    }

    @Test
    @DisplayName("Test should fail parsing an non existing file")
    void parseFileNonExistingFile() {
        assertThrows(MediaInfoParseException.class, () -> parser.parseFile("test.mp4"));
    }

    @Test
    @DisplayName("Test successfully parsing of a full media info data")
    void parseData() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        assertNotNull(info);
        assertEquals(5, info.getSections().size());

        Section general = info.getSection(SectionType.GENERAL.getName());
        assertNotNull(general);
        assertEquals("Matroska", general.getFieldValue("Format"));

        Section video = info.getSection(SectionType.VIDEO.getName());
        assertNotNull(video);
        assertEquals("HEVC", video.getFieldValue("Format"));

        Section audio1 = info.getSection("Audio #1");
        assertNotNull(audio1);
        assertEquals("DTS", audio1.getFieldValue("Format"));

        Section audio2 = info.getSection("Audio #2");
        assertNotNull(audio2);
        assertEquals("AC-3", audio2.getFieldValue("Format"));

        Section text1 = info.getSection("Text #1");
        assertNotNull(text1);
        assertEquals("VobSub", text1.getFieldValue("Format"));

        Section text2 = info.getSection("Text #2");
        assertNotNull(text2);
        assertEquals("VobSub", text2.getFieldValue("Format"));

        Section menu = info.getSection(SectionType.MENU.getName());
        assertNotNull(menu);
        assertEquals("00:00:00.000", menu.getFieldValue("ChapterTimestamp 1"));
    }

    @Test
    @DisplayName("Test should fail parsing a null data")
    void parseDataNullData() {
        assertThrows(MediaInfoParseException.class, () -> parser.parseData(null));
    }

    @Test
    @DisplayName("Test should fail parsing an empty data")
    void parseDataEmptyData() {
        assertThrows(MediaInfoParseException.class, () -> parser.parseData(""));
    }

    @Test
    @DisplayName("Test should fail parsing a data with wrong format")
    void parseDataWrongFormat() {
        assertThrows(MediaInfoParseException.class, () -> parser.parseData("test"));
    }

    @Test
    @DisplayName("Testing MediaInfo methods")
    void mediaInfoMethods() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        assertNotNull(info);
        assertEquals(5, info.getSections().size());

        Section general = info.getSection(SectionType.GENERAL.getName());
        assertNotNull(general);
        assertEquals("Matroska", general.getFieldValue("Format"));

        assertTrue(MediaInfo.isSupportedFileType("test.mp4"));
        assertFalse(MediaInfo.isSupportedFileType("test.txt"));
        assertThrows(IllegalArgumentException.class, () -> MediaInfo.isSupportedFileType(null));
        assertThrows(IllegalArgumentException.class, () -> MediaInfo.isSupportedFileType(""));
        assertThrows(IllegalArgumentException.class, () -> MediaInfo.isSupportedFileType("test"));

        Section audio = info.getOrCreateSection(SectionType.AUDIO, "Audio #1");
        assertNotNull(audio);
        assertEquals("DTS", audio.getFieldValue("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getOrCreateSection(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.getOrCreateSection(SectionType.GENERAL, null));
        assertThrows(IllegalArgumentException.class, () -> info.getOrCreateSection(SectionType.GENERAL, ""));

        Map<SectionType, Map<String, Section>> sections = info.getSections();
        assertNotNull(sections);
        assertEquals(5, sections.size());

        Map<String, Section> sectionsByType = info.getSections(SectionType.AUDIO);
        assertNotNull(sectionsByType);
        assertEquals(2, sectionsByType.size());

        Set<SectionType> sectionTypes = info.getSectionTypes();
        assertNotNull(sectionTypes);
        assertEquals(5, sectionTypes.size());

        Set<String> sectionNames = info.getSectionNames();
        assertNotNull(sectionNames);
        assertEquals(7, sectionNames.size());

        Set<String> sectionNamesByType = info.getSectionNames(SectionType.AUDIO);
        assertNotNull(sectionNamesByType);
        assertEquals(2, sectionNamesByType.size());
        assertThrows(IllegalArgumentException.class, () -> info.getSections(null));

        Section sectionByTypeName = info.getSection(SectionType.AUDIO, "Audio #1");
        assertNotNull(sectionByTypeName);
        assertEquals("DTS", sectionByTypeName.getFieldValue("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(SectionType.GENERAL, null));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(SectionType.GENERAL, ""));

        Section secionByName = info.getSection("Audio #1");
        assertNotNull(secionByName);
        assertEquals("DTS", secionByName.getFieldValue("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(null));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(""));

        assertTrue(info.hasSection("Audio #1"));
        assertFalse(info.hasSection("test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection((String) null));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(""));

        assertTrue(info.hasSection(SectionType.AUDIO));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection((SectionType) null));

        assertTrue(info.hasSection(SectionType.AUDIO, "Audio #1"));
        assertFalse(info.hasSection(SectionType.AUDIO, "test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(SectionType.AUDIO, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(SectionType.AUDIO, ""));

        assertTrue(info.hasFieldName(SectionType.AUDIO, "Audio #1", "Format"));
        assertFalse(info.hasFieldName(SectionType.AUDIO, "Audio #1", "test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, null, ""));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, "", null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, "", ""));

        assertTrue(info.hasFieldName("Audio #1", "Format"));
        assertFalse(info.hasFieldName("Audio #1", "test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null, ""));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName("", null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName("", ""));

        assertTrue(info.hasFieldName("Format"));
        assertFalse(info.hasFieldName("test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(""));

        Set<String> fieldNames = info.getFieldNames();
        assertNotNull(fieldNames);
        assertFalse(fieldNames.isEmpty());
        assertTrue(fieldNames.contains("Format"));

        fieldNames = info.getFieldNames(SectionType.AUDIO);
        assertNotNull(fieldNames);
        assertFalse(fieldNames.isEmpty());
        assertTrue(fieldNames.contains("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getFieldNames(null));

        fieldNames = info.getFieldNames(SectionType.AUDIO, "Audio #1");
        assertNotNull(fieldNames);
        assertFalse(fieldNames.isEmpty());
        assertTrue(fieldNames.contains("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getFieldNames(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.getFieldNames(SectionType.AUDIO, null));
        assertThrows(IllegalArgumentException.class, () -> info.getFieldNames(SectionType.AUDIO, ""));

        assertThrows(IllegalArgumentException.class, () -> info.dump(null));
        assertDoesNotThrow(() -> info.dump(new OutputStreamWriter(System.out)));
    }

    @Test
    @DisplayName("Test MediaInfo section retrieval and field values")
    void testSectionRetrievalAndFieldValues() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        assertNotNull(info);
        assertEquals(5, info.getSections().size());

        Section general = info.getSection(SectionType.GENERAL.getName());
        assertNotNull(general);
        assertEquals("Matroska", general.getFieldValue("Format"));
    }

    @Test
    @DisplayName("Test MediaInfo.isSupportedFileType")
    void testIsSupportedFileType() {
        assertTrue(MediaInfo.isSupportedFileType("test.mp4"));
        assertFalse(MediaInfo.isSupportedFileType("test.txt"));
        assertThrows(IllegalArgumentException.class, () -> MediaInfo.isSupportedFileType(null));
        assertThrows(IllegalArgumentException.class, () -> MediaInfo.isSupportedFileType(""));
        assertThrows(IllegalArgumentException.class, () -> MediaInfo.isSupportedFileType("test"));
    }

    @Test
    @DisplayName("Test getOrCreateSection and its validation")
    void testGetOrCreateSection() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        Section audio = info.getOrCreateSection(SectionType.AUDIO, "Audio #1");
        assertNotNull(audio);
        assertEquals("DTS", audio.getFieldValue("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getOrCreateSection(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.getOrCreateSection(SectionType.GENERAL, null));
        assertThrows(IllegalArgumentException.class, () -> info.getOrCreateSection(SectionType.GENERAL, ""));
    }

    @Test
    @DisplayName("Test getSections and getSections(SectionType)")
    void testGetSections() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        Map<SectionType, Map<String, Section>> sections = info.getSections();
        assertNotNull(sections);
        assertEquals(5, sections.size());

        Map<String, Section> sectionsByType = info.getSections(SectionType.AUDIO);
        assertNotNull(sectionsByType);
        assertEquals(2, sectionsByType.size());
        assertThrows(IllegalArgumentException.class, () -> info.getSections(null));
    }

    @Test
    @DisplayName("Test getSectionTypes, getSectionNames, getSectionNames(SectionType)")
    void testSectionTypesAndNames() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        Set<SectionType> sectionTypes = info.getSectionTypes();
        assertNotNull(sectionTypes);
        assertEquals(5, sectionTypes.size());

        Set<String> sectionNames = info.getSectionNames();
        assertNotNull(sectionNames);
        assertEquals(7, sectionNames.size());

        Set<String> sectionNamesByType = info.getSectionNames(SectionType.AUDIO);
        assertNotNull(sectionNamesByType);
        assertEquals(2, sectionNamesByType.size());
    }

    @Test
    @DisplayName("Test getSection by type and name, and by name only")
    void testGetSectionByTypeAndName() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        Section sectionByTypeName = info.getSection(SectionType.AUDIO, "Audio #1");
        assertNotNull(sectionByTypeName);
        assertEquals("DTS", sectionByTypeName.getFieldValue("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(SectionType.GENERAL, null));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(SectionType.GENERAL, ""));

        Section sectionByName = info.getSection("Audio #1");
        assertNotNull(sectionByName);
        assertEquals("DTS", sectionByName.getFieldValue("Format"));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(null));
        assertThrows(IllegalArgumentException.class, () -> info.getSection(""));
    }

    @Test
    @DisplayName("Test hasSection by name, type, and type+name")
    void testHasSection() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        assertTrue(info.hasSection("Audio #1"));
        assertFalse(info.hasSection("test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection((String) null));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(""));

        assertTrue(info.hasSection(SectionType.AUDIO));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection((SectionType) null));

        assertTrue(info.hasSection(SectionType.AUDIO, "Audio #1"));
        assertFalse(info.hasSection(SectionType.AUDIO, "test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(SectionType.AUDIO, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasSection(SectionType.AUDIO, ""));
    }

    @Test
    @DisplayName("Test hasFieldName with various overloads")
    void testHasFieldName() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        assertTrue(info.hasFieldName(SectionType.AUDIO, "Audio #1", "Format"));
        assertFalse(info.hasFieldName(SectionType.AUDIO, "Audio #1", "test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, null, ""));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, "", null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(SectionType.AUDIO, "", ""));

        assertTrue(info.hasFieldName("Audio #1", "Format"));
        assertFalse(info.hasFieldName("Audio #1", "test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null, null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null, ""));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName("", null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName("", ""));

        assertTrue(info.hasFieldName("Format"));
        assertFalse(info.hasFieldName("test"));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(null));
        assertThrows(IllegalArgumentException.class, () -> info.hasFieldName(""));
    }

    @Test
    @DisplayName("Test dump method and its validation")
    void testDump() throws URISyntaxException, IOException {
        MediaInfo info = getMediaInfoFromText();
        assertThrows(IllegalArgumentException.class, () -> info.dump(null));
        assertDoesNotThrow(() -> info.dump(new OutputStreamWriter(System.out)));
    }

    @Test
    @DisplayName("Testing MediaInfo crc32 checksums")
    void crc32() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp4-file.mp4");
        assertNotNull(info);
        assertEquals(3, info.getSections().size());

        String checksum = info.calculateChecksum();
        assertNotEquals(0, checksum.length());
        assertEqualsIgnoreCase(MP4_CRC32_CHECKSUM, checksum);
        assertTrue(info.verifyChecksum(MP4_CRC32_CHECKSUM));
        assertFalse(info.verifyChecksum(shuffleString(MP4_CRC32_CHECKSUM)));
        assertThrows(IllegalArgumentException.class, () -> info.verifyChecksum(MP4_CRC32_CHECKSUM + 1));
    }

    @Test
    @DisplayName("Testing MediaInfo adler 32 checksums")
    void adler32() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp4-file.mp4");
        assertNotNull(info);
        assertEquals(3, info.getSections().size());

        String checksum = info.calculateChecksum(ChecksumAlgorithm.ADLER32);
        assertNotEquals(0, checksum.length());
        assertEqualsIgnoreCase(MP4_ADLER32_CHECKSUM, checksum);
        assertTrue(info.verifyChecksum(MP4_ADLER32_CHECKSUM));
        assertFalse(info.verifyChecksum(shuffleString(MP4_ADLER32_CHECKSUM)));
        assertThrows(IllegalArgumentException.class, () -> info.verifyChecksum(MP4_ADLER32_CHECKSUM + 1));
    }

    @Test
    @DisplayName("Testing MediaInfo md5 checksums")
    void md5() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp4-file.mp4");
        assertNotNull(info);
        assertEquals(3, info.getSections().size());

        String checksum = info.calculateChecksum(ChecksumAlgorithm.MD5);
        assertNotEquals(0, checksum.length());
        assertEqualsIgnoreCase(MP4_MD5_CHECKSUM, checksum);
        assertTrue(info.verifyChecksum(MP4_MD5_CHECKSUM));
        assertFalse(info.verifyChecksum(shuffleString(MP4_MD5_CHECKSUM)));
        assertThrows(IllegalArgumentException.class, () -> info.verifyChecksum(MP4_MD5_CHECKSUM + 1));
    }

    @Test
    @DisplayName("Testing MediaInfo sha1 checksums")
    void sha1() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp4-file.mp4");
        assertNotNull(info);
        assertEquals(3, info.getSections().size());

        String checksum = info.calculateChecksum(ChecksumAlgorithm.SHA1);
        assertNotEquals(0, checksum.length());
        assertEqualsIgnoreCase(MP4_SHA1_CHECKSUM, checksum);
        assertTrue(info.verifyChecksum(MP4_SHA1_CHECKSUM));
        assertFalse(info.verifyChecksum(shuffleString(MP4_SHA1_CHECKSUM)));
        assertThrows(IllegalArgumentException.class, () -> info.verifyChecksum(MP4_SHA1_CHECKSUM + 1));
    }

    @Test
    @DisplayName("Testing MediaInfo sha256 checksums")
    void sha256() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp4-file.mp4");
        assertNotNull(info);
        assertEquals(3, info.getSections().size());

        String checksum = info.calculateChecksum(ChecksumAlgorithm.SHA256);
        assertNotEquals(0, checksum.length());
        assertEqualsIgnoreCase(MP4_SHA256_CHECKSUM, checksum);
        assertTrue(info.verifyChecksum(MP4_SHA256_CHECKSUM));
        assertFalse(info.verifyChecksum(shuffleString(MP4_SHA256_CHECKSUM)));
        assertThrows(IllegalArgumentException.class, () -> info.verifyChecksum(MP4_SHA256_CHECKSUM + 1));
    }

    @Test
    @DisplayName("Testing MediaInfo sha512 checksums")
    void sha512() throws URISyntaxException {
        MediaInfo info = getMediaInfoFromFile("mp4-file.mp4");
        assertNotNull(info);
        assertEquals(3, info.getSections().size());

        String checksum = info.calculateChecksum(ChecksumAlgorithm.SHA512);
        assertNotEquals(0, checksum.length());
        assertEqualsIgnoreCase(MP4_SHA512_CHECKSUM, checksum);
        assertTrue(info.verifyChecksum(MP4_SHA512_CHECKSUM));
        assertFalse(info.verifyChecksum(shuffleString(MP4_SHA512_CHECKSUM)));
        assertThrows(IllegalArgumentException.class, () -> info.verifyChecksum(MP4_SHA512_CHECKSUM + 1));
    }

    private MediaInfo getMediaInfoFromFile(String resourceName) throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        assertNotNull(resource);

        File file = Paths.get(resource.toURI()).toFile();
        return parser.parseFile(file.getAbsolutePath());
    }

    private MediaInfo getMediaInfoFromText() throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource("full.txt");
        assertNotNull(resource);

        Path path = Paths.get(resource.toURI());
        String data = Files.readString(path);
        return parser.parseData(data);
    }

    private String shuffleString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        char[] characters = input.toCharArray();
        Random random = new Random();

        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];

            characters[i] = characters[j];
            characters[j] = temp;
        }

        return new String(characters);
    }

    private void assertEqualsIgnoreCase(String expected, String actual) {
        assertEquals(expected.toLowerCase(), actual.toLowerCase());
    }
}
