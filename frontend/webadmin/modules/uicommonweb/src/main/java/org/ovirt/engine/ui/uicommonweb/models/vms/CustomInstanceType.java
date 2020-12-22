package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Null object for instance types
 */
public class CustomInstanceType implements InstanceType {

    public static final CustomInstanceType INSTANCE = new CustomInstanceType();

    @Override
    public String getDescription() {
        return ConstantsManager.getInstance().getConstants().customInstanceTypeDescription();
    }

    @Override
    public String getName() {
        return ConstantsManager.getInstance().getConstants().customInstanceTypeName();
    }

    @Override
    public Guid getId() {
        return null;
    }

    @Override
    public void setName(String value) {

    }

    @Override
    public void setDescription(String value) {

    }

    @Override
    public int getMemSizeMb() {
        return 0;
    }

    @Override
    public void setMemSizeMb(int value) {

    }

    @Override
    public int getNumOfSockets() {
        return 0;
    }

    @Override
    public void setNumOfSockets(int value) {

    }

    @Override
    public int getCpuPerSocket() {
        return 0;
    }

    @Override
    public void setCpuPerSocket(int value) {

    }

    @Override
    public int getThreadsPerCpu() {
        return 0;
    }

    @Override
    public void setThreadsPerCpu(int value) {
    }

    @Override
    public List<VmNetworkInterface> getInterfaces() {
        return null;
    }

    @Override
    public void setInterfaces(List<VmNetworkInterface> value) {

    }

    @Override
    public int getNumOfMonitors() {
        return 0;
    }

    @Override
    public void setNumOfMonitors(int value) {

    }

    @Override
    public UsbPolicy getUsbPolicy() {
        return null;
    }

    @Override
    public void setUsbPolicy(UsbPolicy value) {

    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void setAutoStartup(boolean value) {

    }

    @Override
    public BootSequence getDefaultBootSequence() {
        // default boot sequence
        return BootSequence.C;
    }

    @Override
    public void setDefaultBootSequence(BootSequence value) {

    }

    @Override
    public DisplayType getDefaultDisplayType() {
        return null;
    }

    @Override
    public void setDefaultDisplayType(DisplayType value) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void setPriority(int value) {

    }

    @Override
    public int getMinAllocatedMem() {
        return 0;
    }

    @Override
    public void setMinAllocatedMem(int value) {

    }

    @Override
    public Boolean getTunnelMigration() {
        return Boolean.FALSE;
    }

    @Override
    public void setTunnelMigration(Boolean value) {

    }

    @Override
    public boolean isSmartcardEnabled() {
        return false;
    }

    @Override
    public void setSmartcardEnabled(boolean smartcardEnabled) {

    }

    @Override
    public MigrationSupport getMigrationSupport() {
        return null;
    }

    @Override
    public void setMigrationSupport(MigrationSupport migrationSupport) {

    }

    @Override
    public void setMigrationDowntime(Integer migrationDowntime) {

    }

    @Override
    public Integer getMigrationDowntime() {
        return null;
    }

    @Override
    public void setId(Guid id) {

    }

    public String getCustomEmulatedMachine() {
        return null;
    }

    public void setCustomEmulatedMachine(String emulatedMachine) {

    }

    @Override
    public void setCustomCpuName(String customCpuName) {

    }

    @Override
    public String getCustomCpuName() {
        return null;
    }

    @Override
    public int getNumOfIoThreads() {
        return 0;
    }

    @Override
    public Guid getMigrationPolicyId() {
        return null;
    }

}
