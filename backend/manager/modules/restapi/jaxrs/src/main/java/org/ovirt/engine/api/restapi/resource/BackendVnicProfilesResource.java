package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.api.resource.VnicProfileResource;
import org.ovirt.engine.api.resource.VnicProfilesResource;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVnicProfilesResource extends AbstractBackendVnicProfilesResource implements VnicProfilesResource {

    static final String[] SUB_COLLECTIONS = { "permissions" };

    public BackendVnicProfilesResource() {
        super(SUB_COLLECTIONS);
    }

    @Override
    public VnicProfiles list() {
        return performList();
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> getVnicProfilesCollection() {
        return getBackendCollection(VdcQueryType.GetAllVnicProfiles, new VdcQueryParametersBase());
    }

    public Response add(VnicProfile vnicProfile) {
        return super.add(vnicProfile);
    }

    @Override
    protected void validateParameters(VnicProfile vnicProfile) {
        validateParameters(vnicProfile, "name", "network.id");
    }

    @SingleEntityResource
    @Override
    public VnicProfileResource getVnicProfileSubResource(@PathParam("id") String id) {
        return inject(new BackendVnicProfileResource(id));
    }
}
