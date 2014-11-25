package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddExistingBlockStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        AddStorageDomainCommon<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddExistingBlockStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddExistingBlockStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        // Add StorageDomain object to DB and update statistics
        addStorageDomainInDb();
        updateStorageDomainDynamicFromIrs();

        // Add relevant LUNs to DB
        List<LUNs> luns = getLUNsFromVgInfo(getStorageDomain().getStorage());
        saveLUNsInDB(luns);

        setSucceeded(true);
    }

    @Override
    protected boolean canAddDomain() {
        if (getStorageDomainStaticDAO().get(getStorageDomain().getId()) != null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        }
        return true;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getParameters().getStorageDomainId() != null) {
            return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    private List<LUNs> getLUNsFromVgInfo(String vgId) {
        List<LUNs> luns = new ArrayList<>();
        VDSReturnValue returnValue;

        try {
            returnValue = runVdsCommand(VDSCommandType.GetVGInfo,
                    new GetVGInfoVDSCommandParameters(getParameters().getVdsId(), vgId));
        } catch (RuntimeException e) {
            log.errorFormat("Could not get info for VG ID: {0}. Error message: {1}",
                    vgId, e.getMessage());
            return luns;
        }

        luns.addAll((ArrayList<LUNs>) returnValue.getReturnValue());

        return luns;
    }

    private void saveLUNsInDB(final List<LUNs> luns) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                for (LUNs lun : luns) {
                    proceedLUNInDb(lun, getStorageDomain().getStorageType(), getStorageDomain().getStorage());
                }
                return null;
            }
        });
    }

}
