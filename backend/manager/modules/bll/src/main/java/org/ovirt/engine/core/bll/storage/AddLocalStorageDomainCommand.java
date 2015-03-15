package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddLocalStorageDomainCommand<T extends StorageDomainManagementParameter> extends AddStorageDomainCommon<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddLocalStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddLocalStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = super.canDoAction();

        if (retVal) {
            StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().getForVds(getParameters().getVdsId());

            if (storagePool == null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_CLUSTER_HAVE_NOT_EXISTING_DATA_CENTER_NETWORK);
                retVal = false;
            } else {
                setStoragePool(storagePool);
            }

            if (retVal &&
                    getStorageDomain().getStorageType() == StorageType.LOCALFS &&
                    !storagePool.isLocal()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_IS_NOT_LOCAL);
                retVal = false;
            }

            if (retVal && storagePool.getStatus() != StoragePoolStatus.Uninitialized) {
                retVal = checkMasterDomainIsUp();
            }

            // we limit RHEV-H local storage to its persistence mount - /data/images/rhev/
            if (retVal && this.getVds().getVdsType() == VDSType.oVirtNode) {

                StorageServerConnections conn =
                        DbFacade.getInstance().getStorageServerConnectionDao().get(getParameters().getStorageDomain()
                                .getStorage());

                String rhevhLocalFSPath = Config.<String> getValue(ConfigValues.RhevhLocalFSPath);
                if (!conn.getconnection().equals(rhevhLocalFSPath)) {
                    addCanDoActionMessage(VdcBllMessages.RHEVH_LOCALFS_WRONG_PATH_LOCATION);
                    addCanDoActionMessageVariable("path", rhevhLocalFSPath);
                    retVal = false;
                }
            }
        }
        return retVal;
    }

    @Override
    protected void executeCommand() {
        getStorageDomain().setStorageFormat(
                VersionStorageFormatUtil.getPreferredForVersion(
                        getStoragePool().getCompatibilityVersion(), getStorageDomain().getStorageType()));
        super.executeCommand();
        if (getSucceeded()) {
            VdcReturnValueBase returnValue = Backend.getInstance()
                    .runInternalAction(
                            VdcActionType.AttachStorageDomainToPool,
                            new AttachStorageDomainToPoolParameters(getStorageDomain().getId(), getStoragePool().getId()));
            if(!returnValue.getSucceeded()) {
                getReturnValue().setSucceeded(false);
                getReturnValue().setFault(returnValue.getFault());
            }
        }
    }
}
