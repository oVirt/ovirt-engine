package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "vds_statistics")
@Cacheable(true)
public class VdsStatistics implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 69893283302260434L;

    @Id
    @Column(name = "vds_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id;

    @Column(name = "cpu_idle")
    private BigDecimal cpuIdle;

    @Column(name = "cpu_load")
    private BigDecimal cpuLoad;

    @Column(name = "cpu_sys")
    private BigDecimal cpuSys;

    @Column(name = "cpu_user")
    private BigDecimal cpuUser;

    @Column(name = "usage_mem_percent")
    private Integer usageMemPercent;

    @Column(name = "usage_cpu_percent")
    private Integer usageCpuPercent;

    @Column(name = "usage_network_percent")
    private Integer usageNetworkPercent;

    @Column(name = "mem_available")
    private Long memAvailable;

    @Column(name = "mem_free")
    private Long memFree;

    @Column(name = "mem_shared")
    private Long memShared;

    @Column(name = "swap_free")
    private Long swapFree;

    @Column(name = "swap_total")
    private Long swapTotal;

    @Column(name = "ksm_cpu_percent")
    private Integer ksmCpuPercent;

    @Column(name = "ksm_pages")
    private Long ksmPages;

    @Column(name = "ksm_state")
    private Boolean ksmState;

    @Column(name = "anonymous_hugepages")
    private int anonymousHugePages;

    @Column(name = "boot_time")
    private Long bootTime;

    // The following values store the state of the Hosted Engine HA environment
    // for each host and allow the user to see/change that state through the
    // engine UI.  They originate in the HA agent and are updated with the other
    // stats in vdsm's getVdsStats call.
    @Column(name = "ha_score")
    private int highlyAvailableScore;

    @Column(name = "ha_configured")
    private boolean highlyAvailableIsConfigured;

    @Column(name = "ha_active")
    private boolean highlyAvailableIsActive;

    @Column(name = "ha_global_maintenance")
    private boolean highlyAvailableGlobalMaintenance;

    @Column(name = "ha_local_maintenance")
    private boolean highlyAvailableLocalMaintenance;

    @Column(name = "cpu_over_commit_time_stamp")
    private Date cpuOverCommitTimeStamp;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "vds_id", referencedColumnName = "vds_id")
    private List<CpuStatistics> cpuCoreStatistics;

    private transient List<V2VJobInfo> v2vJobs;

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
        return Objects.hash(id);
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
        return ObjectUtils.objectsEqual(id, other.id);
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
     * @return - free mem available for new vm
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
