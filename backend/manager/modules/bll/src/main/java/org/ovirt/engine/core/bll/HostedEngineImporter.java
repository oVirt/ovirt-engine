package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Hosted Engine VM Importer.
 * This class takes a VM and imports it, including its disks and nics.
 * It is assumed that all devices are set on the VM (disks and nics).
 * It is assumed that the disks exists already on the target storage domain.
 * It will try to import the storage domain of the VM.
 */
public class HostedEngineImporter {

    private static final Logger log = LoggerFactory.getLogger(HostedEngineImporter.class);

    @EJB
    private BackendInternal backend;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private DiskProfileDao diskProfileDao;
    @Inject
    private ClusterDao clusterDAO;
    @Inject
    private VmStaticDao vmStaticDAO;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private OsRepository osRepository;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private ResourceManager resourceManager;

    /**
     * Import the VM into ovirt engine by removing the old, un-managed VM
     * and re-import it with its disks and nics.
     *
     * @param vm         the VM to import
     */
    public void doImport(VM vm) {
        StoragePool storagePool = storagePoolDao.getForCluster(vm.getClusterId());
        if (!Config.<Boolean>getValue(ConfigValues.AutoImportHostedEngine)) {
            return;
        }
        ActionReturnValue heVmImported;
        // get the special sd of hosted engine
        Guid storageDomainId = vm.getImages().get(0).getStorageIds().get(0);
        StorageDomain sd = getHEStorageDomain(vm, storagePool, storageDomainId);
        // no point in trying this without the SD
        if (sd != null
                && (sd.getStatus() == StorageDomainStatus.Active)
                && storagePool.getStatus() == StoragePoolStatus.Up) {
            log.info("Try to import the Hosted Engine VM '{}'", vm);
            if (vmStaticDAO.get(vm.getId()) == null || removedHEVM(vm)) {

                heVmImported = importHEVM(vm, sd);

                if (heVmImported.getSucceeded()) {
                    resourceManager.getVmManager(vm.getId()).update(vm.getStaticData());

                    log.info("Successfully imported the Hosted Engine VM");
                    auditLogDirector.log(new AuditLogableImpl(), AuditLogType.HOSTED_ENGINE_VM_IMPORT_SUCCEEDED);
                } else {
                    log.error("Failed importing the Hosted Engine VM");
                    auditLogDirector.log(new AuditLogableImpl(), AuditLogType.HOSTED_ENGINE_VM_IMPORT_FAILED);
                }
            }
        } else {
            if (sd == null) {
                log.debug("Skip trying to import the Hosted Engine VM. Storage Domain '{}' doesn't exist",
                        storageDomainId);
                auditLogDirector.log(new AuditLogableImpl(), AuditLogType.HOSTED_ENGINE_SD_NOT_EXIT);
            } else {
                log.debug("Skip trying to import the Hosted Engine VM. Storage Domain '{}' isn't ACTIVE", sd);
                auditLogDirector.log(new AuditLogableImpl(), AuditLogType.HOSTED_ENGINE_SD_NOT_ACTIVE);
            }
        }
    }

    private boolean removedHEVM(VM vm) {
        RemoveVmParameters parameters = new RemoveVmParameters(vm.getId(), true);
        parameters.setRemoveDisks(false);
        return backend.runInternalAction(
                ActionType.RemoveVm,
                parameters).getSucceeded();
    }

    private ActionReturnValue importHEVM(VM vm, StorageDomain sd) {
        return backend.runInternalAction(
                ActionType.ImportVm,
                createImportParams(vm, sd));
    }

    private ImportVmParameters createImportParams(VM vm, StorageDomain sd) {
        ImportVmParameters parameters = new ImportVmParameters(
                vm,
                sd.getId(),
                sd.getId(),
                sd.getStoragePoolId(),
                vm.getClusterId());
        // assumption is that there's only 1 profile for hosted engine domain. its an unmanged domain.
        Guid sdProfileId = diskProfileDao.getAllForStorageDomain(sd.getId()).get(0).getId();
        for (DiskImage image : vm.getImages()) {
            image.setDiskProfileId(sdProfileId);
            image.setStorageIds(Collections.singletonList(sd.getId()));
            image.setVmSnapshotId(Guid.newGuid());
            image.setImageStatus(ImageStatus.OK);
        }
        // disks are there already(the vm is running)
        parameters.setImagesExistOnTargetStorageDomain(true);
        // distinguish from "regular" he vm.
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        // architecture is a missing attribute from vdsm structure. relying on the cluster is perfectly reliable.
        Cluster cluster = clusterDAO.get(vm.getClusterId());
        vm.setClusterArch(cluster.getArchitecture());
        vm.setVmCreationDate(new Date());
        vm.setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        osRepository.getLinuxOss().stream().sorted().findFirst().ifPresent(vm::setVmOs);
        vm.setPriority(1);

        vmHandler.updateDefaultTimeZone(vm.getStaticData());

        return parameters;
    }

    private StorageDomain getHEStorageDomain(VM vm, StoragePool storagePool, Guid storageDomainId) {
        StorageDomain storageDomain = storageDomainDao.getForStoragePool(storageDomainId, storagePool.getId());
        if(storageDomain != null){
            return  storageDomain;
        }
        StorageDomainManagementParameter importParams = new StorageDomainManagementParameter();
        importParams.setStorageDomainId(storageDomainId);
        importParams.setVdsId(vm.getRunOnVds());
        return backend.runInternalAction(
                    ActionType.ImportHostedEngineStorageDomain,
                    importParams).getActionReturnValue();
    }
}
