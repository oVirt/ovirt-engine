package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.CreateOvfStoresForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.CreateOvfVolumeForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateOvfStoresForStorageDomainCommand<T extends CreateOvfStoresForStorageDomainCommandParameters> extends CommandBase<T> {

    public CreateOvfStoresForStorageDomainCommand(T parameters) {
        this(parameters, null);
    }

    public CreateOvfStoresForStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected void executeCommand() {
        for (int i = 0; i < getParameters().getStoresCount(); i++) {
            CreateOvfVolumeForStorageDomainCommandParameters parameters = createCreateOvfVolumeForStorageDomainParams();

            VdcReturnValueBase vdcReturnValueBase =
                    runInternalAction(VdcActionType.CreateOvfVolumeForStorageDomain,
                            parameters);

            getReturnValue().getInternalVdsmTaskIdList().addAll(vdcReturnValueBase.getInternalVdsmTaskIdList());
        }

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    protected void startFinalizingStep() {}

    @Override
    protected void endSuccessfully() {
        endCommandOperations();
    }

    @Override
    protected void endWithFailure() {
        endCommandOperations();
    }

    private void endCommandOperations() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Guid diskId = ((AddImageFromScratchParameters) p).getDiskInfo().getId();
            if (p.getTaskGroupSuccess()) {

                StorageDomainOvfInfo storageDomainOvfInfoDb =
                        getStorageDomainOvfInfoDao()
                                .get(diskId);

                if (storageDomainOvfInfoDb == null
                        || storageDomainOvfInfoDb.getStatus() != StorageDomainOvfInfoStatus.DISABLED) {
                    continue;
                }

                getBackend().endAction(p.getCommandType(),
                        p,
                        getContext().clone().withoutCompensationContext().withoutExecutionContext());
                storageDomainOvfInfoDb.setStatus(StorageDomainOvfInfoStatus.OUTDATED);
                getStorageDomainOvfInfoDao().update(storageDomainOvfInfoDb);
            } else {
                getBackend().endAction(p.getCommandType(),
                        p,
                        getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
                addCustomValue("DiskId", diskId.toString());
                auditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED);
            }
        }

        // if we'd have the possibility to know whether we failed because of failure to acquire locks as there's an
        // update in progress, we could
        // try again (avoid setSucceeded(true) in that scenario).
        VdcReturnValueBase returnValue = runInternalActionWithTasksContext(VdcActionType.ProcessOvfUpdateForStorageDomain, createProcessOvfUpdateForDomainParams(), null);
        getReturnValue().getInternalVdsmTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());

        setSucceeded(true);
    }

    private ProcessOvfUpdateForStorageDomainCommandParameters createProcessOvfUpdateForDomainParams() {
        ProcessOvfUpdateForStorageDomainCommandParameters params = new ProcessOvfUpdateForStorageDomainCommandParameters(getParameters().getStoragePoolId(), getParameters().getStorageDomainId());
        params.setSkipDomainChecks(getParameters().isSkipDomainChecks());
        params.setParentCommand(getParameters().getParentCommand());
        params.setParentParameters(getParameters().getParentParameters());
        return params;
    }

    public CreateOvfVolumeForStorageDomainCommandParameters createCreateOvfVolumeForStorageDomainParams() {
        CreateOvfVolumeForStorageDomainCommandParameters parameters = new CreateOvfVolumeForStorageDomainCommandParameters(getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId());
        parameters.setSkipDomainChecks(getParameters().isSkipDomainChecks());
        if (hasParentCommand()) {
            parameters.setParentCommand(getParameters().getParentCommand());
            parameters.setParentParameters(getParameters().getParentParameters());
        } else {
            parameters.setParentCommand(getActionType());
            parameters.setParentParameters(getParameters());
        }

        return parameters;
    }

    protected StorageDomainOvfInfoDao getStorageDomainOvfInfoDao() {
        return getDbFacade().getStorageDomainOvfInfoDao();
    }
}
