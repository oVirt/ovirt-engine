package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class NetworkCommonUtils {
    public static Map<String, List<String>> getBondNameToBondSlavesMap(Collection<? extends VdsNetworkInterface> nics) {
        Map<String, List<String>> bondToSlaves = new HashMap<>();
        for (VdsNetworkInterface nic : nics) {
            if (nic.isPartOfBond()) {
                String bondName = nic.getBondName();
                if (!bondToSlaves.containsKey(bondName)) {
                    bondToSlaves.put(bondName, new ArrayList<String>());
                }

                bondToSlaves.get(bondName).add(nic.getName());
            }
        }

        return bondToSlaves;
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
        Map<String, List<String>> bondToSlaves = getBondNameToBondSlavesMap(nics);

        for (VdsNetworkInterface nic : nics) {
            if (nic instanceof Bond) {
                Bond bond = (Bond) nic;
                bond.setSlaves(bondToSlaves.containsKey(bond.getName()) ? bondToSlaves.get(bond.getName())
                        : Collections.<String> emptyList());
            }
        }
    }
}
