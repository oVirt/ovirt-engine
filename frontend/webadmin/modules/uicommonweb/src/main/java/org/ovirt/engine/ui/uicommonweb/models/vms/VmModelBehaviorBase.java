package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
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
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.NumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VmNumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.ExistingBlankTemplateModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.templates.LatestVmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public abstract class VmModelBehaviorBase<TModel extends UnitVmModel> {

    // no need to have this configurable
    public static final int DEFAULT_NUM_OF_IOTHREADS = 1;

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private TModel privateModel;

    protected List<String> dedicatedHostsNames;

    private SystemTreeItemModel privateSystemTreeSelectedItem;

    private PriorityUtil priorityUtil;

    private VirtioScsiUtil virtioScsiUtil;

    public int maxCpus = 0;
    protected int maxCpusPerSocket = 0;
    protected int maxNumOfSockets = 0;
    protected int maxThreadsPerCore = 0;

    private int maxVmsInPool = 1000;

    public List<String> getDedicatedHostsNames() {
        return dedicatedHostsNames;
    }

    public void setDedicatedHostsNames(List<String> dedicatedHostsNames) {
        this.dedicatedHostsNames = dedicatedHostsNames;
    }

    public TModel getModel() {
        return privateModel;
    }

    public void setModel(TModel value) {
        privateModel = value;
    }

    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return privateSystemTreeSelectedItem;
    }

    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        privateSystemTreeSelectedItem = value;
    }

    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        this.setSystemTreeSelectedItem(systemTreeSelectedItem);
        commonInitialize();
    }

    /**
     * If someone overrides the initialize not calling the super, at least this has to be called
     */
    protected void commonInitialize() {
        priorityUtil = new PriorityUtil(getModel());
        virtioScsiUtil = new VirtioScsiUtil(getModel());
        getModel().getVmId().setIsAvailable(false);
        getModel().getMigrationPolicies().setItems(AsyncDataProvider.getInstance().getMigrationPolicies());
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

        getModel().getIsRngEnabled().setIsChangeable(true);
        setRngAvailability();

        postDataCenterWithClusterSelectedItemChanged();
    }

    private void setRngAvailability() {
        TModel model = getModel();
        Set<VmRngDevice.Source> requiredRngSources = model.getSelectedCluster().getRequiredRngSources();

        boolean requiredRngSourcesEmpty = requiredRngSources.isEmpty();
        boolean randomSourceAvailable = requiredRngSources.contains(VmRngDevice.Source.RANDOM);
        boolean hwrngSourceAvailable = requiredRngSources.contains(VmRngDevice.Source.HWRNG);

        model.getIsRngEnabled().setIsChangeable(!requiredRngSourcesEmpty);
        model.getRngPeriod().setIsChangeable(!requiredRngSourcesEmpty);
        model.getRngBytes().setIsChangeable(!requiredRngSourcesEmpty);

        if (requiredRngSourcesEmpty) {
            model.getIsRngEnabled().setChangeProhibitionReason(constants.rngNotSupportedByCluster());
            model.getRngPeriod().setChangeProhibitionReason(constants.rngNotSupportedByCluster());
            model.getRngBytes().setChangeProhibitionReason(constants.rngNotSupportedByCluster());
        }

        model.getRngSourceRandom().setIsChangeable(randomSourceAvailable);
        if (!randomSourceAvailable) {
            model.getRngSourceRandom().setChangeProhibitionReason(messages.rngSourceNotSupportedByCluster(VmRngDevice.Source.RANDOM.toString()));
        }

        model.getRngSourceHwrng().setIsChangeable(hwrngSourceAvailable);
        if (!hwrngSourceAvailable) {
            model.getRngSourceHwrng().setChangeProhibitionReason(messages.rngSourceNotSupportedByCluster(VmRngDevice.Source.HWRNG.toString()));
        }
    }

    protected void updateMigrationForLocalSD() {
        boolean isLocalSD =
                getModel().getSelectedDataCenter() != null
                        && getModel().getSelectedDataCenter().isLocal();
        if(isLocalSD) {
            getModel().getIsAutoAssign().setEntity(false);
            getModel().getMigrationMode().setSelectedItem(MigrationSupport.PINNED_TO_HOST);
        }
        getModel().getIsAutoAssign().setIsChangeable(!isLocalSD);
        getModel().getMigrationMode().setIsChangeable(!isLocalSD);
        getModel().getDefaultHost().setIsChangeable(!isLocalSD);
    }

    protected void buildModel(VmBase vmBase,
                              BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
    }

    public void templateWithVersion_SelectedItemChanged() {}

    public abstract void postDataCenterWithClusterSelectedItemChanged();

    public abstract void defaultHost_SelectedItemChanged();

    public abstract void provisioning_SelectedItemChanged();

    public void oSType_SelectedItemChanged() {
    }

    public void updateMinAllocatedMemory() {
    }

    public void deactivateInstanceTypeManager(InstanceTypeManager.ActivatedListener activatedListener) {
        if (getInstanceTypeManager() != null) {
            getInstanceTypeManager().deactivate(activatedListener);
        }
    }

    public void deactivateInstanceTypeManager() {
        if (getInstanceTypeManager() != null) {
            getInstanceTypeManager().deactivate();
        }
    }

    public void activateInstanceTypeManager() {
        if (getInstanceTypeManager() != null) {
            getInstanceTypeManager().activate();
        }
    }

    public boolean instanceTypeActive() {
        return getInstanceTypeManager() != null ? getInstanceTypeManager().isActive() : false;
    }

    protected InstanceTypeManager getInstanceTypeManager() {
        return null;
    }

    protected void postOsItemChanged() {

    }

    protected void baseTemplateSelectedItemChanged() {
    }

    protected void initTemplateWithVersion(List<VmTemplate> templates, Guid previousTemplateId, boolean useLatest) {
        initTemplateWithVersion(templates, previousTemplateId, useLatest, true);
    }

    /**
     *
     * @param templates empty list is allowed
     * @param previousTemplateId template ID to select, if null -> autodetect based on the model (ignored if latest is set)
     * @param useLatest if true, explicitly selects the latest template
     * @param addLatest if add to all templates also the "latest" or not
     */
    protected void initTemplateWithVersion(List<VmTemplate> templates, Guid previousTemplateId, boolean useLatest, boolean addLatest) {
        List<TemplateWithVersion> templatesWithVersion = createTemplateWithVersionsAddLatest(templates, addLatest);
        if (previousTemplateId == null && !useLatest) {
            TemplateWithVersion previouslySelectedTemplate = getModel().getTemplateWithVersion().getSelectedItem();
            if (previouslySelectedTemplate != null && previouslySelectedTemplate.getTemplateVersion() != null) {
                previousTemplateId = previouslySelectedTemplate.getTemplateVersion().getId();
                useLatest = previouslySelectedTemplate.getTemplateVersion() instanceof LatestVmTemplate;
            }
        }
        TemplateWithVersion templateToSelect =
                computeTemplateWithVersionToSelect(templatesWithVersion, previousTemplateId, useLatest, addLatest);
        getModel().getTemplateWithVersion().setItems(templatesWithVersion, templateToSelect);
    }

    private static TemplateWithVersion computeTemplateWithVersionToSelect(
            List<TemplateWithVersion> newItems,
            Guid previousTemplateId, boolean useLatest, boolean addLatest) {
        if (previousTemplateId == null) {
            return computeNewTemplateWithVersionToSelect(newItems, addLatest);
        }
        TemplateWithVersion oldTemplateToSelect = Linq.firstOrNull(
                newItems,
                new Linq.TemplateWithVersionPredicate(previousTemplateId, useLatest));
        return oldTemplateToSelect != null
                ? oldTemplateToSelect
                : computeNewTemplateWithVersionToSelect(newItems, addLatest);
    }

    /**
     * It prefers to select second element (usually [Blank-1]) to the first one (usually [Blank-latest]).
     * If the latest has not been added, just return the first one
     */
    private static TemplateWithVersion computeNewTemplateWithVersionToSelect(List<TemplateWithVersion> newItems, boolean addLatest) {
        if (newItems.isEmpty()) {
            return null;
        }

        if (addLatest) {
            return newItems.size() >= 2
                    ? newItems.get(1)
                    : newItems.get(0);
        }

        return newItems.get(0);
    }

    /**
     *
     * @param templates raw templates from backend, latest not included
     * @return model ready for 'Template' comobox, including latest
     */
    private static List<TemplateWithVersion> createTemplateWithVersionsAddLatest(List<VmTemplate> templates, boolean addLatest) {
        final Map<Guid, VmTemplate> baseIdToBaseTemplateMap = new HashMap<>();
        final Map<Guid, VmTemplate> baseIdToLastVersionMap = new HashMap<>();
        for (VmTemplate template : templates) {
            if (template.isBaseTemplate())  {
                baseIdToBaseTemplateMap.put(template.getId(), template);
                baseIdToLastVersionMap.put(template.getId(), template);
            }
        }
        final List<TemplateWithVersion> result = new ArrayList<>();
        for (VmTemplate template : templates) {
            // update last version map
            if (baseIdToLastVersionMap.get(template.getBaseTemplateId()).getTemplateVersionNumber() < template.getTemplateVersionNumber()) {
                baseIdToLastVersionMap.put(template.getBaseTemplateId(), template);
            }

            final VmTemplate baseTemplate = baseIdToBaseTemplateMap.get(template.getBaseTemplateId());
            result.add(new TemplateWithVersion(baseTemplate, template));
        }

        // add latest
        if (addLatest) {
            for (Map.Entry<Guid, VmTemplate> pair : baseIdToLastVersionMap.entrySet()) {
                VmTemplate baseTemplate = baseIdToBaseTemplateMap.get(pair.getKey());
                VmTemplate latestTemplate = new LatestVmTemplate(pair.getValue());
                result.add(new TemplateWithVersion(baseTemplate, latestTemplate));
            }
        }

        Collections.sort(result);
        return result;
    }

    protected static List<VmTemplate> keepBaseTemplates(List<VmTemplate> templates) {
        List<VmTemplate> baseTemplates = new ArrayList<>();
        for (VmTemplate template : templates) {
            if (template.isBaseTemplate()) {
                baseTemplates.add(template);
            }
        }
        return baseTemplates;
    }

    protected void isSubTemplateEntityChanged() {
    }

    public boolean validate() {
        return true;
    }

    public int getMaxVmsInPool() {
        return maxVmsInPool;
    }

    public void setMaxVmsInPool(int maxVmsInPool) {
        this.maxVmsInPool = maxVmsInPool;
    }

    protected void updateUserCdImage(Guid storagePoolId) {
        AsyncDataProvider.getInstance().getIrsImageList(new AsyncQuery(getModel(), new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        UnitVmModel model = (UnitVmModel) target;
                        List<String> images = (List<String>) returnValue;
                        setImagesToModel(model, images);
                    }

                }),
                storagePoolId
        );
    }

    protected void setImagesToModel(UnitVmModel model, List<String> images) {
        String oldCdImage = model.getCdImage().getSelectedItem();
        model.getCdImage().setItems(images);
        model.getCdImage().setSelectedItem((oldCdImage != null) ? oldCdImage
                : Linq.firstOrNull(images));
    }

    public void refreshCdImages() {
        updateCdImage(true);
    }

    protected void updateCdImage() {
        updateCdImage(false);
    }

    protected void updateCdImage(boolean forceRefresh) {
        StoragePool dataCenter = getModel().getSelectedDataCenter();
        if (dataCenter == null) {
            return;
        }

        AsyncDataProvider.getInstance().getIrsImageList(new AsyncQuery(getModel(),
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                UnitVmModel model = (UnitVmModel) target;
                                ArrayList<String> images = (ArrayList<String>) returnValue;
                                setImagesToModel(model, images);

                            }
                        }),
                dataCenter.getId(),
                forceRefresh);

    }

    protected void updateTimeZone(final String selectedTimeZone) {
        if (StringHelper.isNullOrEmpty(selectedTimeZone)) {
            updateDefaultTimeZone();
        } else {
            doUpdateTimeZone(selectedTimeZone);
        }
    }

    protected void updateDefaultTimeZone() {
        doUpdateTimeZone(null);
    }

    private void doUpdateTimeZone(final String selectedTimeZone) {
        final Collection<TimeZoneModel> timeZones = TimeZoneModel.getTimeZones(getTimeZoneType());
        getModel().getTimeZone().setItems(timeZones);
        getModel().getTimeZone().setSelectedItem(Linq.firstOrNull(timeZones,
                new Linq.TimeZonePredicate(selectedTimeZone)));
    }

    protected void initPriority(int priority) {
        priorityUtil.initPriority(priority, new PriorityUtil.PriorityUpdatingCallbacks() {
            @Override
            public void beforeUpdates() {
                if (getInstanceTypeManager() != null) {
                    getInstanceTypeManager().deactivate();
                }
            }

            @Override
            public void afterUpdates() {
                if (getInstanceTypeManager() != null) {
                    getInstanceTypeManager().activate();
                }
            }
        });
    }

    public TimeZoneType getTimeZoneType() {
        // can be null as a consequence of setItems on ListModel
        Integer vmOsType = getModel().getOSType().getSelectedItem();
        return AsyncDataProvider.getInstance().isWindowsOsType(vmOsType) ? TimeZoneType.WINDOWS_TIMEZONE
                : TimeZoneType.GENERAL_TIMEZONE;
    }

    protected void changeDefaultHost() {
    }

    protected void doChangeDefaultHost(List<Guid> dedicatedHostIds) {
        getModel().getIsAutoAssign().setEntity(true);
        if (dedicatedHostIds == null) {
            return;
        }

        if (getModel().getDefaultHost().getItems() != null) {
            List<VDS> selectedHosts = new ArrayList<>();
            for (VDS host: getModel().getDefaultHost().getItems()) {
                if (dedicatedHostIds.contains(host.getId())) {
                    selectedHosts.add(host);
                }
            }
            if (!selectedHosts.isEmpty()) {
                getModel().getDefaultHost().setSelectedItems(selectedHosts);
                getModel().getIsAutoAssign().setEntity(false);
            }
        }
    }

    protected void updateDefaultHost() {
        Cluster cluster = getModel().getSelectedCluster();
        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        if (cluster == null) {
            getModel().getDefaultHost().setItems(new ArrayList<VDS>());
            getModel().getDefaultHost().setSelectedItems(new ArrayList<VDS>());
            return;
        }

        AsyncQuery query = new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        List<VDS> hosts = null;
                        if (returnValue instanceof List) {
                            hosts = (List<VDS>) returnValue;
                        } else if (returnValue instanceof VdcQueryReturnValue
                                && ((VdcQueryReturnValue) returnValue).getReturnValue() instanceof List) {
                            hosts = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        } else {
                            throw new IllegalArgumentException("The return value should be List<VDS> or VdcQueryReturnValue with return value List<VDS>"); //$NON-NLS-1$
                        }

                        List<VDS> oldDefaultHosts = model.getDefaultHost().getSelectedItems();
                        if (model.getBehavior().getSystemTreeSelectedItem() != null
                                && model.getBehavior().getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host) {
                            VDS host = (VDS) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                            for (VDS vds : hosts) {
                                if (host.getId().equals(vds.getId())) {
                                    model.getDefaultHost()
                                            .setItems(new ArrayList<>(Collections.singletonList(vds)));
                                    model.getDefaultHost().setSelectedItems(Collections.singletonList(vds));
                                    model.getDefaultHost().setIsChangeable(false);
                                    model.getDefaultHost().setChangeProhibitionReason(constants.cannotChangeHostInTreeContext());
                                    break;
                                }
                            }
                        }
                        else {
                            model.getDefaultHost().setItems(hosts);

                            // attempt to preserve selection as much as possible
                            if (oldDefaultHosts != null && !oldDefaultHosts.isEmpty()) {
                                Set<VDS> oldSelectedIntersectionNewHosts = new HashSet<>(oldDefaultHosts);
                                oldSelectedIntersectionNewHosts.retainAll(hosts);
                                oldDefaultHosts = new ArrayList<>(oldSelectedIntersectionNewHosts);
                            }

                            List<VDS> hostsToSelect = oldDefaultHosts != null && !oldDefaultHosts.isEmpty()
                                              ? oldDefaultHosts
                                              : !hosts.isEmpty()
                                                      ? Collections.singletonList(hosts.get(0))
                                                      : Collections.<VDS>emptyList();
                            model.getDefaultHost().setSelectedItems(hostsToSelect);
                        }
                        changeDefaultHost();

                    }
                });

        getHostListByCluster(cluster, query);
    }

    /**
     * By default admin query is fired, UserPortal overrides it to fire user query
     */
    protected void getHostListByCluster(Cluster cluster, AsyncQuery query) {
        AsyncDataProvider.getInstance().getHostListByCluster(query, cluster.getName());
    }

    protected void updateCustomPropertySheet() {
        if (getModel().getCompatibilityVersion() != null) {
            updateCustomPropertySheet(getModel().getCompatibilityVersion());
        }
    }

    protected void updateCustomPropertySheet(Version clusterVersion) {
        getModel().getCustomPropertySheet().setKeyValueMap(
                getModel().getCustomPropertiesKeysList().get(clusterVersion)
        );
    }

    public void updataMaxVmsInPool() {
        AsyncDataProvider.getInstance().getMaxVmsInPool(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;
                        behavior.setMaxVmsInPool((Integer) returnValue);
                        behavior.updateMaxNumOfVmCpus();
                    }
                }
        ));
    }

    public void updateMaxNumOfVmCpus() {
        String version = getCompatibilityVersion().toString();

        AsyncDataProvider.getInstance().getMaxNumOfVmCpus(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = VmModelBehaviorBase.this;
                        behavior.maxCpus = (Integer) returnValue;
                        behavior.postUpdateNumOfSockets2();
                    }
                }), version);
    }

    public void postUpdateNumOfSockets2() {
        String version = getCompatibilityVersion().toString();

        AsyncDataProvider.getInstance().getMaxNumOfCPUsPerSocket(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = VmModelBehaviorBase.this;
                        behavior.maxCpusPerSocket = (Integer) returnValue;
                        behavior.postUpdateNumOfSockets3();
                    }
                }), version);
    }

    public void postUpdateNumOfSockets3() {
        String version = getCompatibilityVersion().toString();

        AsyncDataProvider.getInstance().getMaxNumOfThreadsPerCpu(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmModelBehaviorBase behavior = VmModelBehaviorBase.this;
                        behavior.maxThreadsPerCore = (Integer) returnValue;

                        behavior.totalCpuCoresChanged();
                    }
                }), version);
    }

    public void initDisks() {
        VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();

        AsyncDataProvider.getInstance().getTemplateDiskList(new AsyncQuery(getModel(),
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                initTemplateDisks((List<DiskImage>) returnValue);
                            }
                        }),
                template.getId());
    }

    protected void initTemplateDisks(List<DiskImage> disks) {
        Collections.sort(disks, new DiskByDiskAliasComparator());
        ArrayList<DiskModel> list = new ArrayList<>();

        for (DiskImage disk : disks) {
            DiskModel diskModel = new DiskModel();
            diskModel.getAlias().setEntity(disk.getDiskAlias());
            diskModel.getVolumeType().setIsAvailable(false);

            switch (disk.getDiskStorageType()) {
                case IMAGE:
                    diskModel.setSize(new EntityModel<>((int) disk.getSizeInGigabytes()));
                    ListModel volumes = new ListModel();
                    volumes.setItems(disk.getVolumeType() == VolumeType.Preallocated ? new ArrayList<>(Arrays.asList(new VolumeType[]{VolumeType.Preallocated}))
                            : AsyncDataProvider.getInstance().getVolumeTypeList(), disk.getVolumeType());
                    diskModel.setVolumeType(volumes);
                    break;
                case CINDER:
                    CinderDisk cinderDisk = (CinderDisk) disk;
                    diskModel.setSize(new EntityModel<>((int) cinderDisk.getSizeInGigabytes()));
                    ListModel volumeTypes = new ListModel();
                    volumeTypes.setItems(new ArrayList<>(Arrays.asList(cinderDisk.getVolumeType())), cinderDisk.getVolumeType());
                    diskModel.setVolumeType(volumeTypes);
                    ListModel volumeFormats = new ListModel();
                    volumeFormats.setItems(new ArrayList<>(Arrays.asList(cinderDisk.getVolumeFormat())), cinderDisk.getVolumeFormat());
                    diskModel.setVolumeFormat(volumeFormats);
                    break;
            }

            diskModel.setDisk(disk);
            list.add(diskModel);
        }

        getModel().setDisks(list);
        updateIsDisksAvailable();
        initStorageDomains();
    }

    public void updateIsDisksAvailable() {

    }

    public void initStorageDomains() {
        if (getModel().getDisks() == null) {
            return;
        }

        TemplateWithVersion templateWithVersion = getModel().getTemplateWithVersion().getSelectedItem();

        if (templateWithVersion != null && !templateWithVersion.getTemplateVersion().getId().equals(Guid.Empty)) {
            postInitStorageDomains();
        }
        else {
            getModel().getStorageDomain().setItems(new ArrayList<StorageDomain>());
            getModel().getStorageDomain().setSelectedItem(null);
            getModel().getStorageDomain().setIsChangeable(false);
        }
    }

    protected void postInitStorageDomains() {
        if (getModel().getDisks() == null) {
            return;
        }

        ActionGroup actionGroup = getModel().isCreateInstanceOnly() ? ActionGroup.CREATE_INSTANCE : ActionGroup.CREATE_VM;
        StoragePool dataCenter = getModel().getSelectedDataCenter();
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VmModelBehaviorBase behavior = VmModelBehaviorBase.this;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                ArrayList<StorageDomain> activeStorageDomains = filterStorageDomains(storageDomains);

                boolean provisioning = behavior.getModel().getProvisioning().getEntity();
                ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                Collections.sort(activeStorageDomains, new NameableComparator());

                ArrayList<DiskModel> diskImages = Linq.filterDisksByType(disks, DiskStorageType.IMAGE);
                for (DiskModel diskModel : diskImages) {
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
                    diskModel.getStorageDomain().setIsChangeable(!availableDiskStorageDomains.isEmpty());
                }
                ArrayList<DiskModel> cinderDisks = Linq.filterDisksByType(disks, DiskStorageType.CINDER);
                Collection<StorageDomain> cinderStorageDomains =
                        Linq.filterStorageDomainsByStorageType(storageDomains, StorageType.CINDER);
                initStorageDomainsForCinderDisks(cinderDisks, cinderStorageDomains);
            }
        }), dataCenter.getId(), actionGroup);
    }

    private void initStorageDomainsForCinderDisks(ArrayList<DiskModel> cinderDisks, Collection<StorageDomain> cinderStorageDomains) {
        for (DiskModel diskModel : cinderDisks) {
            CinderDisk cinderDisk = (CinderDisk) diskModel.getDisk();
            diskModel.getStorageDomain().setItems(Linq.filterStorageDomainById(
                    cinderStorageDomains, cinderDisk.getStorageIds().get(0)));
            diskModel.getStorageDomain().setIsChangeable(false);
            diskModel.getDiskProfile().setIsChangeable(false);
            diskModel.getDiskProfile().setChangeProhibitionReason(
                    ConstantsManager.getInstance().getConstants().notSupportedForCinderDisks());
        }
    }

    public ArrayList<StorageDomain> filterStorageDomains(ArrayList<StorageDomain> storageDomains) {
        // filter only the Active storage domains (Active regarding the relevant storage pool).
        ArrayList<StorageDomain> list = new ArrayList<>();
        for (StorageDomain a : storageDomains) {
            if (Linq.isDataActiveStorageDomain(a)) {
                list.add(a);
            }
        }

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage) {
            StorageDomain selectStorage = (StorageDomain) getSystemTreeSelectedItem().getEntity();
            StorageDomain sd = Linq.firstOrNull(list, new Linq.IdPredicate<>(selectStorage.getId()));
            list = new ArrayList<>(Arrays.asList(new StorageDomain[]{sd}));
        }

        return list;
    }

    protected void updateQuotaByCluster(final Guid defaultQuota, final String quotaName) {
        if (getModel().getQuota().getIsAvailable()) {
            Cluster cluster = getModel().getSelectedCluster();
            if (cluster == null) {
                return;
            }
            AsyncDataProvider.getInstance().getAllRelevantQuotasForClusterSorted(new AsyncQuery(getModel(),
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object returnValue) {
                            UnitVmModel vmModel = (UnitVmModel) model;
                            List<Quota> quotaList =  (List<Quota>) returnValue;
                            if (quotaList == null) {
                                return;
                            }

                            if (!quotaList.isEmpty()) {
                                vmModel.getQuota().setItems(quotaList);
                            }
                            if (defaultQuota != null && !Guid.Empty.equals(defaultQuota)) {
                                boolean hasQuotaInList = false;
                                if (!quotaList.isEmpty()) {
                                    hasQuotaInList = defaultQuota.equals(quotaList.get(0).getId());
                                }

                                // Add the quota to the list only in edit mode
                                if (!hasQuotaInList && !getModel().getIsNew()) {
                                    Quota quota = new Quota();
                                    quota.setId(defaultQuota);
                                    quota.setQuotaName(quotaName);
                                    quotaList.add(0, quota);
                                    vmModel.getQuota().setItems(quotaList);
                                    vmModel.getQuota().setSelectedItem(quota);
                                }
                            }
                        }
                    }), cluster.getId(), defaultQuota);
        }
    }

    protected void updateMemoryBalloon() {
        Cluster cluster = getModel().getSelectedCluster();
        Integer osType = getModel().getOSType().getSelectedItem();

        if (cluster != null && osType != null) {
            updateMemoryBalloon(getModel().getCompatibilityVersion(), osType);
        }
    }

    protected void updateMemoryBalloon(Version clusterVersion, int osType) {
        boolean isBalloonEnabled = AsyncDataProvider.getInstance().isBalloonEnabled(osType,
                clusterVersion);

        if (!isBalloonEnabled) {
            getModel().getMemoryBalloonDeviceEnabled().setEntity(false);
        }
        getModel().getMemoryBalloonDeviceEnabled().setIsAvailable(isBalloonEnabled);

    }

    protected void updateCpuSharesAvailability() {
        if (getModel().getSelectedCluster() != null) {
            getModel().getCpuSharesAmountSelection().setIsAvailable(true);
            getModel().getCpuSharesAmount().setIsAvailable(true);
        }
    }

    protected void updateVirtioScsiAvailability() {
        getModel().getIsVirtioScsiEnabled().setIsAvailable(true);
    }

    protected void setupTemplateWithVersion(final Guid templateId,
            final boolean useLatest,
            final boolean isVersionChangeable) {
        AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery(null,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object nothing, Object returnValue) {
                                VmTemplate rawTemplate = (VmTemplate) returnValue;

                                if (isVersionChangeable) {
                                    // only used by pools therefore query is limited to admin-portal permissions.
                                    AsyncDataProvider.getInstance().getVmTemplatesByBaseTemplateId(
                                            new AsyncQuery(getModel(), new INewAsyncCallback() {
                                                @Override
                                                public void onSuccess(Object target, Object returnValue) {
                                                    ArrayList<VmTemplate> templatesChain =
                                                            new ArrayList<>((List<VmTemplate>) returnValue);
                                                    initTemplateWithVersion(templatesChain, templateId, useLatest);
                                                }
                                            }), rawTemplate.getBaseTemplateId());
                                } else {
                                    final VmTemplate template = useLatest
                                            ? new LatestVmTemplate(rawTemplate)
                                            : rawTemplate;
                                    if (template.isBaseTemplate()) {
                                        TemplateWithVersion templateCouple =
                                                new TemplateWithVersion(template, template);
                                        setReadOnlyTemplateWithVersion(templateCouple);
                                    } else {
                                        AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery(null,
                                                        new INewAsyncCallback() {
                                                            @Override
                                                            public void onSuccess(Object nothing, Object returnValue) {
                                                                VmTemplate baseTemplate = (VmTemplate) returnValue;
                                                                TemplateWithVersion templateCouple =
                                                                        new TemplateWithVersion(baseTemplate, template);
                                                                setReadOnlyTemplateWithVersion(templateCouple);
                                                            }
                                                        }),
                                                template.getBaseTemplateId());
                                    }
                                }
                            }
                        }),
                templateId
        );
    }

    protected void setReadOnlyTemplateWithVersion(TemplateWithVersion templateCouple) {
        getModel().getTemplateWithVersion().setItems(Collections.singleton(templateCouple));
        getModel().getTemplateWithVersion().setSelectedItem(templateCouple);
        getModel().getTemplateWithVersion().setIsChangeable(false);

    }

    protected void updateCpuPinningVisibility() {
        if (getModel().getSelectedCluster() != null) {
            boolean isLocalSD = getModel().getSelectedDataCenter() != null
                    && getModel().getSelectedDataCenter().isLocal();

            // cpu pinning is available on Local SD with no consideration for auto assign value
            boolean hasCpuPinning = Boolean.FALSE.equals(getModel().getIsAutoAssign().getEntity()) || isLocalSD;

            if (Boolean.FALSE.equals(AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.CpuPinMigrationEnabled,
                    AsyncDataProvider.getInstance().getDefaultConfigurationVersion()))
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
            getModel().getCpuPinning().setIsChangeable(hasCpuPinning);
        }
    }

    /**
     * A VM which is set to use pass through host cpu cann't migrate, otherwise all migrationMode are valid
     */
    public void updateUseHostCpuAvailability() {

        boolean clusterSupportsHostCpu = getCompatibilityVersion() != null;
        boolean nonMigratable = MigrationSupport.PINNED_TO_HOST == getModel().getMigrationMode().getSelectedItem();

        if (clusterSupportsHostCpu && nonMigratable && !clusterHasPpcArchitecture()) {
            getModel().getHostCpu().setIsChangeable(true);
        } else {
            getModel().getHostCpu().setEntity(false);
            getModel().getHostCpu().setChangeProhibitionReason(constants.hosCPUUnavailable());
            getModel().getHostCpu().setIsChangeable(false);
        }
    }

    private boolean clusterHasPpcArchitecture() {
        Cluster cluster = getModel().getSelectedCluster();

        return cluster != null
                && cluster.getArchitecture() != null
                && ArchitectureType.ppc == cluster.getArchitecture().getFamily();
    }

    public void updateHaAvailability() {
        boolean automaticMigrationAllowed = getModel().getMigrationMode().getSelectedItem()
                == MigrationSupport.MIGRATABLE;
        final Collection<VDS> allowedHosts = getModel().getDefaultHost().getSelectedItems();
        Collection<VDS> presentHosts = getModel().getDefaultHost().getItems();
        int pinToHostSize = allowedHosts == null ? 0 : allowedHosts.size();
        boolean isHighlyAvailable = getModel().getIsHighlyAvailable().getEntity();
        Boolean isAutoAssign = getModel().getIsAutoAssign().getEntity();

        // This is needed for the unittests to not crash..
        if (presentHosts == null) {
            presentHosts = new ArrayList<>();
        }

        if (!automaticMigrationAllowed
                && (pinToHostSize == 1
                    || (pinToHostSize == 0 && presentHosts.size() < 2))
                && (!isAutoAssign || presentHosts.size() < 2)
                && !isHighlyAvailable) {
            getModel().getIsHighlyAvailable().setChangeProhibitionReason(constants.hostNonMigratable());
            getModel().getIsHighlyAvailable().setEntity(false);
            isHighlyAvailable = false;
        }

        getModel().getIsHighlyAvailable().setIsChangeable(isHighlyAvailable
                || automaticMigrationAllowed
                || (isAutoAssign && presentHosts.size() >= 2)
                || pinToHostSize >= 2
                || (pinToHostSize == 0 && presentHosts.size() >= 2));
    }

    public void updateMigrationAvailability() {
        if (getModel().getIsHighlyAvailable().getEntity() == null
                || getModel().getDefaultHost().getItems() == null
                || getModel().getIsAutoAssign().getEntity() == null) {
            return;
        }
        final boolean haHost = getModel().getIsHighlyAvailable().getEntity();
        final Collection<VDS> allowedHosts = getModel().getDefaultHost().getSelectedItems();
        Collection<VDS> presentHosts = getModel().getDefaultHost().getItems();
        int pinToHostSize = allowedHosts == null ? 0 : allowedHosts.size();
        final boolean isAutoAssign = getModel().getIsAutoAssign().getEntity();

        // This is needed for the unittests to not crash..
        if (presentHosts == null) {
            presentHosts = new ArrayList<>();
        }

        if (haHost
                && (pinToHostSize == 1
                    || (pinToHostSize == 0 && presentHosts.size() < 2))
                && (!isAutoAssign || presentHosts.size() < 2)) {
            getModel().getMigrationMode().setChangeProhibitionReason(constants.hostIsHa());
            getModel().getMigrationMode().setSelectedItem(MigrationSupport.MIGRATABLE);
        }
        getModel().getMigrationMode().setIsChangeable(!haHost
                || (isAutoAssign && presentHosts.size() >= 2)
                || pinToHostSize >= 2
                || (pinToHostSize == 0 && presentHosts.size() >= 2));
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

        getModel().getCpuSharesAmount().setIsChangeable(changeable);
    }

    public void updateCpuSharesSelection() {
        boolean foundEnum = false;
        for (UnitVmModel.CpuSharesAmount cpuSharesAmount : UnitVmModel.CpuSharesAmount.values()) {
            // this has to be done explicitly otherwise it fails on NPE on unboxing since the cpuSharesAmount.getValue() is a small int
            int cpuShares = getModel().getCpuSharesAmount().getEntity() != null ? getModel().getCpuSharesAmount().getEntity() : 0;
            if (cpuSharesAmount.getValue() == cpuShares) {
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
        final int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());
        final int totalCpuCores = getTotalCpuCores();

        if (numOfSockets == 0 || totalCpuCores == 0) {
            return;
        }

        // preselect the first threads/core option
        getModel().getThreadsPerCore().setSelectedItem(1);

        // and compute corresponding coresPerSocket
        getModel().getCoresPerSocket().setSelectedItem(totalCpuCores / numOfSockets);

        setCpuChangeability();
    }

    public void coresPerSocketChanged() {
        final int coresPerSocket = extractIntFromListModel(getModel().getCoresPerSocket());
        int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());
        final int totalCpuCores = getTotalCpuCores();

        if (coresPerSocket == 0 || numOfSockets == 0 || totalCpuCores == 0) {
            return;
        }

        if (numOfSockets * coresPerSocket > totalCpuCores) {// decrease the number of sockets, threads can be 1
            numOfSockets = totalCpuCores / coresPerSocket;
            getModel().getNumOfSockets().setSelectedItem(numOfSockets);
        }

        int threadsPerCore = totalCpuCores / (coresPerSocket * numOfSockets);
        getModel().getThreadsPerCore().setSelectedItem(threadsPerCore);
    }

    public void threadsPerCoreChanged() {
        final int threadsPerCore = extractIntFromListModel(getModel().getThreadsPerCore());
        final int totalCpuCores = getTotalCpuCores();
        Collection<Integer> possCoresPerSocket = getModel().getCoresPerSocket().getItems();

        if (threadsPerCore == 0 || totalCpuCores == 0 || possCoresPerSocket == null || possCoresPerSocket.isEmpty()) {
            return;
        }

        // maximize the number of sockets
        int coresPerSocket = 1;
        int numOfSockets = totalCpuCores / (threadsPerCore * coresPerSocket);

        getModel().getCoresPerSocket().setSelectedItem(coresPerSocket);
        getModel().getNumOfSockets().setSelectedItem(numOfSockets);
    }

    protected void setCpuChangeability() {
        getModel().getThreadsPerCore().setIsChangeable(true);
        getModel().getCoresPerSocket().setIsChangeable(true);
        getModel().getNumOfSockets().setIsChangeable(true);
    }

    public void totalCpuCoresChanged() {
        setCpuChangeability();

        final int totalCpuCores = getTotalCpuCores();
        if (totalCpuCores == 0) {
            return;
        }

        int threadsPerCore = extractIntFromListModel(getModel().getThreadsPerCore());
        int coresPerSocket = extractIntFromListModel(getModel().getCoresPerSocket());
        int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());

        // if has not been yet inited, init to 1
        if (numOfSockets == 0 || coresPerSocket == 0 || threadsPerCore == 0) {
            initListToOne(getModel().getThreadsPerCore());
            initListToOne(getModel().getCoresPerSocket());
            initListToOne(getModel().getNumOfSockets());
        }

        List<Integer> possThreadsPerCore = findIndependentPossibleValues(maxThreadsPerCore);
        List<Integer> possCoresPerSocket = findIndependentPossibleValues(maxCpusPerSocket);
        List<Integer> possSockets = findIndependentPossibleValues(maxNumOfSockets);

        getModel().getThreadsPerCore().setItems(
                filterPossibleValues(possThreadsPerCore, possCoresPerSocket, possSockets));
        getModel().getCoresPerSocket().setItems(
                filterPossibleValues(possCoresPerSocket, possSockets, possThreadsPerCore));
        getModel().getNumOfSockets().setItems(
                filterPossibleValues(possSockets, possCoresPerSocket, possThreadsPerCore));

        // ignore the value already selected in the coresPerSocket/threadsPerCore
        // and always try to set the max possible totalCpuCores
        if (totalCpuCores <= maxNumOfSockets) {
            getModel().getThreadsPerCore().setSelectedItem(1);
            threadsPerCoreChanged();
        } else {
            // we need to compose it from more cores on the available sockets
            composeCoresAndSocketsWhenDontFitInto(totalCpuCores);
        }

        setCpuChangeability();
    }

    public boolean isNumOfSocketsCorrect(int totalCpuCores) {
        int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());
        int coresPerSocket = extractIntFromListModel(getModel().getCoresPerSocket());
        int threadsPerCore = extractIntFromListModel(getModel().getThreadsPerCore());

        return totalCpuCores == (coresPerSocket * numOfSockets * threadsPerCore);
    }

    /**
     * The hard way of finding, what the correct combination of the sockets, cores/socket and threads/socket should be
     * (e.g. checking all possible combinations)
     */
    protected void composeCoresAndSocketsWhenDontFitInto(int totalCpuCores) {
        List<Integer> possibleSockets = findIndependentPossibleValues(maxNumOfSockets);
        List<Integer> possibleCoresPerSocket = findIndependentPossibleValues(maxCpusPerSocket);
        List<Integer> possibleThreadsPerCore = findIndependentPossibleValues(maxThreadsPerCore);

        // the more sockets I can use, the better
        Collections.reverse(possibleSockets);

        for (int socket : possibleSockets) {
            for (int threadsPerSocket : possibleThreadsPerCore) {
                for (int coresPerSocket : possibleCoresPerSocket) {
                    if (socket * coresPerSocket * threadsPerSocket == totalCpuCores) {
                        getModel().getThreadsPerCore().setSelectedItem(threadsPerSocket);
                        getModel().getCoresPerSocket().setSelectedItem(coresPerSocket);
                        getModel().getNumOfSockets().setSelectedItem(socket);

                        return;
                    }
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

    protected void updateNumOfSockets() {
        Version version = getCompatibilityVersion();
        if (version == null) {
            return;
        }

        AsyncDataProvider.getInstance().getMaxNumOfVmSockets(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        VmModelBehaviorBase behavior = VmModelBehaviorBase.this;
                        behavior.maxNumOfSockets = (Integer) returnValue;
                        behavior.updataMaxVmsInPool();
                    }
                }), version.toString());
    }

    /**
     * Returns a list of integers which can divide the param
     */
    protected List<Integer> findIndependentPossibleValues(int max) {
        List<Integer> res = new ArrayList<>();
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
     * i.e. first ~ num of sockets, second ~ cores per socket, thirds ~ threads per core
     */
    protected List<Integer> filterPossibleValues(List<Integer> candidates, List<Integer> second, List<Integer> third) {
        List<Integer> res = new ArrayList<>();
        int currentCpusCores = getTotalCpuCores();

        for (Integer candidate : candidates) {
            for (Integer b : second) {
                for (Integer c : third) {
                    if (candidate * b * c == currentCpusCores) {
                        if (!res.contains(candidate)) {
                            res.add(candidate);
                        }

                        break;
                    }
                }
            }
        }

        return res;
    }

    protected void updateOSValues() {

        List<Integer> vmOsValues;
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster != null) {
            vmOsValues = AsyncDataProvider.getInstance().getOsIds(cluster.getArchitecture());
            Integer selectedOsId = getModel().getOSType().getSelectedItem();
            getModel().getOSType().setItems(vmOsValues);
            if (selectedOsId != null && vmOsValues.contains(selectedOsId)) {
                getModel().getOSType().setSelectedItem(selectedOsId);
            }

            postOsItemChanged();
        }

    }

    /*
     * Updates the emulated machine combobox after a cluster change occurs
     */
    protected void updateEmulatedMachines() {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null) {
            return;
        }

        AsyncDataProvider.getInstance().getEmulatedMachinesByClusterID(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        if (returnValue != null) {
                            Set<String> emulatedSet = new TreeSet<>((HashSet<String>) returnValue);
                            emulatedSet.add(""); //$NON-NLS-1$
                            String oldVal = getModel().getEmulatedMachine().getSelectedItem();
                            getModel().getEmulatedMachine().setItems(emulatedSet);
                            getModel().getEmulatedMachine().setSelectedItem(oldVal);
                        }
                    }
                }), cluster.getId());
    }

    /*
     * Updates the cpu model combobox after a cluster change occurs
     */

    protected void updateCustomCpu() {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null || cluster.getCpuName() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getSupportedCpuList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        if (returnValue != null) {
                            List<String> cpuList = new ArrayList<>();
                            cpuList.add(""); //$NON-NLS-1$
                            for (ServerCpu cpu : (List<ServerCpu>) returnValue) {
                                cpuList.add(cpu.getVdsVerbData());
                            }
                            String oldVal = getModel().getCustomCpu().getSelectedItem();
                            getModel().getCustomCpu().setItems(cpuList);
                            getModel().getCustomCpu().setSelectedItem(oldVal);
                        }
                    }
                }), cluster.getCpuName());
    }

    protected void updateSelectedCdImage(VmBase vmBase) {
        getModel().getCdImage().setSelectedItem(vmBase.getIsoPath());
        boolean hasCd = !StringHelper.isNullOrEmpty(vmBase.getIsoPath());
        getModel().getCdImage().setIsChangeable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);
    }

    protected void updateConsoleDevice(Guid vmId) {
        Frontend.getInstance().runQuery(
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(vmId),
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<String> consoleDevices = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        getModel().getIsConsoleDeviceEnabled().setEntity(!consoleDevices.isEmpty());
                    }
                }));
    }

    protected void updateVirtioScsiEnabledWithoutDetach(final Guid vmId, int osId) {
        virtioScsiUtil.updateVirtioScsiEnabled(vmId, osId, new VirtioScsiUtil.VirtioScasiEnablingFinished() {
            @Override
            public void beforeUpdates() {
                getInstanceTypeManager().deactivate();
            }

            @Override
            public void afterUpdates() {
                getInstanceTypeManager().activate();
            }
        });
    }

    public void vmTypeChanged(VmType vmType) {
        if (basedOnCustomInstanceType()) {
            // this field is normally taken from instance type. If the "custom" is selected, then it is supposed to use the default
            // determined by vm type
            getModel().getIsSoundcardEnabled().setEntity(vmType == VmType.Desktop);
        }

        getModel().getAllowConsoleReconnect().setEntity(vmType == VmType.Server);
    }

    public void enableSinglePCI(boolean enabled) {
        getModel().getIsSingleQxlEnabled().setIsChangeable(enabled);
        if (!enabled) {
            getModel().getIsSingleQxlEnabled().setEntity(false);
        }
    }

    protected void updateRngDevice(Guid templateId) {
        if (!getModel().getIsRngEnabled().getIsChangable()) {
            return;
        }

        Frontend.getInstance().runQuery(
                VdcQueryType.GetRngDevice,
                new IdQueryParameters(templateId),
                new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object model, Object returnValue) {
                                @SuppressWarnings("unchecked")
                                List<VmRngDevice> devs = ((VdcQueryReturnValue) returnValue).getReturnValue();
                                getModel().getIsRngEnabled().setEntity(!devs.isEmpty());
                                getModel().setRngDevice(devs.isEmpty() ? new VmRngDevice() : devs.get(0));
                            }
                        }
                ));
    }

    /*
    * Updates the custom compatibility version combo box options on init/DC-change
    */
    protected void updateCompatibilityVersion() {
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return;
        }
        final StoragePool dataCenter = dataCenterWithCluster.getDataCenter();
        if (dataCenter == null) {
            return;
        }

        AsyncQuery asyncQuery = new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                List<Version> versions = (List<Version>) result;
                versions.add(0, null);
                Version selectedVersion;
                selectedVersion = getModel().getCustomCompatibilityVersion().getSelectedItem();
                if (selectedVersion != null && versions.contains(selectedVersion)) {
                    getModel().getCustomCompatibilityVersion().setItems(versions, selectedVersion);
                } else {
                    getModel().getCustomCompatibilityVersion().setItems(versions);
                }
            }
        });
        AsyncDataProvider.getInstance().getDataCenterVersions(asyncQuery, dataCenter.getId());
    }

    /**
     * In case of a blank template, use the proper value for the default OS.
     */
    protected void setSelectedOSType(VmBase vmBase,
            ArchitectureType architectureType) {
        if (vmBase.getId().equals(Guid.Empty)) {
            Integer osId = AsyncDataProvider.getInstance().getDefaultOs(architectureType);
            if (osId != null) {
                setSelectedOSTypeById(osId.intValue());
            }
        } else {
            setSelectedOSTypeById(vmBase.getOsId());
        }
    }

    private void setSelectedOSTypeById(int osId) {
        for (Integer osIdList : getModel().getOSType().getItems()) {
            if (osIdList.intValue() == osId) {
                getModel().getOSType().setSelectedItem(osIdList);
                break;
            }
        }
    }

    public void updateNumOfIoThreads() {
        getModel().getIoThreadsEnabled().setIsChangeable(true);
        getModel().getNumOfIoThreads().setIsChangeable(true);

        getModel().getNumOfIoThreads().setIsAvailable(getModel().getIoThreadsEnabled().getEntity());
        if (getModel().getIoThreadsEnabled().getEntity() && getModel().getNumOfIoThreads().getEntity() == 0) {
            getModel().getNumOfIoThreads().setEntity(DEFAULT_NUM_OF_IOTHREADS);
        }
    }

    protected Version getCompatibilityVersion() {
        return getModel().getCompatibilityVersion();
    }

    protected Version latestCluster() {
        // instance type and blank template always exposes all the features of the latest cluster and if some is not applicable
        // than that particular feature will not be applicable on the instance creation
        return Version.getLast();
    }

    protected boolean basedOnCustomInstanceType() {
        InstanceType selectedInstanceType = getModel().getInstanceTypes().getSelectedItem();
        return selectedInstanceType == null || selectedInstanceType instanceof CustomInstanceType;
    }

    protected void updateCpuProfile(Guid clusterId, Guid cpuProfileId) {
        getModel().getCpuProfiles().setIsAvailable(true);
        fetchCpuProfiles(clusterId, cpuProfileId);
    }

    private void fetchCpuProfiles(Guid clusterId, final Guid cpuProfileId) {
        if (clusterId == null) {
            return;
        }
        Frontend.getInstance().runQuery(VdcQueryType.GetCpuProfilesByClusterId,
                new IdQueryParameters(clusterId),
                new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<CpuProfile> cpuProfiles = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        getModel().getCpuProfiles().setItems(cpuProfiles);
                        if (cpuProfiles != null) {
                            for (CpuProfile cpuProfile : cpuProfiles) {
                                if (cpuProfile.getId().equals(cpuProfileId)) {
                                    getModel().getCpuProfiles().setSelectedItem(cpuProfile);
                                    break;
                                }
                            }
                        }
                    }
                }));
    }

    public void numaSupport() {
        if (getModel().getWindow() != null) {
            return;
        }
        VM vm = getVmWithNuma();
        final VDS host = getModel().getDefaultHost().getSelectedItems().get(0);
        NumaSupportModel model =
                new VmNumaSupportModel((List<VDS>) getModel().getDefaultHost().getItems(), host , getModel(), vm);
        getModel().setWindow(model);
    }

    protected VM getVmWithNuma() {
        VM vm = new VM();
        String vmName = getModel().getName().getEntity();
        if (vmName == null || vmName.isEmpty()) {
            vmName = "new_vm"; //$NON-NLS-1$
        }
        vm.setName(vmName);
        Integer nodeCount = getModel().getNumaNodeCount().getEntity();
        vm.setvNumaNodeList(new ArrayList<VmNumaNode>());
        for (int i = 0; i < nodeCount; i++) {
            VmNumaNode vmNumaNode = new VmNumaNode();
            vmNumaNode.setIndex(i);
            vm.getvNumaNodeList().add(vmNumaNode);
        }
        return vm;
    }

    protected void updateGraphics(Guid id) {
        AsyncQuery callback = new AsyncQuery();
        callback.setModel(getModel());
        callback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue retVal = (VdcQueryReturnValue) returnValue;
                List<VmDevice> graphicsVmDevs = retVal.getReturnValue();

                List<GraphicsType> graphicsTypes = new ArrayList<>();
                for (VmDevice graphicsVmDev : graphicsVmDevs) {
                    graphicsTypes.add(GraphicsType.fromString(graphicsVmDev.getDevice()));
                }

                boolean hasSpiceAndVnc = graphicsTypes.size() == 2
                        && graphicsTypes.containsAll(Arrays.asList(GraphicsType.SPICE, GraphicsType.VNC));
                boolean canBeSelected = getModel().getGraphicsType().getItems().contains(UnitVmModel.GraphicsTypes.SPICE_AND_VNC);

                if (hasSpiceAndVnc && canBeSelected) {
                    getModel().getGraphicsType().setSelectedItem(UnitVmModel.GraphicsTypes.SPICE_AND_VNC);
                } else if (graphicsVmDevs.size() == 1) {
                    GraphicsType type = GraphicsType.fromString(graphicsVmDevs.get(0).getDevice());
                    getModel().getGraphicsType().setSelectedItem(UnitVmModel.GraphicsTypes.fromGraphicsType(type));
                }
            }
        };

        Frontend.getInstance().runQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(id), callback);
    }

    /**
     * allows to enable numa models in all derived behaviors
     * use updateNumaEnabledHelper in each behavior that requires numa
     */
    protected void updateNumaEnabled() {
    }

    protected final void updateNumaEnabledHelper() {
        boolean enabled = true;
        if (getModel().getMigrationMode().getSelectedItem() != MigrationSupport.PINNED_TO_HOST ||
                getModel().getIsAutoAssign().getEntity() ||
                getModel().getDefaultHost().getSelectedItem() == null ||
                !getModel().getDefaultHost().getSelectedItem().isNumaSupport()) {
            enabled = false;
        }
        if (enabled) {
            getModel().getNumaEnabled().setMessage(constants.numaInfoMessage());
        } else {
            getModel().getNumaEnabled().setMessage(constants.numaDisabledInfoMessage());
            getModel().getNumaNodeCount().setEntity(0);

        }
        getModel().getNumaEnabled().setEntity(enabled);
    }

    public boolean isBlankTemplateBehavior() {
        return this instanceof ExistingBlankTemplateModelBehavior;
    }

    public boolean isExistingTemplateBehavior() {
        return this instanceof TemplateVmModelBehavior;
    }

    public boolean isNewTemplateBehavior() {
        return this instanceof NewTemplateVmModelBehavior;
    }

    public boolean isAnyTemplateBehavior() {
        return this instanceof TemplateVmModelBehavior || this instanceof ExistingBlankTemplateModelBehavior;
    }

    public int getMaxNameLength() {
        return AsyncDataProvider.getInstance().getMaxVmNameLength();
    }

    public IValidation getNameAllowedCharactersIValidation() {
        return new I18NNameValidation();
    }

    protected abstract class UpdateTemplateWithVersionListener implements IEventListener<EventArgs> {

        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            if (getModel().getTemplateWithVersion() == null ||
                    getModel().getTemplateWithVersion().getItems() == null ||
                    getModel().getTemplateWithVersion().getSelectedItem() == null) {
                return;
            }

            List<VmTemplate> baseTemplates = new ArrayList<>();
            for (TemplateWithVersion templateWithVersion : getModel().getTemplateWithVersion().getItems()) {
                if (templateWithVersion.isLatest() || templateWithVersion.getTemplateVersion() == null) {
                    continue;
                }
                baseTemplates.add(templateWithVersion.getTemplateVersion());
            }

            TemplateWithVersion selectedItemTemplateWithVersion = getModel().getTemplateWithVersion().getSelectedItem();

            VmTemplate selectedTemplateWithVersion = selectedItemTemplateWithVersion.getTemplateVersion();
            if (selectedTemplateWithVersion == null) {
                return;
            }

            Guid selectedId = selectedTemplateWithVersion.getId();

            beforeUpdate();
            initTemplateWithVersion(baseTemplates, selectedId, selectedItemTemplateWithVersion.isLatest(), isAddLatestVersion());
        }

        protected void beforeUpdate() {
        }

        protected abstract boolean isAddLatestVersion();

    }

}
