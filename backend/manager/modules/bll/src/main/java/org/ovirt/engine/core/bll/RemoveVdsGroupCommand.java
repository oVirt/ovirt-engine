package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVdsGroupCommand<T extends VdsGroupParametersBase> extends VdsGroupCommandBase<T> {

    public RemoveVdsGroupCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getVdsGroupDao().remove(getVdsGroup().getId());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        List<VmPool> list = null;
        boolean returnValue = true;
        if (getVdsGroup() == null) {
            addCanDoActionMessage(VdcBllMessages.VMT_CLUSTER_IS_NOT_VALID);
            returnValue = false;
        } else {
            if (getVdsGroup().getId().equals(Config.getValue(ConfigValues.AutoRegistrationDefaultVdsGroupID))) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_REMOVE_DEFAULT_VDS_GROUP);
                returnValue = false;
            }

            if (DbFacade.getInstance().getVdsStaticDao()
                    .getAllForVdsGroup(getVdsGroup().getId()).size() != 0) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_REMOVE_VDS_GROUP_VDS_DETECTED);
                returnValue = false;
            }
            if (DbFacade.getInstance().getVmStaticDao()
                    .getAllByVdsGroup(getVdsGroup().getId()).size() != 0) {
                addCanDoActionMessage(VdcBllMessages.VM_CANNOT_REMOVE_VDS_GROUP_VMS_DETECTED);
                returnValue = false;
            }
            if (DbFacade.getInstance().getVmTemplateDao()
                    .getAllForVdsGroup(getVdsGroup().getId()).size() != 0) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_REMOVE_VDS_GROUP_VMTS_DETECTED);
                returnValue = false;
            }
            if ((list = DbFacade.getInstance().getVmPoolDao().getAll()).size() > 0) {
                for (VmPool pool : list) {
                    if (pool.getVdsGroupId().equals(getVdsGroup().getId())) {
                        addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_REMOVE_HAS_VM_POOLS);
                        returnValue = false;
                        break;
                    }
                }
            }
        }

        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VDS_GROUP
                : AuditLogType.USER_REMOVE_VDS_GROUP_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveEntity.class);
        return super.getValidationGroups();
    }

}
