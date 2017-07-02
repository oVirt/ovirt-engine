package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public abstract class AbstractBackendCpuProfileResource
        extends AbstractBackendSubResource<CpuProfile, org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> {

    protected AbstractBackendCpuProfileResource(String id) {
        super(id, CpuProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class);
    }

    protected CpuProfile get() {
        return performGet(QueryType.GetCpuProfileById, new IdQueryParameters(guid));
    }

    @Override
    protected CpuProfile addLinks(CpuProfile model,
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

    private org.ovirt.engine.core.common.businessentities.profiles.CpuProfile getCpuProfile(String id) {
        return getEntity(
            org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class,
            QueryType.GetCpuProfileById,
            new IdQueryParameters(asGuidOr404(id)),
            "CpuProfiles"
        );
    }

    public Response remove() {
        get();
        org.ovirt.engine.core.common.businessentities.profiles.CpuProfile cpuProfile = getCpuProfile(id);
        return performAction(
            ActionType.RemoveCpuProfile,
            new CpuProfileParameters(cpuProfile)
        );
    }
}
