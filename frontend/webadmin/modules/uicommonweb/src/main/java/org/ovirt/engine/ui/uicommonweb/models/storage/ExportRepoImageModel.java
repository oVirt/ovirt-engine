package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExportRepoImageModel extends ImportExportRepoImageBaseModel {

    public void init(List<DiskImage> diskImages) {
        setDiskImages(diskImages);
        updateStorageDomains(null);
    }

    public void setDiskImages(List<DiskImage> diskImages) {
        ArrayList<EntityModel> entities = new ArrayList<EntityModel>();
        for (DiskImage i : diskImages) {
            entities.add(new EntityModel(i));
        }
        setEntities(entities);
    }

    @Override
    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> storageDomains) {
        List<StorageDomain> availableStorageDomains = new ArrayList<StorageDomain>();

        // Take only GLANCE domains
        for (StorageDomain storageDomainItem : storageDomains) {
            if (storageDomainItem.getStorageType() == StorageType.GLANCE) {
                availableStorageDomains.add(storageDomainItem);
            }
        }

        // Sorting by name
        Collections.sort(availableStorageDomains, new NameableComparator());
        return availableStorageDomains;
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        startProgress(null);

        ArrayList<VdcActionParametersBase> actionParameters = new ArrayList<VdcActionParametersBase>();

        for (EntityModel entity : getEntities()) {
            DiskImage diskImage = (DiskImage) entity.getEntity();
            ExportRepoImageParameters exportParameters = new ExportRepoImageParameters(diskImage.getImageId());

            // source
            exportParameters.setStoragePoolId(diskImage.getStoragePoolId());
            exportParameters.setStorageDomainId(diskImage.getStorageIds().get(0));
            exportParameters.setImageGroupID(diskImage.getId());

            // destination
            exportParameters.setDestinationDomainId(((StorageDomain) getStorageDomain().getSelectedItem()).getId());

            actionParameters.add(exportParameters);
        }

        Frontend.RunMultipleAction(VdcActionType.ExportRepoImage, actionParameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        ImportExportRepoImageBaseModel model = (ImportExportRepoImageBaseModel) result.getState();
                        model.stopProgress();
                        model.cancel();
                    }
                }, this);
    }
}
