package org.ovirt.engine.core.bll.exportimport;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessage;
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
public class ExportVmTemplateToOvaCommand<T extends ExportOvaParameters> extends ExportOvaCommand<T> {

    private String cachedTemplateIsBeingExportedMessage;

    public ExportVmTemplateToOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        setVmTemplateId(getParameters().getEntityId());
        if (getVmTemplate() != null) {
            setStoragePoolId(getVmTemplate().getStoragePoolId());
        }
        super.init();
    }

    @Override
    protected Nameable getEntity() {
        return getVmTemplate();
    }

    @Override
    protected boolean validate() {
        if (getEntity() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        return super.validate();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getEntityId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getTemplateIsBeingExportedMessage()));
    }

    private String getTemplateIsBeingExportedMessage() {
        if (cachedTemplateIsBeingExportedMessage == null) {
            cachedTemplateIsBeingExportedMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_EXPORTED)
                    .withOptional("TemplateName", getVmTemplate() != null ? getVmTemplate().getName() : null)
                    .toString();
        }
        return cachedTemplateIsBeingExportedMessage;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_STARTING_EXPORT_TEMPLATE_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA_FAILED;

        case END_SUCCESS:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA_FAILED;
        }
    }
}
