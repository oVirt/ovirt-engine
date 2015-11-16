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

package org.ovirt.api.metamodel.concepts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NameTest {
    /**
     * Check simple alphabetical comparison.
     */
    @Test
    public void testCompareSimple() {
        testCompare("A", "B", -1);
        testCompare("A", "A", 0);
        testCompare("B", "A", 1);
    }

    /**
     * Check that the second word is taken into account if the first word is equal.
     */
    @Test
    public void testCompareFirstWordEqual() {
        testCompare("AaAa", "AaBb", -1);
    }

    /**
     * Check that the second word isn't taken into account if the first word is different.
     */
    @Test
    public void testCompareFirstWordDifferent() {
        testCompare("AaBb", "BbAa", -1);
    }

    /**
     * Check that shorter words are sorted before longer words.
     */
    @Test
    public void testCompareShorterWordBeforeLongerWord() {
        testCompare("Aa", "Aaa", -1);
    }

    /**
     * Check that shorter names (with less words) are sorted before longer names.
     */
    @Test
    public void testCompareShorterNameBeforeLongerWord() {
        testCompare("Aa", "AaBb", -1);
    }

    /**
     * Converts the given strings to names and compares the result of comparing them with the given expected result.
     *
     * @param left the first name compared
     * @param right the second name compoared
     * @param expected the expected result of the {@link Name#compareTo(Name)} method
     */
    private void testCompare(String left, String right, int expected) {
        // Create names from the given strings:
        Name leftName = NameParser.parseUsingCase(left);
        Name rightName = NameParser.parseUsingCase(right);

        // Create a message for the assertion, containing the two compared names:
        StringBuilder buffer = new StringBuilder();
        buffer.append(leftName);
        buffer.append(" ");
        buffer.append(rightName);
        String message = buffer.toString();

        // Perform the comparison, and convert the result to predictable values, as the "compareTo" can return any
        // negative or positive number:
        int actual = leftName.compareTo(rightName);
        if (actual < 0) {
            actual = -1;
        }
        else if (actual > 0) {
            actual = 1;
        }

        // Perform the assertion:
        assertEquals(message, expected, actual);
    }
}
