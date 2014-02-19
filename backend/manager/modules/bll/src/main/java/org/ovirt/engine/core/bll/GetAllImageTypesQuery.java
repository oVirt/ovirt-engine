package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllImageTypesQuery<P extends VdcQueryParametersBase> extends GetAllTemplateBasedEntityQuery<P> {

    public GetAllImageTypesQuery(P parameters) {
        super(parameters, VmEntityType.INSTANCE_TYPE);
    }

}
