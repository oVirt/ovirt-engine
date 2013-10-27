package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

public class VDSPartial extends VDS {

    private static final long serialVersionUID = -3143712074770629093L;

    @Override
    public Version getVdsGroupCompatibilityVersion() {
        return super.getVdsGroupCompatibilityVersion();
    }

    @Override
    public boolean getContainingHooks() {
        return super.getContainingHooks();
    }

    @Override
    public String getHooksStr() {
        return super.getHooksStr();
    }

    @Override
    public Guid getVdsGroupId() {
        return super.getVdsGroupId();
    }

    @Override
    public String getVdsGroupName() {
        return super.getVdsGroupName();
    }

    @Override
    public String getVdsGroupDescription() {
        return super.getVdsGroupDescription();
    }

    @Override
    public String getVdsGroupCpuName() {
        return super.getVdsGroupCpuName();
    }

    @Override
    public Boolean getVdsGroupSupportsVirtService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean getVdsGroupSupportsGlusterService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Guid getId() {
        return super.getId();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getManagementIp() {
        return super.getManagementIp();
    }

    @Override
    public String getUniqueId() {
        return super.getUniqueId();
    }

    @Override
    public String getHostName() {
        return super.getHostName();
    }

    @Override
    public String getComment() {
        return super.getComment();
    }

    @Override
    public int getPort() {
        return super.getPort();
    }

    @Override
    public int getSshPort() {
        return super.getSshPort();
    }

    @Override
    public String getSshUsername() {
        return super.getSshUsername();
    }

    @Override
    public boolean isServerSslEnabled() {
        return super.isServerSslEnabled();
    }

    @Override
    public VDSType getVdsType() {
        return super.getVdsType();
    }

    @Override
    public VDSStatus getStatus() {
        return super.getStatus();
    }

    @Override
    public Integer getCpuCores() {
        return super.getCpuCores();
    }

    @Override
    public Integer getCpuThreads() {
        return super.getCpuThreads();
    }

    @Override
    public String getHardwareUUID() {
        return super.getHardwareUUID();
    }

    @Override
    public String getHardwareManufacturer() {
        return super.getHardwareManufacturer();
    }

    @Override
    public String getHardwareFamily() {
        return super.getHardwareFamily();
    }

    @Override
    public String getHardwareSerialNumber() {
        return super.getHardwareSerialNumber();
    }

    @Override
    public String getHardwareProductName() {
        return super.getHardwareProductName();
    }

    @Override
    public String getHardwareVersion() {
        return super.getHardwareVersion();
    }

    @Override
    public Integer getCpuSockets() {
        return super.getCpuSockets();
    }

    @Override
    public String getCpuModel() {
        return super.getCpuModel();
    }

    @Override
    public Double getCpuSpeedMh() {
        return super.getCpuSpeedMh();
    }

    @Override
    public String getIfTotalSpeed() {
        return super.getIfTotalSpeed();
    }

    @Override
    public Boolean getKvmEnabled() {
        return super.getKvmEnabled();
    }

    @Override
    public Integer getPhysicalMemMb() {
        return super.getPhysicalMemMb();
    }

    @Override
    public String getSupportedClusterLevels() {
        return super.getSupportedClusterLevels();
    }

    @Override
    public HashSet<Version> getSupportedClusterVersionsSet() {
        return super.getSupportedClusterVersionsSet();
    }

    @Override
    public String getSupportedEngines() {
        return super.getSupportedEngines();
    }

    @Override
    public HashSet<Version> getSupportedENGINESVersionsSet() {
        return super.getSupportedENGINESVersionsSet();
    }

    @Override
    public Double getCpuIdle() {
        return super.getCpuIdle();
    }

    @Override
    public Double getCpuLoad() {
        return super.getCpuLoad();
    }

    @Override
    public Double getCpuSys() {
        return super.getCpuSys();
    }

    @Override
    public Double getCpuUser() {
        return super.getCpuUser();
    }

    @Override
    public Integer getMemCommited() {
        return super.getMemCommited();
    }

    @Override
    public Integer getVmActive() {
        return super.getVmActive();
    }

    @Override
    public Integer getHighlyAvailableScore() {
        return super.getHighlyAvailableScore();
    }

    @Override
    public int getVmCount() {
        return super.getVmCount();
    }

    @Override
    public Integer getVmsCoresCount() {
        return super.getVmsCoresCount();
    }

    @Override
    public Integer getVmMigrating() {
        return super.getVmMigrating();
    }

    @Override
    public Integer getUsageMemPercent() {
        return super.getUsageMemPercent();
    }

    @Override
    public Integer getUsageCpuPercent() {
        return super.getUsageCpuPercent();
    }

    @Override
    public Integer getUsageNetworkPercent() {
        return super.getUsageNetworkPercent();
    }

    @Override
    public Integer getGuestOverhead() {
        return super.getGuestOverhead();
    }

    @Override
    public Integer getReservedMem() {
        return super.getReservedMem();
    }

    @Override
    public VDSStatus getPreviousStatus() {
        return super.getPreviousStatus();
    }

    @Override
    public Long getMemAvailable() {
        return super.getMemAvailable();
    }

    @Override
    public Long getMemFree() {
        return super.getMemFree();
    }

    @Override
    public Long getMemShared() {
        return super.getMemShared();
    }

    @Override
    public String getConsoleAddress() {
        return super.getConsoleAddress();
    }

    @Override
    public Integer getMemCommitedPercent() {
        return super.getMemCommitedPercent();
    }

    @Override
    public Integer getMemSharedPercent() {
        return super.getMemSharedPercent();
    }

    @Override
    public Long getSwapFree() {
        return super.getSwapFree();
    }

    @Override
    public Long getSwapTotal() {
        return super.getSwapTotal();
    }

    @Override
    public Integer getKsmCpuPercent() {
        return super.getKsmCpuPercent();
    }

    @Override
    public Long getKsmPages() {
        return super.getKsmPages();
    }

    @Override
    public Boolean getKsmState() {
        return super.getKsmState();
    }

    @Override
    public String getSoftwareVersion() {
        return super.getSoftwareVersion();
    }

    @Override
    public String getVersionName() {
        return super.getVersionName();
    }

    @Override
    public String getBuildName() {
        return super.getBuildName();
    }

    @Override
    public String getCpuFlags() {
        return super.getCpuFlags();
    }

    @Override
    public Date getCpuOverCommitTimestamp() {
        return super.getCpuOverCommitTimestamp();
    }

    @Override
    public int getVdsStrength() {
        return super.getVdsStrength();
    }

    @Override
    public int getHighUtilization() {
        return super.getHighUtilization();
    }

    @Override
    public int getLowUtilization() {
        return super.getLowUtilization();
    }

    @Override
    public int getCpuOverCommitDurationMinutes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Guid getStoragePoolId() {
        return super.getStoragePoolId();
    }

    @Override
    public String getStoragePoolName() {
        return super.getStoragePoolName();
    }

    @Override
    public int getMaxVdsMemoryOverCommit() {
        return super.getMaxVdsMemoryOverCommit();
    }

    @Override
    public Integer getPendingVcpusCount() {
        return super.getPendingVcpusCount();
    }

    @Override
    public int getPendingVmemSize() {
        return super.getPendingVmemSize();
    }

    @Override
    public Boolean getNetConfigDirty() {
        return super.getNetConfigDirty();
    }

    @Override
    public String getPmType() {
        return super.getPmType();
    }

    @Override
    public String getPmUser() {
        return super.getPmUser();
    }

    @Override
    public String getPmPassword() {
        return super.getPmPassword();
    }

    @Override
    public Integer getPmPort() {
        return super.getPmPort();
    }

    @Override
    public String getPmOptions() {
        return super.getPmOptions();
    }

    @Override
    public HashMap<String, String> getPmOptionsMap() {
        return super.getPmOptionsMap();
    }

    @Override
    public HashMap<String, String> getPmSecondaryOptionsMap() {
        return super.getPmSecondaryOptionsMap();
    }

    @Override
    public boolean getpm_enabled() {
        return super.getpm_enabled();
    }

    @Override
    public String getPmProxyPreferences() {
        return super.getPmProxyPreferences();
    }

    @Override
    public String getPmSecondaryIp() {
        return super.getPmSecondaryIp();
    }

    @Override
    public String getPmSecondaryType() {
        return super.getPmSecondaryType();
    }

    @Override
    public String getPmSecondaryUser() {
        return super.getPmSecondaryUser();
    }

    @Override
    public String getPmSecondaryPassword() {
        return super.getPmSecondaryPassword();
    }

    @Override
    public Integer getPmSecondaryPort() {
        return super.getPmSecondaryPort();
    }

    @Override
    public String getPmSecondaryOptions() {
        return super.getPmSecondaryOptions();
    }

    @Override
    public boolean isPmSecondaryConcurrent() {
        return super.isPmSecondaryConcurrent();
    }

    @Override
    public String getHostOs() {
        return super.getHostOs();
    }

    @Override
    public String getKvmVersion() {
        return super.getKvmVersion();
    }

    @Override
    public RpmVersion getLibvirtVersion() {
        return super.getLibvirtVersion();
    }

    @Override
    public String getSpiceVersion() {
        return super.getSpiceVersion();
    }

    @Override
    public String getKernelVersion() {
        return super.getKernelVersion();
    }

    @Override
    public String getIScsiInitiatorName() {
        return super.getIScsiInitiatorName();
    }

    @Override
    public Map<String, List<Map<String, String>>> getHBAs() {
        return super.getHBAs();
    }

    @Override
    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return super.getTransparentHugePagesState();
    }

