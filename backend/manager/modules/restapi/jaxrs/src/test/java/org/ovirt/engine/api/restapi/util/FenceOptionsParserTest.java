package org.ovirt.engine.api.restapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.ovirt.engine.api.restapi.util.FenceOptionsParser.parse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Option;

public class FenceOptionsParserTest {
    private static final String FENCE_OPTION = "apc:secure=secure,port=ipport,slot=port;apc_snmp:port=port,"
            + "encrypt_options=encrypt_options;bladecenter:secure=secure,port=ipport,slot=port;cisco_ucs:secure=ssl,"
            + "slot=port;drac5:secure=secure,slot=port;drac7:;eps:slot=port;hpblade:port=port;ilo:secure=ssl,"
            + "port=ipport;ipmilan:;ilo2:secure=ssl,port=ipport;ilo3:;ilo4:;rsa:secure=secure,port=ipport;rsb:;"
            + "wti:secure=secure,port=ipport,slot=port";
    private static final String FENCE_OPTION_TYPES = "encrypt_options=bool,secure=bool,port=int,slot=int";

    @Test
    public void testParseEmpty() {
        List<Agent> ret = parse("", "");
        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    @Test
    public void testParseSingle() {
        List<Agent> ret = parse("foo:one=1,two=2,three=3", "one=bool,two=int,three=bool");
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
        List<Agent> ret = parse("foo:one=1,two=2,three=3;bar:ninetynine=99",
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
        List<Agent> ret = parse("foo:one=1,two=2,three=3", "one=bool,two=int,three=bool", true);
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
        List<Agent> ret = parse(";;foo:one=1,two=2,three=3;;", "one=bool,two=int,three=bool");
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
        List<Agent> ret = parse("foo:,,one=1,,,two=2,,three=3,,", "one=bool,two=int,three=bool");
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
        List<Agent> ret = parse("foo:", "");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0), "foo");
    }

    @Test
    public void testParseSecureOptions() {
        List<Agent> ret = parse(FENCE_OPTION, FENCE_OPTION_TYPES);
        assertNotNull(ret);
        assertEquals(16, ret.size());
        verifyResult(ret.get(1), "apc_snmp", "port", "int", "port", "encrypt_options", "bool", "encrypt_options");
    }

    @Test
    public void testParseMissingType() {
        List<Agent> ret = parse("foo:one=1", "two=int");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0), "foo");
    }

    @Test
    public void testParseStrayColons() {
        List<Agent> ret = parse("foo:::one=1,two=2,three=3", "one=bool,two=int,three=bool");
        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    @Test
    public void testParseInvalidOption() {
        List<Agent> ret = parse("foo:one=1,two=2,three", "one=bool,two=int,three=bool");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        verifyResult(ret.get(0),
                     "foo",
                     "one", "bool", "1",
                     "two", "int", "2");
    }

    private void verifyResult(Agent result, String type, String... options) {
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
