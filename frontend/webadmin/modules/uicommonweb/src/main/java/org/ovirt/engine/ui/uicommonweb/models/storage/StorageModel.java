package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class StorageModel extends Model implements ISupportSystemTreeContext {
    public static final Guid UnassignedDataCenterId = Guid.Empty;

    private StorageModelBehavior behavior;

    private String localFSPath;

    /**
     * Gets or sets the storage being edited. Null if it's a new one.
     */
    private StorageDomain storageDomain;

    public StorageDomain getStorage() {
        return storageDomain;
    }

    public void setStorage(StorageDomain value) {
        storageDomain = value;
    }

    private IStorageModel currentStorageItem;

    public IStorageModel getCurrentStorageItem() {
        return currentStorageItem;
    }

    public void setCurrentStorageItem(IStorageModel storageModel) {
        currentStorageItem = storageModel;
    }

    public ArrayList<IStorageModel> updatedStorageModels = new ArrayList<>();

    public List<IStorageModel> storageModels = new ArrayList<>();

    public List<IStorageModel> getStorageModels() {
        return storageModels;
    }

    public void setStorageModels(List<IStorageModel> storageModels) {
        this.storageModels = storageModels;
    }

    private String originalName;

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String value) {
        originalName = value;
    }

    private EntityModel<String> name;

    public EntityModel<String> getName() {
        return name;
    }

    private void setName(EntityModel<String> value) {
        name = value;
    }

    private EntityModel<String> description;

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    private EntityModel<String> comment;

    public EntityModel<String> getComment() {
        return comment;
    }

    public void setComment(EntityModel<String> value) {
        comment = value;
    }

    private ListModel<StoragePool> dataCenter;

    public ListModel<StoragePool> getDataCenter() {
        return dataCenter;
    }

    private void setDataCenter(ListModel<StoragePool> value) {
        dataCenter = value;
    }

    private EntityModel<String> dataCenterAlert;

    public EntityModel<String> getDataCenterAlert() {
        return dataCenterAlert;
    }

    public void setDataCenterAlert(EntityModel<String> dataCenterAlert) {
        this.dataCenterAlert = dataCenterAlert;
    }

    private ListModel<VDS> host;

    public ListModel<VDS> getHost() {
        return host;
    }

    public void setHost(ListModel<VDS> value) {
        host = value;
    }

    private ListModel<StorageFormatType> format;

    public ListModel<StorageFormatType> getFormat() {
        return format;
    }

    private void setFormat(ListModel<StorageFormatType> value) {
        format = value;
    }

    private ListModel<StorageType> availableStorageTypeItems;

    public ListModel<StorageType> getAvailableStorageTypeItems() {
        return availableStorageTypeItems;
    }

    private void setAvailableStorageTypeItems(ListModel<StorageType> value) {
        availableStorageTypeItems = value;
    }

    private ListModel<StorageDomainType> availableStorageDomainTypeItems;

    public ListModel<StorageDomainType> getAvailableStorageDomainTypeItems() {
        return availableStorageDomainTypeItems;
    }

    private void setAvailableStorageDomainTypeItems(ListModel<StorageDomainType> value) {
        availableStorageDomainTypeItems = value;
    }

    private EntityModel<Integer> warningLowSpaceIndicator;

    public EntityModel<Integer> getWarningLowSpaceIndicator() {
        return warningLowSpaceIndicator;
    }

    public void setWarningLowSpaceIndicator(EntityModel<Integer> warningLowSpaceIndicator) {
        this.warningLowSpaceIndicator = warningLowSpaceIndicator;
    }

    private EntityModel<String> warningLowSpaceSize;

    public EntityModel<String> getWarningLowSpaceSize() {
        return warningLowSpaceSize;
    }

    public void setWarningLowSpaceSize(EntityModel<String> warningLowSpaceSize) {
        this.warningLowSpaceSize = warningLowSpaceSize;
    }

    private EntityModel<Integer> criticalSpaceActionBlocker;

    public EntityModel<Integer> getCriticalSpaceActionBlocker() {
        return criticalSpaceActionBlocker;
    }

    public void setCriticalSpaceActionBlocker(EntityModel<Integer> criticalSpaceActionBlocker) {
        this.criticalSpaceActionBlocker = criticalSpaceActionBlocker;
    }

    private EntityModel<Boolean> activateDomain;

    public EntityModel<Boolean> getActivateDomain() {
        return activateDomain;
    }

    public void setActivateDomain(EntityModel<Boolean> activateDomain) {
        this.activateDomain = activateDomain;
    }

    private EntityModel<Boolean> wipeAfterDelete;

    public EntityModel<Boolean> getWipeAfterDelete() {
        return wipeAfterDelete;
    }

    public void setWipeAfterDelete(EntityModel<Boolean> wipeAfterDelete) {
        this.wipeAfterDelete = wipeAfterDelete;
    }

    public StorageModel(StorageModelBehavior behavior) {
        this.behavior = behavior;
        this.behavior.setModel(this);

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setDataCenterAlert(new EntityModel<String>());
        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        setHost(new ListModel<VDS>());
        getHost().getSelectedItemChangedEvent().addListener(this);
        setFormat(new ListModel<StorageFormatType>());
        setAvailableStorageTypeItems(new ListModel<StorageType>());
        getAvailableStorageTypeItems().getSelectedItemChangedEvent().addListener(this);
        getAvailableStorageTypeItems().getItemsChangedEvent().addListener(this);
        setAvailableStorageDomainTypeItems(new ListModel<StorageDomainType>());
        getAvailableStorageDomainTypeItems().getSelectedItemChangedEvent().addListener(this);
        setWarningLowSpaceIndicator(new EntityModel<Integer>());
        getWarningLowSpaceIndicator().setEntity(getWarningLowSpaceIndicatorValue());
        setWarningLowSpaceSize(new EntityModel<String>());
        getWarningLowSpaceSize().setIsAvailable(false);
        setCriticalSpaceActionBlocker(new EntityModel<Integer>());
        getCriticalSpaceActionBlocker().setEntity(getCriticalSpaceThresholdValue());
        setActivateDomain(new EntityModel<>(true));
        getActivateDomain().setIsAvailable(false);
        setWipeAfterDelete(new EntityModel<>(false));

        localFSPath = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.RhevhLocalFSPath);
    }

    @Override
    public void initialize() {
        super.initialize();

        behavior.initialize();

        initDataCenter();
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
            if (sender == getDataCenter()) {
                dataCenter_SelectedItemChanged();
            }
            else if (sender == getHost()) {
                host_SelectedItemChanged();
            }
            else if (sender == getAvailableStorageTypeItems()) {
                storageType_SelectedItemChanged();
            }
            else if (sender == getAvailableStorageDomainTypeItems()) {
                behavior.setStorageTypeItems();
            }
        }
        else if (ev.matchesDefinition(ListModel.itemsChangedEventDefinition)) {
            if (sender == getAvailableStorageTypeItems()) {
                storageItemsChanged();
            }
        }
        else if (ev.matchesDefinition(NfsStorageModel.pathChangedEventDefinition)) {
            nfsStorageModel_PathChanged(sender);
        }
    }

    private void nfsStorageModel_PathChanged(Object sender) {
        NfsStorageModel senderModel = (NfsStorageModel) sender;

        for (Object item : getStorageModels()) {
            if (item instanceof NfsStorageModel && item != sender) {
                NfsStorageModel model = (NfsStorageModel) item;
                model.getPath().setEntity(senderModel.getPath().getEntity());
            }
        }
    }

    protected void storageType_SelectedItemChanged() {
        updateCurrentStorageItem();
        if (getCurrentStorageItem() != null) {
            updateFormat();
            updateHost();
        }
        updateWipeAfterDelete();
    }

    protected void storageItemsChanged() {
        if (getStorageModels() != null) {
            for (Object item : getStorageModels()) {
                IStorageModel model = (IStorageModel) item;
                model.setContainer(this);

                if (item instanceof NfsStorageModel) {
                    NfsStorageModel nfsModel = (NfsStorageModel) item;
                    nfsModel.getPathChangedEvent().addListener(this);
                }
            }
        }
    }

    private void dataCenter_SelectedItemChanged() {
        updateItemsAvailability();
        behavior.updateDataCenterAlert();
    }

    private void host_SelectedItemChanged() {
        VDS host = getHost().getSelectedItem();
        if (getCurrentStorageItem() != null) {
            // When changing host clear items for san storage model.
            if (getCurrentStorageItem() instanceof SanStorageModelBase) {
                SanStorageModelBase sanStorageModel = (SanStorageModelBase) getCurrentStorageItem();
                if (getStorage() == null) {
                    sanStorageModel.setItems(null);
                }
            }

            if (host != null) {
                getCurrentStorageItem().getUpdateCommand().execute();

                String prefix = host.isOvirtVintageNode() ? localFSPath : ""; //$NON-NLS-1$
                if (!StringHelper.isNullOrEmpty(prefix)) {
                    for (Object item : getStorageModels()) {
                        if (item instanceof LocalStorageModel) {
                            LocalStorageModel model = (LocalStorageModel) item;
                            model.getPath().setEntity(prefix);
                            model.getPath().setIsChangeable(false);
                        }
                    }
                }
            }
        }
    }

    private void initDataCenter() {
        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System) {
            SystemTreeItemModel dataCenterItem;
            StoragePool dc;
            switch (getSystemTreeSelectedItem().getType()) {
            case DataCenter:
            case Cluster:
            case Storages:
            case Storage:
                dataCenterItem =
                        SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
                dc = (StoragePool) dataCenterItem.getEntity();

                getDataCenter().setItems(new ArrayList<>(Arrays.asList(new StoragePool[]{dc})));
                getDataCenter().setSelectedItem(dc);
                getDataCenter().setIsChangeable(false);
                getDataCenter().setChangeProhibitionReason(constants.cannotChangeDCInTreeContext());
                break;

            case Host:
                VDS host = (VDS) getSystemTreeSelectedItem().getEntity();

                getHost().setIsChangeable(false);
                getHost().setChangeProhibitionReason(constants.cannotChangeHostInTreeContext());
                getHost().setSelectedItem(host);

                dataCenterItem =
                        SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
                dc = (StoragePool) dataCenterItem.getEntity();

                getDataCenter().setItems(new ArrayList<>(Arrays.asList(new StoragePool[]{dc})));
                getDataCenter().setSelectedItem(dc);
                getDataCenter().setIsChangeable(false);
                getDataCenter().setChangeProhibitionReason(constants.cannotChangeDCInTreeContext());
                break;
            }
        }
        else {
            if (getStorage() == null
                    || getStorage().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached) {
            // We are either adding a new storage or editing an unattached storage
            // -> fill DataCenters drop-down with all possible Data-Centers, choose the empty one:
            // [TODO: In case of an Unattached SD, choose only DCs of the same type]
                AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this,
                                                                                 new INewAsyncCallback() {
                                                                                     @Override
                                                                                     public void onSuccess(Object target, Object returnValue) {

                                                                                         StorageModel storageModel = (StorageModel) target;
                                                                                         StorageModelBehavior storageModelBehavior = behavior;
                                                                                         List<StoragePool> dataCenters =
                                                                                                 (ArrayList<StoragePool>) returnValue;
                                                                                         dataCenters = storageModelBehavior.filterDataCenter(dataCenters);
                                                                                         StorageModel.addEmptyDataCenterToList(dataCenters);
                                                                                         StoragePool oldSelectedItem =
                                                                                                 storageModel.getDataCenter().getSelectedItem();
                                                                                         storageModel.getDataCenter().setItems(dataCenters);
                                                                                         if (oldSelectedItem != null) {
                                                                                             storageModel.getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters,
                                                                                                                                                              new Linq.IdPredicate<>(oldSelectedItem.getId())));
                                                                                         } else {
                                                                                             storageModel.getDataCenter()
                                                                                                     .setSelectedItem(getStorage() == null ? Linq.firstOrNull(dataCenters)
                                                                                                                              : Linq.firstOrNull(dataCenters,
                                                                                                                                                    new Linq.IdPredicate<>(UnassignedDataCenterId)));
                                                                                         }

                                                                                     }
                                                                                 }));
            }

            else { // "Edit Storage" mode:
                AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                StorageModel storageModel = (StorageModel) target;
                                List<StoragePool> dataCenters = new ArrayList<>();
                                List<StoragePool> dataCentersWithStorage =
                                        (ArrayList<StoragePool>) returnValue;
                                if (dataCentersWithStorage.size() < 1 || dataCentersWithStorage.get(0) == null) {
                                    StorageModel.addEmptyDataCenterToList(dataCenters);
                                }
                                else {
                                    dataCenters =
                                            new ArrayList<>(Arrays.asList(new StoragePool[]{dataCentersWithStorage.get(0)}));
                                }
                                storageModel.getDataCenter().setItems(dataCenters);
                                storageModel.getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters));

                            }
                        }),
                        getStorage().getId());
            }
        }
    }

    private static void addEmptyDataCenterToList(List<StoragePool> dataCenters) {
        StoragePool tempVar = new StoragePool();
        tempVar.setId(UnassignedDataCenterId);
        tempVar.setName("(none)"); //$NON-NLS-1$
        dataCenters.add(tempVar);
    }

    void updateHost() {
        if (getDataCenter().getItems() == null) {
            return;
        }

        if (getCurrentStorageItem() == null) {
            return;
        }

        StoragePool dataCenter = getDataCenter().getSelectedItem();

        boolean localFsOnly = getCurrentStorageItem() instanceof LocalStorageModel;
        Guid dataCenterId = dataCenter == null ? null : dataCenter.getId();

        AsyncDataProvider.getInstance().getHostsForStorageOperation(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                StorageModel storageModel = (StorageModel) model;
                Collection<VDS> hosts = (Collection<VDS>) returnValue;
                storageModel.postUpdateHost(hosts);
            }
        }), dataCenterId, localFsOnly);
    }

    public void postUpdateHost(Collection<VDS> hosts) {
        // Filter hosts
        hosts = Linq.where(hosts, new Linq.HostStatusPredicate(VDSStatus.Up));

        VDS oldSelectedItem = getHost().getSelectedItem();
        VDS selectedItem = null;

        // On Edit of active storage - only SPM is available. In edit of storage in maintenance,
        //any host can perform the operation, thus no need to filter to use just the SPM
        if (getStorage() != null && getStorage().getStatus() != StorageDomainStatus.Maintenance) {
            VDS spm = getSPM(hosts);
            hosts = spm != null ? Collections.singletonList(spm) : Collections.<VDS> emptyList();
        }

        // Try to select previously selected host.
        if (oldSelectedItem != null) {
            selectedItem = Linq.firstOrNull(hosts, new Linq.IdPredicate<>(oldSelectedItem.getId()));
        }

        // Select a default - if there's a SPM choose it, otherwise choose the first host in the list.
        if (selectedItem == null) {
            VDS spm = getSPM(hosts);
            selectedItem = spm == null ? Linq.firstOrNull(hosts) : spm;
        }

        getHost().setItems(hosts, selectedItem);
    }

    private VDS getSPM(Iterable<VDS> hosts) {
        for (VDS host : hosts) {
            if (host.getSpmStatus() == VdsSpmStatus.SPM) {
                return host;
            }
        }

        return null;
    }

    void updateFormat() {
        StoragePool dataCenter = getDataCenter().getSelectedItem();

        StorageFormatType selectItem = StorageFormatType.V1;

        ArrayList<StorageFormatType> formats = new ArrayList<>();

        if (dataCenter != null && getCurrentStorageItem() != null) {
            if (!dataCenter.getId().equals(UnassignedDataCenterId)) {
                getFormat().setIsChangeable(false);

                // If data center has format defined and the selected-item role is Data, choose it.
                if (dataCenter.getStoragePoolFormatType() != null
                        && getCurrentStorageItem().getRole().isDataDomain()) {
                    formats.add(dataCenter.getStoragePoolFormatType());
                    selectItem = dataCenter.getStoragePoolFormatType();
                }
                // If selected-item role is ISO or Export, add only the 'V1' option.
                // (*** Note that currently both ISO and Export can be only NFS, so theoretically they are covered by
                // the next "else if..." condition; however, just in case we will support non-NFS ISO/Export in the
                // future
                // and in order to make the code more explicit, it is here. ***)
                else if (getCurrentStorageItem().getRole() == StorageDomainType.ISO
                        || getCurrentStorageItem().getRole() == StorageDomainType.ImportExport) {
                    formats.add(StorageFormatType.V1);
                }
                else {
                    formats.add(StorageFormatType.V3);
                    selectItem = StorageFormatType.V3;
                }
            }
            else { // Unassigned DC:
                if (getCurrentStorageItem().getRole() == StorageDomainType.ISO
                        || getCurrentStorageItem().getRole() == StorageDomainType.ImportExport) {
                    // ISO/Export domains should not be available for '(none)' DC
                    return;
                }

                getFormat().setIsChangeable(true);

                if (getCurrentStorageItem().getType() != StorageType.POSIXFS && getCurrentStorageItem().getType() != StorageType.GLUSTERFS) {
                    formats.add(StorageFormatType.V1);
                }

                if ((getCurrentStorageItem().getType() == StorageType.FCP || getCurrentStorageItem().getType() == StorageType.ISCSI)
                        && getCurrentStorageItem().getRole() == StorageDomainType.Data) {
                    formats.add(StorageFormatType.V2);
                }

                formats.add(StorageFormatType.V3);
                selectItem = StorageFormatType.V3;
            }
        }

        getFormat().setItems(formats);
        getFormat().setSelectedItem(selectItem);
    }

    private void updateItemsAvailability() {
        if (getStorageModels() == null) {
            return;
        }

        behavior.updateItemsAvailability();
    }

    private void updateWipeAfterDelete() {
        StorageType storageType = getAvailableStorageTypeItems().getSelectedItem();
        if (isNewStorage()) {
            AsyncDataProvider.getInstance().getStorageDomainDefaultWipeAfterDelete(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override public void onSuccess(Object model, Object returnValue) {
                            StorageModel storageModel = (StorageModel) model;
                            storageModel.getWipeAfterDelete().setEntity((Boolean) returnValue);
                        }
                    }), storageType);
        }
        else {
            getWipeAfterDelete().setEntity(getStorage().getWipeAfterDelete());
        }
    }

    public boolean validate() {
        validateListItems(getHost());
        validateListItems(getAvailableStorageDomainTypeItems());
        validateListItems(getAvailableStorageTypeItems());

        getDescription().validateEntity(new IValidation[] {
                new LengthValidation(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE),
                new SpecialAsciiI18NOrNoneValidation() });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        getWarningLowSpaceIndicator().validateEntity(new IValidation[]{
                new NotEmptyValidation(), new IntegerValidation(0, StorageConstants.LOW_SPACE_THRESHOLD)
        });

        getCriticalSpaceActionBlocker().validateEntity(new IValidation[] {
                new NotEmptyValidation(), new IntegerValidation(0, Integer.MAX_VALUE)
        });

        return getName().getIsValid()
                && getHost().getIsValid()
                && getIsValid()
                && getCurrentStorageItem().validate()
                && getDescription().getIsValid()
                && getComment().getIsValid()
                && getWarningLowSpaceIndicator().getIsValid()
                && getCriticalSpaceActionBlocker().getIsValid();
    }

    private void validateListItems(ListModel<?> listModel) {
        ValidationResult result = new NotEmptyValidation().validate(listModel.getSelectedItem());
        listModel.setIsValid(result.getSuccess());
        listModel.getInvalidityReasons().addAll(result.getReasons());
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        systemTreeSelectedItem = value;
    }

    public boolean isStorageActive() {
         return
                getStorage().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active
                || getStorage().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Mixed;
    }

    public boolean isNewStorage() {
        return getStorage() == null;
    }

    public StorageModelBehavior getBehavior() {
        return behavior;
    }

    private int getWarningLowSpaceIndicatorValue() {
        if (isNewStorage()) {
            return (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.WarningLowSpaceIndicator);
        }
        return getStorage().getWarningLowSpaceIndicator();
    }

    private int getCriticalSpaceThresholdValue() {
        if (isNewStorage()) {
            return (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.CriticalSpaceActionBlocker);
        }
        return getStorage().getCriticalSpaceActionBlocker();
    }

    public void updateCurrentStorageItem() {
        StorageDomainType storageDomainType = getAvailableStorageDomainTypeItems().getSelectedItem();
        StorageType storageType = getAvailableStorageTypeItems().getSelectedItem();
        for (IStorageModel model : getStorageModels()) {
            if (model.getType() == storageType && model.getRole() == storageDomainType) {
                setCurrentStorageItem(model);
                break;
            }
        }
    }

    public List<IStorageModel> getStorageModelsByRole(StorageDomainType role) {
        List<IStorageModel> filteredModels = new LinkedList<>();
        for (IStorageModel model : getStorageModels()) {
            if (model.getRole() == role) {
                filteredModels.add(model);
            }
        }
        return filteredModels;
    }

}