    @Override
    public int getAnonymousHugePages() {
        return super.getAnonymousHugePages();
    }

    @Override
    public VdsStatic getStaticData() {
        return super.getStaticData();
    }

    @Override
    public VdsDynamic getDynamicData() {
        return super.getDynamicData();
    }

    @Override
    public VdsStatistics getStatisticsData() {
        return super.getStatisticsData();
    }

    @Override
    public ArrayList<Network> getNetworks() {
        return super.getNetworks();
    }

    @Override
    public ArrayList<VdsNetworkInterface> getInterfaces() {
        return super.getInterfaces();
    }

    public ArrayList<VDSDomainsData> getDomains() {
        return super.getDomains();
    }

    @Override
    public Double getImagesLastCheck() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double getImagesLastDelay() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RpmVersion getVersion() {
        return super.getVersion();
    }

    @Override
    public String getPartialVersion() {
        return super.getPartialVersion();
    }

    @Override
    public ServerCpu getCpuName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getVdsSpmId() {
        return super.getVdsSpmId();
    }

    @Override
    public long getOtpValidity() {
        return super.getOtpValidity();
    }

    @Override
    public int getVdsSpmPriority() {
        return super.getVdsSpmPriority();
    }

    @Override
    public Object getQueryableId() {
        return super.getQueryableId();
    }

