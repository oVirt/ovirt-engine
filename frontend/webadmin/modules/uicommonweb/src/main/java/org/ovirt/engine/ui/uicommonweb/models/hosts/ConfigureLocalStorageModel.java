package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LocalStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class ConfigureLocalStorageModel extends Model {

    private LocalStorageModel privateStorage;

    public LocalStorageModel getStorage() {
        return privateStorage;
    }

    private void setStorage(LocalStorageModel value) {
        privateStorage = value;
    }

    private DataCenterModel privateDataCenter;

    public DataCenterModel getDataCenter() {
        return privateDataCenter;
    }

    private void setDataCenter(DataCenterModel value) {
        privateDataCenter = value;
    }

    private ClusterModel privateCluster;

    public ClusterModel getCluster() {
        return privateCluster;
    }

    private void setCluster(ClusterModel value) {
        privateCluster = value;
    }

    private EntityModel privateFormattedStorageName;

    public EntityModel getFormattedStorageName() {
        return privateFormattedStorageName;
    }

    private void setFormattedStorageName(EntityModel value) {
        privateFormattedStorageName = value;
    }

    private storage_pool candidateDataCenter;

    public storage_pool getCandidateDataCenter() {
        return candidateDataCenter;
    }

    public void setCandidateDataCenter(storage_pool value) {
        candidateDataCenter = value;
    }

    private VDSGroup candidateCluster;

    public VDSGroup getCandidateCluster() {
        return candidateCluster;
    }

    public void setCandidateCluster(VDSGroup value) {
        candidateCluster = value;
    }

    private String privateCommonName;

    private String getCommonName() {
        return privateCommonName;
    }

    private void setCommonName(String value) {
        privateCommonName = value;
    }

    private boolean isGeneralTabValid;

    public boolean getIsGeneralTabValid() {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value) {

        if (isGeneralTabValid != value) {
            isGeneralTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid"));
        }
    }

    private String frontendHash = getHashName() + new Date();

    public ConfigureLocalStorageModel() {

        setStorage(new LocalStorageModel());

        setDataCenter(new DataCenterModel());
        getDataCenter().getVersion().getSelectedItemChangedEvent().addListener(this);

        setCluster(new ClusterModel());
        getCluster().Init(false);

        setFormattedStorageName(new EntityModel());

        // Subscribe to the Frontend events.
        Frontend.getQueryStartedEvent().addListener(this);
        Frontend.getQueryCompleteEvent().addListener(this);
        Frontend.Subscribe(new VdcQueryType[] {
            VdcQueryType.Search,
            VdcQueryType.GetVdsGroupsByStoragePoolId,
            VdcQueryType.GetAllVdsGroups,
            VdcQueryType.GetVdsGroupById,
            VdcQueryType.GetStoragePoolById,
        });


        // Set the storage type to be Local.
        ListModel storageTypeListModel = getDataCenter().getStorageTypeList();

        for (StorageType item : (ArrayList<StorageType>) storageTypeListModel.getItems()) {
            if (item == StorageType.LOCALFS) {
                storageTypeListModel.setSelectedItem(item);
                break;
            }
        }


        setIsGeneralTabValid(true);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getDataCenter().getVersion()) {
            DataCenterVersion_SelectedItemChanged();
        } else if (ev.equals(Frontend.QueryStartedEventDefinition) && StringHelper.stringsEqual(Frontend.getCurrentContext(), frontendHash)) {
            Frontend_QueryStarted();
        } else if (ev.equals(Frontend.QueryCompleteEventDefinition) && StringHelper.stringsEqual(Frontend.getCurrentContext(), frontendHash)) {
            Frontend_QueryComplete();
        }
    }

    private void DataCenterVersion_SelectedItemChanged() {
        Version version = (Version) getDataCenter().getVersion().getSelectedItem();

        // Keep in sync version for data center and cluster.
        getCluster().getVersion().setSelectedItem(version);
    }

    public boolean Validate() {

        RegexValidation validation = new RegexValidation();
        validation.setExpression("^[A-Za-z0-9_-]+$");
        validation.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.");
        getFormattedStorageName().ValidateEntity(new IValidation[] {validation});

        if (getFormattedStorageName().getEntity() != null
            && Linq.FirstOrDefault(context.storageList, new Linq.StorageNamePredicate((String) getFormattedStorageName().getEntity())) != null) {

            getFormattedStorageName().setIsValid(false);
            getFormattedStorageName().getInvalidityReasons().add("Name must be unique.");
        }

        boolean isStorageValid = getStorage().Validate() && getFormattedStorageName().getIsValid();
        boolean isDataCenterValid = true;
        if (getCandidateDataCenter() == null) {
            isDataCenterValid = getDataCenter().Validate();
        }
        boolean isClusterValid = true;
        if (getCandidateCluster() == null) {
            isClusterValid = getCluster().Validate(false);
        }

        setIsGeneralTabValid(isStorageValid && isDataCenterValid && isClusterValid);

        return isStorageValid && isDataCenterValid && isClusterValid;
    }

    private void SetDefaultNames8() {

        VDS host = context.host;
        ArrayList<storage_pool> dataCenters = context.dataCenterList;
        ArrayList<VDSGroup> clusters = context.clusterList;

        setCommonName(StringFormat.format("%1$s-Local", host.getvds_name().replace('.', '-')));


        storage_pool candidate = null;

        // Check if current settings suitable for local setup (in case just SD creation failed - re-using the same setup)
        boolean useCurrentSettings = false;
        if (host.getstorage_pool_id() != null) {

            storage_pool tempCandidate = context.hostDataCenter;
            if (IsLocalDataCenterEmpty(tempCandidate)) {

                candidate = tempCandidate;
                useCurrentSettings = true;
            } else {

                if (tempCandidate != null && tempCandidate.getstorage_pool_type() == StorageType.LOCALFS) {
                    setMessage("Note: Local Storage is already configured for this Host. The Host belongs to " + host.getstorage_pool_name() + " with local Storage Domain. If OK is clicked - this Host will be moved to a new Data Center, and a new Local Storage Domain will be created. Hit Cancel to abort the operation.");
                }
            }
        }

        // Check if there is other DC suitable for re-use
        if (candidate == null) {
            for (storage_pool dataCenter : dataCenters) {

                // Need to check if the new DC is without host.
                if (IsLocalDataCenterEmpty(dataCenter) && context.localStorageHostByDataCenterMap.get(dataCenter) == null) {
                    candidate = dataCenter;
                    break;
                }
            }
        }

        ArrayList<String> names;

        // In case we found a suitable candidate for re-use:
        if (candidate != null) {

            getDataCenter().setDataCenterId(candidate.getId());
            getDataCenter().getName().setEntity(candidate.getname());
            getDataCenter().getDescription().setEntity(candidate.getdescription());

            Version version = candidate.getcompatibility_version();
            getDataCenter().getVersion().setSelectedItem(version);
            getCluster().getVersion().setSelectedItem(version);

            setCandidateDataCenter(candidate);

            // If we use current settings there is no need to create cluster.
            if (useCurrentSettings) {

                getCluster().setClusterId(host.getvds_group_id().getValue());
                getCluster().getName().setEntity(host.getvds_group_name());

                VDSGroup cluster = context.hostCluster;
                if (cluster != null) {

                    getCluster().getDescription().setEntity(cluster.getdescription());

                    ServerCpu cpu = new ServerCpu();
                    cpu.setCpuName(cluster.getcpu_name());

                    getCluster().getCPU().setSelectedItem(cpu);
                }

                setCandidateCluster(cluster);
            }
            // Use different cluster
            else {

                // Check the DC cluster list (for re-use)
                clusters = context.clusterListByDataCenterMap.get(candidate);

                // No clusters available - pick up new name.
                if (clusters == null || clusters.isEmpty()) {

                    names = new ArrayList<String>();

                    ArrayList<VDSGroup> listClusters = context.clusterList;
                    for (VDSGroup cluster : listClusters) {
                        names.add(cluster.getname());
                    }

                    getCluster().getName().setEntity(AvailableName(names));
                } else {

                    // Use the DC cluster.
                    VDSGroup cluster = Linq.FirstOrDefault(clusters);

                    getCluster().setClusterId(cluster.getId());
                    getCluster().getName().setEntity(cluster.getname());
                    getCluster().getDescription().setEntity(cluster.getdescription());

                    cluster = Linq.FirstOrDefault(context.clusterList, new Linq.ClusterPredicate(getCluster().getClusterId().getValue()));
                    if (cluster != null) {

                        ServerCpu cpu = new ServerCpu();
                        cpu.setCpuName(cluster.getcpu_name());

                        getCluster().getCPU().setSelectedItem(cpu);
                    }

                    setCandidateCluster(cluster);
                }
            }
        } else {

            // Didn't found DC to re-use, so we select new names.
            names = new ArrayList<String>();

            for (storage_pool dataCenter : dataCenters) {
                names.add(dataCenter.getname());
            }

            getDataCenter().getName().setEntity(AvailableName(names));


            // Choose a Data Center version corresponding to the host.
            if (!StringHelper.isNullOrEmpty(host.getsupported_cluster_levels())) {

                // The supported_cluster_levels are sorted.
                String[] array = host.getsupported_cluster_levels().split("[,]", -1);
                Version maxVersion = null;

                for (int i = 0; i < array.length; i++) {

                    Version vdsVersion = new Version(array[i]);

                    for (Version version : (List<Version>) getDataCenter().getVersion().getItems()) {

                        if (version.equals(vdsVersion) && version.compareTo(maxVersion) > 0) {
                            maxVersion = version;
                        }
                    }
                }

                if (maxVersion != null) {
                    getDataCenter().getVersion().setSelectedItem(maxVersion);
                    getCluster().getVersion().setSelectedItem(maxVersion);
                }
            }

            names = new ArrayList<String>();
            if (clusters == null) {
                clusters = context.clusterList;
            }

            for (VDSGroup cluster : clusters) {
                names.add(cluster.getname());
            }
            getCluster().getName().setEntity(AvailableName(names));
        }

        // Choose default CPU name to match host.
        if (host.getCpuName() != null && getCluster().getCPU().getSelectedItem() != null) {
            getCluster().getCPU().setSelectedItem(Linq.FirstOrDefault((List<ServerCpu>) getCluster().getCPU().getItems(), new Linq.ServerCpuPredicate(host.getCpuName().getCpuName())));
        }

        // Always choose a available storage name.
        ArrayList<storage_domains> storages = context.storageList;
        names = new ArrayList<String>();

        for (storage_domains storageDomain : storages) {
            names.add(storageDomain.getstorage_name());
        }
        getFormattedStorageName().setEntity(AvailableName(names));
    }

    private void SetDefaultNames7() {

        // Get all clusters.
        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
            new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {

                    context.storageList = (ArrayList<storage_domains>) returnValue;
                    SetDefaultNames8();
                }
            },
            frontendHash));
    }

    public void SetDefaultNames6() {

        // Fill map of local storage host by data center.

        context.clusterListByDataCenterMap = new HashMap<storage_pool, ArrayList<VDSGroup>>();

        AsyncIterator<storage_pool> iterator = new AsyncIterator<storage_pool>(context.dataCenterList);

        iterator.setComplete(
            new AsyncIteratorComplete<storage_pool>() {
                @Override
                public void run(storage_pool item, Object value) {

                    SetDefaultNames7();
                }
            });

        iterator.Iterate(
            new AsyncIteratorFunc<storage_pool>() {
                @Override
                public void run(storage_pool item, AsyncIteratorCallback callback) {

                    AsyncDataProvider.GetClusterList(callback.getAsyncQuery(), item.getId());
                }
            },
            new AsyncIteratorPredicate<storage_pool>() {
                @Override
                public boolean match(storage_pool item, Object value) {

                    context.clusterListByDataCenterMap.put(item, (ArrayList<VDSGroup>) value);
                    return false;
                }
            },
            frontendHash
        );
    }

    public void SetDefaultNames5() {

        // Fill map of local storage host by data center.

        context.localStorageHostByDataCenterMap = new HashMap<storage_pool, VDS>();

        AsyncIterator<storage_pool> iterator = new AsyncIterator<storage_pool>(context.dataCenterList);

        iterator.setComplete(
            new AsyncIteratorComplete<storage_pool>() {
                @Override
                public void run(storage_pool item, Object value) {

                    SetDefaultNames6();
                }
            });

        iterator.Iterate(
            new AsyncIteratorFunc<storage_pool>() {
                @Override
                public void run(storage_pool item, AsyncIteratorCallback callback) {

                    AsyncDataProvider.GetLocalStorageHost(callback.getAsyncQuery(), item.getname());
                }
            },
            new AsyncIteratorPredicate<storage_pool>() {
                @Override
                public boolean match(storage_pool item, Object value) {

                    context.localStorageHostByDataCenterMap.put(item, (VDS) value);
                    return false;
                }
            },
            frontendHash
        );
    }

    public void SetDefaultNames4() {

        // Get data centers containing 'local' in name.
        AsyncDataProvider.GetDataCenterListByName(new AsyncQuery(null,
            new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {

                    context.dataCenterList = (ArrayList<storage_pool>) returnValue;
                    SetDefaultNames5();
                }
            },
            frontendHash),
            getCommonName() + "*");
    }

    public void SetDefaultNames3() {

        // Get all clusters.
        AsyncDataProvider.GetClusterList(new AsyncQuery(this,
            new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {

                    context.clusterList = (ArrayList<VDSGroup>) returnValue;
                    SetDefaultNames4();
                }
            },
            frontendHash));
    }

    public void SetDefaultNames2() {

        VDS host = context.host;

        // Get cluster of the host.
        if (host.getvds_group_id() != null) {
            AsyncDataProvider.GetClusterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        context.hostCluster = (VDSGroup) returnValue;
                        SetDefaultNames3();
                    }
                },
                frontendHash),
                host.getvds_group_id());
        } else {
            SetDefaultNames3();
        }
    }

    public void SetDefaultNames1() {

        VDS host = context.host;

        // Get data center of the host.
        if (host.getstorage_pool_id() != null) {
            AsyncDataProvider.GetDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        context.hostDataCenter = (storage_pool) returnValue;
                        SetDefaultNames2();
                    }
                },
                frontendHash),
                host.getstorage_pool_id());
        } else {
            SetDefaultNames2();
        }
    }

    public void SetDefaultNames(VDS host) {

        context.host = host;

        setCommonName(StringFormat.format("%1$s-Local", host.getvds_name().replace('.', '-')));

        SetDefaultNames1();

        //        String message = null;
        //
        //        setCommonName(StringFormat.format("%1$s-Local", host.getvds_name().replace('.', '-')));
        //        storage_pool candidate = null;
        //
        //        //Select all possible DCs
        //        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, new SearchParameters(StringFormat.format("DataCenter: name=%1$s", getCommonName() + "*"), SearchType.StoragePool));
        //
        //        java.util.ArrayList<storage_pool> dataCenterList = new java.util.ArrayList<storage_pool>();
        //        java.util.ArrayList<VDSGroup> clusterList = null;
        //        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        //        {
        //            dataCenterList = Linq.<storage_pool>Cast((java.util.ArrayList<IVdcQueryable>)returnValue.getReturnValue());
        //        }
        //        //Check if current settings suitable for local setup (in case just SD creation failed - re-using the same setup)
        //        boolean useCurrentSettings = false;
        //        if (host.getstorage_pool_id() != null)
        //        {
        //            storage_pool tempCandidate = DataProvider.GetDataCenterById(host.getstorage_pool_id());
        //            if (IsLocalDataCenterEmpty(tempCandidate))
        //            {
        //                candidate = tempCandidate;
        //                useCurrentSettings = true;
        //            }
        //            else
        //            {
        //                if (tempCandidate != null && tempCandidate.getstorage_pool_type() == StorageType.LOCALFS)
        //                {
        //                    message = "Note: Local Storage is already configured for this Host. The Host belongs to " + host.getstorage_pool_name() + " with local Storage Domain. If OK is clicked - this Host will be moved to a new Data Center, and a new Local Storage Domain will be created. Hit Cancel to abort the operation.";
        //                }
        //            }
        //        }
        //
        //        //Check if there is other DC suitable for re-use
        //        if (candidate == null)
        //        {
        //            for (storage_pool dataCenter : dataCenterList)
        //            {
        //                //Need to check if the new DC is without host.
        //                if (IsLocalDataCenterEmpty(dataCenter) && DataProvider.GetLocalStorageHost(dataCenter.getname()) == null)
        //                {
        //                    candidate = dataCenter;
        //                    break;
        //                }
        //            }
        //        }
        //        java.util.ArrayList<String> listNames = new java.util.ArrayList<String>();
        //        //In case we found a suitable candidate for re-use:
        //        if(candidate != null)
        //        {
        //            getDataCenter().setDataCenterId(candidate.getId());
        //            getDataCenter().getName().setEntity(candidate.getname());
        //            getDataCenter().getDescription().setEntity(candidate.getdescription());
        //            Version compVersion = candidate.getcompatibility_version();
        //            getDataCenter().getVersion().setSelectedItem(compVersion);
        //            getCluster().getVersion().setSelectedItem(compVersion);
        //            setDontCreateDataCenter(true);
        //            //If we use current settings there is no need to create cluster.
        //            if (useCurrentSettings)
        //            {
        //                getCluster().setClusterId(host.getvds_group_id().getValue());
        //                getCluster().getName().setEntity(host.getvds_group_name());
        //                VDSGroup cluster = DataProvider.GetClusterById(host.getvds_group_id().getValue());
        //                if(cluster != null)
        //                {
        //                    getCluster().getDescription().setEntity(cluster.getdescription());
        //                    ServerCpu tempVar = new ServerCpu();
        //                    tempVar.setCpuName(cluster.getcpu_name());
        //                    getCluster().getCPU().setSelectedItem(tempVar);
        //                }
        //                setDontCreateCluster(true);
        //                setDontChangeHostCluster(true);
        //            }
        //            //Use different cluster
        //            else
        //            {
        //                //Check the DC cluster list (for re-use)
        //                clusterList = DataProvider.GetClusterList(candidate.getId());
        //                //No clusters available - pick up new name.
        //                if(clusterList.isEmpty())
        //                {
        //                    java.util.ArrayList<VDSGroup> listClusters = DataProvider.GetClusterList();
        //                    listNames = new java.util.ArrayList<String>();
        //                    for (VDSGroup cluster : listClusters)
        //                    {
        //                        listNames.add(cluster.getname());
        //                    }
        //                    getCluster().getName().setEntity(AvailableName(listNames));
        //                }
        //                else
        //                {
        //                    //Use the DC cluster.
        //                    getCluster().setClusterId(clusterList.get(0).getID());
        //                    getCluster().getName().setEntity(clusterList.get(0).getname());
        //                    getCluster().getDescription().setEntity(clusterList.get(0).getdescription());
        //                    VDSGroup cluster = DataProvider.GetClusterById(getCluster().getClusterId().getValue());
        //                    if (cluster != null)
        //                    {
        //                        ServerCpu tempVar2 = new ServerCpu();
        //                        tempVar2.setCpuName(cluster.getcpu_name());
        //                        getCluster().getCPU().setSelectedItem(tempVar2);
        //                    }
        //                    setDontCreateCluster(true);
        //                    if (host.getvds_group_id().getValue().equals(getCluster().getClusterId()))
        //                    {
        //                        setDontChangeHostCluster(true);
        //                    }
        //                }
        //            }
        //        }
        //        else
        //        {
        //            //Didn't found DC to re-use, so we select new names:
        //            listNames = new java.util.ArrayList<String>();
        //            for (storage_pool storagePool : dataCenterList)
        //            {
        //                listNames.add(storagePool.getname());
        //            }
        //            getDataCenter().getName().setEntity(AvailableName(listNames));
        //
        //            //Choose a Data Center version corresponding to the host.
        //            if (!StringHelper.isNullOrEmpty(host.getsupported_cluster_levels()))
        //            {
        //                //The supported_cluster_levels are sorted.
        //                String[] array = host.getsupported_cluster_levels().split("[,]", -1);
        //                Version maxVersion = null;
        //
        //                for (int i = 0; i < array.length; i++)
        //                {
        //                    Version vdsVersion = new Version(array[i]);
        //                    for (Version version : (java.util.List<Version>)getDataCenter().getVersion().getItems())
        //                    {
        //                        if (version.equals(vdsVersion) && version.compareTo(maxVersion) > 0)
        //                        {
        //                            maxVersion = version;
        //                        }
        //                    }
        //                }
        //                if (maxVersion != null)
        //                {
        //                    getDataCenter().getVersion().setSelectedItem(maxVersion);
        //                    getCluster().getVersion().setSelectedItem(maxVersion);
        //                }
        //            }
        //
        //            listNames = new java.util.ArrayList<String>();
        //            if (clusterList == null)
        //            {
        //                clusterList = DataProvider.GetClusterList();
        //            }
        //
        //            for (VDSGroup cluster : clusterList)
        //            {
        //                listNames.add(cluster.getname());
        //            }
        //            getCluster().getName().setEntity(AvailableName(listNames));
        //        }
        //
        //        //Choose default CPU name to match host.
        //        if (host.getCpuName() != null && getCluster().getCPU().getSelectedItem() != null)
        //        {
        //            getCluster().getCPU().setSelectedItem(Linq.FirstOrDefault((java.util.List<ServerCpu>)getCluster().getCPU().getItems(), new Linq.ServerCpuPredicate(host.getCpuName().getCpuName())));
        //        }
        //
        //        //Always choose a available storage name.
        //        java.util.ArrayList<storage_domains> listStorageDomains = DataProvider.GetStorageDomainList();
        //        listNames = new java.util.ArrayList<String>();
        //        for (storage_domains storageDomain : listStorageDomains)
        //        {
        //            listNames.add(storageDomain.getstorage_name());
        //        }
        //        getFormattedStorageName().setEntity(AvailableName(listNames));
        //
        //
        //        return message;
    }

    private boolean IsLocalDataCenterEmpty(storage_pool dataCenter) {

        if (dataCenter != null && dataCenter.getstorage_pool_type() == StorageType.LOCALFS
            && dataCenter.getstatus() == StoragePoolStatus.Uninitialized) {
            return true;
        }
        return false;
    }

    private String AvailableName(ArrayList<String> list) {

        String commonName = getCommonName();
        ArrayList<Integer> notAvailableNumberList = new ArrayList<Integer>();

        String temp;
        for (String str : list) {

            temp = str.replace(getCommonName(), "");
            if (StringHelper.isNullOrEmpty(temp)) {
                temp = "0";
            }

            int tempInt = 0;

            RefObject<Integer> tempRef_tempInt = new RefObject<Integer>(tempInt);
            boolean tempVar = IntegerCompat.TryParse(temp, tempRef_tempInt);
            tempInt = tempRef_tempInt.argvalue;
            if (tempVar) {
                notAvailableNumberList.add(tempInt);
            }
        }

        Collections.sort(notAvailableNumberList);
        int i;
        for (i = 0; i < notAvailableNumberList.size(); i++) {
            if (notAvailableNumberList.get(i) == i) {
                continue;
            }
            break;
        }

        if (i > 0) {
            commonName = getCommonName() + (new Integer(i)).toString();
        }

        return commonName;
    }

    private int queryCounter;

    private void Frontend_QueryStarted() {
        queryCounter++;
        if (getProgress() == null) {
            StartProgress(null);
        }
    }

    private void Frontend_QueryComplete() {
        queryCounter--;
        if (queryCounter == 0) {
            StopProgress();
        }
    }


    private final Context context = new Context();


    public final class Context {

        public VDS host;
        public storage_pool hostDataCenter;
        public VDSGroup hostCluster;
        public storage_pool candidate;
        public ArrayList<storage_pool> dataCenterList;
        public ArrayList<VDSGroup> clusterList;
        public ArrayList<storage_domains> storageList;
        public HashMap<storage_pool, VDS> localStorageHostByDataCenterMap;
        public HashMap<storage_pool, ArrayList<VDSGroup>> clusterListByDataCenterMap;
    }
}
