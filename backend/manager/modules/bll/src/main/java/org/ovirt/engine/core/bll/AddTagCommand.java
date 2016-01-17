package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddTagCommand<T extends TagsOperationParameters> extends TagsCommandOperationBase<T> {

    public AddTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {

        DbFacade.getInstance().getTagDao().save(getTag());
        TagsDirector.getInstance().addTag(getTag());

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        Tags tag = DbFacade.getInstance().getTagDao()
                .getByName(getParameters().getTag().getTagName());
        if (tag != null) {
            addValidationMessage(EngineMessage.TAGS_SPECIFY_TAG_IS_IN_USE);
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_TAG : AuditLogType.USER_ADD_TAG_FAILED;
    }
}
