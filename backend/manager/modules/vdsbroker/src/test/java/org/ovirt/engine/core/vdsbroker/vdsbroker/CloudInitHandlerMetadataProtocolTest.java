package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.businessentities.network.CloudInitNetworkProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.JsonHelper;

/**
 * Unit test for different permutations of the Openstack Metadata Service protocol.
 * The tests verify the network configuration output of the {@link CloudInitHandler} for a
 * given network configuration input.
 */
public class CloudInitHandlerMetadataProtocolTest {

    private static final String IFACE_NAME = "iface name";
    private static final String IPV4_ADDRESS = "ipv4 address";
    private static final String IPV4_NETMASK = "ipv4 netmask";
    private static final String IPV4_GATEWAY = "ipv4 gateway";
    private static final String IPV6_ADDRESS = "ipv6 address";
    private static final String IPV6_GATEWAY = "ipv6 gateway";
    private static final int IPV6_PREFIX = 666;

    public static Stream<Arguments> params() {

        Pair noneAndNone = noneAndNone();
        Pair staticIPv4 = staticIPv4();
        Pair staticIPv6 = staticIPv6();
        Pair staticIPv6AddressOnly = staticIPv6AddressOnly();
        Pair staticIPv4AndIPv6 = staticIPv4AndIPv6();
        Pair dhcpIPv4 = dhcpIPv4();
        Pair dhcpIPv6 = dhcpIPv6();
        Pair staticIPv4WithDns = staticIPv4WithDns();
        Pair dnsServersOnly = dnsServersOnly();
        Pair startOnBootFalse = startOnBootFalse();

        return Stream.of(
                Arguments.of(noneAndNone.getFirst(), noneAndNone.getSecond()),
                Arguments.of(staticIPv4.getFirst(), staticIPv4.getSecond()),
                Arguments.of(staticIPv6.getFirst(), staticIPv6.getSecond()),
                Arguments.of(staticIPv6AddressOnly.getFirst(), staticIPv6AddressOnly.getSecond()),
                Arguments.of(staticIPv4AndIPv6.getFirst(), staticIPv4AndIPv6.getSecond()),
                Arguments.of(dhcpIPv4.getFirst(), dhcpIPv4.getSecond()),
                Arguments.of(dhcpIPv6.getFirst(), dhcpIPv6.getSecond()),
                Arguments.of(staticIPv4WithDns.getFirst(), staticIPv4WithDns.getSecond()),
                Arguments.of(dnsServersOnly.getFirst(), dnsServersOnly.getSecond()),
                Arguments.of(startOnBootFalse.getFirst(), startOnBootFalse.getSecond())
        );
    }

    @ParameterizedTest
    @MethodSource("params")
    public void test(VmInit vmInit, Object expected) {
        vmInit.setCloudInitNetworkProtocol(CloudInitNetworkProtocol.OPENSTACK_METADATA);
        CloudInitHandler underTest = new CloudInitHandler(vmInit);
        try {
            Map<String, byte[]> actual = underTest.getFileData();
            if (actual.get("openstack/latest/network_data.json") == null) {
                assertNull(expected);
            } else {
                Map<String, Object> actualNetworkData = parseResult(actual);
                Map<String, Object> expectedNetworkData = JsonHelper.jsonToMap((String) expected);
                assertEquals(expectedNetworkData, actualNetworkData);
                assertEquals(0, underTest.validate(vmInit).size());
            }
        } catch (Exception e) {
            assertEquals(((Exception)expected).getMessage(), e.getMessage());
            assertEquals(((Exception)expected).getCause().getMessage(), e.getCause().getMessage());
            assertNotEquals(0, underTest.validate(vmInit).size());
        }
    }

    private Map<String, Object> parseResult(Map<String, byte[]> actual) throws IOException {
        final byte[] actualJson = actual.get("openstack/latest/network_data.json");
        final String jsonString = new String(actualJson, "US-ASCII");
        return JsonHelper.jsonToMap(jsonString);
    }

