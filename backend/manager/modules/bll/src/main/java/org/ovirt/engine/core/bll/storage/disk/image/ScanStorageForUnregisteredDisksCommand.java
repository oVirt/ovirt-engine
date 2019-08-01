package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDiskId;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;

public class ScanStorageForUnregisteredDisksCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {

    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private OvfUtils ovfUtils;

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }
    private List<UnregisteredDisk> unregisteredDisks = new ArrayList<>();

    public ScanStorageForUnregisteredDisksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public ScanStorageForUnregisteredDisksCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected boolean validate() {
        StoragePoolValidator validator = createStoragePoolValidator();
        return validate(validator.exists())
                && validate(validator.isNotInStatus(StoragePoolStatus.Uninitialized))
                && checkStorageDomain()
                && checkStorageDomainStatus(StorageDomainStatus.Active)
                && checkForActiveVds() != null
                && isSupportedByManagedBlockStorageDomain(getStorageDomain());
    }

    @Override
    protected void executeCommand() {
        // Get all disks from the Storage.
        QueryReturnValue vdcRetVal = getUnregisteredDisksFromHost();
        if (!vdcRetVal.getSucceeded()) {
            log.error("An error occurred while fetching unregistered disks from Storage Domain id '{}'",
                    getParameters().getStorageDomainId());
            setSucceeded(false);
            return;
        }
        List<DiskImage> disksFromStorage = vdcRetVal.getReturnValue();
        castDiskImagesToUnregisteredDisks(disksFromStorage);

        // Filter out all existing disks in the setup.
        List<OvfEntityData> allEntities =
                unregisteredOVFDataDao.getAllForStorageDomainByEntityType(getParameters().getStorageDomainId(),
                        null);
        setVmsForUnregisteredDisks(allEntities);

        // Initialize the unregistered Disks table - Remove all the data related to the Storage Domain.
        removeUnregisteredDisks();

        // Initialize all the disks in the DB.
        initUnregisteredDisksToDB();
        setSucceeded(true);
    }

    protected void removeUnregisteredDisks() {
        unregisteredDisksDao.removeUnregisteredDisk(null, getParameters().getStorageDomainId());
    }

    protected QueryReturnValue getUnregisteredDisksFromHost() {
        return backend.runInternalQuery(QueryType.GetUnregisteredDisks,
                new GetUnregisteredDisksQueryParameters(getParameters().getStorageDomainId(),
                        getParameters().getStoragePoolId()));
    }

    protected void setVmsForUnregisteredDisks(List<OvfEntityData> allEntities) {
        for (OvfEntityData ovfEntity : allEntities) {
            try {
                XmlDocument xmlDocument = new XmlDocument(ovfEntity.getOvfData());
                ovfUtils.updateUnregisteredDisksWithVMs(unregisteredDisks,
                        ovfEntity.getEntityId(),
                        ovfEntity.getEntityName(),
                        xmlDocument);
            } catch (Exception e) {
                log.warn("Could not parse OVF data of VM");
                continue;
            }
        }
    }

    protected void castDiskImagesToUnregisteredDisks(List<DiskImage> disksFromStorage) {
        if (disksFromStorage != null) {
            for (DiskImage disk : disksFromStorage) {
                disk.getStorageIds().set(0, getStorageDomainId());
                UnregisteredDiskId id = new UnregisteredDiskId(disk.getId(), disk.getStorageIds().get(0));
                UnregisteredDisk unregisteredDisk = new UnregisteredDisk(id, disk, new ArrayList<>());
                unregisteredDisks.add(unregisteredDisk);
            }
        }
    }

    protected void initUnregisteredDisksToDB() {
        List<DiskImage> existingDisks = diskImageDao.getAllForStorageDomain(getParameters().getStorageDomainId());
        for (UnregisteredDisk unregisteredDisk : unregisteredDisks) {
            if (existingDisks.stream().anyMatch(diskImage -> diskImage.getId().equals(unregisteredDisk.getDiskImage().getId()))) {
                log.info("Disk {} with id '{}' already exists in the engine, therefore will not be " +
                                "part of the unregistered disks.",
                        unregisteredDisk.getDiskAlias(),
                        unregisteredDisk.getDiskImage().getId());
                continue;
            }
            saveUnregisterDisk(unregisteredDisk);
            log.info("Adding unregistered disk of disk id '{}' and disk alias '{}'",
                    unregisteredDisk.getDiskImage().getId(),
                    unregisteredDisk.getDiskAlias());
        }
    }

    protected void saveUnregisterDisk(UnregisteredDisk unregisteredDisk) {
        unregisteredDisksDao.saveUnregisteredDisk(unregisteredDisk);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
        addValidationMessage(EngineMessage.VAR__ACTION__SCAN);
    }
    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_SCAN_STORAGE_DOMAIN_FOR_UNREGISTERED_DISKS
                : AuditLogType.USER_SCAN_STORAGE_DOMAIN_FOR_UNREGISTERED_DISKS_FAILED;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getParameters().getStorageDomainId() != null) {
            return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        } else {
            return Collections.emptyMap();
        }
    }
}
