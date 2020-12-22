package org.ovirt.engine.core.common.businessentities;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

/**
 * This interface describe InstanceType
 * when creating an instance from instance type
 * the instance type fields override the vm fields.
 * <p/>
 * NOTE: the following devices are part of instance type and not represented here:
 * Smart card, Payload, Balloon, Sound card
 */
public interface InstanceType extends BusinessEntity<Guid>, Nameable {

    void setName(String value);

    String getDescription();

    void setDescription(String value);

    int getMemSizeMb();

    void setMemSizeMb(int value);

    int getNumOfSockets();

    void setNumOfSockets(int value);

    int getCpuPerSocket();

    void setCpuPerSocket(int value);

    int getThreadsPerCpu();

    void setThreadsPerCpu(int value);

    List<VmNetworkInterface> getInterfaces();

    void setInterfaces(List<VmNetworkInterface> value);

    int getNumOfMonitors();

    void setNumOfMonitors(int value);

    UsbPolicy getUsbPolicy();

    void setUsbPolicy(UsbPolicy value);

    boolean isAutoStartup();

    void setAutoStartup(boolean value);

    BootSequence getDefaultBootSequence();

    void setDefaultBootSequence(BootSequence value);

    DisplayType getDefaultDisplayType();

    void setDefaultDisplayType(DisplayType value);

    int getPriority();

    void setPriority(int value);

    int getMinAllocatedMem();

    void setMinAllocatedMem(int value);

    Boolean getTunnelMigration();

    void setTunnelMigration(Boolean value);

    boolean isSmartcardEnabled();

    void setSmartcardEnabled(boolean smartcardEnabled);

    public MigrationSupport getMigrationSupport();

    public void setMigrationSupport(MigrationSupport migrationSupport);

    public void setMigrationDowntime(Integer migrationDowntime);

    public Integer getMigrationDowntime();

    String getCustomEmulatedMachine();

    void setCustomEmulatedMachine(String emulatedMachine);

    void setCustomCpuName(String customCpuName);

    String getCustomCpuName();

    int getNumOfIoThreads();

    Guid getMigrationPolicyId();

    // TODO: these should be add as well
    // userdefined_properties
    // predefined_properties
}
