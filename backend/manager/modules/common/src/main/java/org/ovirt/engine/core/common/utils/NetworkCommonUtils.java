package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
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

    public static IpConfiguration createDefaultIpConfiguration() {
        IpConfiguration ipConfiguration = new IpConfiguration();
        IPv4Address iPv4Address = createDefaultIpv4Address();
        ipConfiguration.getIPv4Addresses().add(iPv4Address);
        IpV6Address ipv6Address = createDefaultIpv6Address();
        ipConfiguration.getIpV6Addresses().add(ipv6Address);
        return ipConfiguration;
    }

    public static IPv4Address createDefaultIpv4Address() {
        IPv4Address ipv4Address = new IPv4Address();
        ipv4Address.setBootProtocol(Ipv4BootProtocol.NONE);
        return ipv4Address;
    }

    public static IpV6Address createDefaultIpv6Address() {
        IpV6Address ipv6Address = new IpV6Address();
        ipv6Address.setBootProtocol(Ipv6BootProtocol.NONE);
        return ipv6Address;
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

    public static boolean isEl8(String kernelVersion) {
        return kernelVersion != null && kernelVersion.toLowerCase().contains("el8"); //$NON-NLS-1$
    }
}
