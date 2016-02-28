package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveDisksParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class MoveDiskModel extends MoveOrCopyDiskModel {
    protected List<String> problematicDisksForWarning = new ArrayList<>();

    public MoveDiskModel() {
        super();

        setIsSourceStorageDomainNameAvailable(true);
    }

    @Override
    public void init(ArrayList<DiskImage> diskImages) {
        setDiskImages(diskImages);

        AsyncDataProvider.getInstance().getDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                MoveDiskModel moveDiskModel = (MoveDiskModel) target;
                ArrayList<Disk> diskImages = (ArrayList<Disk>) returnValue;

                moveDiskModel.onInitAllDisks(diskImages);
                moveDiskModel.onInitDisks();
            }
        }));
    }

    @Override
    protected void onInitDisks() {
        final ArrayList<DiskModel> disks = new ArrayList<>();
        List<VdcQueryType> queries = new ArrayList<>();
        List<VdcQueryParametersBase> params = new ArrayList<>();

        for (DiskImage disk : getDiskImages()) {
            disks.add(Linq.diskToModel(disk));
            queries.add(VdcQueryType.GetVmsByDiskGuid);
            params.add(new IdQueryParameters(disk.getId()));
        }

        Frontend.getInstance().runMultipleQueries(queries, params, new IFrontendMultipleQueryAsyncCallback() {
            @Override
            public void executed(FrontendMultipleQueryAsyncResult result) {
                for (int i = 0; i < result.getReturnValues().size(); i++) {
                    Map<Boolean, List<VM>> resultValue = result.getReturnValues().get(i).getReturnValue();
                    disks.get(i).setPluggedToRunningVm(!isAllVmsDown(resultValue));
                }

                setDisks(disks);
                updateMoveWarning(disks);
                initStorageDomains();
            }
        });
    }

    private void updateMoveWarning(ArrayList<DiskModel> disks) {
        List<String> liveMigrateDisks = new ArrayList<>();
        for (DiskModel diskModel : disks) {
            if (diskModel.isPluggedToRunningVm()) {
                liveMigrateDisks.add(diskModel.getAlias().getEntity());
            }
        }
        if (!liveMigrateDisks.isEmpty()) {
            setMessage(messages.moveDisksWhileVmRunning(StringUtils.join(liveMigrateDisks, ", "))); //$NON-NLS-1$
        }
    }

    @Override
    protected void initStorageDomains() {
        Disk disk = getDisks().get(0).getDisk();
        if (disk.getDiskStorageType() != DiskStorageType.IMAGE) {
            return;
        }

        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                MoveDiskModel moveDiskModel = (MoveDiskModel) target;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                moveDiskModel.onInitStorageDomains(storageDomains);
            }
        }), ((DiskImage) disk).getStoragePoolId());
    }

    @Override
    protected void postInitStorageDomains() {
        super.postInitStorageDomains();

        // Add warning for raw/thin disks that reside on a file domain
        // and selected to be cold moved to a block domain (as it will cause
        // the disks to become preallocated, and it may consume considerably
        // more space on the target domain).
        for (final DiskModel diskModel : getDisks()) {
            ListModel<StorageDomain> sourceStorageDomains = diskModel.getSourceStorageDomain();
            if (sourceStorageDomains.getItems().iterator().hasNext() &&
                    !sourceStorageDomains.getItems().iterator().next().getStorageType().isFileDomain()) {
                continue;
            }

            DiskImage diskImage = (DiskImage) diskModel.getDisk();
            if (diskImage.getVolumeType() != VolumeType.Sparse || diskImage.getVolumeFormat() != VolumeFormat.RAW) {
                continue;
            }

            diskModel.getStorageDomain().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    updateProblematicDisk(diskModel);
                }
            });
            updateProblematicDisk(diskModel);
        }

        setMessage(ConstantsManager.getInstance().getConstants().liveStorageMigrationStorageFilteringNote());
    }

    private void updateProblematicDisk(DiskModel diskModel) {
        StorageDomain storageDomain = diskModel.getStorageDomain().getSelectedItem();
        if (storageDomain == null) {
            return;
        }

        String diskAlias = diskModel.getDisk().getDiskAlias();
        if (storageDomain.getStorageType().isBlockDomain()) {
            if (!problematicDisksForWarning.contains(diskAlias)) {
                problematicDisksForWarning.add(diskAlias);
            }
        }
        else {
            problematicDisksForWarning.remove(diskAlias);
        }

        if (!problematicDisksForWarning.isEmpty()) {
            getDynamicWarning().setEntity(messages.moveDisksPreallocatedWarning(
                    StringHelper.join(", ", problematicDisksForWarning.toArray()))); //$NON-NLS-1$
            getDynamicWarning().setIsAvailable(true);
        } else {
            getDynamicWarning().setIsAvailable(false);
        }
    }

    @Override
    protected VdcActionType getActionType() {
        return VdcActionType.MoveDisks;
    }

    @Override
    protected String getWarning(List<String> disks) {
        return messages.cannotMoveDisks(StringHelper.join(", ", disks.toArray())); //$NON-NLS-1$
    }

    @Override
    protected String getNoActiveSourceDomainMessage() {
        return constants.sourceStorageDomainIsNotActiveMsg();
    }

    @Override
    protected String getNoActiveTargetDomainMessage() {
        return constants.noActiveTargetStorageDomainAvailableMsg();
    }

    @Override
    protected MoveOrCopyImageGroupParameters createParameters(Guid sourceStorageDomainGuid,
            Guid destStorageDomainGuid,
            DiskImage disk) {
        MoveDiskParameters moveDiskParameters = new MoveDiskParameters(disk.getImageId(),
                sourceStorageDomainGuid,
                destStorageDomainGuid);
        moveDiskParameters.setImageGroupID(disk.getId());
        return moveDiskParameters;
    }

    @Override
    protected void doExecute() {
        super.doExecute();

        ArrayList<VdcActionParametersBase> parameters = getParameters();
        if (parameters.isEmpty()) {
            cancel();
            return;
        }

        MoveDisksParameters moveDisksParameters = new MoveDisksParameters((List) parameters);
        Frontend.getInstance().runAction(getActionType(), moveDisksParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        MoveDiskModel localModel = (MoveDiskModel) result.getState();
                        localModel.cancel();
                    }
                }, this);
    }

    @Override
    protected boolean allowedStorageDomain(ArrayList<StorageDomain> sourceActiveStorageDomains, DiskImage diskImage, DiskModel templateDisk, StorageDomain sd) {
        // can not move to the same storage domain
        if (sourceActiveStorageDomains.contains(sd)) {
            return false;
        }

        return super.allowedStorageDomain(sourceActiveStorageDomains, diskImage, templateDisk, sd);
    }


}
