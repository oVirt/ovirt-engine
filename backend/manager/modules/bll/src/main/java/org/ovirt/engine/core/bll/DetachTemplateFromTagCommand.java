package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class DetachTemplateFromTagCommand<T extends AttachEntityToTagParameters> extends TemplatesTagMapBase<T> {

    @Inject
    private TagDao tagDao;
    @Inject
    private VmTemplateDao vmTemplateDao;

    public DetachTemplateFromTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getTagId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TAG_ID_REQUIRED);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        for (Guid templateGuid : getTemplatesList()) {
            if (tagDao.getTagTemplateByTagIdAndByTemplateId(getTagId(), templateGuid) != null) {
                VmTemplate template = vmTemplateDao.get(templateGuid);
                if (template != null) {
                    appendCustomCommaSeparatedValue("TemplatesNames", template.getName());
                    tagDao.detachTemplateFromTag(getTagId(), templateGuid);
                }
            }
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_TEMPLATE_FROM_TAG : AuditLogType.USER_DETACH_TEMPLATE_FROM_TAG_FAILED;
    }
}
