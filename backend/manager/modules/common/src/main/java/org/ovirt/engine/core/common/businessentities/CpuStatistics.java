package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ovirt.engine.core.compat.Guid;

/**
 * Object which represents host per cpu statistics information
 *
 */
@Entity
@Table(name = "vds_cpu_statistics")
@Cacheable(true)
public class CpuStatistics implements Serializable {

    private static final long serialVersionUID = 3274786304152401306L;

    @Id
    @Column(name = "vds_cpu_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid vdsCpuId;

    @Column(name = "vds_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid vdsId;

    @Column(name = "cpu_core_id")
    private int cpuId;

    @Column(name = "cpu_sys")
    private double cpuSys;

    @Column(name = "cpu_user")
    private double cpuUser;

    @Column(name = "cpu_idle")
    private double cpuIdle;

    @Column(name = "usage_cpu_percent")
    private int cpuUsagePercent;

    public int getCpuId() {
        return cpuId;
    }

    public void setCpuId(int cpuId) {
        this.cpuId = cpuId;
    }

    public double getCpuSys() {
        return cpuSys;
    }

    public void setCpuSys(double cpuSys) {
        this.cpuSys = cpuSys;
    }

    public double getCpuUser() {
        return cpuUser;
    }

    public void setCpuUser(double cpuUser) {
        this.cpuUser = cpuUser;
    }

    public double getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(double cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    public int getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(int cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vdsCpuId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CpuStatistics))
            return false;
        return Objects.equals(vdsCpuId, ((CpuStatistics) obj).vdsCpuId);
    }

}
