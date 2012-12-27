package org.ovirt.engine.core.bll.network;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.LongCompat;
import org.ovirt.engine.core.compat.NumberStyles;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MacPoolManager {

    private static final MacPoolManager _instance = new MacPoolManager();

    public static MacPoolManager getInstance() {
        return _instance;
    }

    private final Set<String> mAvailableMacs = new HashSet<String>();
    private final Set<String> mAllocatedMacs = new HashSet<String>();
    private final Set<String> mAllocatedCustomMacs = new HashSet<String>();
    private final ReentrantReadWriteLock mLocObj = new ReentrantReadWriteLock();
    private boolean mInitialized = false;

    public void initialize() {
        mLocObj.writeLock().lock();
        try {
            String ranges = Config.<String> GetValue(ConfigValues.MacPoolRanges);
            if (!"".equals(ranges)) {
                try {
                    initRanges(ranges);
                } catch (MacPoolExceededMaxException e) {
                    log.errorFormat("MAC Pool range exceeded maximum number of mac pool addressed. Please check Mac Pool configuration.");
                }
            }

            List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao().getAll();
            for (VmNetworkInterface iface: interfaces) {
                AddMac(iface.getMacAddress());
            }
            mInitialized = true;
        } finally {
            mLocObj.writeLock().unlock();
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
        if (mAvailableMacs.isEmpty()) {
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
        }
    }

    private String ParseRangePart(String start) {
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
        String parsedRangeStart = ParseRangePart(start);
        String parsedRangeEnd = ParseRangePart(end);
        if (parsedRangeEnd == null || parsedRangeStart == null) {
            return false;
        }
        long startNum = LongCompat.parseLong(ParseRangePart(start), NumberStyles.HexNumber);
        long endNum = LongCompat.parseLong(ParseRangePart(end), NumberStyles.HexNumber);
        if (startNum > endNum) {
            // throw new
            // VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
            return false;
        }
        for (long i = startNum; i <= endNum; i++) {
            String value = String.format("%x", i);
            if (value.length() > 12) {
                // throw new
                // VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
                return false;
            } else if (value.length() < 12) {
                value = StringHelper.padLeft(value, 12, '0');
            }
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < value.length(); j += 2) {
                builder.append(value.substring(j, j + 2));
                builder.append(":");
            }
            value = builder.toString();
            value = value.substring(0, value.length() - 1);
            if (!mAvailableMacs.contains(value)) {
                mAvailableMacs.add(value);
            }
            if (mAvailableMacs.size() > Config.<Integer> GetValue(ConfigValues.MaxMacsCountInPool)) {
                throw new MacPoolExceededMaxException();
            }
        }
        return true;
    }

    public String allocateNewMac() {
        String mac = null;
        log.info("MacPoolManager::allocateNewMac entered");
        mLocObj.writeLock().lock();
        try {
            if (!mInitialized) {
                logInitializationError("Failed to allocate new Mac address.");
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
            }
            if (mAvailableMacs.isEmpty()) {
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NO_MACS_LEFT);
            }
            Iterator<String> my = mAvailableMacs.iterator();
            mac = my.next();
            CommitNewMac(mac);
        } finally {
            mLocObj.writeLock().unlock();
        }
        log.infoFormat("MacPoolManager::allocateNewMac allocated mac = '{0}", mac);
        return mac;
    }

    private boolean CommitNewMac(String mac) {
        mAvailableMacs.remove(mac);
        mAllocatedMacs.add(mac);
        if (mAvailableMacs.isEmpty()) {
            AuditLogableBase logable = new AuditLogableBase();
            AuditLogDirector.log(logable, AuditLogType.MAC_POOL_EMPTY);
            return false;
        }
        return true;
    }

    public int getavailableMacsCount() {
        return mAvailableMacs.size();
    }

    public void freeMac(String mac) {
        log.infoFormat("MacPoolManager::freeMac(mac = '{0}') - entered", mac);
        mLocObj.writeLock().lock();
        try {
            if (!mInitialized) {
                logInitializationError("Failed to free mac address " + mac + " .");
            } else {
                internalFreeMac(mac);
            }
        } finally {
            mLocObj.writeLock().unlock();
        }
    }

    private void logInitializationError(String message) {
        log.error("The MAC addresses pool is not initialized");
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("Message",message);
        AuditLogDirector.log(logable, AuditLogType.MAC_ADDRESSES_POOL_NOT_INITIALIZED);
    }

    private void internalFreeMac(String mac) {
        if (mAllocatedCustomMacs.contains(mac)) {
            mAllocatedCustomMacs.remove(mac);
        } else if (mAllocatedMacs.contains(mac)) {
            mAllocatedMacs.remove(mac);
            mAvailableMacs.add(mac);
        }
    }

    /**
     * Add user define mac address Function return false if the mac is in use
     *
     * @param mac
     * @return
     */
    public boolean AddMac(String mac) {
        boolean retVal = true;
        mLocObj.writeLock().lock();
        try {
            if (mAllocatedMacs.contains(mac)) {
                retVal = false;
            } else {
                if (mAvailableMacs.contains(mac)) {
                    retVal = CommitNewMac(mac);
                } else if (mAllocatedCustomMacs.contains(mac)) {
                    retVal = false;
                } else {
                    mAllocatedCustomMacs.add(mac);
                }
            }
        } finally {
            mLocObj.writeLock().unlock();
        }
        return retVal;
    }

    public boolean IsMacInUse(String mac) {
        mLocObj.readLock().lock();
        try {
            return mAllocatedMacs.contains(mac) || mAllocatedCustomMacs.contains(mac);
        } finally {
            mLocObj.readLock().unlock();
        }
    }

    public void freeMacs(List<String> macs) {
        log.info("MacPoolManager::freeMacs - entered");
        mLocObj.writeLock().lock();
        try {
            if (!mInitialized) {
                logInitializationError("Failed to free MAC addresses.");
            }
            for (String mac : macs) {
                internalFreeMac(mac);
            }

        } finally {
            mLocObj.writeLock().unlock();
        }
    }

    private static Log log = LogFactory.getLog(MacPoolManager.class);

    @SuppressWarnings("serial")
    private class MacPoolExceededMaxException extends RuntimeException {
    }
}
