package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImportRepoImageModel extends ImportExportRepoImageBaseModel {

    protected StorageDomain sourceStorageDomain;

    public void init(StorageDomain sourceStorageDomain, List<RepoImage> repoImages) {
        this.sourceStorageDomain = sourceStorageDomain;
        setRepoImages(repoImages);
        updateDataCenters();
        IEventListener<EventArgs> importAsTemplateListener = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateClusterEnabled();
            }
        };
        getImportAsTemplate().getEntityChangedEvent().addListener(importAsTemplateListener);
    }

    public void setRepoImages(List<RepoImage> repoImages) {
        ArrayList<EntityModel> entities = new ArrayList<EntityModel>();
        for (RepoImage i : repoImages) {
            entities.add(new EntityModel(i));
        }
        setEntities(entities);
    }

    @Override
    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> storageDomains) {
        List<StorageDomain> availableStorageDomains = new ArrayList<StorageDomain>();

        // Filtering out domains that are not active
        for (StorageDomain storageDomainItem : storageDomains) {
            if (Linq.isDataActiveStorageDomain(storageDomainItem)) {
                availableStorageDomains.add(storageDomainItem);
            }
        }

        // Sorting by name
        Collections.sort(availableStorageDomains, new NameableComparator());

        return availableStorageDomains;
    }

    @Override
    public boolean showImportAsTemplateOptions() {
        return true;
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        startProgress(null);

        ArrayList<VdcActionParametersBase> actionParameters = new ArrayList<VdcActionParametersBase>();

        for (EntityModel entity : getEntities()) {
            RepoImage repoImage = (RepoImage) entity.getEntity();
            ImportRepoImageParameters importParameters = new ImportRepoImageParameters();

            // source
            importParameters.setSourceRepoImageId(repoImage.getRepoImageId());
            importParameters.setSourceStorageDomainId(sourceStorageDomain.getId());

            // destination
            importParameters.setStoragePoolId(((StoragePool) getDataCenter().getSelectedItem()).getId());
            importParameters.setStorageDomainId(((StorageDomain) getStorageDomain().getSelectedItem()).getId());
            importParameters.setClusterId(((StorageDomain) getStorageDomain().getSelectedItem()).getId());

            Quota selectedQuota = (Quota) getQuota().getSelectedItem();

            if (selectedQuota != null) {
                importParameters.setQuotaId(selectedQuota.getId());
            }

            Boolean importAsTemplate = getImportAsTemplate().getEntity();
            importParameters.setImportAsTemplate(importAsTemplate);

            if (importAsTemplate) {
                importParameters.setClusterId(((VDSGroup) getCluster().getSelectedItem()).getId());
            }

            actionParameters.add(importParameters);
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.ImportRepoImage, actionParameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        ImportExportRepoImageBaseModel model = (ImportExportRepoImageBaseModel) result.getState();
                        model.stopProgress();
                        model.cancel();
                    }
                }, this);
    }

    public void updateClusterEnabled() {
        boolean importAsTemplate = getImportAsTemplate().getEntity();
        getCluster().setIsAvailable(importAsTemplate);
        getCluster().setIsChangable(!getCluster().getIsEmpty());
    }

}
