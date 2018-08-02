package org.ovirt.engine.core.bll.network.macpool;

import static org.ovirt.engine.core.utils.MacAddressRangeUtils.macToString;

import java.util.Collection;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.math.LongRange;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MacPoolFactory {

    private static final Logger log = LoggerFactory.getLogger(MacPoolFactory.class);

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
        reportOverlappingRanges(macPool);
        return macPoolUsingRanges;
    }

    private void reportOverlappingRanges(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        Collection<LongRange> overlappingRanges = MacAddressRangeUtils.filterOverlappingRanges(macPool);
        if (!overlappingRanges.isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ");
            overlappingRanges.forEach(range -> joiner.add(macToString(range.getMinimumLong()) + "-" + macToString(range.getMaximumLong())));
            String rangeReport = joiner.toString();
            AuditLogableImpl auditLoggable = new AuditLogableImpl();
            auditLoggable.addCustomValue("macPoolName", macPool.getName());
            auditLoggable.addCustomValue("overlapping", rangeReport);
            auditLogDirector.log(auditLoggable, AuditLogType.MAC_POOL_VIOLATES_NO_OVERLAPPING_RANGES);
        }
    }
}
