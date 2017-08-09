package org.ovirt.engine.core.bll.network.macpool;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;

@Singleton
public class MacPoolFactory {

    @Inject
    private MacsUsedAcrossWholeSystem macsUsedAcrossWholeSystem;

    @Inject
    private AuditLogDirector auditLogDirector;

    public MacPool createMacPool(org.ovirt.engine.core.common.businessentities.MacPool macPool, boolean engineStartup) {
        MacPoolUsingRanges macPoolUsingRanges = new MacPoolUsingRanges(macPool.getId(),
                MacAddressRangeUtils.macPoolToRanges(macPool),
                macPool.isAllowDuplicateMacAddresses(),
                auditLogDirector);

        macPoolUsingRanges.initialize(engineStartup, macsUsedAcrossWholeSystem.getMacsForMacPool(macPool.getId()));

        return macPoolUsingRanges;
    }
}
