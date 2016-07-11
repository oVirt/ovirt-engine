package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.Model;

/**
 * Model of a virtual numa node used for drag and drop in the numa pinning dialog. It is backed by a real virtual
 * numa node entity. The numa node entity supplied to construct the numa node model will never be manipulated by the
 * model.
 */
public class VNodeModel extends Model {
    private final VM vm;
    private final VmNumaNode vmNumaNode;
    private boolean pinned;
    private boolean locked;
    private Integer hostNodeIndex;

    public VNodeModel(VM vm, VmNumaNode vmNumaNode) {
        this.vm = vm;
        this.vmNumaNode = vmNumaNode;
        if (vmNumaNode.getVdsNumaNodeList() != null && !vmNumaNode.getVdsNumaNodeList().isEmpty()){
            pinned = vmNumaNode.getVdsNumaNodeList().get(0).getSecond().getFirst();
        }
        if (pinned){
            hostNodeIndex = vmNumaNode.getVdsNumaNodeList().get(0).getSecond().getSecond();
        }
    }

    public VM getVm() {
        return vm;
    }

    public Integer getIndex() {
        return vmNumaNode.getIndex();
    }

    public Integer getHostNodeIndex() {
        return hostNodeIndex;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void pinTo(Integer hostNodeIndex) {
        requireNonNull(hostNodeIndex);
        pinned = true;
        this.hostNodeIndex = hostNodeIndex;
    }

    public void unpin(){
        pinned = false;
        hostNodeIndex = null;
    }

    public boolean isSplitted() {
        return vmNumaNode.getVdsNumaNodeList() != null && vmNumaNode.getVdsNumaNodeList().size() > 1;
    }

    /**
     * Convert the model representation of a virtual numa node to a real virtual numa node
     * @return the numa node
     */
    public VmNumaNode toVmNumaNode(){
        final VmNumaNode newNode = new VmNumaNode();
        newNode.setIndex(vmNumaNode.getIndex());
        newNode.setId(vmNumaNode.getId());
        newNode.setMemTotal(vmNumaNode.getMemTotal());
        newNode.setCpuIds(vmNumaNode.getCpuIds());
        if (isPinned()) {
            newNode.setVdsNumaNodeList(Arrays.asList(
                    new Pair<Guid, Pair<Boolean, Integer>>(null, new Pair(pinned, hostNodeIndex)))
            );
        }
        return newNode;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }
}