    /**
     * ENI after fix 1464043:
     * payload:
     * "network-interfaces" : "iface eth0 inet static\n  address 192.168.122.180\n  netmask 255.255.255.0\n  gateway 192.168.122.1\n"
     *
     * cloud-init-output.log: ok
     *
     * virt-cat img:/etc/sysconfig/network-scripts/ifcfg-eth0:
     * BOOTPROTO=static
     * DEVICE=eth0
     * IPADDR=192.168.122.182
     * NETMASK=255.255.255.0
     * ONBOOT=yes
     * TYPE=Ethernet
     * USERCTL=no
     *
     * ----------------------------------------------------------
     *
     */
    private static Pair<VmInit, String> staticIPv4() {
        final VmInitNetwork underTest = new VmInitNetwork();
        underTest.setName(IFACE_NAME);
        underTest.setBootProtocol(Ipv4BootProtocol.STATIC_IP);
        underTest.setIp(IPV4_ADDRESS);
        underTest.setNetmask(IPV4_NETMASK);
        underTest.setGateway(IPV4_GATEWAY);
        underTest.setStartOnBoot(true);
        VmInit vmInit = new VmInit();
        vmInit.setNetworks(Collections.singletonList(underTest));

        String expectedOutput = "{\n"
                + "  \"links\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"vif\",\n"
                + "      \"name\": \"iface name\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"networks\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"ipv4\",\n"
                + "      \"link\": \"iface name\",\n"
                + "      \"ip_address\": \"ipv4 address\",\n"
                + "      \"netmask\": \"ipv4 netmask\",\n"
                + "      \"gateway\": \"ipv4 gateway\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    /**
     * payload:
     * {"links":[{"name":"eth0","id":"eth0","type":"vif"}],"networks":[{"link":"eth0","id":"eth0","ip_address":"2001:cdba::3257:9652","type":"ipv6"}]}
     *
     * cloud-init-output.log: ok
     * but cannot login to machine (because i dont have ipv6 configured on my local machine?)
     *
     * virt-cat img:/etc/sysconfig/network-scripts/ifcfg-eth0
     * BOOTPROTO=static
     * DEVICE=eth0
     * IPV6ADDR=2001:cdba::3257:9652
     * IPV6INIT=yes
     * ONBOOT=yes
     * TYPE=Ethernet
     * USERCTL=no
     *
     *
     */
    private static Pair<VmInit, String> staticIPv6AddressOnly() {
        final VmInitNetwork underTest = new VmInitNetwork();
        underTest.setName(IFACE_NAME);
        underTest.setIpv6BootProtocol(Ipv6BootProtocol.STATIC_IP);
        underTest.setIpv6Address(IPV6_ADDRESS);
        underTest.setStartOnBoot(true);
        VmInit vmInit = new VmInit();
        vmInit.setNetworks(Collections.singletonList(underTest));

        String expectedOutput = "{\n"
                + "  \"links\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"vif\",\n"
                + "      \"name\": \"iface name\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"networks\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"ipv6\",\n"
                + "      \"link\": \"iface name\",\n"
                + "      \"ip_address\": \"ipv6 address\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    private static Pair<VmInit, String> staticIPv6() {
        final VmInitNetwork underTest = new VmInitNetwork();
        underTest.setName(IFACE_NAME);
        underTest.setIpv6BootProtocol(Ipv6BootProtocol.STATIC_IP);
        underTest.setIpv6Address(IPV6_ADDRESS);
        underTest.setIpv6Prefix(IPV6_PREFIX);
        underTest.setIpv6Gateway(IPV6_GATEWAY);
        underTest.setStartOnBoot(true);
        VmInit vmInit = new VmInit();
        vmInit.setNetworks(Collections.singletonList(underTest));

        String expectedOutput = "{\n"
                + "  \"links\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"vif\",\n"
                + "      \"name\": \"iface name\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"networks\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"ipv6\",\n"
                + "      \"link\": \"iface name\",\n"
                + "      \"ip_address\": \"ipv6 address\",\n"
                + "      \"netmask\": \"666\",\n"
                + "      \"gateway\": \"ipv6 gateway\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    private static Pair<VmInit, String> staticIPv4AndIPv6() {
        final VmInitNetwork underTest = new VmInitNetwork();
        underTest.setName(IFACE_NAME);
        underTest.setBootProtocol(Ipv4BootProtocol.STATIC_IP);
        underTest.setIp(IPV4_ADDRESS);
        underTest.setNetmask(IPV4_NETMASK);
        underTest.setGateway(IPV4_GATEWAY);
        underTest.setIpv6BootProtocol(Ipv6BootProtocol.STATIC_IP);
        underTest.setIpv6Address(IPV6_ADDRESS);
        underTest.setIpv6Prefix(IPV6_PREFIX);
        underTest.setIpv6Gateway(IPV6_GATEWAY);
        underTest.setStartOnBoot(true);
        VmInit vmInit = new VmInit();
        vmInit.setNetworks(Collections.singletonList(underTest));

        String expectedOutput = "{\n"
                + "  \"links\": [\n"
                + "    {\n"
                + "      \"name\": \"iface name\",\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"vif\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"networks\": [\n"
                + "    {\n"
                + "      \"netmask\": \"ipv4 netmask\",\n"
                + "      \"link\": \"iface name\",\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"ip_address\": \"ipv4 address\",\n"
                + "      \"type\": \"ipv4\",\n"
                + "      \"gateway\": \"ipv4 gateway\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"netmask\": \"666\",\n"
                + "      \"link\": \"iface name\",\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"ip_address\": \"ipv6 address\",\n"
                + "      \"type\": \"ipv6\",\n"
                + "      \"gateway\": \"ipv6 gateway\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    private static Pair<VmInit, String> staticIPv4WithDns() {
        final VmInitNetwork underTest = new VmInitNetwork();
        underTest.setName(IFACE_NAME);
        underTest.setBootProtocol(Ipv4BootProtocol.STATIC_IP);
        underTest.setIp(IPV4_ADDRESS);
        underTest.setNetmask(IPV4_NETMASK);
        underTest.setGateway(IPV4_GATEWAY);
        underTest.setStartOnBoot(true);
        VmInit vmInit = new VmInit();
        vmInit.setNetworks(Collections.singletonList(underTest));
        vmInit.setDnsSearch("search1 search2");
        vmInit.setDnsServers("nameserver1 nameserver2");

        String expectedOutput = "{\n"
                + "  \"links\": [\n"
                + "    {\n"
                + "      \"name\": \"iface name\",\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"vif\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"services\": [\n"
                + "    {\n"
                + "      \"address\": \"nameserver1\",\n"
                + "      \"type\": \"dns\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"address\": \"nameserver2\",\n"
                + "      \"type\": \"dns\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"networks\": [\n"
                + "    {\n"
                + "      \"netmask\": \"ipv4 netmask\",\n"
                + "      \"dns_search\": [\n"
                + "        \"search1\",\n"
                + "        \"search2\"\n"
                + "      ],\n"
                + "      \"link\": \"iface name\",\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"ip_address\": \"ipv4 address\",\n"
                + "      \"type\": \"ipv4\",\n"
                + "      \"gateway\": \"ipv4 gateway\",\n"
                + "      \"dns_nameservers\": [\n"
                + "        \"nameserver1\",\n"
                + "        \"nameserver2\"\n"
                + "      ]\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    private static Pair<VmInit, String> dnsServersOnly() {
        VmInit vmInit = new VmInit();
        VmInitNetwork underTest = new VmInitNetwork();
        underTest.setStartOnBoot(true);
        vmInit.setNetworks(Collections.singletonList(underTest));
        vmInit.setDnsSearch("search1 search2");
        vmInit.setDnsServers("nameserver1 nameserver2 nameserver3");

        String expectedOutput = "{\n"
                + "  \"services\": [\n"
                + "    {\n"
                + "      \"address\": \"nameserver1\",\n"
                + "      \"type\": \"dns\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"address\": \"nameserver2\",\n"
                + "      \"type\": \"dns\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"address\": \"nameserver3\",\n"
                + "      \"type\": \"dns\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    /**
     * ENI after fix 1464043:
     * payload:
     * "network-interfaces" : "iface eth0 inet dhcp\n"
     *
     * cloud-init-output.log: ok
     *
     * virt-cat img:/etc/sysconfig/network-scripts/ifcfg-eth0:
     * BOOTPROTO=dhcp
     * DEVICE=eth0
     * ONBOOT=yes
     * TYPE=Ethernet
     * USERCTL=no
     *
     * cat vm:/etc/sysconfig/network-scripts/ifcfg-eth0:
     * same as in virt-cat of img + HWADDR
     *
     * -----------------------------------------------------------------------------------------------
     * openstack metadata
     *
     * payload:
     *{"links":[{"name":"eth0","id":"eth0","type":"vif"}],"networks":[{"link":"eth0","id":"eth0","type":"dhcp4"}]}
     *
     * cloud-init-output.log: ok
     * final behavior: first boot - got ip from dhcp
     *
     * virt-cat img:/etc/sysconfig/network-scripts/ifcfg-eth0:
     * BOOTPROTO=dhcp
     * DEVICE=eth0
     * ONBOOT=yes
     * TYPE=Ethernet
     * USERCTL=no
     *
     * cat vm:/etc/sysconfig/network-scripts/ifcfg-eth0 (after first boot):
     * same as in virt-cat of img
     *
     * cat vm:/etc/sysconfig/network-scripts/ifcfg-eth0 (after second boot):
     * same as after first boot + HWADDR
     *
     */
    private static Pair<VmInit, String> dhcpIPv4() {
        final VmInitNetwork underTest = new VmInitNetwork();
        underTest.setName(IFACE_NAME);
        underTest.setBootProtocol(Ipv4BootProtocol.DHCP);
        underTest.setStartOnBoot(true);
        VmInit vmInit = new VmInit();
        vmInit.setNetworks(Collections.singletonList(underTest));

        String expectedOutput = "{\n"
                + "  \"links\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"vif\",\n"
                + "      \"name\": \"iface name\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"networks\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"ipv4_dhcp\",\n"
                + "      \"link\": \"iface name\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    /**
     * payload:
     * {"links":[{"name":"eth0","id":"eth0","type":"vif"}],"networks":[{"link":"eth0","id":"eth0","type":"dhcp6"}]}
     *
     * virt-cat img:/etc/sysconfig/network-scripts/ifcfg-eth0:
     * BOOTPROTO=dhcp
     * DEVICE=eth0
     * ONBOOT=yes
     * TYPE=Ethernet
     * USERCTL=no
     *
     * cat vm:/etc/sysconfig/network-scripts/ifcfg-eth0 (after first boot):
     * same as in virt-cat img
     *
     * cat vm:/etc/sysconfig/network-scripts/ifcfg-eth0 (after second boot):
     * same as after first boot + HWADDR
     *
     */
    private static Pair<VmInit, String> dhcpIPv6() {
        final VmInitNetwork underTest = new VmInitNetwork();
        underTest.setName(IFACE_NAME);
        underTest.setIpv6BootProtocol(Ipv6BootProtocol.DHCP);
        underTest.setStartOnBoot(true);
        VmInit vmInit = new VmInit();
        vmInit.setNetworks(Collections.singletonList(underTest));

        String expectedOutput = "{\n"
                + "  \"links\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"vif\",\n"
                + "      \"name\": \"iface name\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"networks\": [\n"
                + "    {\n"
                + "      \"id\": \"iface name\",\n"
                + "      \"type\": \"ipv6_dhcp\",\n"
                + "      \"link\": \"iface name\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new Pair<>(vmInit, expectedOutput);
    }

    /**
     * ENI before fix 1464043:
     * payload:
     * "network-interfaces" : "iface eth0 inet none\niface eth0 inet6 none\n" - error:
     * cloud-init-output.log:
     * cloudinit.net.ParserError: Interface eth0 can only be defined once. Re-defined in 'None'.
     *
     * login: impossible
     *
     * -----------------------------------------------------------------------------------------------
     * ENI after fix 1464043:
     * payload:
     * "network-interfaces" : "iface eth0 inet none\n"
     * cloud-init-output.log:
     * ValueError: Unknown subnet type 'none' found for interface 'eth0'
     *
     * /etc/sysconfig/network-scripts/ifcfg-eth0:
     * DEVICE="eth0"
     * BOOTPROTO="dhcp"
     * ONBOOT="yes"
     * TYPE="Ethernet"
     * PERSISTENT_DHCLIENT="yes"
     *
     * login: impossible
     *
     * -----------------------------------------------------------------------------------------------
     * Openstack Metadata Service:
     *
     * payload:
     * {}
     *
     * cloud-init-output.log: ok
     *
     * /etc/sysconfig/network-scripts/ifcfg-eth0
     * BOOTPROTO="dhcp"
     * DEVICE="eth0"
     * ONBOOT="yes"
     * TYPE="Ethernet"
     * PERSISTENT_DHCLIENT="yes"
     *
     * cat console:/etc/sysconfig/network-scripts/ifcfg-eth0 (after reboot)
     * BOOTPROTO=dhcp
     * HWADDR=...
     * DEVICE=eth0
     * ONBOOT=yes
     * TYPE=Ethernet
     * USERCTL=no
     *
     */
    private static Pair<VmInit, String> noneAndNone() {
        VmInit vmInit = new VmInit();
        VmInitNetwork underTest = new VmInitNetwork();
        underTest.setStartOnBoot(true);
        vmInit.setNetworks(Collections.singletonList(underTest));
        return new Pair<>(vmInit, null);
    }

    @SuppressWarnings("unchecked")
    private static Pair startOnBootFalse() {
        VmInit vmInit = new VmInit();
        VmInitNetwork underTest = new VmInitNetwork();
        underTest.setStartOnBoot(false);
        vmInit.setNetworks(Collections.singletonList(underTest));
        return new Pair(vmInit, new IllegalArgumentException("Malformed input", new IllegalArgumentException("'Start on boot' must be true")));
    }
}
