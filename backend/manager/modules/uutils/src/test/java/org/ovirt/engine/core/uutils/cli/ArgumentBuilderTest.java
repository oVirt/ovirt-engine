package org.ovirt.engine.core.uutils.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for argument creation used by {@link ExtendedCliParser}
 */
@RunWith(JUnit4.class)
public class ArgumentBuilderTest {
    /**
     * Tests creating argument without value required specified
     */
    @Test
    public void createValidArgWithoutRequiredSpecified() {
        String shortName = "-h";
        String longName = "--help";
        String destination = "dest";

        Argument arg = new ArgumentBuilder()
                .shortName(shortName)
                .longName(longName)
                .destination(destination)
                .build();

        assertEquals(shortName, arg.getShortName());
        assertEquals(longName, arg.getLongName());
        assertEquals(destination, arg.getDestination());
        assertFalse(arg.isValueRequied());
    }

    /**
     * Tests creating argument with value required specified
     */
    @Test
    public void createValidArgWithRequiredSpecified() {
        String shortName = "-h";
        String longName = "--help";
        String destination = "dest";

        Argument arg = new ArgumentBuilder()
                .shortName(shortName)
                .longName(longName)
                .destination(destination)
                .valueRequired(true)
                .build();

        assertEquals(shortName, arg.getShortName());
        assertEquals(longName, arg.getLongName());
        assertEquals(destination, arg.getDestination());
        assertTrue(arg.isValueRequied());
    }

    /**
     * Tests creating argument with only short name
     */
    @Test
    public void createValidArgWithOnlyShortName() {
        String shortName = "-h";

        Argument arg = new ArgumentBuilder()
                .shortName(shortName)
                .build();

        assertEquals(shortName, arg.getShortName());
    }

    /**
     * Tests creating argument with only long name
     */
    @Test
    public void createValidArgWithOnlyLongName() {
        String longName = "--help";

        Argument arg = new ArgumentBuilder()
                .longName(longName)
                .build();

        assertEquals(longName, arg.getLongName());
    }

    /**
     * Tests creating argument without short and long name
     */
    @Test(expected = IllegalArgumentException.class)
    public void createArgWithoutShortAndLongNames() {
        new ArgumentBuilder().destination("dest").build();
    }

    /**
     * Tests destination defined by destination option
     */
    @Test
    public void parseArgsWithDestDefinedByDest() {
        String shortName = "-h";
        String longName = "--help";
        String destination = "dest";

        Argument arg = new ArgumentBuilder()
                .shortName(shortName)
                .longName(longName)
                .destination(destination)
                .build();

        assertEquals(destination, arg.getDestination());
    }

    /**
     * Tests destination defined by long name option
     */
    @Test
    public void parseArgsWithDestDefinedByLongName() {
        String shortName = "-h";
        String longName = "--help";

        Argument arg = new ArgumentBuilder()
                .shortName(shortName)
                .longName(longName)
                .build();

        assertEquals(longName, arg.getDestination());
    }

    /**
     * Tests destination defined by short name option
     */
    @Test
    public void parseArgsWithDestDefinedByShortName() {
        String shortName = "-h";

        Argument arg = new ArgumentBuilder()
                .shortName(shortName)
                .build();

        assertEquals(shortName, arg.getDestination());
    }
}
