package org.ovirt.engine.core.bll.network.macpool;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

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
    public void forceAddMac(String mac) {
        macPool.forceAddMac(mac);
    }

    @Override
    public void forceAddMacs(List<String> macs) {
        macPool.forceAddMacs(macs);
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
    public final String toString() {
        ToStringBuilder result = ToStringBuilder.forInstance(this);

        if (macPool != null) {
            result.append("macPool", macPool.toString());
        }

        return result.build();
    }
}
