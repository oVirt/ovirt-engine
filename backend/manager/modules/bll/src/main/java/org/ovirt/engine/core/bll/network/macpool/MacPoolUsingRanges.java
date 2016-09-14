package org.ovirt.engine.core.bll.network.macpool;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.math.LongRange;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MacPoolUsingRanges implements MacPool {

    private static final Logger log = LoggerFactory.getLogger(MacPoolUsingRanges.class);

    private final Guid id;
    private final boolean allowDuplicates;
    private MacsStorage macsStorage;
    private Collection<LongRange> rangesBoundaries;

    public MacPoolUsingRanges(Guid id, Collection<LongRange> rangesBoundaries, boolean allowDuplicates) {
        this.id = id;
        this.allowDuplicates = allowDuplicates;
        this.rangesBoundaries = rangesBoundaries;
        initialize();
    }

    private void initialize() {
        log.info("Initializing {}", this);
        this.macsStorage = createMacsStorage(rangesBoundaries);
        log.info("Finished initializing {}. Available MACs in pool: {}", this, macsStorage.getAvailableMacsCount());
    }

    /**
     * create and initialize internal structures to accommodate all macs specified in {@code rangesString} up to {@code
     * maxMacsInPool}.
     *
     * @return initialized {@link MacsStorage} instance.
     */
    private MacsStorage createMacsStorage(Collection<LongRange> rangesBoundaries) {
        MacsStorage macsStorage = new MacsStorage(allowDuplicates);
        for (LongRange range : rangesBoundaries) {
            log.debug("Adding range {} to pool {}.", range, this);
            macsStorage.addRange(range.getMinimumLong(), range.getMaximumLong());
        }

        if (macsStorage.availableMacExist()) {
            return macsStorage;
        } else {
            throw new EngineException(EngineError.MAC_POOL_INITIALIZATION_FAILED);
        }
    }

    private void logWhenMacPoolIsEmpty() {
        if (!macsStorage.availableMacExist()) {
            AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase());
            new AuditLogDirector().log(logable, AuditLogType.MAC_POOL_EMPTY);
        }
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public String allocateNewMac() {
        return allocateMacAddresses(1).get(0);
    }

    @Override
    public int getAvailableMacsCount() {
        int availableMacsSize = macsStorage.getAvailableMacsCount();
        log.debug("Number of available Mac addresses = {}", availableMacsSize);
        return availableMacsSize;
    }

    @Override
    public void freeMac(String mac) {
        this.freeMacs(Collections.singletonList(mac));
    }

    @Override
    public boolean addMac(String mac) {
        log.debug("Allocating custom mac address {} from {}.", mac, this);
        boolean added = macsStorage.useMac(MacAddressRangeUtils.macToLong(mac));
        logWhenMacPoolIsEmpty();
        return added;
    }

    @Override
    public void forceAddMac(String mac) {
        log.debug("Forcibly allocating custom mac address {} from {}", mac, this);
        macsStorage.useMacNoDuplicityCheck(MacAddressRangeUtils.macToLong(mac));
        logWhenMacPoolIsEmpty();
    }

    @Override
    public boolean isMacInUse(String mac) {
        boolean result = macsStorage.isMacInUse(MacAddressRangeUtils.macToLong(mac));
        log.debug("Mac {} isMacInUse={}", mac, result);
        return result;
    }

    @Override
    public void freeMacs(List<String> macs) {
        log.debug("Releasing mac addresses {} back to {}", macs, this);
        for (String mac : macs) {
            macsStorage.freeMac(MacAddressRangeUtils.macToLong(mac));
        }
    }

    @Override
    public List<String> allocateMacAddresses(int numberOfAddresses) {
        log.debug("Allocating {} mac addresses from {}.", numberOfAddresses, this);
        List<Long> macs = macsStorage.allocateAvailableMacs(numberOfAddresses);
        List<String> result = MacAddressRangeUtils.macAddressesToStrings(macs);

        log.debug("Allocated mac addresses: {} from {}.", result, this);
        Collections.sort(result);
        logWhenMacPoolIsEmpty();

        return result;
    }

    @Override
    public boolean isMacInRange(String mac) {
        boolean result = macsStorage.isMacInRange(MacAddressRangeUtils.macToLong(mac));
        log.debug("Mac {} isMacInRange={}", mac, result);
        return result;
    }

    @Override
    public boolean isDuplicateMacAddressesAllowed() {
        return this.allowDuplicates;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", id)
                .build();
    }
}
