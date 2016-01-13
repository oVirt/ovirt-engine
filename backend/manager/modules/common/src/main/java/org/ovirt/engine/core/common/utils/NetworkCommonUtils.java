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

        IPv4Address ipv4Address = new IPv4Address();
        if (nic.getIpv4BootProtocol() == NetworkBootProtocol.STATIC_IP) {
            ipv4Address.setAddress(nic.getIpv4Address());
            ipv4Address.setNetmask(nic.getIpv4Subnet());
            ipv4Address.setGateway(nic.getIpv4Gateway());
        }
        ipv4Address.setBootProtocol(nic.getIpv4BootProtocol());

        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.setIPv4Addresses(Collections.singletonList(ipv4Address));

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

    /**
     * Returns the underlying interface name of a given nic
     *
     * @return Base interface name if the nic is a vlan device.
     *         Otherwise, the name of the nic
     */
     public static String stripVlan(VdsNetworkInterface nic) {
         return isVlan(nic) ? nic.getBaseInterface() : nic.getName();
     }

     /**
      * Determine if a given network interface is a vlan device
      *
      * @param nic
      *            the nic to check.
      * @return <code>true</code> iff the nic is a vlan.
      */
     public static boolean isVlan(VdsNetworkInterface nic) {
         return nic.getVlanId() != null;
     }
}
