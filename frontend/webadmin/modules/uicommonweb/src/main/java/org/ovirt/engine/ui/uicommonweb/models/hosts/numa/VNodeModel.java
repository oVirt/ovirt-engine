package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

/**
 * Model of a virtual numa node used for drag and drop in the numa pinning dialog. It is backed by a real virtual
 * numa node entity. The numa node entity supplied to construct the numa node model will never be manipulated by the
 * model.
 */
public class VNodeModel extends Model {
    static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final VM vm;
    private final VmNumaNode vmNumaNode;
    private boolean pinned;
    private boolean locked;
    private Integer hostNodeIndex;
    private ListModel<NumaTuneMode> numaTuneModeList;

    public VNodeModel(VM vm, VmNumaNode vmNumaNode, boolean editEnabled) {
        this.vm = vm;
        this.vmNumaNode = vmNumaNode;
        if (vmNumaNode.getVdsNumaNodeList() != null && !vmNumaNode.getVdsNumaNodeList().isEmpty()){
            hostNodeIndex = vmNumaNode.getVdsNumaNodeList().get(0);
            pinned = true;
        }
        setNumaTuneModeList(new ListModel<NumaTuneMode>());
        initNumaTunes(editEnabled);
    }

    private void initNumaTunes(boolean editEnabled) {
        getNumaTuneModeList().setItems(AsyncDataProvider.getInstance().getNumaTuneModeList());
        NumaTuneMode selectedMode;

        if (vmNumaNode.getNumaTuneMode() != null) {
            selectedMode = vmNumaNode.getNumaTuneMode();
        } else {
            selectedMode = NumaTuneMode.INTERLEAVE;
        }
        getNumaTuneModeList().setSelectedItem(selectedMode);
        getNumaTuneModeList().setIsChangeable(editEnabled);
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
        newNode.setNumaTuneMode(getNumaTuneModeList().getSelectedItem());
        if (isPinned()) {
            newNode.setVdsNumaNodeList(Arrays.asList(hostNodeIndex));
        }
        return newNode;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public ListModel<NumaTuneMode> getNumaTuneModeList() {
        return numaTuneModeList;
    }

    public void setNumaTuneModeList(ListModel<NumaTuneMode> numaTuneModeList) {
        this.numaTuneModeList = numaTuneModeList;
    }
}
