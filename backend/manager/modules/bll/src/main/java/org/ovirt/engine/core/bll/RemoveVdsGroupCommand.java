package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
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
    protected boolean validate() {
        List<VmPool> list = null;
        boolean returnValue = true;
        if (getVdsGroup() == null) {
            addValidationMessage(EngineMessage.VMT_CLUSTER_IS_NOT_VALID);
            returnValue = false;
        } else {
            if (getVdsGroup().getId().equals(Config.getValue(ConfigValues.AutoRegistrationDefaultVdsGroupID))) {
                addValidationMessage(EngineMessage.VDS_CANNOT_REMOVE_DEFAULT_VDS_GROUP);
                returnValue = false;
            }

            if (DbFacade.getInstance().getVdsStaticDao()
                    .getAllForVdsGroup(getVdsGroup().getId()).size() != 0) {
                addValidationMessage(EngineMessage.VDS_CANNOT_REMOVE_VDS_GROUP_VDS_DETECTED);
                returnValue = false;
            }
            if (DbFacade.getInstance().getVmStaticDao()
                    .getAllByVdsGroup(getVdsGroup().getId()).size() != 0) {
                addValidationMessage(EngineMessage.VM_CANNOT_REMOVE_VDS_GROUP_VMS_DETECTED);
                returnValue = false;
            }
            if (DbFacade.getInstance().getVmTemplateDao()
                    .getAllForVdsGroup(getVdsGroup().getId()).size() != 0) {
                addValidationMessage(EngineMessage.VMT_CANNOT_REMOVE_VDS_GROUP_VMTS_DETECTED);
                returnValue = false;
            }
            if ((list = DbFacade.getInstance().getVmPoolDao().getAll()).size() > 0) {
                for (VmPool pool : list) {
                    if (pool.getVdsGroupId().equals(getVdsGroup().getId())) {
                        addValidationMessage(EngineMessage.VDS_GROUP_CANNOT_REMOVE_HAS_VM_POOLS);
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
        addValidationMessage(EngineMessage.VAR__TYPE__CLUSTER);
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
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