    @Override
    public VdsSpmStatus getSpmStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NonOperationalReason getNonOperationalReason() {
        return super.getNonOperationalReason();
    }

    @Override
    public Map<String, Long> getLocalDisksUsage() {
        return super.getLocalDisksUsage();
    }

    @Override
    public boolean isAutoRecoverable() {
        return super.isAutoRecoverable();
    }

    @Override
    public String getSshKeyFingerprint() {
        return super.getSshKeyFingerprint();
    }

    @Override
    public float getMaxSchedulingMemory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getActiveNic() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSupportedEmulatedMachines() {
        return super.getSupportedEmulatedMachines();
    }

    public VDS clone() {
        VDS vds = new VDSPartial();
        // TODO: Create clone to dynamic, static, statistics --> and reuse in VDS
        vds.setDynamicData(getDynamicData());
        vds.setStaticData(getStaticData());
        vds.setStatisticsData(getStatisticsData());
        vds.setVdsGroupCompatibilityVersion(getVdsGroupCompatibilityVersion());
        vds.setVdsGroupCpuName(getVdsGroupCpuName());
        vds.setVdsGroupDescription(getVdsGroupDescription());
        vds.setVdsGroupName(getVdsGroupName());
        vds.setStoragePoolId(getStoragePoolId());
        vds.setStoragePoolName(getStoragePoolName());
        vds.getInterfaces().addAll(getInterfaces());
        vds.setDomains(getDomains());
        vds.getNetworks().addAll(getNetworks());
        vds.setVdsSpmId(vds.getVdsSpmId());

        vds.setHighUtilization(getHighUtilization());
        vds.setLowUtilization(getLowUtilization());
        vds.setLocalDisksUsage(getLocalDisksUsage());

        return vds;
    }
}
