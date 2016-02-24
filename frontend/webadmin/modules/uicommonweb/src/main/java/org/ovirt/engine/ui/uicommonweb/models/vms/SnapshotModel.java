package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class SnapshotModel extends EntityModel<Snapshot> {
    private VM vm;

    public VM getVm() {
        return vm;
    }

    public void setVm(VM value) {
        if (vm != value) {
            vm = value;
            onPropertyChanged(new PropertyChangedEventArgs("VM")); //$NON-NLS-1$
        }
    }

    private ArrayList<DiskImage> disks;

    public ArrayList<DiskImage> getDisks() {
        return disks;
    }

    public void setDisks(ArrayList<DiskImage> value) {
        if (disks != value) {
            disks = value;
            onPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
    }

    private List<DiskImage> vmDisks;

    public List<DiskImage> getVmDisks() {
        return vmDisks;
    }

    public void setVmDisks(List<DiskImage> value) {
        if (vmDisks != value) {
            vmDisks = value;
            onPropertyChanged(new PropertyChangedEventArgs("VmDisks")); //$NON-NLS-1$
        }
    }

    private List<VmNetworkInterface> nics;

    public List<VmNetworkInterface> getNics() {
        return nics;
    }

    public void setNics(List<VmNetworkInterface> value) {
        if (nics != value) {
            nics = value;
            onPropertyChanged(new PropertyChangedEventArgs("Nics")); //$NON-NLS-1$
        }
    }

    private List<String> apps;

    public List<String> getApps() {
        return apps;
    }

    public void setApps(List<String> value) {
        if (apps != value) {
            apps = value;
            onPropertyChanged(new PropertyChangedEventArgs("Apps")); //$NON-NLS-1$
        }
    }

    private EntityModel<String> privateDescription;

    public EntityModel<String> getDescription() {
        return privateDescription;
    }

    public void setDescription(EntityModel<String> value) {
        privateDescription = value;
    }

    private EntityModel<Boolean> memory;

    public EntityModel<Boolean> getMemory() {
        return memory;
    }

    public void setMemory(EntityModel<Boolean> value) {
        memory = value;
    }

    private boolean validateByVmSnapshots;

    public boolean isValidateByVmSnapshots() {
        return validateByVmSnapshots;
    }

    public void setValidateByVmSnapshots(boolean validateByVmSnapshots) {
        this.validateByVmSnapshots = validateByVmSnapshots;
    }

    private ListModel<DiskImage> snapshotDisks;

    public ListModel<DiskImage> getSnapshotDisks() {
        return snapshotDisks;
    }

    public void setSnapshotDisks(ListModel<DiskImage> value) {
        snapshotDisks = value;
    }

    private boolean showMemorySnapshotWarning;

    public boolean isShowMemorySnapshotWarning() {
        return showMemorySnapshotWarning;
    }

    public void setShowMemorySnapshotWarning(boolean value) {
        showMemorySnapshotWarning = value;
    }

    private boolean oldClusterSnapshotWithMemory;

    public boolean isOldClusterSnapshotWithMemory() {
        return oldClusterSnapshotWithMemory;
    }

    public void setOldClusterSnapshotWithMemory(boolean value) {
        oldClusterSnapshotWithMemory = value;
    }

    private boolean showPartialSnapshotWarning;

    public boolean isShowPartialSnapshotWarning() {
        return showPartialSnapshotWarning;
    }

    public void setShowPartialSnapshotWarning(boolean value) {
        showPartialSnapshotWarning = value;
    }

    private ListModel<PreivewPartialSnapshotOption> partialPreviewSnapshotOptions;

    public ListModel<PreivewPartialSnapshotOption> getPartialPreviewSnapshotOptions() {
        return partialPreviewSnapshotOptions;
    }

    private void setPartialPreviewSnapshotOptions(ListModel<PreivewPartialSnapshotOption> value) {
        partialPreviewSnapshotOptions = value;
    }

    private UICommand cancelCommand;

    public UICommand getCancelCommand() {
        return cancelCommand != null ? cancelCommand : super.getCancelCommand();
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public SnapshotModel() {
        setDescription(new EntityModel<String>());
        setMemory(new EntityModel<>(true));
        setDisks(new ArrayList<DiskImage>());
        setNics(new ArrayList<VmNetworkInterface>());
        setApps(new ArrayList<String>());
        setSnapshotDisks(new ListModel<DiskImage>());

        ListModel<PreivewPartialSnapshotOption> partialPreviewSnapshotOptions = new ListModel<>();
        partialPreviewSnapshotOptions.setItems(Arrays.asList(PreivewPartialSnapshotOption.values()));
        setPartialPreviewSnapshotOptions(partialPreviewSnapshotOptions);
    }

    public static SnapshotModel createNewSnapshotModel(ICommandTarget cancelCommandTarget) {
        SnapshotModel model = new SnapshotModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().createSnapshotTitle());
        model.setHelpTag(HelpTag.create_snapshot);
        model.setHashName("create_snapshot"); //$NON-NLS-1$

        // the cancel command has to be created be before the call to initialize to avoid race condition
        model.setCancelCommand(UICommand.createCancelUiCommand("Cancel", cancelCommandTarget)); //$NON-NLS-1$

        return model;
    }
    @Override
    public void initialize() {
        super.initialize();

        startProgress();
        initMessages();
    }

    private void initMessages() {
        if (vm.isRunning() && !vm.getHasAgent()) {
            setMessage(ConstantsManager.getInstance().getConstants().liveSnapshotWithNoGuestAgentMsg());
        }

        if (isValidateByVmSnapshots()) {
            initVmSnapshots();
        }
        else {
            initVmDisks();
        }
    }

    private void initVmSnapshots() {
        AsyncDataProvider.getInstance().getVmSnapshotList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                ArrayList<Snapshot> snapshots = (ArrayList<Snapshot>) returnValue;

                if (snapshotModel.showWarningForByVmSnapshotsValidation(snapshots)) {
                    UICommand closeCommand = getCancelCommand()
                            .setTitle(ConstantsManager.getInstance().getConstants().close());
                    snapshotModel.getCommands().add(closeCommand);
                    snapshotModel.stopProgress();
                }
                else {
                    snapshotModel.initVmDisks();
                }
            }
        }), vm.getId());
    }

    private void initVmDisks() {
        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                updateSnapshotDisks(disks);

                VmModelHelper.sendWarningForNonExportableDisks(snapshotModel, disks, VmModelHelper.WarningType.VM_SNAPSHOT);
                snapshotModel.getCommands().add(getOnSaveCommand());
                snapshotModel.getCommands().add(getCancelCommand());
                snapshotModel.stopProgress();
            }
        }), vm.getId());
    }

    private void updateSnapshotDisks(ArrayList<Disk> disks) {
        ArrayList<DiskImage> diskImages = Linq.toList(Linq.<DiskImage>filterNonSnapableDisks(disks));
        Collections.sort(diskImages, new DiskByDiskAliasComparator());
        getSnapshotDisks().setItems(diskImages);
    }

    public void updateVmConfiguration(final INewAsyncCallback onUpdateAsyncCallback) {
        Snapshot snapshot = getEntity();
        if (snapshot == null) {
            return;
        }

        AsyncDataProvider.getInstance().getVmConfigurationBySnapshot(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                Snapshot snapshot = snapshotModel.getEntity();
                VM vm = (VM) returnValue;

                if (vm != null && snapshot != null) {
                    snapshotModel.setVm(vm);
                    snapshotModel.setDisks(vm.getDiskList());
                    snapshotModel.setNics(vm.getInterfaces());
                    snapshotModel.setApps(Arrays.asList(snapshot.getAppList() != null ?
                            snapshot.getAppList().split(",") : new String[]{})); //$NON-NLS-1$

                    Collections.sort(snapshotModel.getDisks(), new DiskByDiskAliasComparator());
                    Collections.sort(snapshotModel.getNics(), new LexoNumericNameableComparator<>());
                }

                onUpdateAsyncCallback.onSuccess(snapshotModel, null);
            }
        }), snapshot.getId());
    }

    public DiskImage getImageByDiskId(Guid diskId) {
        for (DiskImage disk : getEntity().getDiskImages()) {
            if (disk.getId().equals(diskId)) {
                return disk;
            }
        }
        return null;
    }

    public boolean validate() {
        getDescription().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new SpecialAsciiI18NOrNoneValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE) });

        return getDescription().getIsValid();
    }

    private boolean showWarningForByVmSnapshotsValidation(ArrayList<Snapshot> snapshots) {
        for (Snapshot snapshot : snapshots) {
            if (!validateNewSnapshotByStatus(snapshot.getStatus()) || !validateNewSnapshotByType(snapshot.getType())) {
                getDescription().setIsAvailable(false);
                getMemory().setIsAvailable(false);
                return true;
            }
        }

        return false;
    }

    private boolean validateNewSnapshotByStatus(SnapshotStatus snapshotStatus) {
        switch (snapshotStatus) {
        case LOCKED:
            setMessage(ConstantsManager.getInstance().getConstants().snapshotCannotBeCreatedLockedSnapshotMsg());
            return false;
        case IN_PREVIEW:
            setMessage(ConstantsManager.getInstance().getConstants().snapshotCannotBeCreatedPreviewSnapshotMsg());
            return false;
        default:
            return true;
        }
    }

    private boolean validateNewSnapshotByType(SnapshotType snapshotType) {
        switch (snapshotType) {
        case STATELESS:
            setMessage(ConstantsManager.getInstance().getConstants().snapshotCannotBeCreatedStatelessSnapshotMsg());
            return false;
        case PREVIEW:
            setMessage(ConstantsManager.getInstance().getConstants().snapshotCannotBeCreatedPreviewSnapshotMsg());
            return false;
        default:
            return true;
        }
    }

    private UICommand getOnSaveCommand() {
        return UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
    }

    public void onSave() {
        if (getProgress() != null || !validate()) {
            return;
        }
        startProgress();

        VM vm = getVm();
        ArrayList<VdcActionParametersBase> params = new ArrayList<>();
        CreateAllSnapshotsFromVmParameters param =
                new CreateAllSnapshotsFromVmParameters(vm.getId(),
                        getDescription().getEntity(),
                        getMemory().getEntity(),
                        getSnapshotDisks().getSelectedItems());
        param.setQuotaId(vm.getQuotaId());
        params.add(param);

        Frontend.getInstance().runMultipleAction(VdcActionType.CreateAllSnapshotsFromVm, params,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        SnapshotModel localModel = (SnapshotModel) result.getState();
                        localModel.stopProgress();
                        getCancelCommand().execute();
                    }
                }, this);
    }

    public enum PreivewPartialSnapshotOption {
        preserveActiveDisks(ConstantsManager.getInstance().getConstants().preserveActiveDisks()),
        excludeActiveDisks(ConstantsManager.getInstance().getConstants().excludeActiveDisks()),
        openCustomPreviewDialog(ConstantsManager.getInstance().getConstants().openCustomPreviewDialog());

        private String description;

        private PreivewPartialSnapshotOption(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public boolean isVMWithMemoryCompatible() {
        return isVMWithMemoryCompatible(getVm());
    }

    public boolean isVMWithMemoryCompatible(VM vm) {
        if (vm == null || vm.getCustomCompatibilityVersion() != null) {
            return true;
        }

        Version recentClusterVersion = vm.getClusterCompatibilityVersion();
        // the cluster version in which the memory snapshot was taken
        Version originalClusterVersion = vm.getClusterCompatibilityVersionOrigin();

        return originalClusterVersion != null
               && recentClusterVersion.getMajor() == originalClusterVersion.getMajor()
               && recentClusterVersion.getMinor() == originalClusterVersion.getMinor();

    }
}
