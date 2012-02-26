package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainTemplateQueryParameters extends StorageDomainQueryParametersBase {

    private static final long serialVersionUID = 3787908881298340953L;
    private Guid templateId;

    public StorageDomainTemplateQueryParameters(Guid storageId) {
        this(storageId, null);
    }

    public StorageDomainTemplateQueryParameters(Guid storageId, Guid templateId) {
        super(storageId);
        this.templateId = templateId;
    }

    public void setTemplateId(Guid templateId) {
        this.templateId = templateId;
    }

    public Guid getTemplateId() {
        return templateId;
    }

}
