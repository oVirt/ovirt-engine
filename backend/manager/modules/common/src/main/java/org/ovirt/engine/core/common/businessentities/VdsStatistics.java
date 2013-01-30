package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;

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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VdsStatistics other = (VdsStatistics) obj;

        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;

        if (cpu_idle == null) {
            if (other.cpu_idle != null)
                return false;
        } else if (!(cpu_idle.doubleValue() == other.cpu_idle.doubleValue()))
            return false;

        if (cpu_load == null) {
            if (other.cpu_load != null)
                return false;
        } else if (!(cpu_load.doubleValue() == other.cpu_load.doubleValue()))
            return false;

        if (cpu_sys == null) {
            if (other.cpu_sys != null)
                return false;
        } else if (!(cpu_sys.doubleValue() == other.cpu_sys.doubleValue()))
            return false;

        if (cpu_user == null) {
            if (other.cpu_user != null)
                return false;
        } else if (!(cpu_user.doubleValue() == other.cpu_user.doubleValue()))
            return false;

        if (mem_available == null) {
            if (other.mem_available != null)
                return false;
        } else if (!mem_available.equals(other.mem_available))
            return false;

        if (mem_shared == null) {
            if (other.mem_shared != null)
                return false;
        } else if (!mem_shared.equals(other.mem_shared))
            return false;

        if (usage_cpu_percent == null) {
            if (other.usage_cpu_percent != null)
                return false;
        } else if (!usage_cpu_percent.equals(other.usage_cpu_percent))
            return false;

        if (usage_network_percent == null) {
            if (other.usage_network_percent != null)
                return false;
        } else if (!usage_network_percent.equals(other.usage_network_percent))
            return false;

        if (ksm_state == null) {
            if (other.ksm_state != null)
                return false;
        } else if (!ksm_state.equals(other.ksm_state))
            return false;

        if (ksm_pages == null) {
            if (other.ksm_pages != null)
                return false;
        } else if (!ksm_pages.equals(other.ksm_pages))
            return false;

        if (ksm_cpu_percent == null) {
            if (other.ksm_cpu_percent != null)
                return false;
        } else if (!ksm_cpu_percent.equals(other.ksm_cpu_percent))
            return false;

        if (swap_total == null) {
            if (other.swap_total != null)
                return false;
        } else if (!swap_total.equals(other.swap_total))
            return false;

        if (swap_free == null) {
            if (other.swap_free != null)
                return false;
        } else if (!swap_free.equals(other.swap_free))
            return false;

        return true;
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
