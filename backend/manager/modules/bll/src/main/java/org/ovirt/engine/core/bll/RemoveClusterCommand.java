package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveClusterCommand<T extends ClusterParametersBase> extends ClusterCommandBase<T> {

    public RemoveClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getClusterDao().remove(getCluster().getId());
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        List<VmPool> list = null;
        boolean returnValue = true;
        if (getCluster() == null) {
            addValidationMessage(EngineMessage.VMT_CLUSTER_IS_NOT_VALID);
            returnValue = false;
        } else {
            if (getCluster().getId().equals(Config.getValue(ConfigValues.AutoRegistrationDefaultClusterID))) {
                addValidationMessage(EngineMessage.VDS_CANNOT_REMOVE_DEFAULT_CLUSTER);
                returnValue = false;
            }

            if (DbFacade.getInstance().getVdsStaticDao()
                    .getAllForCluster(getCluster().getId()).size() != 0) {
                addValidationMessage(EngineMessage.VDS_CANNOT_REMOVE_CLUSTER_VDS_DETECTED);
                returnValue = false;
            }
            if (DbFacade.getInstance().getVmStaticDao()
                    .getAllByCluster(getCluster().getId()).size() != 0) {
                addValidationMessage(EngineMessage.VM_CANNOT_REMOVE_CLUSTER_VMS_DETECTED);
                returnValue = false;
            }
            if (DbFacade.getInstance().getVmTemplateDao()
                    .getAllForCluster(getCluster().getId()).size() != 0) {
                addValidationMessage(EngineMessage.VMT_CANNOT_REMOVE_CLUSTER_VMTS_DETECTED);
                returnValue = false;
            }
            if ((list = DbFacade.getInstance().getVmPoolDao().getAll()).size() > 0) {
                for (VmPool pool : list) {
                    if (pool.getClusterId().equals(getCluster().getId())) {
                        addValidationMessage(EngineMessage.CLUSTER_CANNOT_REMOVE_HAS_VM_POOLS);
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
        return getSucceeded() ? AuditLogType.USER_REMOVE_CLUSTER
                : AuditLogType.USER_REMOVE_CLUSTER_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveEntity.class);
        return super.getValidationGroups();
    }

}
