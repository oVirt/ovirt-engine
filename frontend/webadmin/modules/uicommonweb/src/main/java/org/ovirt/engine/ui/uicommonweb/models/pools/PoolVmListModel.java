package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.VmErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.VmAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestContainerListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class PoolVmListModel extends VmListModel<VmPool> {

    private UICommand privateDetachCommand;

    public UICommand getDetachCommand() {
        return privateDetachCommand;
    }

    private void setDetachCommand(UICommand value) {
        privateDetachCommand = value;
    }

    @Inject
    public PoolVmListModel(final VmGeneralModel vmGeneralModel,
            final VmInterfaceListModel vmInterfaceListModel,
            final VmDiskListModel vmDiskListModel,
            final VmSnapshotListModel vmSnapshotListModel,
            final VmEventListModel vmEventListModel,
            final VmAppListModel<VM> vmAppListModel,
            final PermissionListModel<VM> permissionListModel,
            final VmAffinityGroupListModel vmAffinityGroupListModel,
            final VmGuestInfoModel vmGuestInfoModel,
            final Provider<ImportVmsModel> importVmsModelProvider,
            final VmHostDeviceListModel vmHostDeviceListModel,
            final VmDevicesListModel<VM> vmDevicesListModel,
            final VmAffinityLabelListModel vmAffinityLabelListModel,
            final VmErrataCountModel vmErrataCountModel,
            final VmGuestContainerListModel vmGuestContainerListModel,
            final ConfirmationModelSettingsManager confirmationModelSettingsManager) {
        super(vmGeneralModel, vmInterfaceListModel, vmDiskListModel,
                vmSnapshotListModel, vmEventListModel, vmAppListModel,
                permissionListModel, vmAffinityGroupListModel, vmGuestInfoModel,
                importVmsModelProvider, vmHostDeviceListModel, vmDevicesListModel,
                vmAffinityLabelListModel, vmErrataCountModel, vmGuestContainerListModel,
                confirmationModelSettingsManager);
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHelpTag(HelpTag.virtual_machines);
        setHashName("virtual_machines"); //$NON-NLS-1$

        setDetachCommand(new UICommand("Detach", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            setSearchString("Vms: pool=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("vm_pool_name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    public void detach() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().detachVirtualMachinesTitle());
        model.setHelpTag(HelpTag.detach_virtual_machine);
        model.setHashName("detach_virtual_machine"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (VM item : getSelectedItems()) {
            list.add(item.getName());
        }
        Collections.sort(list);
        model.setItems(list);

        if (list.size() == getEntity().getAssignedVmsCount()) {
            model.getLatch().setIsAvailable(true);
            model.getLatch().setIsChangeable(true);
            model.setNote(ConstantsManager.getInstance().getConstants().detachAllVmsWarning());
        }

        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSurYouWantToDetachSelectedVirtualMachinesMsg());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnDetach", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onDetach() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        boolean latchChecked = !model.validate();

        if (model.getProgress() != null || latchChecked) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            list.add(new RemoveVmFromPoolParameters(vm.getId(), true, true));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveVmFromPool, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                }, model);
    }

    @Override
    protected void onModelChangeRelevantForActions() {
        super.onModelChangeRelevantForActions();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        List<VM> items = getSelectedItems() != null ? getSelectedItems() : new ArrayList<VM>();

        boolean value = true;
        for (VM a : items) {
            if (a.getStatus() != VMStatus.Down) {
                value = false;
                break;
            }
        }
        getDetachCommand().setIsExecutionAllowed(items.size() > 0 && value);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getDetachCommand()) {
            detach();
        }
        if ("OnDetach".equals(command.getName())) { //$NON-NLS-1$
            onDetach();
        }
        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

}
