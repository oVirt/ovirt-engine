package org.ovirt.engine.core.bll.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.math.LongRange;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MacPoolManagerRanges implements MacPoolManagerStrategy {

    private static final Log log = LogFactory.getLog(MacPoolManagerRanges.class);

    private final String rangesString;

    private final ReentrantReadWriteLock lockObj = new ReentrantReadWriteLock();
    private final boolean allowDuplicates;
    private boolean initialized;
    private MacsStorage macsStorage;

    public MacPoolManagerRanges(String rangesString, boolean allowDuplicates) {
        this.rangesString = rangesString;
        this.allowDuplicates = allowDuplicates;
    }

    @Override
    public void initialize() {
        lockObj.writeLock().lock();

        try {
            if (initialized) {
                log.error("Trying to initialized " + getClass().getName() + " multiple times.");
                return;
            }

            log.infoFormat("Start initializing " + getClass().getSimpleName());

            this.macsStorage = createMacsStorage(rangesString);
            addMacsFromDbToPool();

            logWhenMacPoolIsEmpty();
            initialized = true;
            log.infoFormat("Finished initializing. Available MACs in pool: {0}", macsStorage.getAvailableMacsCount());
        } catch (Exception ex) {
            log.errorFormat("Error in initializing MAC Addresses pool manager.", ex);
        } finally {
            lockObj.writeLock().unlock();
        }
    }

    private void addMacsFromDbToPool() {
        List<VmNic> interfaces = getVmNicInterfacesFromDb();

        for (VmNic iface : interfaces) {
            if (iface.getMacAddress() != null) {
                forceAddMacWithoutLocking(iface.getMacAddress());
            }
        }
    }

    private MacsStorage createMacsStorage(String rangesString) {
        Collection<LongRange> rangesBoundaries = MacAddressRangeUtils.parseRangeString(rangesString);
        MacsStorage macsStorage = new MacsStorage(allowDuplicates);
        for (LongRange range : rangesBoundaries) {
            macsStorage.addRange(range.getMinimumLong(), range.getMaximumLong());
        }

        return macsStorage;
    }

    private List<VmNic> getVmNicInterfacesFromDb() {
        return DbFacade.getInstance().getVmNicDao().getAll();
    }

    private void logWhenMacPoolIsEmpty() {
        if (!macsStorage.availableMacExist()) {
            AuditLogableBase logable = new AuditLogableBase();
            AuditLogDirector.log(logable, AuditLogType.MAC_POOL_EMPTY);
        }
    }

    @Override
    public String allocateNewMac() {
        lockObj.writeLock().lock();
        try {
            checkIfInitialized();
            return allocateNewMacsWithoutLocking(1).get(0);
        } finally {
            lockObj.writeLock().unlock();
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
        lockObj.readLock().lock();
        try {
            checkIfInitialized();
            int availableMacsSize = macsStorage.getAvailableMacsCount();
            log.debugFormat("Number of available Mac addresses = {1}", availableMacsSize);
            return availableMacsSize;
        } finally {
            lockObj.readLock().unlock();
        }
    }

    @Override
    public void freeMac(String mac) {
        lockObj.writeLock().lock();
        try {
            checkIfInitialized();
            macsStorage.freeMac(MacAddressRangeUtils.macToLong(mac));
        } finally {
            lockObj.writeLock().unlock();
        }
    }

    @Override
    public boolean addMac(String mac) {
        lockObj.writeLock().lock();
        try {
            checkIfInitialized();
            boolean added = macsStorage.useMac(MacAddressRangeUtils.macToLong(mac));
            logWhenMacPoolIsEmpty();
            return added;


        } finally {
            lockObj.writeLock().unlock();
        }
    }

    @Override
    public void forceAddMac(String mac) {
        lockObj.writeLock().lock();
        try {
            checkIfInitialized();
            forceAddMacWithoutLocking(mac);
            logWhenMacPoolIsEmpty();
        } finally {
            lockObj.writeLock().unlock();
        }
    }

    private void forceAddMacWithoutLocking(String mac) {
        macsStorage.useMacNoDuplicityCheck(MacAddressRangeUtils.macToLong(mac));
    }

    @Override
    public boolean isMacInUse(String mac) {
        lockObj.readLock().lock();
        try {
            checkIfInitialized();
            return macsStorage.isMacInUse(MacAddressRangeUtils.macToLong(mac));

        } finally {
            lockObj.readLock().unlock();
        }
    }

    @Override
    public void freeMacs(List<String> macs) {
        lockObj.writeLock().lock();
        try {
            checkIfInitialized();
            for (String mac : macs) {
                macsStorage.freeMac(MacAddressRangeUtils.macToLong(mac));
            }

        } finally {
            lockObj.writeLock().unlock();
        }
    }

    @Override
    public List<String> allocateMacAddresses(int numberOfAddresses) {
        lockObj.writeLock().lock();
        try {
            checkIfInitialized();
            return allocateNewMacsWithoutLocking(numberOfAddresses);
        } finally {
            lockObj.writeLock().unlock();
        }
    }


    private void checkIfInitialized() {
        if (!initialized) {
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
        }

    }
}
