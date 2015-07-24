package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class InterfacesSpeed {
    private static final Pattern BOND_OPTS_MODE_CAPTURE = Pattern.compile("mode=(\\d)");

    /** maps interface name to it's speed. */
    private final Map<String, Integer> ifaceSpeeds = new HashMap<>();
    private final Map<String, List<VdsNetworkInterface>> bonds;

    public InterfacesSpeed(Map<String, List<VdsNetworkInterface>> bonds) {
        this.bonds = bonds;
    }



    /**
     * Computes the effective speed of an interface, using the following algorithm:
     * <p/>
     * <li>If vdsm reported a non-zero speed for the interface, use that.</li>
     * <p/>
     * <li>Else, if the interface is a newly-created bond and all its slaves have non-zero speed reported by vdsm,
     * compute the effective bond speed according to bonding mode.</li>
     * <p/>
     * <li>Else, return null.
     *
     * @param iface the interface whose effective speed is to be computed.
     *
     * @return the effective interface speed based on vdsm report, or null in case reported speeds are missing or
     * zero.
     */
    public Integer getInterfaceSpeed(VdsNetworkInterface iface) {
        String ifaceName = iface.getName();
        if (ifaceSpeeds.containsKey(ifaceName)) {
            return ifaceSpeeds.get(ifaceName);
        } else {
            Integer speed = calculateInterfaceSpeed(iface, ifaceName);

            // cache the speed for future reference
            ifaceSpeeds.put(ifaceName, speed);

            return speed;
        }
    }

    public boolean containsInterfaceWithoutKnownSpeed() {
        return ifaceSpeeds.values().contains(null);
    }

    public List<String> namesOfInterfacesWithoutKnownSpeed() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ifaceSpeeds.entrySet()) {
            if (entry.getValue() == null) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private Integer calculateInterfaceSpeed(VdsNetworkInterface iface, String ifaceName) {
        if (iface.hasSpeed()) {
            // vdsm reported some speed for this interface, use it
            return iface.getSpeed();
        } else {
            if (iface.isBond()) {
                // vdsm didn't report any speed - if this is a new bond, calculate its speed
                boolean bondModeFailover = isBondModeFailover(iface.getBondOptions());
                return calculateSpeedFromBondSlaves(bondModeFailover, getBondSlaves(ifaceName));
            } else {
                return null;
            }
        }
    }

    private Integer calculateSpeedFromBondSlaves(boolean bondModeFailover, List<VdsNetworkInterface> slaves) {
        if (slaves != null && !slaves.isEmpty()) {
            //choose minimum when bondModeFailover;
            Integer speed = bondModeFailover ? Integer.MAX_VALUE : 0;
            for (VdsNetworkInterface slave : slaves) {
                if (!slave.hasSpeed()) {
                    return null;
                }

                speed = bondModeFailover ? Math.min(speed, slave.getSpeed()) : speed + slave.getSpeed();
            }

            return speed;
        } else {
            return null;
        }
    }

    private List<VdsNetworkInterface> getBondSlaves(String ifaceName) {
        List<VdsNetworkInterface> slaves = bonds.get(ifaceName);
        return slaves == null ? Collections.<VdsNetworkInterface> emptyList() : slaves;
    }

    private boolean isBondModeFailover(String bondOptions) {
        Matcher matcher = BOND_OPTS_MODE_CAPTURE.matcher(bondOptions);
        matcher.find();
        int bondMode = parseInt(matcher.group(1));
        return bondMode == 1 || bondMode == 3;
    }

    private int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();   //TODO MM: propagate to canDoAction.
        }
    }
}
