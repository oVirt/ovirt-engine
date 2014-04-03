package org.ovirt.engine.core.bll.network;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.map.CaseInsensitiveMap;
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

public class MacPoolManagerOriginal implements MacPoolManagerStrategy {


    private static final String INIT_ERROR_MSG = "{0}: Error in initializing MAC Addresses pool manager.";
    private static final Log log = LogFactory.getLog(MacPoolManagerOriginal.class);

    private final int maxMacsInPool;
    private final String ranges;
    private final boolean allowDuplicates;

    /**
     * A Map that holds the allocated MAC addresses as keys, and counters as values. These MAC addresses were taken from
     * the range defined by the user in {@link org.ovirt.engine.core.common.config.ConfigValues#MacPoolRanges}
     */
    @SuppressWarnings("unchecked")
    private final Map<String, Integer> allocatedMacs = new CaseInsensitiveMap();

    /**
     * A Map that holds the allocated MAC addresses as keys, and counters as values. These MAC addresses were allocated
     * when user requested a specific MAC address that is out of the range defined in {@link org.ovirt.engine.core.common.config.ConfigValues#MacPoolRanges}
     */
    @SuppressWarnings("unchecked")
    private final Map<String, Integer> allocatedCustomMacs = new CaseInsensitiveMap();

    /**
     * A Set that holds the non-allocated MAC addresses from the range defined in {@link org.ovirt.engine.core.common.config.ConfigValues#MacPoolRanges}
     * Every MAC in that range should either be in this Set, or be mapped to a value greater to zero in allocatedMacs,
     * but not both
     */
    private final Set<String> availableMacs = new HashSet<>();

    private final ReentrantReadWriteLock lockObj = new ReentrantReadWriteLock();
    private boolean initialized;

    public MacPoolManagerOriginal(int maxMacsInPool, String ranges, boolean allowDuplicates) {
        this.maxMacsInPool = maxMacsInPool;
        this.ranges = ranges;
        this.allowDuplicates = allowDuplicates;
    }

    @Override
    public void initialize() {
        lockObj.writeLock().lock();
        try {
            log.infoFormat("{0}: Start initializing", instanceId());

            if (!"".equals(ranges)) {
                try {
                    initRanges(ranges);
                } catch (MacPoolExceededMaxException e) {
                    log.errorFormat("{0}: MAC Pool range exceeded maximum number of mac pool addresses ({1}). "
                            + "Please check Mac Pool configuration.", instanceId(), maxMacsInPool);
                }
            }

            List<VmNic> interfaces = getVmNicsFromDB();

            for (VmNic iface : interfaces) {
                forceAddMac(iface.getMacAddress());
            }
            initialized = true;
            log.infoFormat("{0}: Finished initializing. Available MACs in pool: {1}",
                    instanceId(),
                    availableMacs.size());
        } catch (Exception ex) {
            log.errorFormat(INIT_ERROR_MSG, instanceId(), ex);
        } finally {
            lockObj.writeLock().unlock();
        }
    }

    //to allow testing
    List<VmNic> getVmNicsFromDB() {
        return DbFacade.getInstance().getVmNicDao().getAll();
    }

    private String instanceId() {
        return MessageFormat.format("{0}({1})",
                getClass().getSimpleName(),
                Integer.toHexString(System.identityHashCode(this)));
    }

    private void initRanges(String ranges) {
        String[] rangesArray = ranges.split("[,]", -1);
        for (String range : rangesArray) {
            String[] startendArray = range.split("[-]", -1);
            if (startendArray.length == 2) {
                initRange(startendArray[0], startendArray[1]);
            } else {
                log.errorFormat("{0}: Failed to initialize Mac Pool range. Please fix Mac Pool range: {1}",
                        instanceId(),
                        range);

            }
        }
        if (availableMacs.isEmpty()) {
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
        }
    }

    private void initRange(String start, String end) {
        List<String> macAddresses = MacAddressRangeUtils.initRange(start, end, maxMacsInPool);

        if (macAddresses.size() + availableMacs.size() > maxMacsInPool) {
            availableMacs.addAll(macAddresses.subList(0, maxMacsInPool - availableMacs.size()));
            log.errorFormat("{0}: Failed to initialize Mac Pool range. Please fix Mac Pool range: <{1}, {2}>",
                    instanceId(),
                    start,
                    end);
            throw new MacPoolExceededMaxException();
        } else {
            availableMacs.addAll(macAddresses);
        }
    }

