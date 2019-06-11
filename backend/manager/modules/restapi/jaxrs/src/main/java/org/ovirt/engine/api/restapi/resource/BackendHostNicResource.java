package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.LinkLayerDiscoveryProtocolResource;
import org.ovirt.engine.api.resource.NetworkAttachmentsResource;
import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworksResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateHostNicVfsConfigParameters;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.InterfaceAndIdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicResource
    extends AbstractBackendActionableResource<HostNic, VdsNetworkInterface>
    implements HostNicResource {

    private BackendHostNicsResource parent;

    public BackendHostNicResource(String id, BackendHostNicsResource parent) {
        super(id, HostNic.class, VdsNetworkInterface.class);
        this.parent = parent;
    }

    public BackendHostNicsResource getParent() {
        return parent;
    }

    @Override
    public HostNic get() {
        return parent.lookupNic(id);
    }

    @Override
    protected HostNic addParents(HostNic nic) {
        return parent.addParents(nic);
    }

    @Override
    public Response updateVirtualFunctionsConfiguration(Action action) {
        validateParameters(action, "virtualFunctionsConfiguration.numberOfVirtualFunctions|allNetworksAllowed");
        final HostNicVfsConfig vfsConfig = parent.findVfsConfig(guid);
        if (vfsConfig == null) {
            return notAllowed(localize(Messages.INVALID_OPERATION_ON_NON_SRIOV_NIC), guid.toString());
        }
        UpdateHostNicVfsConfigParameters params = prepareUpdateHostNicVfsConfigParameters(action, vfsConfig);
        return doAction(ActionType.UpdateHostNicVfsConfig, params, action);
    }

    private UpdateHostNicVfsConfigParameters prepareUpdateHostNicVfsConfigParameters(Action action,
            HostNicVfsConfig vfsConfig) {
        final Mapper<HostNicVfsConfig, UpdateHostNicVfsConfigParameters> entityMapper =
                getMapper(HostNicVfsConfig.class, UpdateHostNicVfsConfigParameters.class);
        UpdateHostNicVfsConfigParameters params = entityMapper.map(vfsConfig, new UpdateHostNicVfsConfigParameters());
        final Mapper<org.ovirt.engine.api.model.HostNicVirtualFunctionsConfiguration, UpdateHostNicVfsConfigParameters>
                userInputMapper =
                getMapper(org.ovirt.engine.api.model.HostNicVirtualFunctionsConfiguration.class,
                        UpdateHostNicVfsConfigParameters.class);
        userInputMapper.map(action.getVirtualFunctionsConfiguration(), params);
        return params;
    }

    private Response notAllowed(String reason, String detail) {
        return Response.status(405).entity(fault(reason, detail)).build();
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver<Guid> resolver = new EntityIdResolver<Guid>() {
            @Override
            public VdsNetworkInterface lookupEntity(Guid guid) throws BackendFailureException {
                return parent.lookupInterface(id);
            }
        };
        HostNicStatisticalQuery query = new HostNicStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
    }

    @Override
    protected HostNic doPopulate(HostNic model, VdsNetworkInterface entity) {
        return parent.doPopulate(model, entity);
    }

    @Override
    protected HostNic deprecatedPopulate(HostNic model, VdsNetworkInterface entity) {
        return parent.deprecatedPopulate(model, entity);
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNewNetwork(HostNic nic) {
        org.ovirt.engine.core.common.businessentities.network.Network newNetwork = null;
        if(nic.isSetNetwork()){
            newNetwork = map(nic.getNetwork(), parent.lookupClusterNetwork(nic.getNetwork()));
        }
        return newNetwork;
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getOldNetwork(VdsNetworkInterface originalInter) {
        String oldNetworkName = originalInter.getNetworkName();
        if (!StringUtils.isEmpty(oldNetworkName)) {
            return lookupAtachedNetwork(originalInter.getNetworkName());
        } else {
            InterfaceAndIdQueryParameters params = new InterfaceAndIdQueryParameters(
                                                                    originalInter.getVdsId(),
                                                                    originalInter);
            List<VdsNetworkInterface> vlans = getBackendCollection(VdsNetworkInterface.class, QueryType.GetAllChildVlanInterfaces, params);
            if (vlans!=null && !vlans.isEmpty()) {
                return lookupAtachedNetwork(vlans.get(0).getNetworkName());
            } else {
                return null;
            }
        }
    }

    private org.ovirt.engine.core.common.businessentities.network.Network lookupAtachedNetwork(String networkName) {
        if(!StringUtils.isEmpty(networkName)){
            for(org.ovirt.engine.core.common.businessentities.network.Network nwk : parent.getClusterNetworks()){
                if(nwk.getName().equals(networkName)) {
                    return nwk;
                }
            }
        }
        return null;
    }

    private org.ovirt.engine.core.common.businessentities.network.Network map(Network network, org.ovirt.engine.core.common.businessentities.network.Network template) {
        return getMapper(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class).map(network, template);
    }

    @Override
    public NetworkLabelsResource getNetworkLabelsResource() {
        return inject(new BackendHostNicLabelsResource(asGuid(id), parent.getHostId()));
    }

    @Override
    public NetworkAttachmentsResource getNetworkAttachmentsResource() {
        return inject(new BackendHostNicNetworkAttachmentsResource(asGuid(id), asGuid(parent.getHostId())));
    }

    @Override
    public NetworkLabelsResource getVirtualFunctionAllowedLabelsResource() {
        return inject(new BackendVirtualFunctionAllowedLabelsResource(guid, parent.getHostId()));
    }

    @Override
    public VirtualFunctionAllowedNetworksResource getVirtualFunctionAllowedNetworksResource() {
        return inject(new BackendVirtualFunctionAllowedNetworksResource(guid, parent.getHostId()));
    }

    @Override
    public LinkLayerDiscoveryProtocolResource getLinkLayerDiscoveryProtocolElementsResource() {
        return inject(new BackendLinkLayerDiscoveryProtocolResource(guid));
    }
}
