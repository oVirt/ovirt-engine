package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public abstract class AbstractBackendDiskProfileResource
        extends AbstractBackendSubResource<DiskProfile, org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {

    protected AbstractBackendDiskProfileResource(String id) {
        super(id, DiskProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class);
    }

    protected DiskProfile get() {
        return performGet(QueryType.GetDiskProfileById, new IdQueryParameters(guid));
    }

    @Override
    protected DiskProfile addLinks(DiskProfile model,
            Class<? extends BaseResource> suggestedParent,
            String... subCollectionMembersToExclude) {
        if (model.isSetQos() && model.getQos().isSetId()) {
            QosBase qos = getEntity(QosBase.class,
                    QueryType.GetQosById,
                    new IdQueryParameters(asGuid(model.getQos().getId())),
                    "qos");
            model.getQos().setDataCenter(new DataCenter());
            model.getQos().getDataCenter().setId(qos.getStoragePoolId().toString());
        }
        return super.addLinks(model, suggestedParent, subCollectionMembersToExclude);
    }

    private org.ovirt.engine.core.common.businessentities.profiles.DiskProfile getDiskProfile(String id) {
        return getEntity(
            org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
            QueryType.GetDiskProfileById,
            new IdQueryParameters(asGuidOr404(id)),
            "DiskProfile"
        );
    }

    public Response remove() {
        get();
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile diskProfile = getDiskProfile(id);
        return performAction(
            ActionType.RemoveDiskProfile,
            new DiskProfileParameters(diskProfile)
        );
    }
}
