package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatistics implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 69893283302260434L;

    private Guid id;
    private BigDecimal cpuIdle;
    private BigDecimal cpuLoad;
    private BigDecimal cpuSys;
    private BigDecimal cpuUser;
    private Integer usageMemPercent;
    private Integer usageCpuPercent;
    private Integer usageNetworkPercent;
    private Long memAvailable;
    private Long memFree;
    private Long memShared;
    private Long swapFree;
    private Long swapTotal;
    private Integer ksmCpuPercent;
    private Long ksmPages;
    private Boolean ksmState;
    private int anonymousHugePages;
    private Long bootTime;
    // The following values store the state of the Hosted Engine HA environment
    // for each host and allow the user to see/change that state through the
    // engine UI.  They originate in the HA agent and are updated with the other
    // stats in vdsm's getVdsStats call.
    private int highlyAvailableScore;
    private boolean highlyAvailableIsConfigured;
    private boolean highlyAvailableIsActive;
    private boolean highlyAvailableGlobalMaintenance;
    private boolean highlyAvailableLocalMaintenance;
    private Date cpuOverCommitTimeStamp;

    private List<CpuStatistics> cpuCoreStatistics;
    private List<V2VJobInfo> v2vJobs;

    public VdsStatistics() {
        cpuIdle = BigDecimal.ZERO;
        cpuLoad = BigDecimal.ZERO;
        cpuSys = BigDecimal.ZERO;
        cpuUser = BigDecimal.ZERO;
        memAvailable = 0L;
        memFree = 0L;
        memShared = 0L;
        swapFree = 0L;
        swapTotal = 0L;
        ksmPages = 0L;
        bootTime = null;
        highlyAvailableScore = 0;
        highlyAvailableIsConfigured = false;
        highlyAvailableIsActive = false;
        highlyAvailableGlobalMaintenance = false;
        highlyAvailableLocalMaintenance = false;
        cpuCoreStatistics = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                cpuIdle,
                cpuLoad,
                cpuSys,
                cpuUser,
                memAvailable,
                memFree,
                memShared,
                usageCpuPercent,
                usageNetworkPercent,
                ksmState,
                ksmPages,
                ksmCpuPercent,
                swapTotal,
                swapFree,
                anonymousHugePages,
                bootTime,
                highlyAvailableScore,
                highlyAvailableIsConfigured,
                highlyAvailableIsActive,
                highlyAvailableGlobalMaintenance,
                highlyAvailableLocalMaintenance,
                cpuCoreStatistics,
                cpuOverCommitTimeStamp
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsStatistics)) {
            return false;
        }
        VdsStatistics other = (VdsStatistics) obj;
        return Objects.equals(id, other.id)
                && ObjectUtils.bigDecimalEqual(cpuIdle, other.cpuIdle)
                && ObjectUtils.bigDecimalEqual(cpuLoad, other.cpuLoad)
                && ObjectUtils.bigDecimalEqual(cpuSys, other.cpuSys)
                && ObjectUtils.bigDecimalEqual(cpuUser, other.cpuUser)
                && Objects.equals(memAvailable, other.memAvailable)
                && Objects.equals(memFree, other.memFree)
                && Objects.equals(memShared, other.memShared)
                && Objects.equals(usageCpuPercent, other.usageCpuPercent)
                && Objects.equals(usageNetworkPercent, other.usageNetworkPercent)
                && Objects.equals(ksmState, other.ksmState)
                && Objects.equals(ksmPages, other.ksmPages)
                && Objects.equals(ksmCpuPercent, other.ksmCpuPercent)
                && Objects.equals(swapTotal, other.swapTotal)
                && Objects.equals(swapFree, other.swapFree)
                && (anonymousHugePages == other.anonymousHugePages)
                && Objects.equals(bootTime, other.bootTime)
                && (highlyAvailableScore == other.highlyAvailableScore)
                && (highlyAvailableIsConfigured == other.highlyAvailableIsConfigured)
                && (highlyAvailableIsActive == other.highlyAvailableIsActive)
                && (highlyAvailableGlobalMaintenance == other.highlyAvailableGlobalMaintenance)
                && (highlyAvailableLocalMaintenance == other.highlyAvailableLocalMaintenance)
                && Objects.equals(cpuCoreStatistics, other.cpuCoreStatistics)
                && Objects.equals(cpuOverCommitTimeStamp, other.cpuOverCommitTimeStamp);
    }

    public int getAnonymousHugePages() {
        return anonymousHugePages;
    }

    public void setAnonymousHugePages(int value) {
        anonymousHugePages = value;
    }

    public Double getCpuIdle() {
        return cpuIdle.doubleValue();
    }

    public void setCpuIdle(Double cpuIdle) {
        this.cpuIdle = BigDecimal.valueOf(cpuIdle);
    }

    public Double getCpuLoad() {
        return cpuLoad.doubleValue();
    }

    public void setCpuLoad(Double cpuLoad) {
        this.cpuLoad = BigDecimal.valueOf(cpuLoad);
    }

    public Double getCpuSys() {
        return cpuSys.doubleValue();
    }

    public void setCpuSys(Double cpuSys) {
        this.cpuSys = BigDecimal.valueOf(cpuSys);
    }

    public Double getCpuUser() {
        return cpuUser.doubleValue();
    }

    public void setCpuUser(Double cpuUser) {
        this.cpuUser = BigDecimal.valueOf(cpuUser);
    }

    /**
     * Returns a rough estimate on how much free mem is available for new vm
     * i.e. MemFree + Cached + Buffers + resident - memCommitted
     *
     * resident set size of qemu processes may grow - up to  memCommitted.
     * Thus, we deduct the growth potential of qemu processes, which is (memCommitted - resident)
     * @return - free mem available for new vm in MiB
     */
    public Long getMemAvailable() {
        return memAvailable;
    }

    public void setMemAvailable(Long value) {
        memAvailable = value;
    }

    /**
     * Returns the actual free memory on host (MB) as it appears in the host's memInfo.
     * i.e. MemFree + Cached + Buffers
     * @return - actual free memory on host
     */
    public Long getMemFree() {
        return memFree;
    }

    public void setMemFree(Long value) {
        memFree = value;
    }

    public Long getMemShared() {
        return memShared;
    }

    public void setMemShared(Long value) {
        memShared = value;
    }

    public Integer getUsageCpuPercent() {
        return usageCpuPercent;
    }

    public void setUsageCpuPercent(Integer value) {
        usageCpuPercent = value;
    }

    public Integer getUsageMemPercent() {
        return usageMemPercent;
    }

    public void setUsageMemPercent(Integer value) {
        usageMemPercent = value;
    }

    public Integer getUsageNetworkPercent() {
        return usageNetworkPercent;
    }

    public void setUsageNetworkPercent(Integer value) {
        usageNetworkPercent = value;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Long getSwapFree() {
        return swapFree;
    }

    public void setSwapFree(Long value) {
        swapFree = value;
    }

    public Long getSwapTotal() {
        return swapTotal;
    }

    public void setSwapTotal(Long value) {
        swapTotal = value;
    }

    public Integer getKsmCpuPercent() {
        return ksmCpuPercent;
    }

    public void setKsmCpuPercent(Integer value) {
        ksmCpuPercent = value;
    }

    public Long getKsmPages() {
        return ksmPages;
    }

    public void setKsmPages(Long value) {
        ksmPages = value;
    }

    public Boolean getKsmState() {
        return ksmState;
    }

    public void setKsmState(Boolean value) {
        ksmState = value;
    }

    public Long getBootTime() {
        return bootTime;
    }

    public void setBootTime(Long value) {
        bootTime = value;
    }

    public int getHighlyAvailableScore() {
        return highlyAvailableScore;
    }

    public void setHighlyAvailableScore(int value) {
        highlyAvailableScore = value;
    }

    public boolean getHighlyAvailableIsConfigured() {
        return highlyAvailableIsConfigured;
    }

    public void setHighlyAvailableIsConfigured(boolean value) {
        highlyAvailableIsConfigured = value;
    }

    public boolean getHighlyAvailableIsActive() {
        return highlyAvailableIsActive;
    }

    public void setHighlyAvailableIsActive(boolean value) {
        highlyAvailableIsActive = value;
    }

    public boolean getHighlyAvailableGlobalMaintenance() {
        return highlyAvailableGlobalMaintenance;
    }

    public void setHighlyAvailableGlobalMaintenance(boolean value) {
        highlyAvailableGlobalMaintenance = value;
    }

    public boolean getHighlyAvailableLocalMaintenance() {
        return highlyAvailableLocalMaintenance;
    }

    public void setHighlyAvailableLocalMaintenance(boolean value) {
        highlyAvailableLocalMaintenance = value;
    }

    public List<CpuStatistics> getCpuCoreStatistics() {
        return cpuCoreStatistics;
    }

    public void setCpuCoreStatistics(List<CpuStatistics> cpuCoreStatistics) {
        this.cpuCoreStatistics = cpuCoreStatistics;
    }

    public Date getCpuOverCommitTimeStamp() {
        return cpuOverCommitTimeStamp;
    }

    public void setCpuOverCommitTimeStamp(Date value) {
        cpuOverCommitTimeStamp = value;
    }

    public List<V2VJobInfo> getV2VJobs() {
        return v2vJobs;
    }

    public void setV2VJobs(List<V2VJobInfo> v2vJobs) {
        this.v2vJobs = v2vJobs;
    }
}
