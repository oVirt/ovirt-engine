/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class BooleanParserTest {
    /**
     * Check that the parser accepts and parses correctly the canonical boolean values.
     */
    @Test
    public void testAcceptsBooleans() {
        assertTrue(BooleanParser.parseBoolean("true"));
        assertFalse(BooleanParser.parseBoolean("false"));
    }

    /**
     * Check that the parser accepts and parses collectly the values {@code 0} and {@code 1}, as these are also
     * valid according to <a href="http://www.w3.org/TR/xmlschema-2/#boolean">section 3.2.2.1</a> of the XML schema
     * specification.
     */
    @Test
    public void testAccepts01() {
        assertTrue(BooleanParser.parseBoolean("1"));
        assertFalse(BooleanParser.parseBoolean("0"));
    }

    /**
     * Check that the parser ignores case for the canonical boolean values.
     */
    @Test
    public void testIgnoresCase() {
        for (String value : getCaseCombinations("true")) {
            assertTrue(BooleanParser.parseBoolean(value));
        }
        for (String value : getCaseCombinations("false")) {
            assertFalse(BooleanParser.parseBoolean(value));
        }
    }

    /**
     * Check that invalid values are rejected.
     */
    @Test
    public void testRejectsJunk() {
        assertThrows(InvalidValueException.class, () -> BooleanParser.parseBoolean("junk"));
    }

    /**
     * Given a string produces all the different case combinations that can be used to produce it. For example, if
     * given the string {@code true} it will produce the strings {@code true}, {@code True}, {@code tRue}, {@code TRue},
     * etc. Note that the number of combinations is 2^length, so refrain from using it for large strings as otherwise
     * it may take ages and consume all the available memory.
     */
    private List<String> getCaseCombinations(String text) {
        // There are 2^length case combinations:
        int length = text.length();
        int combinations = 1 << length;

        // For each combination change to uppercase the characters whose bit position are to 1 in the combination index:
        List<String> result = new ArrayList<>(combinations);
        char[] characters = text.toCharArray();
        for (int combinationIndex = 0; combinationIndex < combinations; combinationIndex++) {
            for (int bitIndex = 0; bitIndex < length; bitIndex++) {
                int bitValue = combinationIndex & (1 << bitIndex);
                char character = characters[bitIndex];
                character = bitValue == 0? Character.toLowerCase(character): Character.toUpperCase(character);
                characters[bitIndex] = character;
            }
            result.add(new String(characters));
        }
        return result;
    }
}
