package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.tags;

public class TagsOperationParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = 4931970264921707074L;
    @Valid
    private tags _tag;

    public TagsOperationParameters(tags tag) {
        super(tag.gettag_id());
        _tag = tag;
    }

    public tags getTag() {
        return _tag;
    }

    public TagsOperationParameters() {
    }
}
