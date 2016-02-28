package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateForStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ProcessOvfUpdateForStoragePoolCommand <T extends ProcessOvfUpdateForStoragePoolParameters> extends StorageHandlingCommandBase<T> {

    private int itemsCountPerUpdate;
    private List<Guid> proccessedIdsInfo;
    private List<Long> proccessedOvfGenerationsInfo;
    private List<String> proccessedOvfConfigurationsInfo;
    private HashSet<Guid> proccessedDomains;
    private List<Guid> removedOvfIdsInfo;
    private OvfUpdateProcessHelper ovfUpdateProcessHelper;
    private List<Guid> activeDataDomainsIds;

    public ProcessOvfUpdateForStoragePoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStoragePoolId(parameters.getStoragePoolId());
        ovfUpdateProcessHelper = new OvfUpdateProcessHelper();
        activeDataDomainsIds = new LinkedList<>();
    }

    @Override
    protected Object getActionReturnValue() {
        return super.getActionReturnValue();
    }

    protected OvfUpdateProcessHelper getOvfUpdateProcessHelper() {
        return ovfUpdateProcessHelper;
    }

    protected int loadConfigValue() {
        return Config.<Integer> getValue(ConfigValues.OvfItemsCountPerUpdate);
    }

    @Override
    protected void executeCommand() {
        itemsCountPerUpdate = loadConfigValue();
        proccessedDomains = new HashSet<>();
        StoragePool pool = getStoragePool();
        proccessDomainsForOvfUpdate(pool);

        log.info("Attempting to update VM OVFs in Data Center '{}'", pool.getName());
        initProcessedInfoLists();

        updateOvfForVmsOfStoragePool(pool);

        log.info("Successfully updated VM OVFs in Data Center '{}'", pool.getName());
        log.info("Attempting to update template OVFs in Data Center '{}'", pool.getName());

        updateOvfForTemplatesOfStoragePool(pool);

        log.info("Successfully updated templates OVFs in Data Center '{}'", pool.getName());
        log.info("Attempting to remove unneeded template/vm OVFs in Data Center '{}'", pool.getName());

        removeOvfForTemplatesAndVmsOfStoragePool(pool);

        log.info("Successfully removed unneeded template/vm OVFs in Data Center '{}'", pool.getName());

        getReturnValue().setActionReturnValue(proccessedDomains);
        setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.OVF_UPDATE,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    protected void proccessDomainsForOvfUpdate(StoragePool pool) {
        List<StorageDomain> domainsInPool = getStorageDomainDao().getAllForStoragePool(pool.getId());
        for (StorageDomain domain : domainsInPool) {
            if (!domain.getStorageDomainType().isDataDomain() || domain.getStatus() != StorageDomainStatus.Active) {
                continue;
            }

            activeDataDomainsIds.add(domain.getId());
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
                performOvfUpdate(vmsAndTemplateMetadata);
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
    protected void performOvfUpdate(Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata) {

        markDomainsWithOvfsForOvfUpdate(vmsAndTemplateMetadata.keySet());

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
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata = new HashMap<>();
        List<VmTemplate> templates = getVmTemplateDao().getVmTemplatesByIds(idsToProcess);

        for (VmTemplate template : templates) {
            if (VmTemplateStatus.Locked != template.getStatus()) {
                updateTemplateDisksFromDb(template);
                boolean verifyDisksNotLocked = verifyImagesStatus(template.getDiskList());
                if (verifyDisksNotLocked) {
                    getOvfUpdateProcessHelper().loadTemplateData(template);
                    Long currentDbGeneration = getVmStaticDao().getDbGeneration(template.getId());
                    // currentDbGeneration can be null in case that the template was deleted during the run of OvfDataUpdater.
                    if (currentDbGeneration != null && template.getDbGeneration() == currentDbGeneration) {
                        proccessedOvfConfigurationsInfo.add(getOvfUpdateProcessHelper().buildMetadataDictionaryForTemplate(template, vmsAndTemplateMetadata));
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

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? super.getAuditLogTypeValue() : AuditLogType.UPDATE_OVF_FOR_STORAGE_POOL_FAILED;
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
                performOvfUpdate(vmsAndTemplateMetadata);
            }
        }
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

    /**
     * Returns true if all snapshots have a valid status to use in the OVF.
     */
    protected boolean verifySnapshotsStatus(List<Snapshot> snapshots) {
        for (Snapshot snapshot : snapshots) {
            if (snapshot.getStatus() != Snapshot.SnapshotStatus.OK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create and returns map contains valid vms metadata
     */
    protected Map<Guid, KeyValuePairCompat<String, List<Guid>>> populateVmsMetadataForOvfUpdate(List<Guid> idsToProcess) {
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata = new HashMap<>();
        List<VM> vms = getVmDao().getVmsByIds(idsToProcess);
        for (VM vm : vms) {
            if (VMStatus.ImageLocked != vm.getStatus()) {
                updateVmDisksFromDb(vm);
                if (!verifyImagesStatus(vm.getDiskList())) {
                    continue;
                }
                ArrayList<DiskImage> vmImages = getOvfUpdateProcessHelper().getVmImagesFromDb(vm);
                if (!verifyImagesStatus(vmImages)) {
                    continue;
                }
                vm.setSnapshots(getSnapshotDao().getAllWithConfiguration(vm.getId()));
                if (!verifySnapshotsStatus(vm.getSnapshots())) {
                    continue;
                }

                getOvfUpdateProcessHelper().loadVmData(vm);
                Long currentDbGeneration = getVmStaticDao().getDbGeneration(vm.getId());
                if (currentDbGeneration == null) {
                    log.warn("currentDbGeneration of VM (name: '{}', id: '{}') is null, probably because the VM was deleted during the run of OvfDataUpdater.",
                            vm.getName(),
                            vm.getId());
                    continue;
                }
                if (vm.getStaticData().getDbGeneration() == currentDbGeneration) {
                    proccessedOvfConfigurationsInfo.add(getOvfUpdateProcessHelper().buildMetadataDictionaryForVm(vm, vmsAndTemplateMetadata, vmImages));
                    proccessedIdsInfo.add(vm.getId());
                    proccessedOvfGenerationsInfo.add(vm.getStaticData().getDbGeneration());
                    proccessDisksDomains(vm.getDiskList());
                }
            }
        }
        return vmsAndTemplateMetadata;
    }

    protected void proccessDisksDomains(List<DiskImage> disks) {
        if (disks.isEmpty()) {
            proccessedDomains.addAll(activeDataDomainsIds);
            return;
        }

        for (DiskImage disk : disks) {
            proccessedDomains.addAll(disk.getStorageIds());
        }
    }

    /**
     * Init the lists contain the processed info.
     */
    private void initProcessedInfoLists() {
        proccessedIdsInfo = new LinkedList<>();
        proccessedOvfGenerationsInfo = new LinkedList<>();
        proccessedOvfConfigurationsInfo = new LinkedList<>();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution).withWait(true);
    }
}
