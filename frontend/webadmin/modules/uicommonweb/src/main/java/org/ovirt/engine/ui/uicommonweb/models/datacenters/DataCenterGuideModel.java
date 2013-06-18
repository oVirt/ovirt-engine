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
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
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
import org.ovirt.engine.ui.uicommonweb.models.hosts.NewHostModel;
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
    private Guid storageId;
    private StorageServerConnections connection;
    private Guid hostId = Guid.Empty;
    private String path;
    private boolean removeConnection;
    private ArrayList<VDSGroup> clusters;
    private ArrayList<StorageDomain> allStorageDomains;
    private ArrayList<StorageDomain> attachedStorageDomains;
    private ArrayList<StorageDomain> isoStorageDomains;
    private ArrayList<VDS> allHosts;
    private VDS localStorageHost;
    private boolean noLocalStorageHost;

    public DataCenterGuideModel() {
    }

    @Override
    public StoragePool getEntity()
    {
        return (StoragePool) super.getEntity();
    }

    public void setEntity(StoragePool value)
    {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        updateOptions();
    }

    private void updateOptionsNonLocalFSData() {
        AsyncDataProvider.getClusterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        dataCenterGuideModel.clusters = clusters;
                        dataCenterGuideModel.updateOptionsNonLocalFS();
                    }
                }), getEntity().getId());

        AsyncDataProvider.getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storageDomains =
                                (ArrayList<StorageDomain>) returnValue;
                        dataCenterGuideModel.allStorageDomains = storageDomains;
                        dataCenterGuideModel.updateOptionsNonLocalFS();
                    }
                }));

        AsyncDataProvider.getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storageDomains =
                                (ArrayList<StorageDomain>) returnValue;
                        dataCenterGuideModel.attachedStorageDomains = storageDomains;
                        dataCenterGuideModel.updateOptionsNonLocalFS();
                    }
                }), getEntity().getId());

        AsyncDataProvider.getISOStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storageDomains =
                                (ArrayList<StorageDomain>) returnValue;
                        dataCenterGuideModel.isoStorageDomains = storageDomains;
                        dataCenterGuideModel.updateOptionsNonLocalFS();
                    }
                }));

        AsyncDataProvider.getHostList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDS> hosts = (ArrayList<VDS>) returnValue;
                        dataCenterGuideModel.allHosts = hosts;
                        dataCenterGuideModel.updateOptionsNonLocalFS();
                    }
                }));
    }

    private void updateOptionsLocalFSData() {
        AsyncDataProvider.getClusterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        dataCenterGuideModel.clusters = clusters;
                        dataCenterGuideModel.updateOptionsLocalFS();
                    }
                }), getEntity().getId());

        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Hosts: datacenter!= " + getEntity().getName() //$NON-NLS-1$
                + " status=maintenance or status=pendingapproval ", SearchType.VDS), new AsyncQuery(this, //$NON-NLS-1$
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<VDS> hosts =
                                (ArrayList<VDS>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        if (hosts == null) {
                            hosts = new ArrayList<VDS>();
                        }
                        dataCenterGuideModel.allHosts = hosts;
                        AsyncDataProvider.getLocalStorageHost(new AsyncQuery(dataCenterGuideModel,
                                new INewAsyncCallback() {
                                    @Override
                                    public void onSuccess(Object target, Object returnValue) {
                                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                                        if (returnValue != null) {
                                        VDS localStorageHost = (VDS) returnValue;
                                        dataCenterGuideModel.localStorageHost = localStorageHost;
                                        } else {
                                            noLocalStorageHost = true;
                                        }
                                        dataCenterGuideModel.updateOptionsLocalFS();
                                    }
                                }), dataCenterGuideModel.getEntity().getName());
                    }
                }));
    }

    private void updateOptionsNonLocalFS() {
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
            if (Linq.isClusterItemExistInList(clusters, vds.getVdsGroupId()))
            {
                hosts.add(vds);
            }

            if ((vds.getStatus() == VDSStatus.Maintenance || vds.getStatus() == VDSStatus.PendingApproval)
                && doesHostSupportAnyCluster(clusters, vds))
            {
                availableHosts.add(vds);
            }

            if (vds.getStatus() == VDSStatus.Up && Linq.isClusterItemExistInList(clusters, vds.getVdsGroupId()))
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


        ArrayList<StorageDomain> unattachedStorage = new ArrayList<StorageDomain>();
        boolean addToList;
        Version version3_0 = new Version(3, 0);
        for (StorageDomain item : allStorageDomains)
        {
            addToList = false;
            if (item.getStorageDomainType() == StorageDomainType.Data
                    && item.getStorageType() == getEntity().getstorage_pool_type()
                    && item.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)
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

        ArrayList<StorageDomain> attachedDataStorages = new ArrayList<StorageDomain>();
        for (StorageDomain a : attachedStorageDomains)
        {
            if (a.getStorageDomainType() == StorageDomainType.Data
                    || a.getStorageDomainType() == StorageDomainType.Master)
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
        ArrayList<StorageDomain> attachedIsoStorages = new ArrayList<StorageDomain>();
        for (StorageDomain sd : attachedStorageDomains)
        {
            if (sd.getStorageDomainType() == StorageDomainType.ISO)
            {
                attachedIsoStorages.add(sd);
            }
        }

        boolean attachIsoAllowed =
                (attachedDataStorages.size() > 0 && Linq.isAnyStorageDomainIsMatserAndActive(attachedDataStorages)
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

        stopProgress();
    }

    private boolean doesHostSupportAnyCluster(List<VDSGroup> clusterList, VDS host){
        for (VDSGroup cluster : clusterList){
            if (host.getSupportedClusterVersionsSet().contains(cluster.getcompatibility_version())){
                return true;
            }
        }
        return false;
    }

    private void updateOptionsLocalFS() {
        if (clusters == null || allHosts == null || (localStorageHost == null && !noLocalStorageHost)) {
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
                String[] hostVersions = vds.getSupportedClusterLevels().split("[,]", -1); //$NON-NLS-1$
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
                if (localStorageHost.getStatus() == VDSStatus.Up)
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

        stopProgress();
    }

    private void updateOptions()
    {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null)
        {
            startProgress(null);

            if (getEntity().getstorage_pool_type() != StorageType.LOCALFS)
            {
                updateOptionsNonLocalFSData();
            }
            else
            {
                updateOptionsLocalFSData();
            }
        }
    }

    private void resetData() {
        storageDomain = null;
        storageModel = null;
        storageId = null;
        connection = null;
        removeConnection = false;
        path = null;
        hostId = Guid.Empty;
        clusters = null;
        allStorageDomains = null;
        attachedStorageDomains = null;
        isoStorageDomains = null;
        allHosts = null;
        localStorageHost = null;
        noLocalStorageHost = false;
    }

    private void addLocalStorage()
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

        AsyncDataProvider.getLocalStorageHost(new AsyncQuery(new Object[] { this, model },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        Object[] array = (Object[]) target;
                        DataCenterGuideModel listModel = (DataCenterGuideModel) array[0];
                        StorageModel model = (StorageModel) array[1];
                        VDS localHost = (VDS) returnValue;

                        model.getHost()
                                .setItems(new ArrayList<VDS>(Arrays.asList(new VDS[] { localHost })));
                        model.getHost().setSelectedItem(localHost);
                        model.getDataCenter()
                                .setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { getEntity() })));
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
                getEntity().getName());
    }

    public void addIsoStorage()
    {
        addStorageInternal(ConstantsManager.getInstance().getConstants().newISOLibraryTitle(), StorageDomainType.ISO);
    }

    public void addDataStorage()
    {
        addStorageInternal(ConstantsManager.getInstance().getConstants().newStorageTitle(), StorageDomainType.Data);
    }

    private void addStorageInternal(String title, StorageDomainType type)
    {
        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(title);
        model.setHashName("new_domain"); //$NON-NLS-1$
        ArrayList<StoragePool> dataCenters = new ArrayList<StoragePool>();
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

        model.initialize();

        UICommand tempVar6 = new UICommand("OnAddStorage", this); //$NON-NLS-1$
        tempVar6.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar6.setIsDefault(true);
        model.getCommands().add(tempVar6);
        UICommand tempVar7 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar7.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar7.setIsCancel(true);
        model.getCommands().add(tempVar7);
    }

    public void onAddStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        String storageName = (String) model.getName().getEntity();

        AsyncDataProvider.isStorageDomainNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

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
                        AsyncDataProvider.getStorageDomainMaxNameLength(new AsyncQuery(dataCenterGuideModel,
                                new INewAsyncCallback() {
                                    @Override
                                    public void onSuccess(Object target1, Object returnValue1) {

                                        DataCenterGuideModel dataCenterGuideModel1 = (DataCenterGuideModel) target1;
                                        StorageModel storageModel1 = (StorageModel) dataCenterGuideModel1.getWindow();
                                        int nameMaxLength = (Integer) returnValue1;
                                        RegexValidation tempVar2 = new RegexValidation();
                                        tempVar2.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$"); //$NON-NLS-1$ //$NON-NLS-2$
                                        tempVar2.setMessage(ConstantsManager.getInstance().getMessages()
                                                .nameCanContainOnlyMsg(nameMaxLength));
                                        storageModel1.getName().validateEntity(new IValidation[] {
                                                new NotEmptyValidation(), tempVar2 });
                                        dataCenterGuideModel1.postOnAddStorage();

                                    }
                                }));

                    }
                }),
                storageName);
    }

    public void postOnAddStorage()
    {
        StorageModel model = (StorageModel) getWindow();

        if (!model.validate())
        {
            return;
        }

        // Save changes.
        if (model.getSelectedItem() instanceof NfsStorageModel)
        {
            saveNfsStorage();
        }
        else if (model.getSelectedItem() instanceof LocalStorageModel)
        {
            saveLocalStorage();
        }
        else
        {
            saveSanStorage();
        }
    }

    private void saveLocalStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().startProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveLocal" }))).Run(); //$NON-NLS-1$
    }

    private void saveLocalStorage(TaskContext context)
    {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        VDS host = (VDS) model.getHost().getSelectedItem();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getSelectedItem();
        LocalStorageModel localModel = (LocalStorageModel) storageModel;
        path = (String) localModel.getPath().getEntity();

        storageDomain = new StorageDomainStatic();
        storageDomain.setStorageType(isNew ? storageModel.getType() : storageDomain.getStorageType());
        storageDomain.setStorageDomainType(isNew ? storageModel.getRole() : storageDomain.getStorageDomainType());
        storageDomain.setStorageName((String) model.getName().getEntity());

        AsyncDataProvider.getStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storages =
                                (ArrayList<StorageDomain>) returnValue;
                        if (storages != null && storages.size() > 0)
                        {
                            String storageName = storages.get(0).getStorageName();
                            onFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else
                        {
                            dataCenterGuideModel.saveNewLocalStorage();
                        }

                    }
                }),
                host.getStoragePoolId(),
                path);
    }

    public void saveNewLocalStorage()
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
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.removeConnection = true;

                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageDomain.setStorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.removeConnection = false;

                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, true, dataCenterGuideModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();

                if (dataCenterGuideModel.removeConnection)
                {
                    dataCenterGuideModel.cleanConnection(dataCenterGuideModel.connection, dataCenterGuideModel.hostId);
                    dataCenterGuideModel.removeConnection = false;
                }

                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, false, dataCenterGuideModel.storageModel);

            }
        };
        Frontend.RunMultipleActions(actionTypes,
                parameters,
                new ArrayList<IFrontendActionAsyncCallback>(Arrays.asList(new IFrontendActionAsyncCallback[] {
                        callback1, callback2 })),
                failureCallback,
                this);
    }

    private void cleanConnection(StorageServerConnections connection, Guid hostId)
    {
        Frontend.RunAction(VdcActionType.DisconnectStorageServerConnection,
                new StorageServerConnectionParametersBase(connection, hostId),
                null,
                this);
    }

    public void onFinish(TaskContext context, boolean isSucceeded, IStorageModel model)
    {
        onFinish(context, isSucceeded, model, null);
    }

    public void onFinish(TaskContext context, boolean isSucceeded, IStorageModel model, String message)
    {
        context.InvokeUIThread(this,
                new ArrayList<Object>(Arrays.asList(new Object[] { "Finish", isSucceeded, model, //$NON-NLS-1$
                        message })));
    }

    private void saveNfsStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().startProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveNfs" }))).Run(); //$NON-NLS-1$
    }

    private void saveNfsStorage(TaskContext context)
    {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getSelectedItem();
        NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
        path = (String) nfsModel.getPath().getEntity();

        storageDomain = new StorageDomainStatic();
        storageDomain.setStorageType(isNew ? storageModel.getType() : storageDomain.getStorageType());
        storageDomain.setStorageDomainType(isNew ? storageModel.getRole() : storageDomain.getStorageDomainType());
        storageDomain.setStorageName((String) model.getName().getEntity());
        storageDomain.setStorageFormat((StorageFormatType) model.getFormat().getSelectedItem());

        AsyncDataProvider.getStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storages =
                                (ArrayList<StorageDomain>) returnValue;
                        if (storages != null && storages.size() > 0)
                        {
                            String storageName = storages.get(0).getStorageName();
                            onFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else
                        {
                            dataCenterGuideModel.saveNewNfsStorage();
                        }

                    }
                }),
                null,
                path);
    }

    public void saveNewNfsStorage()
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
        actionTypes.add(VdcActionType.DisconnectStorageServerConnection);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);
        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageDomain.setStorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageId = (Guid) vdcReturnValueBase.getActionReturnValue();

            }
        };
        IFrontendActionAsyncCallback callback3 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();

                // Attach storage to data center as neccessary.
                StoragePool dataCenter = (StoragePool) storageModel.getDataCenter().getSelectedItem();
                if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                {
                    dataCenterGuideModel.attachStorageToDataCenter(dataCenterGuideModel.storageId,
                            dataCenter.getId());
                }

                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, true, dataCenterGuideModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.cleanConnection(dataCenterGuideModel.connection, dataCenterGuideModel.hostId);
                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, false, dataCenterGuideModel.storageModel);

            }
        };
        Frontend.RunMultipleActions(actionTypes,
                parameters,
                new ArrayList<IFrontendActionAsyncCallback>(Arrays.asList(new IFrontendActionAsyncCallback[] {
                        callback1, callback2, callback3 })),
                failureCallback,
                this);
    }

    private void saveSanStorage()
    {
        if (getWindow().getProgress() != null)
        {
            return;
        }

        getWindow().startProgress(null);

        Task.Create(this, new ArrayList<Object>(Arrays.asList(new Object[] { "SaveSan" }))).Run(); //$NON-NLS-1$
    }

    private void saveSanStorage(TaskContext context)
    {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getSelectedItem();

        storageDomain = new StorageDomainStatic();
        storageDomain.setStorageType(sanModel.getType());
        storageDomain.setStorageDomainType(sanModel.getRole());
        storageDomain.setStorageFormat((StorageFormatType) sanModel.getContainer().getFormat().getSelectedItem());
        storageDomain.setStorageName((String) model.getName().getEntity());

        AsyncDataProvider.getStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storages =
                                (ArrayList<StorageDomain>) returnValue;
                        if (storages != null && storages.size() > 0)
                        {
                            String storageName = storages.get(0).getStorageName();
                            onFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else
                        {
                            dataCenterGuideModel.saveNewSanStorage();
                        }

                        dataCenterGuideModel.getWindow().stopProgress();
                    }
                }),
                null,
                path);
    }

    public void saveNewSanStorage()
    {
        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanStorageModel = (SanStorageModel) model.getSelectedItem();

        ArrayList<String> usedLunsMessages = sanStorageModel.getUsedLunsMessages();

        if (usedLunsMessages.isEmpty()) {
            onSaveSanStorage();
        }
        else {
            forceCreationWarning(usedLunsMessages);
        }
    }

    private void onSaveSanStorage()
    {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();

        if (confirmationModel != null && !confirmationModel.validate())
        {
            return;
        }

        cancelConfirm();
        getWindow().startProgress(null);

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
                    public void executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                        StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();
                        StoragePool dataCenter = (StoragePool) storageModel.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
                        {
                            VdcReturnValueBase returnValue = result.getReturnValue();
                            Guid storageId = (Guid) returnValue.getActionReturnValue();
                            dataCenterGuideModel.attachStorageToDataCenter(storageId, dataCenter.getId());
                        }
                        dataCenterGuideModel.onFinish(dataCenterGuideModel.context,
                                true,
                                dataCenterGuideModel.storageModel);

                    }
                }, this);
    }

    private void forceCreationWarning(ArrayList<String> usedLunsMessages) {
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

    private void attachStorageInternal(List<StorageDomain> storages, String title)
    {
        ListModel model = new ListModel();
        model.setTitle(title);
        setWindow(model);

        ArrayList<EntityModel> items = new ArrayList<EntityModel>();
        for (StorageDomain sd : storages)
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

    private void attachStorageToDataCenter(Guid storageId, Guid dataCenterId)
    {
        Frontend.RunAction(VdcActionType.AttachStorageDomainToPool, new StorageDomainPoolParametersBase(storageId,
                dataCenterId),
                null,
                this);
    }

    public void onAttachStorage()
    {
        ListModel model = (ListModel) getWindow();

        ArrayList<StorageDomain> items = new ArrayList<StorageDomain>();
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems()))
        {
            if (a.getIsSelected())
            {
                items.add((StorageDomain) a.getEntity());
            }
        }

        if (items.size() > 0)
        {
            for (StorageDomain sd : items)
            {
                attachStorageToDataCenter(sd.getId(), getEntity().getId());
            }
        }

        cancel();
        postAction();
    }

    public void attachIsoStorage()
    {
        AsyncDataProvider.getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> attachedStorage =
                                new ArrayList<StorageDomain>();

                        AsyncDataProvider.getISOStorageDomainList(new AsyncQuery(new Object[] { dataCenterGuideModel,
                                attachedStorage },
                                new INewAsyncCallback() {
                                    @Override
                                    public void onSuccess(Object target, Object returnValue) {
                                        Object[] array = (Object[]) target;
                                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) array[0];
                                        ArrayList<StorageDomain> attachedStorage =
                                                (ArrayList<StorageDomain>) array[1];
                                        ArrayList<StorageDomain> isoStorageDomains =
                                                (ArrayList<StorageDomain>) returnValue;
                                        ArrayList<StorageDomain> sdl =
                                                new ArrayList<StorageDomain>();

                                        for (StorageDomain a : isoStorageDomains)
                                        {
                                            boolean isContains = false;
                                            for (StorageDomain b : attachedStorage)
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
                                        dataCenterGuideModel.attachStorageInternal(sdl, ConstantsManager.getInstance()
                                                .getConstants()
                                                .attachISOLibraryTitle());
                                    }
                                }));
                    }
                }),
                getEntity().getId());
    }

    public void attachDataStorage()
    {
        AsyncDataProvider.getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storageDomains =
                                (ArrayList<StorageDomain>) returnValue;

                        ArrayList<StorageDomain> unattachedStorage =
                                new ArrayList<StorageDomain>();
                        boolean addToList;
                        Version version3_0 = new Version(3, 0);
                        for (StorageDomain item : storageDomains)
                        {
                            addToList = false;
                            if (item.getStorageDomainType() == StorageDomainType.Data
                                    && item.getStorageType() == dataCenterGuideModel.getEntity()
                                            .getstorage_pool_type()
                                    && item.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)
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
                        dataCenterGuideModel.attachStorageInternal(unattachedStorage, ConstantsManager.getInstance()
                                .getConstants()
                                .attachStorageTitle());
                    }
                }));
    }

    public void addCluster()
    {
        ClusterModel model = new ClusterModel();
        model.init(false);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newClusterTitle());
        model.setHashName("new_cluster"); //$NON-NLS-1$
        model.setIsNew(true);

        ArrayList<StoragePool> dataCenters = new ArrayList<StoragePool>();
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

    public void onAddCluster()
    {
        ClusterModel model = (ClusterModel) getWindow();
        VDSGroup cluster = new VDSGroup();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.validate((Boolean) model.getEnableOvirtService().getEntity())) // CPU is mandatory only if the
                                                                                  // cluster is virt enabled
        {
            return;
        }

        // Save changes.
        Version version = (Version) model.getVersion().getSelectedItem();

        cluster.setName((String) model.getName().getEntity());
        cluster.setdescription((String) model.getDescription().getEntity());
        cluster.setStoragePoolId(((StoragePool) model.getDataCenter().getSelectedItem()).getId());
        if (model.getCPU().getSelectedItem() != null)
        {
            cluster.setcpu_name(((ServerCpu) model.getCPU().getSelectedItem()).getCpuName());
        }
        cluster.setmax_vds_memory_over_commit(model.getMemoryOverCommit());
        cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0); //$NON-NLS-1$
        cluster.setcompatibility_version(version);
        cluster.setVirtService((Boolean) model.getEnableOvirtService().getEntity());
        cluster.setGlusterService((Boolean) model.getEnableGlusterService().getEntity());

        model.startProgress(null);

        Frontend.RunAction(VdcActionType.AddVdsGroup, new VdsGroupOperationParameters(cluster),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel localModel = (DataCenterGuideModel) result.getState();
                        localModel.postOnAddCluster(result.getReturnValue());

                    }
                }, this);
    }

    public void postOnAddCluster(VdcReturnValueBase returnValue)
    {
        ClusterModel model = (ClusterModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();
            postAction();
        }
    }

    public void selectHost()
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

        AsyncDataProvider.getClusterList(new AsyncQuery(new Object[] { this, model },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        Object[] array = (Object[]) target;
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) array[0];
                        MoveHost moveHostModel = (MoveHost) array[1];
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;

                        moveHostModel.getCluster().setItems(clusters);
                        moveHostModel.getCluster().setSelectedItem(Linq.firstOrDefault(clusters));

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

    public void onSelectHost()
    {
        MoveHost model = (MoveHost) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.validate())
        {
            return;
        }

        model.setSelectedHosts(new ArrayList<VDS>());
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems()))
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
            if (host.getVdsGroupId() != null && !host.getVdsGroupId().equals(cluster.getId()))
            {
                paramerterList.add(new ChangeVDSClusterParameters(cluster.getId(), host.getId()));

            }
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ChangeVDSCluster, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

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
                                if (selectedHost.getStatus() == VDSStatus.PendingApproval && retVals.get(i) != null
                                        && retVals.get(i).getSucceeded())
                                {
                                    Frontend.RunAction(VdcActionType.ApproveVds,

                                            new ApproveVdsParameters(selectedHost.getId()),
                                            null,
                                            this);
                                }
                                i++;
                            }
                        }
                        dataCenterGuideModel.getWindow().stopProgress();
                        dataCenterGuideModel.cancel();
                        dataCenterGuideModel.postAction();
                    }
                },
                this);
    }

    public void addHost()
    {
        HostModel model = new NewHostModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        model.setHashName("new_host_guide_me"); //$NON-NLS-1$
        model.getPort().setEntity(54321);
        model.getOverrideIpTables().setEntity(true);
        model.setSpmPriorityValue(null);

        model.getDataCenter()
                .setItems(new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { getEntity() })));
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

    public void onConfirmPMHost()
    {
        HostModel model = (HostModel) getWindow();

        if (!model.validate())
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
            onAddHost();
        }
    }

    public void onAddHost()
    {
        cancelConfirm();

        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        // Save changes.
        VDS host = new VDS();
        host.setVdsName((String) model.getName().getEntity());
        host.setHostName((String) model.getHost().getEntity());
        host.setPort(Integer.parseInt(model.getPort().getEntity().toString()));
        host.setVdsGroupId(((VDSGroup) model.getCluster().getSelectedItem()).getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save primary PM parameters.
        host.setManagementIp((String) model.getManagementIp().getEntity());
        host.setPmUser((String) model.getPmUserName().getEntity());
        host.setPmPassword((String) model.getPmPassword().getEntity());
        host.setPmType((String) model.getPmType().getSelectedItem());
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
        addVdsParams.setRebootAfterInstallation(((VDSGroup) model.getCluster().getSelectedItem()).supportsVirtService());
        model.startProgress(null);

        Frontend.RunAction(VdcActionType.AddVds, addVdsParams,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel localModel = (DataCenterGuideModel) result.getState();
                        localModel.postOnAddHost(result.getReturnValue());

                    }
                }, this);
    }

    public void postOnAddHost(VdcReturnValueBase returnValue)
    {
        HostModel model = (HostModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();
            postAction();
        }
    }

    private void postAction()
    {
        resetData();
        updateOptions();
    }

    public void cancel()
    {
        resetData();
        setWindow(null);
    }

    public void cancelConfirm()
    {
        setConfirmWindow(null);
    }

    public void cancelConfirmWithFocus()
    {
        setConfirmWindow(null);

        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementTabSelected(true);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "AddCluster")) //$NON-NLS-1$
        {
            addCluster();
        }

        if (StringHelper.stringsEqual(command.getName(), "AddHost")) //$NON-NLS-1$
        {
            addHost();
        }

        if (StringHelper.stringsEqual(command.getName(), "SelectHost")) //$NON-NLS-1$
        {
            selectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "AddDataStorage")) //$NON-NLS-1$
        {
            addDataStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "AttachDataStorage")) //$NON-NLS-1$
        {
            attachDataStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "AddIsoStorage")) //$NON-NLS-1$
        {
            addIsoStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "AttachIsoStorage")) //$NON-NLS-1$
        {
            attachIsoStorage();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddCluster")) //$NON-NLS-1$
        {
            onAddCluster();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnSelectHost")) //$NON-NLS-1$
        {
            onSelectHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddHost")) //$NON-NLS-1$
        {
            onAddHost();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAddStorage")) //$NON-NLS-1$
        {
            onAddStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "OnSaveSanStorage")) //$NON-NLS-1$
        {
            onSaveSanStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "OnAttachStorage")) //$NON-NLS-1$
        {
            onAttachStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "AddLocalStorage")) //$NON-NLS-1$
        {
            addLocalStorage();
        }

        if (StringHelper.stringsEqual(command.getName(), "OnConfirmPMHost")) //$NON-NLS-1$
        {
            onConfirmPMHost();
        }

        if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            cancelConfirm();
        }
        if (StringHelper.stringsEqual(command.getName(), "CancelConfirmWithFocus")) //$NON-NLS-1$
        {
            cancelConfirmWithFocus();
        }

        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
    }

    @Override
    public void run(TaskContext context)
    {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        String key = (String) data.get(0);

        if (StringHelper.stringsEqual(key, "SaveNfs")) //$NON-NLS-1$
        {
            saveNfsStorage(context);

        }
        else if (StringHelper.stringsEqual(key, "SaveLocal")) //$NON-NLS-1$
        {
            saveLocalStorage(context);

        }
        else if (StringHelper.stringsEqual(key, "SaveSan")) //$NON-NLS-1$
        {
            saveSanStorage(context);

        }
        else if (StringHelper.stringsEqual(key, "Finish")) //$NON-NLS-1$
        {
            getWindow().stopProgress();

            if ((Boolean) data.get(1))
            {
                cancel();
                postAction();
            }
            else
            {
                ((Model) data.get(2)).setMessage((String) data.get(3));
            }
        }
    }
}
