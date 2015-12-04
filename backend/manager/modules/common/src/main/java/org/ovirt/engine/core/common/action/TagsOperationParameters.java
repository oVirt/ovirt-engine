package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Tags;

public class TagsOperationParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = 4931970264921707074L;
    @Valid
    private Tags _tag;

    public TagsOperationParameters(Tags tag) {
        super(tag.getTagId());
        _tag = tag;
    }

    public Tags getTag() {
        return _tag;
    }

    public TagsOperationParameters() {
    }
}
