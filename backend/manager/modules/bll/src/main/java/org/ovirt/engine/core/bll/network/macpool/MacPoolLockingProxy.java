package org.ovirt.engine.core.bll.network.macpool;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ovirt.engine.core.utils.lock.AutoCloseableLock;

/**
 * Proxy class wrapping MacPool instance, synchronizing access to its methods.
 */
public class MacPoolLockingProxy implements MacPool {
    private final ReentrantReadWriteLock lockObj = new ReentrantReadWriteLock();
    private final MacPool macPool;

    public MacPoolLockingProxy(MacPool macPool) {
        Objects.requireNonNull(macPool);
        this.macPool = macPool;
    }

    @Override
    public int getAvailableMacsCount() {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.readLock())) {
            return macPool.getAvailableMacsCount();
        }
    }

    @Override
    public boolean isMacInUse(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.readLock())) {
            return macPool.isMacInUse(mac);
        }
    }

    @Override
    public String allocateNewMac() {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            return macPool.allocateNewMac();
        }
    }

    @Override
    public void freeMac(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            macPool.freeMac(mac);
        }
    }

    @Override
    public boolean addMac(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            return macPool.addMac(mac);
        }
    }

    @Override
    public void forceAddMac(String mac) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            macPool.forceAddMac(mac);
        }
    }

    @Override
    public void freeMacs(List<String> macs) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            macPool.freeMacs(macs);
        }
    }

    @Override
    public List<String> allocateMacAddresses(int numberOfAddresses) {
        try (AutoCloseableLock l = new AutoCloseableLock(lockObj.writeLock())) {
            return macPool.allocateMacAddresses(numberOfAddresses);
        }
    }

    @Override
    public boolean isDuplicateMacAddressesAllowed() {
        return macPool.isDuplicateMacAddressesAllowed();
    }
}
