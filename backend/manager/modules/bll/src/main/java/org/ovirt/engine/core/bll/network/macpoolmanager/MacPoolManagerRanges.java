package org.ovirt.engine.core.bll.network.macpoolmanager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.math.LongRange;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;
import org.ovirt.engine.core.utils.lock.AutoCloseableLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacPoolManagerRanges implements MacPoolManagerStrategy {

    private static final Logger log = LoggerFactory.getLogger(MacPoolManagerRanges.class);

    private final ReentrantReadWriteLock lockObj = new ReentrantReadWriteLock();
    private final boolean allowDuplicates;
    private boolean initialized;
    private MacsStorage macsStorage;
    private Collection<LongRange> rangesBoundaries;

    public MacPoolManagerRanges(Collection<LongRange> rangesBoundaries, boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        this.rangesBoundaries = rangesBoundaries;
    }

    @Override
    public void initialize() {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            if (initialized) {
                log.error("Trying to initialize {} multiple times.", getClass().getName());
                return;
            }

            log.info("Start initializing {}", getClass().getSimpleName());

            this.macsStorage = createMacsStorage(rangesBoundaries);

            initialized = true;
            log.info("Finished initializing. Available MACs in pool: {}", macsStorage.getAvailableMacsCount());
        } catch (Exception ex) {
            log.error("Error in initializing MAC Addresses pool manager: {}", ex.getMessage());
            log.debug("Exception", ex);
        }
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
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
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
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            checkIfInitialized();
            return allocateNewMacsWithoutLocking(1).get(0);
        }
    }

    private List<String> allocateNewMacsWithoutLocking(int numberOfMacs) {
        List<Long> macs = macsStorage.allocateAvailableMacs(numberOfMacs);
        Collections.sort(macs);
        logWhenMacPoolIsEmpty();

        return MacAddressRangeUtils.macAddressesToStrings(macs);
    }

    @Override
    public int getAvailableMacsCount() {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.readLock())) {
            checkIfInitialized();
            int availableMacsSize = macsStorage.getAvailableMacsCount();
            log.debug("Number of available Mac addresses = {}", availableMacsSize);
            return availableMacsSize;
        }
    }

    @Override
    public void freeMac(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            checkIfInitialized();
            macsStorage.freeMac(MacAddressRangeUtils.macToLong(mac));
        }
    }

    @Override
    public boolean addMac(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            checkIfInitialized();
            boolean added = macsStorage.useMac(MacAddressRangeUtils.macToLong(mac));
            logWhenMacPoolIsEmpty();
            return added;
        }
    }

    @Override
    public void forceAddMac(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            checkIfInitialized();
            forceAddMacWithoutLocking(mac);
        }
    }

    protected void forceAddMacWithoutLocking(String mac) {
        macsStorage.useMacNoDuplicityCheck(MacAddressRangeUtils.macToLong(mac));
        logWhenMacPoolIsEmpty();
    }

    @Override
    public boolean isMacInUse(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.readLock())) {
            checkIfInitialized();
            return macsStorage.isMacInUse(MacAddressRangeUtils.macToLong(mac));
        }
    }

    @Override
    public void freeMacs(List<String> macs) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            checkIfInitialized();
            for (String mac : macs) {
                macsStorage.freeMac(MacAddressRangeUtils.macToLong(mac));
            }
        }
    }

    @Override
    public List<String> allocateMacAddresses(int numberOfAddresses) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            checkIfInitialized();
            return allocateNewMacsWithoutLocking(numberOfAddresses);
        }
    }


    private void checkIfInitialized() {
        if (!initialized) {
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
        }

    }
}
