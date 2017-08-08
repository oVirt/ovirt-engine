package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HotUnplugMemoryParameters;
import org.ovirt.engine.core.common.businessentities.UsbControllerModel;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
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
    // USB controller model
    public static final String MODEL = "model";  //$NON-NLS-1$


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
            final AsyncQuery<QueryReturnValue> asyncQuery = new AsyncQuery<QueryReturnValue>(
                    new AsyncCallback<QueryReturnValue>() {
                        @Override
                        public void onSuccess(QueryReturnValue returnValue) {
                            final Collection<VmDevice> vmDevices = returnValue.getReturnValue();
                            final ArrayList<VmDeviceFeEntity> frontendDevices = new ArrayList<>();
                            for (VmDevice vmDevice : vmDevices) {
                                // exclude USB controller devices with model property set to 'none'
                                if (vmDevice.getDevice().equals(VmDeviceType.USB.getName())
                                        && vmDevice.getType() == VmDeviceGeneralType.CONTROLLER
                                        && (vmDevice.getSpecParams().get(MODEL)) != null
                                        && vmDevice.getSpecParams().get(MODEL).equals(UsbControllerModel.NONE.libvirtName)) {
                                    continue;
                                }

                                frontendDevices.add(new VmDeviceFeEntity(
                                        vmDevice,
                                        vm,
                                        idsOfDevicesBeingUnplugged.contains(vmDevice.getId())));
                            }
                            setItems(frontendDevices);
                        }
            }) {};
            super.syncSearch(QueryType.GetVmDevicesForVm, new IdQueryParameters(vm.getId()), asyncQuery);
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

        final Optional<Integer> memorySizeOptional =
                VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(deviceEntity.getVmDevice());
        if (!memorySizeOptional.isPresent()) {
            return;
        }
        final int memorySizeMb = memorySizeOptional.get();

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
        idsOfDevicesBeingUnplugged.add(deviceEntity.getVmDevice().getId(), removedItem -> updateItems());
        updateItems();
        Frontend.getInstance().runAction(
                ActionType.HotUnplugMemory,
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
