package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.ovirt.engine.core.compat.Guid;

/**
 * Object which represents host NUMA node information
 *
 */
@Entity
@Table(name = "numa_node")
@Cacheable(true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("case when vds_id is null then '0' else '1' end")
@DiscriminatorValue("1")
@NamedQueries({
        @NamedQuery(name = "VdsNumaNode.getAllVdsNumaNodeByVdsId",
                query = "select n from VdsNumaNode n where n.vdsId = :vdsId")
})
public class VdsNumaNode implements IVdcQueryable, Serializable, BusinessEntity<Guid> {

    private static final long serialVersionUID = -683066053231559224L;

    @Id
    @Column(name = "numa_node_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id;

    @Column(name = "vds_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid vdsId;

    @Column(name = "vm_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid vmId;

    @Column(name = "numa_node_index")
    private int index;

    @GenericGenerator(name = "guid", strategy = "org.ovirt.engine.core.dao.jpa.GuidGenerator")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "numa_node_cpu_map", joinColumns = @JoinColumn(name = "numa_node_id"))
    @CollectionId(columns = @Column(name = "id"), type = @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType"),
            generator = "guid")
    @Column(name = "cpu_core_id")
    private List<Integer> cpuIds;

    @Column(name = "mem_total")
    private long memTotal;

    @Embedded
    private NumaNodeStatistics numaNodeStatistics;

    @Column(name = "distance")
    @Type(type = "org.ovirt.engine.core.dao.jpa.DistanceUserType")
    private Map<Integer, Integer> numaNodeDistances;

    public VdsNumaNode() {
        id = Guid.newGuid();
        cpuIds = new ArrayList<Integer>();
        numaNodeDistances = new HashMap<Integer, Integer>();
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Integer> getCpuIds() {
        return cpuIds;
    }

    public void setCpuIds(List<Integer> cpuIds) {
        this.cpuIds = cpuIds;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(long memTotal) {
        this.memTotal = memTotal;
    }

    public NumaNodeStatistics getNumaNodeStatistics() {
        return numaNodeStatistics;
    }

    public void setNumaNodeStatistics(NumaNodeStatistics numaNodeStatistics) {
        this.numaNodeStatistics = numaNodeStatistics;
    }

    /**
     * Represents the distance between this node to other nodes.
     * The key is the node index(include self), the value is the distance
     * between self and the according node.
     */
    public Map<Integer, Integer> getNumaNodeDistances() {
        return numaNodeDistances;
    }

    public void setNumaNodeDistances(Map<Integer, Integer> numaNodeDistances) {
        this.numaNodeDistances = numaNodeDistances;
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
        if (!(obj instanceof VdsNumaNode)) {
            return false;
        }
        VdsNumaNode other = (VdsNumaNode) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

}
