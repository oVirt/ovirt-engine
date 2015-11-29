package org.ovirt.engine.core.bll.storage.disk.image;

import org.ovirt.engine.core.bll.GetAllTemplateBasedEntityQuery;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllImageTypesQuery<P extends VdcQueryParametersBase> extends GetAllTemplateBasedEntityQuery<P> {

    public GetAllImageTypesQuery(P parameters) {
        super(parameters, VmEntityType.INSTANCE_TYPE);
    }

}
