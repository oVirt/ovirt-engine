package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
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
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.HostsSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.VmsSelectionModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public abstract class AffinityGroupModel extends Model {
    private final AffinityGroup affinityGroup;
    private final ListModel<?> sourceListModel;
    private final ActionType saveActionType;

    private EntityModel<String> name;
    private EntityModel<String> description;
    private ListModel<EntityAffinityRule> vmAffinityRule;
    private EntityModel<Boolean> vmAffinityEnforcing;
    private ListModel<EntityAffinityRule> hostAffinityRule;
    private EntityModel<Boolean> hostAffinityEnforcing;
    private VmsSelectionModel vmsSelectionModel;
    private HostsSelectionModel hostsSelectionModel;
    private final Guid clusterId;
    private final String clusterName;

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

        // Set VM details
        setVmAffinityRule(new ListModel<EntityAffinityRule>());
        vmAffinityRule.setItems(Arrays.asList(EntityAffinityRule.values()), affinityGroup.getVmAffinityRule());
        setVmAffinityEnforcing(new EntityModel<>(affinityGroup.isVmEnforcing()));
        vmAffinityRule.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateChangeableEnforcing());
        setVmsSelectionModel(new VmsSelectionModel());

        // Set host details
        setHostAffinityRule(new ListModel<EntityAffinityRule>());
        hostAffinityRule.setItems(Arrays.asList(EntityAffinityRule.values()), affinityGroup.getVdsAffinityRule());
        setHostAffinityEnforcing(new EntityModel<>(affinityGroup.isVdsEnforcing()));
        hostAffinityRule.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateChangeableEnforcing());
        setHostsSelectionModel(new HostsSelectionModel());

        updateChangeableEnforcing();

        addCommands();
    }

    public void init() {
        startProgress();

        //TODO: should be by cluster id and remove clusterName method from resolver.
        AsyncDataProvider.getInstance().getVmListByClusterName(new AsyncQuery<>(vmList -> {
            List<Guid> vmIds = getAffinityGroup().getVmIds();
            getVmsSelectionModel().init(vmList, vmIds != null ? vmIds : new ArrayList<>());
            stopProgressOnVmsAndHostsInit();
        }), clusterName);

        AsyncDataProvider.getInstance().getHostListByClusterId(new AsyncQuery<>(hostList -> {
            List<Guid> hostIds = getAffinityGroup().getVdsIds();
            getHostsSelectionModel().init(hostList, hostIds != null ? hostIds : new ArrayList<>());
            stopProgressOnVmsAndHostsInit();
        }), clusterId);
    }

    private void stopProgressOnVmsAndHostsInit() {
        if (getVmsSelectionModel().isInitialized() && getHostsSelectionModel().isInitialized()) {
            stopProgress();
        }
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

    public VmsSelectionModel getVmsSelectionModel() {
        return vmsSelectionModel;
    }

    private void setVmsSelectionModel(VmsSelectionModel vmsSelectionModel) {
        this.vmsSelectionModel = vmsSelectionModel;
    }

    public HostsSelectionModel getHostsSelectionModel() {
        return hostsSelectionModel;
    }

    private void setHostsSelectionModel(HostsSelectionModel hostsSelectionModel) {
        this.hostsSelectionModel = hostsSelectionModel;
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

        // Save VM details
        group.setVmEnforcing(getVmAffinityEnforcing().getEntity());
        group.setVmAffinityRule(getVmAffinityRule().getSelectedItem());
        group.setVmIds(getVmsSelectionModel().getSelectedVmIds());

        // Save host details
        group.setVdsEnforcing(getHostAffinityEnforcing().getEntity());
        group.setVdsAffinityRule(getHostAffinityRule().getSelectedItem());
        group.setVdsIds(getHostsSelectionModel().getSelectedHostIds());

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

        return getName().getIsValid() && getDescription().getIsValid();
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
