package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
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
    protected static final int MAX_ITEMS_PER_SQL_STATEMENT = 100;

    private List<Guid> proccessedIdsInfo;
    private List<Long> proccessedOvfGenerationsInfo;

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
                new Object[] {}, Config.<Integer> GetValue(ConfigValues.OvfUpdateIntervalInMinutes),
                Config.<Integer> GetValue(ConfigValues.OvfUpdateIntervalInMinutes), TimeUnit.MINUTES);
        log.info("Initialization of OvfDataUpdater completed successfully.");
    }

    protected int reloadConfigValue() {
        return Config.<Integer> GetValue(ConfigValues.OvfItemsCountPerUpdate);
    }

    @OnTimerMethodAnnotation("ovfUpdate_timer")
    public void ovfUpdate_timer() {
        itemsCountPerUpdate = reloadConfigValue();
        log.info("Attempting to update VMs/Templates Ovf.");
        List<storage_pool> storagePools = getStoragePoolDao().getAllByStatus(StoragePoolStatus.Up);
        for (storage_pool pool : storagePools) {
            try {
                log.infoFormat("Attempting to update VM OVFs in Data Center {0}",
                        pool.getname());
                initProcessedInfoLists();

                updateOvfForVmsOfStoragePool(pool.getId());

                log.infoFormat("Successfully updated VM OVFs in Data Center {0}",
                        pool.getname());
                log.infoFormat("Attempting to update template OVFs in Data Center {0}",
                        pool.getname());

                updateOvfForTemplatesOfStoragePool(pool.getId());

                log.infoFormat("Successfully updated templates OVFs in Data Center {0}",
                        pool.getname());
                log.infoFormat("Attempting to remove unneeded template/vm OVFs in Data Center {0}",
                        pool.getname());

                removeOvfForTemplatesAndVmsOfStoragePool(pool.getId());

                log.infoFormat("Successfully removed unneeded template/vm OVFs in Data Center {0}",
                        pool.getname());
            } catch (Exception ex) {
                addAuditLogError(pool.getname());
                log.errorFormat("Exception while trying to update or remove VMs/Templates ovf in Data Center {0}.", pool.getname(), ex);
            }
        }
        proccessedIdsInfo = null;
        proccessedOvfGenerationsInfo = null;
    }

    /**
     * Update ovfs for updated/newly vms since last run for the given storage pool
     *
     */
    protected void updateOvfForVmsOfStoragePool(Guid poolId) {
        List<Guid> vmsIdsForUpdate = getVmAndTemplatesGenerationsDao().getVmsIdsForOvfUpdate(poolId);
        int i = 0;
        while (i < vmsIdsForUpdate.size()) {
            int size = Math.min(itemsCountPerUpdate, vmsIdsForUpdate.size() - i);
            List<Guid> idsToProcess = vmsIdsForUpdate.subList(i, i + size);
            i += size;

            Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata =
                    populateVmsMetadataForOvfUpdate(idsToProcess);
            if (!vmsAndTemplateMetadata.isEmpty()) {
                performOvfUpdate(poolId, vmsAndTemplateMetadata);
            }
        }
    }

    /**
     * Removes from the storage ovf files of vm/templates that were removed from the db since the last OvfDataUpdater
     * run.
     *
     */
    protected void removeOvfForTemplatesAndVmsOfStoragePool(Guid poolId) {
        List<Guid> idsForRemoval = getVmAndTemplatesGenerationsDao().getIdsForOvfDeletion(poolId);

        if (!idsForRemoval.isEmpty()) {
            for (Guid id : idsForRemoval) {
                executeRemoveVmInSpm(poolId, id, Guid.Empty);
            }

            getVmAndTemplatesGenerationsDao().deleteOvfGenerations(idsForRemoval);
        }
    }

    /**
     * Perform vdsm call which performs ovf update for the given metadata map
     *
     */
    protected void performOvfUpdate(Guid poolId,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndTemplateMetadata) {
        executeUpdateVmInSpmCommand(poolId, vmsAndTemplateMetadata, Guid.Empty);
        int i = 0;
        while (i < proccessedIdsInfo.size()) {
            int sizeToUpdate = Math.min(MAX_ITEMS_PER_SQL_STATEMENT, proccessedIdsInfo.size() - i);
            List<Guid> guidsForUpdate = proccessedIdsInfo.subList(i, i + sizeToUpdate);
            List<Long> ovfGenerationsForUpdate = proccessedOvfGenerationsInfo.subList(i, i + sizeToUpdate);
            getVmAndTemplatesGenerationsDao().updateOvfGenerations(guidsForUpdate, ovfGenerationsForUpdate);
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
            if (VmTemplateStatus.Locked != template.getstatus()) {
                updateTemplateDisksFromDb(template);
                boolean verifyDisksNotLocked = verifyDisksNotLocked(template.getDiskList());
                if (verifyDisksNotLocked) {
                    loadTemplateData(template);
                    Long currentDbGeneration = getVmStaticDao().getDbGeneration(template.getId());
                    // currentDbGeneration can be null in case that the template was deleted during the run of OvfDataUpdater.
                    if (currentDbGeneration != null && template.getDbGeneration() == currentDbGeneration) {
                        buildMetadataDictionaryForTemplate(template, vmsAndTemplateMetadata);
                        proccessedIdsInfo.add(template.getId());
                        proccessedOvfGenerationsInfo.add(template.getDbGeneration());
                    }
                }
            }
        }

        return vmsAndTemplateMetadata;
    }

    protected void updateTemplateDisksFromDb(VmTemplate template) {
        VmTemplateHandler.UpdateDisksFromDb(template);
    }

    protected void updateVmDisksFromDb(VM vm) {
        VmHandler.updateDisksFromDb(vm);
    }

    /**
     * Update ovfs for updated/added templates since last for the given storage pool
     */
    protected void updateOvfForTemplatesOfStoragePool(Guid poolId) {
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
                performOvfUpdate(poolId, vmsAndTemplateMetadata);
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
                if (verifyDisksNotLocked(vm.getDiskList())) {
                    loadVmData(vm);
                    Long currentDbGeneration = getVmStaticDao().getDbGeneration(vm.getId());
                    // currentDbGeneration can be null in case that the vm was deleted during the run of OvfDataUpdater.
                    if (currentDbGeneration != null && vm.getStaticData().getDbGeneration() == currentDbGeneration) {
                        buildMetadataDictionaryForVm(vm, vmsAndTemplateMetadata);
                        proccessedIdsInfo.add(vm.getId());
                        proccessedOvfGenerationsInfo.add(vm.getStaticData().getDbGeneration());
                    }
                }
            }
        }
        return vmsAndTemplateMetadata;
    }

    /**
     * Returns true if none of the given disks is in status 'LOCKED', otherwise false.
     */
    protected boolean verifyDisksNotLocked(List<DiskImage> disks) {
        for (DiskImage disk : disks) {
            if (disk.getImageStatus() == ImageStatus.LOCKED) {
                return false;
            }
        }
        return true;
    }

    protected String generateVmTemplateMetadata(VmTemplate template, List<DiskImage> allTemplateImages) {
        return ovfManager.ExportTemplate(template, allTemplateImages);
    }

    /**
     * Adds the given template metadata to the given map
     */
    protected void buildMetadataDictionaryForTemplate(VmTemplate template,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary) {
        List<DiskImage> allTemplateImages = template.getDiskList();
        String templateMeta = generateVmTemplateMetadata(template, allTemplateImages);
        metaDictionary.put(template.getId(), new KeyValuePairCompat<String, List<Guid>>(
                templateMeta, LinqUtils.foreach(allTemplateImages, new Function<DiskImage, Guid>() {
                    @Override
                    public Guid eval(DiskImage diskImage) {
                        return diskImage.getId().getValue();
                    }
                })));
    }

    /**
     * Loads additional need template data for it's ovf
     */
    protected void loadTemplateData(VmTemplate template) {
        if (template.getInterfaces() == null || template.getInterfaces().isEmpty()) {
            template.setInterfaces(getVmNetworkInterfaceDao()
                    .getAllForTemplate(template.getId()));
        }
    }

    /**
     * Loads additional need vm data for it's ovf
     */
    protected void loadVmData(VM vm) {
        if (vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }
        if (StringUtils.isEmpty(vm.getVmtName())) {
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                VmTemplate t = getVmTemplateDao().get(vm.getVmtGuid());
                vm.setVmtName(t.getname());
            } else {
                vm.setVmtName(VmTemplateHandler.BlankVmTemplateName);
            }
        }
    }

    protected ArrayList<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId(),
                diskImage.getit_guid());
    }

    protected String generateVmMetadata(VM vm, ArrayList<DiskImage> AllVmImages) {
        return ovfManager.ExportVm(vm, AllVmImages);
    }

    /**
     * Adds the given vm metadata to the given map
     */
    protected void buildMetadataDictionaryForVm(VM vm, Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary) {
        ArrayList<DiskImage> AllVmImages = new ArrayList<DiskImage>();
        for (Disk disk : vm.getDiskMap().values()) {
            if (disk.isAllowSnapshot()) {
                DiskImage diskImage = (DiskImage) disk;
                AllVmImages.addAll(getAllImageSnapshots(diskImage));
            }
        }
        String vmMeta = generateVmMetadata(vm, AllVmImages);
        metaDictionary.put(
                vm.getId(),
                new KeyValuePairCompat<String, List<Guid>>(vmMeta, LinqUtils.foreach(vm.getDiskMap().values(),
                        new Function<Disk, Guid>() {
                            @Override
                            public Guid eval(Disk a) {
                                return a.getId();
                            }
                        })));
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    protected VmTemplateDAO getVmTemplateDao() {
        return DbFacade.getInstance().getVmTemplateDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected VmAndTemplatesGenerationsDAO getVmAndTemplatesGenerationsDao() {
        return DbFacade.getInstance().getVmAndTemplatesGenerationsDao();
    }

    protected VmStaticDAO getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDao();
    }

    /**
     * Init the lists contain the processed info.
     */
    private void initProcessedInfoLists() {
        proccessedIdsInfo = new LinkedList<Guid>();
        proccessedOvfGenerationsInfo = new LinkedList<Long>();
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
        logable.AddCustomValue("StoragePoolName", storagePoolName);
        AuditLogDirector.log(logable, AuditLogType.UPDATE_OVF_FOR_STORAGE_POOL_FAILED);
    }
}
