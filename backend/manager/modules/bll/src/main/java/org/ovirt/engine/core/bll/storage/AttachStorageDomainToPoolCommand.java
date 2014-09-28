package org.ovirt.engine.core.bll.storage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RetrieveImageDataParameters;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.AttachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.ovf.OvfInfoFileConstants;
import org.ovirt.engine.core.utils.ovf.OvfParser;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AttachStorageDomainToPoolCommand<T extends AttachStorageDomainToPoolParameters> extends
        StorageDomainCommandBase<T> {
    private StoragePoolIsoMap map;
    private List<DiskImage> ovfDisks;

    public AttachStorageDomainToPoolCommand(T parameters) {
        this(parameters, null);
    }

    public AttachStorageDomainToPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected AttachStorageDomainToPoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        if (getStorageDomain() != null) {
            if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
                StoragePoolWithStoragesParameter parameters = new StoragePoolWithStoragesParameter(getStoragePool(),
                       Arrays.asList(getStorageDomain().getId()),
                        getParameters().getSessionId());
                parameters.setIsInternal(true);
                parameters.setTransactionScopeOption(TransactionScopeOption.Suppress);

                VdcReturnValueBase returnValue = runInternalAction(
                        VdcActionType.AddStoragePoolWithStorages,
                        parameters);
                setSucceeded(returnValue.getSucceeded());
                if (!returnValue.getSucceeded()) {
                    getReturnValue().setFault(returnValue.getFault());
                }
            } else {
                map = getStoragePoolIsoMapDAO().get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getParameters().getStoragePoolId()));
                if (map == null) {
                    executeInNewTransaction(new TransactionMethod<Object>() {

                        @Override
                        public Object runInTransaction() {
                            map = new StoragePoolIsoMap(getStorageDomain().getId(), getParameters()
                                    .getStoragePoolId(), StorageDomainStatus.Locked);
                            getStoragePoolIsoMapDAO().save(map);
                            getCompensationContext().snapshotNewEntity(map);
                            getCompensationContext().stateChanged();
                            return null;
                        }
                    });
                    connectHostsInUpToDomainStorageServer();

                    // Forcibly detach only data storage domains.
                    if (getStorageDomain().getStorageDomainType() == StorageDomainType.Data) {
                        @SuppressWarnings("unchecked")
                        Pair<StorageDomainStatic, Guid> domainFromIrs =
                                (Pair<StorageDomainStatic, Guid>) runVdsCommand(
                                        VDSCommandType.HSMGetStorageDomainInfo,
                                        new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(),
                                                getParameters().getStorageDomainId())
                                ).getReturnValue();
                        // If the storage domain is already related to another Storage Pool, detach it by force.
                        Guid storagePoolId = domainFromIrs.getSecond();
                        if (storagePoolId != null) {
                            if (FeatureSupported.importDataStorageDomain(getStoragePool().getcompatibility_version())) {
                                // Master domain version is not relevant since force remove at
                                // DetachStorageDomainVdsCommand does not use it.
                                // Storage pool id can be empty
                                DetachStorageDomainVDSCommandParameters detachParams =
                                        new DetachStorageDomainVDSCommandParameters(getVds().getStoragePoolId(),
                                                getParameters().getStorageDomainId(),
                                                Guid.Empty,
                                                0);
                                detachParams.setForce(true);
                                detachParams.setDetachFromOldStoragePool(true);
                                VDSReturnValue returnValue =
                                        runVdsCommand(VDSCommandType.DetachStorageDomain, detachParams);
                                if (!returnValue.getSucceeded()) {
                                    log.warnFormat("Detaching Storage Domain {0} from it's previous storage pool {1} has failed. "
                                            +
                                            "The meta data of the Storage Domain might still indicate that it is attached to a different Storage Pool.",
                                            getParameters().getStorageDomainId(),
                                            Guid.Empty,
                                            0);
                                    throw new VdcBLLException(
                                            returnValue.getVdsError() != null ? returnValue.getVdsError().getCode()
                                                    : VdcBllErrors.ENGINE,
                                            returnValue.getExceptionString());
                                }
                            }
                        }
                        createDefaultDiskProfile();
                    }

                    runVdsCommand(VDSCommandType.AttachStorageDomain,
                            new AttachStorageDomainVDSCommandParameters(getParameters().getStoragePoolId(),
                                    getParameters().getStorageDomainId()));
                    final List<OvfEntityData> unregisteredEntitiesFromOvfDisk = getEntitiesFromStorageOvfDisk();
                    executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            final StorageDomainType sdType = getStorageDomain().getStorageDomainType();
                            map.setStatus(StorageDomainStatus.Maintenance);
                            getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getStatus());

                            if (sdType == StorageDomainType.Master) {
                                calcStoragePoolStatusByDomainsStatus();
                            }

                            // upgrade the domain format to the storage pool format
                            if (sdType == StorageDomainType.Data || sdType == StorageDomainType.Master) {
                                updateStorageDomainFormat(getStorageDomain());
                            }
                            registerAllOvfDisks(getAllOVFDisks());
                            // Update unregistered entities
                            for (OvfEntityData ovf : unregisteredEntitiesFromOvfDisk) {
                                getUnregisteredOVFDataDao().removeEntity(ovf.getEntityId(),
                                        getParameters().getStorageDomainId());
                                getUnregisteredOVFDataDao().saveOVFData(ovf);
                                log.infoFormat("Adding OVF data of entity id {0} and entity name {1}",
                                        ovf.getEntityId(),
                                        ovf.getEntityName());
                            }
                            return null;
                        }
                    });

                    if (getParameters().getActivate()) {
                        attemptToActivateDomain();
                    }
                    setSucceeded(true);
                }
            }
        }
    }

    /**
     * Creating default disk profile for existing storage domain.
     */
    private void createDefaultDiskProfile() {
        if (FeatureSupported.storageQoS(getStoragePool().getcompatibility_version())
                && getDiskProfileDao().getAllForStorageDomain(getStorageDomain().getId()).isEmpty()) {
            final DiskProfile diskProfile =
                    DiskProfileHelper.createDiskProfile(getStorageDomain().getId(),
                            getStorageDomainName());
            executeInNewTransaction(new TransactionMethod<Object>() {
                @Override
                public Void runInTransaction() {
                    getDiskProfileDao().save(diskProfile);
                    getCompensationContext().snapshotNewEntity(diskProfile);
                    getCompensationContext().stateChanged();
                    return null;
                }
            });
        }
    }

    protected List<OvfEntityData> getEntitiesFromStorageOvfDisk() {
        // Initialize a new ArrayList with all the ovfDisks in the specified Storage Domain,
        // so the entities can be removed from the list every time we register the latest OVF disk and we can keep the
        // ovfDisks cache list updated.
        List<DiskImage> ovfStoreDiskImages = new ArrayList(getAllOVFDisks());
        if (!ovfStoreDiskImages.isEmpty()) {
            while (!ovfStoreDiskImages.isEmpty()) {
                Pair<DiskImage, Long> ovfDiskAndSize = getLatestOVFDisk(ovfStoreDiskImages);
                DiskImage ovfDisk = ovfDiskAndSize.getFirst();
                if (ovfDisk != null) {
                    try {
                        VdcReturnValueBase vdcReturnValue = runInternalAction(VdcActionType.RetrieveImageData,
                                new RetrieveImageDataParameters(getParameters().getStoragePoolId(),
                                        getParameters().getStorageDomainId(),
                                        ovfDisk.getId(),
                                        ovfDisk.getImage().getId(),
                                        ovfDiskAndSize.getSecond()), cloneContextAndDetachFromParent());

                        getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
                        if (vdcReturnValue.getSucceeded()) {
                            return OvfUtils.getOvfEntities((byte[]) vdcReturnValue.getActionReturnValue(),
                                    getParameters().getStorageDomainId());
                        } else {
                            log.errorFormat("Image data could not be retrieved for disk id {0} in storage domain id {1}",
                                    ovfDisk.getId(),
                                    getParameters().getStorageDomainId());
                        }
                    } catch (RuntimeException e) {
                        // We are catching RuntimeException, since the call for OvfUtils.getOvfEntities will throw
                        // a RuntimeException if there is a problem to untar the file.
                        log.errorFormat("Image data could not be retrieved for disk id {0} in storage domain id {1}. Error: {2}",
                                ovfDisk.getId(),
                                getParameters().getStorageDomainId(),
                                e);
                    }
                    ovfStoreDiskImages.remove(ovfDisk);
                }
            }
            AuditLogDirector.log(this, AuditLogType.RETRIEVE_OVF_STORE_FAILED);
        } else {
            log.warnFormat("There are no OVF_STORE disks on storage domain id {0}",
                    getParameters().getStorageDomainId());
        }
        return Collections.emptyList();
    }

    /**
     * Register all the OVF_STORE disks as floating disks in the engine.
     */
    private void addOvfStoreDiskToDomain(DiskImage ovfDisk) {
        // Setting OVF_STORE disk to be outdated so it will be updated.
        StorageDomainOvfInfo storageDomainOvfInfo =
                new StorageDomainOvfInfo(getStorageDomainId(),
                        null,
                        ovfDisk.getId(),
                        StorageDomainOvfInfoStatus.OUTDATED,
                        null);
        getDbFacade().getStorageDomainOvfInfoDao().save(storageDomainOvfInfo);
    }

    private void registerAllOvfDisks(List<DiskImage> ovfStoreDiskImages) {
        for (DiskImage ovfStoreDiskImage : ovfStoreDiskImages) {
            ovfStoreDiskImage.setDiskAlias(OvfInfoFileConstants.OvfStoreDescriptionLabel);
            ovfStoreDiskImage.setDiskDescription(OvfInfoFileConstants.OvfStoreDescriptionLabel);
            ovfStoreDiskImage.setShareable(true);
            RegisterDiskParameters registerDiskParams =
                    new RegisterDiskParameters(ovfStoreDiskImage, getParameters().getStorageDomainId());
            String result = "succeeded";
            if (!runInternalAction(VdcActionType.RegisterDisk, registerDiskParams, cloneContext()).getSucceeded()) {
                result = "failed";
            }
            log.infoFormat("Register new floating OVF_STORE disk with disk id {0} for storage domain {1} has {2}",
                    ovfStoreDiskImage.getId(),
                    getParameters().getStorageDomainId(),
                    result);
            addOvfStoreDiskToDomain(ovfStoreDiskImage);
        }
    }

    protected List<DiskImage> getAllOVFDisks() {
        if (ovfDisks == null) {
            ovfDisks = new ArrayList<>();

            // Get all unregistered disks.
            List<Disk> unregisteredDisks = getBackend().runInternalQuery(VdcQueryType.GetUnregisteredDisks,
                    new GetUnregisteredDisksQueryParameters(getParameters().getStorageDomainId(),
                            getVds().getStoragePoolId())).getReturnValue();
            for (Disk disk : unregisteredDisks) {
                DiskImage ovfStoreDisk = (DiskImage) disk;
                String diskDecription = ovfStoreDisk.getDescription();
                if (diskDecription.contains(OvfInfoFileConstants.OvfStoreDescriptionLabel)) {
                    Map<String, Object> diskDescriptionMap;
                    try {
                        diskDescriptionMap = JsonHelper.jsonToMap(diskDecription);
                    } catch (IOException e) {
                        log.warnFormat("Exception while generating json containing ovf store info. Exception: {0}", e);
                        continue;
                    }

                    // The purpose of this check is to verify that it's an OVF store with data related to the Storage
                    // Domain.
                    if (!isDomainExistsInDiskDescription(diskDescriptionMap, getParameters().getStorageDomainId())) {
                        log.warnFormat("The disk description does not contain the storage domain id {0}",
                                getParameters().getStorageDomainId());
                        continue;
                    }
                    ovfDisks.add(ovfStoreDisk);
                }
            }
        }
        return ovfDisks;
    }

    /**
     * Returns the best match for OVF disk from all the disks. If no OVF disk was found, it returns null for disk and
     * size 0. If there are OVF disks, we first match the updated ones, and from them we retrieve the one which was last
     * updated.
     *
     * @param ovfStoreDiskImages
     *            - A list of OVF_STORE disks
     * @return A Pair which contains the best OVF disk to retrieve data from and its size.
     */
    private Pair<DiskImage, Long> getLatestOVFDisk(List<DiskImage> ovfStoreDiskImages) {
        Date foundOvfDiskUpdateDate = new Date();
        boolean isFoundOvfDiskUpdated = false;
        Long size = 0L;
        Disk ovfDisk = null;
        for (DiskImage ovfStoreDisk : ovfStoreDiskImages) {
            boolean isBetterOvfDiskFound = false;
            Map<String, Object> diskDescriptionMap;
            try {
                diskDescriptionMap = JsonHelper.jsonToMap(ovfStoreDisk.getDescription());
            } catch (IOException e) {
                log.warnFormat("Exception while generating json containing ovf store info. Exception: {0}", e);
                continue;
            }

            boolean isUpdated = Boolean.valueOf(diskDescriptionMap.get(OvfInfoFileConstants.IsUpdated).toString());
            Date date = getDateFromDiskDescription(diskDescriptionMap);
            if (date == null) {
                continue;
            }
            if (isFoundOvfDiskUpdated && !isUpdated) {
                continue;
            }
            if ((isUpdated && !isFoundOvfDiskUpdated) || date.after(foundOvfDiskUpdateDate)) {
                isBetterOvfDiskFound = true;
            }
            if (isBetterOvfDiskFound) {
                isFoundOvfDiskUpdated = isUpdated;
                foundOvfDiskUpdateDate = date;
                ovfDisk = ovfStoreDisk;
                size = new Long(diskDescriptionMap.get(OvfInfoFileConstants.Size).toString());
            }
        }
        return new Pair<>((DiskImage)ovfDisk, size);
    }

    private Date getDateFromDiskDescription(Map<String, Object> map) {
        try {
            Object lastUpdate = map.get(OvfInfoFileConstants.LastUpdated);
            if (lastUpdate != null) {
                return new SimpleDateFormat(OvfParser.formatStrFromDiskDescription).parse(lastUpdate.toString());
            } else {
                log.info("LastUpdate Date is not initialized in the OVF_STORE disk.");
            }
        } catch (java.text.ParseException e) {
            log.errorFormat("LastUpdate Date could not be parsed from disk desscription. Exception: {0}", e);
        }
        return null;
    }

    private boolean isDomainExistsInDiskDescription(Map<String, Object> map, Guid storageDomainId) {
        return map.get(OvfInfoFileConstants.Domains).toString().contains(storageDomainId.toString());
    }

    protected void attemptToActivateDomain() {
        StorageDomainPoolParametersBase activateParameters = new StorageDomainPoolParametersBase(getStorageDomain().getId(),
                getStoragePool().getId());
        getBackend()
                .runInternalAction(VdcActionType.ActivateStorageDomain, activateParameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL
                : AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        // We can share only ISO or Export domain , or a data domain
        // which is not attached.
        boolean returnValue =
                checkStoragePool()
                        && initializeVds() && checkStorageDomain() && checkDomainCanBeAttached(getStorageDomain());

        if (returnValue && getStoragePool().getStatus() == StoragePoolStatus.Uninitialized
                && getStorageDomain().getStorageDomainType() != StorageDomainType.Data) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_DOMAIN);
        }
        if (returnValue && getStoragePool().getStatus() != StoragePoolStatus.Uninitialized) {
            returnValue = checkMasterDomainIsUp();
        }
        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH);
    }
}
