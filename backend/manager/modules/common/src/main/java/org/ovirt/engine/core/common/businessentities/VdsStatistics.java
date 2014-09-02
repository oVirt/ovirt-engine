package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatistics implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 69893283302260434L;

    private Guid id;
    private BigDecimal cpu_idle;
    private BigDecimal cpu_load;
    private BigDecimal cpu_sys;
    private BigDecimal cpu_user;
    private Integer usage_mem_percent;
    private Integer usage_cpu_percent;
    private Integer usage_network_percent;
    private Long mem_available;
    private Long memFree;
    private Long mem_shared;
    private Long swap_free;
    private Long swap_total;
    private Integer ksm_cpu_percent;
    private Long ksm_pages;
    private Boolean ksm_state;
    private int anonymousHugePages;
    private Long boot_time;
    // The following values store the state of the Hosted Engine HA environment
    // for each host and allow the user to see/change that state through the
    // engine UI.  They originate in the HA agent and are updated with the other
    // stats in vdsm's getVdsStats call.
    private int highlyAvailableScore;
    private boolean highlyAvailableIsConfigured;
    private boolean highlyAvailableIsActive;
    private boolean highlyAvailableGlobalMaintenance;
    private boolean highlyAvailableLocalMaintenance;
    private Date cpu_over_commit_time_stamp;

    private List<CpuStatistics> cpuCoreStatistics;

    public VdsStatistics() {
        this.cpu_idle = BigDecimal.ZERO;
        this.cpu_load = BigDecimal.ZERO;
        this.cpu_sys = BigDecimal.ZERO;
        this.cpu_user = BigDecimal.ZERO;
        mem_available = 0L;
        memFree = 0L;
        mem_shared = 0L;
        swap_free = 0L;
        swap_total = 0L;
        ksm_pages = 0L;
        boot_time = null;
        highlyAvailableScore = 0;
        highlyAvailableIsConfigured = false;
        highlyAvailableIsActive = false;
        highlyAvailableGlobalMaintenance = false;
        highlyAvailableLocalMaintenance = false;
        cpuCoreStatistics = new ArrayList<CpuStatistics>();
    }

    public int getAnonymousHugePages() {
        return this.anonymousHugePages;
    }

    public void setAnonymousHugePages(int value) {
        this.anonymousHugePages = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((cpu_idle == null) ? 0 : cpu_idle.hashCode());
        result = prime * result + ((cpu_load == null) ? 0 : cpu_load.hashCode());
        result = prime * result + ((cpu_sys == null) ? 0 : cpu_sys.hashCode());
        result = prime * result + ((cpu_user == null) ? 0 : cpu_user.hashCode());
        result = prime * result + ((mem_available == null) ? 0 : mem_available.hashCode());
        result = prime * result + ((memFree == null) ? 0 : memFree.hashCode());
        result = prime * result + ((mem_shared == null) ? 0 : mem_shared.hashCode());
        result = prime * result + ((usage_cpu_percent == null) ? 0 : usage_cpu_percent.hashCode());
        result = prime * result + ((usage_network_percent == null) ? 0 : usage_network_percent.hashCode());
        result = prime * result + ((ksm_state == null) ? 0 : ksm_state.hashCode());
        result = prime * result + ((ksm_pages == null) ? 0 : ksm_pages.hashCode());
        result = prime * result + ((ksm_cpu_percent == null) ? 0 : ksm_cpu_percent.hashCode());
        result = prime * result + ((swap_total == null) ? 0 : swap_total.hashCode());
        result = prime * result + ((swap_free == null) ? 0 : swap_free.hashCode());
        result = prime * result + anonymousHugePages;
        result = prime * result + ((boot_time == null) ? 0 : boot_time.hashCode());
        result = prime * result + highlyAvailableScore;
        result = prime * result + (highlyAvailableIsConfigured ? 1231 : 1237);
        result = prime * result + (highlyAvailableIsActive ? 1231 : 1237);
        result = prime * result + (highlyAvailableGlobalMaintenance ? 1231 : 1237);
        result = prime * result + (highlyAvailableLocalMaintenance ? 1231 : 1237);
        result = prime * result + ((cpuCoreStatistics == null) ? 0 : cpuCoreStatistics.hashCode());
        result = prime * result + ((cpu_over_commit_time_stamp == null) ? 0 : cpu_over_commit_time_stamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VdsStatistics other = (VdsStatistics) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.bigDecimalEqual(cpu_idle, other.cpu_idle)
                && ObjectUtils.bigDecimalEqual(cpu_load, other.cpu_load)
                && ObjectUtils.bigDecimalEqual(cpu_sys, other.cpu_sys)
                && ObjectUtils.bigDecimalEqual(cpu_user, other.cpu_user)
                && ObjectUtils.objectsEqual(mem_available, other.mem_available)
                && ObjectUtils.objectsEqual(memFree, other.memFree)
                && ObjectUtils.objectsEqual(mem_shared, other.mem_shared)
                && ObjectUtils.objectsEqual(usage_cpu_percent, other.usage_cpu_percent)
                && ObjectUtils.objectsEqual(usage_network_percent, other.usage_network_percent)
                && ObjectUtils.objectsEqual(ksm_state, other.ksm_state)
                && ObjectUtils.objectsEqual(ksm_pages, other.ksm_pages)
                && ObjectUtils.objectsEqual(ksm_cpu_percent, other.ksm_cpu_percent)
                && ObjectUtils.objectsEqual(swap_total, other.swap_total)
                && ObjectUtils.objectsEqual(swap_free, other.swap_free)
                && (anonymousHugePages == other.anonymousHugePages)
                && ObjectUtils.objectsEqual(boot_time, other.boot_time)
                && (highlyAvailableScore == other.highlyAvailableScore)
                && (highlyAvailableIsConfigured == other.highlyAvailableIsConfigured)
                && (highlyAvailableIsActive == other.highlyAvailableIsActive)
                && (highlyAvailableGlobalMaintenance == other.highlyAvailableGlobalMaintenance)
                && (highlyAvailableLocalMaintenance == other.highlyAvailableLocalMaintenance)
                && ObjectUtils.objectsEqual(cpuCoreStatistics, other.cpuCoreStatistics)
                && ObjectUtils.objectsEqual(cpu_over_commit_time_stamp, other.cpu_over_commit_time_stamp));
    }

    public Double getcpu_idle() {
        return this.cpu_idle.doubleValue();
    }

    public void setcpu_idle(Double cpuIdle) {
        this.cpu_idle = BigDecimal.valueOf(cpuIdle);
    }

    public Double getcpu_load() {
        return this.cpu_load.doubleValue();
    }

    public void setcpu_load(Double cpuLoad) {
        this.cpu_load = BigDecimal.valueOf(cpuLoad);
    }

    public Double getcpu_sys() {
        return this.cpu_sys.doubleValue();
    }

    public void setcpu_sys(Double cpuSys) {
        this.cpu_sys = BigDecimal.valueOf(cpuSys);
    }

    public Double getcpu_user() {
        return this.cpu_user.doubleValue();
    }

    public void setcpu_user(Double cpuUser) {
        this.cpu_user = BigDecimal.valueOf(cpuUser);
    }

    /**
     * Returns a rough estimate on how much free mem is available for new vm
     * i.e. MemFree + Cached + Buffers + resident - memCommitted
     *
     * resident set size of qemu processes may grow - up to  memCommitted.
     * Thus, we deduct the growth potential of qemu processes, which is (memCommitted - resident)
     * @return - free mem available for new vm
     */
    public Long getmem_available() {
        return this.mem_available;
    }

    public void setmem_available(Long value) {
        this.mem_available = value;
    }

    /**
     * Returns the actual free memory on host (MB) as it appears in the host's memInfo.
     * i.e. MemFree + Cached + Buffers
     * @return - actual free memory on host
     */
    public Long getMemFree() {
        return this.memFree;
    }

    public void setMemFree(Long value) {
        this.memFree = value;
    }

    public Long getmem_shared() {
        return this.mem_shared;
    }

    public void setmem_shared(Long value) {
        this.mem_shared = value;
    }

    public Integer getusage_cpu_percent() {
        return this.usage_cpu_percent;
    }

    public void setusage_cpu_percent(Integer value) {
        this.usage_cpu_percent = value;
    }

    public Integer getusage_mem_percent() {
        return this.usage_mem_percent;
    }

    public void setusage_mem_percent(Integer value) {
        this.usage_mem_percent = value;
    }

    public Integer getusage_network_percent() {
        return this.usage_network_percent;
    }

    public void setusage_network_percent(Integer value) {
        this.usage_network_percent = value;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Long getswap_free() {
        return this.swap_free;
    }

    public void setswap_free(Long value) {
        this.swap_free = value;
    }

    public Long getswap_total() {
        return this.swap_total;
    }

    public void setswap_total(Long value) {
        this.swap_total = value;
    }

    public Integer getksm_cpu_percent() {
        return this.ksm_cpu_percent;
    }

    public void setksm_cpu_percent(Integer value) {
        this.ksm_cpu_percent = value;
    }

    public Long getksm_pages() {
        return this.ksm_pages;
    }

    public void setksm_pages(Long value) {
        this.ksm_pages = value;
    }

    public Boolean getksm_state() {
        return this.ksm_state;
    }

    public void setksm_state(Boolean value) {
        this.ksm_state = value;
    }

    public Long getboot_time() {
        return this.boot_time;
    }

    public void setboot_time(Long value) {
        this.boot_time = value;
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

    public Date getcpu_over_commit_time_stamp() {
        return this.cpu_over_commit_time_stamp;
    }

    public void setcpu_over_commit_time_stamp(Date value) {
        this.cpu_over_commit_time_stamp = value;
    }
}
