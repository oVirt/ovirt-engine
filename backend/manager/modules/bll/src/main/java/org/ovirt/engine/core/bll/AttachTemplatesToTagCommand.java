package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.TagsTemplateMap;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachTemplatesToTagCommand<T extends AttachEntityToTagParameters> extends TemplatesTagMapBase<T> {

    public AttachTemplatesToTagCommand(T parameters, CommandContext cmdContext) {
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
            VmTemplate template = DbFacade.getInstance().getVmTemplateDao().get(templateGuid);
            if (template != null) {
                if (DbFacade.getInstance().getTagDao().getTagTemplateByTagIdAndByTemplateId(getTagId(), templateGuid) == null) {
                    appendCustomValue("TemplatesNames", template.getName(), ", ");
                    TagsTemplateMap map = new TagsTemplateMap(getTagId(), templateGuid);
                    DbFacade.getInstance().getTagDao().attachTemplateToTag(map);
                    noActionDone = false;
                } else {
                    appendCustomValue("TemplatesNamesExists", template.getName(), ", ");
                }
            }
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (noActionDone) {
            return AuditLogType.USER_ATTACH_TAG_TO_TEMPLATE_EXISTS;
        }
        return getSucceeded() ? AuditLogType.USER_ATTACH_TAG_TO_TEMPLATE : AuditLogType.USER_ATTACH_TAG_TO_TEMPLATE_FAILED;
    }
}
