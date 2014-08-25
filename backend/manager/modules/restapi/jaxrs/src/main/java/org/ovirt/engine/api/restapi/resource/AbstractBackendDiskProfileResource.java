package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public abstract class AbstractBackendDiskProfileResource
        extends AbstractBackendSubResource<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {

    protected AbstractBackendDiskProfileResource(String id, String... subCollections) {
        super(id, DiskProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
                subCollections);
    }

    protected DiskProfile get() {
        return performGet(VdcQueryType.GetDiskProfileById, new IdQueryParameters(guid));
    }

    @Override
    protected DiskProfile doPopulate(DiskProfile model,
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile entity) {
        return model;
    }

    @Override
    protected DiskProfile addLinks(DiskProfile model,
            Class<? extends BaseResource> suggestedParent,
            String... subCollectionMembersToExclude) {
        if (model.isSetQos() && model.getQos().isSetId()) {
            QosBase qos = getEntity(QosBase.class,
                    VdcQueryType.GetQosById,
                    new IdQueryParameters(asGuid(model.getQos().getId())),
                    "qos");
            model.getQos().setDataCenter(new DataCenter());
            model.getQos().getDataCenter().setId(qos.getStoragePoolId().toString());
        }
        return super.addLinks(model, suggestedParent, subCollectionMembersToExclude);
    }
}
