package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddExistingBlockStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        AddStorageDomainCommon<T> {

    @Inject
    private BlockStorageDomainHelper blockStorageDomainHelper;
    @Inject
    private LunDao lunDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddExistingBlockStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddExistingBlockStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        updateStaticDataDefaults();

        // Add StorageDomain object to DB and update statistics
        addStorageDomainInDb();
        updateStorageDomainFromIrs();

        // Add relevant LUNs to DB
        List<LUNs> luns = getLUNsFromVgInfo();
        saveLUNsInDB(luns);
        updateMetadataDevices();
        setSucceeded(true);
    }

    protected void updateMetadataDevices() {
        if (getStorageDomain().getVgMetadataDevice() == null || getStorageDomain().getFirstMetadataDevice() == null) {
            blockStorageDomainHelper.fillMetadataDevicesInfo(getStorageDomain().getStorageStaticData(),
                    getVds().getId());
            storageDomainStaticDao.update(getStorageDomain().getStorageStaticData());
        }
    }

    @Override
    protected boolean canAddDomain() {
        if (storageDomainStaticDao.get(getStorageDomain().getId()) != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        }

        List<LUNs> lunsOnStorage = getLUNsFromVgInfo();
        if (lunsOnStorage.isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROBLEM_WITH_CANDIDATE_INFO);
        }
        Set<String> allLunIds = getAllLuns().stream().map(LUNs::getId).collect(Collectors.toSet());
        if (lunsOnStorage.stream().map(LUNs::getId).anyMatch(allLunIds::contains)) {
            log.info("There are existing luns in the system which are part of VG id '{}'",
                    getStorageDomain().getStorage());
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMPORT_STORAGE_DOMAIN_EXTERNAL_LUN_DISK_EXIST);
        }

        return true;
    }

    protected List<LUNs> getAllLuns() {
        return lunDao.getAll();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getParameters().getStorageDomainId() != null) {
            return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    protected boolean validateDiscardAfterDeleteLegal(StorageDomainValidator storageDomainValidator) {
        return !getStorageDomain().getDiscardAfterDelete() ||
                validate(storageDomainValidator.isDiscardAfterDeleteLegalForNewBlockStorageDomain(getLUNsFromVgInfo()));
    }

    protected List<LUNs> getLUNsFromVgInfo() {
        List<LUNs> luns = new ArrayList<>();
        VDSReturnValue returnValue;

        try {
            returnValue = runVdsCommand(VDSCommandType.GetVGInfo,
                    new GetVGInfoVDSCommandParameters(getParameters().getVdsId(), getStorageDomain().getStorage()));
        } catch (RuntimeException e) {
            log.error("Could not get info for VG ID '{}': {}",
                    getStorageDomain().getStorage(), e.getMessage());
            log.debug("Exception", e);
            return luns;
        }

        luns.addAll((ArrayList<LUNs>) returnValue.getReturnValue());

        return luns;
    }

    protected void saveLUNsInDB(final List<LUNs> luns) {
        TransactionSupport.executeInNewTransaction(() -> {
            for (LUNs lun : luns) {
                lunHelper.proceedLUNInDb(lun, getStorageDomain().getStorageType(), getStorageDomain().getStorage());
            }
            return null;
        });
    }

}
