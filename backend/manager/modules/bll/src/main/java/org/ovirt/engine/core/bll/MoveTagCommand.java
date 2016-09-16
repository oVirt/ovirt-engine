package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.MoveTagParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class MoveTagCommand<T extends MoveTagParameters> extends TagsCommandBase<T> {

    @Inject
    private TagsDirector tagsDirector;

    private String _oldParnetTagName = "[null]";

    public MoveTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean returnValue = true;
        if (getParameters().getNewParentId().equals(getParameters().getTagId())) {
            addValidationMessage(EngineMessage.TAGS_SPECIFIED_TAG_CANNOT_BE_THE_PARENT_OF_ITSELF);
            returnValue = false;
        }
        if (tagsDirector
                .isTagDescestorOfTag(getParameters().getTagId(), getParameters().getNewParentId())) {
            addValidationMessage(EngineMessage.TAGS_SPECIFIED_TAG_CANNOT_BE_THE_PARENT_OF_ITSELF);
            returnValue = false;
        }
        return returnValue;
    }

    public String getOldParnetTagName() {
        return _oldParnetTagName;
    }

    public String getNewParentTagName() {
        Tags newParent = tagsDirector.getTagById(getParameters().getNewParentId());
        if (newParent != null) {
            return newParent.getTagName();
        }

        return "[null]";
    }

    private void initOldParentTagName() {
        if (getTag() != null && getTag().getParentId() != null) {
            Tags parent = tagsDirector.getTagById(new Guid(getTag().getParentId().toString()));
            if (parent != null) {
                _oldParnetTagName = parent.getTagName();
            }
        }
    }

    @Override
    protected void executeCommand() {
        initOldParentTagName();
        tagsDirector.moveTag(getParameters().getTagId(), getParameters().getNewParentId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_MOVE_TAG : AuditLogType.USER_MOVE_TAG_FAILED;
    }
}
