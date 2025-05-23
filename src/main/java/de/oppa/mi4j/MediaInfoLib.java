package de.oppa.mi4j;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

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
    /**
     * The singleton instance of the MediaInfoLib interface.
     * <p>
     * This instance is used to call the native methods defined in this interface.
     * </p>
     */
    MediaInfoLib INSTANCE = Native.load(
        Platform.isWindows() ? "MediaInfo" : (Platform.isMac() ? "media-info" : "mediainfo"),
        MediaInfoLib.class
    );

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
