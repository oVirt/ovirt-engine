package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.action.HotUnplugMemoryParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.utils.ExpiringSet;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VmDevicesListModel<E extends VM>
        extends SearchableListModel<E, VmDeviceFeEntity> {

    private static final int UNPLUGGING_LABEL_DURATION_SEC = 15;

    private final UICommand memoryHotUnplugCommand =
            UICommand.createOkUiCommand("memoryHotUnplug", this); //$NON-NLS-1$
    private final UICommand cancelMemoryHotUnplugCommand =
            UICommand.createCancelUiCommand("cancelMemoryHotUnplug", this); //$NON-NLS-1$

    private ExpiringSet<VmDeviceId> idsOfDevicesBeingUnplugged = new ExpiringSet<>(UNPLUGGING_LABEL_DURATION_SEC);

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
            final E vm = getEntity();
            final AsyncQuery<VdcQueryReturnValue> asyncQuery = new AsyncQuery<VdcQueryReturnValue>(
                    new AsyncCallback<VdcQueryReturnValue>() {
                        @Override
                        public void onSuccess(VdcQueryReturnValue returnValue) {
                            final Collection<VmDevice> vmDevices = returnValue.getReturnValue();
                            final ArrayList<VmDeviceFeEntity> frontendDevices = new ArrayList<>();
                            for (VmDevice vmDevice : vmDevices) {
                                frontendDevices.add(new VmDeviceFeEntity(
                                        vmDevice,
                                        vm,
                                        idsOfDevicesBeingUnplugged.contains(vmDevice.getId())));
                            }
                            setItems(frontendDevices);
                        }
            }) {};
            super.syncSearch(VdcQueryType.GetVmDevicesForVm, new IdQueryParameters(vm.getId()), asyncQuery);
        }
    }

    @Override
    protected String getListName() {
        return "VmDevicesListModel"; //$NON-NLS-1$
    }

    public void onHotUnplug(VmDeviceFeEntity deviceEntity) {
        if (getEntity() == null) {
            return;
        }
        if (deviceEntity == null || deviceEntity.getVmDevice().getType() != VmDeviceGeneralType.MEMORY) {
            return;
        }
        final Integer memorySizeMb = (Integer) deviceEntity.getVmDevice().getSpecParams().get("size"); //$NON-NLS-1$
        if (memorySizeMb == null) {
            return;
        }
        setSelectedItem(deviceEntity);
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
        final VmDeviceFeEntity deviceEntity = getSelectedItem();
        if (deviceEntity == null || deviceEntity.getVmDevice().getType() != VmDeviceGeneralType.MEMORY) {
            return;
        }
        idsOfDevicesBeingUnplugged.add(deviceEntity.getVmDevice().getId(), new ExpiringSet.RemovalAction<VmDeviceId>() {
            @Override
            public void itemRemoved(VmDeviceId item) {
                updateItems();
            }
        });
        updateItems();
        Frontend.getInstance().runAction(
                VdcActionType.HotUnplugMemory,
                new HotUnplugMemoryParameters(deviceEntity.getVmDevice().getId()), true);
    }

    private void updateItems() {
        Collection<VmDeviceFeEntity> items = getItems();
        for (VmDeviceFeEntity item: items) {
            item.setBeingUnplugged(idsOfDevicesBeingUnplugged.contains(item.getVmDevice().getId()));
        }
        setItems(new ArrayList<>(items));
    }
}
