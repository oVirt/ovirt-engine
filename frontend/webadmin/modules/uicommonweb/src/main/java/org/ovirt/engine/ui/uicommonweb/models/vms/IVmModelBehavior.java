package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;

@SuppressWarnings("unused")
public abstract class IVmModelBehavior
{
    private UnitVmModel privateModel;

    public UnitVmModel getModel()
    {
        return privateModel;
    }

    public void setModel(UnitVmModel value)
    {
        privateModel = value;
    }

    private SystemTreeItemModel privateSystemTreeSelectedItem;

    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return privateSystemTreeSelectedItem;
    }

    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        privateSystemTreeSelectedItem = value;
    }

    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        this.setSystemTreeSelectedItem(systemTreeSelectedItem);
    }

    public abstract void DataCenter_SelectedItemChanged();

    public abstract void Template_SelectedItemChanged();

    public abstract void Cluster_SelectedItemChanged();

    public abstract void DefaultHost_SelectedItemChanged();

    public abstract void Provisioning_SelectedItemChanged();

    public abstract void UpdateMinAllocatedMemory();

    public boolean Validate()
    {
        return true;
    }

    protected void UpdateCdImage()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        if (dataCenter == null)
        {
            return;
        }

        AsyncDataProvider.GetIsoDomainByDataCenterId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        IVmModelBehavior behavior = (IVmModelBehavior) target;
                        storage_domains storageDomain = (storage_domains) returnValue;
                        if (storageDomain != null)
                        {
                            behavior.PostUpdateCdImage(storageDomain.getId());
                        }
                        else
                        {
                            behavior.getModel().getCdImage().setItems(new java.util.ArrayList<String>());
                        }

                    }
                }, getModel().getHash()), dataCenter.getId());
    }

    public void PostUpdateCdImage(Guid storageDomainId)
    {
        AsyncDataProvider.GetIrsImageList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        java.util.ArrayList<String> images = (java.util.ArrayList<String>) returnValue;
                        String oldCdImage = (String) model.getCdImage().getSelectedItem();
                        model.getCdImage().setItems(images);
                        model.getCdImage().setSelectedItem((oldCdImage != null) ? oldCdImage
                                : Linq.FirstOrDefault(images));

                    }
                }, getModel().getHash()),
                storageDomainId,
                false);
    }

    private Iterable<java.util.Map.Entry<String, String>> cachedTimeZones;

    protected void UpdateTimeZone()
    {
        if (cachedTimeZones == null)
        {
            AsyncDataProvider.GetTimeZoneList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            IVmModelBehavior behavior = (IVmModelBehavior) target;
                            cachedTimeZones = ((java.util.HashMap<String, String>) returnValue).entrySet();
                            behavior.PostUpdateTimeZone();

                        }
                    }, getModel().getHash()));
        }
        else
        {
            PostUpdateTimeZone();
        }
    }

    public void PostUpdateTimeZone()
    {
        // If there was some time zone selected before, try select it again.
        String oldTimeZoneKey =
                ((java.util.Map.Entry<String, String>) getModel().getTimeZone().getSelectedItem()).getKey();

        getModel().getTimeZone().setItems(cachedTimeZones);
        if (oldTimeZoneKey != null)
        {
            getModel().getTimeZone().setSelectedItem(Linq.FirstOrDefault(cachedTimeZones,
                    new Linq.TimeZonePredicate(oldTimeZoneKey)));
        }
        else
        {
            getModel().getTimeZone().setSelectedItem(null);
        }
    }

    private String cachedDefaultTimeZoneKey;

    protected void UpdateDefaultTimeZone()
    {
        if (cachedDefaultTimeZoneKey == null)
        {
            AsyncDataProvider.GetDefaultTimeZone(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            IVmModelBehavior behavior = (IVmModelBehavior) target;
                            cachedDefaultTimeZoneKey = (String) returnValue;
                            behavior.PostUpdateDefaultTimeZone();

                        }
                    }, getModel().getHash()));
        }
        else
        {
            PostUpdateDefaultTimeZone();
        }
    }

    public void PostUpdateDefaultTimeZone()
    {
        // Patch! Create key-value pair with a right key.
        getModel().getTimeZone().setSelectedItem(new KeyValuePairCompat<String, String>(cachedDefaultTimeZoneKey, ""));

        UpdateTimeZone();
    }

    protected void UpdateDomain()
    {
        AsyncDataProvider.GetDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        IVmModelBehavior behavior = (IVmModelBehavior) target;
                        java.util.List<String> domains = (java.util.List<String>) returnValue;
                        String oldDomain = (String) behavior.getModel().getDomain().getSelectedItem();
                        if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain))
                        {
                            domains.add(0, oldDomain);
                        }
                        behavior.getModel().getDomain().setItems(domains);
                        behavior.getModel()
                                .getDomain()
                                .setSelectedItem((oldDomain != null) ? oldDomain : Linq.FirstOrDefault(domains));

                    }
                }, getModel().getHash()),
                true);
    }

    protected void InitPriority(int priority)
    {
        AsyncDataProvider.GetRoundedPriority(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        int value = (Integer) returnValue;
                        EntityModel tempVar = new EntityModel();
                        tempVar.setEntity(value);
                        model.getPriority().setSelectedItem(tempVar);
                        UpdatePriority();

                    }
                }, getModel().getHash()), priority);
    }

    private Integer cachedMaxPrority;

    protected void UpdatePriority()
    {
        if (cachedMaxPrority == null)
        {
            AsyncDataProvider.GetMaxVmPriority(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            IVmModelBehavior behavior = (IVmModelBehavior) target;
                            cachedMaxPrority = (Integer) returnValue;
                            behavior.PostUpdatePriority();

                        }
                    }, getModel().getHash()));
        }
        else
        {
            PostUpdatePriority();
        }
    }

    private void PostUpdatePriority()
    {
        java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle("Low");
        tempVar.setEntity(1);
        items.add(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle("Medium");
        tempVar2.setEntity(cachedMaxPrority / 2);
        // C# TO JAVA CONVERTER TODO TASK: Arithmetic operations involving nullable type instances are not converted to
        // null-value logic:
        items.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle("High");
        tempVar3.setEntity(cachedMaxPrority);
        items.add(tempVar3);

        // If there was some priority selected before, try select it again.
        EntityModel oldPriority = (EntityModel) getModel().getPriority().getSelectedItem();

        getModel().getPriority().setItems(items);

        if (oldPriority != null)
        {
            for (EntityModel item : items)
            {
                int val1 = (Integer) item.getEntity();
                int val2 = (Integer) oldPriority.getEntity();
                if ((new Integer(val1)).equals(val2))
                {
                    getModel().getPriority().setSelectedItem(item);
                    break;
                }
            }
        }
        else
        {
            getModel().getPriority().setSelectedItem(Linq.FirstOrDefault(items));
        }
    }

    protected void ChangeDefualtHost()
    {

    }

    protected void UpdateDefaultHost()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();

        if (cluster == null)
        {
            getModel().getDefaultHost().setItems(new java.util.ArrayList<VDS>());
            getModel().getDefaultHost().setSelectedItem(null);

            return;
        }

        AsyncDataProvider.GetHostListByCluster(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        java.util.ArrayList<VDS> hosts = (java.util.ArrayList<VDS>) returnValue;
                        VDS oldDefaultHost = (VDS) model.getDefaultHost().getSelectedItem();
                        if (model.getBehavior().getSystemTreeSelectedItem() != null
                                && model.getBehavior().getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host)
                        {
                            VDS host = (VDS) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                            for (VDS vds : hosts)
                            {
                                if (host.getId().equals(vds.getId()))
                                {
                                    model.getDefaultHost()
                                            .setItems(new java.util.ArrayList<VDS>(java.util.Arrays.asList(new VDS[] { vds })));
                                    model.getDefaultHost().setSelectedItem(vds);
                                    model.getDefaultHost().setIsChangable(false);
                                    model.getDefaultHost().setInfo("Cannot choose other Host in tree context");
                                    break;
                                }
                            }
                        }
                        else
                        {
                            model.getDefaultHost().setItems(hosts);
                            model.getDefaultHost().setSelectedItem(oldDefaultHost != null ? Linq.FirstOrDefault(hosts,
                                    new Linq.HostPredicate(oldDefaultHost.getId())) : Linq.FirstOrDefault(hosts));
                        }
                        ChangeDefualtHost();

                    }
                },
                getModel().getHash()),
                cluster.getname());
    }

    protected void UpdateIsCustomPropertiesAvailable()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();

        if (cluster != null)
        {
            AsyncDataProvider.IsCustomPropertiesAvailable(new AsyncQuery(getModel(),
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            UnitVmModel model = (UnitVmModel) target;
                            model.setIsCustomPropertiesAvailable((Boolean) returnValue);

                        }
                    }, getModel().getHash()), cluster.getcompatibility_version().toString());
        }
    }

    public int maxCpus = 0;
    public int maxCpusPerSocket = 0;

    protected void UpdateNumOfSockets()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        String version = cluster.getcompatibility_version().toString();

        AsyncDataProvider.GetMaxNumOfVmSockets(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        IVmModelBehavior behavior = (IVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        model.getNumOfSockets().setMax((Integer) returnValue);
                        model.getNumOfSockets().setMin(1);
                        model.getNumOfSockets().setInterval(1);
                        model.getNumOfSockets().setIsAllValuesSet(true);
                        if (model.getNumOfSockets().getEntity() == null)
                        {
                            model.getNumOfSockets().setEntity(1);
                        }
                        behavior.PostUpdateNumOfSockets();

                    }
                }, getModel().getHash()), version);
    }

    public void PostUpdateNumOfSockets()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        String version = cluster.getcompatibility_version().toString();

        AsyncDataProvider.GetMaxNumOfVmCpus(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        IVmModelBehavior behavior = (IVmModelBehavior) target;
                        behavior.maxCpus = (Integer) returnValue;
                        behavior.PostUpdateNumOfSockets2();

                    }
                }, getModel().getHash()), version);
    }

    public void PostUpdateNumOfSockets2()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        String version = cluster.getcompatibility_version().toString();

        AsyncDataProvider.GetMaxNumOfCPUsPerSocket(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        IVmModelBehavior behavior = (IVmModelBehavior) target;
                        behavior.maxCpusPerSocket = (Integer) returnValue;
                        behavior.UpdateTotalCpus();

                    }
                }, getModel().getHash()), version);
    }

    public void UpdateTotalCpus()
    {
        int numOfSockets = Integer.parseInt(getModel().getNumOfSockets().getEntity().toString());

        int totalCPUCores =
                getModel().getTotalCPUCores().getEntity() != null ? Integer.parseInt(getModel().getTotalCPUCores()
                        .getEntity()
                        .toString()) : 0;

        int realMaxCpus = maxCpus < numOfSockets * maxCpusPerSocket ? maxCpus : numOfSockets * maxCpusPerSocket;

        if (maxCpus == 0 || maxCpusPerSocket == 0)
        {
            return;
        }

        getModel().getTotalCPUCores().setMax(realMaxCpus - (realMaxCpus % numOfSockets));
        getModel().getTotalCPUCores().setMin(numOfSockets);
        getModel().getTotalCPUCores().setInterval(numOfSockets);

        // update value if needed
        // if the slider in the range but not on tick update it to lowest tick
        if ((totalCPUCores % numOfSockets != 0) && totalCPUCores < getModel().getTotalCPUCores().getMax()
                && totalCPUCores > getModel().getTotalCPUCores().getMin())
        {
            getModel().getTotalCPUCores().setEntity(totalCPUCores - (totalCPUCores % numOfSockets));
        }
        // if the value is lower than range update it to min
        else if (totalCPUCores < getModel().getTotalCPUCores().getMin())
        {
            getModel().getTotalCPUCores().setEntity((int) getModel().getTotalCPUCores().getMin());
        }
        // if the value is higher than range update it to max
        else if (totalCPUCores > getModel().getTotalCPUCores().getMax())
        {
            getModel().getTotalCPUCores().setEntity((int) getModel().getTotalCPUCores().getMax());
        }

        getModel().getTotalCPUCores().setIsAllValuesSet(true);
    }

    public void InitDisks()
    {
        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        java.util.ArrayList<DiskImage> disks = (java.util.ArrayList<DiskImage>) returnValue;
                        Collections.sort(disks, new Linq.DiskByInternalDriveMappingComparer());
                        java.util.ArrayList<DiskModel> list = new java.util.ArrayList<DiskModel>();
                        for (DiskImage a : disks)
                        {
                            DiskModel diskModel = new DiskModel();
                            diskModel.setIsNew(true);
                            diskModel.setName(a.getinternal_drive_mapping());
                            EntityModel tempVar = new EntityModel();
                            tempVar.setEntity(a.getSizeInGigabytes());
                            diskModel.setSize(tempVar);
                            ListModel tempVar2 = new ListModel();
                            tempVar2.setItems((a.getvolume_type() == VolumeType.Preallocated ? new java.util.ArrayList<VolumeType>(java.util.Arrays.asList(new VolumeType[] { VolumeType.Preallocated }))
                                    : DataProvider.GetVolumeTypeList()));
                            tempVar2.setSelectedItem(a.getvolume_type());
                            diskModel.setVolumeType(tempVar2);
                            diskModel.setDiskImage(a);
                            diskModel.getVolumeType().setIsAvailable(false);
                            list.add(diskModel);
                        }
                        model.setDisks(list);
                        UpdateIsDisksAvailable();
                        InitStorageDomains();
                    }
                },
                getModel().getHash()),
                template.getId());
    }

    public void UpdateIsDisksAvailable() {

    }

    public void InitStorageDomains()
    {
        if (getModel().getDisks() == null) {
            return;
        }

        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        if (template != null && !template.getId().equals(NGuid.Empty))
        {
            storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

            AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    IVmModelBehavior behavior = (IVmModelBehavior) target;
                    ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;
                    ArrayList<storage_domains> activeStorageDomains = FilterStorageDomains(storageDomains);
                    DisksAllocationModel disksAllocationModel = getModel().getDisksAllocationModel();

                    boolean provisioning = (Boolean) behavior.getModel().getProvisioning().getEntity();
                    ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                    Linq.Sort(activeStorageDomains, new Linq.StorageDomainByNameComparer());
                    disksAllocationModel.setActiveStorageDomains(activeStorageDomains);

                    for (DiskModel diskModel : disks) {
                        ArrayList<storage_domains> availableDiskStorageDomains;
                        if (provisioning) {
                            availableDiskStorageDomains = activeStorageDomains;
                        }
                        else
                        {
                            ArrayList<Guid> storageIds = diskModel.getDiskImage().getstorage_ids();
                            availableDiskStorageDomains =
                                    Linq.getStorageDomainsByIds(storageIds, activeStorageDomains);
                        }
                        Linq.Sort(availableDiskStorageDomains, new Linq.StorageDomainByNameComparer());
                        diskModel.getStorageDomain().setItems(availableDiskStorageDomains);
                    }

                    ArrayList<storage_domains> storageDomainsDisjoint =
                            Linq.getStorageDomainsDisjoint(disks, activeStorageDomains);

                    Linq.Sort(storageDomainsDisjoint, new Linq.StorageDomainByNameComparer());

                    ArrayList<storage_domains> singleDestDomains =
                            provisioning ? activeStorageDomains : storageDomainsDisjoint;
                    getModel().getStorageDomain().setItems(singleDestDomains);
                    getModel().getStorageDomain().setSelectedItem(Linq.FirstOrDefault(singleDestDomains));
                }
            }, getModel().getHash()), dataCenter.getId());
        }
        else
        {
            getModel().getStorageDomain().setItems(new java.util.ArrayList<storage_domains>());
            getModel().getStorageDomain().setSelectedItem(null);
            getModel().getStorageDomain().setIsChangable(false);
        }
    }

    public ArrayList<storage_domains> FilterStorageDomains(ArrayList<storage_domains> storageDomains)
    {
        // filter only the Active storage domains (Active regarding the relevant storage pool).
        ArrayList<storage_domains> list = new ArrayList<storage_domains>();
        for (storage_domains a : storageDomains)
        {
            if (Linq.IsDataActiveStorageDomain(a))
            {
                list.add(a);
            }
        }

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            storage_domains selectStorage = (storage_domains) getSystemTreeSelectedItem().getEntity();
            storage_domains sd = Linq.FirstOrDefault(list, new Linq.StoragePredicate(selectStorage.getId()));
            list = new ArrayList<storage_domains>(Arrays.asList(new storage_domains[] { sd }));
        }

        return list;
    }

}
