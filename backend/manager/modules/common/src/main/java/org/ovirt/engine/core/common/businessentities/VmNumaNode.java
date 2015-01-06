package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * Object which represents vm virtual NUMA node information
 *
 */
@Entity
@DiscriminatorValue("0")
@NamedQueries({
        @NamedQuery(name = "VmNumaNode.getAllVmNumaNodeByVmId",
                query = "select n from VmNumaNode n where n.vmId = :vmId")
})
public class VmNumaNode extends VdsNumaNode {

    private static final long serialVersionUID = -5384287037435972730L;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "vmNumaNode")
    private List<NumaNodeVmVds> numaNodeVdsList;

    public VmNumaNode() {
        setNumaNodeVdsList(new ArrayList<NumaNodeVmVds>());
    }

    public List<NumaNodeVmVds> getNumaNodeVdsList() {
        return numaNodeVdsList;
    }

    public void setNumaNodeVdsList(List<NumaNodeVmVds> numaNodeVdsList) {
        this.numaNodeVdsList = numaNodeVdsList;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
