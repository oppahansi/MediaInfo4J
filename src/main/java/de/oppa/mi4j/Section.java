package de.oppa.mi4j;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
 * Section class to hold field-value pairs.
 * <p>
 * This class provides methods to add field-value pairs, retrieve values,
 * and iterate over the fields.
 * </p>
 */
public final class Section implements Iterable<String> {
    /**
     * Map to hold field-value pairs.
     * <p>
     * The map is sorted by field names for consistent ordering.
     * </p>
     */
    private final Map<String, String> fieldToValue = new TreeMap<>();

    /**
     * Add a field-value pair to this section.
     *
     * @param field the field key
     * @param value the field value
     */
    public void addFieldValue(String field, String value) {
        fieldToValue.put(field, value);
    }

    /**
     * Get the unmodifiable map of field key-value pairs.
     *
     * @return unmodifiable map of field values
     */
    public Map<String, String> getFieldValues() {
        return Collections.unmodifiableMap(fieldToValue);
    }

    /**
     * Get the value for a specific key.
     *
     * @param field the field key
     * @return the value, or null if not found
     */
    public String getFieldValue(String field) {
        return fieldToValue.get(field);
    }

    /**
     * Get the value for a specific key, or return a default value if not found.
     *
     * @param fieldName the field key
     * @return the value, or the default value if not found
     */
    public boolean hasField(String fieldName) {
        return fieldToValue.containsKey(fieldName);
    }

    /**
     * Get the set of field names in this section.
     *
     * @return the set of field names
     */
    public Set<String> getFieldNames() {
        return fieldToValue.keySet();
    }

    /**
     * Get the list of values in this section.
     *
     * @return the list of values
     */
    public List<String> getValues() {
        return fieldToValue.values().stream().toList();
    }

    @Override
    public Iterator<String> iterator() {
        return fieldToValue.keySet().iterator();
    }

    @Override
    public String toString() {
        return "%s[fieldToValue=%s]".formatted(getClass().getSimpleName(), fieldToValue);
    }

}
