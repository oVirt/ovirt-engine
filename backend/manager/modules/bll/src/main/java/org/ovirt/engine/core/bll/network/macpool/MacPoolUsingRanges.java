package org.ovirt.engine.core.bll.network.macpool;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.math.LongRange;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacPoolUsingRanges implements MacPool {

    private static final Logger log = LoggerFactory.getLogger(MacPoolUsingRanges.class);

    private final boolean allowDuplicates;
    private MacsStorage macsStorage;
    private Collection<LongRange> rangesBoundaries;

    public MacPoolUsingRanges(Collection<LongRange> rangesBoundaries, boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        this.rangesBoundaries = rangesBoundaries;
        initialize();
    }

    private void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        this.macsStorage = createMacsStorage(rangesBoundaries);
        log.info("Finished initializing. Available MACs in pool: {}", macsStorage.getAvailableMacsCount());
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
            AuditLogableBase logable = new AuditLogableBase();
            new AuditLogDirector().log(logable, AuditLogType.MAC_POOL_EMPTY);
        }
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
        macsStorage.freeMac(MacAddressRangeUtils.macToLong(mac));
    }

    @Override
    public boolean addMac(String mac) {
        boolean added = macsStorage.useMac(MacAddressRangeUtils.macToLong(mac));
        logWhenMacPoolIsEmpty();
        return added;
    }

    @Override
    public void forceAddMac(String mac) {
        macsStorage.useMacNoDuplicityCheck(MacAddressRangeUtils.macToLong(mac));
        logWhenMacPoolIsEmpty();
    }

    @Override
    public boolean isMacInUse(String mac) {
        return macsStorage.isMacInUse(MacAddressRangeUtils.macToLong(mac));
    }

    @Override
    public void freeMacs(List<String> macs) {
        for (String mac : macs) {
            macsStorage.freeMac(MacAddressRangeUtils.macToLong(mac));
        }
    }

    @Override
    public List<String> allocateMacAddresses(int numberOfAddresses) {
        List<Long> macs = macsStorage.allocateAvailableMacs(numberOfAddresses);
        Collections.sort(macs);
        logWhenMacPoolIsEmpty();

        return MacAddressRangeUtils.macAddressesToStrings(macs);
    }

    @Override
    public boolean isMacInRange(String mac) {
        return macsStorage.isMacInRange(MacAddressRangeUtils.macToLong(mac));
    }

    @Override
    public boolean isDuplicateMacAddressesAllowed() {
        return this.allowDuplicates;
    }
}