    @Override
    public String allocateNewMac() {
        String mac = null;
        lockObj.writeLock().lock();
        try {
            if (!initialized) {
                logInitializationError("Failed to allocate new Mac address.");
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
            }
            if (availableMacs.isEmpty()) {
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NO_MACS_LEFT);
            }
            Iterator<String> my = availableMacs.iterator();
            mac = my.next();
            commitNewMac(mac);
        } finally {
            lockObj.writeLock().unlock();
        }
        return mac;
    }

    /**
     * Adds the given MAC to the allocateMacs Map, and removes it from the Set of available MACs. Should only be called
     * with a MAC that is in the Set of available MACs.
     */
    private boolean commitNewMac(String mac) {
        availableMacs.remove(mac);
        allocatedMacs.put(mac, 1);
        if (availableMacs.isEmpty()) {
            logMacPoolEmpty();
            return false;
        }
        return true;
    }

    void logMacPoolEmpty() {
        AuditLogableBase logable = new AuditLogableBase();
        AuditLogDirector.log(logable, AuditLogType.MAC_POOL_EMPTY);
    }

    @Override
    public int getAvailableMacsCount() {
        lockObj.readLock().lock();
        try {
            if (!initialized) {
                logInitializationError("Failed to get available Macs count.");
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
            }

            int availableMacsSize = availableMacs.size();
            log.debugFormat("{0}: Number of available Mac addresses = {1}", instanceId(), availableMacsSize);
            return availableMacsSize;
        } finally {
            lockObj.readLock().unlock();
        }
    }

    @Override
    public void freeMac(String mac) {
        lockObj.writeLock().lock();
        try {
            if (!initialized) {
                logInitializationError("Failed to free mac address " + mac + " .");
            } else {
                internalFreeMac(mac);
            }
        } finally {
            lockObj.writeLock().unlock();
        }
    }

    private void logInitializationError(String message) {
        log.errorFormat("{0}: The MAC addresses pool is not initialized", instanceId());
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("Message", message);
        AuditLogDirector.log(logable, AuditLogType.MAC_ADDRESSES_POOL_NOT_INITIALIZED);
    }

    private void internalFreeMac(String mac) {
        if (allocatedMacs.containsKey(mac)) {
            removeMacFromMap(allocatedMacs, mac);
            if (!allocatedMacs.containsKey(mac)) {
                availableMacs.add(mac);
            }
        } else if (allocatedCustomMacs.containsKey(mac)) {
            removeMacFromMap(allocatedCustomMacs, mac);
        }
    }

    private void removeMacFromMap(Map<String, Integer> macMap, String mac) {
        if (macMap.get(mac) <= 1) {
            macMap.remove(mac);
        } else {
            decrementMacInMap(macMap, mac);
        }
    }

    /**
     * Add given MAC address if possible.
     * Add user define mac address Function return false if the mac is in use
     * @return true if MAC was added successfully, and false if the MAC is in use and
     *         {@link org.ovirt.engine.core.common.config.ConfigValues#AllowDuplicateMacAddresses} is set to false
     */
    @Override
    public boolean addMac(String mac) {
        boolean retVal = false;
        lockObj.writeLock().lock();
        try {
            if (availableMacs.contains(mac)) {
                retVal = commitNewMac(mac);
            } else if (allocatedMacs.containsKey(mac)) {
                retVal = addMacToMap(allocatedMacs, mac);
            } else {
                retVal = addMacToMap(allocatedCustomMacs, mac);
            }
        } finally {
            lockObj.writeLock().unlock();
        }
        return retVal;
    }

    /**
     * Add given MAC address, regardless of it being in use.
     */
    @Override
    public void forceAddMac(String mac) {
        lockObj.writeLock().lock();
        try {
            if (availableMacs.contains(mac)) {
                commitNewMac(mac);
            } else if (allocatedMacs.containsKey(mac)) {
                incrementMacInMap(allocatedMacs, mac);
            } else if (allocatedCustomMacs.containsKey(mac)) {
                incrementMacInMap(allocatedCustomMacs, mac);
            } else {
                allocatedCustomMacs.put(mac, 1);
            }
        } finally {
            lockObj.writeLock().unlock();
        }
    }

    @Override
    public boolean isMacInUse(String mac) {
        lockObj.readLock().lock();
        try {
            return allocatedMacs.containsKey(mac) || allocatedCustomMacs.containsKey(mac);
        } finally {
            lockObj.readLock().unlock();
        }
    }

    @Override
    public void freeMacs(List<String> macs) {
        if (!macs.isEmpty()) {
            lockObj.writeLock().lock();
            try {
                if (!initialized) {
                    logInitializationError("Failed to free MAC addresses.");
                }
                for (String mac : macs) {
                    internalFreeMac(mac);
                }

            } finally {
                lockObj.writeLock().unlock();
            }
        }
    }

    @SuppressWarnings("serial")
    private static class MacPoolExceededMaxException extends RuntimeException {
    }

    /**
     * Adds the given MAC to the given Map, either by putting it in the Map, or incrementing its counter. If MAC is
     * already taken and {@link org.ovirt.engine.core.common.config.ConfigValues#AllowDuplicateMacAddresses} is set to false, returns false and does not add
     * MAC to Map
     *
     * @param macMap
     *            the Map to add the MAC to
     * @param mac
     *            the MAC address to add
     * @return true if succeeded, false otherwise
     */
    private boolean addMacToMap(Map<String, Integer> macMap, String mac) {
        if (!macMap.containsKey(mac)) {
            macMap.put(mac, 1);
            return true;
        } else if (allowDuplicates) {
            incrementMacInMap(macMap, mac);
            return true;
        }
        return false;
    }

    private void incrementMacInMap(Map<String, Integer> macMap, String mac) {
        macMap.put(mac, macMap.get(mac) + 1);
    }

    private void decrementMacInMap(Map<String, Integer> macMap, String mac) {
        macMap.put(mac, macMap.get(mac) - 1);
    }

    /**
     * Allocates mac addresses according to the input, sorting them in ascending order
     *
     * @param numberOfAddresses
     *            The number of MAC addresses to allocate
     * @return The list of MAC addresses, sorted in ascending order
     */
    @Override
    public List<String> allocateMacAddresses(int numberOfAddresses) {
        List<String> macAddresses = new ArrayList<>(numberOfAddresses);

        for (int i = 0; i < numberOfAddresses; ++i) {
            macAddresses.add(this.allocateNewMac());
        }
        Collections.sort(macAddresses);

        return macAddresses;
    }

}
