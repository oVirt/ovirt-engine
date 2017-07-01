package org.ovirt.engine.core.common.queries;

public class GetTagsByTemplateIdParameters extends QueryParametersBase {
    private static final long serialVersionUID = -8537901288950684062L;

    public GetTagsByTemplateIdParameters(String templateId) {
        this.templateId = templateId;
    }

    private String templateId;

    public String getTemplateId() {
        return templateId;
    }

    public GetTagsByTemplateIdParameters() {
    }
}
