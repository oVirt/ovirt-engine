package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

public class NumaSupportModel extends Model {

    public static final String SUBMIT_NUMA_SUPPORT = "SubmitNumaSupport"; //$NON-NLS-1$
    private final Model parentModel;
    private ListModel<VDS> hosts;
    private List<VdsNumaNode> numaNodeList;
    private List<VM> vmsWithvNumaNodeList;
    private Set<VNodeModel> unassignedNumaNodes;
    protected Map<Integer, Set<VNodeModel>> assignedNumaNodes;
    private List<Pair<Integer, Set<VdsNumaNode>>> firstLevelDistanceSetList;
    private final Event modelReady = new Event(new EventDefinition("ModelReady", NumaSupportModel.class)); //$NON-NLS-1$
    private Map<Integer, VdsNumaNode> indexNodeMap;
    private Map <Guid, Map<Integer, VNodeModel>> numaModelsPerVm = new HashMap<>();
    private Set <Guid> vmsToUpdate = new HashSet<>();

    public NumaSupportModel(List<VDS> hosts, VDS host, Model parentModel) {
        this.parentModel = parentModel;
        setHosts(new ListModel<VDS>());
        setNumaNodeList(new ArrayList<VdsNumaNode>());
        setTitle(ConstantsManager.getInstance().getMessages().numaTopologyTitle(host.getName()));
        setHelpTag(HelpTag.numa_support);
        setHashName("numa_support"); //$NON-NLS-1$
        initCommands();
        initHosts(hosts, host);
    }

    private void initHosts(List<VDS> hosts, VDS host) {
        getHosts().setItems(hosts);
        if (host == null) {
            host = hosts.get(0);
        }
        if (getHosts().getItems().size() <= 1) {
            getHosts().setIsChangeable(false);
        }
        getHosts().getSelectedItemChangedEvent().addListener((ev, sender, args) -> initHostNUMATopology());
        getHosts().setSelectedItem(host);
    }

