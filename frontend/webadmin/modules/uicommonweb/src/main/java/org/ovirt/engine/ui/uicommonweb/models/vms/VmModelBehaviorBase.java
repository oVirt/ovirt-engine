package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
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
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.dataprovider.ImagesDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
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

    protected final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private TModel privateModel;

    protected List<String> dedicatedHostsNames;

    private PriorityUtil priorityUtil;

    private VirtioScsiUtil virtioScsiUtil;

    public int maxCpus = 0;
    protected int maxCpusPerSocket = 0;
    protected int maxNumOfSockets = 0;
    protected int maxThreadsPerCore = 0;

    private int maxVmsInPool = 1000;

    protected boolean isCustomCompatibilityVersionChangeInProgress = false;
    protected Version savedCurrentCustomCompatibilityVersion = null;

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

    public void initialize() {
        commonInitialize();
    }

    /**
     * If someone overrides the initialize not calling the super, at least this has to be called
     */
    protected void commonInitialize() {
        priorityUtil = new PriorityUtil(getModel());
        virtioScsiUtil = new VirtioScsiUtil(getModel());
        getModel().getVmId().setIsAvailable(false);
        getModel().getLease().setIsChangeable(false);
        getModel().getIsHighlyAvailable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean ha = getModel().getIsHighlyAvailable().getEntity();
            setVmLeasesAvailability();
            if (!ha) {
                getModel().getLease().setSelectedItem(null);
            }
        });
        List<MigrationPolicy> policies = AsyncDataProvider.getInstance().getMigrationPolicies(Version.getLast());
        policies.add(0, null);
        getModel().getMigrationPolicies().setItems(policies);
        initializeBiosType();
    }

    protected void initializeBiosType() {
        getModel().getBiosType().setItems(AsyncDataProvider.getInstance().getBiosTypeList());
    }

    protected Guid findDefaultStorageDomainForVmLease(Collection<Disk> disks) {
        // The default storage domain for the VM lease will be the storage domain
        // that contains the VM bootable disk
        for (Disk disk : disks) {
            DiskVmElement bootableDiskVmElement = disk.getDiskVmElements().stream()
                    .filter(DiskVmElement::isBoot)
                    .findFirst()
                    .orElse(null);
            if (bootableDiskVmElement != null && disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage diskImage = (DiskImage) disk;
                return diskImage.getStorageIds().get(0);
            }
        }

        return null;
    }

    protected void setVmLeaseDefaultStorageDomain(Guid defaultStorageDomainId) {
        Collection<StorageDomain> domains = getModel().getLease().getItems();
        domains.stream()
                .filter(Objects::nonNull)
                .filter(sd -> sd.getId().equals(defaultStorageDomainId))
                .findFirst().ifPresent(sd -> getModel().getLease().setSelectedItem(sd));
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

        getModel().getQuota().setIsAvailable(dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED);
        getModel().getIsRngEnabled().setIsChangeable(true);
        setRngAvailability();

        postDataCenterWithClusterSelectedItemChanged();
    }

    private void setVmLeasesAvailability() {
        TModel model = getModel();
        if (model.getSelectedCluster() == null) {
            return;
        }

        StorageDomain leaseStorageDomain = model.getLease().getSelectedItem();
        if (!model.getIsHighlyAvailable().getEntity()) {
            model.getLease().setIsChangeable(false);
            model.getLease().setChangeProhibitionReason(constants.vmLeasesNotSupportedWithoutHA());
        } else if (leaseStorageDomain != null && leaseStorageDomain.getStatus() != StorageDomainStatus.Active) {
            model.getLease().setIsChangeable(false);
            model.getLease().setChangeProhibitionReason(constants.cannotEditNotActiveLeaseDomain());
        } else {
            model.getLease().setIsChangeable(true);
        }
    }

    private void setRngAvailability() {
        TModel model = getModel();
        Set<VmRngDevice.Source> requiredRngSources = model.getSelectedCluster().getRequiredRngSources();

        boolean urandomSourceAvailable = requiredRngSources.contains(VmRngDevice.Source.URANDOM)
                || requiredRngSources.contains(VmRngDevice.Source.RANDOM);
        boolean hwrngSourceAvailable = requiredRngSources.contains(VmRngDevice.Source.HWRNG);

        model.getRngSourceUrandom().setIsChangeable(urandomSourceAvailable);
        if (!urandomSourceAvailable) {
            model.getRngSourceUrandom().setChangeProhibitionReason(messages.rngSourceNotSupportedByCluster(
                    VmRngDevice.Source.getUrandomOrRandomFor(getModel().getSelectedCluster().getCompatibilityVersion())
                            .toString().toLowerCase()));
        }

        model.getRngSourceHwrng().setIsChangeable(hwrngSourceAvailable);
        if (!hwrngSourceAvailable) {
            model.getRngSourceHwrng().setChangeProhibitionReason(messages.rngSourceNotSupportedByCluster(
                    VmRngDevice.Source.HWRNG.toString().toLowerCase()));
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

    public void templateWithVersion_SelectedItemChanged() {
        updateSeal();
    }

    public abstract void postDataCenterWithClusterSelectedItemChanged();

    public abstract void defaultHost_SelectedItemChanged();

    public abstract void provisioning_SelectedItemChanged();

    public void oSType_SelectedItemChanged() {
        updateSeal();
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

    protected TemplateWithVersion computeTemplateWithVersionToSelect(
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
    protected static TemplateWithVersion computeNewTemplateWithVersionToSelect(List<TemplateWithVersion> newItems, boolean addLatest) {
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
        ImagesDataProvider.getISOImagesList(new AsyncQuery<>(images -> setImagesToModel(getModel(), images)),
                storagePoolId
        );
    }

    protected void setImagesToModel(UnitVmModel model, List<RepoImage> images) {
        RepoImage oldCdImage = model.getCdImage().getSelectedItem();
        model.getCdImage().setItems(images);
        model.getCdImage().setSelectedItem((oldCdImage != null) ? oldCdImage
                : Linq.firstOrNull(images));
        postUpdateCdImages();
    }

    // This can be overridden by child behaviors to
    // add logic after fetching cd images.
    protected void postUpdateCdImages() {
        // do nothing
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

        ImagesDataProvider.getISOImagesList(asyncQuery(
                images -> setImagesToModel(getModel(), images)),
                dataCenter.getId(),
                forceRefresh);

    }

    protected String getSelectedTimeZone() {
        String timeZoneKey =  getModel().getTimeZone().getSelectedItem().getTimeZoneKey();
        return timeZoneKey != null ? timeZoneKey : "";
    }

    protected int getSelectedOSType() {
        return getModel().getOSType().getSelectedItem();
    }

    protected void updateOSValue(final Integer selectedOsId) {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null) {
            return;
        }

        List<Integer> vmOsValues = getOsValues(cluster.getArchitecture(), getCompatibilityVersion());

        if (selectedOsId == null || !vmOsValues.contains(selectedOsId)) {
            updateOSValues();
            return;
        }

        getModel().getOSType().setSelectedItem(selectedOsId);
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
        if (dedicatedHostIds == null) {
            getModel().getIsAutoAssign().setEntity(true);
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
                return;
            }
        }
        getModel().getIsAutoAssign().setEntity(true);
    }

    protected void updateDefaultHost() {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null) {
            getModel().getDefaultHost().setItems(new ArrayList<>());
            getModel().getDefaultHost().setSelectedItems(new ArrayList<>());
            return;
        }

        getHostListByCluster(cluster, asyncQuery(hosts -> {
                    List<VDS> oldDefaultHosts = getModel().getDefaultHost().getSelectedItems();
                    getModel().getDefaultHost().setItems(hosts);

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
                                              : Collections.emptyList();
                    getModel().getDefaultHost().setSelectedItems(hostsToSelect);
                    changeDefaultHost();

                }));
    }

    protected void getHostListByCluster(Cluster cluster, AsyncQuery<List<VDS>> query) {
        AsyncDataProvider.getInstance().getHostListByCluster(query, cluster.getName());
    }

    protected void updateCustomPropertySheet() {
        if (getModel().getCompatibilityVersion() != null) {
            updateCustomPropertySheet(getModel().getCompatibilityVersion());
        }
    }

    protected void updateCustomPropertySheet(Version clusterVersion) {
        // KeyValueModel resets all data when setting new key map and it does not have any other
        // method of setting the data other than the deserialize method
        String currentCustomProperties = getModel().getCustomPropertySheet().serialize();
        getModel().getCustomPropertySheet()
                .setKeyValueMap(
                        getModel().getCustomPropertiesKeysList().get(clusterVersion));
        getModel().getCustomPropertySheet().deserialize(currentCustomProperties);
    }

    public void updataMaxVmsInPool() {
        AsyncDataProvider.getInstance().getMaxVmsInPool(asyncQuery(
                returnValue -> {
                    setMaxVmsInPool(returnValue);
                    updateMaxNumOfVmCpus();
                }
        ));
    }

    public void updateMaxNumOfVmCpus() {
        String version = getCompatibilityVersion().toString();

        Function<Map<String, Integer>, Integer> getMaxCpus = archToLimit -> {
            Cluster cluster = getModel().getSelectedCluster();
            ArchitectureType architecture = cluster != null ? cluster.getArchitecture() : null;
            return architecture != null ?
                    archToLimit.get(architecture.getFamily().name())
                    : archToLimit.values().stream().mapToInt(v -> v).max().orElse(1);
        };

        AsyncDataProvider.getInstance().getMaxNumOfVmCpus(asyncQuery(
                returnValue -> {
                    maxCpus = getMaxCpus.apply(returnValue);
                    postUpdateNumOfSockets2();
                }), version);
    }

    public void postUpdateNumOfSockets2() {
        String version = getCompatibilityVersion().toString();

        AsyncDataProvider.getInstance().getMaxNumOfCPUsPerSocket(asyncQuery(
                returnValue -> {
                    maxCpusPerSocket = returnValue;
                    postUpdateNumOfSockets3();
                }), version);
    }

    public void postUpdateNumOfSockets3() {
        String version = getCompatibilityVersion().toString();

        AsyncDataProvider.getInstance().getMaxNumOfThreadsPerCpu(asyncQuery(
                returnValue -> {
                    maxThreadsPerCore = returnValue;
                    totalCpuCoresChanged();
                }), version);
    }

    public void initDisks() {
        VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();

        AsyncDataProvider.getInstance().getTemplateDiskList(asyncQuery(this::initTemplateDisks), template.getId());
    }

    protected void initTemplateDisks(List<? extends Disk> disks) {
        disks.sort(new DiskByDiskAliasComparator());
        ArrayList<DiskModel> list = new ArrayList<>();

        for (Disk disk : disks) {
            if (disk.getDiskStorageType() == DiskStorageType.LUN) {
                continue;
            }

            DiskModel diskModel = new DiskModel();
            diskModel.getAlias().setEntity(disk.getDiskAlias());
            diskModel.getVolumeType().setIsAvailable(false);

            switch (disk.getDiskStorageType()) {
                case IMAGE:
                    DiskImage diskImage = (DiskImage) disk;
                    diskModel.setSize(new EntityModel<>((int) diskImage.getSizeInGigabytes()));
                    ListModel volumes = new ListModel();
                    volumes.setItems(diskImage.getVolumeType() == VolumeType.Preallocated ?
                            new ArrayList<>(Collections.singletonList(VolumeType.Preallocated))
                            : AsyncDataProvider.getInstance().getVolumeTypeList(), diskImage.getVolumeType());
                    diskModel.setVolumeType(volumes);
                    VM vm = new VM();
                    vm.setClusterCompatibilityVersion(getCompatibilityVersion());
                    vm.setClusterBiosType(getSelectedBiosType());
                    diskModel.setVm(vm);
                    break;
                case MANAGED_BLOCK_STORAGE:
                    ManagedBlockStorageDisk managedBlockDisk = (ManagedBlockStorageDisk) disk;
                    diskModel.setSize(new EntityModel<>((int) managedBlockDisk.getSizeInGigabytes()));
                    ListModel managedBlockVolumeTypes = new ListModel();
                    managedBlockVolumeTypes.setItems(new ArrayList<>(Collections.singletonList(VolumeType.Preallocated)), VolumeType.Preallocated);
                    diskModel.setVolumeType(managedBlockVolumeTypes);
                    ListModel managedBlockVolumeFormats = new ListModel();
                    managedBlockVolumeFormats.setItems(new ArrayList<>(Collections.singletonList(VolumeFormat.RAW)), VolumeFormat.RAW);
                    diskModel.setVolumeFormat(managedBlockVolumeFormats);
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
        } else {
            getModel().getStorageDomain().setItems(new ArrayList<>());
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
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(new AsyncQuery<>(storageDomains -> {
            ArrayList<StorageDomain> activeStorageDomains = filterStorageDomains(storageDomains);

            boolean provisioning = getModel().getProvisioning().getEntity();
            ArrayList<DiskModel> disks = (ArrayList<DiskModel>) getModel().getDisks();
            activeStorageDomains.sort(new NameableComparator());

            List<DiskModel> diskImages = Linq.filterDisksByType(disks, DiskStorageType.IMAGE);
            for (DiskModel diskModel : diskImages) {
                List<StorageDomain> availableDiskStorageDomains;
                diskModel.getQuota().setItems(getModel().getQuota().getItems());
                List<Guid> storageIds = ((DiskImage) diskModel.getDisk()).getStorageIds();

                // Active storage domains that the disk resides on
                List<StorageDomain> activeDiskStorageDomains =
                        Linq.getStorageDomainsByIds(storageIds, activeStorageDomains);

                // Set target storage domains
                availableDiskStorageDomains = provisioning ? activeStorageDomains : activeDiskStorageDomains;
                availableDiskStorageDomains.sort(new NameableComparator());
                diskModel.getStorageDomain().setItems(availableDiskStorageDomains);

                diskModel.getStorageDomain().setIsChangeable(!availableDiskStorageDomains.isEmpty());
                diskModel.getStorageDomain().setChangeProhibitionReason(
                        constants.noActiveTargetStorageDomainAvailableMsg());
            }
            initStorageDomainForType(StorageType.MANAGED_BLOCK_STORAGE,
                    DiskStorageType.MANAGED_BLOCK_STORAGE,
                    disks,
                    storageDomains);
        }), dataCenter.getId(), actionGroup);
    }

    protected void initStorageDomainForType(StorageType storageType,
            DiskStorageType diskStorageType,
            ArrayList<DiskModel> disks,
            List<StorageDomain> storageDomains) {
        List<DiskModel> disksModels = Linq.filterDisksByType(disks, diskStorageType);
        if (!disksModels.isEmpty()) {
            Collection<StorageDomain> storageDomainsByStorageType =
                    Linq.filterStorageDomainsByStorageType(storageDomains, storageType);
            initStorageDomainsForDisks(disksModels, storageDomainsByStorageType);
        }
    }

    private void initStorageDomainsForDisks(List<DiskModel> diskModels, Collection<StorageDomain> storageDomains) {
        for (DiskModel diskModel : diskModels) {
            DiskImage diskImage = (DiskImage) diskModel.getDisk();
            diskModel.getStorageDomain().setItems(Linq.filterStorageDomainById(
                    storageDomains, diskImage.getStorageIds().get(0)));
            diskModel.getStorageDomain().setIsChangeable(false);
            diskModel.getDiskProfile().setIsChangeable(false);
            diskModel.getDiskProfile().setChangeProhibitionReason(
                    ConstantsManager.getInstance().getConstants().notSupportedForManagedBlockDisks());
        }
    }

    public ArrayList<StorageDomain> filterStorageDomains(List<StorageDomain> storageDomains) {
        // filter only the Active storage domains (Active regarding the relevant storage pool).
        ArrayList<StorageDomain> list = new ArrayList<>();
        for (StorageDomain a : storageDomains) {
            if (Linq.isDataActiveStorageDomain(a)) {
                list.add(a);
            }
        }
        return list;
    }

    protected void updateQuotaByCluster(final Guid defaultQuota, final String quotaName) {
        Cluster cluster = getModel().getSelectedCluster();
        if (cluster == null) {
            return;
        }

        if (!getModel().getQuota().getIsAvailable()) {
            getModel().getQuota().setItems(null);
            return;
        }

        AsyncDataProvider.getInstance().getAllRelevantQuotasForClusterSorted(new AsyncQuery<>(
                quotaList -> {
                    UnitVmModel vmModel = getModel();
                    vmModel.getQuota().setItems(quotaList);

                    if (quotaList == null) {
                        return;
                    }

                    if (!Guid.isNullOrEmpty(defaultQuota)) {
                        boolean hasQuotaInList = false;
                        if (!quotaList.isEmpty()) {
                            hasQuotaInList = defaultQuota.equals(quotaList.get(0).getId());
                        }

                        // Add the quota to the list only in edit or clone mode
                        if (!hasQuotaInList && !getModel().getIsNew()) {
                            Quota quota = new Quota();
                            quota.setId(defaultQuota);
                            quota.setQuotaName(quotaName);
                            quotaList.add(0, quota);
                            vmModel.getQuota().setItems(quotaList);
                            vmModel.getQuota().setSelectedItem(quota);
                        }
                    }
                }), cluster.getId(), defaultQuota);
    }

    protected void updateMemoryBalloon() {
        Cluster cluster = getModel().getSelectedCluster();
        Integer osType = getModel().getOSType().getSelectedItem();

        if (cluster != null && osType != null) {
            getModel().getMemoryBalloonEnabled().setIsAvailable(true);
        }
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
        AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery<>(
                        rawTemplate -> {
                            if (isVersionChangeable) {
                                // only used by pools therefore query is limited to admin-portal permissions.
                                AsyncDataProvider.getInstance().getVmTemplatesByBaseTemplateId(
                                        new AsyncQuery<>(templatesChain -> initTemplateWithVersion(templatesChain, templateId, useLatest)), rawTemplate.getBaseTemplateId());
                            } else {
                                final VmTemplate template = useLatest
                                        ? new LatestVmTemplate(rawTemplate)
                                        : rawTemplate;
                                if (template.isBaseTemplate()) {
                                    TemplateWithVersion templateCouple =
                                            new TemplateWithVersion(template, template);
                                    setReadOnlyTemplateWithVersion(templateCouple);
                                } else {
                                    AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery<>(
                                                    baseTemplate -> {
                                                        TemplateWithVersion templateCouple =
                                                                new TemplateWithVersion(baseTemplate, template);
                                                        setReadOnlyTemplateWithVersion(templateCouple);
                                                    }),
                                            template.getBaseTemplateId());
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
        if (getModel().getIsAutoAssign().getEntity() == null) {
            return;
        }

        if (getModel().getCpuPinningPolicy().getSelectedItem().getPolicy() != CpuPinningPolicy.MANUAL) {
            getModel().getCpuPinning().setIsChangeable(false, constants.cpuPinningUnavailable());
            getModel().getCpuPinning().setEntity(null);
            return;
        }

        if (getModel().getSelectedCluster() != null) {
            boolean isLocalSD = getModel().getSelectedDataCenter() != null
                    && getModel().getSelectedDataCenter().isLocal();

            // cpu pinning is available on Local SD with no consideration for auto assign value
            boolean hasCpuPinning = Boolean.FALSE.equals(getModel().getIsAutoAssign().getEntity()) || isLocalSD;

            getModel().getCpuPinning().setIsChangeable(hasCpuPinning);
            if (!hasCpuPinning) {
                getModel().getCpuPinning().setChangeProhibitionReason(constants.cpuPinningUnavailable());
            }
        }
    }

    public void updateUseHostCpuAvailability() {

        boolean clusterSupportsHostCpu = getCompatibilityVersion() != null;
        Boolean isAutoAssign = getModel().getIsAutoAssign().getEntity();
        int numOfPinnedHosts = getModel().getDefaultHost().getSelectedItems() == null ?
                0 : getModel().getDefaultHost().getSelectedItems().size();

        if (isAutoAssign == null) {
            return;
        }

        if (clusterSupportsHostCpu && !clusterHasPpcArchitecture()
                && ((Boolean.FALSE.equals(isAutoAssign) && numOfPinnedHosts > 0)
                || getModel().getVmType().getSelectedItem() == VmType.HighPerformance)) {
            getModel().getHostCpu().setIsChangeable(true);
        } else {
            getModel().getHostCpu().setEntity(false);
            getModel().getHostCpu().setIsChangeable(false);
            getModel().getHostCpu().setChangeProhibitionReason(constants.hosCPUUnavailable());
        }
    }

    protected boolean clusterHasPpcArchitecture() {
        Cluster cluster = getModel().getSelectedCluster();

        return cluster != null
                && cluster.getArchitecture() != null
                && ArchitectureType.ppc == cluster.getArchitecture().getFamily();
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
        list.setItems(Collections.singletonList(1));
        list.setSelectedItem(1);
    }

    protected void updateNumOfSockets() {
        Version version = getCompatibilityVersion();
        if (version == null) {
            return;
        }

        AsyncDataProvider.getInstance().getMaxNumOfVmSockets(asyncQuery(
                returnValue -> {
                    maxNumOfSockets = returnValue;
                    updataMaxVmsInPool();
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
            vmOsValues = getOsValues(cluster.getArchitecture(), getCompatibilityVersion());
            Integer selectedOsId = getModel().getOSType().getSelectedItem();
            getModel().getOSType().setItems(vmOsValues);
            if (selectedOsId != null && vmOsValues.contains(selectedOsId)) {
                getModel().getOSType().setSelectedItem(selectedOsId);
            }

            postOsItemChanged();
        }

    }

    protected void updateLeaseStorageDomains(final Guid selectedStorageDomainId) {
        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(
                returnValue -> {
                    // initialize the list of storage domains that available for lease creation
                    List<StorageDomain> domains = new ArrayList<>();
                    domains.add(null);
                    for (StorageDomain domain : returnValue) {
                        if (domain.getStorageDomainType().isDataDomain()
                                && domain.getStatus() == StorageDomainStatus.Active) {
                            domains.add(domain);
                        }
                    }
                    getModel().getLease().setItems(domains);

                    // initialize the storage domain that currently holds the lease.
                    // VM lease storage domain can be on non-active domain, therefore we need to search
                    // through all the data domains for the lease storage domain.
                    if (selectedStorageDomainId != null) {
                        returnValue.stream()
                                .filter(d -> d.getStorageDomainType().isDataDomain())
                                .filter(d -> selectedStorageDomainId.equals(d.getId()))
                                .findFirst().ifPresent(d -> getModel().getLease().setSelectedItem(d));
                    }
                    setVmLeasesAvailability();
                }), getModel().getSelectedDataCenter().getId());
    }

    /*
     * Updates the emulated machine combobox after a cluster change occurs
     */
    protected void updateEmulatedMachines() {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null) {
            return;
        }

        AsyncDataProvider.getInstance().getEmulatedMachinesByClusterID(new AsyncQuery<>(
                returnValue -> {
                    if (returnValue != null) {
                        Set<String> emulatedSet = new TreeSet<>(returnValue);
                        emulatedSet.add(""); //$NON-NLS-1$
                        String oldVal = getModel().getEmulatedMachine().getSelectedItem();
                        getModel().getEmulatedMachine().setItems(emulatedSet);
                        getModel().getEmulatedMachine().setSelectedItem(oldVal);
                    }
                }), cluster.getId());
    }

    protected void updateBiosType() {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null) {
            return;
        }

        if (cluster.getArchitecture().getFamily() != ArchitectureType.x86) {
            getModel().getBiosType().setIsChangeable(false, ConstantsManager.getInstance().getMessages().biosTypeSupportedForX86Only());
        } else {
            getModel().getBiosType().updateChangeability(ConfigValues.BiosTypeSupported, getCompatibilityVersion());
        }
    }

    /*
     * Used in new vm and new vm pool to select the value from the current template
     */
    protected void selectBiosTypeFromTemplate() {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null) {
            return;
        }

        if (!getModel().getBiosType().getIsChangable()) {
            getModel().getBiosType().setSelectedItem(cluster.getBiosType());
            return;
        }

        if (basedOnCustomInstanceType()) {
            TemplateWithVersion template = getModel().getTemplateWithVersion().getSelectedItem();

            if (template == null) {
                return;
            }

            if (template.getTemplateVersion().getClusterId() != null) {
                getModel().getBiosType().setSelectedItem(template.getTemplateVersion().getBiosType());
                return;
            }
        }

        getModel().getBiosType().setSelectedItem(cluster.getBiosType());
    }

    /*
     * Updates the cpu model combobox after a cluster change occurs
     */

    protected void updateCustomCpu() {
        Cluster cluster = getModel().getSelectedCluster();

        if (cluster == null || cluster.getCpuName() == null || "".equals(cluster.getCpuName())) {
            return;
        }

        AsyncDataProvider.getInstance().getSupportedCpuList(new AsyncQuery<>(
                returnValue -> {
                    if (returnValue != null) {
                        List<String> cpuList = new ArrayList<>();
                        cpuList.add(""); //$NON-NLS-1$
                        for (ServerCpu cpu : returnValue) {
                            cpuList.add(cpu.getVdsVerbData());
                        }
                        String oldVal = getModel().getCustomCpu().getSelectedItem();
                        getModel().getCustomCpu().setItems(cpuList);
                        getModel().getCustomCpu().setSelectedItem(oldVal);
                    }
                }), cluster.getCpuName(), cluster.getCompatibilityVersion());
    }

    protected void updateSelectedCdImage(VmBase vmBase) {
        boolean hasCd = !StringHelper.isNullOrEmpty(vmBase.getIsoPath());
        if (hasCd) {
            getModel().getCdImage().setSelectedItem(new RepoImage(vmBase.getIsoPath()));
        }
        getModel().getCdImage().setIsChangeable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);
    }

    protected void updateTpm(Guid vmId) {
        Frontend.getInstance().runQuery(
                QueryType.GetTpmDevices,
                new IdQueryParameters(vmId),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<String> tpmDevices = returnValue.getReturnValue();
                    boolean tpmEnabled = !tpmDevices.isEmpty();
                    getModel().getTpmEnabled().setEntity(tpmEnabled);
                    getModel().setTpmOriginallyEnabled(tpmEnabled);
                }));
    }

    protected void updateConsoleDevice(Guid vmId) {
        Frontend.getInstance().runQuery(
                QueryType.GetConsoleDevices,
                new IdQueryParameters(vmId),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<String> consoleDevices = returnValue.getReturnValue();
                    getModel().getIsConsoleDeviceEnabled().setEntity(!consoleDevices.isEmpty());
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
            // determined by vm type & ppc architecture
            getModel().getIsSoundcardEnabled().setEntity(vmType == VmType.Desktop && !clusterHasPpcArchitecture());
        }

        getModel().getAllowConsoleReconnect().setEntity(vmType == VmType.Server);

        if (vmType == VmType.Server) {
            getModel().getIoThreadsEnabled().setEntity(true);
            if (getModel().getDisplayType().getItems().contains(DisplayType.bochs)) {
                getModel().getDisplayType().setSelectedItem(DisplayType.bochs);
            }
        }

        // Configuration relevant only for High Performance
        if (vmType == VmType.HighPerformance) {
            // Console tab
            getModel().getIsHeadlessModeEnabled().setEntity(true);
            getModel().getIsConsoleDeviceEnabled().setEntity(true);
            getModel().getIsUsbEnabled().setEntity(false);
            getModel().getIsSmartcardEnabled().setEntity(false);

            // High Availability tab
            getModel().getWatchdogModel().setSelectedItem(null);
            getModel().getWatchdogAction().setSelectedItem(getModel().getWatchdogAction().getItems().iterator().next());

            // Random Generator tab
            getModel().getIsRngEnabled().setEntity(true);

            // Host tab
            if (!clusterHasPpcArchitecture()) {
                getModel().getHostCpu().setEntity(true);
            }

            // Resource allocation tab
            getModel().getMemoryBalloonEnabled().setEntity(false);
            getModel().getIoThreadsEnabled().setEntity(true);
            if (getModel().getMultiQueues().getIsAvailable()) {
                getModel().getMultiQueues().setEntity(true);
            }
        } else {
            getModel().getHostCpu().setEntity(false);
        }

        // Configuration relevant for either High Performance or VMs with pinned configuration
        if (isVmHpOrPinningConfigurationEnabled()) {
            getModel().getMigrationMode().setSelectedItem(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        } else {
            getModel().getMigrationMode().setSelectedItem(MigrationSupport.MIGRATABLE);
        }
    }

    protected void updateRngDevice(Guid templateId) {
        Frontend.getInstance().runQuery(
                QueryType.GetRngDevice,
                new IdQueryParameters(templateId),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                            List<VmRngDevice> devs = returnValue.getReturnValue();
                            getModel().getIsRngEnabled().setEntity(!devs.isEmpty());
                            final VmRngDevice rngDevice = devs.isEmpty()
                                    ? new VmRngDevice()
                                    : devs.get(0);
                            rngDevice.updateSourceByVersion(getModel().getCompatibilityVersion());
                            getModel().setRngDevice(rngDevice);
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

        AsyncDataProvider.getInstance().getDataCenterVersions(new AsyncQuery<>(versions -> {
            versions.add(0, null);
            Version selectedVersion;
            selectedVersion = getModel().getCustomCompatibilityVersion().getSelectedItem();
            if (selectedVersion != null && versions.contains(selectedVersion)) {
                getModel().getCustomCompatibilityVersion().setItems(versions, selectedVersion);
            } else {
                getModel().getCustomCompatibilityVersion().setItems(versions);
            }
        }), dataCenter.getId());
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

        Integer numOfIoThreads = IntegerCompat.tryParse(getModel().getNumOfIoThreads().getEntity());
        if (numOfIoThreads == null || numOfIoThreads == 0) {
            getModel().getNumOfIoThreads().setEntity(Integer.toString(DEFAULT_NUM_OF_IOTHREADS));
        }
    }

    protected Version getCompatibilityVersion() {
        return getModel().getCompatibilityVersion();
    }

    protected BiosType getSelectedBiosType() {
        return getModel().getBiosType().getSelectedItem();
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
        Frontend.getInstance().runQuery(QueryType.GetCpuProfilesByClusterId,
                new IdQueryParameters(clusterId),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<CpuProfile> cpuProfiles = returnValue.getReturnValue();
                    getModel().getCpuProfiles().setItems(cpuProfiles);
                    if (cpuProfiles != null) {
                        for (CpuProfile cpuProfile : cpuProfiles) {
                            if (cpuProfile.getId().equals(cpuProfileId)) {
                                getModel().getCpuProfiles().setSelectedItem(cpuProfile);
                                break;
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
        vm.setvNumaNodeList(new ArrayList<>());
        for (int i = 0; i < nodeCount; i++) {
            VmNumaNode vmNumaNode = new VmNumaNode();
            vmNumaNode.setIndex(i);
            vm.getvNumaNodeList().add(vmNumaNode);
        }
        return vm;
    }

    protected void updateGraphics(Guid id) {
        Frontend.getInstance().runQuery(QueryType.GetGraphicsDevices, new IdQueryParameters(id), new AsyncQuery<QueryReturnValue>(returnValue -> {
            List<VmDevice> graphicsVmDevs = returnValue.getReturnValue();

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
        }));
    }

    /**
     * allows to enable numa models in all derived behaviors
     * use updateNumaEnabledHelper in each behavior that requires numa
     */
    protected void updateNumaEnabled() {
    }

    protected final void updateNumaEnabledHelper() {
        boolean enabled = true;
        if (getModel().getIsAutoAssign().getEntity() == null) {
            return;
        }

        if (getModel().getIsAutoAssign().getEntity() ||
                getModel().getDefaultHost().getSelectedItems() == null ||
                getModel().getDefaultHost().getSelectedItems().stream().anyMatch(x -> !x.isNumaSupport())) {
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

    public int getMaxNameLength() {
        return AsyncDataProvider.getInstance().getMaxVmNameLength();
    }

    public IValidation getNameAllowedCharactersIValidation() {
        return new I18NNameValidation();
    }

    public VmRngDevice.Source getUrandomOrRandomRngSource() {
        return VmRngDevice.Source.getUrandomOrRandomFor(getModel().getCompatibilityVersion());
    }

    public void updateMaxMemory() {
        final Integer memoryMb = getModel().getMemSize().getEntity();
        if (memoryMb != null) {
            int calculatedMaxMemory = VmCommonUtils.getMaxMemorySizeDefault(memoryMb);
            int allowedMaxMemory = AsyncDataProvider.getInstance()
                    .getMaxMaxMemorySize(getModel().getOSType().getSelectedItem(), getCompatibilityVersion());
            getModel().getMaxMemorySize().setEntity(Math.min(calculatedMaxMemory, allowedMaxMemory));
        }
    }

    public void updateMemory() {
        Integer memoryMb = getModel().getMemSize().getEntity();
        AsyncDataProvider.getMinMemoryForOs(new AsyncQuery<>(minMemory -> {
            if (memoryMb == null || memoryMb < minMemory) {
                getModel().getMemSize().setEntity(minMemory);
            }
        }), getSelectedOSType(), getCompatibilityVersion());
    }

    public void updateTotalCpuCores() {
        int cpuCores = getTotalCpuCores();
        AsyncDataProvider.getMinCpus(new AsyncQuery<>(minCpus -> {
            if (cpuCores < minCpus) {
                getModel().getTotalCPUCores().setEntity(Integer.toString(minCpus));
            }
        }), getSelectedOSType());
    }

    protected void updateSeal() {
        if (!getModel().getIsSealed().getIsAvailable()) {
            return;
        }

        if (getModel().getIsWindowsOS()) {
            getModel().getIsSealed().setEntity(false);
            getModel().getIsSealed().setIsChangeable(false,
                    ConstantsManager.getInstance().getConstants().sealWindowsUnavailable());
            return;
        }

        getModel().getIsSealed().setIsChangeable(true);

        TemplateWithVersion selectedTemplateWithVersion = getModel().getTemplateWithVersion().getSelectedItem();
        if (selectedTemplateWithVersion == null) {
            getModel().getIsSealed().setEntity(false);
            return;
        }
        VmTemplate template = selectedTemplateWithVersion.getTemplateVersion();
        getModel().getIsSealed().setEntity(isSealByDefault(template));
    }

    protected boolean isSealByDefault(VmTemplate template) {
        return template.isSealed();
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

    protected <T> AsyncQuery<T> asyncQuery(AsyncCallback<T> callback) {
        return getModel().asyncQuery(callback);
    }

    public boolean isCustomCompatibilityVersionChangeInProgress() {
        return isCustomCompatibilityVersionChangeInProgress;
    }

    public void setCustomCompatibilityVersionChangeInProgress(boolean isCustomCompatibilityVersionChangeInProgress) {
    }

    public void setSavedCurrentCustomCompatibilityVersion(Version savedCurrentCustomCompatibilityVersion) {
        this.savedCurrentCustomCompatibilityVersion = savedCurrentCustomCompatibilityVersion;
    }

    public Version getSavedCurrentCustomCompatibilityVersion() {
        return savedCurrentCustomCompatibilityVersion;
    }

    public VirtioScsiUtil getVirtioScsiUtil() {
        return virtioScsiUtil;
    }

    /**
     * Return true if VM is of High Performance type or if includes host pinning
     * configuration (like cpu pinning or cpu pass through or NUMA pinning).
     */
    protected boolean isVmHpOrPinningConfigurationEnabled() {
        return getModel().getVmType().getSelectedItem() == VmType.HighPerformance
                || (getModel().getCpuPinning().getEntity() != null && !getModel().getCpuPinning().getEntity().isEmpty())
                || (getModel().getHostCpu().getEntity() != null && getModel().getHostCpu().getEntity().equals(true))
                || (getModel().getVmNumaNodes() != null
                    && getModel().getVmNumaNodes().stream().anyMatch(node -> !node.getVdsNumaNodeList().isEmpty()));
    }

    protected void useHostCpuValueChanged() {
        if(getModel().getHostCpu().getEntity() != null && getModel().getHostCpu().getEntity()) {
            getModel().getCustomCpu().setIsChangeable(false);
            getModel().getCustomCpu().setSelectedItem(""); //$NON-NLS-1$
        } else {
            getModel().getCustomCpu().setIsChangeable(true);
        }

        // Set migration mode
        getModel().getMigrationMode().setSelectedItem(
                isVmHpOrPinningConfigurationEnabled() ? MigrationSupport.IMPLICITLY_NON_MIGRATABLE : MigrationSupport.MIGRATABLE
        );
    }

    protected void useNumaPinningChanged(List<VmNumaNode> vNumaNodeList) {
        // Set migration mode
        getModel().getMigrationMode().setSelectedItem(
                isVmHpOrPinningConfigurationEnabled() ? MigrationSupport.IMPLICITLY_NON_MIGRATABLE : MigrationSupport.MIGRATABLE
        );
    }

    /**
     * Return true if VM is of "High Performance" type or if "Host Auto Assign" is enabled.
     * since calling updateDefaultHost() for checking which hosts are available for current chosen cluster
     * may change "Host Auto Assign" mode, then we need this method to change "cpu-pass-through" value accordingly.
     */
     protected boolean isHostCpuValueStillBasedOnTemp() {
         return !getModel().getIsAutoAssign().getEntity()
                 || getModel().getVmType().getSelectedItem() == VmType.HighPerformance;
     }

     protected List<Integer> getOsValues(ArchitectureType architectureType, Version version) {
         return AsyncDataProvider.getInstance().getOsIds(architectureType);
     }
}
