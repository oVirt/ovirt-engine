package org.ovirt.engine.core.notifier.transport.snmp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.utils.Pair;
import org.snmp4j.mp.SnmpConstants;

public class ProfileTest {

    // this has to be defined as paris since Snmp.Hosts doesn't distinguish between Host(x, null) to Host(x, 162)
    private static final List<Pair<String, String>> hosts = new ArrayList<>(
        Arrays.asList(
            new Pair<>("[::1]", null),
            new Pair<>("[::1]", "1"),
            new Pair<>("localhost", null),
            new Pair<>("localhost", "162"),
            new Pair<>("192.168.0.1", null),
            new Pair<>("192.168.0.1", "163"),
            new Pair<>("manager1.example.com", null),
            new Pair<>("manager1.example.com", "164"),
            new Pair<>("[FE80:0000:0000:0000:0202:B3FF:FE1E:8329]", null),
            new Pair<>("[FE80:0000:0000:0000:0202:B3FF:FE1E:8329]", "165")
        )
    );


    @Test
    public void testSnmpManagersParsing() {
        List<Snmp.Host> expectedManagers = new ArrayList<>();
        StringBuilder snmpManagers = new StringBuilder();
        for (Pair<String, String> host : hosts) {
            expectedManagers.add(new Snmp.Host(host.getFirst(), host.getSecond()));
            snmpManagers.append(host.getFirst());
            if (host.getSecond() != null) {
                snmpManagers.append(":").append(host.getSecond());
            }
            snmpManagers.append(" ");
        }
        Snmp.Profile profile = Snmp.Profile.buildProfile(
            snmpManagers.toString(),
            "public",
            "1.3.6.1.4.1.2312.13.1.1",
            SnmpConstants.version2c);
        for (int i = 0; i < expectedManagers.size(); i++) {
            Snmp.Host parsed = profile.hosts.get(i);
            Snmp.Host expected = expectedManagers.get(i);
            assertEquals(parsed.name, expected.name);
            assertEquals(parsed.port, expected.port);
        }
    }


}
