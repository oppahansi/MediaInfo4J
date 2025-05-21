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

public enum ChecksumAlgorithm {
    CRC32("CRC32"),
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256"),
    SHA512("SHA-512"),
    ADLER32("Adler-32");

    private final String name;

    ChecksumAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
