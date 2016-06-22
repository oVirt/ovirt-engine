package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
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
    private VdsGroupDao vdsGroupDAO;
    @Inject
    private VmStaticDao vmStaticDAO;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private OsRepository osRepository;

    /**
     * Import the VM into ovirt engine by removing the old, un-managed VM
     * and re-import it with its disks and nics.
     *
     * @param vm         the VM to import
     */
    public void doImport(VM vm) {
        StoragePool storagePool = storagePoolDao.getForVdsGroup(vm.getVdsGroupId());
        VDSGroup vdsGroup = vdsGroupDAO.get(vm.getVdsGroupId());
        if (!importSupported(storagePool.getCompatibilityVersion(), vdsGroup.getCompatibilityVersion())) {
            return;
        }
        VdcReturnValueBase heVmImported;
        // get the special sd of hosted engine
        StorageDomain sd = getHEStorageDomain(vm);
        // no point in trying this without the SD
        if (sd != null
                && (sd.getStatus() == StorageDomainStatus.Active)
                && storagePool.getStatus() == StoragePoolStatus.Up) {
            log.info("Try to import the Hosted Engine VM '{}'", vm);
            if (vmStaticDAO.get(vm.getId()) == null || removedHEVM(vm)) {

                heVmImported = importHEVM(vm, sd);

                if (heVmImported.getSucceeded()) {
                    log.info("Successfully imported the Hosted Engine VM");
                    auditLogDirector.log(new AuditLogableBase(), AuditLogType.HOSTED_ENGINE_VM_IMPORT_SUCCEEDED);
                } else {
                    log.error("Failed importing the Hosted Engine VM");
                    auditLogDirector.log(new AuditLogableBase(), AuditLogType.HOSTED_ENGINE_VM_IMPORT_FAILED);
                }
            }
        } else {
            if (sd == null) {
                log.debug("Skip trying to import the Hosted Engine VM. Storage Domain '{}' doesn't exist",
                        Config.<String>getValue(ConfigValues.HostedEngineStorageDomainName));
                auditLogDirector.log(new AuditLogableBase(), AuditLogType.HOSTED_ENGINE_SD_NOT_EXIT);
            } else {
                log.debug("Skip trying to import the Hosted Engine VM. Storage Domain '{}' isn't ACTIVE", sd);
                auditLogDirector.log(new AuditLogableBase(), AuditLogType.HOSTED_ENGINE_SD_NOT_ACTIVE);
            }
        }

    }

    private boolean importSupported(Version spVersion, Version vdsGroupVersion) {
        return spVersion.greaterOrEquals(Version.v3_5)
                && vdsGroupVersion.greaterOrEquals(Version.v3_6)
                && Config.<Boolean> getValue(ConfigValues.AutoImportHostedEngine);
    }

    private boolean removedHEVM(VM vm) {
        RemoveVmParameters parameters = new RemoveVmParameters(vm.getId(), true);
        parameters.setRemoveDisks(false);
        return backend.runInternalAction(
                VdcActionType.RemoveVm,
                parameters).getSucceeded();
    }

    private VdcReturnValueBase importHEVM(VM vm, StorageDomain sd) {
        return backend.runInternalAction(
                VdcActionType.ImportVm,
                createImportParams(vm, sd));
    }

    private ImportVmParameters createImportParams(VM vm, StorageDomain sd) {
        ImportVmParameters parameters = new ImportVmParameters(
                vm,
                sd.getId(),
                sd.getId(),
                sd.getStoragePoolId(),
                vm.getVdsGroupId());
        // assumption is that there's only 1 profile for hosted engine domain. its an unmanged domain.
        Guid sdProfileId = diskProfileDao.getAllForStorageDomain(sd.getId()).get(0).getId();
        for (DiskImage image : vm.getImages()) {
            image.setDiskProfileId(sdProfileId);
            image.setStorageIds(new ArrayList(Arrays.asList(sd.getId())));
            image.setVmSnapshotId(Guid.newGuid());
            image.setImageStatus(ImageStatus.OK);
        }
        // disks are there already(the vm is running)
        parameters.setImagesExistOnTargetStorageDomain(true);
        // distinguish from "regular" he vm.
        vm.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        // architecture is a missing attribute from vdsm structure. relying on the cluster is perfectly reliable.
        VDSGroup vdsGroup = vdsGroupDAO.get(vm.getVdsGroupId());
        vm.setClusterArch(vdsGroup.getArchitecture());
        vm.setVmCreationDate(new Date());
        vm.setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        ArrayList<Integer> linuxOss = osRepository.getLinuxOss();
        Collections.sort(linuxOss);
        vm.setVmOs(linuxOss.get(0));

        vm.setPriority(1);
        VmHandler.updateDefaultTimeZone(vm.getStaticData());

        return parameters;
    }

    private StorageDomain getHEStorageDomain(VM vm) {
        ArrayList<StorageDomain> searchResult =
                backend.runInternalQuery(
                        VdcQueryType.Search,
                        new SearchParameters(
                                "Storage: name=" + Config.<String>getValue(ConfigValues.HostedEngineStorageDomainName),
                                SearchType.StorageDomain
                        )
                ).getReturnValue();
        if (searchResult != null && !searchResult.isEmpty()) {
            return searchResult.get(0);
        } else {
            StorageDomainManagementParameter importParams = new StorageDomainManagementParameter();
            importParams.setVdsId(vm.getRunOnVds());
            return backend.runInternalAction(
                    VdcActionType.ImportHostedEngineStorageDomain,
                    importParams).getActionReturnValue();
        }
    }

}
