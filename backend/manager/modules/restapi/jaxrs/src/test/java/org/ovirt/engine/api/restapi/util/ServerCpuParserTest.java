package org.ovirt.engine.api.restapi.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.ServerCpu;

import static org.ovirt.engine.api.restapi.util.ServerCpuParser.parseCpus;

public class ServerCpuParserTest extends Assert {

    @Test
    public void testParseEmpty() {
        List<ServerCpu> cpus = parseCpus("");
        assertNotNull(cpus);
        assertEquals(0, cpus.size());
    }

    @Test
    public void testParseSingle() {
        List<ServerCpu> cpus = parseCpus("0:foo:one,two,three:blue");
        assertNotNull(cpus);
        assertEquals(1, cpus.size());
        verifyCpu(cpus.get(0), 0, "foo", "blue", "one", "two", "three");
    }

    @Test
    public void testParseMultiple() {
        List<ServerCpu> cpus = parseCpus("0:foo:one,two,three:blue;99:bar foo:four,five,six:pink");
        assertNotNull(cpus);
        assertEquals(2, cpus.size());
        verifyCpu(cpus.get(0), 0, "foo", "blue", "one", "two", "three");
        verifyCpu(cpus.get(1), 99, "bar foo", "pink", "four", "five", "six");
    }

    @Test
    public void testParseStraySemiColons() {
        List<ServerCpu> cpus = parseCpus(";;0:foo:one,two,three:blue;;");
        assertNotNull(cpus);
        assertEquals(1, cpus.size());
        verifyCpu(cpus.get(0), 0, "foo", "blue", "one", "two", "three");
    }

    @Test
    public void testParseStrayCommas() {
        List<ServerCpu> cpus = parseCpus("0:foo:,,one,two,,,three,,:blue");
        assertNotNull(cpus);
        assertEquals(1, cpus.size());
        verifyCpu(cpus.get(0), 0, "foo", "blue", "one", "two", "three");
    }

    @Test
    public void testParseNoFlags() {
        List<ServerCpu> cpus = parseCpus("0:foo::blue");
        assertNotNull(cpus);
        assertEquals(1, cpus.size());
        verifyCpu(cpus.get(0), 0, "foo", "blue");
    }

    @Test
    public void testParseNoNameFlagsOrVerb() {
        List<ServerCpu> cpus = parseCpus("0:::");
        assertNotNull(cpus);
        assertEquals(1, cpus.size());
        verifyCpu(cpus.get(0), 0, "", "");
    }

    @Test
    public void testParseInvalid() {
        try {
            parseCpus("0::");
            fail("expected exception");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testParseInvalidLevel() {
        try {
            parseCpus("!:::");
            fail("expected exception");
        } catch (NumberFormatException ex) {
        }
    }

    private void verifyCpu(ServerCpu cpu, int level, String name, String verb, String... flags) {
        assertEquals(level, cpu.getLevel());
        assertEquals(name, cpu.getCpuName());
        assertEquals(verb, cpu.getVdsVerbData());
        assertNotNull(cpu.getFlags());
        assertEquals(flags.length, cpu.getFlags().size());
        for (String f : flags) {
            assertTrue(cpu.getFlags().contains(f));
        }
    }
}
