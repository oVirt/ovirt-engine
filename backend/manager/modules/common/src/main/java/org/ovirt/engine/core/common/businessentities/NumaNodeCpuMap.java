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
 * Object which represents host NUMA node information
 *
 */
@Entity
@Table(name = "numa_node_cpu_map")
@Cacheable(true)
public class NumaNodeCpuMap implements Serializable, BusinessEntity<Guid> {

    private static final long serialVersionUID = -4953998248947476825L;

    @Id
    @Column(name = "id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id;

    @Column(name = "numa_node_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid numaNodeId;

    @Column(name = "cpu_core_id")
    private int cpuCoreId;

    public NumaNodeCpuMap() {
        id = Guid.newGuid();
    }

    public NumaNodeCpuMap(Guid id, Guid numaNodeId, int cpuCoreId) {
        this.id = id;
        this.numaNodeId = numaNodeId;
        this.cpuCoreId = cpuCoreId;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public int getCpuCoreId() {
        return cpuCoreId;
    }

    public void setCpuCoreId(int cpuCoreId) {
        this.cpuCoreId = cpuCoreId;
    }

    public Guid getNumaNodeId() {
        return numaNodeId;
    }

    public void setNumaNodeId(Guid numaNodeId) {
        this.numaNodeId = numaNodeId;
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
        if (!(obj instanceof NumaNodeCpuMap)) {
            return false;
        }
        NumaNodeCpuMap other = (NumaNodeCpuMap) obj;
        return Objects.equals(id, other.id);
    }
}
