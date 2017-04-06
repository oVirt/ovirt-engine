package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.TagDao;

public class UpdateTagCommand<T extends TagsOperationParameters> extends TagsCommandOperationBase<T> {

    @Inject
    private TagsDirector tagsDirector;
    @Inject
    private TagDao tagDao;

    public UpdateTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        tagsDirector.updateTag(getTag());
        tagDao.update(getTag());
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        // we fetch by new name to see if it is in use
        Tags tag = tagDao.getByName(getParameters().getTag().getTagName());
        if (tag != null && !tag.getTagId().equals(getParameters().getTag().getTagId())) {
            addValidationMessage(EngineMessage.TAGS_SPECIFY_TAG_IS_IN_USE);
            return false;
        }
        // we fetch by id to see if the tag is realy read-only
        tag = tagDao.get(getParameters().getTag().getTagId());
        if (tag.getIsReadonly() != null && tag.getIsReadonly()) {
            addValidationMessage(EngineMessage.TAGS_CANNOT_EDIT_READONLY_TAG);
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_TAG : AuditLogType.USER_UPDATE_TAG_FAILED;
    }
}
