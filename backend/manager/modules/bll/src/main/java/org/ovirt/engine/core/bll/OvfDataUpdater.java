package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class OvfDataUpdater {
    private static final Log log = LogFactory.getLog(OvfDataUpdater.class);
    private static final OvfDataUpdater INSTANCE = new OvfDataUpdater();
    private int itemsCountPerUpdate;

    private List<Guid> proccessedIdsInfo;
    private List<Long> proccessedOvfGenerationsInfo;
    private List<String> proccessedOvfConfigurationsInfo;
    private HashSet<Guid> proccessedDomains;
    private List<Guid> removedOvfIdsInfo;

    private OvfManager ovfManager;

    private OvfDataUpdater() {
        ovfManager = new OvfManager();
    }

    public static OvfDataUpdater getInstance() {
        return INSTANCE;
    }

    protected StoragePoolDAO getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

    public void initOvfDataUpdater() {
        SchedulerUtil scheduler = SchedulerUtilQuartzImpl.getInstance();
        scheduler.scheduleAFixedDelayJob(this, "ovfUpdate_timer", new Class[] {},
                new Object[] {}, Config.<Integer> getValue(ConfigValues.OvfUpdateIntervalInMinutes),
                Config.<Integer> getValue(ConfigValues.OvfUpdateIntervalInMinutes), TimeUnit.MINUTES);
        log.info("Initialization of OvfDataUpdater completed successfully.");
    }

    protected int reloadConfigValue() {
        return Config.<Integer> getValue(ConfigValues.OvfItemsCountPerUpdate);
    }

    private LockManager getLockManager() {
        return LockManagerFactory.getLockManager();
    }

    protected boolean acquireLock(EngineLock engineLock) {
        return LockManagerFactory.getLockManager().acquireLock(engineLock).getFirst();
    }

    protected void releaseLock(EngineLock engineLock) {
        LockManagerFactory.getLockManager().releaseLock(engineLock);
    }

    private void logInfoIfNeeded(StoragePool pool, String message, Object... args) {
        // if supported, the info would be logged when executing for each domain
        if (!ovfOnAnyDomainSupported(pool)) {
            log.infoFormat(message, args);
        }
    }

    protected void proceedPoolOvfUpdate(StoragePool pool) {
        proccessedDomains = new HashSet<>();
        if (ovfOnAnyDomainSupported(pool)) {
            proccessDomainsForOvfUpdate(pool);
        }

        logInfoIfNeeded(pool, "Attempting to update VM OVFs in Data Center {0}",
                pool.getName());
        initProcessedInfoLists();

        updateOvfForVmsOfStoragePool(pool);

        logInfoIfNeeded(pool, "Successfully updated VM OVFs in Data Center {0}",
                pool.getName());
        logInfoIfNeeded(pool, "Attempting to update template OVFs in Data Center {0}",
                pool.getName());

        updateOvfForTemplatesOfStoragePool(pool);

        logInfoIfNeeded(pool, "Successfully updated templates OVFs in Data Center {0}",
                pool.getName());
        logInfoIfNeeded(pool, "Attempting to remove unneeded template/vm OVFs in Data Center {0}",
                pool.getName());

        removeOvfForTemplatesAndVmsOfStoragePool(pool);

        logInfoIfNeeded(pool, "Successfully removed unneeded template/vm OVFs in Data Center {0}",
                pool.getName());
    }

    protected void performOvfUpdateForDomain(Guid storagePoolId, Guid domainId) {
        Backend.getInstance().runInternalAction(VdcActionType.ProcessOvfUpdateForStorageDomain, new StorageDomainParametersBase(storagePoolId, domainId));
    }

    private EngineLock buildPoolEngineLock(StoragePool pool) {
        Map<String, Pair<String, String>> exclusiveLocks =
                Collections.singletonMap(pool.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.OVF_UPDATE,
                                VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return new EngineLock(exclusiveLocks, null);
    }

    @OnTimerMethodAnnotation("ovfUpdate_timer")
    public void ovfUpdate_timer() {
        itemsCountPerUpdate = reloadConfigValue();
        log.info("Attempting to update VMs/Templates Ovf.");
        List<StoragePool> storagePools = getStoragePoolDao().getAllByStatus(StoragePoolStatus.Up);
        for (StoragePool pool : storagePools) {
            EngineLock poolLock = buildPoolEngineLock(pool);
            if (!acquireLock(poolLock)) {
                    log.errorFormat("Failed to update OVFs in Data Center {0} as there is a related operation in progress.", pool.getName());
                continue;
            }

            boolean lockReleased = false;

            try {
                proceedPoolOvfUpdate(pool);
                if (ovfOnAnyDomainSupported(pool)) {
                    logInfoIfNeeded(pool, "Attempting to update ovfs in domain in Data Center {0}",
                            pool.getName());

                    releaseLock(poolLock);
                    lockReleased = true;

                    for (Guid id : proccessedDomains) {
                        performOvfUpdateForDomain(pool.getId(), id);
                    }
                }
            } catch (Exception ex) {
                addAuditLogError(pool.getName());
                log.errorFormat("Exception while trying to update or remove VMs/Templates ovf in Data Center {0}.", pool.getName(), ex);
            } finally {
                if (!lockReleased) {
                    releaseLock(poolLock);
                }
            }
        }
        proccessedIdsInfo = null;
        removedOvfIdsInfo = null;
        proccessedOvfGenerationsInfo = null;
        proccessedOvfConfigurationsInfo = null;
        proccessedDomains = null;
    }


    protected void proccessDomainsForOvfUpdate(StoragePool pool) {
        List<StorageDomain> domainsInPool = getStorageDomainDao().getAllForStoragePool(pool.getId());
        for (StorageDomain domain : domainsInPool) {
            if (!domain.getStorageDomainType().isDataDomain() || domain.getStatus() != StorageDomainStatus.Active) {
               continue;
            }

            Integer ovfStoresCountForDomain = Config.<Integer> getValue(ConfigValues.StorageDomainOvfStoreCount);
            List<StorageDomainOvfInfo> storageDomainOvfInfos = getStorageDomainOvfInfoDao().getAllForDomain(domain.getId());

            if (storageDomainOvfInfos.size() < ovfStoresCountForDomain) {
                proccessedDomains.add(domain.getId());
                continue;
            }

            for (StorageDomainOvfInfo storageDomainOvfInfo : storageDomainOvfInfos) {
                if (storageDomainOvfInfo.getStatus() == StorageDomainOvfInfoStatus.OUTDATED) {
                    proccessedDomains.add(storageDomainOvfInfo.getStorageDomainId());
                    break;
                }
            }
        }
    }

    /**
     * Update ovfs for updated/newly vms since last run for the given storage pool
     *
     */
    protected void updateOvfForVmsOfStoragePool(StoragePool pool) {
        Guid poolId = pool.getId();
        List<Guid> vmsIdsForUpdate = getVmAndTemplatesGenerationsDao().getVmsIdsForOvfUpdate(poolId);
        int i = 0;
        while (i < vmsIdsForUpdate.size()) {
            int size = Math.min(itemsCountPerUpdate, vmsIdsForUpdate.size() - i);
            List<Guid> idsToProcess = vmsIdsForUpdate.subList(i, i + size);
            i += size;

            Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata =
                    populateVmsMetadataForOvfUpdate(idsToProcess);
            if (!vmsAndTemplateMetadata.isEmpty()) {
                performOvfUpdate(pool, vmsAndTemplateMetadata);
            }
        }
    }

    /**
     * Removes from the storage ovf files of vm/templates that were removed from the db since the last OvfDataUpdater
     * run.
     *
     */
    protected void removeOvfForTemplatesAndVmsOfStoragePool(StoragePool pool) {
        Guid poolId = pool.getId();
        removedOvfIdsInfo = getVmAndTemplatesGenerationsDao().getIdsForOvfDeletion(poolId);

        if (!ovfOnAnyDomainSupported(pool)) {
            for (Guid id : removedOvfIdsInfo) {
                executeRemoveVmInSpm(poolId, id, Guid.Empty);
            }
        }

        markDomainsWithOvfsForOvfUpdate(removedOvfIdsInfo);
        getVmAndTemplatesGenerationsDao().deleteOvfGenerations(removedOvfIdsInfo);
    }

    protected void markDomainsWithOvfsForOvfUpdate(Collection<Guid> ovfIds) {
        List<Guid> relevantDomains = getStorageDomainOvfInfoDao().loadStorageDomainIdsForOvfIds(ovfIds);
        proccessedDomains.addAll(relevantDomains);
        getStorageDomainOvfInfoDao().updateOvfUpdatedInfo(proccessedDomains, StorageDomainOvfInfoStatus.OUTDATED, StorageDomainOvfInfoStatus.DISABLED);
    }

    /**
     * Perform vdsm call which performs ovf update for the given metadata map
     *
     */
    protected void performOvfUpdate(StoragePool pool,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata) {
        if (!ovfOnAnyDomainSupported(pool)) {
            executeUpdateVmInSpmCommand(pool.getId(), vmsAndTemplateMetadata, Guid.Empty);
        } else {
            markDomainsWithOvfsForOvfUpdate(vmsAndTemplateMetadata.keySet());
        }

        int i = 0;
        while (i < proccessedIdsInfo.size()) {
            int sizeToUpdate = Math.min(StorageConstants.OVF_MAX_ITEMS_PER_SQL_STATEMENT, proccessedIdsInfo.size() - i);
            List<Guid> guidsForUpdate = proccessedIdsInfo.subList(i, i + sizeToUpdate);
            List<Long> ovfGenerationsForUpdate = proccessedOvfGenerationsInfo.subList(i, i + sizeToUpdate);
            List<String> ovfConfigurationsInfo = proccessedOvfConfigurationsInfo.subList(i, i + sizeToUpdate);
            getVmAndTemplatesGenerationsDao().updateOvfGenerations(guidsForUpdate, ovfGenerationsForUpdate, ovfConfigurationsInfo);
            i += sizeToUpdate;
            initProcessedInfoLists();
        }
    }

    /**
     * Creates and returns a map containing valid templates metadata
     */
    protected Map<Guid, KeyValuePairCompat<String, List<Guid>>> populateTemplatesMetadataForOvfUpdate(List<Guid> idsToProcess) {
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata =
                new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>();
        List<VmTemplate> templates = getVmTemplateDao().getVmTemplatesByIds(idsToProcess);

        for (VmTemplate template : templates) {
            if (VmTemplateStatus.Locked != template.getStatus()) {
                updateTemplateDisksFromDb(template);
                boolean verifyDisksNotLocked = verifyImagesStatus(template.getDiskList());
                if (verifyDisksNotLocked) {
                    loadTemplateData(template);
                    Long currentDbGeneration = getVmStaticDao().getDbGeneration(template.getId());
                    // currentDbGeneration can be null in case that the template was deleted during the run of OvfDataUpdater.
                    if (currentDbGeneration != null && template.getDbGeneration() == currentDbGeneration) {
                        proccessedOvfConfigurationsInfo.add(buildMetadataDictionaryForTemplate(template, vmsAndTemplateMetadata));
                        proccessedIdsInfo.add(template.getId());
                        proccessedOvfGenerationsInfo.add(template.getDbGeneration());
                        proccessDisksDomains(template.getDiskList());
                    }
                }
            }
        }

        return vmsAndTemplateMetadata;
    }

    protected void updateTemplateDisksFromDb(VmTemplate template) {
        VmTemplateHandler.updateDisksFromDb(template);
    }

    protected void updateVmDisksFromDb(VM vm) {
        VmHandler.updateDisksFromDb(vm);
    }

    /**
     * Update ovfs for updated/added templates since last for the given storage pool
     */
    protected void updateOvfForTemplatesOfStoragePool(StoragePool pool) {
        Guid poolId = pool.getId();
        List<Guid> templateIdsForUpdate =
                getVmAndTemplatesGenerationsDao().getVmTemplatesIdsForOvfUpdate(poolId);
        int i = 0;
        while (i < templateIdsForUpdate.size()) {
            int size = Math.min(templateIdsForUpdate.size() - i, itemsCountPerUpdate);
            List<Guid> idsToProcess = templateIdsForUpdate.subList(i, i + size);
            i += size;

            Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata =
                    populateTemplatesMetadataForOvfUpdate(idsToProcess);
            if (!vmsAndTemplateMetadata.isEmpty()) {
                performOvfUpdate(pool, vmsAndTemplateMetadata);
            }
        }
    }

    /**
     * Create and returns map contains valid vms metadata
     */
    protected Map<Guid, KeyValuePairCompat<String, List<Guid>>> populateVmsMetadataForOvfUpdate(List<Guid> idsToProcess) {
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata =
                new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>();
        List<VM> vms = getVmDao().getVmsByIds(idsToProcess);
        for (VM vm : vms) {
            if (VMStatus.ImageLocked != vm.getStatus()) {
                updateVmDisksFromDb(vm);
                if (!verifyImagesStatus(vm.getDiskList())) {
                    continue;
                }
                ArrayList<DiskImage> vmImages = getVmImagesFromDb(vm);
                if (!verifyImagesStatus(vmImages)) {
                    continue;
                }
                vm.setSnapshots(getSnapshotDao().getAllWithConfiguration(vm.getId()));
                if (!verifySnapshotsStatus(vm.getSnapshots())) {
                    continue;
                }

                loadVmData(vm);
                Long currentDbGeneration = getVmStaticDao().getDbGeneration(vm.getId());
                if (currentDbGeneration == null) {
                    log.warnFormat("currentDbGeneration of VM (name: {0}, id: {1}) is null, probably because the VM was deleted during the run of OvfDataUpdater.",
                            vm.getName(),
                            vm.getId());
                    continue;
                }
                if (vm.getStaticData().getDbGeneration() == currentDbGeneration) {
                    proccessedOvfConfigurationsInfo.add(buildMetadataDictionaryForVm(vm, vmsAndTemplateMetadata, vmImages));
                    proccessedIdsInfo.add(vm.getId());
                    proccessedOvfGenerationsInfo.add(vm.getStaticData().getDbGeneration());
                    proccessDisksDomains(vm.getDiskList());
                }
            }
        }
        return vmsAndTemplateMetadata;
    }

    protected ArrayList<DiskImage> getVmImagesFromDb(VM vm) {
        ArrayList<DiskImage> allVmImages = new ArrayList<>();
        List<DiskImage> filteredDisks = ImagesHandler.filterImageDisks(vm.getDiskList(), false, true, true);

        for (DiskImage diskImage : filteredDisks) {
            allVmImages.addAll(getAllImageSnapshots(diskImage));
        }
        return allVmImages;
    }

    protected void proccessDisksDomains(List<DiskImage> disks) {
        for (DiskImage disk : disks) {
            proccessedDomains.addAll(disk.getStorageIds());
        }
    }

    /**
     * Returns true if all snapshots have a valid status to use in the OVF.
     */
    protected boolean verifySnapshotsStatus(List<Snapshot> snapshots) {
        for (Snapshot snapshot : snapshots) {
            if (snapshot.getStatus() != SnapshotStatus.OK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if none of the given disks is in status 'LOCKED', otherwise false.
     */
    protected boolean verifyImagesStatus(List<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getImageStatus() == ImageStatus.LOCKED) {
                return false;
            }
        }
        return true;
    }

    protected String generateVmTemplateMetadata(VmTemplate template, List<DiskImage> allTemplateImages) {
        return ovfManager.ExportTemplate(template, allTemplateImages, ClusterUtils.getCompatibilityVersion(template));
    }

    /**
     * Adds the given template metadata to the given map
     */
    protected String buildMetadataDictionaryForTemplate(VmTemplate template,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary) {
        List<DiskImage> allTemplateImages = template.getDiskList();
        String templateMeta = generateVmTemplateMetadata(template, allTemplateImages);
        metaDictionary.put(template.getId(), new KeyValuePairCompat<String, List<Guid>>(
                templateMeta, LinqUtils.transformToList(allTemplateImages, new Function<DiskImage, Guid>() {
                    @Override
                    public Guid eval(DiskImage diskImage) {
                        return diskImage.getId();
                    }
                })));
        return templateMeta;
    }

    /**
     * Loads additional need template data for it's ovf
     */
    protected void loadTemplateData(VmTemplate template) {
        setManagedVideoDevices(template);
        if (template.getInterfaces() == null || template.getInterfaces().isEmpty()) {
            template.setInterfaces(getVmNetworkInterfaceDao()
                    .getAllForTemplate(template.getId()));
        }
    }

    /**
     * Loads additional need vm data for it's ovf
     */
    protected void loadVmData(VM vm) {
        setManagedVideoDevices(vm.getStaticData());
        if (vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }
        if (StringUtils.isEmpty(vm.getVmtName())) {
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                VmTemplate t = getVmTemplateDao().get(vm.getVmtGuid());
                vm.setVmtName(t.getName());
            } else {
                vm.setVmtName(VmTemplateHandler.BLANK_VM_TEMPLATE_NAME);
            }
        }
    }

    private void setManagedVideoDevices(VmBase vmBase) {
        Map<Guid, VmDevice> managedDeviceMap = vmBase.getManagedDeviceMap();
        if (managedDeviceMap == null) {
            managedDeviceMap = new HashMap<Guid, VmDevice>();
        }
        List<VmDevice> devices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vmBase.getId(), VmDeviceGeneralType.VIDEO);
        for (VmDevice device : devices) {
            managedDeviceMap.put(device.getDeviceId(), device);
        }
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
    }

    protected String generateVmMetadata(VM vm, ArrayList<DiskImage> AllVmImages) {
        return ovfManager.ExportVm(vm, AllVmImages, ClusterUtils.getCompatibilityVersion(vm));
    }

    /**
     * Adds the given vm metadata to the given map
     */
    protected String buildMetadataDictionaryForVm(VM vm,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
            ArrayList<DiskImage> allVmImages) {
        String vmMeta = generateVmMetadata(vm, allVmImages);
        metaDictionary.put(
                vm.getId(),
                new KeyValuePairCompat<String, List<Guid>>(vmMeta, LinqUtils.transformToList(vm.getDiskMap().values(),
                        new Function<Disk, Guid>() {
                            @Override
                            public Guid eval(Disk a) {
                                return a.getId();
                            }
                        })));
        return vmMeta;
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    protected VmTemplateDAO getVmTemplateDao() {
        return DbFacade.getInstance().getVmTemplateDao();
    }

    protected StoragePoolIsoMapDAO getSoragePoolIsoMapDao() {
        return DbFacade.getInstance().getStoragePoolIsoMapDao();
    }

    protected StorageDomainDAO getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected VmAndTemplatesGenerationsDAO getVmAndTemplatesGenerationsDao() {
        return DbFacade.getInstance().getVmAndTemplatesGenerationsDao();
    }

    protected StorageDomainOvfInfoDao getStorageDomainOvfInfoDao() {
        return DbFacade.getInstance().getStorageDomainOvfInfoDao();
    }

    protected VmStaticDAO getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    /**
     * Init the lists contain the processed info.
     */
    private void initProcessedInfoLists() {
        proccessedIdsInfo = new LinkedList<Guid>();
        proccessedOvfGenerationsInfo = new LinkedList<Long>();
        proccessedOvfConfigurationsInfo = new LinkedList<>();
        removedOvfIdsInfo = null;
    }

    /**
     * Update the information contained in the given meta dictionary table in the given storage pool/storage domain.
     */
    protected boolean executeUpdateVmInSpmCommand(Guid storagePoolId,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
            Guid storageDomainId) {
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, metaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
    }

    /**
     * Removes the ovf of the vm/template with the given id from the given storage pool/storage domain.
     */
    protected boolean executeRemoveVmInSpm(Guid storagePoolId, Guid id, Guid storageDomainId) {
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.RemoveVM,
                new RemoveVMVDSCommandParameters(storagePoolId, id, storageDomainId)).getSucceeded();
    }

    protected void addAuditLogError(String storagePoolName) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("StoragePoolName", storagePoolName);
        AuditLogDirector.log(logable, AuditLogType.UPDATE_OVF_FOR_STORAGE_POOL_FAILED);
    }

    protected boolean ovfOnAnyDomainSupported(StoragePool pool) {
        return FeatureSupported.ovfStoreOnAnyDomain(pool.getcompatibility_version());
    }
}
