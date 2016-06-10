package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.util.Map;

public interface VmInfoBuilder {
    void buildVmProperties();

    void buildVmNetworkCluster();

    void buildVmBootOptions();

    void buildVmTimeZone();

    void buildVmSerialNumber();

    void buildVmVideoCards();

    /**
     * Builds graphics cards for a vm. If there is a pre-filled information about graphics in graphics info (this means
     * vm is run via run once ), this information is used to create graphics devices. Otherwise graphics devices are
     * build from database.
     */
    void buildVmGraphicsDevices();

    void buildVmCD();

    void buildVmFloppy();

    void buildVmDrives();

    void buildVmNetworkInterfaces();

    void buildVmSoundDevices();

    void buildVmConsoleDevice();

    void buildUnmanagedDevices();

    void buildVmBootSequence();

    void buildSysprepVmPayload(String strSysPrepContent);

    void buildCloudInitVmPayload(Map<String, byte[]> cloudInitContent);

    void buildVmUsbDevices();

    void buildVmMemoryBalloon();

    void buildVmWatchdog();

    void buildVmVirtioScsi();

    void buildVmRngDevice();

    void buildVmVirtioSerial();

    void buildVmNumaProperties();

    void buildVmHostDevices();
}
