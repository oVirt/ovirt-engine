package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CopyHostNetworksHelperTest {

    private static final Logger log = LoggerFactory.getLogger(CopyHostNetworksHelperTest.class);
    // Non-vlan
    private static final Guid NET1 = new Guid("00000000-0000-0000-0020-000000000000");
    // Vlan 10
    private static final Guid NET2 = new Guid("00000000-0000-0000-0020-000000000001");
    // Vlan 20
    private static final Guid NET3 = new Guid("00000000-0000-0000-0020-000000000002");
    // Vlan 30
    private static final Guid NET4 = new Guid("00000000-0000-0000-0020-000000000003");
    // ovirt-mgmt
    private static final Guid MGMT = new Guid("00000000-0000-0000-0020-010203040506");

    private static final ScenarioBuilder scenarios = new ScenarioBuilder();
    private static final Node SOURCE = Node.SOURCE;
    private static final Node DEST = Node.DEST;

    @Test
    void testScenarioNoneToNone() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.none(SOURCE), scenarios.none(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());
        assertTrue(helper.getAttachmentsToApply().isEmpty());
    }

    @Test
    void testScenarioMgmtToNone() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.mgmtOnly(SOURCE), scenarios.none(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());
        assertTrue(helper.getAttachmentsToApply().isEmpty());
    }

    @Test
    void testScenarioNoneToMgmt() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.none(SOURCE), scenarios.mgmtOnly(DEST));
        assertThrows(IndexOutOfBoundsException.class, helper::buildDestinationConfig);
    }

    @Test
    void testScenarioOneToNone() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.oneVm(SOURCE), scenarios.none(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        assertEquals(1, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET1, "eth1");
    }

    @Test
    void testScenarioMgmtToMgmt() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(
                scenarios.mgmtOnly(SOURCE), scenarios.mgmtOnly(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        assertTrue(attachmentsToApply.isEmpty());
    }

    @Test
    void testScenarioTwoToOne() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.two(SOURCE), scenarios.mgmtOnly(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        assertEquals(1, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET1, "eth1");
    }

    @Test
    void testScenarioThreeToOne() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.three(SOURCE), scenarios.mgmtOnly(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(2, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET2, "eth1");
        assertAttachment(attachmentsToApply.get(1), NET1, "eth2");
    }

    @Test
    void testScenarioFourToOne() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.four(SOURCE), scenarios.mgmtOnly(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET3, "bond0");
        assertAttachment(attachmentsToApply.get(1), NET4, "bond0");
        assertAttachment(attachmentsToApply.get(2), NET1, "eth1");
        assertAttachment(attachmentsToApply.get(3), NET2, "eth1");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth2", "eth3"), "bond0");
    }

    @Test
    void testScenarioFiveToOne() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.five(SOURCE), scenarios.mgmtOnly(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET1, "bond0");
        assertAttachment(attachmentsToApply.get(1), NET2, "bond0");
        assertAttachment(attachmentsToApply.get(2), NET3, "bond0");
        assertAttachment(attachmentsToApply.get(3), NET4, "eth2");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth1", "eth3"), "bond0");
    }

    @Test
    void testScenarioSevenToSix() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.seven(SOURCE), scenarios.six(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(1, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET1, "bond1");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth2", "eth3"), "bond1");
    }

    @Test
    void testScenarioThreeToTwo() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.three(SOURCE), scenarios.two(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(2, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET2, "eth1");
        assertAttachmentReused(attachmentsToApply.get(1), NET1, "eth2");

    }

    @Test
    void testScenarioFourToThree() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.four(SOURCE), scenarios.three(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET3, "bond0");
        assertAttachment(attachmentsToApply.get(1), NET4, "bond0");
        assertAttachmentReused(attachmentsToApply.get(2), NET1, "eth1");
        assertAttachmentReused(attachmentsToApply.get(3), NET2, "eth1");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth2", "eth3"), "bond0");
    }

    @Test
    void testScenarioFiveToFour() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.five(SOURCE), scenarios.four(DEST));
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachmentReused(attachmentsToApply.get(0), NET1, "bond0");
        assertAttachmentReused(attachmentsToApply.get(1), NET2, "bond0");
        assertAttachmentReused(attachmentsToApply.get(2), NET3, "bond0");
        assertAttachmentReused(attachmentsToApply.get(3), NET4, "eth2");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth1", "eth3"), "bond0");
    }

    @Test
    void testScenarioOneToFive() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.mgmtOnly(SOURCE), scenarios.five(DEST));
        helper.buildDestinationConfig();

        assertEquals(4, helper.getAttachmentsToRemove().size());
        assertEquals(1, helper.getBondsToRemove().size());
        assertTrue(helper.getBondsToApply().isEmpty());
        assertTrue(helper.getAttachmentsToApply().isEmpty());
    }

    @Test
    void testIPv4Configuration() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.ipv4(SOURCE), scenarios.mgmtOnly(DEST));
        helper.buildDestinationConfig();

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(3, attachmentsToApply.size());
        assertIPv4Configuration(attachmentsToApply.get(0), Ipv4BootProtocol.NONE);
        assertIPv4Configuration(attachmentsToApply.get(1), Ipv4BootProtocol.DHCP);
        assertIPv4Configuration(attachmentsToApply.get(2), Ipv4BootProtocol.NONE);

        assertAttachment(attachmentsToApply.get(0), NET1, "eth1");
        assertAttachment(attachmentsToApply.get(1), NET2, "eth2");
        assertAttachment(attachmentsToApply.get(2), NET3, "eth3");
    }

    @Test
    void testIPv6Configuration() {
        CopyHostNetworksHelper helper = createCopyHostNetworksHelper(scenarios.ipv6(SOURCE), scenarios.mgmtOnly(DEST));
        helper.buildDestinationConfig();

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertIPv6Configuration(attachmentsToApply.get(0), Ipv6BootProtocol.NONE);
        assertIPv6Configuration(attachmentsToApply.get(1), Ipv6BootProtocol.DHCP);
        assertIPv6Configuration(attachmentsToApply.get(2), Ipv6BootProtocol.AUTOCONF);
        assertIPv6Configuration(attachmentsToApply.get(3), Ipv6BootProtocol.NONE);

        assertAttachment(attachmentsToApply.get(0), NET1, "eth1");
        assertAttachment(attachmentsToApply.get(1), NET2, "eth2");
        assertAttachment(attachmentsToApply.get(2), NET3, "eth2");
        assertAttachment(attachmentsToApply.get(3), NET4, "eth3");
    }

    private CopyHostNetworksHelper createCopyHostNetworksHelper(
            Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> sourceConfiguration,
            Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> destinationConfiguration) {
        return new CopyHostNetworksHelper(sourceConfiguration.getFirst(), sourceConfiguration.getSecond(),
            destinationConfiguration.getFirst(), destinationConfiguration.getSecond());
    }

    private void assertAttachment(NetworkAttachment attachment, Guid netId, String nicName) {
        assertEquals(netId, attachment.getNetworkId());
        assertEquals(nicName, attachment.getNicName());
    }

    private void assertAttachmentReused(NetworkAttachment attachment, Guid netId, String nicName) {
        assertAttachment(attachment, netId, nicName);
        assertNotNull(attachment.getId());
    }

    private void assertIPv4Configuration(NetworkAttachment attachment, Ipv4BootProtocol bootProtocol) {
        IpConfiguration ipConfig = attachment.getIpConfiguration();
        assertTrue(ipConfig.hasIpv4PrimaryAddressSet());
        assertEquals(bootProtocol, ipConfig.getIpv4PrimaryAddress().getBootProtocol());
    }

    private void assertIPv6Configuration(NetworkAttachment attachment, Ipv6BootProtocol bootProtocol) {
        IpConfiguration ipConfig = attachment.getIpConfiguration();
        assertTrue(ipConfig.hasIpv6PrimaryAddressSet());
        assertEquals(bootProtocol, ipConfig.getIpv6PrimaryAddress().getBootProtocol());
    }

    private void assertBond(CreateOrUpdateBond bond, List<String> slaves, String bondName) {
        assertEquals(new HashSet<>(slaves), bond.getSlaves());
        assertEquals(bondName, bond.getName());
    }

    private enum Node { SOURCE, DEST }

    private static class ScenarioBuilder {

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> none(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .build();
        }


        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> oneVm(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .attachNetwork("eth1", NET1)
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> mgmtOnly(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .attachMgmtNetwork("eth0")
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> two(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .attachMgmtNetwork("eth0")
                    .attachNetwork("eth1", NET1)
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> three(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .attachMgmtNetwork("eth0")
                    .attachVlanNetwork("eth1", NET2, 10)
                    .attachNetwork("eth2", NET1)
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> four(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .attachMgmtNetwork("eth0")
                    .attachNetwork("eth1", NET1)
                    .attachVlanNetwork("eth1", NET2, 20)
                    .createBondIface("bond0", Arrays.asList("eth2", "eth3"))
                    .attachVlanNetwork("bond0", NET3, 30)
                    .attachVlanNetwork("bond0", NET4, 40)
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> five(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .attachMgmtNetwork("eth0")
                    .attachVlanNetwork("eth2", NET4, 30)
                    .createBondIface("bond0", Arrays.asList("eth1", "eth3"))
                    .attachNetwork("bond0", NET1)
                    .attachVlanNetwork("bond0", NET2, 10)
                    .attachVlanNetwork("bond0", NET3, 20)
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> six(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .createBondIface("bond0", Arrays.asList("eth0", "eth1"))
                    .attachMgmtNetwork("bond0")
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> seven(Node node) {
            return new NetConfigBuilder(3, new NetsConfigPrinter(node))
                    .setDefaultIpConfig(IpConfigBuilder.NULL4_NULL6)
                    .attachMgmtNetwork("eth0")
                    .createBondIface("bond0", Arrays.asList("eth1", "eth2"))
                    .attachNetwork("bond0", NET1)
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> ipv4(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .attachMgmtNetwork("eth0", IpConfigBuilder.NULL4_NULL6)
                    .attachNetwork("eth1", NET1, IpConfigBuilder.NONE4_NULL6)
                    .attachNetwork("eth2", NET2, IpConfigBuilder.DHCP4_NULL6)
                    .attachNetwork("eth3", NET3, IpConfigBuilder.STATIC4_NULL6)
                    .build();
        }

        private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> ipv6(Node node) {
            return new NetConfigBuilder(4, new NetsConfigPrinter(node))
                    .attachMgmtNetwork("eth0", IpConfigBuilder.NULL4_NULL6)
                    .attachNetwork("eth1", NET1, IpConfigBuilder.NULL4_NONE6)
                    .attachNetwork("eth2", NET2, IpConfigBuilder.NULL4_DHCP6)
                    .attachNetwork("eth2", NET3, IpConfigBuilder.NULL4_AUTOCONF6)
                    .attachNetwork("eth3", NET4, IpConfigBuilder.NULL4_STATIC6)
                    .build();
        }

    }

    /**
     * Prints a Graphiz representation of the network layout. see graphviz.org
     */
    private static class NetsConfigPrinter {

        private Node node;
        private boolean printIfaceAttachment = true;
        private StringBuilder printout = new StringBuilder();

        NetsConfigPrinter(Node node) {
            this.node = node;
        }

        void printIfaceAttachment(String ifaceName, Guid networkId) {
            if (printIfaceAttachment) {
                printShape(ifaceName);
                String networkName = toNetworkName(networkId);
                printShape(networkName);
                printout.append("    " + prefix() + ifaceName + " -> " + prefix() +  networkName);
            }
            printIfaceAttachment = true;
        }

        void printVlanToNetwork(Integer vlanId, Guid networkId) {
            String networkName = toNetworkName(networkId);
            printShape(networkName);
            printout.append("    " + prefix() + toVlanName(vlanId) + " -> " + prefix() +  networkName);
            printIfaceAttachment = false;
        }

        void printIfaceToVlan(String ifaceName, Integer vlanId) {
            printShape(ifaceName);
            printout.append("    " + prefix() + ifaceName + " -> " + prefix() + toVlanName(vlanId));
            printShape(toVlanName(vlanId));
        }

        void printBondAttachment(String bondName, List<String> slaveNames) {
            slaveNames.forEach( slaveName -> {
                printShape(slaveName);
                printout.append("    " + prefix() + slaveName + " -> " + prefix() + bondName);
            });
        }

        private void printShape(String fullName) {
            String shape = fullName.contains("bond") ? "invtrapezium" :
                fullName.contains("net") ? "circle" :
                fullName.contains("mgmt") ? "circle" :
                fullName.contains("eth") ? "component" :
                fullName.contains("vlan") ? "diamond" :
                "missing shape. you have an error";

            String color = fullName.contains("bond") ? "cyan" :
                fullName.contains("net") ? "pink" :
                fullName.contains("mgmt") ? "red" :
                fullName.contains("eth") ? "grey" :
                fullName.contains("vlan") ? "sienna" :
                "missing color. you have an error";
            printout.append("    " + prefix() + fullName + "[shape=" + "\"" + shape + "\",color=\"" + color + "\"]");
        }

        String toNetworkName(Guid networkId) {
            return NET1.equals(networkId) ? "net1" :
                NET2.equals(networkId) ? "net2" :
                NET3.equals(networkId) ? "net3" :
                NET4.equals(networkId) ? "net4" :
                MGMT.equals(networkId) ? "mgmt" :
                "missing network name. you have an error";
        }

        private String toVlanName(Integer vlanId) {
            return "vlan_" + vlanId;
        }

        private String prefix() {
            return node == SOURCE ? "" : "_";
        }

        void printDigraphSuffix() {
            switch (node) {
            case SOURCE:
                printout.append("  }");
                break;
            case DEST:
                printout.append("  }\n"
                        + "}");
                break;
            }
        }

        void printDigraphPrefix() {
            switch(node){
            case SOURCE:
                printout.append("digraph ScenarioResult {\n"
                        + "\n"
                        + "  subgraph cluster_source {\n"
                        + "    label = \"source layout\";\n"
                        + "    color=blue;\n"
                        + "    node [style=filled];");
                break;
            case DEST:
                printout.append("\n"
                        + "  subgraph cluster_dest {\n"
                        + "    label = \"destination layout\";\n"
                        + "    color=blue\n"
                        + "    node [style=filled];");
                break;
            }
        }

        void writePrintout() {
            log.debug(printout.toString());
        }
    }

    private static class NetConfigBuilder {

        private static final Integer MGMT_TYPE = 2;

        private Map<String, VdsNetworkInterface> interfaces;
        private List<NetworkAttachment> attachments;
        private IpConfiguration ipConfiguration;
        private final NetsConfigPrinter netsConfigPrinter;

        NetConfigBuilder(int interfaceCount, NetsConfigPrinter netsConfigPrinter) {
            this.netsConfigPrinter = netsConfigPrinter;
            netsConfigPrinter.printDigraphPrefix();
            interfaces = createNics(interfaceCount);
            attachments = new ArrayList<>();
        }

        Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> build() {
            Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> pair = new Pair<>();
            pair.setFirst(new ArrayList<>(interfaces.values()));
            pair.setSecond(attachments);
            netsConfigPrinter.printDigraphSuffix();
            netsConfigPrinter.writePrintout();
            return pair;
        }

        NetConfigBuilder setDefaultIpConfig(IpConfiguration ipConfiguration) {
            this.ipConfiguration = ipConfiguration;
            return this;
        }

        NetConfigBuilder attachMgmtNetwork(String ifaceName) {
            return attachMgmtNetwork(ifaceName, ipConfiguration);
        }

        NetConfigBuilder attachMgmtNetwork(String ifaceName, IpConfiguration ipConfiguration) {
            VdsNetworkInterface iface = interfaces.get(ifaceName);
            iface.setType(MGMT_TYPE);
            return attachNetwork(ifaceName, MGMT, ipConfiguration);
        }

        NetConfigBuilder attachNetwork(String ifaceName, Guid networkId) {
            return attachNetwork(ifaceName, networkId, ipConfiguration);
        }

        NetConfigBuilder attachNetwork(String ifaceName, Guid networkId, IpConfiguration ipConfig) {
            VdsNetworkInterface iface = interfaces.get(ifaceName);
            NetworkAttachment attachment = createAttachment(iface.getId(), networkId);
            attachment.setIpConfiguration(ipConfig);
            attachments.add(attachment);
            netsConfigPrinter.printIfaceAttachment(ifaceName, networkId);
            return this;
        }

        NetConfigBuilder attachVlanNetwork(String ifaceName, Guid networkId, Integer vlanId) {
            return attachVlanNetwork(ifaceName, networkId, vlanId, ipConfiguration);
        }

        NetConfigBuilder attachVlanNetwork(String ifaceName, Guid networkId, Integer vlanId, IpConfiguration ipConfiguration) {
            VdsNetworkInterface vlanIface = createVlan(ifaceName, vlanId);
            interfaces.put(vlanIface.getName(), vlanIface);
            netsConfigPrinter.printIfaceToVlan(ifaceName, vlanId);
            netsConfigPrinter.printVlanToNetwork(vlanId, networkId);
            return attachNetwork(ifaceName, networkId, ipConfiguration);
        }

        NetConfigBuilder createBondIface(String bondName, List<String> slaveNames) {
            Bond bond = createBond(bondName, slaveNames);
            netsConfigPrinter.printBondAttachment(bondName, slaveNames);
            bond.setBonded(true);
            slaveNames.stream()
                    .map(interfaces::get)
                    .forEach(iface -> iface.setBondName(bondName));
            interfaces.put(bondName, bond);
            return this;
        }

        private Map<String, VdsNetworkInterface> createNics(int count) {
            Map<String, VdsNetworkInterface> nicMap = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String nicName = "eth" + i;
                VdsNetworkInterface nic = createNic(nicName);
                netsConfigPrinter.printShape(nicName);
                nicMap.put(nicName, nic);
            }
            return nicMap;
        }

        private Nic createNic(String name) {
            var iface = new Nic();
            iface.setId(Guid.newGuid());
            iface.setName(name);
            return iface;
        }

        private Vlan createVlan(String baseName, Integer vlanId) {
            var iface = new Vlan();
            iface.setId(Guid.newGuid());
            iface.setName(baseName + "." + vlanId);
            iface.setVlanId(vlanId);
            return iface;
        }

        private Bond createBond(String name, List<String> slaves) {
            var iface = new Bond();
            iface.setId(Guid.newGuid());
            iface.setName(name);
            iface.setSlaves(slaves);
            return iface;
        }

        private NetworkAttachment createAttachment(Guid nicId, Guid netId) {
            NetworkAttachment attachment = new NetworkAttachment();
            attachment.setId(Guid.newGuid());
            attachment.setNicId(nicId);
            attachment.setNetworkId(netId);
            return attachment;
        }
    }

    private static class IpConfigBuilder {

        private static final IpConfigBuilder IP_CONFIG_BUILDER = new IpConfigBuilder();
        static final IpConfiguration NULL4_NULL6 = IP_CONFIG_BUILDER.createIpConfiguration(null, null);
        static final IpConfiguration NULL4_NONE6 = IP_CONFIG_BUILDER.createIpConfiguration(null, Ipv6BootProtocol.NONE);
        static final IpConfiguration NULL4_DHCP6 = IP_CONFIG_BUILDER.createIpConfiguration(null, Ipv6BootProtocol.DHCP);
        static final IpConfiguration NULL4_AUTOCONF6 = IP_CONFIG_BUILDER.createIpConfiguration(null, Ipv6BootProtocol.AUTOCONF);
        static final IpConfiguration NULL4_STATIC6 = IP_CONFIG_BUILDER.createIpConfiguration(null, Ipv6BootProtocol.STATIC_IP);
        static final IpConfiguration STATIC4_NULL6 = IP_CONFIG_BUILDER.createIpConfiguration(Ipv4BootProtocol.STATIC_IP, null);
        static final IpConfiguration DHCP4_NULL6 = IP_CONFIG_BUILDER.createIpConfiguration(Ipv4BootProtocol.DHCP, null);
        static final IpConfiguration NONE4_NULL6 = IP_CONFIG_BUILDER.createIpConfiguration(Ipv4BootProtocol.NONE, null);

        IpConfiguration createIpConfiguration(Ipv4BootProtocol ipv4BootProto, Ipv6BootProtocol ipv6BootProto) {
            var ipconfig = new IpConfiguration();
            if (ipv4BootProto != null) {
                ipconfig.setIPv4Addresses(Collections.singletonList(createIpv4Addr(ipv4BootProto)));
            }
            if (ipv6BootProto != null) {
                ipconfig.setIpV6Addresses(Collections.singletonList(createIpv6Addr(ipv6BootProto)));
            }
            return ipconfig;
        }

        private IPv4Address createIpv4Addr(Ipv4BootProtocol bootProtocol) {
            var ipv4 = new IPv4Address();
            ipv4.setBootProtocol(bootProtocol);
            return ipv4;
        }

        private IpV6Address createIpv6Addr(Ipv6BootProtocol bootProtocol) {
            var ipv6 = new IpV6Address();
            ipv6.setBootProtocol(bootProtocol);
            return ipv6;
        }
    }
}
