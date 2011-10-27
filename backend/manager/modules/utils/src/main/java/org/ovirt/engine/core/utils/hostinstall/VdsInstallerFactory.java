package org.ovirt.engine.core.utils.hostinstall;

public final class VdsInstallerFactory {
    public static ICAWrapper CreateCaWrapper() {
        return new OpenSslCAWrapper();
    }

    public static IVdsInstallWrapper CreateVdsInstallWrapper() {
        return new MinaInstallWrapper();
    }
}
