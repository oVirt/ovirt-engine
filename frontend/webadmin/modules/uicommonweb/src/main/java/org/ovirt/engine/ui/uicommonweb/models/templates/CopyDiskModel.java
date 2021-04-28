package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.MoveOrCopyDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

public class CopyDiskModel extends MoveOrCopyDiskModel {
    public CopyDiskModel() {
        super();

        setIsSourceStorageDomainAvailable(true);
    }

    @Override
    public void init(ArrayList<DiskImage> disksImages) {
        if (disksImages.size() > 0) {
            setIsAliasChangeable(!isTemplateDisk(disksImages.get(0)));
        }

        setDiskImages(disksImages);
        setAllowedForManagedBlockDisks(true);

        AsyncDataProvider.getInstance().getDiskList(new AsyncQuery<>(disks -> {
            onInitAllDisks((List) disks);
            onInitDisks();
        }));
    }

    @Override
    protected void onInitDisks() {
        ArrayList<DiskModel> disks = new ArrayList<>();
        for (DiskImage disk : getDiskImages()) {
            disks.add(DiskModel.diskToModel(disk));
        }
        setDisks(disks);
        initStorageDomains();
        getTargetStorageDomains().setIsAvailable(getDiskImages().size() > 1);
    }

    @Override
    protected void initStorageDomains() {
        Disk disk = getDisks().get(0).getDisk();
        if (disk.getDiskStorageType() != DiskStorageType.IMAGE &&
                disk.getDiskStorageType() != DiskStorageType.MANAGED_BLOCK_STORAGE) {
            return;
        }

        AsyncDataProvider.getInstance().getStorageDomainList(
                new AsyncQuery<>(this::onInitStorageDomains),
                ((DiskImage) disk).getStoragePoolId());
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.MoveOrCopyDisk;
    }

    @Override
    protected String getWarning(List<String> disks) {
        return messages.cannotCopyDisks(String.join(", ", disks)); //$NON-NLS-1$
    }

    @Override
    protected String getNoActiveSourceDomainMessage() {
        return constants.noActiveSourceStorageDomainAvailableMsg();
    }

    @Override
    protected String getNoActiveTargetDomainMessage() {
        return constants.diskExistsOnAllActiveStorageDomainsMsg();
    }

    @Override
    protected MoveOrCopyImageGroupParameters createParameters(Guid sourceStorageDomainGuid,
            Guid destStorageDomainGuid,
            DiskImage disk) {
        MoveOrCopyImageGroupParameters moveOrCopyImageGroupParameters = new MoveOrCopyImageGroupParameters(disk.getImageId(),
                sourceStorageDomainGuid,
                destStorageDomainGuid,
                ImageOperation.Copy);
        moveOrCopyImageGroupParameters.setImageGroupID(disk.getId());
        return moveOrCopyImageGroupParameters;
    }

    @Override
    protected void doExecute() {
        super.doExecute();

        ArrayList<ActionParametersBase> parameters = getParameters();
        if (parameters.isEmpty()) {
            cancel();
            return;
        }

        Frontend.getInstance().runMultipleAction(getActionType(), parameters,
                result -> {
                    CopyDiskModel localModel = (CopyDiskModel) result.getState();
                    localModel.cancel();
                }, this);
    }

    @Override
    protected boolean allowedStorageDomain(List<StorageDomain> sourceActiveStorageDomains, DiskImage diskImage, DiskModel templateDisk, StorageDomain sd) {
        // can not move template to the same storage domain
        boolean isTemplate = isTemplateDisk(diskImage);
        if (isTemplate && sourceActiveStorageDomains.contains(sd)) {
            return false;
        }

        return super.allowedStorageDomain(sourceActiveStorageDomains, diskImage, templateDisk, sd);
    }

    private boolean isTemplateDisk(DiskImage  diskImage) {
        return diskImage.getVmEntityType() != null && diskImage.getVmEntityType().isTemplateType();
    }

}
