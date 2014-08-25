package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendVnicProfilesResource
        extends AbstractBackendCollectionResource<VnicProfile, org.ovirt.engine.core.common.businessentities.network.VnicProfile> {

    static final String[] SUB_COLLECTIONS = { "permissions" };

    public AbstractBackendVnicProfilesResource() {
        super(VnicProfile.class,
                org.ovirt.engine.core.common.businessentities.network.VnicProfile.class,
                SUB_COLLECTIONS);
    }

    protected VnicProfiles mapCollection(List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> entities) {
        VnicProfiles collection = new VnicProfiles();
        Map<Guid, QoS> qosMap = new HashMap<>();
        for (org.ovirt.engine.core.common.businessentities.network.VnicProfile entity : entities) {
            VnicProfile profile = populate(map(entity), entity);
            collection.getVnicProfiles().add(profile);
            if (entity.getNetworkQosId() != null) {
                qosMap.put(entity.getNetworkQosId(), profile.getQos());
            }
        }

        handleQosDataCenterLinks(qosMap);
        for (VnicProfile vnicProfile : collection.getVnicProfiles()) {
            addLinks(vnicProfile);
        }
        return collection;
    }

    /**
     * used to set qos's href (requires dc id).
     */
    private void handleQosDataCenterLinks(Map<Guid, QoS> qosMap) {
        if (!qosMap.isEmpty()) {
            List<NetworkQoS> list = getBackendCollection(
                    NetworkQoS.class,
                    VdcQueryType.GetAllQosByType,
                    new QosQueryParameterBase(null, QosType.NETWORK));
            for (NetworkQoS networkQoS : list) {
                QoS qos = qosMap.get(networkQoS.getId());
                if (qos != null) {
                    qos.setDataCenter(new DataCenter());
                    qos.getDataCenter().setId(networkQoS.getStoragePoolId().toString());
                }
            }
        }
    }

    protected Response add(VnicProfile vnicProfile) {
        validateParameters(vnicProfile);
        return performCreate(VdcActionType.AddVnicProfile,
                new VnicProfileParameters(map(vnicProfile)),
                new QueryIdResolver<Guid>(VdcQueryType.GetVnicProfileById, IdQueryParameters.class));
    }

    protected abstract void validateParameters(VnicProfile vnicProfile);

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVnicProfile, new VnicProfileParameters(getVnicProfile(id)));
    }

    protected org.ovirt.engine.core.common.businessentities.network.VnicProfile getVnicProfile(String id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.network.VnicProfile.class,
                VdcQueryType.GetVnicProfileById,
                new IdQueryParameters(asGuidOr404(id)),
                "VnicProfiles");
    }

    @Override
    protected VnicProfile doPopulate(VnicProfile model,
            org.ovirt.engine.core.common.businessentities.network.VnicProfile entity) {
        return model;
    }

    protected abstract List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> getVnicProfilesCollection();

    protected VnicProfiles performList() {
        return mapCollection(getVnicProfilesCollection());
    }
}
