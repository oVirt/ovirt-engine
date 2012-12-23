package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.uicommonweb.models.storage.FcpStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LocalStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NewEditStorageModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ITaskTarget;
import org.ovirt.engine.ui.uicompat.Task;
import org.ovirt.engine.ui.uicompat.TaskContext;

@SuppressWarnings("unused")
public class DataCenterGuideModel extends GuideModel implements ITaskTarget
{

    public final String DataCenterConfigureClustersAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureClustersAction();
    public final String DataCenterAddAnotherClusterAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAddAnotherClusterAction();
    public final String DataCenterConfigureHostsAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureHostsAction();
    public final String DataCenterSelectHostsAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterSelectHostsAction();
    public final String DataCenterConfigureStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureStorageAction();
    public final String DataCenterAddMoreStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAddMoreStorageAction();
    public final String DataCenterAttachStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAttachStorageAction();
    public final String DataCenterAttachMoreStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAttachMoreStorageAction();
    public final String DataCenterConfigureISOLibraryAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureISOLibraryAction();
    public final String DataCenterAttachISOLibraryAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAttachISOLibraryAction();

    public final String NoUpHostReason = ConstantsManager.getInstance().getConstants().noUpHostReason();
    public final String NoDataDomainAttachedReason = ConstantsManager.getInstance()
            .getConstants()
            .noDataDomainAttachedReason();

    private StorageDomainStatic storageDomain;
    private TaskContext context;
    private IStorageModel storageModel;
    private NGuid storageId;
    private StorageServerConnections nfsConnection;
    private StorageServerConnections connection;
    private Guid hostId = new Guid();
    private String path;
    private StorageDomainType domainType = StorageDomainType.values()[0];
    private boolean removeConnection;
    private ArrayList<VDSGroup> clusters;
    private ArrayList<storage_domains> allStorageDomains;
    private ArrayList<storage_domains> attachedStorageDomains;
    private ArrayList<storage_domains> isoStorageDomains;
    private ArrayList<VDS> allHosts;
    private VDS localStorageHost;

    public DataCenterGuideModel() {
    }

    @Override
    public storage_pool getEntity()
    {
        return (storage_pool) super.getEntity();
    }

    public void setEntity(storage_pool value)
    {
        super.setEntity(value);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        UpdateOptions();
    }

