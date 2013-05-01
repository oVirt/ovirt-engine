package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
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

public class StorageModel extends ListModel implements ISupportSystemTreeContext
{
    public static final Guid UnassignedDataCenterId = Guid.Empty;
    private StorageModelBehavior behavior;

    private String localFSPath;

    @Override
    public IStorageModel getSelectedItem()
    {
        return (IStorageModel) super.getSelectedItem();
    }

    public void setSelectedItem(IStorageModel value)
    {
        super.setSelectedItem(value);
    }

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

    public ArrayList<IStorageModel> UpdatedStorageModels = new ArrayList<IStorageModel>();
    private String privateOriginalName;

    public String getOriginalName()
    {
        return privateOriginalName;
    }

    public void setOriginalName(String value)
    {
        privateOriginalName = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel description;

    public EntityModel getDescription() {
        return description;
    }

    public void setDescription(EntityModel description) {
        this.description = description;
    }

    private ListModel privateDataCenter;

    public ListModel getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel value)
    {
        privateDataCenter = value;
    }

    private EntityModel dataCenterAlert;

    public EntityModel getDataCenterAlert() {
        return dataCenterAlert;
    }

    public void setDataCenterAlert(EntityModel dataCenterAlert) {
        this.dataCenterAlert = dataCenterAlert;
    }

    private ListModel privateHost;

    public ListModel getHost()
    {
        return privateHost;
    }

    public void setHost(ListModel value)
    {
        privateHost = value;
    }

    private ListModel privateFormat;

    public ListModel getFormat()
    {
        return privateFormat;
    }

    private void setFormat(ListModel value)
    {
        privateFormat = value;
    }

    private ListModel privateAvailableStorageItems;

    public ListModel getAvailableStorageItems()
    {
        return privateAvailableStorageItems;
    }

    private void setAvailableStorageItems(ListModel value)
    {
        privateAvailableStorageItems = value;
    }

    private String privateHash;

    public String getHash()
    {
        return privateHash;
    }

    public void setHash(String value)
    {
        privateHash = value;
    }

    public StorageModel(StorageModelBehavior behavior)
    {
        this.behavior = behavior;
        this.behavior.setModel(this);

        Frontend.getQueryStartedEvent().addListener(this);
        Frontend.getQueryCompleteEvent().addListener(this);
        Frontend.subscribeAdditionalQueries(new VdcQueryType[] { VdcQueryType.Search, VdcQueryType.GetConfigurationValue,
                VdcQueryType.GetStoragePoolsByStorageDomainId, VdcQueryType.GetStorageDomainsByStoragePoolId,
                VdcQueryType.GetLunsByVgId, VdcQueryType.GetAllVdsByStoragePool,
                VdcQueryType.DiscoverSendTargets, VdcQueryType.GetDeviceList, VdcQueryType.GetExistingStorageDomainList });

        setName(new EntityModel());
        setDescription(new EntityModel());
        setDataCenterAlert(new EntityModel());
        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        setHost(new ListModel());
        getHost().getSelectedItemChangedEvent().addListener(this);
        setFormat(new ListModel());
        setAvailableStorageItems(new ListModel());
        getAvailableStorageItems().getSelectedItemChangedEvent().addListener(this);

        localFSPath = (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.RhevhLocalFSPath);
    }

    @Override
    public void Initialize()
    {
        super.Initialize();

        setHash(getHashName() + new Date());
        behavior.setHash(getHash());

        InitDataCenter();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(selectedItemChangedEventDefinition))
        {
            if (sender == getDataCenter())
            {
                DataCenter_SelectedItemChanged();
            }
            else if (sender == getHost())
            {
                Host_SelectedItemChanged();
            }
            else if (sender == getAvailableStorageItems())
            {
                if (getAvailableStorageItems().getSelectedItem() instanceof IStorageModel)
                {
                    setSelectedItem(null);
                    setSelectedItem((IStorageModel) getAvailableStorageItems().getSelectedItem());
                }
            }
        }
        else if (ev.matchesDefinition(NfsStorageModel.PathChangedEventDefinition))
        {
            NfsStorageModel_PathChanged(sender, args);
        }
        else if (ev.matchesDefinition(Frontend.QueryStartedEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            Frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.QueryCompleteEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            Frontend_QueryComplete();
        }
    }

