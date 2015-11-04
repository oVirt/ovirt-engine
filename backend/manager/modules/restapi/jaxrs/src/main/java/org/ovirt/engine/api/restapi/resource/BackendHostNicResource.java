package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResource.SUB_COLLECTIONS;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.LabelsResource;
import org.ovirt.engine.api.resource.NetworkAttachmentsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworksResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.RemoveBondParameters;
import org.ovirt.engine.core.common.action.UpdateHostNicVfsConfigParameters;
import org.ovirt.engine.core.common.action.UpdateNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.InterfaceAndIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicResource
    extends AbstractBackendActionableResource<HostNic, VdsNetworkInterface>
    implements HostNicResource {

    private BackendHostNicsResource parent;

    public BackendHostNicResource(String id, BackendHostNicsResource parent) {
        super(id, HostNic.class, VdsNetworkInterface.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    public BackendHostNicsResource getParent() {
        return parent;
    }

    @Override
    public HostNic get() {
        return parent.lookupNic(id, false);
    }

    @Override
    protected HostNic addParents(HostNic nic) {
        return parent.addParents(nic);
    }

    protected Response doAttachAction(Action action, VdcActionType actionType) {
        VdsNetworkInterface hostInterface = parent.lookupInterface(id);
        org.ovirt.engine.core.common.businessentities.network.Network network =
                action.getNetwork() == null ? null : parent.lookupNetwork(action.getNetwork());
        AttachNetworkToVdsParameters params = new AttachNetworkToVdsParameters(asGuid(parent.getHostId()),
                                                                               network,
                                                                               hostInterface);
        params.setBondingOptions(hostInterface.getBondOptions());

        // TODO: Delete the next block since it misuses the nic parameters
        if (network == null || network.getVlanId() == null) {
            params.setBootProtocol(hostInterface.getBootProtocol());
            params.setAddress(hostInterface.getAddress());
            params.setSubnet(hostInterface.getSubnet());
        }

        return doAction(actionType, params, action);
    }

    @Override
    public Response attach(Action action) {
        validateParameters(action, "network.id|name");
        return doAttachAction(action, VdcActionType.AttachNetworkToVdsInterface);
    }

    @Override
    public Response detach(Action action) {
        return doAttachAction(action, VdcActionType.DetachNetworkFromVdsInterface);
    }

    @Override
    public Response updateVirtualFunctionsConfig(Action action) {
        validateParameters(action, "virtualFunctionsConfiguration.numberOfVirtualFunctions|allNetworksAllowed");
        final HostNicVfsConfig vfsConfig = parent.findVfsConfig(guid);
        if (vfsConfig == null) {
            return notAllowed(localize(Messages.INVALID_OPERATION_ON_NON_SRIOV_NIC), guid.toString());
        }
        UpdateHostNicVfsConfigParameters params = prepareUpdateHostNicVfsConfigParameters(action, vfsConfig);
        return doAction(VdcActionType.UpdateHostNicVfsConfig, params, action);
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

    @SuppressWarnings("serial")
    @Override
    public HostNic update(HostNic nic) {
        validateEnums(HostNic.class, nic);
        VdsNetworkInterface originalInter = parent.lookupInterface(id);
        final VdsNetworkInterface inter = map(nic, originalInter);
        org.ovirt.engine.core.common.businessentities.network.Network oldNetwork = getOldNetwork(originalInter);
        org.ovirt.engine.core.common.businessentities.network.Network newNetwork = getNewNetwork(nic);
        UpdateNetworkToVdsParameters params =
            new UpdateNetworkToVdsParameters(Guid.createGuidFromStringDefaultEmpty(parent.getHostId()),
                                             newNetwork!=null ? newNetwork : oldNetwork ,
                                             new ArrayList<VdsNetworkInterface>(){{add(inter);}});

        params.setOldNetworkName(oldNetwork!=null ? oldNetwork.getName() : null);
        if(nic.isSetName() && inter.getBonded() != null && inter.getBonded()){
            params.setBondName(nic.getName());
        }
        if(nic.isSetIp()){
            if(nic.getIp().isSetAddress()){
                params.setAddress(nic.getIp().getAddress());
            }
            if(nic.getIp().isSetNetmask()){
                params.setSubnet(nic.getIp().getNetmask());
            }
            if(nic.getIp().isSetGateway()){
                params.setGateway(nic.getIp().getGateway());
            }
        }
        if(nic.isSetBootProtocol()){
            BootProtocol bootProtocol = BootProtocol.fromValue(nic.getBootProtocol());
            if(bootProtocol != null){
                params.setBootProtocol(map(bootProtocol, null));
            }
        }else if(nic.isSetIp() && nic.getIp().isSetAddress() && !nic.getIp().getAddress().isEmpty()){
            params.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        }
        if(nic.isSetBonding() && nic.getBonding().isSetOptions()){
           params.setBondingOptions(getBondingOptions(nic.getBonding().getOptions().getOptions()));
        }
        if(nic.isSetCheckConnectivity()){
            params.setCheckConnectivity(nic.isCheckConnectivity());
        }
        performAction(VdcActionType.UpdateNetworkToVdsInterface, params);

        return parent.lookupNic(id, true);
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
            List<VdsNetworkInterface> vlans = getBackendCollection(VdsNetworkInterface.class, VdcQueryType.GetAllChildVlanInterfaces, params);
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
                if(nwk.getName().equals(networkName)) return nwk;
            }
        }
        return null;
    }

    private String getBondingOptions(List<Option> options) {
        final StringBuilder bufOptions = new StringBuilder(options.size() * 32);
        for(Option opt : options){
            bufOptions.append(opt.getName() + "=" + opt.getValue() + " ");
        }
        return bufOptions.toString().substring(0, bufOptions.length() - 1);
    }

    private NetworkBootProtocol map(BootProtocol bootProtocol, NetworkBootProtocol template) {
        return getMapper(BootProtocol.class, NetworkBootProtocol.class).map(bootProtocol, template);
    }

    private org.ovirt.engine.core.common.businessentities.network.Network map(Network network, org.ovirt.engine.core.common.businessentities.network.Network template) {
        return getMapper(Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class).map(network, template);
    }

    @Override
    public LabelsResource getLabelsResource() {
        return inject(new BackendHostNicLabelsResource(asGuid(id), parent.getHostId()));
    }

    @Override
    public Response remove() {
        // No need to call "get" here, as the call to "lookupInterface" already generates a 404 error if there is no
        // such network interface.
        Guid hostId = asGuid(parent.getHostId());
        String nicName = parent.lookupInterface(id).getName();
        return performAction(VdcActionType.RemoveBond, new RemoveBondParameters(hostId, nicName));
    }

    @Override
    public NetworkAttachmentsResource getNetworkAttachmentsResource() {
        return inject(new BackendHostNicNetworkAttachmentsResource(asGuid(id), asGuid(parent.getHostId())));
    }

    @Override
    public LabelsResource getVfAllowedLabelsResource() {
        return inject(new BackendVirtualFunctionAllowedLabelsResource(guid, parent.getHostId()));
    }

    @Override
    public VirtualFunctionAllowedNetworksResource getVfAllowedNetworksResource() {
        return inject(new BackendVirtualFunctionAllowedNetworksResource(guid, parent.getHostId()));
    }
}