    private void UpdateOptionsNonLocalFSData() {
        AsyncDataProvider.GetClusterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        dataCenterGuideModel.clusters = clusters;
                        dataCenterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }), getEntity().getId());

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> storageDomains =
                                (ArrayList<storage_domains>) returnValue;
                        dataCenterGuideModel.allStorageDomains = storageDomains;
                        dataCenterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }));

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> storageDomains =
                                (ArrayList<storage_domains>) returnValue;
                        dataCenterGuideModel.attachedStorageDomains = storageDomains;
                        dataCenterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }), getEntity().getId());

        AsyncDataProvider.GetISOStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> storageDomains =
                                (ArrayList<storage_domains>) returnValue;
                        dataCenterGuideModel.isoStorageDomains = storageDomains;
                        dataCenterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }));

        AsyncDataProvider.GetHostList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                        dataCenterGuideModel.allHosts = hosts;
                        dataCenterGuideModel.UpdateOptionsNonLocalFS();
                    }
                }));
    }

    private void UpdateOptionsLocalFSData() {
        AsyncDataProvider.GetClusterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        dataCenterGuideModel.clusters = clusters;
                        dataCenterGuideModel.UpdateOptionsLocalFS();
                    }
                }), getEntity().getId());

        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Hosts: datacenter!= " + getEntity().getname() //$NON-NLS-1$
                + " status=maintenance or status=pendingapproval ", SearchType.VDS), new AsyncQuery(this, //$NON-NLS-1$
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDS> hosts =
                                (ArrayList<VDS>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        if (hosts == null) {
                            hosts = new ArrayList<VDS>();
                        }
                        dataCenterGuideModel.allHosts = hosts;

                        AsyncDataProvider.GetLocalStorageHost(new AsyncQuery(dataCenterGuideModel,
                                new INewAsyncCallback() {
                                    @Override
                                    public void OnSuccess(Object target, Object returnValue) {
                                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                                        VDS localStorageHost = (VDS) returnValue;
                                        ;
                                        dataCenterGuideModel.localStorageHost = localStorageHost;
                                        dataCenterGuideModel.UpdateOptionsLocalFS();
                                    }
                                }), dataCenterGuideModel.getEntity().getname());
                    }
                }));
    }

    private void UpdateOptionsNonLocalFS() {
        if (clusters == null || allStorageDomains == null || attachedStorageDomains == null
                || isoStorageDomains == null || allHosts == null) {
            return;
        }

        // Add cluster action.
        UICommand addClusterAction = new UICommand("AddCluster", this); //$NON-NLS-1$
        if (clusters.isEmpty())
        {
            addClusterAction.setTitle(DataCenterConfigureClustersAction);
            getCompulsoryActions().add(addClusterAction);
        }
        else
        {
            addClusterAction.setTitle(DataCenterAddAnotherClusterAction);
            getOptionalActions().add(addClusterAction);
        }


        ArrayList<VDS> hosts = new ArrayList<VDS>();
        ArrayList<VDS> availableHosts = new ArrayList<VDS>();
        ArrayList<VDS> upHosts = new ArrayList<VDS>();
        for (VDS vds : allHosts)
        {
            if (Linq.IsClusterItemExistInList(clusters, vds.getvds_group_id()))
            {
                hosts.add(vds);
            }

            if ((vds.getstatus() == VDSStatus.Maintenance || vds.getstatus() == VDSStatus.PendingApproval)
                && doesHostSupportAnyCluster(clusters, vds))
            {
                availableHosts.add(vds);
            }

            if (vds.getstatus() == VDSStatus.Up && Linq.IsClusterItemExistInList(clusters, vds.getvds_group_id()))
            {
                upHosts.add(vds);
            }
        }

        UICommand tempVar = new UICommand("AddHost", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(clusters.size() > 0);
        UICommand addHostAction = tempVar;

        addHostAction.setTitle(DataCenterConfigureHostsAction);
        getCompulsoryActions().add(addHostAction);

        // Select host action.
        UICommand selectHostAction = new UICommand("SelectHost", this); //$NON-NLS-1$

        // If now compatible hosts are found - disable the select host button
        selectHostAction.setIsChangable(availableHosts.size() > 0);
        selectHostAction.setIsExecutionAllowed(availableHosts.size() > 0);

        if (clusters.size() > 0) {
            if (hosts.isEmpty())
            {
                selectHostAction.setTitle(DataCenterSelectHostsAction);
                getCompulsoryActions().add(selectHostAction);
            }
            else
            {
                selectHostAction.setTitle(DataCenterSelectHostsAction);
                getOptionalActions().add(selectHostAction);
            }
        }


        ArrayList<storage_domains> unattachedStorage = new ArrayList<storage_domains>();
        boolean addToList;
        Version version3_0 = new Version(3, 0);
        for (storage_domains item : allStorageDomains)
        {
            addToList = false;
            if (item.getstorage_domain_type() == StorageDomainType.Data
                    && item.getstorage_type() == getEntity().getstorage_pool_type()
                    && item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
            {
                if (getEntity().getStoragePoolFormatType() == null)
                {
                    // compat logic: in case its not v1 and the version is less than 3.0 break.
                    if (item.getStorageStaticData().getStorageFormat() != StorageFormatType.V1
                            && getEntity().getcompatibility_version().compareTo(version3_0) < 0)
                    {
                        continue;
                    }
                    addToList = true;
                }
                else if (getEntity().getStoragePoolFormatType() == item.getStorageStaticData().getStorageFormat())
                {
                    addToList = true;
                }
            }

            if (addToList)
            {
                unattachedStorage.add(item);
            }
        }

        ArrayList<storage_domains> attachedDataStorages = new ArrayList<storage_domains>();
        for (storage_domains a : attachedStorageDomains)
        {
            if (a.getstorage_domain_type() == StorageDomainType.Data
                    || a.getstorage_domain_type() == StorageDomainType.Master)
            {
                attachedDataStorages.add(a);
            }
        }

        UICommand addDataStorageAction = new UICommand("AddDataStorage", this); //$NON-NLS-1$
        addDataStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
        addDataStorageAction.setIsExecutionAllowed(upHosts.size() > 0);

        if (unattachedStorage.isEmpty() && attachedDataStorages.isEmpty())
        {
            addDataStorageAction.setTitle(DataCenterConfigureStorageAction);
            getCompulsoryActions().add(addDataStorageAction);
        }
        else
        {
            addDataStorageAction.setTitle(DataCenterAddMoreStorageAction);
            getOptionalActions().add(addDataStorageAction);
        }

        // Attach data storage action.
        UICommand attachDataStorageAction = new UICommand("AttachDataStorage", this); //$NON-NLS-1$
        if (upHosts.isEmpty())
        {
            attachDataStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
        }
        attachDataStorageAction.setIsExecutionAllowed(unattachedStorage.size() > 0 && upHosts.size() > 0);

        if (attachedDataStorages.isEmpty())
        {
            attachDataStorageAction.setTitle(DataCenterAttachStorageAction);
            getCompulsoryActions().add(attachDataStorageAction);
        }
        else
        {
            attachDataStorageAction.setTitle(DataCenterAttachMoreStorageAction);
            getOptionalActions().add(attachDataStorageAction);
        }

        UICommand addIsoStorageAction = new UICommand("AddIsoStorage", this); //$NON-NLS-1$
        addIsoStorageAction.getExecuteProhibitionReasons().add(NoDataDomainAttachedReason);
        addIsoStorageAction.setIsExecutionAllowed(getEntity().getstatus() == StoragePoolStatus.Up);

        if (isoStorageDomains.isEmpty())
        {
            addIsoStorageAction.setTitle(DataCenterConfigureISOLibraryAction);
            getOptionalActions().add(addIsoStorageAction);
        }

        // Attach ISO storage action.
        // Allow to attach ISO domain only when there are Data storages attached
        // and there ISO storages to attach and ther are no ISO storages actually
        // attached.
        ArrayList<storage_domains> attachedIsoStorages = new ArrayList<storage_domains>();
        for (storage_domains sd : attachedStorageDomains)
        {
            if (sd.getstorage_domain_type() == StorageDomainType.ISO)
            {
                attachedIsoStorages.add(sd);
            }
        }

        boolean attachIsoAllowed =
                (attachedDataStorages.size() > 0 && Linq.IsAnyStorageDomainIsMatserAndActive(attachedDataStorages)
                        && isoStorageDomains.size() > 0 && attachedIsoStorages.isEmpty() && upHosts.size() > 0);

        // The action is available if there are no storages attached to the
        // Data Center. It will not always be allowed.
        boolean attachIsoAvailable = attachedIsoStorages.isEmpty();

        UICommand attachIsoStorageAction = new UICommand("AttachIsoStorage", this); //$NON-NLS-1$
        attachIsoStorageAction.setIsAvailable(attachIsoAvailable);
        if (upHosts.isEmpty())
        {
            attachIsoStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
        }
        attachIsoStorageAction.setIsExecutionAllowed(attachIsoAllowed);

        if (attachIsoAvailable)
        {
            attachIsoStorageAction.setTitle(DataCenterAttachISOLibraryAction);
            getOptionalActions().add(attachIsoStorageAction);
        }

        StopProgress();
    }

    private boolean doesHostSupportAnyCluster(List<VDSGroup> clusterList, VDS host){
        for (VDSGroup cluster : clusterList){
            if (host.getSupportedClusterVersionsSet().contains(cluster.getcompatibility_version())){
                return true;
            }
        }
        return false;
    }

    private void UpdateOptionsLocalFS() {
        if (clusters == null || allHosts == null) {
            return;
        }

        UICommand addClusterAction = new UICommand("AddCluster", this); //$NON-NLS-1$
        if (clusters.isEmpty())
        {
            addClusterAction.setTitle(DataCenterConfigureClustersAction);
            getCompulsoryActions().add(addClusterAction);
        }
        else
        {
            UICommand tempVar6 = new UICommand("AddHost", this); //$NON-NLS-1$
            tempVar6.setTitle(DataCenterConfigureHostsAction);
            UICommand addHostAction = tempVar6;
            UICommand tempVar7 = new UICommand("SelectHost", this); //$NON-NLS-1$
            tempVar7.setTitle(DataCenterSelectHostsAction);
            UICommand selectHost = tempVar7;
            boolean hasMaintenance3_0Host = false;

            Version version3_0 = new Version(3, 0);
            for (VDS vds : allHosts)
            {
                String[] hostVersions = vds.getsupported_cluster_levels().split("[,]", -1); //$NON-NLS-1$
                for (String hostVersion : hostVersions)
                {
                    if (version3_0.compareTo(new Version(hostVersion)) <= 0)
                    {
                        hasMaintenance3_0Host = true;
                        break;
                    }
                }
                if (hasMaintenance3_0Host)
                {
                    break;
                }
            }

            if (localStorageHost != null)
            {
                String hasHostReason =
                        ConstantsManager.getInstance().getConstants().localDataCenterAlreadyContainsAHostDcGuide();
                addHostAction.getExecuteProhibitionReasons().add(hasHostReason);
                addHostAction.setIsExecutionAllowed(false);
                selectHost.getExecuteProhibitionReasons().add(hasHostReason);
                selectHost.setIsExecutionAllowed(false);
                if (localStorageHost.getstatus() == VDSStatus.Up)
                {
                    UICommand tempVar8 = new UICommand("AddLocalStorage", this); //$NON-NLS-1$
                    tempVar8.setTitle(ConstantsManager.getInstance().getConstants().addLocalStorageTitle());
                    UICommand addLocalStorageAction = tempVar8;
                    getOptionalActions().add(addLocalStorageAction);
                }
            }
            else if (getEntity().getstatus() != StoragePoolStatus.Uninitialized)
            {
                String dataCenterInitializeReason =
                        ConstantsManager.getInstance().getConstants().dataCenterWasAlreadyInitializedDcGuide();
                addHostAction.getExecuteProhibitionReasons().add(dataCenterInitializeReason);
                addHostAction.setIsExecutionAllowed(false);
                selectHost.getExecuteProhibitionReasons().add(dataCenterInitializeReason);
                selectHost.setIsExecutionAllowed(false);
            }

            if (hasMaintenance3_0Host)
            {
                getOptionalActions().add(selectHost);
            }
            getCompulsoryActions().add(addHostAction);
        }

        StopProgress();
    }

    private void UpdateOptions()
    {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null)
        {
            StartProgress(null);

            if (getEntity().getstorage_pool_type() != StorageType.LOCALFS)
            {
                UpdateOptionsNonLocalFSData();
            }
            else
            {
                UpdateOptionsLocalFSData();
            }
        }
    }

    private void ResetData() {
        storageDomain = null;
        storageModel = null;
        storageId = null;
        nfsConnection = null;
        connection = null;
        removeConnection = false;
        path = null;
        hostId = new Guid();
        domainType = StorageDomainType.values()[0];
        clusters = null;
        allStorageDomains = null;
        attachedStorageDomains = null;
        isoStorageDomains = null;
        allHosts = null;
        localStorageHost = null;
    }

    private void AddLocalStorage()
    {
        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newLocalDomainTitle());
        model.setHashName("new_local_domain"); //$NON-NLS-1$
        LocalStorageModel localStorageModel = new LocalStorageModel();
        localStorageModel.setRole(StorageDomainType.Data);

        ArrayList<IStorageModel> list = new ArrayList<IStorageModel>();
        list.add(localStorageModel);
        model.setItems(list);
        model.setSelectedItem(list.get(0));

        AsyncDataProvider.GetLocalStorageHost(new AsyncQuery(new Object[] { this, model },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        Object[] array = (Object[]) target;
                        DataCenterGuideModel listModel = (DataCenterGuideModel) array[0];
                        StorageModel model = (StorageModel) array[1];
                        VDS localHost = (VDS) returnValue;

                        model.getHost()
                                .setItems(new ArrayList<VDS>(Arrays.asList(new VDS[] { localHost })));
                        model.getHost().setSelectedItem(localHost);
                        model.getDataCenter()
                                .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { getEntity() })));
                        model.getDataCenter().setSelectedItem(getEntity());
                        UICommand tempVar = new UICommand("OnAddStorage", listModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                        tempVar.setIsDefault(true);
                        model.getCommands().add(tempVar);
                        UICommand tempVar2 = new UICommand("Cancel", listModel); //$NON-NLS-1$
                        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                        tempVar2.setIsCancel(true);
                        model.getCommands().add(tempVar2);
                    }
                }),
                getEntity().getname());
    }

    public void AddIsoStorage()
    {
        AddStorageInternal(ConstantsManager.getInstance().getConstants().newISOLibraryTitle(), StorageDomainType.ISO);
    }

    public void AddDataStorage()
    {
        AddStorageInternal(ConstantsManager.getInstance().getConstants().newStorageTitle(), StorageDomainType.Data);
    }

    private void AddStorageInternal(String title, StorageDomainType type)
    {
        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(title);
        model.setHashName("new_domain"); //$NON-NLS-1$
        ArrayList<storage_pool> dataCenters = new ArrayList<storage_pool>();
        dataCenters.add(getEntity());
        model.getDataCenter().setItems(dataCenters);
        model.getDataCenter().setSelectedItem(getEntity());
        model.getDataCenter().setIsChangable(false);

        ArrayList<IStorageModel> items = new ArrayList<IStorageModel>();

        if (type == StorageDomainType.Data)
        {
            NfsStorageModel tempVar = new NfsStorageModel();
            tempVar.setRole(StorageDomainType.Data);
            items.add(tempVar);
            IscsiStorageModel tempVar2 = new IscsiStorageModel();
            tempVar2.setRole(StorageDomainType.Data);
            tempVar2.setIsGrouppedByTarget(true);
            items.add(tempVar2);
            FcpStorageModel tempVar3 = new FcpStorageModel();
            tempVar3.setRole(StorageDomainType.Data);
            items.add(tempVar3);
            LocalStorageModel tempVar4 = new LocalStorageModel();
            tempVar4.setRole(StorageDomainType.Data);
            items.add(tempVar4);
        }
        else if (type == StorageDomainType.ISO)
        {
            NfsStorageModel tempVar5 = new NfsStorageModel();
            tempVar5.setRole(StorageDomainType.ISO);
            items.add(tempVar5);
        }

        model.setItems(items);

        model.Initialize();

        UICommand tempVar6 = new UICommand("OnAddStorage", this); //$NON-NLS-1$
        tempVar6.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar6.setIsDefault(true);
        model.getCommands().add(tempVar6);
        UICommand tempVar7 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar7.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar7.setIsCancel(true);
        model.getCommands().add(tempVar7);
    }

    public void OnAddStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        String storageName = (String) model.getName().getEntity();

        AsyncDataProvider.IsStorageDomainNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();
                        String name = (String) storageModel.getName().getEntity();
                        String tempVar = storageModel.getOriginalName();
                        String originalName = (tempVar != null) ? tempVar : ""; //$NON-NLS-1$
                        boolean isNameUnique = (Boolean) returnValue;
                        if (!isNameUnique && name.compareToIgnoreCase(originalName) != 0)
                        {
                            storageModel.getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                            storageModel.getName().setIsValid(false);
                        }
                        AsyncDataProvider.GetStorageDomainMaxNameLength(new AsyncQuery(dataCenterGuideModel,
                                new INewAsyncCallback() {
                                    @Override
                                    public void OnSuccess(Object target1, Object returnValue1) {

                                        DataCenterGuideModel dataCenterGuideModel1 = (DataCenterGuideModel) target1;
                                        StorageModel storageModel1 = (StorageModel) dataCenterGuideModel1.getWindow();
                                        int nameMaxLength = (Integer) returnValue1;
                                        RegexValidation tempVar2 = new RegexValidation();
                                        tempVar2.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$"); //$NON-NLS-1$ //$NON-NLS-2$
                                        tempVar2.setMessage(ConstantsManager.getInstance().getMessages()
                                                .nameCanContainOnlyMsg(nameMaxLength));
                                        storageModel1.getName().ValidateEntity(new IValidation[] {
                                                new NotEmptyValidation(), tempVar2 });
                                        dataCenterGuideModel1.PostOnAddStorage();

                                    }
                                }));

                    }
                }),
                storageName);
    }

    public void PostOnAddStorage()
    {
        StorageModel model = (StorageModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        // Save changes.
        if (model.getSelectedItem() instanceof NfsStorageModel)
        {
            SaveNfsStorage();
        }
        else if (model.getSelectedItem() instanceof LocalStorageModel)
        {
            SaveLocalStorage();
        }
        else
        {
            SaveSanStorage();
        }
    }

    private void SaveLocalStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveLocal" }))).Run(); //$NON-NLS-1$
    }

    private void SaveLocalStorage(TaskContext context)
    {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        VDS host = (VDS) model.getHost().getSelectedItem();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getSelectedItem();
        LocalStorageModel localModel = (LocalStorageModel) storageModel;
        path = (String) localModel.getPath().getEntity();

        storageDomain = new StorageDomainStatic();
        storageDomain.setstorage_type(isNew ? storageModel.getType() : storageDomain.getstorage_type());
        storageDomain.setstorage_domain_type(isNew ? storageModel.getRole() : storageDomain.getstorage_domain_type());
        storageDomain.setstorage_name((String) model.getName().getEntity());

        AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> storages =
                                (ArrayList<storage_domains>) returnValue;
                        if (storages != null && storages.size() > 0)
                        {
                            String storageName = storages.get(0).getstorage_name();
                            OnFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else
                        {
                            dataCenterGuideModel.SaveNewLocalStorage();
                        }

                    }
                }),
                host.getStoragePoolId(),
                path);
    }

    public void SaveNewLocalStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        LocalStorageModel localModel = (LocalStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setconnection(path);
        tempVar.setstorage_type(localModel.getType());
        connection = tempVar;

        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddLocalStorageDomain);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.removeConnection = true;

                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageDomain.setstorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.removeConnection = false;

                dataCenterGuideModel.OnFinish(dataCenterGuideModel.context, true, dataCenterGuideModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();

                if (dataCenterGuideModel.removeConnection)
                {
                    dataCenterGuideModel.CleanConnection(dataCenterGuideModel.connection, dataCenterGuideModel.hostId);
                    dataCenterGuideModel.removeConnection = false;
                }

                dataCenterGuideModel.OnFinish(dataCenterGuideModel.context, false, dataCenterGuideModel.storageModel);

            }
        };
        Frontend.RunMultipleActions(actionTypes,
                parameters,
                new ArrayList<IFrontendActionAsyncCallback>(Arrays.asList(new IFrontendActionAsyncCallback[] {
                        callback1, callback2 })),
                failureCallback,
                this);
    }

    private void CleanConnection(StorageServerConnections connection, Guid hostId)
    {
        Frontend.RunAction(VdcActionType.RemoveStorageServerConnection,
                new StorageServerConnectionParametersBase(connection, hostId),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                },
                this);
    }

    public void OnFinish(TaskContext context, boolean isSucceeded, IStorageModel model)
    {
        OnFinish(context, isSucceeded, model, null);
    }

    public void OnFinish(TaskContext context, boolean isSucceeded, IStorageModel model, String message)
    {
        context.InvokeUIThread(this,
                new ArrayList<Object>(Arrays.asList(new Object[] { "Finish", isSucceeded, model, //$NON-NLS-1$
                        message })));
    }

    private void SaveNfsStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveNfs" }))).Run(); //$NON-NLS-1$
    }

    private void SaveNfsStorage(TaskContext context)
    {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getSelectedItem();
        NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
        path = (String) nfsModel.getPath().getEntity();

        storageDomain = new StorageDomainStatic();
        storageDomain.setstorage_type(isNew ? storageModel.getType() : storageDomain.getstorage_type());
        storageDomain.setstorage_domain_type(isNew ? storageModel.getRole() : storageDomain.getstorage_domain_type());
        storageDomain.setstorage_name((String) model.getName().getEntity());
        storageDomain.setStorageFormat((StorageFormatType) model.getFormat().getSelectedItem());

        AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> storages =
                                (ArrayList<storage_domains>) returnValue;
                        if (storages != null && storages.size() > 0)
                        {
                            String storageName = storages.get(0).getstorage_name();
                            OnFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else
                        {
                            dataCenterGuideModel.SaveNewNfsStorage();
                        }

                    }
                }),
                null,
                path);
    }

    public void SaveNewNfsStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        NfsStorageModel nfsModel = (NfsStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setconnection(path);
        tempVar.setstorage_type(nfsModel.getType());
        connection = tempVar;

        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddNFSStorageDomain);
        actionTypes.add(VdcActionType.RemoveStorageServerConnection);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);
        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageDomain.setstorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageId = (NGuid) vdcReturnValueBase.getActionReturnValue();

            }
        };
        IFrontendActionAsyncCallback callback3 = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();

                // Attach storage to data center as neccessary.
                storage_pool dataCenter = (storage_pool) storageModel.getDataCenter().getSelectedItem();
                if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                {
                    dataCenterGuideModel.AttachStorageToDataCenter((Guid) dataCenterGuideModel.storageId,
                            dataCenter.getId());
                }

                dataCenterGuideModel.OnFinish(dataCenterGuideModel.context, true, dataCenterGuideModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.CleanConnection(dataCenterGuideModel.connection, dataCenterGuideModel.hostId);
                dataCenterGuideModel.OnFinish(dataCenterGuideModel.context, false, dataCenterGuideModel.storageModel);

            }
        };
        Frontend.RunMultipleActions(actionTypes,
                parameters,
                new ArrayList<IFrontendActionAsyncCallback>(Arrays.asList(new IFrontendActionAsyncCallback[] {
                        callback1, callback2, callback3 })),
                failureCallback,
                this);
    }

    private void SaveSanStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().StartProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveSan" }))).Run(); //$NON-NLS-1$
    }

    private void SaveSanStorage(TaskContext context)
    {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();

        storageDomain = new StorageDomainStatic();
        storageDomain.setstorage_type(sanModel.getType());
        storageDomain.setstorage_domain_type(sanModel.getRole());
        storageDomain.setStorageFormat((StorageFormatType) sanModel.getContainer().getFormat().getSelectedItem());
        storageDomain.setstorage_name((String) model.getName().getEntity());

        AsyncDataProvider.GetStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> storages =
                                (ArrayList<storage_domains>) returnValue;
                        if (storages != null && storages.size() > 0)
                        {
                            String storageName = storages.get(0).getstorage_name();
                            OnFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else
                        {
                            dataCenterGuideModel.SaveNewSanStorage();
                        }

                        dataCenterGuideModel.getWindow().StopProgress();
                    }
                }),
                null,
                path);
    }

    public void SaveNewSanStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanStorageModel = (SanStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();

        ArrayList<String> usedLunsMessages = sanStorageModel.getUsedLunsMessages();

        if (usedLunsMessages.isEmpty()) {
            OnSaveSanStorage();
        }
        else {
            ForceCreationWarning(usedLunsMessages);
        }
    }

    private void OnSaveSanStorage()
    {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();

        if (confirmationModel != null && !confirmationModel.Validate())
        {
            return;
        }

        CancelConfirm();
        getWindow().StartProgress(null);

        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getSelectedItem();
        VDS host = (VDS) model.getHost().getSelectedItem();
        boolean force = sanModel.isForce();

        ArrayList<String> lunIds = new ArrayList<String>();
        for (LunModel lun : sanModel.getAddedLuns())
        {
            lunIds.add(lun.getLunId());
        }

        AddSANStorageDomainParameters params = new AddSANStorageDomainParameters(storageDomain);
        params.setVdsId(host.getId());
        params.setLunIds(lunIds);
        params.setForce(force);
        Frontend.RunAction(VdcActionType.AddSANStorageDomain, params,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                        StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();
                        storage_pool dataCenter = (storage_pool) storageModel.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                        {
                            VdcReturnValueBase returnValue = result.getReturnValue();
                            NGuid storageId = (NGuid) returnValue.getActionReturnValue();
                            dataCenterGuideModel.AttachStorageToDataCenter((Guid) storageId, dataCenter.getId());
                        }
                        dataCenterGuideModel.OnFinish(dataCenterGuideModel.context,
                                true,
                                dataCenterGuideModel.storageModel);

                    }
                }, this);
    }

    private void ForceCreationWarning(ArrayList<String> usedLunsMessages) {
        StorageModel storageModel = (StorageModel) getWindow();
        SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getSelectedItem();
        sanStorageModel.setForce(true);

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);

        model.setTitle(ConstantsManager.getInstance().getConstants().forceStorageDomainCreation());
        model.setMessage(ConstantsManager.getInstance().getConstants().lunsAlreadyInUse());
        model.setHashName("force_storage_domain_creation"); //$NON-NLS-1$
        model.setItems(usedLunsMessages);

        UICommand command;
        command = new UICommand("OnSaveSanStorage", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("CancelConfirm", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void AttachStorageInternal(List<storage_domains> storages, String title)
    {
        ListModel model = new ListModel();
        model.setTitle(title);
        setWindow(model);

        ArrayList<EntityModel> items = new ArrayList<EntityModel>();
        for (storage_domains sd : storages)
        {
            EntityModel tempVar = new EntityModel();
            tempVar.setEntity(sd);
            items.add(tempVar);
        }

        model.setItems(items);

        UICommand tempVar2 = new UICommand("OnAttachStorage", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar2.setIsDefault(true);
        model.getCommands().add(tempVar2);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
        model.getCommands().add(tempVar3);
    }

    private void AttachStorageToDataCenter(Guid storageId, Guid dataCenterId)
    {
        Frontend.RunAction(VdcActionType.AttachStorageDomainToPool, new StorageDomainPoolParametersBase(storageId,
                dataCenterId),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {
                    }
                }, this);
    }

    public void OnAttachStorage()
    {
        ListModel model = (ListModel) getWindow();

        ArrayList<storage_domains> items = new ArrayList<storage_domains>();
        for (EntityModel a : Linq.<EntityModel> Cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                items.add((storage_domains) a.getEntity());
            }
        }

        if (items.size() > 0)
        {
            for (storage_domains sd : items)
            {
                AttachStorageToDataCenter(sd.getId(), getEntity().getId());
            }
        }

        Cancel();
        PostAction();
    }

    public void AttachIsoStorage()
    {
        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> attachedStorage =
                                new ArrayList<storage_domains>();

                        AsyncDataProvider.GetISOStorageDomainList(new AsyncQuery(new Object[] { dataCenterGuideModel,
                                attachedStorage },
                                new INewAsyncCallback() {
                                    @Override
                                    public void OnSuccess(Object target, Object returnValue) {
                                        Object[] array = (Object[]) target;
                                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) array[0];
                                        ArrayList<storage_domains> attachedStorage =
                                                (ArrayList<storage_domains>) array[1];
                                        ArrayList<storage_domains> isoStorageDomains =
                                                (ArrayList<storage_domains>) returnValue;
                                        ArrayList<storage_domains> sdl =
                                                new ArrayList<storage_domains>();

                                        for (storage_domains a : isoStorageDomains)
                                        {
                                            boolean isContains = false;
                                            for (storage_domains b : attachedStorage)
                                            {
                                                if (b.getId().equals(a.getId()))
                                                {
                                                    isContains = true;
                                                    break;
                                                }
                                            }
                                            if (!isContains)
                                            {
                                                sdl.add(a);
                                            }
                                        }
                                        dataCenterGuideModel.AttachStorageInternal(sdl, ConstantsManager.getInstance()
                                                .getConstants()
                                                .attachISOLibraryTitle());
                                    }
                                }));
                    }
                }),
                getEntity().getId());
    }

    public void AttachDataStorage()
    {
        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<storage_domains> storageDomains =
                                (ArrayList<storage_domains>) returnValue;

                        ArrayList<storage_domains> unattachedStorage =
                                new ArrayList<storage_domains>();
                        boolean addToList;
                        Version version3_0 = new Version(3, 0);
                        for (storage_domains item : storageDomains)
                        {
                            addToList = false;
                            if (item.getstorage_domain_type() == StorageDomainType.Data
                                    && item.getstorage_type() == dataCenterGuideModel.getEntity()
                                            .getstorage_pool_type()
                                    && item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
                            {
                                if (getEntity().getStoragePoolFormatType() == null)
                                {
                                    // compat logic: in case its not v1 and the version is less than 3.0 continue.
                                    if (item.getStorageStaticData().getStorageFormat() != StorageFormatType.V1
                                            && dataCenterGuideModel.getEntity()
                                                    .getcompatibility_version()
                                                    .compareTo(version3_0) < 0)
                                    {
                                        continue;
                                    }
                                    addToList = true;
                                }
                                else if (getEntity().getStoragePoolFormatType() == item.getStorageStaticData()
                                        .getStorageFormat())
                                {
                                    addToList = true;
                                }
                            }

                            if (addToList)
                            {
                                unattachedStorage.add(item);
                            }
                        }
                        dataCenterGuideModel.AttachStorageInternal(unattachedStorage, ConstantsManager.getInstance()
                                .getConstants()
                                .attachStorageTitle());
                    }
                }));
    }

    public void AddCluster()
    {
        ClusterModel model = new ClusterModel();
        model.Init(false);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newClusterTitle());
        model.setHashName("new_cluster"); //$NON-NLS-1$
        model.setIsNew(true);

        ArrayList<storage_pool> dataCenters = new ArrayList<storage_pool>();
        dataCenters.add(getEntity());
        model.getDataCenter().setItems(dataCenters);
        model.getDataCenter().setSelectedItem(getEntity());
        model.getDataCenter().setIsChangable(false);

        UICommand tempVar = new UICommand("OnAddCluster", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnAddCluster()
    {
        ClusterModel model = (ClusterModel) getWindow();
        VDSGroup cluster = new VDSGroup();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate((Boolean) model.getEnableOvirtService().getEntity())) // CPU is mandatory only if the
                                                                                  // cluster is virt enabled
        {
            return;
        }

        // Save changes.
        Version version = (Version) model.getVersion().getSelectedItem();

        cluster.setname((String) model.getName().getEntity());
        cluster.setdescription((String) model.getDescription().getEntity());
        cluster.setStoragePoolId(((storage_pool) model.getDataCenter().getSelectedItem()).getId());
        if (model.getCPU().getSelectedItem() != null)
        {
            cluster.setcpu_name(((ServerCpu) model.getCPU().getSelectedItem()).getCpuName());
        }
        cluster.setmax_vds_memory_over_commit(model.getMemoryOverCommit());
        cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0); //$NON-NLS-1$
        cluster.setcompatibility_version(version);
        cluster.setVirtService((Boolean) model.getEnableOvirtService().getEntity());
        cluster.setGlusterService((Boolean) model.getEnableGlusterService().getEntity());

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.AddVdsGroup, new VdsGroupOperationParameters(cluster),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel localModel = (DataCenterGuideModel) result.getState();
                        localModel.PostOnAddCluster(result.getReturnValue());

                    }
                }, this);
    }

    public void PostOnAddCluster(VdcReturnValueBase returnValue)
    {
        ClusterModel model = (ClusterModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
            PostAction();
        }
    }

    public void SelectHost()
    {
        MoveHost model = new MoveHost();
        model.setTitle(ConstantsManager.getInstance().getConstants().selectHostTitle());
        model.setHashName("select_host"); //$NON-NLS-1$

        // In case of local storage, do not show the cluster selection in host select menu as there can be only one cluster in that case
        //also only one host is allowed in the cluster so we should disable multi selection
        boolean isMultiHostDC = getEntity().getstorage_pool_type() == StorageType.LOCALFS;
        if (isMultiHostDC) {
            model.getCluster().setIsAvailable(false);
            model.setMultiSelection(false);
        }

        setWindow(model);

        AsyncDataProvider.GetClusterList(new AsyncQuery(new Object[] { this, model },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        Object[] array = (Object[]) target;
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) array[0];
                        MoveHost moveHostModel = (MoveHost) array[1];
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;

                        moveHostModel.getCluster().setItems(clusters);
                        moveHostModel.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));

                        UICommand tempVar = new UICommand("OnSelectHost", dataCenterGuideModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                        tempVar.setIsDefault(true);
                        moveHostModel.getCommands().add(tempVar);
                        UICommand tempVar2 = new UICommand("Cancel", dataCenterGuideModel); //$NON-NLS-1$
                        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                        tempVar2.setIsCancel(true);
                        moveHostModel.getCommands().add(tempVar2);
                    }
                }), getEntity().getId());
    }

    public void OnSelectHost()
    {
        MoveHost model = (MoveHost) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        model.setSelectedHosts(new ArrayList<VDS>());
        for (EntityModel a : Linq.<EntityModel> Cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                model.getSelectedHosts().add((VDS) a.getEntity());
            }
        }

        VDSGroup cluster = (VDSGroup) model.getCluster().getSelectedItem();
        ArrayList<VdcActionParametersBase> paramerterList =
                new ArrayList<VdcActionParametersBase>();
        for (VDS host : model.getSelectedHosts())
        {
            // Try to change host's cluster as neccessary.
            if (host.getvds_group_id() != null && !host.getvds_group_id().equals(cluster.getId()))
            {
                paramerterList.add(new ChangeVDSClusterParameters(cluster.getId(), host.getId()));

            }
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ChangeVDSCluster, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                        ArrayList<VDS> hosts =
                                ((MoveHost) dataCenterGuideModel.getWindow()).getSelectedHosts();
                        ArrayList<VdcReturnValueBase> retVals =
                                (ArrayList<VdcReturnValueBase>) result.getReturnValue();
                        if (retVals != null && hosts.size() == retVals.size())
                        {
                            int i = 0;
                            for (VDS selectedHost : hosts)
                            {
                                if (selectedHost.getstatus() == VDSStatus.PendingApproval && retVals.get(i) != null
                                        && retVals.get(i).getSucceeded())
                                {
                                    Frontend.RunAction(VdcActionType.ApproveVds,

                                            new ApproveVdsParameters(selectedHost.getId()),
                                            new IFrontendActionAsyncCallback() {
                                                @Override
                                                public void Executed(FrontendActionAsyncResult result) {
                                                }
                                            },
                                            this);
                                }
                            }
                            i++;
                        }
                        dataCenterGuideModel.getWindow().StopProgress();
                        dataCenterGuideModel.Cancel();
                        dataCenterGuideModel.PostAction();
                    }
                },
                this);
    }

    public void AddHost()
    {
        HostModel model = new HostModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        model.setHashName("new_host_guide_me"); //$NON-NLS-1$
        model.getPort().setEntity(54321);
        model.getOverrideIpTables().setEntity(true);
        model.setSpmPriorityValue(null);

        model.getDataCenter()
                .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { getEntity() })));
        model.getDataCenter().setSelectedItem(getEntity());
        model.getDataCenter().setIsChangable(false);

        UICommand tempVar = new UICommand("OnConfirmPMHost", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnConfirmPMHost()
    {
        HostModel model = (HostModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        if (!((Boolean) model.getIsPm().getEntity()))
        {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().powerManagementConfigurationTitle());
            confirmModel.setHashName("power_management_configuration"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance().getConstants().youHavntConfigPmMsg());

            UICommand tempVar = new UICommand("OnAddHost", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmWithFocus", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            confirmModel.getCommands().add(tempVar2);
        }
        else
        {
            OnAddHost();
        }
    }

    public void OnAddHost()
    {
        CancelConfirm();

        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        // Save changes.
        VDS host = new VDS();
        host.setvds_name((String) model.getName().getEntity());
        host.sethost_name((String) model.getHost().getEntity());
        host.setport(Integer.parseInt(model.getPort().getEntity().toString()));
        host.setvds_group_id(((VDSGroup) model.getCluster().getSelectedItem()).getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save primary PM parameters.
        host.setManagmentIp((String) model.getManagementIp().getEntity());
        host.setpm_user((String) model.getPmUserName().getEntity());
        host.setpm_password((String) model.getPmPassword().getEntity());
        host.setpm_type((String) model.getPmType().getSelectedItem());
        host.setPmOptionsMap(new ValueObjectMap(model.getPmOptionsMap(), false));

        // Save secondary PM parameters.
        host.setPmSecondaryIp((String) model.getPmSecondaryIp().getEntity());
        host.setPmSecondaryUser((String) model.getPmSecondaryUserName().getEntity());
        host.setPmSecondaryPassword((String) model.getPmSecondaryPassword().getEntity());
        host.setPmSecondaryType((String) model.getPmSecondaryType().getSelectedItem());
        host.setPmSecondaryOptionsMap(new ValueObjectMap(model.getPmSecondaryOptionsMap(), false));

        // Save other PM parameters.
        host.setpm_enabled((Boolean) model.getIsPm().getEntity());
        host.setPmSecondaryConcurrent((Boolean) model.getPmSecondaryConcurrent().getEntity());


        AddVdsActionParameters addVdsParams = new AddVdsActionParameters();
        addVdsParams.setVdsId(host.getId());
        addVdsParams.setvds(host);
        addVdsParams.setRootPassword((String) model.getRootPassword().getEntity());

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.AddVds, addVdsParams,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel localModel = (DataCenterGuideModel) result.getState();
                        localModel.PostOnAddHost(result.getReturnValue());

                    }
                }, this);
    }

    public void PostOnAddHost(VdcReturnValueBase returnValue)
    {
        HostModel model = (HostModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
            PostAction();
        }
    }

    private void PostAction()
    {
        ResetData();
        UpdateOptions();
    }

    public void Cancel()
    {
        ResetData();
        setWindow(null);
    }

    public void CancelConfirm()
    {
        setConfirmWindow(null);
    }

    public void CancelConfirmWithFocus()
    {
        setConfirmWindow(null);

        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementTabSelected(true);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "AddCluster")) //$NON-NLS-1$
        {
            AddCluster();
        }

        if (StringHelper.stringsEqual(command.getName(), "AddHost")) //$NON-NLS-1$
        {
            AddHost();
        }

        if (StringHelper.stringsEqual(command.getName(), "SelectHost")) //$NON-NLS-1$
        {
            SelectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "AddDataStorage")) //$NON-NLS-1$
        {
            AddDataStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "AttachDataStorage")) //$NON-NLS-1$
        {
            AttachDataStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "AddIsoStorage")) //$NON-NLS-1$
        {
            AddIsoStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "AttachIsoStorage")) //$NON-NLS-1$
        {
            AttachIsoStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddCluster")) //$NON-NLS-1$
        {
            OnAddCluster();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnSelectHost")) //$NON-NLS-1$
        {
            OnSelectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddHost")) //$NON-NLS-1$
        {
            OnAddHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddStorage")) //$NON-NLS-1$
        {
            OnAddStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "OnSaveSanStorage")) //$NON-NLS-1$
        {
            OnSaveSanStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "OnAttachStorage")) //$NON-NLS-1$
        {
            OnAttachStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "AddLocalStorage")) //$NON-NLS-1$
        {
            AddLocalStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "OnConfirmPMHost")) //$NON-NLS-1$
        {
            OnConfirmPMHost();
        }

        if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            CancelConfirm();
        }
        if (StringHelper.stringsEqual(command.getName(), "CancelConfirmWithFocus")) //$NON-NLS-1$
        {
            CancelConfirmWithFocus();
        }

        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    public void run(TaskContext context)
    {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        String key = (String) data.get(0);

        if (StringHelper.stringsEqual(key, "SaveNfs")) //$NON-NLS-1$
        {
            SaveNfsStorage(context);

        }
        else if (StringHelper.stringsEqual(key, "SaveLocal")) //$NON-NLS-1$
        {
            SaveLocalStorage(context);

        }
        else if (StringHelper.stringsEqual(key, "SaveSan")) //$NON-NLS-1$
        {
            SaveSanStorage(context);

        }
        else if (StringHelper.stringsEqual(key, "Finish")) //$NON-NLS-1$
        {
            getWindow().StopProgress();

            if ((Boolean) data.get(1))
            {
                Cancel();
                PostAction();
            }
            else
            {
                ((Model) data.get(2)).setMessage((String) data.get(3));
            }
        }
    }
}
