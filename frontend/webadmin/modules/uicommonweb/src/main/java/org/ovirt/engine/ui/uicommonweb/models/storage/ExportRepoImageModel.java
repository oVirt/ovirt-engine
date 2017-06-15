package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class ExportRepoImageModel extends ImportExportRepoImageBaseModel {

    public void init(List<DiskImage> diskImages) {
        setDiskImages(diskImages);
        updateStorageDomains(null);
    }

    public void setDiskImages(List<DiskImage> diskImages) {
        ArrayList<EntityModel> entities = new ArrayList<>();
        for (DiskImage i : diskImages) {
            entities.add(new EntityModel(i));
        }
        setEntities(entities);
    }

    @Override
    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> storageDomains) {
        List<StorageDomain> availableStorageDomains = new ArrayList<>();

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
    protected String getNoDomainAvailableMessage() {
        return constants.noExternalImageProviderHasBeenConfiguredMsg();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        startProgress();

        ArrayList<ActionParametersBase> actionParameters = new ArrayList<>();

        for (EntityModel entity : getEntities()) {
            actionParameters.add(new ExportRepoImageParameters(
                    ((DiskImage) entity.getEntity()).getId(),  // Source
                    getStorageDomain().getSelectedItem().getId())  // Destination
            );
        }

        Frontend.getInstance().runMultipleAction(ActionType.ExportRepoImage, actionParameters,
                result -> {
                    ImportExportRepoImageBaseModel model = (ImportExportRepoImageBaseModel) result.getState();
                    model.stopProgress();
                    model.cancel();
                }, this);
    }

    @Override
    public boolean isImportModel() {
        return false;
    }
}
