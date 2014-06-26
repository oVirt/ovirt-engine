package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.LUNs;
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
        return true;
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
