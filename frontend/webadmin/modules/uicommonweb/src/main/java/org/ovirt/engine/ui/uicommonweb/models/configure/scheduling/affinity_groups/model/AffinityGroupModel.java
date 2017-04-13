package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
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
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.VmsSelectionModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public abstract class AffinityGroupModel extends Model {
    private final AffinityGroup affinityGroup;
    private final ListModel<?> sourceListModel;
    private final VdcActionType saveActionType;

    private EntityModel<String> name;
    private EntityModel<String> description;
    private ListModel<EntityAffinityRule> vmAffinityRule;
    private EntityModel<Boolean> enforcing;
    private VmsSelectionModel vmsSelectionModel;
    private final Guid clusterId;
    private final String clusterName;

    public AffinityGroupModel(AffinityGroup affinityGroup, ListModel<?> sourceListModel,
            VdcActionType saveActionType,
            Guid clusterId,
            String clusterName) {
        this.affinityGroup = affinityGroup;
        this.sourceListModel = sourceListModel;
        this.saveActionType = saveActionType;
        this.clusterId = clusterId;
        this.clusterName = clusterName;

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setVmAffinityRule(new ListModel<EntityAffinityRule>());
        vmAffinityRule.setItems(Arrays.asList(EntityAffinityRule.values()), EntityAffinityRule.DISABLED);
        setEnforcing(new EntityModel<>(true));
        enforcing.setIsChangeable(false);

        vmAffinityRule.getSelectedItemChangedEvent().addListener((ev, sender, args) -> enforcing.setIsChangeable(vmAffinityRule.getSelectedItem() != EntityAffinityRule.DISABLED));

        setVmsSelectionModel(new VmsSelectionModel());

        addCommands();
    }

    public void init() {
        startProgress();
        //TODO: should be by cluster id and remove clusterName method from resolver.
        AsyncDataProvider.getInstance().getVmListByClusterName(new AsyncQuery<>(vmList -> {
            List<Guid> vmIds = getAffinityGroup().getVmIds();
            getVmsSelectionModel().init(vmList, vmIds != null ? vmIds : new ArrayList<Guid>());
            stopProgress();
        }), clusterName);
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

    public EntityModel<Boolean> getEnforcing() {
        return enforcing;
    }

    private void setEnforcing(EntityModel<Boolean> enforcing) {
        this.enforcing = enforcing;
    }

    public VmsSelectionModel getVmsSelectionModel() {
        return vmsSelectionModel;
    }

    private void setVmsSelectionModel(VmsSelectionModel vmsSelectionModel) {
        this.vmsSelectionModel = vmsSelectionModel;
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
        group.setVmEnforcing(getEnforcing().getEntity());
        group.setVmAffinityRule(getVmAffinityRule().getSelectedItem());
        group.setVmIds(getVmsSelectionModel().getSelectedVmIds());

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
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }
}
