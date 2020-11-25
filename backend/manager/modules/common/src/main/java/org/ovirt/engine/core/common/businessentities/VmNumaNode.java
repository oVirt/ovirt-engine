package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Object which represents vm virtual NUMA node information
 *
 */
public class VmNumaNode extends NumaNode {

    private static final long serialVersionUID = -5384287037435972730L;

    private List<Integer> vdsNumaNodeList = new ArrayList<>();

    private NumaTuneMode numaTuneMode;

    /**
     * @return pNUMA node index
     */
    public List<Integer> getVdsNumaNodeList() {
        return vdsNumaNodeList;
    }

    public void setVdsNumaNodeList(List<Integer> vdsNumaNodeList) {
        this.vdsNumaNodeList = vdsNumaNodeList;
    }

    public NumaTuneMode getNumaTuneMode() {
        return numaTuneMode;
    }

    public void setNumaTuneMode(NumaTuneMode numaTuneMode) {
        this.numaTuneMode = numaTuneMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                vdsNumaNodeList,
                numaTuneMode
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmNumaNode)) {
            return false;
        }
        VmNumaNode other = (VmNumaNode) obj;
        return super.equals(obj)
                && Objects.equals(vdsNumaNodeList, other.vdsNumaNodeList)
                && numaTuneMode == other.numaTuneMode;
    }

}