    private int queryCounter;

    public void Frontend_QueryStarted()
    {
        queryCounter++;
        if (getProgress() == null)
        {
            StartProgress(null);
        }
    }

    public void Frontend_QueryComplete()
    {
        queryCounter--;
        if (queryCounter == 0)
        {
            StopProgress();
        }
    }

    private void NfsStorageModel_PathChanged(Object sender, EventArgs args)
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
            UpdateFormat();
            UpdateHost();
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

    private void DataCenter_SelectedItemChanged()
    {
        UpdateItemsAvailability();
        updateDataCenterAlert();
    }

    private void updateDataCenterAlert() {
        if (getDataCenter().getSelectedItem() != null
                && !UnassignedDataCenterId.equals(((StoragePool) getDataCenter().getSelectedItem()).getId())
                && ((StoragePool) getDataCenter().getSelectedItem()).getstatus() == StoragePoolStatus.Uninitialized) {
            getDataCenterAlert().setIsAvailable(true);
            getDataCenterAlert().setEntity(ConstantsManager.getInstance().getConstants().dataCenterUninitializedAlert());
        }
        else {
            getDataCenterAlert().setIsAvailable(false);
            getDataCenterAlert().setEntity("");
        }
    }

    private void Host_SelectedItemChanged()
    {
        VDS host = (VDS) getHost().getSelectedItem();
        if (getSelectedItem() != null)
        {
            // When changing host clear items for san storage model.
            if (getSelectedItem() instanceof SanStorageModelBase)
            {
                SanStorageModelBase sanStorageModel = (SanStorageModelBase) getSelectedItem();
                sanStorageModel.setHash(getHash());

                if (getStorage() == null) {
                    sanStorageModel.setItems(null);
                }
            }

            if (host != null)
            {
                getSelectedItem().getUpdateCommand().Execute();

                VDSType vdsType = ((VDS) this.getHost().getSelectedItem()).getVdsType();
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

    private void InitDataCenter()
    {
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (getSystemTreeSelectedItem().getType())
            {
            case DataCenter:
            case Cluster:
            case Storages:
            case Storage: {
                SystemTreeItemModel dataCenterItem =
                        SystemTreeItemModel.FindAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
                StoragePool dc = (StoragePool) dataCenterItem.getEntity();

                getDataCenter().setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dc })));
                getDataCenter().setSelectedItem(dc);
                getDataCenter().setIsChangable(false);
                getDataCenter().setInfo("Cannot choose Storage's Data Center in tree context"); //$NON-NLS-1$
            }
                break;

            case Host: {
                VDS host = (VDS) getSystemTreeSelectedItem().getEntity();

                getHost().setIsChangable(false);
                getHost().setInfo("Cannot choose Storage's Host in tree context"); //$NON-NLS-1$
                getHost().setSelectedItem(host);

                SystemTreeItemModel dataCenterItem =
                        SystemTreeItemModel.FindAncestor(SystemTreeItemType.DataCenter, getSystemTreeSelectedItem());
                StoragePool dc = (StoragePool) dataCenterItem.getEntity();

                getDataCenter().setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dc })));
                getDataCenter().setSelectedItem(dc);
                getDataCenter().setIsChangable(false);
                getDataCenter().setInfo("Cannot choose Storage's Data Center in tree context"); //$NON-NLS-1$
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
                AsyncDataProvider.GetDataCenterList(new AsyncQuery(new Object[] { this, behavior },
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                Object[] array = (Object[]) target;
                                StorageModel storageModel = (StorageModel) array[0];
                                StorageModelBehavior storageModelBehavior = (StorageModelBehavior) array[1];
                                List<StoragePool> dataCenters =
                                        (ArrayList<StoragePool>) returnValue;
                                dataCenters = storageModelBehavior.FilterDataCenter(dataCenters);
                                StorageModel.AddEmptyDataCenterToList(dataCenters);
                                StoragePool oldSelectedItem =
                                        (StoragePool) storageModel.getDataCenter().getSelectedItem();
                                storageModel.getDataCenter().setItems(dataCenters);
                                if (oldSelectedItem != null)
                                {
                                    storageModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters,
                                            new Linq.DataCenterPredicate(oldSelectedItem.getId())));
                                }
                                else
                                {
                                    storageModel.getDataCenter()
                                            .setSelectedItem(getStorage() == null ? Linq.firstOrDefault(dataCenters)
                                                    : Linq.firstOrDefault(dataCenters,
                                                            new Linq.DataCenterPredicate(UnassignedDataCenterId)));
                                }

                            }
                        }, getHash()));
            }

            else // "Edit Storage" mode:
            {
                AsyncDataProvider.GetDataCentersByStorageDomain(new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                StorageModel storageModel = (StorageModel) target;
                                List<StoragePool> dataCenters = new ArrayList<StoragePool>();
                                List<StoragePool> dataCentersWithStorage =
                                        (ArrayList<StoragePool>) returnValue;
                                if (dataCentersWithStorage.size() < 1 || dataCentersWithStorage.get(0) == null)
                                {
                                    StorageModel.AddEmptyDataCenterToList(dataCenters);
                                }
                                else
                                {
                                    dataCenters =
                                            new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dataCentersWithStorage.get(0) }));
                                }
                                storageModel.getDataCenter().setItems(dataCenters);
                                storageModel.getDataCenter().setSelectedItem(Linq.firstOrDefault(dataCenters));

                            }
                        },
                        getHash()),
                        getStorage().getId());
            }
        }
    }

    private static void AddEmptyDataCenterToList(List<StoragePool> dataCenters)
    {
        StoragePool tempVar = new StoragePool();
        tempVar.setId(UnassignedDataCenterId);
        tempVar.setname("(none)"); //$NON-NLS-1$
        dataCenters.add(tempVar);
    }

    void UpdateHost()
    {
        if (getDataCenter().getItems() == null)
        {
            return;
        }

        if (getSelectedItem() == null)
        {
            return;
        }

        StoragePool dataCenter = (StoragePool) getDataCenter().getSelectedItem();

        if (getSelectedItem() instanceof LocalStorageModel
                && (dataCenter == null || dataCenter.getId().equals(UnassignedDataCenterId)))
        {
            ArrayList<StoragePool> dataCenterList =
                    (ArrayList<StoragePool>) getDataCenter().getItems();
            ArrayList<StoragePool> localDCList = new ArrayList<StoragePool>();
            StringBuilder dataCenterQueryLine = new StringBuilder();

            for (StoragePool storagePool : dataCenterList)
            {
                if (storagePool.getstorage_pool_type() == StorageType.LOCALFS)
                {
                    localDCList.add(storagePool);
                }
            }

            if (localDCList.size() > 0)
            {
                int i = 0;
                for (; i < localDCList.size() - 1; i++)
                {
                    dataCenterQueryLine.append("datacenter=").append(localDCList.get(i).getname()).append(" or "); //$NON-NLS-1$ //$NON-NLS-2$
                }
                dataCenterQueryLine.append("datacenter=").append(localDCList.get(i).getname()); //$NON-NLS-1$

                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.setContext(getHash());
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue)
                    {
                        StorageModel storageModel = (StorageModel) model;
                        Iterable<VDS> hosts =
                                (ArrayList<VDS>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        storageModel.PostUpdateHost(hosts);
                    }
                };
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Hosts: status=Up " + dataCenterQueryLine.toString(), //$NON-NLS-1$
                        SearchType.VDS), _asyncQuery);
            }
        }
        else
        {
            if (dataCenter == null || dataCenter.getId().equals(UnassignedDataCenterId))
            {
                AsyncDataProvider.GetHostList(new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                StorageModel storageModel = (StorageModel) target;
                                Iterable<VDS> hosts = (Iterable<VDS>) returnValue;
                                storageModel.PostUpdateHost(hosts);
                            }
                        }, getHash()));
            }
            else
            {
                AsyncDataProvider.GetHostListByDataCenter(new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {

                                StorageModel storageModel = (StorageModel) target;
                                Iterable<VDS> hosts = (Iterable<VDS>) returnValue;
                                storageModel.PostUpdateHost(hosts);

                            }
                        }, getHash()), dataCenter.getId());
            }
        }
    }

    public void PostUpdateHost(Iterable<VDS> hosts)
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

        VDS oldSelectedItem = (VDS) getHost().getSelectedItem();
        VDS selectedItem = null;

        // On Edit of active storage - only SPM is available. In edit of storage in maintenance,
        //any host can perform the operation, thus no need to filter to use just the SPM
        if (getStorage() != null && getStorage().getStatus() != StorageDomainStatus.Maintenance) {
            hosts = Collections.singletonList(getSPM(hosts));
        }

        // Try to select previously selected host.
        if (oldSelectedItem != null) {
            selectedItem = Linq.firstOrDefault(hosts, new Linq.HostPredicate(oldSelectedItem.getId()));
        }

        // Select a default - first host in the list.
        if (selectedItem == null) {
            selectedItem = Linq.firstOrDefault(hosts);
        }

        getHost().setItems(hosts);
        getHost().setSelectedItem(selectedItem);
    }

    private VDS getSPM(Iterable<VDS> hosts) {
        for (VDS host : hosts) {
            if (host.getSpmStatus() == VdsSpmStatus.SPM) {
                return host;
            }
        }

        return null;
    }

    void UpdateFormat()
    {
        StoragePool dataCenter = (StoragePool) getDataCenter().getSelectedItem();

        StorageFormatType selectItem = StorageFormatType.V1;

        ArrayList<StorageFormatType> formats = new ArrayList<StorageFormatType>();

        if (dataCenter != null && getSelectedItem() != null)
        {
            if (!dataCenter.getId().equals(UnassignedDataCenterId))
            {
                getFormat().setIsChangable(false);

                // If data center has format defined and the selected-item role is Data, choose it.
                if (dataCenter.getStoragePoolFormatType() != null
                        && getSelectedItem().getRole() == StorageDomainType.Data)
                {
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
                        && (dataCenter.getcompatibility_version().compareTo(Version.v3_1) < 0))
                {
                    formats.add(StorageFormatType.V1);
                }
                else if (getSelectedItem().getType().isBlockDomain()
                        && dataCenter.getcompatibility_version().compareTo(Version.v3_0) < 0)
                {
                    formats.add(StorageFormatType.V1);
                }
                else if (getSelectedItem().getType().isBlockDomain()
                        && dataCenter.getcompatibility_version().compareTo(Version.v3_0) == 0)
                {
                    formats.add(StorageFormatType.V2);
                    selectItem = StorageFormatType.V2;
                }
                else if (dataCenter.getcompatibility_version().compareTo(Version.v3_1) >= 0)
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

                formats.add(StorageFormatType.V1);

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

    private void UpdateItemsAvailability()
    {
        if (getItems() == null)
        {
            return;
        }

        behavior.UpdateItemsAvailability();
    }

    public boolean Validate() {
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

        return getName().getIsValid() && getHost().getIsValid() && getIsValid() && getSelectedItem().Validate() && getDescription().getIsValid();
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

    public boolean isNewStorage() {
        return getStorage() == null;
    }
}
