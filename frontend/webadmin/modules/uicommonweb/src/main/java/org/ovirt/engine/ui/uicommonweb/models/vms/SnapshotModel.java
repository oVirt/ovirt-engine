package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class SnapshotModel extends EntityModel
{
    private VM vm;

    public VM getVm()
    {
        return vm;
    }

    public void setVm(VM value)
    {
        if (vm != value)
        {
            vm = value;
            OnPropertyChanged(new PropertyChangedEventArgs("VM")); //$NON-NLS-1$
        }
    }

    private ArrayList<DiskImage> disks;

    public ArrayList<DiskImage> getDisks()
    {
        return disks;
    }

    public void setDisks(ArrayList<DiskImage> value)
    {
        if (disks != value)
        {
            disks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
    }

    private List<VmNetworkInterface> nics;

    public List<VmNetworkInterface> getNics()
    {
        return nics;
    }

    public void setNics(List<VmNetworkInterface> value)
    {
        if (nics != value)
        {
            nics = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Nics")); //$NON-NLS-1$
        }
    }

    private List<String> apps;

    public List<String> getApps()
    {
        return apps;
    }

    public void setApps(List<String> value)
    {
        if (apps != value)
        {
            apps = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Apps")); //$NON-NLS-1$
        }
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private EntityModel isPropertiesUpdated;

    public EntityModel getIsPropertiesUpdated()
    {
        return isPropertiesUpdated;
    }

    public void setIsPropertiesUpdated(EntityModel value)
    {
        isPropertiesUpdated = value;
    }

    private boolean validateByVmSnapshots;

    public boolean isValidateByVmSnapshots() {
        return validateByVmSnapshots;
    }

    public void setValidateByVmSnapshots(boolean validateByVmSnapshots) {
        this.validateByVmSnapshots = validateByVmSnapshots;
    }

    private UICommand cancelCommand;

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    private UICommand closeCommand;

    public void setCloseCommand(UICommand closeCommand) {
        this.closeCommand = closeCommand;
    }

    public UICommand getCloseCommand() {
        return closeCommand;
    }

    public SnapshotModel()
    {
        setDescription(new EntityModel());
        setDisks(new ArrayList<DiskImage>());
        setNics(new ArrayList<VmNetworkInterface>());
        setApps(new ArrayList<String>());

        setIsPropertiesUpdated(new EntityModel());
        getIsPropertiesUpdated().setEntity(false);
    }

    @Override
    public void Initialize()
    {
        super.Initialize();

        StartProgress(null);
        initMessages();
    }

    private void initMessages() {
        if (isValidateByVmSnapshots()) {
            initVmSnapshots();
        }
        else {
            initVmDisks();
        }
    }

    private void initVmSnapshots() {
        AsyncDataProvider.GetVmSnapshotList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                ArrayList<Snapshot> snapshots = (ArrayList<Snapshot>) returnValue;

                if (snapshotModel.showWarningForByVmSnapshotsValidation(snapshots)) {
                    snapshotModel.getCommands().add(getCloseCommand());
                    snapshotModel.StopProgress();
                }
                else {
                    snapshotModel.initVmDisks();
                }
            }
        }), vm.getId());
    }

    private void initVmDisks() {
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;

                snapshotModel.showWarningForNonExportableDisks(disks);
                snapshotModel.getCommands().add(getOnSaveCommand());
                snapshotModel.getCommands().add(getCancelCommand());
                snapshotModel.StopProgress();
            }
        }), vm.getId());
    }

    public void UpdateVmConfiguration()
    {
        Snapshot snapshot = ((Snapshot) getEntity());

        AsyncDataProvider.GetVmConfigurationBySnapshot(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                Snapshot snapshot = ((Snapshot) snapshotModel.getEntity());
                VM vm = (VM) returnValue;

                if (vm != null && snapshot != null) {
                    snapshotModel.setVm(vm);
                    snapshotModel.setDisks(vm.getDiskList());
                    snapshotModel.setNics(vm.getInterfaces());
                    snapshotModel.setApps(Arrays.asList(snapshot.getAppList() != null ?
                            snapshot.getAppList().split(",") : new String[] {})); //$NON-NLS-1$

                    snapshotModel.getIsPropertiesUpdated().setEntity(true);
                }
            }
        }), snapshot.getId());
    }

    public boolean Validate()
    {
        getDescription().ValidateEntity(new IValidation[] { new NotEmptyValidation(),
                new SpecialAsciiI18NOrNoneValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE) });

        return getDescription().getIsValid();
    }

    // send warning message, if a disk which doesn't allow snapshot is detected (e.g. LUN)
    private boolean showWarningForNonExportableDisks(ArrayList<Disk> disks) {
        // filter non-exportable disks
        final List<String> list = new ArrayList<String>();
        for (Disk disk : disks) {
            if (!disk.isAllowSnapshot()) {
                list.add(disk.getDiskAlias());
            }
        }

        if(!list.isEmpty()) {
            final String s = StringUtils.join(list, ", "); //$NON-NLS-1$
            // append warning message
            setMessage(ConstantsManager.getInstance().getMessages().disksWillNotBePartOfTheExportedVMSnapshot(s));
            return true;
        }

        return false;
    }

    private boolean showWarningForByVmSnapshotsValidation(ArrayList<Snapshot> snapshots) {
        for (Snapshot snapshot : snapshots) {
            if (!validateNewSnapshotByStatus(snapshot.getStatus()) || !validateNewSnapshotByType(snapshot.getType())) {
                getDescription().setIsAvailable(false);
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
        UICommand onSaveCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        onSaveCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        onSaveCommand.setIsDefault(true);

        return onSaveCommand;
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) { //$NON-NLS-1$
            onSave();
        }
    }

    public void onSave() {
        if (getProgress() != null || !Validate()) {
            return;
        }
        StartProgress(null);

        VM vm = getVm();
        ArrayList<VdcActionParametersBase> params = new ArrayList<VdcActionParametersBase>();
        CreateAllSnapshotsFromVmParameters param =
                new CreateAllSnapshotsFromVmParameters(vm.getId(), (String) getDescription().getEntity());
        param.setQuotaId(vm.getQuotaId());
        params.add(param);

        Frontend.RunMultipleAction(VdcActionType.CreateAllSnapshotsFromVm, params,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        SnapshotModel localModel = (SnapshotModel) result.getState();
                        localModel.StopProgress();
                        getCancelCommand().Execute();
                    }
                }, this);
    }

}
