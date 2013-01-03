package org.ovirt.engine.api.restapi.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.Option;

import static org.ovirt.engine.api.restapi.util.FenceOptionsParser.parse;

public class FenceOptionsParserTest extends Assert {

    @Test
    public void testParseEmpty() {
        List<PowerManagement> ret = parse("", "");
        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    @Test
    public void testParseSingle() {
        List<PowerManagement> ret = parse("foo:one=1,two=2,three=3", "one=bool,two=int,three=bool");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0),
                     "foo",
                     "one",   "bool", "1",
                     "two",   "int",  "2",
                     "three", "bool", "3");
    }

    @Test
    public void testParseMultiple() {
        List<PowerManagement> ret = parse("foo:one=1,two=2,three=3;bar:ninetynine=99",
                                          "one=bool,two=int,three=bool,ninetynine=int");
        assertNotNull(ret);
        assertEquals(2, ret.size());
        verifyResult(ret.get(0),
                     "foo",
                     "one",   "bool", "1",
                     "two",   "int",  "2",
                     "three", "bool", "3");
        verifyResult(ret.get(1),
                     "bar",
                     "ninetynine", "int", "99");
    }

    @Test
    public void testParseIngoreValues() {
        List<PowerManagement> ret = parse("foo:one=1,two=2,three=3", "one=bool,two=int,three=bool", true);
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0),
                     "foo",
                     "one",   "bool", null,
                     "two",   "int",  null,
                     "three", "bool", null);
    }

    @Test
    public void testParseStraySemiColons() {
        List<PowerManagement> ret = parse(";;foo:one=1,two=2,three=3;;", "one=bool,two=int,three=bool");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0),
                     "foo",
                     "one",   "bool", "1",
                     "two",   "int",  "2",
                     "three", "bool", "3");
    }

    @Test
    public void testParseStrayCommas() {
        List<PowerManagement> ret = parse("foo:,,one=1,,,two=2,,three=3,,", "one=bool,two=int,three=bool");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0),
                     "foo",
                     "one",   "bool", "1",
                     "two",   "int",  "2",
                     "three", "bool", "3");
    }

    @Test
    public void testParseNoOptions() {
        List<PowerManagement> ret = parse("foo:", "");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0), "foo");
    }

    @Test
    public void testParseMissingType() {
        try {
            parse("foo:one=1", "two=int");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            System.out.println(iae);
        }
    }

    @Test
    public void testParseStrayColons() {
        try {
            parse("foo:::one=1,two=2,three=3", "one=bool,two=int,three=bool");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testParseInvalidOption() {
        try {
            parse("foo:one=1,two=2,three", "one=bool,two=int,three=bool");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
        }
    }

    private void verifyResult(PowerManagement result, String type, String... options) {
        assertEquals(type, result.getType());
        assertNotNull(result.getOptions());
        assertEquals(options.length, result.getOptions().getOptions().size() * 3);
        for (int i = 0; i < options.length; i += 3) {
            Option opt = result.getOptions().getOptions().get(i/3);
            assertEquals(options[i], opt.getName());
            assertEquals(options[i+1], opt.getType());
            assertEquals(options[i+2], opt.getValue());
        }
    }
}
