package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForVdsGroupParameters;
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
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class VmModelBehaviorBase<TModel extends UnitVmModel> {

    private final Constants constants = ConstantsManager.getInstance().getConstants();

    private TModel privateModel;

    public TModel getModel() {
        return privateModel;
    }

    public void setModel(TModel value) {
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

    protected void PostInitTemplate() {

    }

    public boolean Validate()
    {
        return true;
    }

    private int maxVmsInPool = 1000;

    public int getMaxVmsInPool() {
        return maxVmsInPool;
    }

    public void setMaxVmsInPool(int maxVmsInPool) {
        this.maxVmsInPool = maxVmsInPool;
    }

    protected void updateUserCdImage(Guid storagePoolId) {
        AsyncDataProvider.GetIrsImageList(new AsyncQuery(getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                UnitVmModel model = (UnitVmModel) target;
                List<String> images = (List<String>) returnValue;
                setImagesToModel(model, images);
            }

        }),
                storagePoolId);
    }

    protected void setImagesToModel(UnitVmModel model, List<String> images) {
        String oldCdImage = (String) model.getCdImage().getSelectedItem();
        model.getCdImage().setItems(images);
        model.getCdImage().setSelectedItem((oldCdImage != null) ? oldCdImage
                : Linq.FirstOrDefault(images));
    }

    protected void UpdateCdImage()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        if (dataCenter == null)
        {
            return;
        }

        AsyncDataProvider.GetIrsImageList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        UnitVmModel model = (UnitVmModel) target;
                        ArrayList<String> images = (ArrayList<String>) returnValue;
                        setImagesToModel(model, images);

                    }
                }, getModel().getHash()),
                dataCenter.getId());

    }

    private final Map<TimezoneType, Iterable<Entry<String, String>>> cachedTimeZones = new HashMap<VmModelBehaviorBase.TimezoneType, Iterable<Entry<String,String>>>();

    private enum TimezoneType {
        WINDOWS_TIMEZONE,
        GENERAL_TIMEZONE;

        static TimezoneType getTimezoneByOs(boolean windows) {
            return windows ? WINDOWS_TIMEZONE : GENERAL_TIMEZONE;
        }
    }

    private boolean lastIsWinOS = false;
    private String winTimezoneString;
    private String generalTimezonesString;

    protected void updateTimeZone() {
        updateTimeZone(null);
    }

    protected void updateTimeZone(final String selectedTimeZone)
    {
        final TimezoneType timezoneType = TimezoneType.getTimezoneByOs(getModel().getIsWindowsOS());

        if (cachedTimeZones.get(timezoneType) == null)
        {
            AsyncDataProvider.GetTimeZoneList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                            Map<String, String> timezones = (Map<String, String>) returnValue;
                            // empty entry for default timezone
                            timezones.put(null, ""); //$NON-NLS-1$
                            cachedTimeZones.put(timezoneType, timezones.entrySet());
                            behavior.PostUpdateTimeZone(selectedTimeZone);
                        }
                    }, getModel().getHash()));
        }
        else
        {
            PostUpdateTimeZone(selectedTimeZone);
        }
    }

    private void PostUpdateTimeZone(String selectedTimeZone)
    {
        if (getModel().getIsWindowsOS() != lastIsWinOS) {
            lastIsWinOS = getModel().getIsWindowsOS();
            if (selectedTimeZone != null) {
                if (getModel().getIsWindowsOS()) {
                    generalTimezonesString = selectedTimeZone;
                } else {
                    winTimezoneString = selectedTimeZone;
                }
            }
            selectedTimeZone = getModel().getIsWindowsOS() ? winTimezoneString : generalTimezonesString;
        } else {
            if (selectedTimeZone != null) {
                if (getModel().getIsWindowsOS()) {
                    winTimezoneString = selectedTimeZone;
                } else {
                    generalTimezonesString = selectedTimeZone;
                }
            }
        }
        final TimezoneType timezoneType = TimezoneType.getTimezoneByOs(getModel().getIsWindowsOS());
        getModel().getTimeZone().setItems(cachedTimeZones.get(timezoneType));

        // If there was some time zone selected before, try select it again.
        Entry<String, String> selectedTimeZoneEntry =
                getTimezoneEntryByKey(selectedTimeZone, cachedTimeZones.get(timezoneType));
        Map.Entry<String, String> selectedItem = selectedTimeZoneEntry != null ?
                selectedTimeZoneEntry : (Map.Entry<String, String>) getModel().getTimeZone().getSelectedItem();

        getModel().getTimeZone().setSelectedItem(selectedItem == null ? null :
            Linq.FirstOrDefault(cachedTimeZones.get(timezoneType), new Linq.TimeZonePredicate(selectedItem.getKey())));
    }

    private Entry<String, String> getTimezoneEntryByKey(String key, Iterable<Entry<String, String>> timeZones) {
        for (Entry<String, String> entry : timeZones) {
            if (StringHelper.stringsEqual(key, entry.getKey())) {
                return entry;
            }
        }

        return null;
    }

    private String cachedDefaultTimeZoneKey;

    protected void UpdateDefaultTimeZone()
    {
        if (cachedDefaultTimeZoneKey == null)
        {
            AsyncDataProvider.GetDefaultTimeZone(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
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
        updateTimeZone(cachedDefaultTimeZoneKey);
    }

    protected void UpdateDomain()
    {
        AsyncDataProvider.GetDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                        List<String> domains = (List<String>) returnValue;
                        String oldDomain = (String) behavior.getModel().getDomain().getSelectedItem();
                        if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain)) //$NON-NLS-1$
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

    private Integer cachedMaxPriority;

    protected void InitPriority(int priority)
    {
        AsyncDataProvider.GetMaxVmPriority(new AsyncQuery(new Object[] { getModel(), priority },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        UnitVmModel model = (UnitVmModel) array[0];
                        int vmPriority = (Integer) array[1];
                        cachedMaxPriority = (Integer) returnValue;

                        int value = AsyncDataProvider.GetRoundedPriority(vmPriority, cachedMaxPriority);
                        EntityModel tempVar = new EntityModel();
                        tempVar.setEntity(value);
                        model.getPriority().setSelectedItem(tempVar);
                        UpdatePriority();

                    }
                }, getModel().getHash()));
    }

    protected void UpdatePriority()
    {
        if (cachedMaxPriority == null)
        {
            AsyncDataProvider.GetMaxVmPriority(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                            cachedMaxPriority = (Integer) returnValue;
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
        ArrayList<EntityModel> items = new ArrayList<EntityModel>();
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().lowTitle());
        tempVar.setEntity(1);
        items.add(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().mediumTitle());
        tempVar2.setEntity(cachedMaxPriority / 2);
        items.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().highTitle());
        tempVar3.setEntity(cachedMaxPriority);
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

    protected void doChangeDefautlHost(NGuid hostGuid) {
        if (hostGuid != null)
        {
            Guid vdsId = hostGuid.getValue();
            if (getModel().getDefaultHost().getItems() != null)
            {
                getModel().getDefaultHost().setSelectedItem(Linq.FirstOrDefault(getModel().getDefaultHost().getItems(),
                        new Linq.HostPredicate(vdsId)));
            }
            getModel().getIsAutoAssign().setEntity(false);
        }
        else
        {
            getModel().getIsAutoAssign().setEntity(true);
        }
    }

    protected void UpdateDefaultHost()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();

        if (cluster == null)
        {
            getModel().getDefaultHost().setItems(new ArrayList<VDS>());
            getModel().getDefaultHost().setSelectedItem(null);

            return;
        }

        AsyncQuery query = new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        ArrayList<VDS> hosts = null;
                        if (returnValue instanceof ArrayList) {
                            hosts = (ArrayList<VDS>) returnValue;
                        } else if (returnValue instanceof VdcQueryReturnValue
                                && ((VdcQueryReturnValue) returnValue).getReturnValue() instanceof ArrayList) {
                            hosts = (ArrayList<VDS>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        } else {
                            throw new IllegalArgumentException("The return value should be ArrayList<VDS> or VdcQueryReturnValue with return value ArrayList<VDS>"); //$NON-NLS-1$
                        }

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
                                            .setItems(new ArrayList<VDS>(Arrays.asList(new VDS[] { vds })));
                                    model.getDefaultHost().setSelectedItem(vds);
                                    model.getDefaultHost().setIsChangable(false);
                                    model.getDefaultHost().setInfo("Cannot choose other Host in tree context"); //$NON-NLS-1$
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
                getModel().getHash());

        getHostListByCluster(cluster, query);
    }

    /**
     * By default admin query is fired, UserPortal overrides it to fire user query
     */
    protected void getHostListByCluster(VDSGroup cluster, AsyncQuery query) {
        AsyncDataProvider.GetHostListByCluster(query, cluster.getname());
    }

    protected void updateCustomPropertySheet() {
        if (getModel().getCluster().getSelectedItem() == null) {
            return;
        }
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        getModel().getCustomPropertySheet().setKeyValueString(getModel().getCustomPropertiesKeysList()
                .get(cluster.getcompatibility_version()));
    }

    public int maxCpus = 0;
    public int maxCpusPerSocket = 0;
    public int maxNumOfSockets = 0;

    public void UpdataMaxVmsInPool() {
        AsyncDataProvider.GetMaxVmsInPool(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                        behavior.setMaxVmsInPool((Integer) returnValue);
                        behavior.UpdateMaxNumOfVmCpus();
                    }
                }));
    }

    public void UpdateMaxNumOfVmCpus()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        String version = cluster.getcompatibility_version().toString();

        AsyncDataProvider.GetMaxNumOfVmCpus(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
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
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                        behavior.maxCpusPerSocket = (Integer) returnValue;
                        behavior.totalCpuCoresChanged();
                    }
                }, getModel().getHash()), version);
    }

    public void InitDisks()
    {
        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) returnValue;
                        Collections.sort(disks, new Linq.DiskByAliasComparer());
                        ArrayList<DiskModel> list = new ArrayList<DiskModel>();

                        for (Disk disk : disks) {
                            DiskModel diskModel = new DiskModel();
                            diskModel.getAlias().setEntity(disk.getDiskAlias());

                            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                                DiskImage diskImage = (DiskImage) disk;

                                EntityModel tempVar = new EntityModel();
                                tempVar.setEntity(diskImage.getSizeInGigabytes());
                                diskModel.setSize(tempVar);
                                ListModel tempVar2 = new ListModel();
                                tempVar2.setItems((diskImage.getVolumeType() == VolumeType.Preallocated ? new ArrayList<VolumeType>(Arrays.asList(new VolumeType[]{VolumeType.Preallocated}))
                                        : AsyncDataProvider.GetVolumeTypeList()));
                                tempVar2.setSelectedItem(diskImage.getVolumeType());
                                diskModel.setVolumeType(tempVar2);
                                diskModel.getVolumeType().setIsAvailable(false);
                            }

                            diskModel.setDisk(disk);
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
            PostInitStorageDomains();
        }
        else
        {
            getModel().getStorageDomain().setItems(new ArrayList<StorageDomain>());
            getModel().getStorageDomain().setSelectedItem(null);
            getModel().getStorageDomain().setIsChangable(false);
        }
    }

    protected void PostInitStorageDomains() {
        if (getModel().getDisks() == null) {
            return;
        }

        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();
        AsyncDataProvider.GetPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                ArrayList<StorageDomain> activeStorageDomains = FilterStorageDomains(storageDomains);

                boolean provisioning = (Boolean) behavior.getModel().getProvisioning().getEntity();
                ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                Linq.Sort(activeStorageDomains, new Linq.StorageDomainByNameComparer());

                for (DiskModel diskModel : disks) {
                    ArrayList<StorageDomain> availableDiskStorageDomains = new ArrayList<StorageDomain>();
                    diskModel.getQuota().setItems(behavior.getModel().getQuota().getItems());
                    ArrayList<Guid> storageIds = ((DiskImage) diskModel.getDisk()).getStorageIds();

                    // Active storage domains that the disk resides on
                    ArrayList<StorageDomain> activeDiskStorageDomains =
                            Linq.getStorageDomainsByIds(storageIds, activeStorageDomains);

                    // Set target storage domains
                    availableDiskStorageDomains = provisioning ? activeStorageDomains : activeDiskStorageDomains;
                    Linq.Sort(availableDiskStorageDomains, new Linq.StorageDomainByNameComparer());
                    diskModel.getStorageDomain().setItems(availableDiskStorageDomains);

                    diskModel.getStorageDomain().setChangeProhibitionReason(
                            constants.noActiveTargetStorageDomainAvailableMsg());
                    diskModel.getStorageDomain().setIsChangable(!availableDiskStorageDomains.isEmpty());
                }
            }
        }, getModel().getHash()), dataCenter.getId(), ActionGroup.CREATE_VM);
    }

    public ArrayList<StorageDomain> FilterStorageDomains(ArrayList<StorageDomain> storageDomains)
    {
        // filter only the Active storage domains (Active regarding the relevant storage pool).
        ArrayList<StorageDomain> list = new ArrayList<StorageDomain>();
        for (StorageDomain a : storageDomains)
        {
            if (Linq.IsDataActiveStorageDomain(a))
            {
                list.add(a);
            }
        }

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            StorageDomain selectStorage = (StorageDomain) getSystemTreeSelectedItem().getEntity();
            StorageDomain sd = Linq.FirstOrDefault(list, new Linq.StoragePredicate(selectStorage.getId()));
            list = new ArrayList<StorageDomain>(Arrays.asList(new StorageDomain[] { sd }));
        }

        return list;
    }

    protected void updateQuotaByCluster(final Guid defaultQuota, final String quotaName) {
        if (getModel().getQuota().getIsAvailable()) {
            VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
            if (cluster == null) {
                return;
            }
            Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForVdsGroup,
                    new GetAllRelevantQuotasForVdsGroupParameters(cluster.getId()), new AsyncQuery(getModel(),
                            new INewAsyncCallback() {

                                @Override
                                public void onSuccess(Object model, Object returnValue) {
                                    UnitVmModel vmModel = (UnitVmModel) model;
                                    ArrayList<Quota> quotaList =
                                            (ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                                    if (quotaList != null && !quotaList.isEmpty()) {
                                        vmModel.getQuota().setItems(quotaList);
                                    }
                                    if (defaultQuota != null) {
                                        boolean hasQuotaInList = false;
                                        for (Quota quota : quotaList) {
                                            if (quota.getId().equals(defaultQuota)) {
                                                vmModel.getQuota().setSelectedItem(quota);
                                                hasQuotaInList = true;
                                                break;
                                            }
                                        }
                                        if (!hasQuotaInList) {
                                            Quota quota = new Quota();
                                            quota.setId(defaultQuota);
                                            quota.setQuotaName(quotaName);
                                            quotaList.add(quota);
                                            vmModel.getQuota().setItems(quotaList);
                                            vmModel.getQuota().setSelectedItem(quota);
                                        }
                                    }
                                }
                            }));
        }
    }

    protected void setupTemplate(VM vm, ListModel model) {
        AsyncDataProvider.GetTemplateById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        VmTemplate template = (VmTemplate) returnValue;
                        model.getTemplate()
                                .setItems(new ArrayList<VmTemplate>(Arrays.asList(new VmTemplate[] { template })));
                        model.getTemplate().setSelectedItem(template);
                        model.getTemplate().setIsChangable(false);
                        PostInitTemplate();
                    }
                },
                getModel().getHash()),
                vm.getVmtGuid());
    }

    protected void updateCpuPinningVisibility() {
        if (getModel().getCluster().getSelectedItem() != null) {
            VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
            String compatibilityVersion = cluster.getcompatibility_version().toString();
            boolean hasCpuPinning = Boolean.FALSE.equals(getModel().getIsAutoAssign().getEntity());

            if (Boolean.FALSE.equals(AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.CpuPinningEnabled,
                    compatibilityVersion))) {
                hasCpuPinning = false;
            } else if (Boolean.FALSE.equals(AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.CpuPinMigrationEnabled,
                    AsyncDataProvider.getDefaultConfigurationVersion()))
                    && isVmMigratable()) {
                hasCpuPinning = false;
            }

            getModel().getCpuPinning()
                    .setIsChangable(hasCpuPinning);
            if (!hasCpuPinning) {
                getModel().getCpuPinning().setEntity("");
            }
        }
    }

    public void updateUseHostCpuAvailability() {

        boolean clusterSupportsHostCpu =
                getModel().getCluster().getSelectedItem() != null
                        && ((VDSGroup) (getModel().getCluster().getSelectedItem())).getcompatibility_version()
                                .compareTo(Version.v3_2) >= 0;
        boolean nonMigratable = MigrationSupport.PINNED_TO_HOST == getModel().getMigrationMode().getSelectedItem();
        boolean manuallyMigratableAndAnyHostInCluster =
                MigrationSupport.IMPLICITLY_NON_MIGRATABLE == getModel().getMigrationMode().getSelectedItem()
                        && Boolean.TRUE.equals(getModel().getIsAutoAssign().getEntity());

        if (clusterSupportsHostCpu && (nonMigratable || manuallyMigratableAndAnyHostInCluster)) {
            getModel().getHostCpu().setIsChangable(true);
        } else {
            getModel().getHostCpu().setEntity(false);
            getModel().getHostCpu().setChangeProhibitionReason(constants.hosCPUUnavailable());
            getModel().getHostCpu().setIsChangable(false);

        }
    }

    private boolean isVmMigratable() {
        return getModel().getMigrationMode().getSelectedItem() != MigrationSupport.PINNED_TO_HOST;
    }

    public void numOfSocketChanged() {
        int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());
        int totalCpuCores = getTotalCpuCores();

        if (numOfSockets == 0 || numOfSockets == 0) {
            return;
        }

        getModel().getCoresPerSocket().setSelectedItem(totalCpuCores / numOfSockets);
    }

    public void coresPerSocketChanged() {
        int coresPerSocket = extractIntFromListModel(getModel().getCoresPerSocket());
        int totalCpuCores = getTotalCpuCores();

        if (coresPerSocket == 0 || totalCpuCores == 0) {
            return;
        }

        // no need to check, if the new value is in the list of items, because it is filled
        // only by enabled values
        getModel().getNumOfSockets().setSelectedItem(totalCpuCores / coresPerSocket);
    }

    public void totalCpuCoresChanged() {
        int totalCpuCores = getTotalCpuCores();

        int coresPerSocket = extractIntFromListModel(getModel().getCoresPerSocket());
        int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());

        // if incorrect value put - e.g. not an integer
        getModel().getCoresPerSocket().setIsChangable(totalCpuCores != 0);
        getModel().getNumOfSockets().setIsChangable(totalCpuCores != 0);
        if (totalCpuCores == 0) {
            return;
        }

        // if has not been yet inited, init to 1
        if (numOfSockets == 0 || coresPerSocket == 0) {
            initListToOne(getModel().getCoresPerSocket());
            initListToOne(getModel().getNumOfSockets());
            coresPerSocket = 1;
            numOfSockets = 1;
        }

        List<Integer> coresPerSocets = findIndependentPossibleValues(maxCpusPerSocket);
        List<Integer> sockets = findIndependentPossibleValues(maxNumOfSockets);

        getModel().getCoresPerSocket().setItems(filterPossibleValues(coresPerSocets, sockets));
        getModel().getNumOfSockets().setItems(filterPossibleValues(sockets, coresPerSocets));

        // ignore the value already selected in the coresPerSocket
        // and always try to set the max possible totalcpuCores
        if (totalCpuCores <= maxNumOfSockets) {
            getModel().getCoresPerSocket().setSelectedItem(1);
            getModel().getNumOfSockets().setSelectedItem(totalCpuCores);
        } else {
            // we need to compose it from more cores on the available sockets
            composeCoresAndSocketsWhenDontFitInto(totalCpuCores);
        }

        boolean isNumOfVcpusCorrect = isNumOfSocketsCorrect(totalCpuCores);

        getModel().getCoresPerSocket().setIsChangable(isNumOfVcpusCorrect);
        getModel().getNumOfSockets().setIsChangable(isNumOfVcpusCorrect);
    }

    public boolean isNumOfSocketsCorrect(int totalCpuCores) {
        boolean isNumOfVcpusCorrect =
                (extractIntFromListModel(getModel().getCoresPerSocket()) * extractIntFromListModel(getModel().getNumOfSockets())) == totalCpuCores;
        return isNumOfVcpusCorrect;
    }

    /**
     * The hard way of finding, what the correct combination of the sockets and cores/socket should be (e.g. checking
     * all possible combinations)
     */
    private void composeCoresAndSocketsWhenDontFitInto(int totalCpuCores) {
        List<Integer> possibleSockets = findIndependentPossibleValues(maxNumOfSockets);
        List<Integer> possibleCoresPerSocket = findIndependentPossibleValues(maxCpusPerSocket);

        // the more sockets I can use, the better
        Collections.reverse(possibleSockets);

        for (Integer socket : possibleSockets) {
            for (Integer coresPerSocket : possibleCoresPerSocket) {
                if (socket * coresPerSocket == totalCpuCores) {
                    getModel().getCoresPerSocket().setSelectedItem(coresPerSocket);
                    getModel().getNumOfSockets().setSelectedItem(socket);
                    return;
                }
            }
        }
    }

    protected int getTotalCpuCores() {
        try {
            return getModel().getTotalCPUCores().getEntity() != null ? Integer.parseInt(getModel().getTotalCPUCores()
                    .getEntity()
                    .toString()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int extractIntFromListModel(ListModel model) {
        return model.getSelectedItem() != null ? Integer.parseInt(model
                .getSelectedItem()
                .toString())
                : 0;
    }

    private void initListToOne(ListModel list) {
        list.setItems(Arrays.asList(1));
        list.setSelectedItem(1);
    }

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
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) array[0];
                        behavior.maxNumOfSockets = ((Integer) returnValue);
                        behavior.UpdataMaxVmsInPool();

                    }
                }, getModel().getHash()), version);
    }

    /**
     * Returns a list of integers which can divide the param
     */
    private List<Integer> findIndependentPossibleValues(int max) {
        List<Integer> res = new ArrayList<Integer>();
        int totalCPUCores = getTotalCpuCores();

        for (int i = 1; i <= Math.min(totalCPUCores, max); i++) {
            if (totalCPUCores % i == 0) {
                res.add(i);
            }
        }

        return res;
    }

    /**
     * Filters out the values, which can not be used in conjuction with the others to reach the total CPUs
     */
    private List<Integer> filterPossibleValues(List<Integer> candidates, List<Integer> others) {
        List<Integer> res = new ArrayList<Integer>();
        int currentCpusCores = getTotalCpuCores();

        for (Integer candidate : candidates) {
            for (Integer other : others) {
                if (candidate * other == currentCpusCores) {
                    res.add(candidate);
                    break;
                }
            }
        }

        return res;
    }

    protected void updateHostPinning(MigrationSupport migrationSupport) {
        getModel().getMigrationMode().setSelectedItem(migrationSupport);
    }
}
