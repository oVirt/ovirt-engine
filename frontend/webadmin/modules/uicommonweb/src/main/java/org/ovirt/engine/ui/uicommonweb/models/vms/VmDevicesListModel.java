package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.HotUnplugMemoryParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VmDevicesListModel <E extends BusinessEntity<Guid> & Nameable> extends SearchableListModel<E, VmDevice> {

    private final UICommand memoryHotUnplugCommand =
            UICommand.createOkUiCommand("memoryHotUnplug", this); //$NON-NLS-1$
    private final UICommand cancelMemoryHotUnplugCommand =
            UICommand.createCancelUiCommand("cancelMemoryHotUnplug", this); //$NON-NLS-1$

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    public VmDevicesListModel() {
        setTitle(constants.vmDevicesTitle());
        setHelpTag(HelpTag.vm_devices);
        setHashName("vm_devices"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }


    @Override
    protected void syncSearch() {
        if (getEntity() != null) {
            super.syncSearch(VdcQueryType.GetVmDevicesForVm, new IdQueryParameters(getEntity().getId()));
        }
    }

    @Override
    protected String getListName() {
        return "VmDevicesListModel"; //$NON-NLS-1$
    }

    public void onHotUnplug(VmDevice vmDevice) {
        if (getEntity() == null) {
            return;
        }
        if (vmDevice == null || vmDevice.getType() != VmDeviceGeneralType.MEMORY) {
            return;
        }
        final Integer memorySizeMb = (Integer) vmDevice.getSpecParams().get("size"); //$NON-NLS-1$
        if (memorySizeMb == null) {
            return;
        }
        setSelectedItem(vmDevice);
        ConfirmationModel confirmationModel = new ConfirmationModel();

        confirmationModel.setTitle(constants.memoryHotUnplug());
        confirmationModel.setHelpTag(HelpTag.template_not_found_on_export_domain);

        confirmationModel.setMessage(messages.memoryHotUnplugConfirmation(memorySizeMb, getEntity().getName()));


        confirmationModel.getCommands().add(memoryHotUnplugCommand);
        confirmationModel.getCommands().add(cancelMemoryHotUnplugCommand);

        setConfirmWindow(confirmationModel);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command == memoryHotUnplugCommand) {
            onMemoryHotUnplugConfirmed();
        } else if (command == cancelMemoryHotUnplugCommand) {
            setConfirmWindow(null);
        }
    }

    private void onMemoryHotUnplugConfirmed() {
        setConfirmWindow(null);
        final VmDevice vmDevice = getSelectedItem();
        if (vmDevice == null || vmDevice.getType() != VmDeviceGeneralType.MEMORY) {
            return;
        }
        Frontend.getInstance().runAction(
                VdcActionType.HotUnplugMemory, new HotUnplugMemoryParameters(vmDevice.getId()), true);
    }
}
