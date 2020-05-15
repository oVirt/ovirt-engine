package org.ovirt.engine.ui.uicommonweb.models.vms.hostdev;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class AddVmHostDevicesModel extends ModelWithPinnedHost {

    private ListModel<String> capability;

    private SortedListModel<EntityModel<HostDeviceView>> allAvailableHostDevices;
    private SortedListModel<EntityModel<HostDeviceView>> availableHostDevices;
    private SortedListModel<EntityModel<HostDeviceView>> selectedHostDevices;

    private UICommand addDeviceCommand = new UICommand(null, this);
    private UICommand removeDeviceCommand = new UICommand(null, this);

    private Set<String> alreadyAttachedDevices = new HashSet<>();

    public AddVmHostDevicesModel() {
        setCapability(new ListModel<String>());
        setAllAvailableHostDevices(new SortedListModel<EntityModel<HostDeviceView>>());
        setAvailableHostDevices(new SortedListModel<EntityModel<HostDeviceView>>());
        setSelectedHostDevices(new SortedListModel<EntityModel<HostDeviceView>>());

        setTitle(ConstantsManager.getInstance().getConstants().addVmHostDevicesTitle());
        setHelpTag(HelpTag.add_host_device);
        setHashName("add_host_device"); //$NON-NLS-1$

        getPinnedHost().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateAvailableHostDevices());

        getCapability().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateSelectedCapability());

        getAllAvailableHostDevices().getItemsChangedEvent().addListener((ev, sender, args) -> updateSelectedCapability());
    }

    @Override
    public void init(VM vm) {
        super.init(vm);

        initCapabilities();
        fetchExistingDevices();
    }

    private void fetchExistingDevices() {
        startProgress();
        AsyncDataProvider.getInstance().getConfiguredVmHostDevices(new AsyncQuery<>(devices -> {
            for (VmHostDevice device : devices) {
                if (!device.isIommuPlaceholder()) {
                    alreadyAttachedDevices.add(device.getDevice());
                }
            }
            // initHosts must be called after alreadyAttachedDevices are initialized
            initHosts();
        }), getVm().getId());
    }

    @SuppressWarnings("unchecked")
    private void initCapabilities() {
        List<String> capabilities = (List<String>) AsyncDataProvider.getInstance()
                .getConfigValuePreConverted(ConfigValues.HostDevicePassthroughCapabilities,
                                            getVm().getCompatibilityVersion().getValue());
        getCapability().setItems(capabilities);
    }

    private void updateAvailableHostDevices() {
        if (getPinnedHost().getSelectedItem() == null) {
            return;
        }

        availableHostDevices.setItems(new ArrayList<EntityModel<HostDeviceView>>());
        selectedHostDevices.setItems(new ArrayList<EntityModel<HostDeviceView>>());

        startProgress();
        AsyncDataProvider.getInstance().getHostDevicesByHostId(new AsyncQuery<>(fetchedDevices -> {
            stopProgress();
            // Show only devices that support assignment and are not yet attached to a VM or used by an SD.
            List<EntityModel<HostDeviceView>> models = fetchedDevices.stream()
                    .filter(hostDevice -> hostDevice.isAssignable() &&
                            !alreadyAttachedDevices.contains(hostDevice.getDeviceName()))
                    .map(EntityModel::new)
                    .collect(Collectors.toList());
            allAvailableHostDevices.setItems(models);
        }), getPinnedHost().getSelectedItem().getId());
    }

    private void updateSelectedCapability() {
        if (getAllAvailableHostDevices().getItems() == null) {
            return;
        }

        final String selectedCapability = getCapability().getSelectedItem();
        availableHostDevices.setItems(Linq.where(allAvailableHostDevices.getItems(),
                hostDevice -> hostDevice.getEntity().getCapability().equals(selectedCapability) &&
                        !getSelectedHostDevices().getItems().contains(hostDevice)));
    }

    public ListModel<String> getCapability() {
        return capability;
    }

    public void setCapability(ListModel<String> capability) {
        this.capability = capability;
    }

    public SortedListModel<EntityModel<HostDeviceView>> getAllAvailableHostDevices() {
        return allAvailableHostDevices;
    }

    public void setAllAvailableHostDevices(SortedListModel<EntityModel<HostDeviceView>> allAvailableHostDevices) {
        this.allAvailableHostDevices = allAvailableHostDevices;
    }

    public SortedListModel<EntityModel<HostDeviceView>> getAvailableHostDevices() {
        return availableHostDevices;
    }

    public void setAvailableHostDevices(SortedListModel<EntityModel<HostDeviceView>> availableHostDevices) {
        this.availableHostDevices = availableHostDevices;
    }

    public SortedListModel<EntityModel<HostDeviceView>> getSelectedHostDevices() {
        return selectedHostDevices;
    }

    public void setSelectedHostDevices(SortedListModel<EntityModel<HostDeviceView>> selectedHostDevices) {
        this.selectedHostDevices = selectedHostDevices;
    }

    public UICommand getAddDeviceCommand() {
        return addDeviceCommand;
    }

    public void setAddDeviceCommand(UICommand addDeviceCommand) {
        this.addDeviceCommand = addDeviceCommand;
    }

    public UICommand getRemoveDeviceCommand() {
        return removeDeviceCommand;
    }

    public void setRemoveDeviceCommand(UICommand removeDeviceCommand) {
        this.removeDeviceCommand = removeDeviceCommand;
    }

    public boolean validate() {
        getPinnedHost().validateSelectedItem(new IValidation[]{new NotEmptyValidation()});

        return getPinnedHost().getIsValid();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (getAddDeviceCommand().equals(command)) {
            addDevice();
        } else if (getRemoveDeviceCommand().equals(command)) {
            removeDevice();
        } else {
            super.executeCommand(command);
        }
    }

    private void addDevice() {
        // TODO: add all devices from given IOMMU group and notify user
    }

    private void removeDevice() {
        // TODO: remove all devices from given IOMMU group and notify user
    }
}
