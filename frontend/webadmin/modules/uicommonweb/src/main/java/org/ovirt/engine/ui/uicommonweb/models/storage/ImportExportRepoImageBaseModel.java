package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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

import java.util.Collections;
import java.util.List;


public abstract class ImportExportRepoImageBaseModel extends EntityModel implements ICommandTarget {

    protected static EventDefinition selectedItemChangedEventDefinition;

    static {
        selectedItemChangedEventDefinition = new EventDefinition("SelectedItemChanged", ListModel.class); //$NON-NLS-1$
    }

    protected static UIConstants constants = ConstantsManager.getInstance().getConstants();

    protected List<EntityModel> entities;

    private ListModel dataCenter;
    private ListModel storageDomain;
    private ListModel quota;

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

    public ListModel getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(ListModel value)
    {
       dataCenter = value;
    }

    public ListModel getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(ListModel storageDomain) {
        this.storageDomain = storageDomain;
    }

    public ListModel getQuota() {
        return quota;
    }

    public void setQuota(ListModel quota) {
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
        setDataCenter(new ListModel());
        getDataCenter().setIsEmpty(true);
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        setStorageDomain(new ListModel());
        getStorageDomain().setIsEmpty(true);
        getStorageDomain().getSelectedItemChangedEvent().addListener(this);

        setQuota(new ListModel());
        getQuota().setIsEmpty(true);

        setOkCommand(new UICommand("Ok", this));  //$NON-NLS-1$
        getOkCommand().setTitle(constants.ok());
        getOkCommand().setIsDefault(true);
        getOkCommand().setIsExecutionAllowed(false);

        getCommands().add(getOkCommand());
    }

    protected void updateDataCenters() {
        INewAsyncCallback callback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ImportExportRepoImageBaseModel model = (ImportExportRepoImageBaseModel) target;
                List<StoragePool> dataCenters = (List<StoragePool>) returnValue;

                // Sorting by name
                Collections.sort(dataCenters, new NameableComparator());

                model.getDataCenter().setItems(dataCenters);
                model.getDataCenter().setIsEmpty(dataCenters.isEmpty());
                model.updateControlsAvailability();

                stopProgress();
            }
        };

        startProgress(null);
        AsyncDataProvider.getDataCenterList(new AsyncQuery(this, callback));
    }

    protected void updateControlsAvailability() {
        getDataCenter().setIsChangable(!getDataCenter().getIsEmpty());
        getStorageDomain().setIsChangable(!getStorageDomain().getIsEmpty());
        getQuota().setIsChangable(!getQuota().getIsEmpty());
        getOkCommand().setIsExecutionAllowed(!getStorageDomain().getIsEmpty());
        setMessage(getStorageDomain().getIsEmpty() ? constants.noStorageDomainAvailableMsg() : null);
    }

    protected abstract List<StorageDomain> filterStorageDomains(List<StorageDomain> storageDomains);

    protected void updateStorageDomains(Guid storagePoolId) {
        INewAsyncCallback callback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ImportExportRepoImageBaseModel model = (ImportExportRepoImageBaseModel) target;
                List<StorageDomain> storageDomains = model.filterStorageDomains((List<StorageDomain>) returnValue);
                model.getStorageDomain().setItems(storageDomains);
                model.getStorageDomain().setIsEmpty(storageDomains.isEmpty());
                model.updateControlsAvailability();
                stopProgress();
            }
        };

        startProgress(null);

        if (storagePoolId != null) {
            AsyncDataProvider.getStorageDomainList(new AsyncQuery(this, callback), storagePoolId);
        } else {
            AsyncDataProvider.getStorageDomainList(new AsyncQuery(this, callback));
        }
    }

    protected QuotaEnforcementTypeEnum getDataCenterQuotaEnforcementType() {
        StoragePool dataCenter = (StoragePool) getDataCenter().getSelectedItem();
        return (dataCenter == null) ? null : dataCenter.getQuotaEnforcementType();
    }

    private void updateQuotas() {
        StorageDomain storageDomain = (StorageDomain) getStorageDomain().getSelectedItem();

        if (getDataCenterQuotaEnforcementType() == QuotaEnforcementTypeEnum.DISABLED || storageDomain == null) {
            getQuota().setItems(null);
            getQuota().setIsEmpty(true);
            updateControlsAvailability();
            return;
        }

        INewAsyncCallback callback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ImportExportRepoImageBaseModel model = (ImportExportRepoImageBaseModel) target;
                List<Quota> quotas = (List<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                model.getQuota().setItems(quotas);
                model.getQuota().setIsEmpty(quotas.isEmpty());
                model.updateControlsAvailability();
                stopProgress();
            }
        };

        startProgress(null);
        Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                new IdQueryParameters(storageDomain.getId()),
                new AsyncQuery(this, callback));
    }

    protected void cancel() {
        stopProgress();
        ((ListModel) getEntity()).setWindow(null);
    }

    protected void dataCenter_SelectedItemChanged() {
        updateStorageDomains(((StoragePool) getDataCenter().getSelectedItem()).getId());
    }

    protected void storageDomain_SelectedItemChanged() {
        updateQuotas();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
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
}
