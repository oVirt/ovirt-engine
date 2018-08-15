package org.ovirt.engine.core.bll.network.macpool;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.AcquireReadLock;

public class DelegatingMacPoolDecorator implements MacPoolDecorator {
    protected MacPool macPool;

    public DelegatingMacPoolDecorator() {
    }

    @Override
    public String allocateNewMac() {
        return macPool.allocateNewMac();
    }

    @Override
    public int getAvailableMacsCount() {
        return macPool.getAvailableMacsCount();
    }

    @Override
    @AcquireReadLock
    public int getTotalMacsCount() {
        return macPool.getTotalMacsCount();
    }

    @Override
    public void freeMac(String mac) {
        macPool.freeMac(mac);
    }

    @Override
    public boolean addMac(String mac) {
        return macPool.addMac(mac);
    }

    @Override
    public List<String> addMacs(List<String> macs) {
        return macPool.addMacs(macs);
    }

    @Override
    public boolean isMacInUse(String mac) {
        return macPool.isMacInUse(mac);
    }

    @Override
    public void freeMacs(List<String> macs) {
        macPool.freeMacs(macs);
    }

    @Override
    public List<String> allocateMacAddresses(int numberOfAddresses) {
        return macPool.allocateMacAddresses(numberOfAddresses);
    }

    @Override
    public boolean canAllocateMacAddresses(int numberOfAddresses) {
        return getAvailableMacsCount() >= numberOfAddresses;
    }

    @Override
    public boolean isMacInRange(String mac) {
        return macPool.isMacInRange(mac);
    }

    @Override
    public boolean isDuplicateMacAddressesAllowed() {
        return macPool.isDuplicateMacAddressesAllowed();
    }

    @Override
    public void setMacPool(MacPool macPool) {
        this.macPool = macPool;
    }

    @Override
    public Guid getId() {
        return macPool.getId();
    }

    @Override
    public boolean containsDuplicates() {
        return macPool.containsDuplicates();
    }

    @Override
    public MacsStorage getMacsStorage() {
        return macPool.getMacsStorage();
    }

    @Override
    public boolean overlaps(MacPool macPool) {
        return macPool.overlaps(macPool);
    }

    @Override
    public final String toString() {
        ToStringBuilder result = ToStringBuilder.forInstance(this);

        if (macPool != null) {
            result.append("macPool", macPool.toString());
        }

        return result.build();
    }
}
