package org.ovirt.engine.core.common.queries;

public class GetTagVdsMapByTagNameParameters extends GetTagByTagNameParametersBase {
    private static final long serialVersionUID = -2896819836634242313L;

    public GetTagVdsMapByTagNameParameters(String tagName) {
        super(tagName);
    }

    public GetTagVdsMapByTagNameParameters() {
    }
}
