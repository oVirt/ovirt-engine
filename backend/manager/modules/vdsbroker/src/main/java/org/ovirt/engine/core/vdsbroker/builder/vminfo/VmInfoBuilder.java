package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.compat.Guid;

public interface VmInfoBuilder {
    void buildVmProperties(String hibernationVolHandle);

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

    void buildVmCD(VmPayload payload);

    void buildVmFloppy(VmPayload payload);

    void buildVmDrives();

    void buildVmNetworkInterfaces(Map<Guid, String> passthroughVnicToVfMap);

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