    protected void initCommands() {
        UICommand command = new UICommand(SUBMIT_NUMA_SUPPORT, this);
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        getCommands().add(command);

        getCommands().add(UICommand.createDefaultCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    protected void initHostNUMATopology() {
        startProgress();
        AsyncDataProvider.getInstance().getHostNumaTopologyByHostId(new AsyncQuery<>(returnValue -> {
            // TODO: host query can be skipped in case it was already fetched.
            getNumaNodeList().addAll(returnValue);
            initFirstLevelDistanceSetList();
            AsyncDataProvider.getInstance().getVMsWithVNumaNodesByClusterId(new AsyncQuery<>(returnValue1 -> {
                setVmsWithvNumaNodeList(returnValue1);
                initVNumaNodes();
                modelReady();
            }), hosts.getSelectedItem().getClusterId());
        }), hosts.getSelectedItem().getId());
    }

    protected void initVNumaNodes() {
        unassignedNumaNodes = new LinkedHashSet<>();
        assignedNumaNodes = new HashMap<>();
        final Set<Integer> hostIndices = new HashSet<>();
        for (VdsNumaNode numaNode : numaNodeList) {
            hostIndices.add(numaNode.getIndex());
        }

        for (final VM vm : getVmsWithvNumaNodeList()) {
            numaModelsPerVm.put(vm.getId(), new HashMap<Integer, VNodeModel>());
            for (VmNumaNode vmNumaNode : vm.getvNumaNodeList()) {
                VNodeModel vNodeModel = createVNodeModel(vm, vmNumaNode);
                numaModelsPerVm.get(vm.getId()).put(vNodeModel.getIndex(), vNodeModel);
                if (vNodeModel.isPinned()) {
                    if (!hostIndices.contains(vNodeModel.getHostNodeIndex())) {
                        // host numa node does not exist. Unpin the numa node and update the configuration
                        vNodeModel.unpin();
                        vmsToUpdate.add(vm.getId());
                    }
                }
                if (!vNodeModel.isPinned()) {
                    // virtual numa node is not assigned to any host numa node
                    unassignedNumaNodes.add(vNodeModel);
                } else {
                    // virtual numa node is assigned to a host numa node
                    assignVNumaToPhysicalNuma(vNodeModel);
                }
            }
        }
    }

    private void assignVNumaToPhysicalNuma(VNodeModel vNodeModel) {
        final Integer hostNodeIndex = vNodeModel.getHostNodeIndex();
        if (!assignedNumaNodes.containsKey(hostNodeIndex)) {
            assignedNumaNodes.put(hostNodeIndex, new LinkedHashSet<VNodeModel>());
        }
        assignedNumaNodes.get(hostNodeIndex).add(vNodeModel);
    }

    private VdsNumaNode getNodeByIndex(Integer index) {
        if (indexNodeMap == null) {
            indexNodeMap = new HashMap<>();
            for (VdsNumaNode node : getNumaNodeList()) {
                indexNodeMap.put(node.getIndex(), node);
            }
        }
        return indexNodeMap.get(index);
    }

    public Collection<VNodeModel> getVNumaNodeByNodeIndx(Integer nodeIdx) {
        return assignedNumaNodes.get(nodeIdx);
    }

    private void initFirstLevelDistanceSetList() {
        firstLevelDistanceSetList = new ArrayList<>();
        for (VdsNumaNode node : getNumaNodeList()) {
            Map<Integer, Set<VdsNumaNode>> distances = new HashMap<>();
            for (Entry<Integer, Integer> entry : node.getNumaNodeDistances().entrySet()) {
                Set<VdsNumaNode> sameDistanceNodes = distances.get(entry.getValue());
                if (sameDistanceNodes == null) {
                    sameDistanceNodes = new HashSet<>();
                    sameDistanceNodes.add(getNodeByIndex(entry.getKey()));
                    distances.put(entry.getValue(), sameDistanceNodes);
                }
                sameDistanceNodes.add(node);
            }

            Entry<Integer, Set<VdsNumaNode>> minDistance = null;
            for (Entry<Integer, Set<VdsNumaNode>> entry : distances.entrySet()) {
                if (minDistance == null || entry.getKey() < minDistance.getKey()) {
                    minDistance = entry;
                }
            }

            boolean found = false;
            for (Pair<Integer, Set<VdsNumaNode>> group : firstLevelDistanceSetList) {
                // 'true' if the two specified collections have no elements in common
                boolean isDisjoint = Collections.disjoint(group.getSecond(), minDistance.getValue());
                if (group.getFirst().equals(minDistance.getKey()) && !isDisjoint) {
                    group.getSecond().addAll(minDistance.getValue());
                    found = true;
                    break;
                }
            }
            if (!found && minDistance != null) {
                firstLevelDistanceSetList.add(new Pair<>(minDistance.getKey(),
                        minDistance.getValue()));
            }
        }
    }

    private void modelReady() {
        getModelReady().raise(this, EventArgs.EMPTY);
        stopProgress();
    }

    public void cancel() {
        parentModel.setWindow(null);
    }

    public Model getParentModel() {
        return parentModel;
    }

    public List<VdsNumaNode> getNumaNodeList() {
        return numaNodeList;
    }

    public void setNumaNodeList(List<VdsNumaNode> numaNodeList) {
        this.numaNodeList = numaNodeList;
    }

    public List<VM> getVmsWithvNumaNodeList() {
        return vmsWithvNumaNodeList;
    }

    public void setVmsWithvNumaNodeList(List<VM> vmsWithvNumaNodeList) {
        if (!hosts.getSelectedItem().isNumaSupport()) {
            vmsWithvNumaNodeList.clear();
        }
        for (Iterator<VM> vmIterator = vmsWithvNumaNodeList.iterator(); vmIterator.hasNext(); ) {
            final VM vm = vmIterator.next();

            if (vm.getDedicatedVmForVdsList().isEmpty()
                    || !vm.getDedicatedVmForVdsList().contains(getHosts().getSelectedItem().getId())
                    || vm.getvNumaNodeList() == null || vm.getvNumaNodeList().isEmpty()) {
                vmIterator.remove();
            }
        }
        this.vmsWithvNumaNodeList = vmsWithvNumaNodeList;
    }

    public Collection<VNodeModel> getUnassignedNumaNodes() {
        return unassignedNumaNodes;
    }

    public List<Pair<Integer, Set<VdsNumaNode>>> getFirstLevelDistanceSetList() {
        return firstLevelDistanceSetList;
    }

    public Event getModelReady() {
        return modelReady;
    }

    public ListModel<VDS> getHosts() {
        return hosts;
    }

    public void setHosts(ListModel<VDS> hosts) {
        this.hosts = hosts;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (SUBMIT_NUMA_SUPPORT.equals(command.getName())) {
            parentModel.executeCommand(command);
            parentModel.setWindow(null);
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    /**
     * Dragging virtual numa nodes to host numa nodes emits a pin event. The pin event calls this callback.
     * @param sourceVMGuid Guid of the VM the numa node belongs to
     * @param sourceVNumaIndex Index of the VM numa node
     * @param pNumaNodeIndex Index of the host numa node
     */
    public void pinVNode(Guid sourceVMGuid, int sourceVNumaIndex, int pNumaNodeIndex) {
        VNodeModel vNodeModel = getNodeModel(sourceVMGuid, sourceVNumaIndex);
        if (vNodeModel.isPinned()) {
            assignedNumaNodes.get(vNodeModel.getHostNodeIndex()).remove(vNodeModel);
        } else {
            unassignedNumaNodes.remove(vNodeModel);
        }
        vmsToUpdate.add(vNodeModel.getVm().getId());
        vNodeModel.pinTo(pNumaNodeIndex);
        assignVNumaToPhysicalNuma(vNodeModel);
        modelReady();
    }

    /**
     * Moving an virtual numa node away from a host numa node triggers an unpin event. The unpin event calls this
     * method.
     * @param sourceVMGuid Guid of the VM
     * @param sourceVNumaIndex Index of the VM numa node
     */
    public void unpinVNode(Guid sourceVMGuid, int sourceVNumaIndex) {
        VNodeModel vNodeModel = getNodeModel(sourceVMGuid, sourceVNumaIndex);
        if (vNodeModel.isPinned()) {
            assignedNumaNodes.get(vNodeModel.getHostNodeIndex()).remove(vNodeModel);
            unassignedNumaNodes.add(vNodeModel);
        }
        vNodeModel.unpin();
        vmsToUpdate.add(vNodeModel.getVm().getId());
        modelReady();
    }

    /**
     * Get the current numa node pinning - as currently visible for the user - for a specific VM.
     * @param vmId guid of the VM
     * @return List of numa nodes with current mapping
     */
    public List<VmNumaNode> getNumaNodes(Guid vmId) {
        final List<VmNumaNode> numaNodes = new ArrayList<>();
        Map<Integer, VNodeModel> numaModels = numaModelsPerVm.get(vmId);
        if (numaModels != null) {
            for (final VNodeModel model : numaModels.values()) {
                if (model != null) {
                    numaNodes.add(model.toVmNumaNode());
                }
            }
        }
        return numaNodes;
    }

    private VNodeModel getNodeModel(Guid vmId, int nodeIndex) {
        return numaModelsPerVm.get(vmId).get(nodeIndex);
    }

    /**
     * Return a list of action parameters which contain numa pinning updates for different VMs.
     * Used when accessing the numa support screen from the host list panel.
     * @return List of updated numa configurations
     */
    public ArrayList<ActionParametersBase> getUpdateParameters() {
        final ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (Guid vmId : vmsToUpdate) {
            final List<VmNumaNode> numaNodes = new ArrayList<>();
            for (final VNodeModel model : numaModelsPerVm.get(vmId).values()) {
                numaNodes.add(model.toVmNumaNode());
            }
            parameters.add(new VmNumaNodeOperationParameters(vmId, numaNodes));
        }
        return parameters;
    }

    protected VNodeModel createVNodeModel(VM vm, VmNumaNode vmNumaNode) {
        return new VNodeModel(vm, vmNumaNode, false);
    }
}
