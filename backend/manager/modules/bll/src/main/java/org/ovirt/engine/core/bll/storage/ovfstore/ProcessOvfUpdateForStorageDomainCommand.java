package org.ovirt.engine.core.bll.storage.ovfstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.UploadStreamParameters;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateOvfVolumeForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateParameters;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateParameters.OvfUpdateStep;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.archivers.tar.InMemoryTar;
import org.ovirt.engine.core.utils.ovf.OvfInfoFileConstants;

@NonTransactiveCommandAttribute
public class ProcessOvfUpdateForStorageDomainCommand<T extends ProcessOvfUpdateParameters> extends StorageDomainCommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VmAndTemplatesGenerationsDao vmAndTemplatesGenerationsDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StorageDomainOvfInfoDao storageDomainOvfInfoDao;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    private LinkedList<Pair<StorageDomainOvfInfo, DiskImage>> domainOvfStoresInfoForUpdate = new LinkedList<>();
    private int ovfDiskCount;
    private String postUpdateDescription;
    private Date updateDate;
    private List<Guid> failedOvfDisks;

    public ProcessOvfUpdateForStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public ProcessOvfUpdateForStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        super.init();
        setStorageDomainId(getParameters().getStorageDomainId());

        Guid poolId = getParameters().getStoragePoolId();
        Guid storageDomainId = getParameters().getStorageDomainId();
        if (poolId == null || poolId.equals(Guid.Empty)) {
            List<StoragePool> pool = storagePoolDao.getAllForStorageDomain(storageDomainId);
            if (!pool.isEmpty()) {
                getParameters().setStoragePoolId(pool.get(0).getId());
            }
        }
        setStoragePoolId(getParameters().getStoragePoolId());

        populateStorageDomainOvfData();
    }


    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected boolean validate() {
        if (!getParameters().isSkipDomainChecks()) {
            StorageDomainValidator storageDomainValidator = new StorageDomainValidator(getStorageDomain());
            return validate(storageDomainValidator.isDomainExistAndActive()) &&
                    validate(storageDomainValidator.isDataDomain());
        }
        return true;
    }

    private void populateStorageDomainOvfData() {
        List<StorageDomainOvfInfo> storageDomainOvfInfos =
                storageDomainOvfInfoDao.getAllForDomain(getStorageDomainId());
        ovfDiskCount = storageDomainOvfInfos.size();
        storageDomainOvfInfos.sort(OVF_INFO_COMPARATOR);

        for (StorageDomainOvfInfo storageDomainOvfInfo : storageDomainOvfInfos) {
            if (storageDomainOvfInfo.getStatus() != StorageDomainOvfInfoStatus.DISABLED) {
                DiskImage ovfDisk = (DiskImage) diskDao.get(storageDomainOvfInfo.getOvfDiskId());
                domainOvfStoresInfoForUpdate.add(new Pair<>(storageDomainOvfInfo, ovfDisk));
            }
        }
    }

    private String getPostUpdateOvfStoreDescription(long size) {
        if (postUpdateDescription == null) {
            postUpdateDescription = generateOvfStoreDescription(updateDate, true, size);
        }

        return postUpdateDescription;
    }

    private String generateOvfStoreDescription(Date updateDate, boolean isUpdated, Long size) {
        Map<String, Object> description = new HashMap<>();
        description.put(OvfInfoFileConstants.DiskDescription, OvfInfoFileConstants.OvfStoreDescriptionLabel);
        description.put(OvfInfoFileConstants.Domains, Collections.singletonList(getParameters().getStorageDomainId()));
        description.put(OvfInfoFileConstants.IsUpdated, isUpdated);
        description.put(OvfInfoFileConstants.LastUpdated, updateDate != null ? updateDate.toString() : null);
        if (size != null) {
            description.put(OvfInfoFileConstants.Size, size);
        }
        return buildJson(description, false);
    }

    private String generateInfoFileData() {
        Map<String, Object> data = new HashMap<>();
        data.put(OvfInfoFileConstants.LastUpdated, updateDate.toString());
        data.put(OvfInfoFileConstants.Domains, Collections.singletonList(getParameters().getStorageDomainId()));
        return buildJson(data, true);
    }

    private Map<String, Object> generateMetaDataFile(List<Guid> vmAndTemplatesIds) {
        Map<String, Object> data = new HashMap<>();
        addVmsStatus(vmAndTemplatesIds, data);
        return data;
    }

    private void addVmsStatus(List<Guid> vmAndTemplatesIds, Map<String, Object> data) {
        Map<String, Object> vmsStatus = new HashMap<>();
        for (Guid vmId : vmAndTemplatesIds) {
            VmDynamic vmDynamic = vmDynamicDao.get(vmId);
            if (vmDynamic != null && vmDynamic.getStatus() != VMStatus.Down) {
                vmsStatus.put(vmId.toString(), vmDynamic.getStatus().getValue());
                log.debug("OVF_STORE - Add vm id '{}' with status: '{}'",
                        vmId,
                        vmDynamic.getStatus());
            } else {
                log.debug("OVF_STORE - Skip entity id '{}' with status: '{}'",
                        vmId,
                        vmDynamic != null ? vmDynamic.getStatus() : "N/A");
            }
        }
        data.put(OvfInfoFileConstants.VmStatus, vmsStatus);
    }

    private String buildJson(Map<String, Object> map, boolean prettyPrint) {
        try {
            return JsonHelper.mapToJson(map, prettyPrint);
        } catch (IOException e) {
            throw new RuntimeException("Exception while generating json containing ovf store info", e);
        }
    }

    private byte[] buildOvfInfoFileByteArray(List<Guid> vmAndTemplatesIds) {
        ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream();
        Set<Guid> processedIds = new HashSet<>();

        try (InMemoryTar inMemoryTar = new InMemoryTar(bufferedOutputStream)) {
            inMemoryTar.addTarEntry(generateInfoFileData().getBytes(),
                    OvfInfoFileConstants.InfoFileName);
            Map<String, Object> metaDataForEntities = generateMetaDataFile(vmAndTemplatesIds);
            int i = 0;
            while (i < vmAndTemplatesIds.size()) {
                int size =
                        Math.min(StorageConstants.OVF_MAX_ITEMS_PER_SQL_STATEMENT, vmAndTemplatesIds.size() - i);
                List<Guid> idsToProcess = vmAndTemplatesIds.subList(i, i + size);
                i += size;

                List<Pair<Guid, String>> ovfs = vmAndTemplatesGenerationsDao.loadOvfDataForIds(idsToProcess);
                if (!ovfs.isEmpty()) {
                    processedIds.addAll(buildFilesForOvfs(ovfs, inMemoryTar));
                }
            }

            List<Pair<Guid, String>> unprocessedOvfData = retrieveUnprocessedUnregisteredOvfData(processedIds, metaDataForEntities);
            inMemoryTar.addTarEntry(buildJson(metaDataForEntities, true).getBytes(), OvfInfoFileConstants.MetaDataFileName);
            buildFilesForOvfs(unprocessedOvfData, inMemoryTar);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception while building in memory tar of the OVFs of domain %s",
                    getParameters().getStorageDomainId()), e);
        }

        return bufferedOutputStream.toByteArray();
    }

    private List<Pair<Guid, String>> retrieveUnprocessedUnregisteredOvfData(Set<Guid> processedIds,
            Map<String, Object> metaDataForEntities) {
        Map<String, Object> statusMap = (Map<String, Object>) metaDataForEntities.get(OvfInfoFileConstants.VmStatus);
        List<OvfEntityData> ovfList = unregisteredOVFDataDao.getAllForStorageDomainByEntityType(
                getParameters().getStorageDomainId(), null);
        List<Pair<Guid, String>> ovfData = new LinkedList<>();
        for (OvfEntityData ovfEntityData : ovfList) {
            if (!processedIds.contains(ovfEntityData.getEntityId())) {
                Pair<Guid, String> data = new Pair<>(ovfEntityData.getEntityId(), ovfEntityData.getOvfData());
                ovfData.add(data);
            }
            statusMap.putIfAbsent(ovfEntityData.getEntityId().toString(), ovfEntityData.getStatus().getValue());
        }
        return ovfData;
    }

    protected boolean updateOvfStoreContent() {
        if (domainOvfStoresInfoForUpdate.isEmpty()) {
            return true;
        }

        log.debug("Updating OVF stores: {}", domainOvfStoresInfoForUpdate);

        updateDate = new Date();

        List<Guid> vmAndTemplatesIds =
                storageDomainDao.getVmAndTemplatesIdsByStorageDomainId(getParameters().getStorageDomainId(),
                        false,
                        false);

        vmAndTemplatesIds.addAll(vmStaticDao.getVmAndTemplatesIdsWithoutAttachedImageDisks(getParameters().getStoragePoolId(), false));

        byte[] bytes = buildOvfInfoFileByteArray(vmAndTemplatesIds);

        Pair<StorageDomainOvfInfo, DiskImage> lastOvfStoreForUpdate = domainOvfStoresInfoForUpdate.getLast();

        // means that the last ovf store was never updated, if it was - we don't want to update
        // it within the loop unless some other ovf store was updated successfully (we use it as best effort backup so
        // we'll
        // possibly have some ovf data on storage)
        if (lastOvfStoreForUpdate.getFirst().getLastUpdated() != null) {
            log.debug("Removing last OVF store from list: {}", lastOvfStoreForUpdate);
            domainOvfStoresInfoForUpdate.removeLast();
        } else {
            lastOvfStoreForUpdate = null;
        }

        boolean shouldUpdateLastOvfStore = false;
        failedOvfDisks = new ArrayList<>();

        for (Pair<StorageDomainOvfInfo, DiskImage> pair : domainOvfStoresInfoForUpdate) {
            shouldUpdateLastOvfStore |=
                    performOvfUpdateForDomain(bytes,
                            pair.getFirst(),
                            pair.getSecond(),
                            vmAndTemplatesIds);
        }

        // if we successfully updated any ovf store, we can attempt to also update the one we kept for best effort
        // backup (if we did)
        if (shouldUpdateLastOvfStore && lastOvfStoreForUpdate != null) {
            performOvfUpdateForDomain(bytes,
                    lastOvfStoreForUpdate.getFirst(),
                    lastOvfStoreForUpdate.getSecond(),
                    vmAndTemplatesIds);
        }

        if (!failedOvfDisks.isEmpty()) {
            addCustomValue("DataCenterName", getStoragePool().getName());
            addCustomValue("StorageDomainName", getStorageDomain().getName());
            addCustomValue("DisksIds", StringUtils.join(failedOvfDisks, ", "));
            auditLogDirector.log(this, AuditLogType.UPDATE_FOR_OVF_STORES_FAILED);
            return false;
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE_OVFS);
    }

    private void setOvfVolumeDescription(Guid storagePoolId,
            Guid storageDomainId,
            Guid diskId,
            Guid volumeId,
            String description) {

        SetVolumeDescriptionVDSCommandParameters vdsCommandParameters =
                new SetVolumeDescriptionVDSCommandParameters(storagePoolId, storageDomainId,
                        diskId, volumeId, description);
        runVdsCommand(VDSCommandType.SetVolumeDescription, vdsCommandParameters);
    }

    private boolean performOvfUpdateForDomain(byte[] ovfData,
            StorageDomainOvfInfo storageDomainOvfInfo,
            DiskImage ovfDisk,
            List<Guid> vmAndTemplatesIds) {
        Guid storagePoolId = ovfDisk.getStoragePoolId();
        Guid storageDomainId = ovfDisk.getStorageIds().get(0);
        Guid diskId = ovfDisk.getId();
        Guid volumeId = ovfDisk.getImageId();

        storageDomainOvfInfo.setStoredOvfIds(null);

        try {
            setOvfVolumeDescription(storagePoolId,
                    storageDomainId,
                    diskId,
                    volumeId,
                    generateOvfStoreDescription(storageDomainOvfInfo.getLastUpdated(), false, null));

            storageDomainOvfInfoDao.update(storageDomainOvfInfo);

            ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(ovfData);

            Long size = Long.valueOf(ovfData.length);
            UploadStreamParameters uploadStreamParameters =
                    new UploadStreamParameters(storagePoolId, storageDomainId,
                            diskId, volumeId, byteArrayInputStream,
                            size);

            uploadStreamParameters.setParentCommand(getActionType());
            uploadStreamParameters.setParentParameters(getParameters());
            uploadStreamParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            ActionReturnValue actionReturnValue =
                    runInternalActionWithTasksContext(ActionType.UploadStream, uploadStreamParameters);
            if (actionReturnValue.getSucceeded()) {
                storageDomainOvfInfo.setStatus(StorageDomainOvfInfoStatus.UPDATED);
                storageDomainOvfInfo.setStoredOvfIds(vmAndTemplatesIds);
                storageDomainOvfInfo.setLastUpdated(updateDate);
                setOvfVolumeDescription(storagePoolId, storageDomainId,
                        diskId, volumeId, getPostUpdateOvfStoreDescription(size));
                storageDomainOvfInfoDao.update(storageDomainOvfInfo);
                ovfDisk.setLastModified(updateDate);
                if (storageDomainDao.get(storageDomainId).getStorageType().isFileDomain()) {
                    ovfDisk.setSize(size);
                    ovfDisk.setActualSize(size);
                    DiskImageDynamic destinationDiskDynamic = diskImageDynamicDao.get(ovfDisk.getImageId());
                    if (destinationDiskDynamic != null) {
                        destinationDiskDynamic.setActualSize(size);
                        diskImageDynamicDao.update(destinationDiskDynamic);
                    }
                }
                imageDao.update(ovfDisk.getImage());
                return true;
            }
        } catch (EngineException e) {
            log.warn("failed to update domain '{}' ovf store disk '{}'", storageDomainId, diskId);
        }

        failedOvfDisks.add(diskId);
        return false;
    }

    private int getMissingDiskCount() {
        return Config.<Integer>getValue(ConfigValues.StorageDomainOvfStoreCount) - ovfDiskCount;
    }

    public boolean createOvfStoreDisks(int missingDiskCount) {
        boolean allOvfStoreDisksCreated = true;
        for (int i = 0; i < missingDiskCount; i++) {
            CreateOvfVolumeForStorageDomainCommandParameters parameters = createCreateOvfVolumeForStorageDomainParams();
            ActionReturnValue returnValue = runInternalAction(ActionType.CreateOvfVolumeForStorageDomain,
                    parameters,
                    getContext().clone().withoutLock());
            if (!returnValue.getSucceeded()) {
                allOvfStoreDisksCreated = false;
            }
        }
        return allOvfStoreDisksCreated;
    }

    @Override
    public boolean ignoreChildCommandFailure() {
        return true;
    }

    public CreateOvfVolumeForStorageDomainCommandParameters createCreateOvfVolumeForStorageDomainParams() {
        CreateOvfVolumeForStorageDomainCommandParameters parameters = new CreateOvfVolumeForStorageDomainCommandParameters(getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId());
        parameters.setSkipDomainChecks(getParameters().isSkipDomainChecks());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getOvfUpdateStep() == OvfUpdateStep.OVF_STORES_CREATION) {
            setOvfUpdateStep(OvfUpdateStep.OVF_UPLOAD);
            if (!updateOvfStoreContent()) {
                setSucceeded(false);
                log.error("Failed to update OVF_STORE content");
                throw new RuntimeException();
            }
            return true;
        }

        return false;
    }

    private void setOvfUpdateStep(OvfUpdateStep step){
        getParameters().setOvfUpdateStep(step);
        persistCommand(getParameters().getParentCommand(), true);
    }

    @Override
    protected void executeCommand() {
        int missingDiskCount = getMissingDiskCount();
        if (missingDiskCount <= 0) {
            setOvfUpdateStep(OvfUpdateStep.OVF_UPLOAD);
            setSucceeded(updateOvfStoreContent());
        } else {
            setOvfUpdateStep(OvfUpdateStep.OVF_STORES_CREATION);
            setSucceeded(createOvfStoreDisks(getMissingDiskCount()));
        }
    }

    protected Set<Guid> buildFilesForOvfs(List<Pair<Guid, String>> ovfs, InMemoryTar inMemoryTar) throws Exception {
        Set<Guid> addedOvfIds = new HashSet<>();
        for (Pair<Guid, String> pair : ovfs) {
            if (pair.getSecond() != null) {
                inMemoryTar.addTarEntry(pair.getSecond().getBytes(), pair.getFirst() + ".ovf");
                addedOvfIds.add(pair.getFirst());
            }
        }
        return addedOvfIds;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getParameters().isSkipDomainChecks() ? Collections.emptyMap() :
                Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                                EngineMessage.ACTION_TYPE_FAILED_DOMAIN_OVF_ON_UPDATE));
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.OVF_UPDATE,
                        EngineMessage.ACTION_TYPE_FAILED_DOMAIN_OVF_ON_UPDATE));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        this.addCustomValue("StorageDomainName", getStorageDomain().getName());
        this.setUserName(getUserName());
        if (getActionState() == CommandActionState.EXECUTE) {
            if (!getSucceeded()) {
                return AuditLogType.UPDATE_OVF_FOR_STORAGE_DOMAIN_FAILED;
            }
        } else if (getActionState() == CommandActionState.END_SUCCESS &&
                !SYSTEM_USER_NAME.equals(this.getUserName())) {
            return AuditLogType.USER_UPDATE_OVF_STORE;
        }
        return super.getAuditLogTypeValue();
    }

    /**
     * Ordering to provide consistent ovf update order - the order should be as follows, so that in case
     * of failure we will have "previous" version of the data on other disk.
     * 1. disks that were never ovf updated (getLastUpdated is null)
     * 2. disk id
     */
    public static final Comparator<StorageDomainOvfInfo> OVF_INFO_COMPARATOR =
            Comparator.comparing(StorageDomainOvfInfo::getLastUpdated,
                    Comparator.nullsFirst(Comparator.naturalOrder())).
                    thenComparing(StorageDomainOvfInfo::getOvfDiskId);
}
