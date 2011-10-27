package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDynamicDAO;

public class RemoveVdsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    public RemoveVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        if (getVdsIdRef() != null && CanBeRemoved(getVdsId())) {
            Guid vdsGroupId = getVds().getvds_group_id();
            RemoveVdsStatisticsFromDb();
            RemoveVdsDynamicFromDb();
            RemoveVdsStaticFromDb();
            RemoveVdsFromCollection();
            setSucceeded(true);
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = CanRemoveVds(getVdsId(), getReturnValue().getCanDoActionMessages());
        storage_pool storagePool = getStoragePoolDAO().getForVds(getParameters().getVdsId());

        if (returnValue && storagePool != null && storagePool.getstorage_pool_type() == StorageType.LOCALFS) {
            if (!getStorageDomainDAO().getAllForStoragePool(storagePool.getId()).isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_REMOVE_HOST_WITH_LOCAL_STORAGE);
                returnValue = false;
            }
        }
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VDS : AuditLogType.USER_FAILED_REMOVE_VDS;
    }

    private boolean HasRunningVms(Guid vdsId) {
        VdsDynamic vdsDynamic = getVdsDynamicDAO().get(vdsId);
        return vdsDynamic.getvm_count() > 0;
    }

    protected VdsDynamicDAO getVdsDynamicDAO() {
        return DbFacade.getInstance().getVdsDynamicDAO();
    }

    private boolean StatusLegalForRemove(Guid vdsId) {
        // error: check this
        // VDS vds = ResourceManager.Instance.getVds(vdsId);
        VDS vds = getVdsDAO().get(vdsId);

        if (vds != null) {
            return ((vds.getstatus() == VDSStatus.NonResponsive) || (vds.getstatus() == VDSStatus.Maintenance)
                    || (vds.getstatus() == VDSStatus.Down) || (vds.getstatus() == VDSStatus.Unassigned)
                    || (vds.getstatus() == VDSStatus.InstallFailed) || (vds.getstatus() == VDSStatus.PendingApproval) || (vds
                    .getstatus() == VDSStatus.NonOperational));
        }
        return false;
    }

    private boolean CanBeRemoved(Guid vdsId) {
        return StatusLegalForRemove(vdsId) && !HasRunningVms(vdsId);
    }

    private void RemoveVdsFromCollection() {
        // ResourceManager.Instance.removeVds(VdsId);
        Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.RemoveVds, new RemoveVdsVDSCommandParameters(getVdsId()));
    }

    private void RemoveVdsStaticFromDb() {
        DbFacade.getInstance().getVdsStaticDAO().remove(getVdsId());
    }

    private void RemoveVdsDynamicFromDb() {
        getVdsDynamicDAO().remove(getVdsId());
    }

    private void RemoveVdsStatisticsFromDb() {
        DbFacade.getInstance().getVdsStatisticsDAO().remove(getVdsId());
    }

    private boolean CanRemoveVds(Guid vdsId, java.util.ArrayList<String> text) {
        boolean returnValue = true;
        // check if vds id is valid
        VDS vds = getVdsDAO().get(vdsId);
        if (vds == null) {
            text.add(VdcBllMessages.VDS_INVALID_SERVER_ID.toString());
            returnValue = false;
        } else {
            if (!StatusLegalForRemove(vdsId)) {
                text.add(VdcBllMessages.VDS_CANNOT_REMOVE_VDS_STATUS_ILLEGAL.toString());
                returnValue = false;
            }
            if (HasRunningVms(vdsId)) {
                text.add(VdcBllMessages.VDS_CANNOT_REMOVE_VDS_DETECTED_RUNNING_VM.toString());
                returnValue = false;
            }
            List<String> vmNamesPinnedToHost = getVmStaticDAO().getAllNamesPinnedToHost(vdsId);
            if (!vmNamesPinnedToHost.isEmpty()) {
                text.add(VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS.toString());
                text.add(String.format("$VmNames %s", StringUtils.join(vmNamesPinnedToHost, ',')));
                returnValue = false;
            }
        }
        return returnValue;
    }
}
