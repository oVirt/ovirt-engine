package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateWatchdogsResource extends BackendWatchdogsResource {

    public BackendTemplateWatchdogsResource(Guid parentId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        super(parentId, queryType, queryParams);
    }

    @Override
    public WatchDog addParents(WatchDog device) {
        device.setTemplate(new Template());
        device.getTemplate().setId(parentId.toString());
        return device;
    }

}
