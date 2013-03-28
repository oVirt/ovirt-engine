package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;

/** A utility class for verifying running a vm*/
public class VmRunHandler {
    private static final VmRunHandler instance = new VmRunHandler();

    public static VmRunHandler getInstance() {
        return instance;
    }

    /**
     * This method checks whether the given VM is capable to run.
     *
     * @param vm not null {@link VM}
     * @param message
     * @param runParams
     * @param vdsSelector
     * @param snapshotsValidator
     * @param vmPropsUtils
     * @return true if the given VM can run with the given properties, false otherwise
     */
    public boolean canRunVm(VM vm, ArrayList<String> message, RunVmParams runParams,
            VdsSelector vdsSelector, SnapshotsValidator snapshotsValidator) {
        boolean retValue = true;

        /**
         * only if can do action ok then check with actions matrix that status is valid for this action
         */
        if (retValue
                && !VdcActionUtils.CanExecute(Arrays.asList(vm), VM.class,
                        VdcActionType.RunVm)) {
            retValue = false;
            message.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL.toString());
        }
        return retValue;
    }

    protected IsoDomainListSyncronizer getIsoDomainListSyncronizer() {
        return IsoDomainListSyncronizer.getInstance();
    }

    public boolean shouldVmRunAsStateless(RunVmParams param, VM vm) {
        if (param.getRunAsStateless() != null) {
            return param.getRunAsStateless();
        }
        return vm.isStateless();
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected StorageDomainDAO getStorageDomainDAO() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    protected StoragePoolDAO getStoragePoolDAO() {
        return DbFacade.getInstance().getStoragePoolDao();
    }
}
