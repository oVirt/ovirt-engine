package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.EntitySelectionModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.DoubleValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public abstract class AffinityGroupModel extends Model {
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private static class IdAndName implements BusinessEntity<Guid>, Nameable {
        private Guid id;
        private final String name;

        public IdAndName(Guid id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public Guid getId() {
            return id;
        }

        @Override
        public void setId(Guid id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private final AffinityGroup affinityGroup;
    private final ListModel<?> sourceListModel;
    private final ActionType saveActionType;

    private EntityModel<String> name;
    private EntityModel<String> description;
    private EntityModel<String> priority;
    private ListModel<EntityAffinityRule> vmAffinityRule;
    private EntityModel<Boolean> vmAffinityEnforcing;
    private ListModel<EntityAffinityRule> hostAffinityRule;
    private EntityModel<Boolean> hostAffinityEnforcing;
    private EntitySelectionModel vmsOrLabelsSelectionModel;
    private EntitySelectionModel hostsOrLabelsSelectionModel;
    private final Guid clusterId;
    private final String clusterName;

    private List<VM> vms;
    private List<VDS> hosts;
    private List<Label> labels;

    private Set<Guid> labelIds;

    public AffinityGroupModel(AffinityGroup affinityGroup, ListModel<?> sourceListModel,
            ActionType saveActionType,
            Guid clusterId,
            String clusterName) {
        this.affinityGroup = affinityGroup;
        this.sourceListModel = sourceListModel;
        this.saveActionType = saveActionType;
        this.clusterId = clusterId;
        this.clusterName = clusterName;

        setName(new EntityModel<>(getAffinityGroup().getName()));
        setDescription(new EntityModel<>(getAffinityGroup().getDescription()));
        setPriority(new EntityModel<>(Double.toString(getAffinityGroup().getPriorityAsDouble())));

        // Set VM details
        setVmAffinityRule(new ListModel<EntityAffinityRule>());
        vmAffinityRule.setItems(Arrays.asList(EntityAffinityRule.values()), affinityGroup.getVmAffinityRule());
        setVmAffinityEnforcing(new EntityModel<>(affinityGroup.isVmEnforcing()));
        vmAffinityRule.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateChangeableEnforcing());
        setVmsOrLabelsSelectionModel(new EntitySelectionModel(constants.selectVm(), constants.noAvailableVms()));

        // Set host details
        setHostAffinityRule(new ListModel<EntityAffinityRule>());
        hostAffinityRule.setItems(Arrays.asList(EntityAffinityRule.values()), affinityGroup.getVdsAffinityRule());
        setHostAffinityEnforcing(new EntityModel<>(affinityGroup.isVdsEnforcing()));
        hostAffinityRule.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateChangeableEnforcing());
        setHostsOrLabelsSelectionModel(new EntitySelectionModel(constants.selectHost(), constants.noAvailableHosts()));

        updateChangeableEnforcing();

        addCommands();
    }

    public void init() {
        startProgress();

        //TODO: should be by cluster id and remove clusterName method from resolver.
        AsyncDataProvider.getInstance().getVmListByClusterName(new AsyncQuery<>(vmList -> {
            vms = vmList;
            onFetchedData();
        }), clusterName);

        AsyncDataProvider.getInstance().getHostListByClusterId(new AsyncQuery<>(hostList -> {
            hosts = hostList;
            onFetchedData();
        }), clusterId);

        AsyncDataProvider.getInstance().getLabelList(new AsyncQuery<>(labelList -> {
            labels = labelList;
            onFetchedData();
        }));
    }

    private void onFetchedData() {
        if (vms == null || hosts == null || labels == null) {
            return;
        }

        Set<Guid> vmIds = vms.stream().map(VM::getId).collect(Collectors.toSet());
        Set<Guid> hostIds = hosts.stream().map(VDS::getId).collect(Collectors.toSet());

        // Filter out labels that contain VM or host that is not from the current cluster
        // TODO - this will not be needed once labels are cluster entities
        labels = labels.stream()
                .filter(label -> vmIds.containsAll(label.getVms()))
                .filter(label -> hostIds.containsAll(label.getHosts()))
                .collect(Collectors.toList());

        labelIds = labels.stream().map(Label::getId).collect(Collectors.toSet());

        List<IdAndName> vmsAndLabels = vms.stream()
                .map(vm -> new IdAndName(vm.getId(), messages.vmName(vm.getName())))
                .collect(Collectors.toList());

        List<IdAndName> hostsAndLabels = hosts.stream()
                .map(host -> new IdAndName(host.getId(), messages.hostName(host.getName())))
                .collect(Collectors.toList());

        for (Label label : labels) {
            IdAndName obj = new IdAndName(label.getId(), messages.labelName(label.getName()));
            vmsAndLabels.add(obj);
            hostsAndLabels.add(obj);
        }

        List<Guid> selectedVmIds = new ArrayList<>(getAffinityGroup().getVmIds());
        selectedVmIds.addAll(getAffinityGroup().getVmLabels());
        getVmsOrLabelsSelectionModel().init(vmsAndLabels, selectedVmIds);

        List<Guid> selectedHostIds = new ArrayList<>(getAffinityGroup().getVdsIds());
        selectedHostIds.addAll(getAffinityGroup().getHostLabels());
        getHostsOrLabelsSelectionModel().init(hostsAndLabels, selectedHostIds);

        stopProgress();
    }

    private void updateChangeableEnforcing() {
        vmAffinityEnforcing.setIsChangeable(vmAffinityRule.getSelectedItem() != EntityAffinityRule.DISABLED);
        hostAffinityEnforcing.setIsChangeable(hostAffinityRule.getSelectedItem() != EntityAffinityRule.DISABLED);
    }

    protected AffinityGroup getAffinityGroup() {
        return affinityGroup;
    }

    protected void addCommands() {
        UICommand command = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(command);
        getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    public EntityModel<String> getName() {
        return name;
    }

    private void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    private void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public EntityModel<String> getPriority() {
        return priority;
    }

    public void setPriority(EntityModel<String> priority) {
        this.priority = priority;
    }

    public ListModel<EntityAffinityRule> getVmAffinityRule() {
        return vmAffinityRule;
    }

    private void setVmAffinityRule(ListModel<EntityAffinityRule> vmAffinityRule) {
        this.vmAffinityRule = vmAffinityRule;
    }

    public ListModel<EntityAffinityRule> getHostAffinityRule() {
        return hostAffinityRule;
    }

    private void setHostAffinityRule(ListModel<EntityAffinityRule> hostAffinityRule) {
        this.hostAffinityRule = hostAffinityRule;
    }

    public EntityModel<Boolean> getVmAffinityEnforcing() {
        return vmAffinityEnforcing;
    }

    private void setVmAffinityEnforcing(EntityModel<Boolean> vmAffinityEnforcing) {
        this.vmAffinityEnforcing = vmAffinityEnforcing;
    }

    public EntityModel<Boolean> getHostAffinityEnforcing() {
        return hostAffinityEnforcing;
    }

    private void setHostAffinityEnforcing(EntityModel<Boolean> hostAffinityEnforcing) {
        this.hostAffinityEnforcing = hostAffinityEnforcing;
    }

    public EntitySelectionModel getVmsOrLabelsSelectionModel() {
        return vmsOrLabelsSelectionModel;
    }

    private void setVmsOrLabelsSelectionModel(EntitySelectionModel vmsOrLabelsSelectionModel) {
        this.vmsOrLabelsSelectionModel = vmsOrLabelsSelectionModel;
    }

    public EntitySelectionModel getHostsOrLabelsSelectionModel() {
        return hostsOrLabelsSelectionModel;
    }

    private void setHostsOrLabelsSelectionModel(EntitySelectionModel hostsOrLabelsSelectionModel) {
        this.hostsOrLabelsSelectionModel = hostsOrLabelsSelectionModel;
    }

    protected void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    void onSave() {
        if (!validate()) {
            return;
        }

        if (getProgress() != null) {
            return;
        }
        AffinityGroup group = getAffinityGroup();
        group.setName(getName().getEntity());
        group.setDescription(getDescription().getEntity());
        group.setClusterId(clusterId);
        group.setPriorityFromDouble(Double.parseDouble(getPriority().getEntity()));

        // Save VM details
        group.setVmEnforcing(getVmAffinityEnforcing().getEntity());
        group.setVmAffinityRule(getVmAffinityRule().getSelectedItem());

        group.setVmIds(new ArrayList<>());
        group.setVmLabels(new ArrayList<>());
        for (Guid selectedId : getVmsOrLabelsSelectionModel().getSelectedEntityIds()) {
            List<Guid> idList = labelIds.contains(selectedId) ? group.getVmLabels() : group.getVmIds();
            idList.add(selectedId);
        }

        // Save host details
        group.setVdsEnforcing(getHostAffinityEnforcing().getEntity());
        group.setVdsAffinityRule(getHostAffinityRule().getSelectedItem());

        group.setVdsIds(new ArrayList<>());
        group.setHostLabels(new ArrayList<>());
        for (Guid selectedId : getHostsOrLabelsSelectionModel().getSelectedEntityIds()) {
            List<Guid> idList = labelIds.contains(selectedId) ? group.getHostLabels() : group.getVdsIds();
            idList.add(selectedId);
        }

        startProgress();

        Frontend.getInstance().runAction(saveActionType,
                new AffinityGroupCRUDParameters(group.getId(), group),
                result -> {
                    stopProgress();
                    if (result != null && result.getReturnValue() != null && result.getReturnValue().getSucceeded()) {
                        cancel();
                    }
                },
                this);
    }

    protected boolean validate() {
        getName().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255),
                new I18NNameValidation() });
        getDescription().validateEntity(new IValidation[] { new AsciiOrNoneValidation() });
        getPriority().validateEntity(new IValidation[] { new DoubleValidation() });

        return getName().getIsValid() && getDescription().getIsValid() && getPriority().getIsValid();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }
}
