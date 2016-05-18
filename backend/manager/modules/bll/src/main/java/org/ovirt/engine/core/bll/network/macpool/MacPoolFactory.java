package org.ovirt.engine.core.bll.network.macpool;

import javax.ejb.Singleton;

import org.ovirt.engine.core.utils.MacAddressRangeUtils;

@Singleton
public class MacPoolFactory {

    public MacPool createMacPool(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        return new MacPoolUsingRanges(MacAddressRangeUtils.macPoolToRanges(macPool),
                macPool.isAllowDuplicateMacAddresses());
    }
}
