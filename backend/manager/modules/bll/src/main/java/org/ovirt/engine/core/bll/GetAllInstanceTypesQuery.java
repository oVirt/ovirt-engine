package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetAllInstanceTypesQuery<P extends QueryParametersBase> extends GetAllTemplateBasedEntityQuery<P> {

    public GetAllInstanceTypesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext, VmEntityType.INSTANCE_TYPE);
    }

}
