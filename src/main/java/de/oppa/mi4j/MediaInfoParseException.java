package de.oppa.mi4j;

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
 * Exception thrown when parsing media information fails.
 */
public final class MediaInfoParseException extends RuntimeException {

    /**
     * Create an exception with a message.
     *
     * @param message exception message
     */
    public MediaInfoParseException(String message) {
        super(message);
    }

    /**
     * Create an exception with a message and cause.
     *
     * @param message exception message
     * @param cause   root cause of the exception
     */
    public MediaInfoParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
