package org.ovirt.engine.core.bll;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDAO;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.archivers.tar.InMemoryTar;
import org.ovirt.engine.core.utils.ovf.OvfInfoFileConstants;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
public class ProcessOvfUpdateForStorageDomainCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {
    private LinkedList<Pair<StorageDomainOvfInfo, DiskImage>> domainOvfStoresInfoForUpdate = new LinkedList<>();
    private StorageDomain storageDomain;
    private int ovfDiskCount;

    public ProcessOvfUpdateForStorageDomainCommand(T parameters) {
        super(parameters);
        setStorageDomainId(parameters.getStorageDomainId());
        setStoragePoolId(parameters.getStoragePoolId());
        populateStorageDomainOvfData();
    }

    protected ProcessOvfUpdateForStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        loadStorageDomain();
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(storageDomain);
        if (!validate(storageDomainValidator.isDomainExistAndActive())) {
            return false;
        }

        return true;
    }

    private void populateStorageDomainOvfData() {
        List<StorageDomainOvfInfo> storageDomainOvfInfos =
                getDbFacade().getStorageDomainOvfInfoDao().getAllForDomain(getStorageDomainId());
        ovfDiskCount = storageDomainOvfInfos.size();
        // Ordering to provide consistent ovf update order - the order should be as follows, so that in case
        // of failure we will have "previous" version of the data on other disk.
        // 1. disks that were never ovf updated (getLastUpdated is null)
        // 2. disk id
        Collections.sort(storageDomainOvfInfos, new Comparator<StorageDomainOvfInfo>() {
            @Override
            public int compare(StorageDomainOvfInfo storageDomainOvfInfo, StorageDomainOvfInfo storageDomainOvfInfo2) {
                int compareResult =
                        ObjectUtils.compare(storageDomainOvfInfo.getLastUpdated(),
                                storageDomainOvfInfo2.getLastUpdated());
                if (compareResult != 0) {
                    return compareResult;
                }
                return storageDomainOvfInfo.getOvfDiskId().compareTo(storageDomainOvfInfo2.getOvfDiskId());
            }
        });

        for (StorageDomainOvfInfo storageDomainOvfInfo : storageDomainOvfInfos) {
            if (storageDomainOvfInfo.getStatus() != StorageDomainOvfInfoStatus.DISABLED) {
                DiskImage ovfDisk = (DiskImage) getDbFacade().getDiskDao().get(storageDomainOvfInfo.getOvfDiskId());
                domainOvfStoresInfoForUpdate.add(new Pair(storageDomainOvfInfo, ovfDisk));
            }
        }
    }

    private void loadStorageDomain() {
        storageDomain =
                getDbFacade().getStorageDomainDao().getForStoragePool(getParameters().getStorageDomainId(),
                        getParameters().getStoragePoolId());
    }

    private Map<String, Object> buildOvfGeneralInfo(List<Guid> vmAndTemplatesIds, Date updateDate) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(OvfInfoFileConstants.LastUpdated, updateDate.toString());
        map.put(OvfInfoFileConstants.Domains, Arrays.asList(getParameters().getStorageDomainId()));
        map.put(OvfInfoFileConstants.ContainedOvfIds, vmAndTemplatesIds);
        return map;
    }

    private byte[] buildOvfInfoFileByteArray(List<Guid> vmAndTemplatesIds, Date updateDate) {
        ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream();

        try (InMemoryTar inMemoryTar = new InMemoryTar(bufferedOutputStream)) {
            inMemoryTar.addTarEntry(JsonHelper.mapToJson(buildOvfGeneralInfo(vmAndTemplatesIds, updateDate)).getBytes(),
                    "info.json");
            int i = 0;
            while (i < vmAndTemplatesIds.size()) {
                int size =
                        Math.min(StorageConstants.OVF_MAX_ITEMS_PER_SQL_STATEMENT, vmAndTemplatesIds.size() - i);
                List<Guid> idsToProcess = vmAndTemplatesIds.subList(i, i + size);
                i += size;

                List<Pair<Guid, String>> ovfs =
                        getVmAndTemplatesGenerationsDao().loadOvfDataForIds(idsToProcess);
                if (!ovfs.isEmpty()) {
                    buildFilesForOvfs(ovfs, inMemoryTar);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception while building in memory tar of the OVFs of domain %s",
                    getParameters().getStorageDomainId()), e);
        }

        return bufferedOutputStream.toByteArray();
    }

    protected void updateOvfStoreContent() {
        if (domainOvfStoresInfoForUpdate.isEmpty()) {
            return;
        }

        List<Guid> vmAndTemplatesIds =
                getStorageDomainDAO().getVmAndTemplatesIdsByStorageDomainId(getParameters().getStorageDomainId(),
                        false,
                        false);

        Date updateDate = new Date();

        byte[] bytes = buildOvfInfoFileByteArray(vmAndTemplatesIds, updateDate);

        Pair<StorageDomainOvfInfo, DiskImage> lastOvfStoreForUpdate = domainOvfStoresInfoForUpdate.getLast();

        // means that the last ovf store was never updated, if it was - we don't want to update
        // it within the loop unless some other ovf store was updated successfully (we use it as best effort backup so
        // we'll
        // possibly have some ovf data on storage)
        if (lastOvfStoreForUpdate.getFirst().getLastUpdated() == null) {
            domainOvfStoresInfoForUpdate.removeLast();
        }

        boolean shouldUpdateLastOvfStore = false;

        for (Pair<StorageDomainOvfInfo, DiskImage> pair : domainOvfStoresInfoForUpdate) {
            shouldUpdateLastOvfStore |=
                    performOvfUpdateForDomain(bytes,
                            updateDate,
                            pair.getFirst(),
                            pair.getSecond(),
                            vmAndTemplatesIds);
        }

        // if we successfully updated any ovf store, we can attempt to also update the one we kept for best effort
        // backup (if we did)
        if (shouldUpdateLastOvfStore && lastOvfStoreForUpdate != null) {
            performOvfUpdateForDomain(bytes,
                    updateDate,
                    lastOvfStoreForUpdate.getFirst(),
                    lastOvfStoreForUpdate.getSecond(),
                    vmAndTemplatesIds);
        }
    }

    private boolean performOvfUpdateForDomain(byte[] ovfData,
            Date updateDate,
            StorageDomainOvfInfo storageDomainOvfInfo,
            DiskImage ovfDisk,
            List<Guid> vmAndTemplatesIds) {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(ovfData);

        UploadStreamParameters uploadStreamParameters =
                new UploadStreamParameters(ovfDisk.getStoragePoolId(), ovfDisk.getStorageIds().get(0),
                        ovfDisk.getId(), ovfDisk.getImageId(), byteArrayInputStream,
                        Long.valueOf(ovfData.length));
        uploadStreamParameters.setParentCommand(getActionType());
        uploadStreamParameters.setParentParameters(getParameters());

        storageDomainOvfInfo.setStoredOvfIds(null);
        getStorageDomainOvfInfoDao().update(storageDomainOvfInfo);

        VdcReturnValueBase vdcReturnValueBase =
                Backend.getInstance().runInternalAction(VdcActionType.UploadStream, uploadStreamParameters);
        if (vdcReturnValueBase.getSucceeded()) {
            storageDomainOvfInfo.setStatus(StorageDomainOvfInfoStatus.UPDATED);
            storageDomainOvfInfo.setStoredOvfIds(vmAndTemplatesIds);
            storageDomainOvfInfo.setLastUpdated(updateDate);
            getStorageDomainOvfInfoDao().update(storageDomainOvfInfo);
            getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValueBase.getInternalVdsmTaskIdList());
        }

        return vdcReturnValueBase.getSucceeded();
    }

    @Override
    protected void executeCommand() {
        Integer ovfStoresCountForDomain = Config.<Integer> getValue(ConfigValues.StorageDomainOvfStoreCount);

        for (int i = ovfDiskCount; i < ovfStoresCountForDomain; i++) {
            Backend.getInstance().runInternalAction(VdcActionType.CreateOvfVolumeForStorageDomain,
                    new StorageDomainParametersBase(getParameters().getStoragePoolId(),
                            getParameters().getStorageDomainId()));
        }

        updateOvfStoreContent();

        setSucceeded(true);
    }

    protected void buildFilesForOvfs(List<Pair<Guid, String>> ovfs, InMemoryTar inMemoryTar) throws Exception {
        for (Pair<Guid, String> pair : ovfs) {
            inMemoryTar.addTarEntry(pair.getSecond().getBytes(), pair.getFirst() + ".ovf");
        }
    }

    protected VmAndTemplatesGenerationsDAO getVmAndTemplatesGenerationsDao() {
        return DbFacade.getInstance().getVmAndTemplatesGenerationsDao();
    }

    protected StorageDomainOvfInfoDao getStorageDomainOvfInfoDao() {
        return DbFacade.getInstance().getStorageDomainOvfInfoDao();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> lockMap = new HashMap<>();
        lockMap.put(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                        VdcBllMessages.ACTION_TYPE_FAILED_DOMAIN_OVF_ON_UPDATE));

        for (Pair<StorageDomainOvfInfo, DiskImage> pair : domainOvfStoresInfoForUpdate) {
            StorageDomainOvfInfo storageDomainOvfInfo = pair.getFirst();
            lockMap.put(storageDomainOvfInfo.getOvfDiskId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                            VdcBllMessages.ACTION_TYPE_FAILED_OVF_DISK_IS_BEING_USED));
        }

        return lockMap;
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
                        VdcBllMessages.ACTION_TYPE_FAILED_DOMAIN_OVF_ON_UPDATE));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getActionState() == CommandActionState.EXECUTE && !getSucceeded()) {
            return AuditLogType.UPDATE_OVF_FOR_STORAGE_DOMAIN_FAILED;
        }

        return super.getAuditLogTypeValue();
    }
}
