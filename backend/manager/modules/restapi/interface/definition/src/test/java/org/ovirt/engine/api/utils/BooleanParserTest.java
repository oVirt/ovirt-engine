/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BooleanParserTest {
    /**
     * Check that the parser accepts and parses correctly the canonical boolean values.
     */
    @Test
    public void testAcceptsBooleans() {
        assertEquals(BooleanParser.parseBoolean("true"), true);
        assertEquals(BooleanParser.parseBoolean("false"), false);
    }

    /**
     * Check that the parser accepts and parses collectly the values {@code 0} and {@code 1}, as these are also
     * valid according to <a href="http://www.w3.org/TR/xmlschema-2/#boolean">section 3.2.2.1</a> of the XML schema
     * specification.
     */
    @Test
    public void testAccepts01() {
        assertEquals(BooleanParser.parseBoolean("1"), true);
        assertEquals(BooleanParser.parseBoolean("0"), false);
    }

    /**
     * Check that the parser ignores case for the canonical boolean values.
     */
    @Test
    public void testIgnoresCase() {
        for (String value : getCaseCombinations("true")) {
            assertEquals(BooleanParser.parseBoolean(value), true);
        }
        for (String value : getCaseCombinations("false")) {
            assertEquals(BooleanParser.parseBoolean(value), false);
        }
    }

    /**
     * Check that invalid values are rejected.
     */
    @Test(expected = InvalidValueException.class)
    public void testRejectsJunk() {
        BooleanParser.parseBoolean("junk");
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
