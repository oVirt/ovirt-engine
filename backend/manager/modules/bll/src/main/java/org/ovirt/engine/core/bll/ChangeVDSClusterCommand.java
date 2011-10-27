package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ChangeVDSClusterCommand<T extends ChangeVDSClusterParameters> extends VdsCommand<T> {

    private storage_pool targetStoragePool;

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId
     */
    public ChangeVDSClusterCommand(Guid commandId) {
        super(commandId);
    }

    public ChangeVDSClusterCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        VDS vds = getVds();
        if (vds == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            return false;
        }
        if (!ObjectIdentityChecker.CanUpdateField(vds, "vds_group_id", vds.getstatus())) {
            addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE);
            return false;
        }
        VDSGroup targetCluster = DbFacade.getInstance().getVdsGroupDAO().get(getParameters().getClusterId());
        if (targetCluster == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }

        targetStoragePool = DbFacade.getInstance().getStoragePoolDAO().getForVdsGroup(targetCluster.getID());
        if (targetStoragePool != null && targetStoragePool.getstorage_pool_type() == StorageType.LOCALFS) {
            if (!DbFacade.getInstance().getVdsStaticDAO().getAllForVdsGroup(getParameters().getClusterId()).isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        VDSGroup sourceCluster = getVdsGroup();

        final Guid targetClusterId = getParameters().getClusterId();
        if (sourceCluster.getID().equals(targetClusterId)) {
            setSucceeded(true);
            return;
        }
        // save the new cluster id
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                VdsStatic staticData = getVds().getStaticData();
                getCompensationContext().snapshotEntity(staticData);
                staticData.setvds_group_id(targetClusterId);
                DbFacade.getInstance().getVdsStaticDAO().update(staticData);
                getCompensationContext().stateChanged();
                return null;
            }
        });

        // handle spm
        getParameters().setCompensationEnabled(true);
        getParameters().setTransactionScopeOption(TransactionScopeOption.RequiresNew);
        if (sourceCluster.getstorage_pool_id() != null) {
            VdcReturnValueBase removeVdsSpmIdReturn = Backend.getInstance().runInternalAction(
                    VdcActionType.RemoveVdsSpmId, getParameters(), getCompensationContext());
            if (!removeVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(removeVdsSpmIdReturn.getFault());
                return;
            }
        }

        if (targetStoragePool != null) {
            VdcReturnValueBase addVdsSpmIdReturn = Backend.getInstance().runInternalAction(
                        VdcActionType.AddVdsSpmId, getParameters(), getCompensationContext());
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return;
            }
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VDS : AuditLogType.USER_FAILED_UPDATE_VDS;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        Map<Guid, VdcObjectType> map = new HashMap<Guid, VdcObjectType>(2);
        map.put(getParameters().getVdsId(), VdcObjectType.VDS);
        map.put(getParameters().getClusterId(), VdcObjectType.VdsGroups);
        Map<Guid, VdcObjectType> unmodifiableMap = Collections.unmodifiableMap(map);
        return unmodifiableMap;
    }
}
