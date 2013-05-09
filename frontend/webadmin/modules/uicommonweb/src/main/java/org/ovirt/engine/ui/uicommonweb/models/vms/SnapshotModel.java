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
            onPropertyChanged(new PropertyChangedEventArgs("VM")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("Nics")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("Apps")); //$NON-NLS-1$
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

    @Override
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
    public void initialize()
    {
        super.initialize();

        startProgress(null);
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
        AsyncDataProvider.getVmSnapshotList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                ArrayList<Snapshot> snapshots = (ArrayList<Snapshot>) returnValue;

                if (snapshotModel.showWarningForByVmSnapshotsValidation(snapshots)) {
                    snapshotModel.getCommands().add(getCloseCommand());
                    snapshotModel.stopProgress();
                }
                else {
                    snapshotModel.initVmDisks();
                }
            }
        }), vm.getId());
    }

    private void initVmDisks() {
        AsyncDataProvider.getVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;

                VmModelHelper.sendWarningForNonExportableDisks(snapshotModel, disks, VmModelHelper.WarningType.VM_SNAPSHOT);
                snapshotModel.getCommands().add(getOnSaveCommand());
                snapshotModel.getCommands().add(getCancelCommand());
                snapshotModel.stopProgress();
            }
        }), vm.getId());
    }

    public void updateVmConfiguration()
    {
        Snapshot snapshot = ((Snapshot) getEntity());

        AsyncDataProvider.getVmConfigurationBySnapshot(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
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

    public boolean validate()
    {
        getDescription().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new SpecialAsciiI18NOrNoneValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE) });

        return getDescription().getIsValid();
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
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) { //$NON-NLS-1$
            onSave();
        }
    }

    public void onSave() {
        if (getProgress() != null || !validate()) {
            return;
        }
        startProgress(null);

        VM vm = getVm();
        ArrayList<VdcActionParametersBase> params = new ArrayList<VdcActionParametersBase>();
        CreateAllSnapshotsFromVmParameters param =
                new CreateAllSnapshotsFromVmParameters(vm.getId(), (String) getDescription().getEntity());
        param.setQuotaId(vm.getQuotaId());
        params.add(param);

        Frontend.RunMultipleAction(VdcActionType.CreateAllSnapshotsFromVm, params,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        SnapshotModel localModel = (SnapshotModel) result.getState();
                        localModel.stopProgress();
                        getCancelCommand().execute();
                    }
                }, this);
    }

}
