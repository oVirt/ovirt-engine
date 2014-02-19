package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVmTemplatesQuery<P extends VdcQueryParametersBase> extends GetAllTemplateBasedEntityQuery<P> {

    public GetAllVmTemplatesQuery(P parameters) {
        super(parameters, VmEntityType.TEMPLATE);
    }

}
