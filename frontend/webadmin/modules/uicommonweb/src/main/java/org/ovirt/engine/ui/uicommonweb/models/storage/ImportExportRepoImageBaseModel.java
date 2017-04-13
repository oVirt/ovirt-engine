package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;


public abstract class ImportExportRepoImageBaseModel extends EntityModel implements ICommandTarget {

    protected static final EventDefinition selectedItemChangedEventDefinition;

    static {
        selectedItemChangedEventDefinition = new EventDefinition("SelectedItemChanged", ListModel.class); //$NON-NLS-1$
    }

    protected static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    protected List<EntityModel> entities;
    private EntityModel<Boolean> importAsTemplate;
    private EntityModel<String> templateName;

    public EntityModel<Boolean> getImportAsTemplate() {
        return importAsTemplate;
    }

    public void setImportAsTemplate(EntityModel<Boolean> importAsTemplate) {
        this.importAsTemplate = importAsTemplate;
    }

    private ListModel<StoragePool> dataCenter;
    private ListModel<Cluster> cluster;
    private ListModel<StorageDomain> storageDomain;
    private ListModel<Quota> quota;

    private UICommand okCommand;
    private UICommand cancelCommand;

    public List<EntityModel> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityModel> entities) {
        if (entities == this.entities) {
            return;
        }
        this.entities = entities;
        onPropertyChanged(new PropertyChangedEventArgs("ImportExportEntities")); //$NON-NLS-1$
    }

    public EntityModel<String> getTemplateName() {
        return templateName;
    }

    public void setTemplateName(EntityModel<String> templateName) {
        this.templateName = templateName;
    }

    public ListModel<StoragePool> getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> value) {
       dataCenter = value;
    }

    public ListModel<Cluster> getCluster() {
        return cluster;
    }

    public void setCluster(ListModel<Cluster> value) {
        cluster = value;
    }

    public ListModel<StorageDomain> getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(ListModel<StorageDomain> storageDomain) {
        this.storageDomain = storageDomain;
    }

    public ListModel<Quota> getQuota() {
        return quota;
    }

    public void setQuota(ListModel<Quota> quota) {
        this.quota = quota;
    }


    public UICommand getOkCommand() {
        return okCommand;
    }

    public void setOkCommand(UICommand okCommand) {
        this.okCommand = okCommand;
    }

    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public ImportExportRepoImageBaseModel() {
        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().setIsEmpty(true);
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        setCluster(new ListModel<Cluster>());
        getCluster().setIsEmpty(true);
        getCluster().getSelectedItemChangedEvent().addListener(this);

        setStorageDomain(new ListModel<StorageDomain>());
        getStorageDomain().setIsEmpty(true);
        getStorageDomain().getSelectedItemChangedEvent().addListener(this);

        setQuota(new ListModel<Quota>());
        getQuota().setIsEmpty(true);
        setImportAsTemplate(new EntityModel<Boolean>());
        getImportAsTemplate().setEntity(false);
        setTemplateName(new EntityModel<String>());
        getTemplateName().setEntity(""); //$NON-NLS-1$

        setOkCommand(new UICommand("Ok", this));  //$NON-NLS-1$
        getOkCommand().setTitle(constants.ok());
        getOkCommand().setIsDefault(true);
        getOkCommand().setIsExecutionAllowed(false);

        getCommands().add(getOkCommand());
    }

    protected void updateDataCenters() {
        AsyncCallback<List<StoragePool>> callback = dataCenters -> {
            // Sorting by name
            Collections.sort(dataCenters, new NameableComparator());

            getDataCenter().setItems(dataCenters);
            getDataCenter().setIsEmpty(dataCenters.isEmpty());
            updateControlsAvailability();

            stopProgress();
        };

        startProgress();
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(callback));
    }

    protected void updateControlsAvailability() {
        getDataCenter().setIsChangeable(!getDataCenter().getIsEmpty());
        getStorageDomain().setIsChangeable(!getStorageDomain().getIsEmpty());
        getCluster().setIsAvailable(getImportAsTemplate().getEntity());
        getCluster().setIsChangeable(!getCluster().getIsEmpty());
        getTemplateName().setIsAvailable(getImportAsTemplate().getEntity());
        getQuota().setIsChangeable(!getQuota().getIsEmpty());
        getOkCommand().setIsExecutionAllowed(!getStorageDomain().getIsEmpty());
        setMessage(getStorageDomain().getIsEmpty() ? getNoDomainAvailableMessage() : null);
    }

    protected abstract String getNoDomainAvailableMessage();

    protected abstract List<StorageDomain> filterStorageDomains(List<StorageDomain> storageDomains);

    protected void updateStorageDomains(Guid storagePoolId) {
        AsyncCallback<List<StorageDomain>> callback = returnValue -> {
            List<StorageDomain> storageDomains = filterStorageDomains(returnValue);
            getStorageDomain().setItems(storageDomains);
            getStorageDomain().setIsEmpty(storageDomains.isEmpty());
            updateControlsAvailability();
            stopProgress();
        };

        startProgress();

        if (storagePoolId != null) {
            AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(callback), storagePoolId);
        } else {
            AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(callback));
        }
    }

    protected void updateClusters(Guid storagePoolId) {
        AsyncCallback<List<Cluster>> callback = returnValue -> {
            List<Cluster> clusters = AsyncDataProvider.getInstance().filterClustersWithoutArchitecture(returnValue);
            getCluster().setItems(clusters);
            getCluster().setIsEmpty(clusters.isEmpty());
            updateControlsAvailability();
            stopProgress();
        };

        startProgress();

        if (storagePoolId != null) {
            AsyncDataProvider.getInstance().getClusterList(new AsyncQuery<>(callback), storagePoolId);
        } else {
            AsyncDataProvider.getInstance().getClusterList(new AsyncQuery<>(callback));
        }
    }

    protected QuotaEnforcementTypeEnum getDataCenterQuotaEnforcementType() {
        StoragePool dataCenter = getDataCenter().getSelectedItem();
        return (dataCenter == null) ? null : dataCenter.getQuotaEnforcementType();
    }

    private void updateQuotas() {
        StorageDomain storageDomain = getStorageDomain().getSelectedItem();

        if (getDataCenterQuotaEnforcementType() == QuotaEnforcementTypeEnum.DISABLED || storageDomain == null) {
            getQuota().setItems(null);
            getQuota().setIsEmpty(true);
            updateControlsAvailability();
            return;
        }

        startProgress();
        AsyncDataProvider.getInstance().getAllRelevantQuotasForStorageSorted(new AsyncQuery<>(
                quotas -> {
                    quotas = (quotas != null) ? quotas : new ArrayList<Quota>();

                    getQuota().setItems(quotas);
                    getQuota().setIsEmpty(quotas.isEmpty());
                    updateControlsAvailability();
                    stopProgress();
                }), storageDomain.getId(), null);
    }

    protected void cancel() {
        stopProgress();
        ((ListModel) getEntity()).setWindow(null);
    }

    protected void dataCenter_SelectedItemChanged() {
        updateStorageDomains(getDataCenter().getSelectedItem().getId());
        updateClusters(getDataCenter().getSelectedItem().getId());
    }

    protected void storageDomain_SelectedItemChanged() {
        updateQuotas();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(selectedItemChangedEventDefinition)) {
            if (sender == getDataCenter()) {
                dataCenter_SelectedItemChanged();
            }
            if (sender == getStorageDomain()) {
                storageDomain_SelectedItemChanged();
            }
        }
    }

    public abstract boolean isImportModel();

}
