package de.oppa.mi4j;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

import java.io.IOException;
import java.util.Set;

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
 * MediaInfoLib interface for native library access.
 * <p>
 * This interface defines the methods for interacting with the MediaInfo library.
 * It uses JNA (Java Native Access) to load the native library and provide
 * access to its functions.
 */
public interface MediaInfoLib extends Library {
    Set<String> SUPPORTED_EXTENSIONS = Set.of(
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
     * Loads the MediaInfo library.
     * <p>
     * This method loads the MediaInfo library using JNA. It is called automatically
     * when the class is loaded.
     * </p>
     *
     * @return An instance of the MediaInfoLib interface.
     */
    static MediaInfoLib getInstance() {
        try {
            NativeLibraryLoader.loadMediaInfoLibrary();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load MediaInfo library", e);
        }

        MediaInfoLib instance = Native.load(Platform.isWindows() ? "MediaInfo" : "mediainfo", MediaInfoLib.class);
        if (instance == null) {
            throw new IllegalStateException("Failed to load MediaInfo library");
        }

        return instance;
    }

    /**
     * Creates a new MediaInfo handle.
     * <p>
     * This method initializes a new MediaInfo handle for use in extracting metadata.
     * </p>
     *
     * @return A pointer to the newly created MediaInfo handle.
     */
    Pointer MediaInfo_New();

    /**
     * Deletes the MediaInfo handle.
     * <p>
     * This method releases the resources associated with the MediaInfo handle.
     * </p>
     *
     * @param handle A pointer to the MediaInfo handle to be deleted.
     */
    void MediaInfo_Delete(Pointer handle);

    /**
     * Opens a media file for analysis.
     * <p>
     * This method opens the specified media file for analysis using the MediaInfo handle.
     * </p>
     *
     * @param handle   A pointer to the MediaInfo handle.
     * @param filename The path to the media file to be opened.
     * @return 1 if successful, 0 otherwise.
     */
    int MediaInfo_Open(Pointer handle, WString filename);

    /**
     * Closes the media file.
     * <p>
     * This method closes the currently opened media file in the MediaInfo handle.
     * </p>
     *
     * @param handle A pointer to the MediaInfo handle.
     */
    void MediaInfo_Close(Pointer handle);

    /**
     * Sets an option for the MediaInfo handle.
     * <p>
     * This method sets a specific option for the MediaInfo handle.
     * </p>
     *
     * @param handle    A pointer to the MediaInfo handle.
     * @param parameter The name of the option to set.
     * @param value     The value to set for the option.
     */
    void MediaInfo_Option(Pointer handle, WString parameter, WString value);

    /**
     * Retrieves information about the media file.
     * <p>
     * This method retrieves metadata information about the currently opened media file.
     * </p>
     *
     * @param handle A pointer to the MediaInfo handle.
     * @return A WString containing the metadata information.
     */
    WString MediaInfo_Inform(Pointer handle);
}
