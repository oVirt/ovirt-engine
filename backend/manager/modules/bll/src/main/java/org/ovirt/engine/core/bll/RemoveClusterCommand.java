package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.errors.EngineMessage.VMT_CLUSTER_IS_NOT_VALID;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.kubevirt.ForceClusterResourcesRemover;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class RemoveClusterCommand<T extends ClusterParametersBase> extends ClusterCommandBase<T> {

    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private ForceClusterResourcesRemover clusterResourcesRemover;

    public RemoveClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (isForceRemovalOfUnmanagedCluster()) {
            clusterResourcesRemover.forceRemove(getCluster().getId());
            setSucceeded(true);
            return;
        }

        clusterDao.remove(getCluster().getId());
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        List<VmPool> list = null;
        boolean returnValue = true;
        if (getCluster() == null) {
            addValidationMessage(VMT_CLUSTER_IS_NOT_VALID);
            returnValue = false;
        } else {
            if (isForceRemovalOfUnmanagedCluster()) {
                return true;
            }

            if (getCluster().getId().equals(Config.getValue(ConfigValues.AutoRegistrationDefaultClusterID))) {
                addValidationMessage(EngineMessage.VDS_CANNOT_REMOVE_DEFAULT_CLUSTER);
                returnValue = false;
            }

            if (!vdsStaticDao.getAllForCluster(getCluster().getId()).isEmpty()) {
                addValidationMessage(EngineMessage.VDS_CANNOT_REMOVE_CLUSTER_VDS_DETECTED);
                returnValue = false;
            }
            if (!vmStaticDao.getAllByCluster(getCluster().getId()).isEmpty()) {
                addValidationMessage(EngineMessage.VM_CANNOT_REMOVE_CLUSTER_VMS_DETECTED);
                returnValue = false;
            }
            if (!vmTemplateDao.getAllForCluster(getCluster().getId()).isEmpty()) {
                addValidationMessage(EngineMessage.VMT_CANNOT_REMOVE_CLUSTER_VMTS_DETECTED);
                returnValue = false;
            }
            if (!(list = vmPoolDao.getAll()).isEmpty()) {
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

    private boolean isForceRemovalOfUnmanagedCluster() {
        return !getCluster().isManaged() && getParameters().isForce();
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
