package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public abstract class AbstractBackendVnicProfileResource
        extends AbstractBackendSubResource<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile> {

    protected AbstractBackendVnicProfileResource(String id) {
        super(id, VnicProfile.class,
                org.ovirt.engine.core.common.businessentities.network.VnicProfile.class,
                AbstractBackendVnicProfilesResource.SUB_COLLECTIONS);
    }

    protected VnicProfile get() {
        return performGet(VdcQueryType.GetVnicProfileById, new IdQueryParameters(guid));
    }

    protected AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                VnicProfile.class,
                VdcObjectType.VnicProfile));
    }

    @Override
    protected VnicProfile addLinks(VnicProfile model,
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

    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVnicProfile, new VnicProfileParameters(getVnicProfile(id)));
    }

    protected org.ovirt.engine.core.common.businessentities.network.VnicProfile getVnicProfile(String id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.network.VnicProfile.class,
                VdcQueryType.GetVnicProfileById,
                new IdQueryParameters(guid),
                "VnicProfiles");
    }
}
