package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.MultiQueuesVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class NewTemplateVmModelBehavior extends VmModelBehaviorBase<UnitVmModel> {
    private final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private VM vm;
    private Guid snapshotId;

    public NewTemplateVmModelBehavior() {
    }

    public NewTemplateVmModelBehavior(VM vm) {
        this.vm = vm;
    }

    public NewTemplateVmModelBehavior(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    protected VM getVm() {
        return vm;
    }

    @Override
    public void initialize() {
        super.initialize();
        getModel().getVmInitEnabled().setEntity(vm.getVmInit() != null);
        getModel().getVmInitModel().init(vm.getStaticData());

        getModel().getVmType().setIsChangeable(true);
        getModel().getCopyPermissions().setIsAvailable(true);

        DisksAllocationModel disksAllocationModel = getModel().getDisksAllocationModel();
        disksAllocationModel.setIsVolumeFormatAvailable(true);
        disksAllocationModel.setIsVolumeFormatChangeable(true);
        disksAllocationModel.setIsAliasChangeable(true);
        disksAllocationModel.setContainer(getModel());
        disksAllocationModel.setIsThinProvisioning(false);

        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery<>(
                        dataCenter -> {
                            if (dataCenter == null) {
                                disableNewTemplateModel(ConstantsManager.getInstance()
                                        .getConstants()
                                        .dataCenterIsNotAccessibleMsg());
                            } else {

                                AsyncDataProvider.getInstance().getClusterListByService(
                                        new AsyncQuery<>(clusters -> {
                                            List<Cluster> filteredClusters =
                                                    AsyncDataProvider.getInstance().filterByArchitecture(clusters,
                                                            vm.getClusterArch());

                                            getModel().setDataCentersAndClusters(getModel(),
                                                    Arrays.asList(dataCenter),
                                                    filteredClusters,
                                                    vm.getClusterId());

                                            initTemplate();

                                        }),
                                        true,
                                        false);

                                AsyncDataProvider.getInstance().isSoundcardEnabled(new AsyncQuery<>(
                                        returnValue -> getModel().getIsSoundcardEnabled().setEntity(returnValue)), vm.getId());

                            }

                        }),
                vm.getStoragePoolId());
    }

    protected void updateTemplate() {
        final DataCenterWithCluster dataCenterWithCluster =
                getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool dataCenter = dataCenterWithCluster == null ? null : dataCenterWithCluster.getDataCenter();
        if (dataCenter == null) {
            return;
        }

        AsyncDataProvider.getInstance().getTemplateListByDataCenter(new AsyncQuery<>(
                templates -> postInitTemplate(AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templates,
                        dataCenterWithCluster.getCluster().getArchitecture()))), dataCenter.getId());
    }

    private void postInitTemplate(List<VmTemplate> templates) {
        List<VmTemplate> baseWithoutBlank = filterOutBlank(keepBaseTemplates(templates));

        getModel().getIsSubTemplate().setEntity(false);
        if (baseWithoutBlank.isEmpty()) {
            // it is not allowed to create sub-templates of Blank template
            getModel().getIsSubTemplate().setIsChangeable(false,
                    constants.someNonDefaultTemplateHasToExistFirst());
            return;
        }

        getModel().getIsSubTemplate().setIsChangeable(true);

        VmTemplate currentTemplate = Linq.firstOrNull(templates,
                new Linq.IdPredicate<>(vm.getVmtGuid()));

        getModel().getBaseTemplate().setItems(baseWithoutBlank);

        getModel().getBaseTemplate().setSelectedItem(Linq.firstOrNull(baseWithoutBlank,
                new Linq.IdPredicate<>(currentTemplate.getBaseTemplateId())));
    }

    private List<VmTemplate> filterOutBlank(List<VmTemplate> templates) {
        final List<VmTemplate> result = new ArrayList<>();
        for (VmTemplate template : templates) {
            if (!template.isBlank()) {
                result.add(template);
            }
        }
        return result;
    }

    @Override
    public void dataCenterWithClusterSelectedItemChanged() {
        super.dataCenterWithClusterSelectedItemChanged();

        if (snapshotId != null) {
            // In case of creating a template from a snapshot, the snapshot VM configuration is needed
            AsyncDataProvider.getInstance().getVmConfigurationBySnapshot(asyncQuery(
                    vm -> initDisksAndStorageDomains(new ArrayList<>(vm.getDiskMap().values()))), snapshotId);
        } else {
            // If a VM has at least one disk, present its storage domain.
            AsyncDataProvider.getInstance().getVmDiskList(asyncQuery(
                    this::initDisksAndStorageDomains),
                    vm.getId(),
                    true);
        }
    }

    private void initDisksAndStorageDomains(List<Disk> disks) {
        ArrayList<Disk> imageDisks = new ArrayList<>();

        for (Disk disk : disks) {
            if (disk.isShareable() || disk.isDiskSnapshot()) {
                continue;
            }
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE ||
                    disk.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                imageDisks.add(disk);
            }
        }

        initStorageDomains();
        initDisks(imageDisks);

        VmModelHelper.sendWarningForNonExportableDisks(getModel(),
                disks,
                VmModelHelper.WarningType.VM_TEMPLATE);
    }

    private void initDisks(ArrayList<Disk> disks) {
        Collections.sort(disks, new DiskByDiskAliasComparator());
        ArrayList<DiskModel> list = new ArrayList<>();

        for (Disk disk : disks) {
            DiskModel diskModel = new DiskModel();
            switch (disk.getDiskStorageType()) {
                case IMAGE:
                    DiskImage diskImage = (DiskImage) disk;
                    diskModel.setSize(new EntityModel<>((int) diskImage.getSizeInGigabytes()));
                    ListModel volumes = new ListModel();
                    volumes.setItems(diskImage.getVolumeType() == VolumeType.Preallocated ? new ArrayList<>(Arrays.asList(new VolumeType[]{VolumeType.Preallocated}))
                            : AsyncDataProvider.getInstance().getVolumeTypeList(), diskImage.getVolumeType());
                    diskModel.setVolumeType(volumes);
                    diskModel.getAlias().setEntity(diskImage.getDiskAlias());
                    diskModel.setVm(getVm());
                    break;
            case MANAGED_BLOCK_STORAGE:
                    ManagedBlockStorageDisk managedBlockDisk = (ManagedBlockStorageDisk) disk;
                    diskModel.setSize(new EntityModel<>((int) managedBlockDisk.getSizeInGigabytes()));
                    ListModel managedBlockVolumeTypes = new ListModel();
                    managedBlockVolumeTypes.setItems(new ArrayList<>(Arrays.asList(VolumeType.Preallocated)),
                                VolumeType.Preallocated);
                    diskModel.setVolumeType(managedBlockVolumeTypes);
                    diskModel.getAlias().setEntity(managedBlockDisk.getDiskAlias());
                    diskModel.getVolumeFormat().setIsChangeable(false);
                    break;
            }
            diskModel.setDisk(disk);
            list.add(diskModel);
            diskModel.getVolumeFormat().setIsAvailable(true);
        }
        getModel().setDisks(list);
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        updateQuotaByCluster(vm.getQuotaId(), vm.getQuotaName());
        updateMemoryBalloon();
        updateCpuSharesAvailability();
        updateVirtioScsiAvailability();
        updateOSValues();
        updateTemplate();
        updateNumOfSockets();
        if(getModel().getSelectedCluster() != null) {
            updateCpuProfile(getModel().getSelectedCluster().getId(), vm.getCpuProfileId());
        }
        updateCustomPropertySheet();
        getModel().getCustomPropertySheet().deserialize(vm.getCustomProperties());
        updateLeaseStorageDomains(vm.getLeaseStorageDomainId());
    }

    @Override
    public void oSType_SelectedItemChanged() {
        super.oSType_SelectedItemChanged();

        Integer osType = getModel().getOSType().getSelectedItem();
        if (osType != null) {
            getVirtioScsiUtil().updateVirtioScsiEnabled(vm.getId(), osType);
        }
    }

    @Override
    public void defaultHost_SelectedItemChanged() {
    }

    @Override
    public void provisioning_SelectedItemChanged() {
    }

    private void initTemplate() {
        // Update model state according to VM properties.
        buildModel(this.vm.getStaticData(), (source, destination) -> {
            updateSelectedCdImage(vm.getStaticData());
            updateTimeZone(vm.getTimeZone());
            updateTpm(vm.getId());
            updateConsoleDevice(vm.getId());

            getModel().getStorageDomain().setIsChangeable(true);
            getModel().getProvisioning().setIsAvailable(false);

            // Select display protocol.
            DisplayType displayType = vm.getDefaultDisplayType();
            if (getModel().getDisplayType().getItems().contains(displayType)) {
                getModel().getDisplayType().setSelectedItem(displayType);
            }

            initPriority(vm.getPriority());
        });
    }

    @Override
    protected void buildModel(VmBase vmBase,
                              BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
        new BuilderExecutor<>(callback,
                              new CommonVmBaseToUnitBuilder(),
                              new MultiQueuesVmBaseToUnitBuilder())
                .build(vmBase, getModel());
    }

    @Override
    public void initStorageDomains() {
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(asyncQuery(
                storageDomains -> {
                    ArrayList<StorageDomain> activeStorageDomainList = new ArrayList<>();

                    for (StorageDomain storageDomain : storageDomains) {
                        if (storageDomain.getStatus() == StorageDomainStatus.Active
                                && storageDomain.getStorageDomainType().isDataDomain()) {
                            activeStorageDomainList.add(storageDomain);
                        }
                    }

                    for (DiskModel diskModel : getModel().getDisks()) {
                        if (diskModel.getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
                            DiskImage diskImage = (DiskImage) diskModel.getDisk();
                            List<StorageDomain> activeDiskStorages =
                                    Linq.getStorageDomainsByIds(diskImage.getStorageIds(), activeStorageDomainList);

                            if (activeDiskStorages.isEmpty()) {
                                disableNewTemplateModel(
                                        ConstantsManager.getInstance()
                                                .getMessages()
                                                .vmStorageDomainIsNotAccessible());

                                return;
                            }
                        }
                    }

                    if (activeStorageDomainList.size() > 0) {
                        getModel().getStorageDomain().setItems(activeStorageDomainList);
                        getModel().getStorageDomain().setIsChangeable(true);
                    } else {
                        disableNewTemplateModel(ConstantsManager.getInstance()
                                .getMessages()
                                .noActiveStorageDomain());
                    }

                    ArrayList<DiskModel> disks = (ArrayList<DiskModel>) getModel().getDisksAllocationModel().getDisks();

                    Collections.sort(activeStorageDomainList, new NameableComparator());
                    if (disks != null) {
                        List<DiskModel> diskImages = Linq.filterDisksByType(disks, DiskStorageType.IMAGE);
                        for (DiskModel diskModel : diskImages) {
                            diskModel.getStorageDomain().setItems(activeStorageDomainList);
                        }
                        initStorageDomainForType(StorageType.MANAGED_BLOCK_STORAGE, DiskStorageType.MANAGED_BLOCK_STORAGE, disks, storageDomains);
                    }
                }),
                vm.getStoragePoolId(),
                ActionGroup.CREATE_TEMPLATE);
    }

    private void disableNewTemplateModel(String errMessage) {
        getModel().setIsValid(false);
        getModel().setMessage(errMessage);
        getModel().getName().setIsChangeable(false);
        getModel().getDescription().setIsChangeable(false);
        getModel().getDataCenterWithClustersList().setIsChangeable(false);
        getModel().getComment().setIsChangeable(false);
        getModel().getStorageDomain().setIsChangeable(false);
        getModel().getIsTemplatePublic().setIsChangeable(false);
        getModel().getDefaultCommand().setIsAvailable(false);
    }

    @Override
    public void postOsItemChanged() {
        if (vm != null) {
            getModel().getOSType().setSelectedItem(vm.getOs());
        }
    }

    @Override
    protected void baseTemplateSelectedItemChanged() {
        if (getModel().getBaseTemplate().getSelectedItem() != null && getModel().getIsSubTemplate().getEntity()) {
            // template.name for version should be the same as the base template name
            getModel().getName().setEntity(getModel().getBaseTemplate().getSelectedItem().getName());
        }
    }

    @Override
    protected void isSubTemplateEntityChanged() {
        getModel().getName().setIsChangeable(!getModel().getIsSubTemplate().getEntity());
        if (!getModel().getIsSubTemplate().getEntity()) {
            getModel().getName().setEntity(""); //$NON-NLS-1$
        } else {
            // there will always be at least 'Blank' base template
            getModel().getBaseTemplate().setSelectedItem(getModel().getBaseTemplate().getItems().iterator().next());

            // copy any entered name to be the template-version name
            getModel().getTemplateVersionName().setEntity(getModel().getName().getEntity());
            getModel().getName().setEntity(getModel().getBaseTemplate().getSelectedItem().getName());
        }
    }
}
