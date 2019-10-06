package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
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

public class StorageModel extends Model {
    public static final Guid UnassignedDataCenterId = Guid.Empty;

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private StorageModelBehavior behavior;

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

    private EntityModel<Integer> warningLowConfirmedSpaceIndicator;

    public EntityModel<Integer> getWarningLowConfirmedSpaceIndicator() {
        return warningLowConfirmedSpaceIndicator;
    }

    public void setWarningLowConfirmedSpaceIndicator(EntityModel<Integer> warningLowConfirmedSpaceIndicator) {
        this.warningLowConfirmedSpaceIndicator = warningLowConfirmedSpaceIndicator;
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

    private EntityModel<Boolean> discardAfterDelete;

    public EntityModel<Boolean> getDiscardAfterDelete() {
        return discardAfterDelete;
    }

    public void setDiscardAfterDelete(EntityModel<Boolean> discardAfterDelete) {
        this.discardAfterDelete = discardAfterDelete;
    }

    private EntityModel<Boolean> backup;

    public EntityModel<Boolean> getBackup() {
        return backup;
    }

    public void setBackup(EntityModel<Boolean> backup) {
        this.backup = backup;
    }

    public StorageModel(StorageModelBehavior behavior) {
        this.behavior = behavior;
        this.behavior.setModel(this);

        setName(new EntityModel<>());
        setDescription(new EntityModel<>());
        setComment(new EntityModel<>());
        setDataCenterAlert(new EntityModel<>());
        setDataCenter(new ListModel<>());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        setHost(new ListModel<>());
        getHost().getSelectedItemChangedEvent().addListener(this);
        setFormat(new ListModel<>());
        setAvailableStorageTypeItems(new ListModel<>());
        getAvailableStorageTypeItems().getSelectedItemChangedEvent().addListener(this);
        getAvailableStorageTypeItems().getItemsChangedEvent().addListener(this);
        setAvailableStorageDomainTypeItems(new ListModel<>());
        getAvailableStorageDomainTypeItems().getSelectedItemChangedEvent().addListener(this);
        setWarningLowSpaceIndicator(new EntityModel<>());
        getWarningLowSpaceIndicator().setEntity(getWarningLowSpaceIndicatorValue());
        setWarningLowSpaceSize(new EntityModel<>());
        getWarningLowSpaceSize().setIsAvailable(false);
        setCriticalSpaceActionBlocker(new EntityModel<>());
        getCriticalSpaceActionBlocker().setEntity(getCriticalSpaceThresholdValue());
        setWarningLowConfirmedSpaceIndicator(new EntityModel<>());
        getWarningLowConfirmedSpaceIndicator().setEntity(getWarningLowConfirmedSpaceIndicatorValue());
        setActivateDomain(new EntityModel<>(true));
        getActivateDomain().setIsAvailable(false);
        setWipeAfterDelete(new EntityModel<>(false));
        setDiscardAfterDelete(new EntityModel<>(false));
        getDiscardAfterDelete().getEntityChangedEvent().addListener(this);
        setBackup(new EntityModel<>(false));
        getBackup().setIsAvailable(false);
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
            } else if (sender == getHost()) {
                host_SelectedItemChanged();
            } else if (sender == getAvailableStorageTypeItems()) {
                storageType_SelectedItemChanged();
            } else if (sender == getAvailableStorageDomainTypeItems()) {
                behavior.setStorageTypeItems();
            }
        } else if (ev.matchesDefinition(ListModel.itemsChangedEventDefinition)) {
            if (sender == getAvailableStorageTypeItems()) {
                storageItemsChanged();
            }
        } else if (ev.matchesDefinition(NfsStorageModel.pathChangedEventDefinition)) {
            nfsStorageModel_PathChanged(sender);
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getDiscardAfterDelete()) {
                if (getDiscardAfterDelete().getIsAvailable()) {
                    ((SanStorageModelBase) getCurrentStorageItem()).updateLunWarningForDiscardAfterDelete();
                }
            }
        }
    }

    private void nfsStorageModel_PathChanged(Object sender) {
        NfsStorageModel senderModel = (NfsStorageModel) sender;

        getStorageModels().stream()
                .filter(item -> item instanceof NfsStorageModel && item != sender)
                .map(item -> (NfsStorageModel) item)
                .forEach(model -> model.getPath().setEntity(senderModel.getPath().getEntity()));
    }

    protected void storageType_SelectedItemChanged() {
        updateCurrentStorageItem();
        if (getCurrentStorageItem() != null) {
            updateFormat();
            updateHost();
        }
        updateWipeAfterDelete();
        updateDiscardAfterDelete();
        updateBackup();
    }

    protected void storageItemsChanged() {
        if (getStorageModels() != null) {
            getStorageModels().forEach(item -> {
                item.setContainer(this);

                if (item instanceof NfsStorageModel) {
                    NfsStorageModel nfsModel = (NfsStorageModel) item;
                    nfsModel.getPathChangedEvent().addListener(this);
                }
            });
        }
    }

    private void dataCenter_SelectedItemChanged() {
        if (getCurrentStorageItem() instanceof SanStorageModelBase) {
            SanStorageModelBase sanStorageModel = (SanStorageModelBase) getCurrentStorageItem();
            boolean isMaintenance = !isNewStorage() && getStorage().getStatus() == StorageDomainStatus.Maintenance;
            sanStorageModel.setReduceDeviceSupported(isMaintenance);
            sanStorageModel.updateRemovableLuns();
        }
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

                String prefix = ""; //$NON-NLS-1$
                if (!StringHelper.isNullOrEmpty(prefix)) {
                    getStorageModels().stream()
                            .filter(item -> item instanceof LocalStorageModel)
                            .map(item -> (LocalStorageModel) item)
                            .forEach(model -> {
                                model.getPath().setEntity(prefix);
                                model.getPath().setIsChangeable(false);
                            });
                }
            }
        }
    }

    private void initDataCenter() {
        if (getStorage() == null
                || getStorage().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached) {
        // We are either adding a new storage or editing an unattached storage
        // -> fill DataCenters drop-down with all possible Data-Centers, choose the empty one:
        // [TODO: In case of an Unattached SD, choose only DCs of the same type]
            AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(
                    dataCenters -> {

                        StorageModelBehavior storageModelBehavior = behavior;
                        dataCenters = storageModelBehavior.filterDataCenter(dataCenters);
                        addEmptyDataCenterToList(dataCenters);
                        StoragePool oldSelectedItem = getDataCenter().getSelectedItem();
                        getDataCenter().setItems(dataCenters);
                        if (oldSelectedItem != null) {
                            getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters,
                                    new Linq.IdPredicate<>(oldSelectedItem.getId())));
                        } else {
                            getDataCenter()
                                    .setSelectedItem(getStorage() == null ? Linq.firstOrNull(dataCenters)
                                            : Linq.firstOrNull(dataCenters,
                                                    new Linq.IdPredicate<>(UnassignedDataCenterId)));
                        }

                    }));
        } else { // "Edit Storage" mode:
            AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery<>(
                            dataCentersWithStorage -> {

                                List<StoragePool> dataCenters = new ArrayList<>();
                                if (dataCentersWithStorage.size() < 1 || dataCentersWithStorage.get(0) == null) {
                                    addEmptyDataCenterToList(dataCenters);
                                } else {
                                    dataCenters =
                                            new ArrayList<>(Collections.singletonList(dataCentersWithStorage.get(0)));
                                }
                                getDataCenter().setItems(dataCenters);
                                getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters));

                            }),
                    getStorage().getId());
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

        AsyncDataProvider.getInstance().getHostsForStorageOperation(new AsyncQuery<>(this::postUpdateHost), dataCenterId, localFsOnly);
    }

    public void postUpdateHost(Collection<VDS> hosts) {
        // Filter hosts
        hosts = Linq.where(hosts, new Linq.StatusPredicate<>(VDSStatus.Up));

        VDS oldSelectedItem = getHost().getSelectedItem();
        VDS selectedItem = null;

        // On Edit of active storage - only SPM is available. In edit of storage in maintenance,
        //any host can perform the operation, thus no need to filter to use just the SPM
        if (getStorage() != null && getStorage().getStatus() != StorageDomainStatus.Maintenance) {
            VDS spm = getSPM(hosts);
            hosts = spm != null ? Collections.singletonList(spm) : Collections.emptyList();
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

    private VDS getSPM(Collection<VDS> hosts) {
        return hosts.stream().filter(host -> host.getSpmStatus() == VdsSpmStatus.SPM).findFirst().orElse(null);
    }

    void updateFormat() {
        StoragePool dataCenter = getDataCenter().getSelectedItem();

        StorageFormatType selectItem = StorageFormatType.V1;

        ArrayList<StorageFormatType> formats = new ArrayList<>();

        if (dataCenter != null && getCurrentStorageItem() != null) {
            if (!dataCenter.getId().equals(UnassignedDataCenterId)) {
                getFormat().setIsChangeable(false);

                // If data center is not yet initialized the storage pool format type is null although its version might
                // not support specific storage formats for example v4.0 Data Center and v4 storage domains which
                // supported only for v4.1.
                if (dataCenter.getStoragePoolFormatType() == null) {
                    StorageFormatType targetFormat =
                            VersionStorageFormatUtil.getForVersion(dataCenter.getCompatibilityVersion());
                    dataCenter.setStoragePoolFormatType(targetFormat);
                }

                // If data center has format defined and the selected-item role is Data, choose it unless this is an Managed block storage.
                if (getCurrentStorageItem().getRole().isDataDomain() && !getCurrentStorageItem().getType().equals(StorageType.MANAGED_BLOCK_STORAGE)) {
                    formats.add(dataCenter.getStoragePoolFormatType());
                    selectItem = dataCenter.getStoragePoolFormatType();
                } else if (getCurrentStorageItem().getRole() == StorageDomainType.ISO
                        || getCurrentStorageItem().getRole() == StorageDomainType.ImportExport) {
                    // If selected-item role is ISO or Export, add only the 'V1' option.
                    // (*** Note that currently both ISO and Export can be only NFS, so theoretically they are covered
                    // by the next "else if..." condition; however, just in case we will support non-NFS ISO/Export in
                    // the future and in order to make the code more explicit, it is here. ***)
                    formats.add(StorageFormatType.V1);
                }
            } else { // Unassigned DC:
                if (getCurrentStorageItem().getRole() == StorageDomainType.ISO
                        || getCurrentStorageItem().getRole() == StorageDomainType.ImportExport) {
                    // ISO/Export domains should not be available for '(none)' DC
                    formats.add(StorageFormatType.V1);
                    getFormat().setItems(formats);
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
                formats.add(StorageFormatType.V4);
                formats.add(StorageFormatType.V5);
                selectItem = StorageFormatType.V5;
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
            AsyncDataProvider.getInstance().getStorageDomainDefaultWipeAfterDelete(new AsyncQuery<>(
                    returnValue -> getWipeAfterDelete().setEntity(returnValue)), storageType);
        } else {
            getWipeAfterDelete().setEntity(getStorage().getWipeAfterDelete());
        }
    }

    private void updateDiscardAfterDelete() {
        if (getDataCenter().getSelectedItem() != null && getAvailableStorageTypeItems().getSelectedItem() != null) {
            boolean isBlockDomain = getAvailableStorageTypeItems().getSelectedItem().isBlockDomain();
            boolean isStorageDomainUnattached = getDataCenter().getSelectedItem().getId().equals(Guid.Empty);
            if (!isBlockDomain || isStorageDomainUnattached) {
                getDiscardAfterDelete().setIsAvailable(false);
                getDiscardAfterDelete().setEntity(false);
                return;
            }

            getDiscardAfterDelete().setIsAvailable(true);
            if (getDiscardAfterDelete().getIsAvailable() && !isNewStorage()) {
                getDiscardAfterDelete().setEntity(getStorage().getDiscardAfterDelete());
            } else {
                getDiscardAfterDelete().setEntity(false);
            }
        }
    }

    private void updateBackup() {
        if (getCurrentStorageItem() != null && getAvailableStorageTypeItems().getSelectedItem() != null) {
            boolean isStorageDomainUnattached =
                    getDataCenter().getSelectedItem().getId().equals(UnassignedDataCenterId);
            if (isStorageDomainUnattached) {
                if (getCurrentStorageItem().getRole().isDataDomain()) {
                    getBackup().setIsAvailable(true);
                    getBackup().setEntity(isNewStorage() ? false : getStorage().isBackup());
                } else {
                    getBackup().setIsAvailable(false);
                    getBackup().setEntity(false);
                }
            } else if (getCurrentStorageItem().getRole() == StorageDomainType.ISO
                    || getCurrentStorageItem().getRole() == StorageDomainType.ImportExport) {
                getBackup().setIsAvailable(false);
                getBackup().setEntity(false);
            } else {
                getBackup().setIsAvailable(true);
                getBackup().setEntity(isNewStorage() ? false : getStorage().isBackup());
            }
        } else {
            getBackup().setIsAvailable(false);
            getBackup().setEntity(false);
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

        int maxAllowedSpace = Integer.MAX_VALUE;

        if (!isNewStorage() && getStorage().getTotalDiskSize() != 0 ) {
            maxAllowedSpace = getStorage().getTotalDiskSize();
        }
        getCriticalSpaceActionBlocker().validateEntity(new IValidation[] {
                new NotEmptyValidation(), new IntegerValidation(0, maxAllowedSpace)
        });

        getWarningLowConfirmedSpaceIndicator().validateEntity(new IValidation[]{
                new NotEmptyValidation(), new IntegerValidation(0, StorageConstants.LOW_SPACE_THRESHOLD)
        });

        validateDiscardAfterDelete();

        return getName().getIsValid()
                && getHost().getIsValid()
                && getIsValid()
                && getCurrentStorageItem().validate()
                && getDescription().getIsValid()
                && getComment().getIsValid()
                && getWarningLowSpaceIndicator().getIsValid()
                && getCriticalSpaceActionBlocker().getIsValid()
                && getWarningLowConfirmedSpaceIndicator().getIsValid()
                && getDiscardAfterDelete().getIsValid()
                && getBackup().getIsValid();
    }

    private void validateDiscardAfterDelete() {
        if (getDiscardAfterDelete().getIsAvailable() && getDiscardAfterDelete().getEntity()) {
            SanStorageModelBase sanStorageModel = (SanStorageModelBase) getCurrentStorageItem();
            Collection<LunModel> luns = sanStorageModel.getSelectedLuns();
            if (luns != null && !storageDomainSupportsDiscard(luns)) {
                getDiscardAfterDelete().getInvalidityReasons().add(
                        constants.discardIsNotSupportedByUnderlyingStorage());
                getDiscardAfterDelete().setIsValid(false);
                return;
            }
        }
        getDiscardAfterDelete().setIsValid(true);
    }

    private boolean storageDomainSupportsDiscard(Collection<LunModel> luns) {
        return luns.stream().allMatch(lun -> lun.getEntity().supportsDiscard());
    }

    private void validateListItems(ListModel<?> listModel) {
        ValidationResult result = new NotEmptyValidation().validate(listModel.getSelectedItem());
        listModel.setIsValid(result.getSuccess());
        listModel.getInvalidityReasons().addAll(result.getReasons());
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

    private Integer  getWarningLowSpaceIndicatorValue() {
        if (isNewStorage()) {
            return (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.WarningLowSpaceIndicator);
        }
        return getStorage().getWarningLowSpaceIndicator();
    }

    private Integer getCriticalSpaceThresholdValue() {
        if (isNewStorage()) {
            return (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.CriticalSpaceActionBlocker);
        }
        return getStorage().getCriticalSpaceActionBlocker();
    }

    private Integer getWarningLowConfirmedSpaceIndicatorValue() {
        if (isNewStorage()) {
            return (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.WarningLowSpaceIndicator);
        }
        return getStorage().getWarningLowConfirmedSpaceIndicator();
    }

    public void updateCurrentStorageItem() {
        StorageDomainType storageDomainType = getAvailableStorageDomainTypeItems().getSelectedItem();
        StorageType storageType = getAvailableStorageTypeItems().getSelectedItem();
        getStorageModels().stream()
                .filter(model -> model.getType() == storageType && model.getRole() == storageDomainType)
                .findFirst()
                .ifPresent(this::setCurrentStorageItem);
    }

    public List<IStorageModel> getStorageModelsByRole(StorageDomainType role) {
        return getStorageModels().stream().filter(model -> model.getRole() == role).collect(Collectors.toList());
    }
}
