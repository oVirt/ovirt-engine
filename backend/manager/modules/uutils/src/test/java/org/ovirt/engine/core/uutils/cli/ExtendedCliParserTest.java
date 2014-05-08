package org.ovirt.engine.core.uutils.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ovirt.engine.core.uutils.cli.ExtendedCliParser.VALUE_SEP_LONG;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Parsing short and long arguments with {@code ExtendedCliParser} tests
 */
@RunWith(JUnit4.class)
public class ExtendedCliParserTest {
    /**
     * Tests parsing empty arguments
     */
    @Test
    public void emptyArgs() {
        Map<String, String> results;
        ExtendedCliParser parser = new ExtendedCliParser();

        final String[] args = {};

        results = parser.parse(args);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * Tests parsing short argument without value
     */
    @Test
    public void shortArgWithoutValue() {
        Map<String, String> results;
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName = "-h";

        final String[] args = { argName };

        parser.addArg(new ArgumentBuilder().shortName(argName).build());
        results = parser.parse(args);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        assertTrue(results.containsKey(argName));
        assertNull(results.get(argName));
    }

    /**
     * Tests parsing short argument with value
     */
    @Test
    public void shortArgWithValue() {
        Map<String, String> results;
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName = "-f";
        String argValue = "/tmp/xxx.txt";

        final String[] args = { argName, argValue };

        parser.addArg(new ArgumentBuilder().shortName(argName).build());
        results = parser.parse(args);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        assertTrue(results.containsKey(argName));
        assertEquals(argValue, results.get(argName));
    }

    /**
     * Tests parsing long argument without value
     */
    @Test
    public void longArgWithoutValue() {
        Map<String, String> results;
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName = "--help";

        final String[] args = { argName };

        parser.addArg(new ArgumentBuilder().longName(argName).build());
        results = parser.parse(args);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        assertTrue(results.containsKey(argName));
        assertNull(results.get(argName));
    }

    /**
     * Tests parsing long argument with value
     */
    @Test
    public void longArgWithValue() {
        Map<String, String> results;
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName = "--file";
        String argValue = "/tmp/xxx.txt";

        final String[] args = { argName + VALUE_SEP_LONG + argValue };

        parser.addArg(new ArgumentBuilder().longName(argName).build());
        results = parser.parse(args);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        assertTrue(results.containsKey(argName));
        assertEquals(argValue, results.get(argName));
    }

    /**
     * Tests parsing short argument with value delimited by long argument value separator
     */
    @Test(expected = IllegalArgumentException.class)
    public void shortArgWithLongValueSep() {
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName = "-f";
        String argValue = "/tmp/xxx.txt";

        final String[] args = { argName + VALUE_SEP_LONG + argValue };

        parser.addArg(new ArgumentBuilder().shortName(argName).build());
        parser.parse(args);
    }

    /**
     * Tests parsing long argument with value delimited by short argument value separator
     */
    @Test(expected = IllegalArgumentException.class)
    public void longArgWithShortValueSep() {
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName = "--file";
        String argValue = "/tmp/xxx.txt";

        final String[] args = { argName, argValue };

        parser.addArg(new ArgumentBuilder().longName(argName).build());
        parser.parse(args);
    }

    /**
     * Tries to parse argument with specified invalid name
     *
     * @param name
     *            invalid argument name
     */
    protected void invalidArgName(String name) {
        try {
            ExtendedCliParser parser = new ExtendedCliParser();
            parser.parse(new String[] {name});
            fail("IllegalArgumentException expected for '" + name + "' argument!");
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * Tests arguments with invalid names
     */
    @Test
    public void invalidArgName() {
        invalidArgName("\t ");
        invalidArgName("A");
        invalidArgName("- ");
        invalidArgName("-test");
        invalidArgName("--");
        invalidArgName("--?");
        invalidArgName("--force=");
    }

    /**
     * Tests parsing argument only in selected range
     */
    @Test
    public void parseArgsInRange() {
        Map<String, String> results;
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName1 = "--help";
        String argName2 = "-f";
        String argName3 = "--output";
        String argValue3 = "/dev/null";
        String argName4 = "-u";
        String argValue4 = "root";

        final String[] args = {
                argName1,
                argName2,
                argName3 + VALUE_SEP_LONG + argValue3,
                argName4,
                argValue4
        };

        parser.addArg(new ArgumentBuilder().shortName(argName2).build());
        parser.addArg(new ArgumentBuilder().longName(argName3).build());
        results = parser.parse(args, 1, 3);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        assertTrue(results.containsKey(argName2));

        assertTrue(results.containsKey(argName3));
        assertEquals(argValue3, results.get(argName3));
    }

    /**
     * Tests parsing argument with missing required value
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseArgWithMissingRequiredValue() {
        ExtendedCliParser parser = new ExtendedCliParser();
        String argName = "-f";

        final String[] args = { argName };

        parser.addArg(new ArgumentBuilder().shortName(argName).valueRequired(true).build());
        parser.parse(args);
    }

    /**
     * Tests adding two arguments with the same short name
     */
    @Test(expected = IllegalArgumentException.class)
    public void twoArgsWithSameShortName() {
        ExtendedCliParser parser = new ExtendedCliParser();

        parser.addArg(new ArgumentBuilder()
                .shortName("-a")
                .longName("--aa")
                .valueRequired(true)
                .build());

        parser.addArg(new ArgumentBuilder()
                .shortName("-a")
                .longName("--bb")
                .valueRequired(true)
                .build());
    }

    /**
     * Tests adding two arguments with the same long name
     */
    @Test(expected = IllegalArgumentException.class)
    public void twoArgsWithSameLongName() {
        ExtendedCliParser parser = new ExtendedCliParser();

        parser.addArg(new ArgumentBuilder()
                .shortName("-a")
                .longName("--aa")
                .valueRequired(true)
                .build());

        parser.addArg(new ArgumentBuilder()
                .shortName("-b")
                .longName("--aa")
                .valueRequired(true)
                .build());
    }
}
