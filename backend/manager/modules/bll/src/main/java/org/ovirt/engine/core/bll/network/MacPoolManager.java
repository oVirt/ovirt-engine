package org.ovirt.engine.core.bll.network;

import java.util.List;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class MacPoolManager {

    private final MacPoolManagerStrategy macPoolManagerStrategy;

    private static final MacPoolManager INSTANCE = new MacPoolManager();

    private MacPoolManager() {
        macPoolManagerStrategy = createDefaultScopeMacPoolManager();
    }

    private MacPoolManagerStrategy createDefaultScopeMacPoolManager() {
        final String macPoolRanges = Config.getValue(ConfigValues.MacPoolRanges);
        final Boolean allowDuplicates = Config.getValue(ConfigValues.AllowDuplicateMacAddresses);
        return new MacPoolManagerRangesRegisterAcquiredMacsOnInit(macPoolRanges, allowDuplicates);
    }

    public static MacPoolManager getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        macPoolManagerStrategy.initialize();
    }

    public boolean addMac(String mac) {
        return macPoolManagerStrategy.addMac(mac);
    }

    public boolean isMacInUse(String mac) {
        return macPoolManagerStrategy.isMacInUse(mac);
    }

    public List<String> allocateMacAddresses(int numberOfAddresses) {
        return macPoolManagerStrategy.allocateMacAddresses(numberOfAddresses);
    }

    public int getAvailableMacsCount() {
        return macPoolManagerStrategy.getAvailableMacsCount();
    }

    public void forceAddMac(String mac) {
        macPoolManagerStrategy.forceAddMac(mac);
    }

    public void freeMacs(List<String> macs) {
        macPoolManagerStrategy.freeMacs(macs);
    }

    public String allocateNewMac() {
        return macPoolManagerStrategy.allocateNewMac();
    }

    public void freeMac(String mac) {
        macPoolManagerStrategy.freeMac(mac);
    }
}
