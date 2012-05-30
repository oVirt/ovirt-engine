package org.ovirt.engine.core.common.queries;

public class GetTagByTagNameParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4620515574262550994L;

    public GetTagByTagNameParametersBase(String tagName) {
        _tagName = tagName;
    }

    private String _tagName;

    public String getTagName() {
        return _tagName;
    }

    public GetTagByTagNameParametersBase() {
    }
}
