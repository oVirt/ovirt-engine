package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;

public final class NetworkUtils {
    public static int MaxVmInterfaces = 8;
    public static final String DASH = "-";
    public static final String OS_REFERENCE_TO_MACHINE_NAME = "HOSTNAME";

    public static String getEngineNetwork() {
        return Config.<String> GetValue(ConfigValues.ManagementNetwork);
    }
    // method return interface name without vlan:
    // input: eth0.5 output eth0
    // input" eth0 output eth0
    public static String StripVlan(String name) {
        String[] tokens = name.split("[.]", -1);
        if (tokens.length == 1) {
            return name;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            sb.append(tokens[i]).append(".");
        }
        return StringHelper.trimEnd(sb.toString(), '.');
    }

    // method return interface name without vlan:
    // if the interface is not vlan it return null
    // input: eth0.5 returns eth0
    // input" eth0 returns null
    public static String getVlanInterfaceName(String name) {
        String[] tokens = name.split("[.]", -1);
        if (tokens.length == 1) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            sb.append(tokens[i]).append(".");
        }
        return StringHelper.trimEnd(sb.toString(), '.');
    }

    // method return the vlan part of the interface name (if exists),
    // else - return null
    public static Integer GetVlanId(String ifaceName) {
        String[] tokens = ifaceName.split("[.]", -1);
        if (tokens.length > 1) {
            int vlan = 0;
            RefObject<Integer> tempRefObject = new RefObject<Integer>(vlan);
            boolean tempVar = IntegerCompat.TryParse(tokens[tokens.length - 1], tempRefObject);
            vlan = tempRefObject.argvalue;
            if (tempVar) {
                return vlan;
            }
        }
        return null;
    }

    public static boolean IsBondVlan(List<VdsNetworkInterface> interfaces, VdsNetworkInterface iface) {
        boolean retVal = false;

        if (iface.getVlanId() != null) {
            for (VdsNetworkInterface i : interfaces) {
                if (i.getBonded() != null && i.getBonded() == true
                        && StringHelper.EqOp(i.getName(), StripVlan(iface.getName()))) {
                    retVal = true;
                    break;
                }
            }
        }

        return retVal;
    }

    public static boolean interfaceHasVlan(VdsNetworkInterface iface, List<VdsNetworkInterface> allIfaces) {
        for(VdsNetworkInterface i: allIfaces) {
            if (i.getVlanId() != null && NetworkUtils.StripVlan(i.getName()).equals(iface.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, network> networksByName(List<network> networks) {
        if (!networks.isEmpty()) {
            Map<String, network> byName = new HashMap<String, network>();
            for (network net : networks) {
                byName.put(net.getname(), net);
            }
            return byName;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * filter networks which are not VM networks from the newtorkNames list
     * @param networks
     *            logical networks
     * @param networkNames
     *            target names to match non-VM networks upon
     * @return
     */
    public static List<String> filterNonVmNetworkNames(List<network> networks, Set<String> networkNames) {
        List<String> list = new ArrayList<String>();
        for (network net : networks) {
            if (!net.isVmNetwork() && networkNames.contains(net.getName())) {
                list.add(net.getName());
            }
        }
        return list;
    }
}
