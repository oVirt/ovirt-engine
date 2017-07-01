package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetAllVmTemplatesQuery<P extends QueryParametersBase> extends GetAllTemplateBasedEntityQuery<P> {

    public GetAllVmTemplatesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext, VmEntityType.TEMPLATE);
    }

}
