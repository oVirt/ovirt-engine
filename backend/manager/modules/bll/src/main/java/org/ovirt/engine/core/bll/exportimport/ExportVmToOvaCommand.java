package org.ovirt.engine.core.bll.exportimport;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExportOvaParameters;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;

@NonTransactiveCommandAttribute
public class ExportVmToOvaCommand<T extends ExportOvaParameters> extends ExportOvaCommand<T> {

    public ExportVmToOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        setVmId(getParameters().getEntityId());
        if (getVm() != null) {
            setStoragePoolId(getVm().getStoragePoolId());
        }
        super.init();
    }

    @Override
    protected Nameable getEntity() {
        return getVm();
    }

    @Override
    protected boolean validate() {
        if (getEntity() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        return super.validate();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_STARTING_EXPORT_VM_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;

        case END_SUCCESS:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getEntityId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    };
}
