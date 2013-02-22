package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;

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
    private Long mem_available = 0L;
    private Long mem_shared = 0L;
    private Long swap_free = 0L;
    private Long swap_total = 0L;
    private Integer ksm_cpu_percent;
    private Long ksm_pages = 0L;
    private Boolean ksm_state;

    public VdsStatistics() {
        this.cpu_idle = new BigDecimal(0);
        this.cpu_load = new BigDecimal(0);
        this.cpu_sys = new BigDecimal(0);
        this.cpu_user = new BigDecimal(0);
    }

    public VdsStatistics(Double cpu_idle, Double cpu_load, Double cpu_sys,
            Double cpu_user, Long mem_available, Long mem_shared, Integer usage_cpu_percent,
            Integer usage_mem_percent, Integer usage_network_percent, Guid vds_id) {
        this.cpu_idle = BigDecimal.valueOf(cpu_idle);
        this.cpu_load = BigDecimal.valueOf(cpu_load);
        this.cpu_sys = BigDecimal.valueOf(cpu_sys);
        this.cpu_user = BigDecimal.valueOf(cpu_user);
        this.mem_available = mem_available;
        this.mem_shared = mem_shared;
        this.usage_cpu_percent = usage_cpu_percent;
        this.usage_mem_percent = usage_mem_percent;
        this.usage_network_percent = usage_network_percent;
        // this.vds_id = vds_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null)?0:id.hashCode());
        result = prime * result + ((cpu_idle == null)?0:cpu_idle.hashCode());
        result = prime * result + ((cpu_load == null)?0:cpu_load.hashCode());
        result = prime * result + ((cpu_sys == null)?0:cpu_sys.hashCode());
        result = prime * result + ((cpu_user == null)?0:cpu_user.hashCode());
        result = prime * result + ((mem_available == null)?0:mem_available.hashCode());
        result = prime * result + ((mem_shared == null)?0:mem_shared.hashCode());
        result = prime * result + ((usage_cpu_percent == null)?0:usage_cpu_percent.hashCode());
        result = prime * result + ((usage_network_percent == null)?0:usage_network_percent.hashCode());
        result = prime * result + ((ksm_state == null)?0:ksm_state.hashCode());
        result = prime * result + ((ksm_pages == null)?0:ksm_pages.hashCode());
        result = prime * result + ((ksm_cpu_percent == null)?0:ksm_cpu_percent.hashCode());
        result = prime * result + ((swap_total == null)?0:swap_total.hashCode());
        result = prime * result + ((swap_free == null)?0:swap_free.hashCode());
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
                && ObjectUtils.objectsEqual(mem_shared, other.mem_shared)
                && ObjectUtils.objectsEqual(usage_cpu_percent, other.usage_cpu_percent)
                && ObjectUtils.objectsEqual(usage_network_percent, other.usage_network_percent)
                && ObjectUtils.objectsEqual(ksm_state, other.ksm_state)
                && ObjectUtils.objectsEqual(ksm_pages, other.ksm_pages)
                && ObjectUtils.objectsEqual(ksm_cpu_percent, other.ksm_cpu_percent)
                && ObjectUtils.objectsEqual(swap_total, other.swap_total)
                && ObjectUtils.objectsEqual(swap_free, other.swap_free));
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

    public Long getmem_available() {
        return this.mem_available;
    }

    public void setmem_available(Long value) {
        this.mem_available = value;
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

}
