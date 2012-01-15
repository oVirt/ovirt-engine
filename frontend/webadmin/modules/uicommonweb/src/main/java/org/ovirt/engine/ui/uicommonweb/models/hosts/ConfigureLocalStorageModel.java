package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LocalStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class ConfigureLocalStorageModel extends Model
{

    private LocalStorageModel privateStorage;

    public LocalStorageModel getStorage()
    {
        return privateStorage;
    }

    private void setStorage(LocalStorageModel value)
    {
        privateStorage = value;
    }

    private DataCenterModel privateDataCenter;

    public DataCenterModel getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(DataCenterModel value)
    {
        privateDataCenter = value;
    }

    private ClusterModel privateCluster;

    public ClusterModel getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ClusterModel value)
    {
        privateCluster = value;
    }

    private EntityModel privateFormattedStorageName;

    public EntityModel getFormattedStorageName()
    {
        return privateFormattedStorageName;
    }

    private void setFormattedStorageName(EntityModel value)
    {
        privateFormattedStorageName = value;
    }

    private boolean isEditedFlag;

    private boolean editMode;

    public boolean getEditMode()
    {
        return editMode;
    }

    public void setEditMode(boolean value)
    {
        // once editing the view the flags stays true
        if (isEditedFlag != true)
        {
            isEditedFlag = value;
        }
        editMode = value;
    }

    private boolean privatedontCreateDataCenter;

    private boolean getdontCreateDataCenter()
    {
        return privatedontCreateDataCenter;
    }

    private void setdontCreateDataCenter(boolean value)
    {
        privatedontCreateDataCenter = value;
    }

    private boolean privatedontCreateCluster;

    private boolean getdontCreateCluster()
    {
        return privatedontCreateCluster;
    }

    private void setdontCreateCluster(boolean value)
    {
        privatedontCreateCluster = value;
    }

    private boolean privatedontChangeHostCluster;

    private boolean getdontChangeHostCluster()
    {
        return privatedontChangeHostCluster;
    }

    private void setdontChangeHostCluster(boolean value)
    {
        privatedontChangeHostCluster = value;
    }

    public boolean getDontCreateDataCenter()
    {
        return getdontCreateDataCenter() & !isEditedFlag;
    }

    public void setDontCreateDataCenter(boolean value)
    {
        setdontCreateDataCenter(value);
    }

    public boolean getDontCreateCluster()
    {
        return getdontCreateCluster() & !isEditedFlag;
    }

    public void setDontCreateCluster(boolean value)
    {
        setdontCreateCluster(value);
    }

    public boolean getDontChangeHostCluster()
    {
        return getdontChangeHostCluster() & !isEditedFlag;
    }

    public void setDontChangeHostCluster(boolean value)
    {
        setdontChangeHostCluster(value);
    }

    private String privateCommonName;

    private String getCommonName()
    {
        return privateCommonName;
    }

    private void setCommonName(String value)
    {
        privateCommonName = value;
    }

    private boolean isGeneralTabValid;

    public boolean getIsGeneralTabValid()
    {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value)
    {
        if (isGeneralTabValid != value)
        {
            isGeneralTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid"));
        }
    }

    public ConfigureLocalStorageModel()
    {
        setStorage(new LocalStorageModel());
        setDataCenter(new DataCenterModel());
        getDataCenter().getVersion().getSelectedItemChangedEvent().addListener(this);
        setCluster(new ClusterModel());
        getCluster().Init(false);
        setFormattedStorageName(new EntityModel());

        // Set the storage type to be Local.
        for (StorageType item : (java.util.ArrayList<StorageType>) getDataCenter().getStorageTypeList().getItems())
        {
            if (item == StorageType.LOCALFS)
            {
                getDataCenter().getStorageTypeList().setSelectedItem(item);
                break;
            }
        }

        setIsGeneralTabValid(true);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getDataCenter().getVersion())
        {
            DataCenterVersion_SelectedItemChanged();
        }
    }

    private void DataCenterVersion_SelectedItemChanged()
    {
        Version version = (Version) getDataCenter().getVersion().getSelectedItem();

        // Keep in sync version for data center and cluster.
        getCluster().getVersion().setSelectedItem(version);
    }

    public boolean Validate()
    {
        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression("^[A-Za-z0-9_-]+$");
        tempVar.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.");
        getFormattedStorageName().ValidateEntity(new IValidation[] { tempVar });
        if (getFormattedStorageName().getEntity() != null
                && !DataProvider.IsStorageDomainNameUnique((String) getFormattedStorageName().getEntity()))
        {
            getFormattedStorageName().setIsValid(false);
            getFormattedStorageName().getInvalidityReasons().add("Name must be unique.");
        }
        boolean isStorageValid = getStorage().Validate() && getFormattedStorageName().getIsValid();
        boolean isDataCenterValid = true;
        if (!getDontCreateDataCenter())
        {
            isDataCenterValid = getDataCenter().Validate();
        }
        boolean isClusterValid = true;
        if (!getDontCreateCluster())
        {
            isClusterValid = getCluster().Validate(false);
        }

        setIsGeneralTabValid(isStorageValid && isDataCenterValid && isClusterValid);

        return isStorageValid && isDataCenterValid && isClusterValid;
    }

    public void SetDefaultNames(VDS host, RefObject<String> message)
    {
        message.argvalue = null;
        setCommonName(StringFormat.format("%1$s-Local", host.getvds_name().replace('.', '-')));
        storage_pool candidate = null;

        // selecet all possible DCs
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search,
                        new SearchParameters(StringFormat.format("DataCenter: name=%1$s", getCommonName() + "*"),
                                SearchType.StoragePool));

        java.util.ArrayList<storage_pool> dataCenterList = new java.util.ArrayList<storage_pool>();
        java.util.ArrayList<VDSGroup> clusterList = null;
        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            dataCenterList =
                    Linq.<storage_pool> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }
        // check if current settings suitable for local setup (in case just SD creation failed - re-using the same
        // setup)
        boolean useCurrentSettings = false;
        if (host.getstorage_pool_id() != null)
        {
            storage_pool tempCandidate = DataProvider.GetDataCenterById(host.getstorage_pool_id());
            if (IsLocalDataCenterEmpty(tempCandidate))
            {
                candidate = tempCandidate;
                useCurrentSettings = true;
            }
            else
            {
                if (tempCandidate != null && tempCandidate.getstorage_pool_type() == StorageType.LOCALFS)
                {
                    message.argvalue =
                            "Note: Local Storage is already configured for this Host. The Host belongs to "
                                    + host.getstorage_pool_name()
                                    + " with local Storage Domain. If OK is clicked - this Host will be moved to a new Data Center, and a new Local Storage Domain will be created. Hit Cancel to abort the operation.";
                }
            }
        }
        // check if there is other DC suitable for re-use
        if (candidate == null)
        {
            for (storage_pool dataCenter : dataCenterList)
            {
                // need to check if the new DC is without host.
                if (IsLocalDataCenterEmpty(dataCenter)
                        && DataProvider.GetLocalStorageHost(dataCenter.getname()) == null)
                {
                    candidate = dataCenter;
                    break;
                }
            }
        }
        java.util.ArrayList<String> listNames = new java.util.ArrayList<String>();
        // in case we found a suitable candidte for re-use:
        if (candidate != null)
        {
            getDataCenter().setDataCenterId(candidate.getId());
            getDataCenter().getName().setEntity(candidate.getname());
            getDataCenter().getDescription().setEntity(candidate.getdescription());
            Version compVersion = candidate.getcompatibility_version();
            getDataCenter().getVersion().setSelectedItem(compVersion);
            getCluster().getVersion().setSelectedItem(compVersion);
            setDontCreateDataCenter(true);
            // if we use current settings there is no need to create cluster.
            if (useCurrentSettings)
            {
                getCluster().setClusterId(host.getvds_group_id().getValue());
                getCluster().getName().setEntity(host.getvds_group_name());
                VDSGroup cluster = DataProvider.GetClusterById(host.getvds_group_id().getValue());
                if (cluster != null)
                {
                    getCluster().getDescription().setEntity(cluster.getdescription());
                    ServerCpu tempVar = new ServerCpu();
                    tempVar.setCpuName(cluster.getcpu_name());
                    getCluster().getCPU().setSelectedItem(tempVar);
                }
                setDontCreateCluster(true);
                setDontChangeHostCluster(true);
            }
            // use differnt cluster
            else
            {
                // check the DC cluster list (for re-use)
                clusterList = DataProvider.GetClusterList(candidate.getId());
                // no clusters avilable - pick up new name.
                if (clusterList.isEmpty())
                {
                    java.util.ArrayList<VDSGroup> listClusters = DataProvider.GetClusterList();
                    listNames = new java.util.ArrayList<String>();
                    for (VDSGroup cluster : listClusters)
                    {
                        listNames.add(cluster.getname());
                    }
                    getCluster().getName().setEntity(AvailableName(listNames));
                }
                else
                {
                    // use the DC cluster.
                    getCluster().setClusterId(clusterList.get(0).getID());
                    getCluster().getName().setEntity(clusterList.get(0).getname());
                    getCluster().getDescription().setEntity(clusterList.get(0).getdescription());
                    VDSGroup cluster = DataProvider.GetClusterById(getCluster().getClusterId().getValue());
                    if (cluster != null)
                    {
                        ServerCpu tempVar2 = new ServerCpu();
                        tempVar2.setCpuName(cluster.getcpu_name());
                        getCluster().getCPU().setSelectedItem(tempVar2);
                    }
                    setDontCreateCluster(true);
                    if (host.getvds_group_id().getValue().equals(getCluster().getClusterId()))
                    {
                        setDontChangeHostCluster(true);
                    }
                }
            }
        }
        else
        {
            // didn't found DC to re-use, so we select new names:
            listNames = new java.util.ArrayList<String>();
            for (storage_pool storagePool : dataCenterList)
            {
                listNames.add(storagePool.getname());
            }
            getDataCenter().getName().setEntity(AvailableName(listNames));

            // Choose a Data Center version corresponding to the host.
            if (!StringHelper.isNullOrEmpty(host.getsupported_cluster_levels()))
            {
                // the supported_cluster_levels are sorted.
                String[] array = host.getsupported_cluster_levels().split("[,]", -1);
                Version maxCombindVersion = null;

                for (int i = 0; i < array.length; i++)
                {
                    Version vdsVersion = new Version(array[i]);
                    for (Version version : (java.util.List<Version>) getDataCenter().getVersion().getItems())
                    {
                        if (version.equals(vdsVersion) && version.compareTo(maxCombindVersion) > 0)
                        {
                            maxCombindVersion = version;
                        }
                    }
                }
                if (maxCombindVersion != null)
                {
                    getDataCenter().getVersion().setSelectedItem(maxCombindVersion);
                    getCluster().getVersion().setSelectedItem(maxCombindVersion);
                }
            }

            listNames = new java.util.ArrayList<String>();
            if (clusterList == null)
            {
                clusterList = DataProvider.GetClusterList();
            }

            for (VDSGroup cluster : clusterList)
            {
                listNames.add(cluster.getname());
            }
            getCluster().getName().setEntity(AvailableName(listNames));
        }

        // Choose default CPU name to match host.
        if (host.getCpuName() != null && getCluster().getCPU().getSelectedItem() != null)
        {
            getCluster().getCPU().setSelectedItem(Linq.FirstOrDefault(getCluster().getCPU().getItems(),
                    new Linq.ServerCpuPredicate(host.getCpuName().getCpuName())));
        }
        // always choose a avialable storage name.
        java.util.ArrayList<storage_domains> listStorageDomains = DataProvider.GetStorageDomainList();
        listNames = new java.util.ArrayList<String>();
        for (storage_domains storageDomain : listStorageDomains)
        {
            listNames.add(storageDomain.getstorage_name());
        }
        getFormattedStorageName().setEntity(AvailableName(listNames));
    }

    private boolean IsLocalDataCenterEmpty(storage_pool dataCenter)
    {
        if (dataCenter != null && dataCenter.getstorage_pool_type() == StorageType.LOCALFS
                && dataCenter.getstatus() == StoragePoolStatus.Uninitialized)
        {
            return true;
        }
        return false;
    }

    private String AvailableName(java.util.ArrayList<String> list)
    {
        String retVal = getCommonName();
        java.util.ArrayList<Integer> notAvialbleNumberList = new java.util.ArrayList<Integer>();
        String temp;
        for (String str : list)
        {
            temp = str.replace(getCommonName(), "");
            if (StringHelper.isNullOrEmpty(temp))
            {
                temp = "0";
            }
            int tempInt = 0;
            RefObject<Integer> tempRef_tempInt = new RefObject<Integer>(tempInt);
            boolean tempVar = IntegerCompat.TryParse(temp, tempRef_tempInt);
            tempInt = tempRef_tempInt.argvalue;
            if (tempVar)
            {
                notAvialbleNumberList.add(tempInt);
            }
        }
        Collections.sort(notAvialbleNumberList);
        int i = 0;
        for (i = 0; i < notAvialbleNumberList.size(); i++)
        {
            if (notAvialbleNumberList.get(i) == i)
            {
                continue;
            }
            break;
        }
        if (i > 0)
        {
            retVal = getCommonName() + (new Integer(i)).toString();
        }
        return retVal;
    }
}
