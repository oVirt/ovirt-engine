package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;

public class ScanStorageForUnregisteredDisksCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;

    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;

    @Inject
    private DiskImageDao diskImageDao;

    private List<UnregisteredDisk> unregisteredDisks = new ArrayList<>();

    public ScanStorageForUnregisteredDisksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        init(parameters);
    }

    public ScanStorageForUnregisteredDisksCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected boolean validate() {
        boolean returnValue = checkStoragePool()
                && checkStoragePoolStatusNotEqual(StoragePoolStatus.Uninitialized,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL)
                && checkStorageDomain()
                && checkStorageDomainStatus(StorageDomainStatus.Active)
                && checkForActiveVds() != null;
        return returnValue;
    }

    @Override
    protected void executeCommand() {
        // Get all disks from the Storage.
        VdcQueryReturnValue vdcRetVal = getUnregisteredDisksFromHost();
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
                getUnregisteredOVFDataDao().getAllForStorageDomainByEntityType(getParameters().getStorageDomainId(),
                        null);
        setVmsForUnregisteredDisks(allEntities);

        // Initialize the unregistered Disks table - Remove all the data related to the Storage Domain.
        removeUnregisteredDisks();

        // Initialize all the disks in the DB.
        initUnregisteredDisksToDB();
        setSucceeded(true);
    }

    protected void removeUnregisteredDisks() {
        getUnregisteredDisksDao().removeUnregisteredDisk(null, getParameters().getStorageDomainId());
    }

    protected VdcQueryReturnValue getUnregisteredDisksFromHost() {
        return getBackend().runInternalQuery(VdcQueryType.GetUnregisteredDisks,
                new GetUnregisteredDisksQueryParameters(getParameters().getStorageDomainId(),
                        getParameters().getStoragePoolId()));
    }

    protected void setVmsForUnregisteredDisks(List<OvfEntityData> allEntities) {
        for (OvfEntityData ovfEntity : allEntities) {
            try {
                XmlDocument xmlDocument = new XmlDocument(ovfEntity.getOvfData());
                OvfUtils.updateUnregisteredDisksWithVMs(unregisteredDisks,
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
                UnregisteredDisk unregisteredDisk = new UnregisteredDisk(disk, new ArrayList<>());
                unregisteredDisks.add(unregisteredDisk);
            }
        }
    }

    protected void initUnregisteredDisksToDB() {
        List<DiskImage> existingDisks = getDiskImageDao().getAllForStorageDomain(getParameters().getStorageDomainId());
        for (UnregisteredDisk unregisteredDisk : unregisteredDisks) {
            if (existingDisks.stream().anyMatch(diskImage -> diskImage.getId().equals(unregisteredDisk.getId()))) {
                log.info("Disk {} with id '{}' already exists in the engine, therefore will not be " +
                                "part of the unregistered disks.",
                        unregisteredDisk.getDiskAlias(),
                        unregisteredDisk.getId());
                continue;
            }
            saveUnregisterDisk(unregisteredDisk);
            log.info("Adding unregistered disk of disk id '{}' and disk alias '{}'",
                    unregisteredDisk.getId(),
                    unregisteredDisk.getDiskAlias());
        }
    }

    protected void saveUnregisterDisk(UnregisteredDisk unregisteredDisk) {
        getUnregisteredDisksDao().saveUnregisteredDisk(unregisteredDisk);
    }

    public UnregisteredDisksDao getUnregisteredDisksDao() {
        return unregisteredDisksDao;
    }

    public UnregisteredOVFDataDao getUnregisteredOVFDataDao() {
        return unregisteredOVFDataDao;
    }

    public DiskImageDao getDiskImageDao() {
        return diskImageDao;
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
            return Collections.EMPTY_MAP;
        }
    }
}
