package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({ MockitoExtension.class, MockConfigExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
class CopyHostNetworksHelperTest {

    // Non-vlan
    private static Guid NET1 = new Guid("00000000-0000-0000-0020-000000000000");
    // Vlan 10
    private static Guid NET2 = new Guid("00000000-0000-0000-0020-000000000001");
    // Vlan 20
    private static Guid NET3 = new Guid("00000000-0000-0000-0020-000000000002");
    // Vlan 30
    private static Guid NET4 = new Guid("00000000-0000-0000-0020-000000000003");

    @Test
    void testScenarioTwoToOne() {
        var sourceConfiguration = createScenarioTwo();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
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
        var sourceConfiguration = createScenarioThree();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
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
        var sourceConfiguration = createScenarioFour();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
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
        var sourceConfiguration = createScenarioFive();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
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

    private static void assertAttachment(NetworkAttachment attachment, Guid netId, String nicName) {
        assertEquals(netId, attachment.getNetworkId());
        assertEquals(nicName, attachment.getNicName());
    }

    private static void assertBond(CreateOrUpdateBond bond, List<String> slaves, String bondName) {
        assertEquals(new HashSet<>(slaves), bond.getSlaves());
        assertEquals(bondName, bond.getName());
    }

    private static Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioOne() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .build();
    }

    private static Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioTwo() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachNetwork("eth1", NET1)
                .build();
    }

    private static Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioThree() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachVlanNetwork("eth1", NET2, 10)
                .attachNetwork("eth2", NET1)
                .build();
    }

    private static Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioFour() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachNetwork("eth1", NET1)
                .attachVlanNetwork("eth1", NET2, 10)
                .createBondIface("bond0", Arrays.asList("eth2", "eth3"))
                .attachVlanNetwork("bond0", NET3, 20)
                .attachVlanNetwork("bond0", NET4, 30)
                .build();
    }

    private static Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioFive() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachVlanNetwork("eth2", NET4, 30)
                .createBondIface("bond0", Arrays.asList("eth1", "eth3"))
                .attachNetwork("bond0", NET1)
                .attachVlanNetwork("bond0", NET2, 10)
                .attachVlanNetwork("bond0", NET3, 20)
                .build();
    }

    private static class ScenarioBuilder {

        private static Guid MGMT_ID = new Guid("00000000-0000-0000-0020-010203040506");
        private static Integer MGMT_TYPE = 2;

        Map<String, VdsNetworkInterface> interfaces;
        List<NetworkAttachment> attachments;

        ScenarioBuilder(int interfaceCount) {
            interfaces = createNics(interfaceCount);
            attachments = new ArrayList<>();
        }

        public Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> build() {
            Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> pair = new Pair<>();
            pair.setFirst(new ArrayList<>(interfaces.values()));
            pair.setSecond(attachments);
            return pair;
        }

        ScenarioBuilder attachMgmtNetwork(String ifaceName) {
            VdsNetworkInterface iface = interfaces.get(ifaceName);
            iface.setType(MGMT_TYPE);
            this.attachNetwork(ifaceName, MGMT_ID);
            return this;
        }

        ScenarioBuilder attachNetwork(String ifaceName, Guid networkId) {
            VdsNetworkInterface iface = interfaces.get(ifaceName);
            NetworkAttachment attachment = createAttachment(iface.getId(), networkId);
            attachments.add(attachment);
            return this;
        }

        ScenarioBuilder attachVlanNetwork(String ifaceName, Guid networkId, Integer vlanId) {
            VdsNetworkInterface vlanIface = createVlan(ifaceName, vlanId);
            interfaces.put(vlanIface.getName(), vlanIface);
            this.attachNetwork(ifaceName, networkId);
            return this;
        }

        ScenarioBuilder createBondIface(String bondName, List<String> slaveNames) {
            Bond bond = createBond(bondName, slaveNames);
            bond.setBonded(true);
            slaveNames.stream()
                    .map(interfaces::get)
                    .forEach(iface -> iface.setBondName(bondName));
            interfaces.put(bondName, bond);
            return this;
        }

        static IpConfiguration createIpConfiguration(Ipv4BootProtocol ipv4BootProto, Ipv6BootProtocol ipv6BootProto) {
            var ipconfig = new IpConfiguration();
            if (ipv4BootProto != null) {
                ipconfig.setIPv4Addresses(Collections.singletonList(createIpv4Addr(ipv4BootProto)));
            }
            if (ipv6BootProto != null) {
                ipconfig.setIpV6Addresses(Collections.singletonList(createIpv6Addr(ipv6BootProto)));
            }
            return ipconfig;
        }

        private static Map<String, VdsNetworkInterface> createNics(int count) {
            Map<String, VdsNetworkInterface> nicMap = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String nicName = "eth" + i;
                VdsNetworkInterface nic = createNic(nicName);
                nicMap.put(nicName, nic);
            }
            return nicMap;
        }

        private static Nic createNic(String name) {
            var iface = new Nic();
            iface.setId(Guid.newGuid());
            iface.setName(name);
            return iface;
        }

        private static Vlan createVlan(String baseName, Integer vlanId) {
            var iface = new Vlan();
            iface.setId(Guid.newGuid());
            iface.setName(baseName + "." + vlanId);
            iface.setVlanId(vlanId);
            return iface;
        }

        private static Bond createBond(String name, List<String> slaves) {
            var iface = new Bond();
            iface.setId(Guid.newGuid());
            iface.setName(name);
            iface.setSlaves(slaves);
            return iface;
        }

        private static NetworkAttachment createAttachment(Guid nicId, Guid netId) {
            NetworkAttachment attachment = new NetworkAttachment();
            attachment.setId(Guid.newGuid());
            attachment.setNicId(nicId);
            attachment.setNetworkId(netId);
            attachment.setIpConfiguration(createIpConfiguration(null, null));
            return attachment;
        }

        private static IPv4Address createIpv4Addr(Ipv4BootProtocol bootProtocol) {
            var ipv4 = new IPv4Address();
            ipv4.setBootProtocol(bootProtocol);
            return ipv4;
        }

        private static IpV6Address createIpv6Addr(Ipv6BootProtocol bootProtocol) {
            var ipv6 = new IpV6Address();
            ipv6.setBootProtocol(bootProtocol);
            return ipv6;
        }

    }
}
