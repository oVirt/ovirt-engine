package org.ovirt.engine.ui.uicommonweb.models.hosts.numa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class NumaSupportModel extends Model {

    public static final String SUBMIT_NUMA_SUPPORT = "SubmitNumaSupport"; //$NON-NLS-1$
    private final Model parentModel;
    private ListModel<VDS> hosts;
    private List<VdsNumaNode> numaNodeList;
    private List<VM> vmsWithvNumaNodeList;
    private List<VNodeModel> unassignedVNodeModelList;
    protected Map<Guid, List<VNodeModel>> p2vNumaNodesMap;
    private List<Pair<Integer, Set<VdsNumaNode>>> firstLevelDistanceSetList;
    private final Event modelReady = new Event(new EventDefinition("ModelReady", NumaSupportModel.class)); //$NON-NLS-1$
    private Map<Integer, VdsNumaNode> indexNodeMap;
    private final Map<Guid, VdcActionParametersBase> updateParametersMap = new HashMap<Guid, VdcActionParametersBase>();

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
            getHosts().setIsChangable(false);
        }
        getHosts().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                initHostNUMATopology();
            }
        });
        getHosts().setSelectedItem(host);
    }

    protected void initCommands() {
        UICommand command = new UICommand(SUBMIT_NUMA_SUPPORT, this);
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        command.setIsDefault(true);
        getCommands().add(command);
    }

    protected void initHostNUMATopology() {
        startProgress(null);
        AsyncDataProvider.getInstance().getHostNumaTopologyByHostId(new AsyncQuery(new INewAsyncCallback() {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object model, Object returnValue) {
                // TODO: host query can be skipped in case it was already fetched.
                NumaSupportModel.this.getNumaNodeList().addAll((List<VdsNumaNode>) returnValue);
                NumaSupportModel.this.initFirstLevelDistanceSetList();
                AsyncDataProvider.getInstance().getVMsWithVNumaNodesByClusterId(new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        NumaSupportModel.this.setVmsWithvNumaNodeList((List<VM>) returnValue);
                        NumaSupportModel.this.modelReady();
                    }

                }), NumaSupportModel.this.hosts.getSelectedItem().getVdsGroupId());
            }

        }), hosts.getSelectedItem().getId());
    }

    protected void initVNumaNodes() {
        unassignedVNodeModelList = new ArrayList<VNodeModel>();
        p2vNumaNodesMap = new HashMap<Guid, List<VNodeModel>>();

        for (VM vm : getVmsWithvNumaNodeList()) {
            if (vm.getvNumaNodeList() != null) {
                for (VmNumaNode vmNumaNode : vm.getvNumaNodeList()) {
                    VNodeModel vNodeModel = new VNodeModel(this, vm, vmNumaNode, false);
                    if (vmNumaNode.getVdsNumaNodeList() != null && !vmNumaNode.getVdsNumaNodeList().isEmpty()) {
                        for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                            if (!pair.getSecond().getFirst()) {
                                unassignedVNodeModelList.add(vNodeModel);
                                break;
                            } else {
                                vNodeModel.setPinned(true);
                                Guid nodeId = pair.getFirst();
                                assignVNumaToPhysicalNuma(vNodeModel, nodeId);
                            }
                        }
                    } else {
                        unassignedVNodeModelList.add(vNodeModel);
                    }
                }
            }
        }
    }

    private void assignVNumaToPhysicalNuma(VNodeModel vNodeModel, Guid nodeId) {
        if (!p2vNumaNodesMap.containsKey(nodeId)) {
            p2vNumaNodesMap.put(nodeId, new ArrayList<VNodeModel>());
        }
        p2vNumaNodesMap.get(nodeId)
                .add(vNodeModel);
    }

    private VdsNumaNode getNodeByIndex(Integer index) {
        if (indexNodeMap == null) {
            indexNodeMap = new HashMap<Integer, VdsNumaNode>();
            for (VdsNumaNode node : getNumaNodeList()) {
                indexNodeMap.put(node.getIndex(), node);
            }
        }
        return indexNodeMap.get(index);
    }

    public List<VNodeModel> getVNumaNodeByNodeId(Guid nodeId) {
        return p2vNumaNodesMap.get(nodeId);
    }

    private void initFirstLevelDistanceSetList() {
        firstLevelDistanceSetList = new ArrayList<Pair<Integer, Set<VdsNumaNode>>>();
        for (VdsNumaNode node : getNumaNodeList()) {
            Map<Integer, Set<VdsNumaNode>> distances = new HashMap<Integer, Set<VdsNumaNode>>();
            for (Entry<Integer, Integer> entry : node.getNumaNodeDistances().entrySet()) {
                Set<VdsNumaNode> sameDistanceNodes = distances.get(entry.getValue());
                if (sameDistanceNodes == null) {
                    sameDistanceNodes = new HashSet<VdsNumaNode>();
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
                firstLevelDistanceSetList.add(new Pair<Integer, Set<VdsNumaNode>>(minDistance.getKey(),
                        minDistance.getValue()));
            }
        }
    }

    private void modelReady() {
        initVNumaNodes();
        getModelReady().raise(this, EventArgs.EMPTY);
        stopProgress();
    }

    public void cancel() {
        parentModel.setWindow(null);
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
        this.vmsWithvNumaNodeList = vmsWithvNumaNodeList;
    }

    public List<VNodeModel> getUnassignedVNodeModelList() {
        return unassignedVNodeModelList;
    }

    public List<Pair<Integer, Set<VdsNumaNode>>> getFirstLevelDistanceSetList() {
        return firstLevelDistanceSetList;
    }

    public void setFirstLevelDistanceSetList(List<Pair<Integer, Set<VdsNumaNode>>> firstLevelDistanceSetList) {
        this.firstLevelDistanceSetList = firstLevelDistanceSetList;
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

    public void pinVNodeToNumaNode(Guid sourceVMGuid, boolean isPinned, int sourceVNumaIndex, int targetPNumaNodeIndex) {
        boolean breakFlag = false;
        for (VM vm : getVmsWithvNumaNodeList()) {
            if (vm.getId().equals(sourceVMGuid)) {
                for (VmNumaNode vmNumaNode : vm.getvNumaNodeList()) {
                    if (vmNumaNode.getIndex() == sourceVNumaIndex) {
                        breakFlag = true;
                        if (vmNumaNode.getVdsNumaNodeList().isEmpty()) {
                            Pair<Guid, Pair<Boolean, Integer>> pair = new Pair<Guid, Pair<Boolean, Integer>>();
                            pair.setFirst(getNodeByIndex(targetPNumaNodeIndex).getId());
                            pair.setSecond(new Pair<Boolean, Integer>());
                            pair.getSecond().setFirst(true);
                            pair.getSecond().setSecond(targetPNumaNodeIndex);
                            vmNumaNode.getVdsNumaNodeList().add(pair);
                        } else {
                            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                                if (targetPNumaNodeIndex == -1) {
                                    pair.setFirst(null);
                                    pair.getSecond().setFirst(false);
                                } else {
                                    pair.setFirst(getNodeByIndex(targetPNumaNodeIndex).getId());
                                    pair.getSecond().setFirst(true);
                                    pair.getSecond().setSecond(targetPNumaNodeIndex);
                                }
                            }
                        }
                        break;
                    }
                }
                updateParametersMap.put(vm.getId(),
                        new VmNumaNodeOperationParameters(vm.getId(), vm.getvNumaNodeList()));
            }
            if (breakFlag) {
                break;
            }
        }
        modelReady();
    }

    public ArrayList<VdcActionParametersBase> getUpdateParameters() {
        return new ArrayList<VdcActionParametersBase>(updateParametersMap.values());
    }
}
