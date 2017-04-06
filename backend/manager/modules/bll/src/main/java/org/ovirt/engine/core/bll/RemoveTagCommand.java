package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDao;

public class RemoveTagCommand<T extends TagsActionParametersBase> extends TagsCommandBase<T> {

    @Inject
    private TagsDirector tagsDirector;
    @Inject
    private TagDao tagDao;

    public RemoveTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            String tagIdAndChildrenIds = tagsDirector.getTagIdAndChildrenIds(getTagId());
            tagsDirector.removeTag(getTag().getTagId());
            String[] IDsArray = tagIdAndChildrenIds.split("[,]", -1);
            for (String id : IDsArray) {
                id = id.replace("'", "");
                tagDao.remove(new Guid(id));
            }
            setSucceeded(true);
        }
    }

    @Override
    protected boolean validate() {
        boolean returnValue = true;
        if (getTagId() == null || tagDao.get(getTagId()) == null) {
            addValidationMessage(EngineMessage.TAGS_CANNOT_REMOVE_TAG_NOT_EXIST);
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_TAG : AuditLogType.USER_REMOVE_TAG_FAILED;
    }
}
