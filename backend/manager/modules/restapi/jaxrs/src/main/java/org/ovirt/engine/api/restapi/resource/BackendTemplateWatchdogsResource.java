package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateWatchdogsResource extends BackendWatchdogsResource {

    private Guid templateId;

    public BackendTemplateWatchdogsResource(Guid templateId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        super(false, templateId, queryType, queryParams);
        this.templateId = templateId;
    }

    @Override
    public WatchDog addParents(WatchDog device) {
        device.setTemplate(new Template());
        device.getTemplate().setId(templateId.toString());
        return device;
    }

}
