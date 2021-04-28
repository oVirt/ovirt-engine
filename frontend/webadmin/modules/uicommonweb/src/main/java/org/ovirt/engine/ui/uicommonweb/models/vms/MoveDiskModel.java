package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;

public class MoveDiskModel extends MoveOrCopyDiskModel {
    protected List<String> problematicDisksForWarning = new ArrayList<>();

    public MoveDiskModel() {
        super();

        setIsSourceStorageDomainNameAvailable(true);
    }

    @Override
    public void init(ArrayList<DiskImage> diskImages) {
        setDiskImages(diskImages);
        // Currently only the copy operation is supported for managed block disks.
        setAllowedForManagedBlockDisks(false);

        AsyncDataProvider.getInstance().getDiskList(new AsyncQuery<>(list -> {
            onInitAllDisks(list);
            onInitDisks();
        }));
    }

    @Override
    protected void onInitDisks() {
        final ArrayList<DiskModel> disks = new ArrayList<>();
        List<QueryType> queries = new ArrayList<>();
        List<QueryParametersBase> params = new ArrayList<>();

        for (DiskImage disk : getDiskImages()) {
            disks.add(DiskModel.diskToModel(disk));
            queries.add(QueryType.GetVmsByDiskGuid);
            params.add(new IdQueryParameters(disk.getId()));
        }

        Frontend.getInstance().runMultipleQueries(queries, params, result -> {
            for (int i = 0; i < result.getReturnValues().size(); i++) {
                Map<Boolean, List<VM>> resultValue = result.getReturnValues().get(i).getReturnValue();
                disks.get(i).setPluggedToRunningVm(!isAllVmsDown(resultValue));
            }

            setDisks(disks);
            updateMoveWarning(disks);
            initStorageDomains();
        });

        getTargetStorageDomains().setIsAvailable(getDiskImages().size() > 1);
    }

    private void updateMoveWarning(ArrayList<DiskModel> disks) {
        List<String> liveMigrateDisks = new ArrayList<>();
        for (DiskModel diskModel : disks) {
            if (diskModel.isPluggedToRunningVm()) {
                liveMigrateDisks.add(diskModel.getAlias().getEntity());
            }
        }
        if (!liveMigrateDisks.isEmpty()) {
            setMessage(messages.moveDisksWhileVmRunning(String.join(", ", liveMigrateDisks))); //$NON-NLS-1$
        }
    }

    @Override
    protected void initStorageDomains() {
        Disk disk = getDisks().get(0).getDisk();
        if (disk.getDiskStorageType() != DiskStorageType.IMAGE) {
            return;
        }

        AsyncDataProvider.getInstance().getStorageDomainList(
                new AsyncQuery<>(this::onInitStorageDomains),
                ((DiskImage) disk).getStoragePoolId());
    }

    @Override
    protected void postInitStorageDomains() {
        super.postInitStorageDomains();

        // Add warning for raw/thin disks that reside on a file domain
        // and selected to be cold moved to a block domain (as it will cause
        // the disks to become cow-sparse instead of raw-sparse).
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

            diskModel.getStorageDomain().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateProblematicDisk(diskModel));
            updateProblematicDisk(diskModel);
        }
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
        } else {
            problematicDisksForWarning.remove(diskAlias);
        }

        if (!problematicDisksForWarning.isEmpty()) {
            getDynamicWarning().setEntity(messages.moveDisksConvertToCowWarning(
                    String.join(", ", problematicDisksForWarning))); //$NON-NLS-1$
            getDynamicWarning().setIsAvailable(true);
        } else {
            getDynamicWarning().setIsAvailable(false);
        }
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.MoveDisk;
    }

    @Override
    protected String getWarning(List<String> disks) {
        return messages.cannotMoveDisks(String.join(", ", disks)); //$NON-NLS-1$
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

        ArrayList<ActionParametersBase> parameters = getParameters();
        if (parameters.isEmpty()) {
            cancel();
            return;
        }

        Frontend.getInstance().runMultipleActions(getActionType(), parameters, result -> {
            MoveDiskModel localModel = (MoveDiskModel) result.getState();
            localModel.cancel();
        }, this);
    }

    @Override
    protected boolean allowedStorageDomain(List<StorageDomain> sourceActiveStorageDomains, DiskImage diskImage, DiskModel templateDisk, StorageDomain sd) {
        // can not move to the same storage domain
        if (sourceActiveStorageDomains.contains(sd)) {
            return false;
        }

        return super.allowedStorageDomain(sourceActiveStorageDomains, diskImage, templateDisk, sd);
    }


}
