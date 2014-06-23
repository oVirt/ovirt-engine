package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class PoolVmListModel extends VmListModel {

    private UICommand privateDetachCommand;

    public UICommand getDetachCommand() {
        return privateDetachCommand;
    }

    private void setDetachCommand(UICommand value) {
        privateDetachCommand = value;
    }

    @Override
    public VmPool getEntity() {
        return (VmPool) super.getEntity();
    }

    @Inject
    public PoolVmListModel(final VmGeneralModel vmGeneralModel, final VmInterfaceListModel vmInterfaceListModel,
            final VmDiskListModel vmDiskListModel, final VmSnapshotListModel vmSnapshotListModel,
            final VmEventListModel vmEventListModel, final VmAppListModel vmAppListModel,
            final PermissionListModel permissionListModel, final VmAffinityGroupListModel vmAffinityGroupListModel,
            final VmSessionsModel vmSessionsModel) {
        super(vmGeneralModel, vmInterfaceListModel, vmDiskListModel, vmSnapshotListModel, vmEventListModel,
                vmAppListModel, permissionListModel, vmAffinityGroupListModel, vmSessionsModel);
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

        ArrayList<String> list = new ArrayList<String>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            list.add(a.getName());
        }
        Collections.sort(list);
        model.setItems(list);

        if (list.size() == getEntity().getAssignedVmsCount()) {
            model.getLatch().setIsAvailable(true);
            model.getLatch().setIsChangable(true);
            model.setNote(ConstantsManager.getInstance().getConstants().detachAllVmsWarning());
        }

        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSurYouWantToDetachSelectedVirtualMachinesMsg());

        UICommand tempVar = new UICommand("OnDetach", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void onDetach() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        boolean latchChecked = !model.validate();

        if (model.getProgress() != null || latchChecked) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            list.add(new RemoveVmFromPoolParameters(vm.getId(), true));
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveVmFromPool, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) {//$NON-NLS-1$
            updateActionAvailability();
        }
    }

    private void updateActionAvailability() {
        ArrayList<VM> items =
                getSelectedItems() != null ? Linq.<VM> cast(getSelectedItems()) : new ArrayList<VM>();

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
