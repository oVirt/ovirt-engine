package org.ovirt.engine.core.bll.network.predicate;

import java.util.Objects;
import java.util.function.Predicate;

import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

/**
 * A MAC is considered as bad when either one occurs:
 * <ul>
 *     <li>It is in use by another VM and the pool doesn't allow duplicates.</li>
 *     <li>It is out of the range of the mac-pool.</li>
 * </ul>
 */
public class VnicWithBadMacPredicate implements Predicate<VmNetworkInterface> {
    private final MacPool macPool;

    public VnicWithBadMacPredicate(MacPool macPool) {
        this.macPool = Objects.requireNonNull(macPool);
    }

    @Override
    public boolean test(VmNetworkInterface vnic) {
        final String mac = vnic.getMacAddress();
        if (mac == null) {
            return false;
        }
        if (!macPool.isDuplicateMacAddressesAllowed() && macPool.isMacInUse(mac)) {
            return true;
        }
        if (!macPool.isMacInRange(mac)) {
            return true;
        }
        return false;
    }
}
