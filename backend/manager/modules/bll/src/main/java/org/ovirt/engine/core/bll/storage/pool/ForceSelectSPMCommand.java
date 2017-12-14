package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.irsbroker.SpmStopOnIrsVDSCommandParameters;

@NonTransactiveCommandAttribute
public class ForceSelectSPMCommand<T extends ForceSelectSPMParameters> extends CommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private AsyncTaskDao asyncTaskDao;
    @Inject
    private StoragePoolDao storagePoolDao;

    private StoragePool storagePoolForVds;

    public ForceSelectSPMCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(getParameters().getPreferredSPMId());
    }

    @Override
    protected boolean validate() {
        if (getVds() == null) {
            return failValidation(EngineMessage.VDS_NOT_EXIST);
        }

        if(!getVds().getClusterSupportsVirtService()){
            return failValidation(EngineMessage.CANNOT_FORCE_SELECT_SPM_HOST_DOES_NOT_SUPPORT_VIRT_SERVICES);
        }

        if (getVds().getStatus() != VDSStatus.Up) {
            return failValidation(EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_NOT_UP);
        }

        if (getStoragePoolForVds() == null) {
            return failValidation(EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_NOT_IN_POOL);
        }

        if (getVds().getSpmStatus() != VdsSpmStatus.None) {
            return failValidation(EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_ALREADY_SPM);
        }

        if (getVds().getVdsSpmPriority() == BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY) {
            return failValidation(EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_MARKED_AS_NEVER_SPM);
        }

        if (!validate(new StoragePoolValidator(getStoragePoolForVds()).existsAndUp())) {
            return false;
        }

        if (isAsyncTasksRunningOnPool(getStoragePoolForVds().getId())) {
            return failValidation(EngineMessage.CANNOT_FORCE_SELECT_SPM_STORAGE_POOL_HAS_RUNNING_TASKS);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        SpmStopOnIrsVDSCommandParameters params =
                new SpmStopOnIrsVDSCommandParameters(getStoragePoolForVds().getId(),
                        getParameters().getPreferredSPMId());

        if (runVdsCommand(VDSCommandType.SpmStopOnIrs, params).getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.USER_FORCE_SELECTED_SPM);
        } else {
            auditLogDirector.log(this, AuditLogType.USER_FORCE_SELECTED_SPM_STOP_FAILED);
        }

        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__FORCE_SELECT);
        addValidationMessage(EngineMessage.VAR__TYPE__SPM);
        addValidationMessageVariable("VdsName", getVds().getName());
    }

    private boolean isAsyncTasksRunningOnPool(Guid storagePoolId) {
        List<AsyncTask> tasks = asyncTaskDao.getAsyncTaskIdsByStoragePoolId(storagePoolId);
        return !tasks.isEmpty();
    }

    private StoragePool getStoragePoolForVds() {
        if (storagePoolForVds == null) {
            storagePoolForVds = storagePoolDao.getForVds(getVds().getId());
        }
        return storagePoolForVds;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            if (getVds() != null) {
                jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), getVds().getStoragePoolName());
            }
        }
        return jobProperties;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissions = new ArrayList<>();
        permissions.add(new PermissionSubject(getParameters().getPreferredSPMId(),
                VdcObjectType.VDS,
                ActionGroup.MANIPULATE_HOST));
        return permissions;
    }
}
