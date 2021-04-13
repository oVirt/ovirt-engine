package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class AttachDiskModel extends NewDiskModel {
    protected static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private Map<DiskStorageType, ListModel<EntityModel<DiskModel>>> attachableDisksMap;
    private EntityModel<String> messageLabel;
    private EntityModel<String> warningLabel;

    public AttachDiskModel() {
        attachableDisksMap = new HashMap<>();
        attachableDisksMap.put(DiskStorageType.IMAGE, new ListModel<EntityModel<DiskModel>>());
        attachableDisksMap.put(DiskStorageType.LUN, new ListModel<EntityModel<DiskModel>>());
        attachableDisksMap.put(DiskStorageType.MANAGED_BLOCK_STORAGE, new ListModel<EntityModel<DiskModel>>());
        setWarningLabel(new EntityModel<String>());
        getWarningLabel().setIsAvailable(false);
        setMessageLabel(new EntityModel<String>());
        getMessageLabel().setIsAvailable(false);
        addListeners();
    }

    public Map<DiskStorageType, ListModel<EntityModel<DiskModel>>> getAttachableDisksMap() {
        return attachableDisksMap;
    }

    @Override
    public void flush() {
        // no need to do any flush
    }

    @Override
    public void initialize() {
        super.initialize();

        getIsPlugged().setIsAvailable(true);

        if (getVm().getId() != null) {
            loadAttachableDisks();
        }

        getIsBootable().setIsChangeable(true);
    }

    @Override
    public void updateCanSetBoot(List<Disk> vmDisks) {
        boolean bootDiskFound = false;
        for (Disk disk : vmDisks) {
            if (disk.getDiskVmElementForVm(getVmId()).isBoot()) {
                bootDiskFound = true;
                break;
            }
        }
        if (bootDiskFound) {
            getIsBootable().setIsChangeable(false);
        } else {
            addBootChangeListener();
        }
    }

    @Override
    protected void updatePassDiscardAvailability() {
        // Pass discard is not relevant for the attach
        // disk window, only for the new and edit windows.
    }

    @Override
    protected void updatePassDiscardChangeability() {
        // Pass discard is not relevant for the attach
        // disk window, only for the new and edit windows.
    }

    private void addBootChangeListener() {
        // Whenever the changeability of isBootable changes propagate it to all child DiskModels to reflect it in the view
        getIsBootable().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                for (ListModel<EntityModel<DiskModel>> disks : getAttachableDisksMap().values()) {
                    if (disks.getItems() != null) {
                        for (EntityModel<DiskModel> disk : disks.getItems()) {
                            boolean isChangeBootAllowed = ((EntityModel) sender).getIsChangable();
                            disk.getEntity().getIsBootable().setIsChangeable(isChangeBootAllowed);
                        }
                        disks.getItemsChangedEvent().raise(disks, EventArgs.EMPTY);
                    }
                }
            }
        });
    }

    public void loadAttachableDisks() {
        doLoadAttachableDisks(new GetDisksCallback(DiskStorageType.IMAGE),
                new GetDisksCallback(DiskStorageType.LUN),
                new GetDisksCallback(DiskStorageType.MANAGED_BLOCK_STORAGE));
    }

    protected void doLoadAttachableDisks(GetDisksCallback imageCallback, GetDisksCallback lunCallback,
            GetDisksCallback managedBlockCallback) {
        AsyncDataProvider.getInstance().getAllAttachableDisks(
                new AsyncQuery<>(imageCallback), getVm().getStoragePoolId(), getVm().getId());

        AsyncDataProvider.getInstance().getAllAttachableDisks(
                new AsyncQuery<>(lunCallback), null, getVm().getId());

        AsyncDataProvider.getInstance().getAllAttachableDisks(
                new AsyncQuery<>(managedBlockCallback), getVm().getStoragePoolId(), getVm().getId());
    }

    class GetDisksCallback implements AsyncCallback<List<Disk>> {

        private DiskStorageType diskStorageType;

        GetDisksCallback(DiskStorageType diskStorageType) {
            this.diskStorageType = diskStorageType;
        }

        @Override
        public void onSuccess(List<Disk> returnValue) {
            List<Disk> disks = adjustReturnValue(returnValue);
            Collections.sort(disks, new DiskByDiskAliasComparator());
            final List<DiskModel> diskModels = DiskModel.disksToDiskModelList(disks);

            AsyncDataProvider.getInstance().getDiskInterfaceList(getVm().getVmOsId(),
                        getVm().getClusterCompatibilityVersion(), getVm().getBiosType().getChipsetType(),
                        new AsyncQuery<>(
                    diskInterfaces -> AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(new AsyncQuery<>(
                            virtioScsiEnabledReturnValue -> {
                                boolean virtioScsiEnabled = Boolean.TRUE.equals(virtioScsiEnabledReturnValue);
                                if (!virtioScsiEnabled) {
                                    diskInterfaces.remove(DiskInterface.VirtIO_SCSI);
                                }
                                for (DiskModel diskModel : diskModels) {
                                    diskModel.getDiskInterface().setItems(diskInterfaces);
                                    diskModel.getDiskInterface().setSelectedItem(virtioScsiEnabled ? DiskInterface.VirtIO_SCSI : DiskInterface.VirtIO);
                                    if (getIsBootable().getIsChangable()) { // no point in adding a listener if the value cam't be changed
                                        diskModel.getIsBootable().getEntityChangedEvent().addListener((ev, sender, args) -> {
                                            boolean isBootableMarked = (Boolean) ((EntityModel) sender).getEntity();
                                            getIsBootable().setIsChangeable(!isBootableMarked);
                                        });
                                    } else {
                                        diskModel.getIsBootable().setIsChangeable(false);
                                        diskModel.getIsBootable().setChangeProhibitionReason(constants.onlyOneBootableDisk());
                                    }
                                }
                                List<EntityModel<DiskModel>> entities =
                                        diskModels.stream()
                                            .filter(m -> m.getDisk().getDiskStorageType() == diskStorageType)
                                            .map(EntityModel::new)
                                            .collect(Collectors.toList());

                                initAttachableDisks(entities);
                            }), getVmId())));


        }

        protected void initAttachableDisks(List<EntityModel<DiskModel>> entities) {
            getAttachableDisksMap().get(diskStorageType).setItems(entities);
        }

        protected List<Disk> adjustReturnValue(List<Disk> returnValue) {
            return returnValue;
        }
    }

    @Override
    public boolean validate() {
        if (isNoSelection()) {
            getInvalidityReasons().add(constants.noDisksSelected());
            setIsValid(false);
            return false;
        }
        return true;
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        if (getProgress() != null || !validate()) {
            return;
        }

        ArrayList<ActionType> actionTypes = new ArrayList<>();
        ArrayList<ActionParametersBase> paramerterList = new ArrayList<>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();

        IFrontendActionAsyncCallback onFinishCallback = callback != null ? callback : result -> {
            NewDiskModel diskModel = (NewDiskModel) result.getState();
            diskModel.stopProgress();
            diskModel.cancel();
        };

        List<EntityModel<DiskModel>> disksToAttach = getSelectedDisks();
        for (int i = 0; i < disksToAttach.size(); i++) {
            DiskModel disk = disksToAttach.get(i).getEntity();

            /*
            IDE disks can be activated only when the VM is down.
            Other disks can be hot plugged.
             */
            boolean activate = false;
            if (getIsPlugged().getEntity()) {
                activate = disk.getDiskInterface().getSelectedItem() == DiskInterface.IDE ?
                        getVm().getStatus() == VMStatus.Down : true;
            }


            DiskVmElement dve = new DiskVmElement(disk.getDisk().getId(), getVm().getId());
            dve.setBoot(disk.getIsBootable().getEntity());
            dve.setDiskInterface(disk.getDiskInterface().getSelectedItem());
            dve.setReadOnly(disk.isReadOnly());
            AttachDetachVmDiskParameters parameters = new AttachDetachVmDiskParameters(dve , activate);

            actionTypes.add(ActionType.AttachDiskToVm);
            paramerterList.add(parameters);
            callbacks.add(i == disksToAttach.size() - 1 ? onFinishCallback : null);
        }

        startProgress();

        Frontend.getInstance().runMultipleActions(actionTypes, paramerterList, callbacks, null, this);
    }

    public EntityModel<String> getWarningLabel() {
        return warningLabel;
    }

    public void setWarningLabel(EntityModel<String> value) {
        warningLabel = value;
    }

    public EntityModel<String> getMessageLabel() {
        return messageLabel;
    }

    public void setMessageLabel(EntityModel<String> messageLabel) {
        this.messageLabel = messageLabel;
    }

    private boolean isNoSelection() {
        for (ListModel<EntityModel<DiskModel>> listModel : attachableDisksMap.values()) {
            boolean multipleSelectionSelected = listModel.getSelectedItems() != null && !listModel.getSelectedItems().isEmpty();
            boolean singleSelectionSelected = listModel.getSelectedItem() != null;
            if (multipleSelectionSelected || singleSelectionSelected) {
                return false;
            }
        }
        return true;
    }

    public List<EntityModel<DiskModel>> getSelectedDisks() {
        List<EntityModel<DiskModel>> selectedDisks = new ArrayList<>();
        for (ListModel<EntityModel<DiskModel>> listModel : attachableDisksMap.values()) {
            if (listModel.getSelectedItems() != null && !listModel.getSelectedItems().isEmpty()) {
                selectedDisks.addAll(listModel.getSelectedItems());
            }

            if (listModel.getSelectedItem() != null) {
                selectedDisks.add(listModel.getSelectedItem());
            }
        }
        return selectedDisks;
    }

    private boolean isSelectedDiskInterfaceIDE(List<EntityModel<DiskModel>> selectedDisks) {
        for (EntityModel<DiskModel> selectedDisk : selectedDisks) {
            if (selectedDisk.getEntity().getDiskInterface().getSelectedItem() == DiskInterface.IDE) {
                return true;
            }
        }
        return false;
    }

    private void addListeners() {
        addSelectedItemsChangedListener();
        addIsPluggedEntityChangedListener();
    }

    private void updateWarningLabel() {
        getWarningLabel().setIsAvailable(false);
        if (getIsPlugged().getEntity().equals(Boolean.TRUE) && getVm().getStatus() != VMStatus.Down) {
            List<EntityModel<DiskModel>> selectedDisks = getSelectedDisks();
            if (selectedDisks != null && isSelectedDiskInterfaceIDE(selectedDisks)) {
                getWarningLabel().setEntity(constants.ideDisksWillBeAttachedButNotActivated());
                getWarningLabel().setIsAvailable(true);
            }
        }
    }

    private void addSelectedItemsChangedListener() {
        IEventListener<EventArgs> selectionChangedListener = (ev, sender, args) -> updateWarningLabel();
        attachableDisksMap.get(DiskStorageType.IMAGE).
                getSelectedItemsChangedEvent().addListener(selectionChangedListener);
        attachableDisksMap.get(DiskStorageType.LUN).
                getSelectedItemsChangedEvent().addListener(selectionChangedListener);
    }

    private void addIsPluggedEntityChangedListener() {
        IEventListener<EventArgs> entityChangedListener = (ev, sender, args) -> updateWarningLabel();
        getIsPlugged().getEntityChangedEvent().addListener(entityChangedListener);
    }
}
