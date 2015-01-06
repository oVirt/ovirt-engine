package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ovirt.engine.core.compat.Guid;

/**
 * Object which represents host NUMA node information
 *
 */
@Entity
@Table(name = "vm_vds_numa_node_map")
@Cacheable(true)
public class NumaNodeVmVds implements Serializable, BusinessEntity<Guid> {

    private static final long serialVersionUID = -683066053231559224L;

    @Id
    @Column(name = "id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id = Guid.newGuid();

    @ManyToOne
    @JoinColumn(name = "vm_numa_node_id")
    private VmNumaNode vmNumaNode;

    @ManyToOne
    @JoinColumn(name = "vds_numa_node_id")
    private VdsNumaNode vdsNumaNode;

    private transient Guid vdsNumaNodeGuid;

    @Column(name = "vds_numa_node_index")
    private Integer nodeIndex;

    @Column(name = "is_pinned")
    private boolean pinned;

    public NumaNodeVmVds() {
    }

    public NumaNodeVmVds(Guid id, VmNumaNode vmNumaNode, boolean pinned, Integer nodeIndex) {
        this.id = id;
        this.vmNumaNode = vmNumaNode;
        this.pinned = pinned;
        this.nodeIndex = nodeIndex;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Integer getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(Integer nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public VmNumaNode getVmNumaNode() {
        return vmNumaNode;
    }

    public void setVmNumaNode(VmNumaNode vmNumaNode) {
        this.vmNumaNode = vmNumaNode;
    }

    public VdsNumaNode getVdsNumaNode() {
        return vdsNumaNode;
    }

    public void setVdsNumaNode(VdsNumaNode vdsNumaNode) {
        this.vdsNumaNode = vdsNumaNode;
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
        if (!(obj instanceof NumaNodeVmVds)) {
            return false;
        }
        NumaNodeVmVds other = (NumaNodeVmVds) obj;
        return Objects.equals(id, other.id);
    }

    public Guid getVdsNumaNodeGuid() {
        return vdsNumaNodeGuid;
    }

    public void setVdsNumaNodeGuid(Guid vdsNumaNodeGuid) {
        this.vdsNumaNodeGuid = vdsNumaNodeGuid;
    }

}
