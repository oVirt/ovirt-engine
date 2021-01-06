package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class VmHostDeviceListModel extends HostDeviceListModelBase<VM> {

    private UICommand addCommand;
    private UICommand removeCommand;
    private UICommand repinHostCommand;

    private final UIConstants constants;

    @Inject
    public VmHostDeviceListModel(UIConstants constants) {
        this.constants = constants;

        setTitle(constants.hostDevicesTitle());
        setHelpTag(HelpTag.vm_host_devices);
        setHashName("host_devices"); //$NON-NLS-1$

        setAddCommand(new UICommand("New", this)); //$NON-NLS-1$
        getAddCommand().setIsExecutionAllowed(true);

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        setRepinHostCommand(new UICommand("RepinHost", this)); //$NON-NLS-1$
        getRepinHostCommand().setIsExecutionAllowed(true);

        getCommands().add(UICommand.createDefaultOkUiCommand("OnRepin", this)); //$NON-NLS-1$
        getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getRemoveCommand().setIsExecutionAllowed(!getSelectedPrimaryDeviceNames().isEmpty());
    }

    @Override
    protected String getListName() {
        return "VmHostDeviceListModel"; //$NON-NLS-1$
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddCommand()) {
            addDevices();
        } else if (command == getRemoveCommand()) {
            removeDevices();
        } else if (command == getRepinHostCommand()) {
            repinHost();
        } else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("OnRepin".equals(command.getName())) { //$NON-NLS-1$
            onRepin();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() != null) {
            syncSearch(QueryType.GetExtendedVmHostDevicesByVmId, new IdQueryParameters(getEntity().getId()));
        }
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void addDevices() {
        if (getWindow() != null) {
            return;
        }

        AddVmHostDevicesModel window = new AddVmHostDevicesModel();
        window.init(getEntity());

        window.getCommands().add(UICommand.createDefaultOkUiCommand("OnSave", this)); //$NON-NLS-1$
        window.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        setWindow(window);
    }

    private void removeDevices() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel window = new ConfirmationModel();
        setConfirmWindow(window);
        window.setTitle(constants.removeHostDevices());
        window.setHelpTag(HelpTag.remove_host_device);
        window.setHashName("remove_host_device"); //$NON-NLS-1$

        window.setItems(getSelectedPrimaryDeviceNames());

        window.getCommands().add(UICommand.createDefaultOkUiCommand("OnRemove", this)); //$NON-NLS-1$
        window.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void repinHost() {
        if (getWindow() != null) {
            return;
        }

        RepinHostModel window = new RepinHostModel();
        window.init(getEntity());

        window.getCommands().add(UICommand.createDefaultOkUiCommand("OnRepin", this)); //$NON-NLS-1$
        window.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        setWindow(window);
    }

    private void onSave() {
        final AddVmHostDevicesModel model = (AddVmHostDevicesModel) getWindow();
        if (!model.validate()) {
            return;
        }

        model.startProgress();
        if (getEntity().getDedicatedVmForVdsList().isEmpty() || !getEntity().getDedicatedVmForVdsList().contains(model.getPinnedHost().getSelectedItem().getId())) {
            pinVmToHost(model.getPinnedHost().getSelectedItem().getId(), result -> {
                if (result.getReturnValue().getSucceeded()) {
                    doAttachDevices(model.getSelectedHostDevices().getItems());
                }
            });
        } else {
            doAttachDevices(model.getSelectedHostDevices().getItems());
        }
    }

    private void doAttachDevices(Collection<EntityModel<HostDeviceView>> items) {
        List<String> deviceNamesToAttach = new ArrayList<>();
        for (EntityModel<HostDeviceView> model : items) {
            deviceNamesToAttach.add(model.getEntity().getDeviceName());
        }
        Frontend.getInstance().runAction(ActionType.AddVmHostDevices, new VmHostDevicesParameters(getEntity().getId(), deviceNamesToAttach),
                result -> {
                    syncSearch();
                    getWindow().stopProgress();
                    setWindow(null);
                });
    }

    private void pinVmToHost(Guid hostId, IFrontendActionAsyncCallback callback) {
        getEntity().setDedicatedVmForVdsList(Collections.singletonList(hostId));
        Frontend.getInstance().runAction(ActionType.UpdateVm, new VmManagementParametersBase(getEntity().getStaticData()), callback);
    }

    private void onRemove() {
        final ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        model.startProgress();
        ArrayList<String> deviceNames = getSelectedPrimaryDeviceNames();
        Frontend.getInstance().runAction(ActionType.RemoveVmHostDevices, new VmHostDevicesParameters(getEntity().getId(), deviceNames),
                result -> {
                    model.stopProgress();
                    setConfirmWindow(null);
                });
    }

    /**
     * Returns list of primary (non-placeholder) device names from the current selection.
     * Returns empty list when none or only placeholder devices are selected.
     */
    private ArrayList<String> getSelectedPrimaryDeviceNames() {
        ArrayList<String> deviceNames = new ArrayList<>();
        if (getSelectedItems() != null) {
            for (HostDeviceView vmHostDevice : getSelectedItems()) {
                // only non-placeholder devices should be taken into account
                if (!vmHostDevice.isIommuPlaceholder()) {
                    deviceNames.add(vmHostDevice.getDeviceName());
                }
            }
        }
        return deviceNames;
    }

    private void onRepin() {
        final RepinHostModel model = (RepinHostModel) getWindow();

        model.startProgress();
        pinVmToHost(model.getPinnedHost().getSelectedItem().getId(), result -> {
            model.stopProgress();
            setWindow(null);
        });
    }

    private void cancel() {
        setWindow(null);
        setConfirmWindow(null);
    }

    public UICommand getAddCommand() {
        return addCommand;
    }

    private void setAddCommand(UICommand addCommand) {
        this.addCommand = addCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand removeCommand) {
        this.removeCommand = removeCommand;
    }

    public UICommand getRepinHostCommand() {
        return repinHostCommand;
    }

    private void setRepinHostCommand(UICommand repinHostCommand) {
        this.repinHostCommand = repinHostCommand;
    }
}
