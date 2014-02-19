package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllInstanceTypesQuery<P extends VdcQueryParametersBase> extends GetAllTemplateBasedEntityQuery<P> {

    public GetAllInstanceTypesQuery(P parameters) {
        super(parameters, VmEntityType.INSTANCE_TYPE);
    }

}
