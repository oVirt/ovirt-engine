package org.ovirt.engine.core.bll.network.macpool;

import java.util.ArrayList;
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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
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
    private final AuditLogDirector auditLogDirector;

    public MacPoolUsingRanges(Guid id,
            Collection<LongRange> rangesBoundaries,
            boolean allowDuplicates,
            AuditLogDirector auditLogDirector) {
        this.id = id;
        this.allowDuplicates = allowDuplicates;
        this.rangesBoundaries = rangesBoundaries;
        this.auditLogDirector = auditLogDirector;

    }

    void initialize(boolean engineStartup, List<String> macsForMacPool) {
        log.info("Initializing {}", this);
        this.macsStorage = createMacsStorage(this.rangesBoundaries);

        log.debug("Initializing {} with macs: {}", this, macsForMacPool);
        List<String> notAddedMacs = addMacs(macsForMacPool);

        if (!notAddedMacs.isEmpty()) {
            if (engineStartup) {

                String auditLogMessage =
                        "Following MACs violates duplicity restriction, and was pushed into MAC pool without respect to it:"
                                + notAddedMacs;
                auditLogDirector.log(new AuditLogableImpl(),
                        AuditLogType.MAC_ADDRESS_VIOLATES_NO_DUPLICATES_SETTING,
                        auditLogMessage);

                forceAddMacs(notAddedMacs);
            } else {
                throw new EngineException(EngineError.MAC_POOL_INITIALIZATION_FAILED,
                        "Unable to initialize MAC pool due to existing duplicates");
            }
        }

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
        for (LongRange longRange : rangesBoundaries) {
            log.debug("Adding range {} to pool {}.", longRange, this);
            macsStorage.addRange(new Range(longRange));
        }

        if (macsStorage.availableMacExist()) {
            return macsStorage;
        } else {
            throw new EngineException(EngineError.MAC_POOL_INITIALIZATION_FAILED);
        }
    }

    @Override
    public boolean containsDuplicates() {
        return macsStorage.containsDuplicates();
    }

    private void logWhenMacPoolIsEmpty() {
        if (!macsStorage.availableMacExist()) {
            AuditLogable logable = new AuditLogableImpl();
            Injector.get(AuditLogDirector.class).log(logable, AuditLogType.MAC_POOL_EMPTY);
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
        return macsStorage.getAvailableMacsCount();
    }

    @Override
    public int getTotalMacsCount() {
        return macsStorage.getTotalNumberOfMacs();
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
    public List<String> addMacs(List<String> macs) {
        List<String> notAddedMacs = new ArrayList<>(macs.size());
        for (String mac : macs) {
            if (!addMac(mac)) {
                notAddedMacs.add(mac);
            }
        }

        return notAddedMacs;
    }

    private void forceAddMac(String mac) {
        log.debug("Forcibly allocating custom mac address {} from {}", mac, this);
        macsStorage.useMacNoDuplicityCheck(MacAddressRangeUtils.macToLong(mac));
        logWhenMacPoolIsEmpty();
    }

    private void forceAddMacs(List<String> macs) {
        for (String mac : macs) {
            forceAddMac(mac);
        }
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

    public boolean canAllocateMacAddresses(int numberOfAddresses) {
        return macsStorage.getAvailableMacsCount() >= numberOfAddresses;
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

    @Override
    public MacsStorage getMacsStorage() {
        return macsStorage;
    }

    @Override
    public boolean overlaps(MacPool macPool) {
        return macsStorage.overlaps(macPool.getMacsStorage());
    }
}
