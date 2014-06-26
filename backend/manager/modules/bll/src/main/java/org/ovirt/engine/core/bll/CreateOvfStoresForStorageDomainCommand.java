package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.CreateOvfStoresForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateOvfStoresForStorageDomainCommand<T extends CreateOvfStoresForStorageDomainCommandParameters> extends CommandBase<T> {

    public CreateOvfStoresForStorageDomainCommand(T parameters) {
        this(parameters, null);
    }

    public CreateOvfStoresForStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected void executeCommand() {
        for (int i = 0; i < getParameters().getStoresCount(); i++) {
            StorageDomainParametersBase storageDomainParametersBase = new StorageDomainParametersBase(getParameters().getStoragePoolId(),
                    getParameters().getStorageDomainId());
            storageDomainParametersBase.setParentCommand(getActionType());
            storageDomainParametersBase.setParentParameters(getParameters());
            VdcReturnValueBase vdcReturnValueBase =
                    runInternalAction(VdcActionType.CreateOvfVolumeForStorageDomain,
                            storageDomainParametersBase);
            getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValueBase.getInternalVdsmTaskIdList());
        }

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    protected void endSuccessfully() {
        endCommandOperations();
    }

    @Override
    protected void endWithFailure() {
        endCommandOperations();
    }

    private void endCommandOperations() {
        boolean atleastOneSucceeded = false;
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            if (p.getTaskGroupSuccess()) {
                atleastOneSucceeded = true;
                Guid diskId = ((AddImageFromScratchParameters) p).getDiskInfo().getId();

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
                AuditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED);
            }
        }

        if (atleastOneSucceeded) {
            // if we'd have the possibility to know whether we failed because of failure to acquire locks as there's an
            // update in progress, we could
            // try again (avoid setSucceeded(true) in that scenario).
            getBackend().runInternalAction(VdcActionType.ProcessOvfUpdateForStorageDomain, getParameters());
        }
        setSucceeded(true);
    }

    protected StorageDomainOvfInfoDao getStorageDomainOvfInfoDao() {
        return getDbFacade().getStorageDomainOvfInfoDao();
    }
}
