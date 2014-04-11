package org.ovirt.engine.core.bll.network.macpoolmanager;

public class MacPoolPerDcSingleton {
    private static MacPoolPerDc INSTANCE = new MacPoolPerDc();

    public static MacPoolPerDc getInstance() {
        return INSTANCE;
    }
}
