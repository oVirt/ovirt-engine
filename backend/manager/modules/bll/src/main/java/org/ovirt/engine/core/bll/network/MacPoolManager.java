package org.ovirt.engine.core.bll.network;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MacPoolManager {

    private static final int HEX_RADIX = 16;
    private static final String INIT_ERROR_MSG = "Error in initializing MAC Addresses pool manager. ";
    private static final MacPoolManager INSTANCE = new MacPoolManager();

    private static Log log = LogFactory.getLog(MacPoolManager.class);

    /**
     * A Map that holds the allocated MAC addresses as keys, and counters as values. These MAC addresses were taken from
     * the range defined by the user in {@link ConfigValues#MacPoolRanges}
     */
    @SuppressWarnings("unchecked")
    private final Map<String, Integer> allocatedMacs = new CaseInsensitiveMap();

    /**
     * A Map that holds the allocated MAC addresses as keys, and counters as values. These MAC addresses were allocated
     * when user requested a specific MAC address that is out of the range defined in {@link ConfigValues#MacPoolRanges}
     */
    @SuppressWarnings("unchecked")
    private final Map<String, Integer> allocatedCustomMacs = new CaseInsensitiveMap();

    /**
     * A Set that holds the non-allocated MAC addresses from the range defined in {@link ConfigValues#MacPoolRanges}
     * Every MAC in that range should either be in this Set, or be mapped to a value greater to zero in allocatedMacs,
     * but not both
     */
    private final Set<String> availableMacs = new HashSet<String>();

    private final ReentrantReadWriteLock lockObj = new ReentrantReadWriteLock();
    private boolean initialized;

    private MacPoolManager() {
        // Empty ctor since this is singleton.
    }

    public static MacPoolManager getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        lockObj.writeLock().lock();
        try {
            log.info("Start initializing " + getClass().getSimpleName());
            String ranges = Config.<String> GetValue(ConfigValues.MacPoolRanges);
            if (!"".equals(ranges)) {
                try {
                    initRanges(ranges);
                } catch (MacPoolExceededMaxException e) {
                    log.error("MAC Pool range exceeded maximum number of mac pool addressed. Please check Mac Pool configuration.");
                }
            }

            List<VmNic> interfaces = DbFacade.getInstance().getVmNicDao().getAll();

            for (VmNic iface : interfaces) {
                forceAddMac(iface.getMacAddress());
            }
            initialized = true;
            log.info("Finished initializing " + getClass().getSimpleName());
        } catch (Exception ex) {
            log.error(INIT_ERROR_MSG, ex);
        } finally {
            lockObj.writeLock().unlock();
        }
    }

    private void initRanges(String ranges) {
        String[] rangesArray = ranges.split("[,]", -1);
        for (String range : rangesArray) {
            String[] startendArray = range.split("[-]", -1);
            if (startendArray.length == 2) {
                if (!initRange(startendArray[0], startendArray[1])) {
                    log.errorFormat("Failed to initialize Mac Pool range. Please fix Mac Pool range: {0}", range);
                }
            } else {
                log.errorFormat("Failed to initialize Mac Pool range. Please fix Mac Pool range: {0}", range);

            }
        }
        if (availableMacs.isEmpty()) {
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
        }
    }

    private String parseRangePart(String start) {
        StringBuilder builder = new StringBuilder();
        for (String part : start.split("[:]", -1)) {
            String tempPart = part.trim();
            if (tempPart.length() == 1) {
                builder.append('0');
            } else if (tempPart.length() > 2) {
                return null;
            }
            builder.append(tempPart);
        }
        return builder.toString();
    }

    private boolean initRange(String start, String end) {
        String parsedRangeStart = parseRangePart(start);
        String parsedRangeEnd = parseRangePart(end);
        if (parsedRangeEnd == null || parsedRangeStart == null) {
            return false;
        }
        long startNum = Long.parseLong(parseRangePart(start), HEX_RADIX);
        long endNum = Long.parseLong(parseRangePart(end), HEX_RADIX);
        if (startNum > endNum) {
            return false;
        }
        for (long i = startNum; i <= endNum; i++) {
            String value = String.format("%x", i);
            if (value.length() > 12) {
                return false;
            } else if (value.length() < 12) {
                value = StringUtils.leftPad(value, 12, '0');
            }
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < value.length(); j += 2) {
                builder.append(value.substring(j, j + 2));
                builder.append(":");
            }
            value = builder.toString();
            value = value.substring(0, value.length() - 1);
            if (!availableMacs.contains(value)) {
                availableMacs.add(value);
            }
            if (availableMacs.size() > Config.<Integer> GetValue(ConfigValues.MaxMacsCountInPool)) {
                throw new MacPoolExceededMaxException();
            }
        }
        return true;
    }

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
     *
     * @param mac
     * @return
     */
    private boolean commitNewMac(String mac) {
        availableMacs.remove(mac);
        allocatedMacs.put(mac, 1);
        if (availableMacs.isEmpty()) {
            AuditLogableBase logable = new AuditLogableBase();
            AuditLogDirector.log(logable, AuditLogType.MAC_POOL_EMPTY);
            return false;
        }
        return true;
    }

    public int getAvailableMacsCount() {
        lockObj.readLock().lock();
        try {
            if (!initialized) {
                logInitializationError("Failed to get available Macs count.");
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
            }

            int availableMacsSize = availableMacs.size();
            log.debugFormat("Number of available Mac addresses = {0}", availableMacsSize);
            return availableMacsSize;
        } finally {
            lockObj.readLock().unlock();
        }
    }

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
        log.error("The MAC addresses pool is not initialized");
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
     * @param mac
     * @return true if MAC was added successfully, and false if the MAC is in use and
     *         {@link ConfigValues#AllowDuplicateMacAddresses} is set to false
     */
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
     *
     * @param mac
     */
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

    public boolean isMacInUse(String mac) {
        lockObj.readLock().lock();
        try {
            return allocatedMacs.containsKey(mac) || allocatedCustomMacs.containsKey(mac);
        } finally {
            lockObj.readLock().unlock();
        }
    }

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
    private class MacPoolExceededMaxException extends RuntimeException {
    }

    private boolean allowDuplicate() {
        return Config.<Boolean> GetValue(ConfigValues.AllowDuplicateMacAddresses);
    }

    /**
     * Adds the given MAC to the given Map, either by putting it in the Map, or incrementing its counter. If MAC is
     * already taken and {@link ConfigValues#AllowDuplicateMacAddresses} is set to false, returns false and does not add
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
        } else if (allowDuplicate()) {
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
}
