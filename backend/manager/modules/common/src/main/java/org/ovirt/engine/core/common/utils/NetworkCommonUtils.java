package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class NetworkCommonUtils {
    public static Map<String, List<String>> getBondNameToBondSlaveNamesMap(Collection<? extends VdsNetworkInterface> nics) {
        Map<String, List<String>> result = new HashMap<>();
        Map<String, List<VdsNetworkInterface>> map = getBondNameToBondSlavesMap(nics);

        for (Map.Entry<String, List<VdsNetworkInterface>> entry : map.entrySet()) {
            result.put(entry.getKey(), getNicNames(entry.getValue()));
        }

        return result;
    }

    public static Map<String, List<VdsNetworkInterface>> getBondNameToBondSlavesMap(Collection<? extends VdsNetworkInterface> nics) {
        Map<String, List<VdsNetworkInterface>> bondToSlaves = new HashMap<>();
        for (VdsNetworkInterface nic : nics) {
            if (nic.isPartOfBond()) {
                String bondName = nic.getBondName();
                if (!bondToSlaves.containsKey(bondName)) {
                    bondToSlaves.put(bondName, new ArrayList<VdsNetworkInterface>());
                }

                bondToSlaves.get(bondName).add(nic);
            }
        }

        return bondToSlaves;
    }

    private static List<String> getNicNames(List<VdsNetworkInterface> nics) {
        List<String> result = new ArrayList<>(nics.size());

        for (VdsNetworkInterface nic : nics) {
            result.add(nic.getName());
        }

        return result;
    }

    public static Collection<VdsNetworkInterface> getBondsWithSlavesInformation(Collection<? extends VdsNetworkInterface> nics) {
        List<VdsNetworkInterface> bonds = new ArrayList<>();

        fillBondSlaves(nics);
        for (VdsNetworkInterface nic : nics) {
            if (nic instanceof Bond) {
                bonds.add(nic);
            }
        }

        return bonds;
    }

    public static void fillBondSlaves(Collection<? extends VdsNetworkInterface> nics) {
        Map<String, List<String>> bondToSlaves = getBondNameToBondSlaveNamesMap(nics);

        for (VdsNetworkInterface nic : nics) {
            if (nic instanceof Bond) {
                Bond bond = (Bond) nic;
                bond.setSlaves(bondToSlaves.containsKey(bond.getName()) ? bondToSlaves.get(bond.getName())
                        : new ArrayList<String>());
            }
        }
    }

    public static IpConfiguration createIpConfigurationFromVdsNetworkInterface(VdsNetworkInterface nic) {
        if (nic == null) {
            return createDefaultIpConfiguration();
        }

        IPv4Address iPv4Address = new IPv4Address();
        if (nic.getBootProtocol() == NetworkBootProtocol.STATIC_IP) {
            iPv4Address.setAddress(nic.getAddress());
            iPv4Address.setNetmask(nic.getSubnet());
            iPv4Address.setGateway(nic.getGateway());
        }
        iPv4Address.setBootProtocol(nic.getBootProtocol());

        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.setIPv4Addresses(Collections.singletonList(iPv4Address));

        return ipConfiguration;
    }

    public static IpConfiguration createDefaultIpConfiguration() {
        IpConfiguration output = new IpConfiguration();
        IPv4Address iPv4Address = createDefaultIpAddress();
        output.getIPv4Addresses().add(iPv4Address);
        return output;
    }

    private static IPv4Address createDefaultIpAddress() {
        IPv4Address output = new IPv4Address();
        output.setBootProtocol(NetworkBootProtocol.NONE);
        return output;
    }

}
