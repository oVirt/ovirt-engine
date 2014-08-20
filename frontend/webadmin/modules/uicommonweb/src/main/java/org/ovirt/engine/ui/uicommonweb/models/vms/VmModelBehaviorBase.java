package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
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
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class VmModelBehaviorBase<TModel extends UnitVmModel> {

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private TModel privateModel;
    private HashMap<Guid, List<VmTemplate>> baseTemplateToSubTemplates = new HashMap<Guid, List<VmTemplate>>();

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

    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        this.setSystemTreeSelectedItem(systemTreeSelectedItem);
    }

    public void dataCenterWithClusterSelectedItemChanged() {
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return;

        }
        StoragePool dataCenter = dataCenterWithCluster.getDataCenter();
        if (dataCenter == null) {
            return;
        }

        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
        } else {
            getModel().getQuota().setIsAvailable(false);
        }

        postDataCenterWithClusterSelectedItemChanged();
    }

    protected void updateMigrationForLocalSD() {
        boolean isLocalSD =
                getModel().getSelectedDataCenter() != null
                        && getModel().getSelectedDataCenter().isLocal();
        if(isLocalSD) {
            getModel().getIsAutoAssign().setEntity(false);
            getModel().getMigrationMode().setSelectedItem(MigrationSupport.PINNED_TO_HOST);
        }
        getModel().getIsAutoAssign().setIsChangable(!isLocalSD);
        getModel().getMigrationMode().setIsChangable(!isLocalSD);
        getModel().getDefaultHost().setIsChangable(!isLocalSD);
    }

    public abstract void template_SelectedItemChanged();

    public abstract void postDataCenterWithClusterSelectedItemChanged();

    public abstract void defaultHost_SelectedItemChanged();

    public abstract void provisioning_SelectedItemChanged();

    public abstract void oSType_SelectedItemChanged();

    public abstract void updateMinAllocatedMemory();

    protected void postOsItemChanged() {

    }

    protected List<VmTemplate> filterNotBaseTemplates(List<VmTemplate> templates) {
        List<VmTemplate> baseTemplates = new ArrayList<VmTemplate>();
        for (VmTemplate template : templates) {
            if (template.getId().equals(template.getBaseTemplateId())) {
                baseTemplates.add(template);
                baseTemplateToSubTemplates.put(template.getId(),
                        new ArrayList<VmTemplate>());
            }
        }

        for (VmTemplate template : templates) {
            Guid baseTemplateId = template.getBaseTemplateId();
            if (baseTemplateToSubTemplates.containsKey(baseTemplateId)) {
                baseTemplateToSubTemplates.get(baseTemplateId).add(template);
            }
        }

        for (List<VmTemplate> subversions : (Collection<List<VmTemplate>>) baseTemplateToSubTemplates.values()) {
            Collections.sort(subversions, new Comparator<VmTemplate>() {
                @Override
                public int compare(VmTemplate o1, VmTemplate o2) {
                    return o2.getTemplateVersionNumber() - o1.getTemplateVersionNumber();
                }
            });
        }

        for (List<VmTemplate> subversions : baseTemplateToSubTemplates.values()) {
            subversions.add(0, createLatestTemplate(subversions.get(0)));
        }

        return baseTemplates;
    }

    /**
     *
     * @param template - the template that the latest template should be based on
     * @return template representing the latest template
     */
    private VmTemplate createLatestTemplate(VmTemplate template) {
        VmTemplate latestTemplate = new VmTemplate(template);
        latestTemplate.setTemplateVersionName(constants.latestTemplateVersionName());
        latestTemplate.setDescription(constants.latestTemplateVersionDescription());

        return latestTemplate;
    }

    protected void baseTemplateSelectedItemChanged() {
        VmTemplate baseTemplate = getModel().getBaseTemplate().getSelectedItem();
        if (baseTemplate != null) {
            List<VmTemplate> subVersions = baseTemplateToSubTemplates.get(baseTemplate.getId());
            getModel().getTemplate().setItems(new ArrayList<VmTemplate>(subVersions));

            // it's safe because in index 0 there's the latest version and
            // in index 1 the base version or the last custom version
            getModel().getTemplate().setSelectedItem(subVersions.get(1));
        }
    }

    protected void isSubTemplateEntityChanged() {
    }

    public boolean validate()
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
        AsyncDataProvider.getIrsImageList(new AsyncQuery(getModel(), new INewAsyncCallback() {
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
        String oldCdImage = model.getCdImage().getSelectedItem();
        model.getCdImage().setItems(images);
        model.getCdImage().setSelectedItem((oldCdImage != null) ? oldCdImage
                : Linq.firstOrDefault(images));
    }

    public void refreshCdImages() {
        updateCdImage(true);
    }

    protected void updateCdImage() {
        updateCdImage(false);
    }

    protected void updateCdImage(boolean forceRefresh)
    {
        StoragePool dataCenter = getModel().getSelectedDataCenter();
        if (dataCenter == null)
        {
            return;
        }

        AsyncDataProvider.getIrsImageList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        UnitVmModel model = (UnitVmModel) target;
                        ArrayList<String> images = (ArrayList<String>) returnValue;
                        setImagesToModel(model, images);

                    }
                }, getModel().getHash()),
                dataCenter.getId(),
                forceRefresh);

    }

    protected void updateTimeZone(final String selectedTimeZone)
    {
        if (StringHelper.isNullOrEmpty(selectedTimeZone)) {
            updateDefaultTimeZone();
        } else {
            doUpdateTimeZone(selectedTimeZone);
        }
    }

    protected void updateDefaultTimeZone()
    {
        doUpdateTimeZone(null);
    }

    private void doUpdateTimeZone(final String selectedTimeZone) {
        final Iterable<TimeZoneModel> timeZones = TimeZoneModel.getTimeZones(getTimeZoneType());
        getModel().getTimeZone().setItems(timeZones);
        getModel().getTimeZone().setSelectedItem(Linq.firstOrDefault(timeZones, new Linq.TimeZonePredicate(selectedTimeZone)));
        getModel().getTimeZone().setChangeProhibitionReason(constants.timeZoneNotChangeableForLinuxVms());
        getModel().getTimeZone().setIsChangable(!getModel().getIsLinuxOS());
    }

    /**
     * This method checks the item that will be selected.
     * For a good UX, if the getItems contains the last selected item, this one not will be changed.
     * @param oldDisplayProtocolOption
     */
    public void postDisplayTypeItemChanged(DisplayType oldDisplayProtocolOption) {

        List<DisplayType> displayProtocolOptions = new ArrayList<DisplayType>();

        if (getModel().getDisplayProtocol().getItems() != null) {
            for (EntityModel<DisplayType> item : (ArrayList<EntityModel<DisplayType>>) getModel().getDisplayProtocol().getItems()) {
                displayProtocolOptions.add(item.getEntity());
            }
        }

        if (oldDisplayProtocolOption != null
                && displayProtocolOptions.contains(oldDisplayProtocolOption)) {
            selectDisplayProtocol(oldDisplayProtocolOption);
        }

    }

    private void selectDisplayProtocol(DisplayType displayProtocol) {
        if (getModel().getDisplayProtocol().getItems() == null)
            return;

        EntityModel selectedDisplayProtocol = null;
        boolean isFirst = true;
        for (Object item : getModel().getDisplayProtocol().getItems())
        {
            EntityModel a = (EntityModel) item;
            if (isFirst)
            {
                selectedDisplayProtocol = a;
                isFirst = false;
            }
            DisplayType dt = (DisplayType) a.getEntity();
            if (dt == displayProtocol)
            {
                selectedDisplayProtocol = a;
                break;
            }
        }

        getModel().getDisplayProtocol().setSelectedItem(selectedDisplayProtocol);
    }

    public TimeZoneType getTimeZoneType() {
        // can be null as a consequence of setItems on ListModel
        Integer vmOsType = getModel().getOSType().getSelectedItem();
        return AsyncDataProvider.isWindowsOsType(vmOsType) ? TimeZoneType.WINDOWS_TIMEZONE
                : TimeZoneType.GENERAL_TIMEZONE;
    }

    private Integer cachedMaxPriority;

    protected void initPriority(int priority)
    {
        AsyncDataProvider.getMaxVmPriority(new AsyncQuery(new Object[] { getModel(), priority },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        UnitVmModel model = (UnitVmModel) array[0];
                        int vmPriority = (Integer) array[1];
                        cachedMaxPriority = (Integer) returnValue;

                        int value = AsyncDataProvider.getRoundedPriority(vmPriority, cachedMaxPriority);
                        EntityModel tempVar = new EntityModel();
                        tempVar.setEntity(value);
                        model.getPriority().setSelectedItem(tempVar);
                        updatePriority();

                    }
                }, getModel().getHash()));
    }

    protected void updatePriority()
    {
        if (cachedMaxPriority == null)
        {
            AsyncDataProvider.getMaxVmPriority(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                            cachedMaxPriority = (Integer) returnValue;
                            behavior.postUpdatePriority();

                        }
                    }, getModel().getHash()));
        }
        else
        {
            postUpdatePriority();
        }
    }

    private void postUpdatePriority()
    {
        List<EntityModel<Integer>> items = new ArrayList<EntityModel<Integer>>();
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
        EntityModel<Integer> oldPriority = getModel().getPriority().getSelectedItem();

        getModel().getPriority().setItems(items);

        if (oldPriority != null)
        {
            for (EntityModel<Integer> item : items)
            {
                Integer val1 = item.getEntity();
                Integer val2 = oldPriority.getEntity();
                if (val1 != null && val1.equals(val2))
                {
                    getModel().getPriority().setSelectedItem(item);
                    break;
                }
            }
        }
        else
        {
            getModel().getPriority().setSelectedItem(Linq.firstOrDefault(items));
        }
    }

    protected void changeDefualtHost()
    {

    }

    protected void doChangeDefautlHost(Guid hostGuid) {
        if (hostGuid != null)
        {
            Guid vdsId = hostGuid;
            if (getModel().getDefaultHost().getItems() != null)
            {
                getModel().getDefaultHost().setSelectedItem(Linq.firstOrDefault(getModel().getDefaultHost().getItems(),
                        new Linq.HostPredicate(vdsId)));
            }
            getModel().getIsAutoAssign().setEntity(false);
        }
        else
        {
            getModel().getIsAutoAssign().setEntity(true);
        }
    }

    protected void updateDefaultHost()
    {
        VDSGroup cluster = getModel().getSelectedCluster();
        final UIConstants constants = ConstantsManager.getInstance().getConstants();

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
                            hosts = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        } else {
                            throw new IllegalArgumentException("The return value should be ArrayList<VDS> or VdcQueryReturnValue with return value ArrayList<VDS>"); //$NON-NLS-1$
                        }

                        VDS oldDefaultHost = model.getDefaultHost().getSelectedItem();
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
                                    model.getDefaultHost().setChangeProhibitionReason(constants.cannotChangeHostInTreeContext());
                                    break;
                                }
                            }
                        }
                        else
                        {
                            model.getDefaultHost().setItems(hosts);

                            if (oldDefaultHost != null) {
                                // Trying to get host object from hosts list
                                oldDefaultHost = Linq.firstOrDefault(hosts, new Linq.HostPredicate(oldDefaultHost.getId()));
                            }

                            // If found 'oldDefaultHost' in existing list, select it;
                            // otherwise, select first in hosts list.
                            model.getDefaultHost().setSelectedItem(oldDefaultHost != null ?
                                    oldDefaultHost : Linq.firstOrDefault(hosts));
                        }
                        changeDefualtHost();

                    }
                },
                getModel().getHash());

        getHostListByCluster(cluster, query);
    }

    /**
     * By default admin query is fired, UserPortal overrides it to fire user query
     */
    protected void getHostListByCluster(VDSGroup cluster, AsyncQuery query) {
        AsyncDataProvider.getHostListByCluster(query, cluster.getName());
    }

    protected void updateCustomPropertySheet() {
        if (getModel().getSelectedCluster() == null) {
            return;
        }
        VDSGroup cluster = getModel().getSelectedCluster();
        if (getModel().getCustomPropertiesKeysList() != null) {
            getModel().getCustomPropertySheet().setKeyValueString(getModel().getCustomPropertiesKeysList()
                    .get(cluster.getcompatibility_version()));
        }
    }

    public int maxCpus = 0;
    public int maxCpusPerSocket = 0;
    public int maxNumOfSockets = 0;

    public void updataMaxVmsInPool() {
        AsyncDataProvider.getMaxVmsInPool(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                        behavior.setMaxVmsInPool((Integer) returnValue);
                        behavior.updateMaxNumOfVmCpus();
                    }
                }));
    }

    public void updateMaxNumOfVmCpus()
    {
        VDSGroup cluster = getModel().getSelectedCluster();
        String version = cluster.getcompatibility_version().toString();

        AsyncDataProvider.getMaxNumOfVmCpus(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                        behavior.maxCpus = (Integer) returnValue;
                        behavior.postUpdateNumOfSockets2();
                    }
                }, getModel().getHash()), version);
    }

    public void postUpdateNumOfSockets2()
    {
        VDSGroup cluster = getModel().getSelectedCluster();
        String version = cluster.getcompatibility_version().toString();

        AsyncDataProvider.getMaxNumOfCPUsPerSocket(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                        behavior.maxCpusPerSocket = (Integer) returnValue;
                        behavior.totalCpuCoresChanged();
                    }
                }, getModel().getHash()), version);
    }

    public void initDisks()
    {
        VmTemplate template = getModel().getTemplate().getSelectedItem();

        AsyncDataProvider.getTemplateDiskList(new AsyncQuery(getModel(),
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
                                        : AsyncDataProvider.getVolumeTypeList()));
                                tempVar2.setSelectedItem(diskImage.getVolumeType());
                                diskModel.setVolumeType(tempVar2);
                                diskModel.getVolumeType().setIsAvailable(false);
                            }

                            diskModel.setDisk(disk);
                            list.add(diskModel);
                        }

                        model.setDisks(list);
                        updateIsDisksAvailable();
                        initStorageDomains();
                    }
                },
                getModel().getHash()),
                template.getId());
    }

    public void updateIsDisksAvailable() {

    }

    public void initStorageDomains()
    {
        if (getModel().getDisks() == null) {
            return;
        }

        VmTemplate template = getModel().getTemplate().getSelectedItem();

        if (template != null && !template.getId().equals(Guid.Empty))
        {
            postInitStorageDomains();
        }
        else
        {
            getModel().getStorageDomain().setItems(new ArrayList<StorageDomain>());
            getModel().getStorageDomain().setSelectedItem(null);
            getModel().getStorageDomain().setIsChangable(false);
        }
    }

    protected void postInitStorageDomains() {
        if (getModel().getDisks() == null) {
            return;
        }

        StoragePool dataCenter = getModel().getSelectedDataCenter();
        AsyncDataProvider.getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                ArrayList<StorageDomain> activeStorageDomains = filterStorageDomains(storageDomains);

                boolean provisioning = behavior.getModel().getProvisioning().getEntity();
                ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                Collections.sort(activeStorageDomains, new NameableComparator());

                for (DiskModel diskModel : disks) {
                    ArrayList<StorageDomain> availableDiskStorageDomains;
                    diskModel.getQuota().setItems(behavior.getModel().getQuota().getItems());
                    ArrayList<Guid> storageIds = ((DiskImage) diskModel.getDisk()).getStorageIds();

                    // Active storage domains that the disk resides on
                    ArrayList<StorageDomain> activeDiskStorageDomains =
                            Linq.getStorageDomainsByIds(storageIds, activeStorageDomains);

                    // Set target storage domains
                    availableDiskStorageDomains = provisioning ? activeStorageDomains : activeDiskStorageDomains;
                    Collections.sort(availableDiskStorageDomains, new NameableComparator());
                    diskModel.getStorageDomain().setItems(availableDiskStorageDomains);

                    diskModel.getStorageDomain().setChangeProhibitionReason(
                            constants.noActiveTargetStorageDomainAvailableMsg());
                    diskModel.getStorageDomain().setIsChangable(!availableDiskStorageDomains.isEmpty());
                }
            }
        }, getModel().getHash()), dataCenter.getId(), ActionGroup.CREATE_VM);
    }

    public ArrayList<StorageDomain> filterStorageDomains(ArrayList<StorageDomain> storageDomains)
    {
        // filter only the Active storage domains (Active regarding the relevant storage pool).
        ArrayList<StorageDomain> list = new ArrayList<StorageDomain>();
        for (StorageDomain a : storageDomains)
        {
            if (Linq.isDataActiveStorageDomain(a))
            {
                list.add(a);
            }
        }

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            StorageDomain selectStorage = (StorageDomain) getSystemTreeSelectedItem().getEntity();
            StorageDomain sd = Linq.firstOrDefault(list, new Linq.StoragePredicate(selectStorage.getId()));
            list = new ArrayList<StorageDomain>(Arrays.asList(new StorageDomain[] { sd }));
        }

        return list;
    }

    protected void updateQuotaByCluster(final Guid defaultQuota, final String quotaName) {
        if (getModel().getQuota().getIsAvailable()) {
            VDSGroup cluster = getModel().getSelectedCluster();
            if (cluster == null) {
                return;
            }
            Frontend.getInstance().runQuery(VdcQueryType.GetAllRelevantQuotasForVdsGroup,
                    new IdQueryParameters(cluster.getId()), new AsyncQuery(getModel(),
                            new INewAsyncCallback() {

                                @Override
                                public void onSuccess(Object model, Object returnValue) {
                                    UnitVmModel vmModel = (UnitVmModel) model;
                                    ArrayList<Quota> quotaList = ((VdcQueryReturnValue) returnValue).getReturnValue();
                                    if (quotaList != null && !quotaList.isEmpty()) {
                                        vmModel.getQuota().setItems(quotaList);
                                    }
                                    if (defaultQuota != null && !Guid.Empty.equals(defaultQuota)) {
                                        boolean hasQuotaInList = false;
                                        for (Quota quota : quotaList) {
                                            if (quota.getId().equals(defaultQuota)) {
                                                vmModel.getQuota().setSelectedItem(quota);
                                                hasQuotaInList = true;
                                                break;
                                            }
                                        }
                                        // Add the quota to the list only in edit mode
                                        if (!hasQuotaInList && !getModel().getIsNew()) {
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

    protected void updateMemoryBalloon() {
        if (getModel().getSelectedCluster() != null) {
            VDSGroup cluster = getModel().getSelectedCluster();
            boolean hasMemoryBalloon = (cluster.getcompatibility_version()
                    .compareTo(VmListModel.BALLOON_DEVICE_MIN_VERSION) >= 0
                    && cluster.getArchitecture()
                    .equals(VmListModel.BALLOON_DEVICE_BLACKLISTED_ARCH));

            getModel().getMemoryBalloonDeviceEnabled().setIsAvailable(hasMemoryBalloon);
        }
    }


    protected void updateCpuSharesAvailability() {
        if (getModel().getSelectedCluster() != null) {
            VDSGroup cluster = getModel().getSelectedCluster();
            boolean availableCpuShares = cluster.getcompatibility_version()
                    .compareTo(Version.v3_3) >= 0;
            getModel().getCpuSharesAmountSelection().setIsAvailable(availableCpuShares);
            getModel().getCpuSharesAmount().setIsAvailable(availableCpuShares);
        }
    }

    protected void updateVirtioScsiAvailability() {
        VDSGroup cluster = getModel().getSelectedCluster();
        boolean isVirtioScsiEnabled = (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                ConfigurationValues.VirtIoScsiEnabled, cluster.getcompatibility_version().getValue());
        getModel().getIsVirtioScsiEnabled().setIsAvailable(isVirtioScsiEnabled);
    }

    protected void setupTemplate(Guid templateId, final boolean useLatest) {
        AsyncDataProvider.getTemplateById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        VmTemplate template = (VmTemplate) returnValue;

                        if (useLatest) {
                            template = createLatestTemplate(template);
                        }

                        setupBaseTemplate(template.getBaseTemplateId());

                        model.getTemplate().setItems(Collections.singletonList(template));
                        model.getTemplate().setSelectedItem(template);
                        model.getTemplate().setIsChangable(false);
                        if (!template.getId().equals(Guid.Empty)) {
                            getModel().getVmInitModel().init(template);
                        }
                    }
                },
                getModel().getHash()),
                templateId);
    }

    protected void setupBaseTemplate(Guid baseTemplateId) {
        AsyncDataProvider.getTemplateById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        VmTemplate template = (VmTemplate) returnValue;

                        model.getBaseTemplate().setItems(Collections.singletonList(template));
                        model.getBaseTemplate().setSelectedItem(template);
                        model.getBaseTemplate().setIsChangable(false);
                    }
                },
                getModel().getHash()),
                baseTemplateId);
    }

    protected void updateCpuPinningVisibility() {
        if (getModel().getSelectedCluster() != null) {
            VDSGroup cluster = getModel().getSelectedCluster();
            String compatibilityVersion = cluster.getcompatibility_version().toString();
            boolean isLocalSD = getModel().getSelectedDataCenter() != null
                    && getModel().getSelectedDataCenter().isLocal();

            // cpu pinning is available on Local SD with no consideration for auto assign value
            boolean hasCpuPinning = Boolean.FALSE.equals(getModel().getIsAutoAssign().getEntity()) || isLocalSD;

            if (Boolean.FALSE.equals(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.CpuPinningEnabled,
                    compatibilityVersion))) {
                hasCpuPinning = false;
            } else if (Boolean.FALSE.equals(AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.CpuPinMigrationEnabled,
                    AsyncDataProvider.getDefaultConfigurationVersion()))
                    && isVmMigratable()
                    && !isLocalSD) {
                hasCpuPinning = false;
            }

            if (!hasCpuPinning) {
                if(isLocalSD) {
                    getModel().getCpuPinning().setChangeProhibitionReason(constants.cpuPinningUnavailableLocalStorage());
                } else {
                    getModel().getCpuPinning().setChangeProhibitionReason(constants.cpuPinningUnavailable());
                }
                getModel().getCpuPinning().setEntity("");
            }
            getModel().getCpuPinning().setIsChangable(hasCpuPinning);
        }
    }

    public void updateUseHostCpuAvailability() {

        boolean clusterSupportsHostCpu =
                getModel().getSelectedCluster() != null
                        && (getModel().getSelectedCluster()).getcompatibility_version()
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

    public void updateHaAvailability() {
        boolean automaticMigrationAllowed = getModel().getMigrationMode().getSelectedItem()
                == MigrationSupport.MIGRATABLE;
        if (!automaticMigrationAllowed) {
            getModel().getIsHighlyAvailable().setChangeProhibitionReason(constants.hostNonMigratable());
            getModel().getIsHighlyAvailable().setEntity(false);
        }
        getModel().getIsHighlyAvailable().setIsChangable(automaticMigrationAllowed);
    }

    public void updateMigrationAvailability() {
        Boolean haHost = getModel().getIsHighlyAvailable().getEntity();
        if (haHost) {
            getModel().getMigrationMode().setChangeProhibitionReason(constants.hostIsHa());
            getModel().getMigrationMode().setSelectedItem(MigrationSupport.MIGRATABLE);
        }
        getModel().getMigrationMode().setIsChangable(!haHost);

    }

    public void updateCpuSharesAmountChangeability() {
        boolean changeable =
                getModel().getCpuSharesAmountSelection().getSelectedItem() == UnitVmModel.CpuSharesAmount.CUSTOM;
        boolean none =
                getModel().getCpuSharesAmountSelection().getSelectedItem() == UnitVmModel.CpuSharesAmount.DISABLED;
        getModel().getCpuSharesAmount()
                .setEntity(changeable || none
                        ? null //$NON-NLS-1$
                        : getModel().getCpuSharesAmountSelection().getSelectedItem().getValue());
    }

    public void updateCpuSharesSelection() {
        boolean foundEnum = false;
        for (UnitVmModel.CpuSharesAmount cpuSharesAmount : UnitVmModel.CpuSharesAmount.values()) {
            if (cpuSharesAmount.getValue() == getModel().getCpuSharesAmount().getEntity()) {
                getModel().getCpuSharesAmountSelection().setSelectedItem(cpuSharesAmount);
                foundEnum = true;
                break;
            }
        }
        if (!foundEnum) {
            // saving the value - because when Custom is selected the value automatically clears.
            Integer currentVal = getModel().getCpuSharesAmount().getEntity();
            getModel().getCpuSharesAmountSelection().setSelectedItem(UnitVmModel.CpuSharesAmount.CUSTOM);
            getModel().getCpuSharesAmount().setEntity(currentVal);
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
    protected void composeCoresAndSocketsWhenDontFitInto(int totalCpuCores) {
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
                    .getEntity()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected int extractIntFromListModel(ListModel model) {
        return model.getSelectedItem() != null ? Integer.parseInt(model
                .getSelectedItem()
                .toString())
                : 0;
    }

    private void initListToOne(ListModel list) {
        list.setItems(Arrays.asList(1));
        list.setSelectedItem(1);
    }

    protected void updateNumOfSockets()
    {
        VDSGroup cluster = getModel().getSelectedCluster();
        if (cluster == null)
        {
            return;
        }

        String version = cluster.getcompatibility_version().toString();

        AsyncDataProvider.getMaxNumOfVmSockets(new AsyncQuery(new Object[]{this, getModel()},
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) array[0];
                        behavior.maxNumOfSockets = ((Integer) returnValue);
                        behavior.updataMaxVmsInPool();
                    }
                }, getModel().getHash()), version);
    }

    /**
     * Returns a list of integers which can divide the param
     */
    protected List<Integer> findIndependentPossibleValues(int max) {
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
    protected List<Integer> filterPossibleValues(List<Integer> candidates, List<Integer> others) {
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

    protected void updateOSValues() {

        List<Integer> vmOsValues;
        VDSGroup cluster = getModel().getSelectedCluster();

        if (cluster != null) {
            vmOsValues = AsyncDataProvider.getOsIds(cluster.getArchitecture());
            Integer selectedOsId = getModel().getOSType().getSelectedItem();
            getModel().getOSType().setItems(vmOsValues);
            if (selectedOsId != null && vmOsValues.contains(selectedOsId)) {
                getModel().getOSType().setSelectedItem(selectedOsId);
            }

            postOsItemChanged();
        }

    }

    protected void updateSelectedCdImage(VmBase vmBase) {
        getModel().getCdImage().setSelectedItem(vmBase.getIsoPath());
        boolean hasCd = !StringHelper.isNullOrEmpty(vmBase.getIsoPath());
        getModel().getCdImage().setIsChangable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);
    }

    protected void updateConsoleDevice(Guid vmId) {
        Frontend.getInstance().runQuery(VdcQueryType.GetConsoleDevices, new IdQueryParameters(vmId), new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<String> consoleDevices = ((VdcQueryReturnValue)returnValue).getReturnValue();
                getModel().getIsConsoleDeviceEnabled().setEntity(!consoleDevices.isEmpty());
            }
        }));
    }

    protected void updateVirtioScsiEnabled(final Guid vmId, int osId) {
        final VDSGroup cluster = getModel().getSelectedCluster();
        if (cluster == null) {
            return;
        }

        AsyncDataProvider.getDiskInterfaceList(osId, cluster.getcompatibility_version(),
            new AsyncQuery(getModel(), new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {
                    ArrayList<DiskInterface> diskInterfaces = (ArrayList<DiskInterface>) returnValue;
                    boolean isOsSupportVirtioScsi = diskInterfaces.contains(DiskInterface.VirtIO_SCSI);

                    getModel().getIsVirtioScsiEnabled().setIsChangable(isOsSupportVirtioScsi);

                    if (!isOsSupportVirtioScsi) {
                        getModel().getIsVirtioScsiEnabled().setEntity(false);
                        getModel().getIsVirtioScsiEnabled().setChangeProhibitionReason(constants.cannotEnableVirtioScsiForOs());
                    }
                    else {
                        if (Guid.isNullOrEmpty(vmId)) {
                            boolean isVirtioScsiEnabled = (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                                    ConfigurationValues.VirtIoScsiEnabled, cluster.getcompatibility_version().getValue());
                            getModel().getIsVirtioScsiEnabled().setEntity(isVirtioScsiEnabled);
                        }
                        else {
                            AsyncDataProvider.isVirtioScsiEnabledForVm(new AsyncQuery(getModel(), new INewAsyncCallback() {
                                @Override
                                public void onSuccess(Object model, Object returnValue) {
                                    getModel().getIsVirtioScsiEnabled().setEntity((Boolean) returnValue);
                                }
                            }), vmId);
                        }
                    }
                }
            }));
    }

    public void vmTypeChanged(VmType vmType) {
        getModel().getIsSoundcardEnabled().setEntity(vmType == VmType.Desktop);
        getModel().getAllowConsoleReconnect().setEntity(vmType == VmType.Server);
    }

    protected void initSoundCard(Guid id) {
        AsyncDataProvider.isSoundcardEnabled(new AsyncQuery(getModel(), new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                getModel().getIsSoundcardEnabled().setEntity((Boolean) returnValue);
            }
        }), id);
    }

    protected void updateNetworkInterfaces(final ProfileBehavior behavior, final List<VmNetworkInterface> argNics) {
        boolean hotUpdateSupported =
                (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.NetworkLinkingSupported,
                        getModel().getSelectedCluster().getcompatibility_version().toString());

        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<VnicProfileView> profiles = (List<VnicProfileView>) returnValue;
                List<VnicInstanceType> vnicInstanceTypes = new ArrayList<VnicInstanceType>();
                List<VmNetworkInterface> nics = (argNics == null) ? new ArrayList<VmNetworkInterface>() : argNics;

                for (VmNetworkInterface nic : nics) {
                    final VnicInstanceType vnicInstanceType = new VnicInstanceType(nic);
                    vnicInstanceType.setItems(profiles);
                    behavior.initSelectedProfile(vnicInstanceType, vnicInstanceType.getNetworkInterface());
                    vnicInstanceTypes.add(vnicInstanceType);
                }

                getModel().getNicsWithLogicalNetworks().getVnicProfiles().setItems(profiles);
                getModel().getNicsWithLogicalNetworks().setItems(vnicInstanceTypes);
                getModel().getNicsWithLogicalNetworks().setSelectedItem(Linq.firstOrDefault(vnicInstanceTypes));
            }
        });

        behavior.initProfiles(hotUpdateSupported, getModel().getSelectedCluster().getId(), getModel().getSelectedDataCenter().getId(), query);
    }

    public void enableSinglePCI(boolean enabled) {
        getModel().getIsSingleQxlEnabled().setIsChangable(enabled);
        getModel().getIsSingleQxlEnabled().setEntity(enabled);
    }
}
