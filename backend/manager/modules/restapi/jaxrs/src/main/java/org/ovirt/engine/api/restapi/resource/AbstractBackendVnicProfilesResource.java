package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
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
        Map<Guid, Qos> qosMap = new HashMap<>();
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
    private void handleQosDataCenterLinks(Map<Guid, Qos> qosMap) {
        if (!qosMap.isEmpty()) {
            List<NetworkQoS> list = getBackendCollection(
                    NetworkQoS.class,
                    VdcQueryType.GetAllQosByType,
                    new QosQueryParameterBase(null, QosType.NETWORK));
            for (NetworkQoS networkQoS : list) {
                Qos qos = qosMap.get(networkQoS.getId());
                if (qos != null) {
                    qos.setDataCenter(new DataCenter());
                    qos.getDataCenter().setId(networkQoS.getStoragePoolId().toString());
                }
            }
        }
    }

    protected Response add(VnicProfile vnicProfile) {
        validateParameters(vnicProfile);
        AddVnicProfileParameters addVnicProfileParameters =
                new AddVnicProfileParameters(map(vnicProfile), !vnicProfile.isSetNetworkFilter());
        return performCreate(VdcActionType.AddVnicProfile,
                addVnicProfileParameters,
                new QueryIdResolver<Guid>(VdcQueryType.GetVnicProfileById, IdQueryParameters.class));
    }

    protected abstract void validateParameters(VnicProfile vnicProfile);

    protected abstract List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> getVnicProfilesCollection();

    protected VnicProfiles performList() {
        return mapCollection(getVnicProfilesCollection());
    }
}
