package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TagsVmMapParameters;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.TagDao;

public class UpdateTagsVmMapDefaultDisplayTypeCommand<T extends TagsVmMapParameters> extends TagsCommandBase<T> {

    @Inject
    private TagDao tagDao;

    public UpdateTagsVmMapDefaultDisplayTypeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        TagsVmMap tagsVmMap;
        tagsVmMap = tagDao.getTagVmByTagIdAndByVmId(getParameters().getTagsVmMap().getTagId(),
                getParameters().getTagsVmMap().getVmId());
        if (tagsVmMap == null) {
            addValidationMessage(EngineMessage.TAGS_SPECIFY_TAG_IS_NOT_EXISTS);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        tagDao.updateDefaultDisplayForVmTag(getParameters().getTagsVmMap());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.UPDATE_TAGS_VM_DEFAULT_DISPLAY_TYPE
                : AuditLogType.UPDATE_TAGS_VM_DEFAULT_DISPLAY_TYPE_FAILED;
    }
}
