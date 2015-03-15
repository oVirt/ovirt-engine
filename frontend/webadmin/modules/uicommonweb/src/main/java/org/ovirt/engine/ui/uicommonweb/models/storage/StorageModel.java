package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class StorageModel extends ListModel<IStorageModel> implements ISupportSystemTreeContext
{
    public static final Guid UnassignedDataCenterId = Guid.Empty;
    private StorageModelBehavior behavior;

    private String localFSPath;

    /**
     * Gets or sets the storage being edited. Null if it's a new one.
     */
    private StorageDomain privateStorage;

    public StorageDomain getStorage()
    {
        return privateStorage;
    }

    public void setStorage(StorageDomain value)
    {
        privateStorage = value;
    }

    public ArrayList<IStorageModel> updatedStorageModels = new ArrayList<IStorageModel>();
    private String privateOriginalName;

    public String getOriginalName()
    {
        return privateOriginalName;
    }

    public void setOriginalName(String value)
    {
        privateOriginalName = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName()
    {
        return privateName;
    }

    private void setName(EntityModel<String> value)
    {
        privateName = value;
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

    private ListModel<StoragePool> privateDataCenter;

    public ListModel<StoragePool> getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel<StoragePool> value)
    {
        privateDataCenter = value;
    }

    private EntityModel<String> dataCenterAlert;

    public EntityModel<String> getDataCenterAlert() {
        return dataCenterAlert;
    }

    public void setDataCenterAlert(EntityModel<String> dataCenterAlert) {
        this.dataCenterAlert = dataCenterAlert;
    }

    private ListModel<VDS> privateHost;

    public ListModel<VDS> getHost()
    {
        return privateHost;
    }

    public void setHost(ListModel<VDS> value)
    {
        privateHost = value;
    }

    private ListModel<StorageFormatType> privateFormat;

    public ListModel<StorageFormatType> getFormat()
    {
        return privateFormat;
    }

    private void setFormat(ListModel<StorageFormatType> value)
    {
        privateFormat = value;
    }

    private ListModel<IStorageModel> privateAvailableStorageItems;

    public ListModel<IStorageModel> getAvailableStorageItems()
    {
        return privateAvailableStorageItems;
    }

    private void setAvailableStorageItems(ListModel<IStorageModel> value)
    {
        privateAvailableStorageItems = value;
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

    public StorageModel(StorageModelBehavior behavior)
    {
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
        setAvailableStorageItems(new ListModel<IStorageModel>());
        getAvailableStorageItems().getSelectedItemChangedEvent().addListener(this);
        setActivateDomain(new EntityModel<Boolean>(true));
        getActivateDomain().setIsAvailable(false);
        setWipeAfterDelete(new EntityModel<>(false));

        localFSPath = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.RhevhLocalFSPath);
    }

    @Override
    public void initialize()
    {
        super.initialize();

        behavior.initialize();

        initDataCenter();
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(selectedItemChangedEventDefinition))
        {
            if (sender == getDataCenter())
            {
                dataCenter_SelectedItemChanged();
            }
            else if (sender == getHost())
            {
                host_SelectedItemChanged();
            }
            else if (sender == getAvailableStorageItems())
            {
                if (getAvailableStorageItems().getSelectedItem() instanceof IStorageModel)
                {
                    setSelectedItem(null);
                    setSelectedItem(getAvailableStorageItems().getSelectedItem());
                    updateWipeAfterDelete();
                }
            }
        }
        else if (ev.matchesDefinition(NfsStorageModel.pathChangedEventDefinition))
        {
            nfsStorageModel_PathChanged(sender, args);
        }
    }

    private void nfsStorageModel_PathChanged(Object sender, EventArgs args)
    {
        NfsStorageModel senderModel = (NfsStorageModel) sender;

        for (Object item : getItems())
        {
            if (item instanceof NfsStorageModel && item != sender)
            {
                NfsStorageModel model = (NfsStorageModel) item;
                model.getPath().setEntity(senderModel.getPath().getEntity());
            }
        }
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();

        if (getSelectedItem() != null)
        {
            updateFormat();
            updateHost();
        }
    }

    @Override
    protected void itemsChanged()
    {
        super.itemsChanged();

        if (getItems() != null)
        {
            for (Object item : getItems())
            {
                IStorageModel model = (IStorageModel) item;
                model.setContainer(this);

                if (item instanceof NfsStorageModel)
                {
                    NfsStorageModel nfsModel = (NfsStorageModel) item;
                    nfsModel.getPathChangedEvent().addListener(this);
                }
            }
        }
    }

    private void dataCenter_SelectedItemChanged()
    {
        updateItemsAvailability();
        behavior.updateDataCenterAlert();
    }

    private void host_SelectedItemChanged()
    {
        VDS host = getHost().getSelectedItem();
        if (getSelectedItem() != null)
        {
            // When changing host clear items for san storage model.
            if (getSelectedItem() instanceof SanStorageModelBase)
            {
                SanStorageModelBase sanStorageModel = (SanStorageModelBase) getSelectedItem();

                if (getStorage() == null) {
                    sanStorageModel.setItems(null);
                }
            }

            if (host != null)
            {
                getSelectedItem().getUpdateCommand().execute();

                VDSType vdsType = this.getHost().getSelectedItem().getVdsType();
                String prefix = vdsType.equals(VDSType.oVirtNode) ? localFSPath : ""; //$NON-NLS-1$
                if (!StringHelper.isNullOrEmpty(prefix))
                {
                    for (Object item : getItems())
                    {
                        if (item instanceof LocalStorageModel)
                        {
                            LocalStorageModel model = (LocalStorageModel) item;
                            model.getPath().setEntity(prefix);
                            model.getPath().setIsChangable(false);
                        }
                    }
                }
            }
        }
    }

    private void initDataCenter()
    {
        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (getSystemTreeSelectedItem().getType())
            {
            case DataCenter:
            case Cluster:
            case Storages:
            case Storage: {
                SystemTreeItemModel dataCenterItem =
                        SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
                StoragePool dc = (StoragePool) dataCenterItem.getEntity();

                getDataCenter().setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dc })));
                getDataCenter().setSelectedItem(dc);
                getDataCenter().setIsChangable(false);
                getDataCenter().setChangeProhibitionReason(constants.cannotChangeDCInTreeContext());
            }
                break;

            case Host: {
                VDS host = (VDS) getSystemTreeSelectedItem().getEntity();

                getHost().setIsChangable(false);
                getHost().setChangeProhibitionReason(constants.cannotChangeHostInTreeContext());
                getHost().setSelectedItem(host);

                SystemTreeItemModel dataCenterItem =
                        SystemTreeItemModel.findAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
                StoragePool dc = (StoragePool) dataCenterItem.getEntity();

                getDataCenter().setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dc })));
                getDataCenter().setSelectedItem(dc);
                getDataCenter().setIsChangable(false);
                getDataCenter().setChangeProhibitionReason(constants.cannotChangeDCInTreeContext());
            }
                break;
            }
        }
        else
        {
            if (getStorage() == null
                    || getStorage().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)
            // We are either adding a new storage or editing an unattached storage
            // -> fill DataCenters drop-down with all possible Data-Centers, choose the empty one:
            // [TODO: In case of an Unattached SD, choose only DCs of the same type]
            {
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
                                                                                             storageModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters,
                                                                                                                                                              new Linq.DataCenterPredicate(oldSelectedItem.getId())));
                                                                                         } else {
                                                                                             storageModel.getDataCenter()
                                                                                                     .setSelectedItem(getStorage() == null ? Linq.firstOrDefault(dataCenters)
                                                                                                                              : Linq.firstOrDefault(dataCenters,
                                                                                                                                                    new Linq.DataCenterPredicate(UnassignedDataCenterId)));
                                                                                         }

                                                                                     }
                                                                                 }));
            }

            else // "Edit Storage" mode:
            {
                AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                StorageModel storageModel = (StorageModel) target;
                                List<StoragePool> dataCenters = new ArrayList<StoragePool>();
                                List<StoragePool> dataCentersWithStorage =
                                        (ArrayList<StoragePool>) returnValue;
                                if (dataCentersWithStorage.size() < 1 || dataCentersWithStorage.get(0) == null)
                                {
                                    StorageModel.addEmptyDataCenterToList(dataCenters);
                                }
                                else
                                {
                                    dataCenters =
                                            new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dataCentersWithStorage.get(0) }));
                                }
                                storageModel.getDataCenter().setItems(dataCenters);
                                storageModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters));

                            }
                        }),
                        getStorage().getId());
            }
        }
    }

    private static void addEmptyDataCenterToList(List<StoragePool> dataCenters)
    {
        StoragePool tempVar = new StoragePool();
        tempVar.setId(UnassignedDataCenterId);
        tempVar.setName("(none)"); //$NON-NLS-1$
        dataCenters.add(tempVar);
    }

    void updateHost()
    {
        if (getDataCenter().getItems() == null)
        {
            return;
        }

        if (getSelectedItem() == null)
        {
            return;
        }

        StoragePool dataCenter = getDataCenter().getSelectedItem();

        boolean localFsOnly = getSelectedItem() instanceof LocalStorageModel;
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

    public void postUpdateHost(Collection<VDS> hosts)
    {
        // Filter hosts
        hosts = Linq.where(hosts, new Linq.HostStatusPredicate(VDSStatus.Up));

        // Allow only hosts with version above 2.2 for export storage.
        ArrayList<VDS> list = new ArrayList<VDS>();
        if (getSelectedItem() != null && getSelectedItem().getRole() == StorageDomainType.ImportExport)
        {
            for (VDS host : hosts)
            {
                if (host.getVdsGroupCompatibilityVersion().compareTo(new Version("2.2")) >= 0) //$NON-NLS-1$
                {
                    list.add(host);
                }
            }
            hosts = list;
        }

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
            selectedItem = Linq.firstOrDefault(hosts, new Linq.HostPredicate(oldSelectedItem.getId()));
        }

        // Select a default - if there's a SPM choose it, otherwise choose the first host in the list.
        if (selectedItem == null) {
            VDS spm = getSPM(hosts);
            selectedItem = spm == null ? Linq.firstOrDefault(hosts) : spm;
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

    void updateFormat()
    {
        StoragePool dataCenter = getDataCenter().getSelectedItem();

        StorageFormatType selectItem = StorageFormatType.V1;

        ArrayList<StorageFormatType> formats = new ArrayList<StorageFormatType>();

        if (dataCenter != null && getSelectedItem() != null)
        {
            if (!dataCenter.getId().equals(UnassignedDataCenterId))
            {
                getFormat().setIsChangable(false);

                // If data center has format defined and the selected-item role is Data, choose it.
                if (dataCenter.getStoragePoolFormatType() != null
                        && getSelectedItem().getRole().isDataDomain()) {
                    formats.add(dataCenter.getStoragePoolFormatType());
                    selectItem = dataCenter.getStoragePoolFormatType();
                }
                // If selected-item role is ISO or Export, add only the 'V1' option.
                // (*** Note that currently both ISO and Export can be only NFS, so theoretically they are covered by
                // the next "else if..." condition; however, just in case we will support non-NFS ISO/Export in the
                // future
                // and in order to make the code more explicit, it is here. ***)
                else if ((getSelectedItem().getRole() == StorageDomainType.ISO
                        || getSelectedItem().getRole() == StorageDomainType.ImportExport))
                {
                    formats.add(StorageFormatType.V1);
                }
                else if ((getSelectedItem().getType() == StorageType.NFS
                        || getSelectedItem().getType() == StorageType.LOCALFS)
                        && (dataCenter.getCompatibilityVersion().compareTo(Version.v3_1) < 0))
                {
                    formats.add(StorageFormatType.V1);
                }
                else if (getSelectedItem().getType().isBlockDomain()
                        && dataCenter.getCompatibilityVersion().compareTo(Version.v3_0) < 0)
                {
                    formats.add(StorageFormatType.V1);
                }
                else if (getSelectedItem().getType().isBlockDomain()
                        && dataCenter.getCompatibilityVersion().compareTo(Version.v3_0) == 0)
                {
                    formats.add(StorageFormatType.V2);
                    selectItem = StorageFormatType.V2;
                }
                else if (dataCenter.getCompatibilityVersion().compareTo(Version.v3_1) >= 0)
                {
                    formats.add(StorageFormatType.V3);
                    selectItem = StorageFormatType.V3;
                }
            }
            else // Unassigned DC:
            {
                if ((getSelectedItem().getRole() == StorageDomainType.ISO
                        || getSelectedItem().getRole() == StorageDomainType.ImportExport))
                {
                    // ISO/Export domains should not be available for '(none)' DC
                    return;
                }

                getFormat().setIsChangable(true);

                if (getSelectedItem().getType() != StorageType.POSIXFS && getSelectedItem().getType() != StorageType.GLUSTERFS) {
                    formats.add(StorageFormatType.V1);
                }

                if ((getSelectedItem().getType() == StorageType.FCP || getSelectedItem().getType() == StorageType.ISCSI)
                        && getSelectedItem().getRole() == StorageDomainType.Data)
                {
                    formats.add(StorageFormatType.V2);
                }

                formats.add(StorageFormatType.V3);
                selectItem = StorageFormatType.V3;
            }
        }

        getFormat().setItems(formats);
        getFormat().setSelectedItem(selectItem);
    }

    private void updateItemsAvailability()
    {
        if (getItems() == null)
        {
            return;
        }

        behavior.updateItemsAvailability();
    }

    private void updateWipeAfterDelete() {
        StorageType storageType = getAvailableStorageItems().getSelectedItem().getType();
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
        ValidationResult result = new NotEmptyValidation().validate(getHost().getSelectedItem());
        if (!result.getSuccess()) {
            getHost().setIsValid(false);
            for (String reason : result.getReasons()) {
                getHost().getInvalidityReasons().add(reason);
            }
        }
        else {
            getHost().setIsValid(true);
        }
        validateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });
        getDescription().validateEntity(new IValidation[] {
                new LengthValidation(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE),
                new SpecialAsciiI18NOrNoneValidation() });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        return getName().getIsValid() && getHost().getIsValid() && getIsValid() && getSelectedItem().validate()
                && getDescription().getIsValid() && getComment().getIsValid();
    }

    private SystemTreeItemModel privateSystemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return privateSystemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        privateSystemTreeSelectedItem = value;
    }

    public boolean isStorageActive() {
         return
                getStorage().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Active
                || getStorage().getStorageDomainSharedStatus() == StorageDomainSharedStatus.Mixed;
    }

    public boolean isStorageNotLocked() {
        return getStorage().getStorageDomainSharedStatus() != StorageDomainSharedStatus.Locked;
    }

    public boolean isNewStorage() {
        return getStorage() == null;
    }

    public StorageModelBehavior getBehavior() {
        return behavior;
    }
}
