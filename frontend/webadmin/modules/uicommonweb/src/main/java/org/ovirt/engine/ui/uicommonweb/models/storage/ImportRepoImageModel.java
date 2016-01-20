package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NExtraNameOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

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
        ArrayList<EntityModel> entities = new ArrayList<>();
        for (RepoImage i : repoImages) {
            entities.add(new RepoImageModel(i));
        }
        setEntities(entities);
    }

    @Override
    protected List<StorageDomain> filterStorageDomains(List<StorageDomain> storageDomains) {
        List<StorageDomain> availableStorageDomains = new ArrayList<>();

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
    protected String getNoDomainAvailableMessage() {
        return constants.noStorageDomainAvailableMsg();
    }

    @Override
    public boolean isImportModel() {
        return true;
    }

    @Override
    public void executeCommand(UICommand command) {
        if (!validate()) {
            return;
        }
        super.executeCommand(command);

        startProgress();

        ArrayList<VdcActionParametersBase> actionParameters = new ArrayList<>();

        for (EntityModel entity : getEntities()) {
            RepoImage repoImage = (RepoImage) entity.getEntity();
            ImportRepoImageParameters importParameters = new ImportRepoImageParameters();

            // source
            importParameters.setSourceRepoImageId(repoImage.getRepoImageId());
            importParameters.setSourceStorageDomainId(sourceStorageDomain.getId());

            // destination
            importParameters.setDiskAlias(((RepoImageModel) entity).getDiskImageAlias());
            importParameters.setStoragePoolId(getDataCenter().getSelectedItem().getId());
            importParameters.setStorageDomainId(getStorageDomain().getSelectedItem().getId());
            importParameters.setClusterId(getStorageDomain().getSelectedItem().getId());

            Quota selectedQuota = getQuota().getSelectedItem();

            if (selectedQuota != null) {
                importParameters.setQuotaId(selectedQuota.getId());
            }

            Boolean importAsTemplate = getImportAsTemplate().getEntity();
            importParameters.setImportAsTemplate(importAsTemplate);

            if (importAsTemplate) {
                importParameters.setClusterId(getCluster().getSelectedItem().getId());
                String templateName = getTemplateName().getEntity();
                if (StringUtils.isNotEmpty(templateName)) {
                    importParameters.setTemplateName(templateName);
                }
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

    public boolean validate() {
        getTemplateName().validateEntity(
                new IValidation[] {
                        new LengthValidation(BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE),
                        new I18NExtraNameOrNoneValidation()
                });
        return getTemplateName().getIsValid();
    }

    public void updateClusterEnabled() {
        boolean importAsTemplate = getImportAsTemplate().getEntity();
        getCluster().setIsAvailable(importAsTemplate);
        getCluster().setIsChangeable(!getCluster().getIsEmpty());
        getTemplateName().setIsAvailable(importAsTemplate);
    }

}
